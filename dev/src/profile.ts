import { cpSync, existsSync, mkdirSync, readFileSync, readdirSync, rmSync, statSync, writeFileSync } from 'node:fs';
import { basename, dirname, join, resolve } from 'node:path';
import { parse, stringify } from 'smol-toml';
import { parseArgs } from 'node:util';
import type { Command, Ctx } from './command.ts';
import { type DevConfig, type PluginConfig, type Platform, loadConfig } from './config.ts';
import { appliedChangelogs, dbReachable, dumpDatabase, restoreDump } from './db.ts';
import { readResolved, setup } from './downloads.ts';
import { isRunning } from './supervisor.ts';
import { askText, confirmAction, interactive, pickFrom, pickMany, searchFrom } from './prompt.ts';
import { instancesOf } from './templates.ts';
import { CACHE, DEV, DevError, ROOT, RUN, TEMPLATES_LOCAL, capture, fail, log, warn } from './util.ts';

/**
 * Profiles: one .tar.gz that carries a whole dev setup to another machine.
 *
 *   profile.toml                 manifest (format, versions, pins, what is included)
 *   config/dev.local.toml        effective settings (servers, jvm, ui, plugin pins) merged over the importer's dev.toml
 *   config/templates.local/      config overrides (see `dev override`)
 *   plugins/<platform>/<key>.jar jars that cannot be re-downloaded exactly (or all, with --embed-jars)
 *   db/<database>.dump           pg_dump -Fc
 *   worlds/<server>.tar          world directories of a stopped server
 *
 * The importer treats the archive as untrusted input: every entry is validated before extraction and a review
 * screen lists the things that can run code (plugin download hosts, JVM flags, first-start commands).
 */

const FORMAT = 1;
const PROFILES = join(DEV, 'profiles');
const SECTIONS = ['plugins', 'servers', 'overrides', 'db', 'worlds'] as const;
type Section = (typeof SECTIONS)[number];
const JAR_DIR = join('dev', '.cache', 'profile-jars');
const LOCAL_TOML = join(DEV, 'dev.local.toml');
const ALLOWED_TOP = ['profile.toml', 'config', 'plugins', 'db', 'worlds'];
const WORLD_DIRS = (name: string) => name === 'world' || name.startsWith('world_');

interface Manifest {
  format: number;
  name: string;
  description: string;
  created_at: string;
  created_by: string;
  source_commit: string;
  includes: Section[];
  servers?: {
    minecraft: string;
    purpur_build: string;
    velocity_version: string;
    velocity_build: string;
    limbo_version?: string;
    data_version?: number;
  };
  plugins?: { key: string; platform: Platform; version: string; url?: string; sha256: string; embedded: boolean }[];
  db?: { changelogs: string[]; databases: string[] };
  worlds?: { server: string; file: string; dirs: string[] }[];
}

type Json = Record<string, unknown>;

function git(args: string[]): string {
  return capture('git', args).stdout.trim();
}

function tar(args: string[]): string {
  const result = capture('tar', args);
  if (result.status !== 0) fail(`tar ${args.join(' ')} failed: ${result.stderr.trim()}`);
  return result.stdout;
}

function stamp(): string {
  return new Date().toISOString().replace(/[:.]/g, '-');
}

// ---------------------------------------------------------------------------------------------------------------
// Export
// ---------------------------------------------------------------------------------------------------------------

export interface ExportOptions {
  name: string;
  include: Section[];
  worlds: string[];
  embedJars: boolean;
  out?: string;
  description?: string;
}

function worldDirsOf(server: string): string[] {
  const dir = join(RUN, server);
  return existsSync(dir)
    ? readdirSync(dir, { withFileTypes: true })
        .filter((entry) => entry.isDirectory() && WORLD_DIRS(entry.name))
        .map((entry) => entry.name)
    : [];
}

/** The settings a profile carries, from the effective config. Machine-specific keys never leave the machine, and `share` (tunnel tokens) is never included. */
function profileSettings(config: DevConfig, resolved: Awaited<ReturnType<typeof readResolved>>): Json {
  const { public_port: _p, ...proxy } = config.proxy;
  const { public_port: _l, ...limbo } = config.limbo;
  return {
    minecraft_version: resolved.minecraft,
    purpur_build: resolved.purpur.build,
    velocity_version: resolved.velocity.version,
    velocity_build: resolved.velocity.build,
    ...(resolved.limbo ? { limbo_version: resolved.limbo.version } : {}),
    online_mode: config.online_mode,
    jvm: config.jvm,
    proxy,
    limbo,
    ui: config.ui,
    servers: config.servers,
  };
}

export async function exportProfile(config: DevConfig, options: ExportOptions): Promise<string> {
  const include = new Set(options.include);
  const stage = join(CACHE, `profile-${stamp()}`);
  rmSync(stage, { recursive: true, force: true });
  mkdirSync(stage, { recursive: true });
  const resolved = await readResolved();
  if (resolved.plugins.some((plugin) => !plugin.sha256)) {
    fail('dev/.cache/resolved.json was written by an older version of the dev tool; run `dev setup` and export again');
  }

  const manifest: Manifest = {
    format: FORMAT,
    name: options.name,
    description: options.description ?? '',
    created_at: new Date().toISOString(),
    created_by: git(['config', 'user.name']) || 'unknown',
    source_commit: `${git(['rev-parse', 'HEAD'])}${git(['status', '--porcelain']) ? ' (dirty)' : ''}`,
    includes: SECTIONS.filter((section) => include.has(section)),
  };
  const settings: Json = {};

  if (include.has('servers')) {
    Object.assign(settings, profileSettings(config, resolved));
    manifest.servers = {
      minecraft: resolved.minecraft,
      purpur_build: resolved.purpur.build,
      velocity_version: resolved.velocity.version,
      velocity_build: resolved.velocity.build,
      limbo_version: resolved.limbo?.version,
    };
    try {
      const { dataVersion } = await import('./mc/dataversion.ts');
      manifest.servers.data_version = await dataVersion(config);
    } catch {
      // informational only
    }
  }

  if (include.has('plugins')) {
    manifest.plugins = [];
    const pluginConfigs: PluginConfig[] = [];
    for (const plugin of config.plugins) {
      const pinned: NonNullable<PluginConfig['pinned']> = {};
      for (const platform of plugin.platforms) {
        const entry = resolved.plugins.find((candidate) => candidate.key === plugin.key && candidate.platform === platform);
        if (!entry) {
          warn(`${plugin.key} (${platform}) is not downloaded; run \`dev setup\`. It is left unpinned.`);
          continue;
        }
        // Jars that cannot be fetched again byte for byte have to travel inside the archive
        const embed = options.embedJars || !entry.immutable || !entry.url;
        if (embed) {
          const dest = join(stage, 'plugins', platform, `${plugin.key}.jar`);
          mkdirSync(dirname(dest), { recursive: true });
          cpSync(entry.file, dest);
        }
        pinned[platform] = {
          url: entry.url ?? '',
          sha256: entry.sha256,
          version: entry.version,
          ...(embed ? { file: join(JAR_DIR, platform, `${plugin.key}.jar`) } : {}),
        };
        manifest.plugins.push({
          key: plugin.key,
          platform,
          version: entry.version,
          url: entry.url,
          sha256: entry.sha256,
          embedded: embed,
        });
      }
      pluginConfigs.push({ ...plugin, pinned });
    }
    settings.plugins = pluginConfigs;
  }

  if (include.has('overrides') && existsSync(TEMPLATES_LOCAL)) {
    cpSync(TEMPLATES_LOCAL, join(stage, 'config', 'templates.local'), { recursive: true });
  }

  if (include.has('db')) {
    if (!dbReachable()) fail('PostgreSQL is not reachable; cannot export the database');
    mkdirSync(join(stage, 'db'), { recursive: true });
    const databases = ['paradisu'];
    dumpDatabase(join(stage, 'db', 'paradisu.dump'), 'paradisu');
    if (capture('psql', ['-X', '-tAc', "select 1 from pg_database where datname = 'luckperms'"], { env: { PGDATABASE: 'postgres' } }).stdout.trim() === '1') {
      dumpDatabase(join(stage, 'db', 'luckperms.dump'), 'luckperms');
      databases.push('luckperms');
    }
    manifest.db = { changelogs: appliedChangelogs().map((file) => basename(file, '.xml')), databases };
  }

  if (include.has('worlds')) {
    manifest.worlds = [];
    for (const server of options.worlds) {
      if (isRunning(instancesOf(config).find((i) => i.name === server)!)) {
        fail(`Stop ${server} before exporting its world (the files change while it runs)`);
      }
      const dirs = worldDirsOf(server);
      if (dirs.length === 0) {
        warn(`${server} has no world directories; skipped`);
        continue;
      }
      mkdirSync(join(stage, 'worlds'), { recursive: true });
      tar(['-cf', join(stage, 'worlds', `${server}.tar`), '--exclude=session.lock', '-C', join(RUN, server), ...dirs]);
      manifest.worlds.push({ server, file: `worlds/${server}.tar`, dirs });
    }
  }

  if (Object.keys(settings).length > 0) {
    mkdirSync(join(stage, 'config'), { recursive: true });
    writeFileSync(join(stage, 'config', 'dev.local.toml'), stringify(settings));
  }
  writeFileSync(join(stage, 'profile.toml'), stringify(manifest as unknown as Json));

  mkdirSync(PROFILES, { recursive: true });
  const out = resolve(ROOT, options.out ?? join(PROFILES, `${options.name}.tar.gz`));
  mkdirSync(dirname(out), { recursive: true });
  tar(['-czf', out, '-C', stage, '.']);
  rmSync(stage, { recursive: true, force: true });
  log(`Exported ${out} (${(statSync(out).size / 1024 / 1024).toFixed(1)} MB; includes: ${manifest.includes.join(', ') || 'nothing'})`);
  return out;
}

// ---------------------------------------------------------------------------------------------------------------
// Inspect / import
// ---------------------------------------------------------------------------------------------------------------

function readManifest(file: string): Manifest {
  let text = capture('tar', ['-xzf', file, '-O', './profile.toml']);
  if (text.status !== 0) text = capture('tar', ['-xzf', file, '-O', 'profile.toml']);
  if (text.status !== 0) fail(`${file} is not a profile (no profile.toml)`);
  const manifest = parse(text.stdout) as unknown as Manifest;
  if (manifest.format === undefined) fail(`${file} has no format version`);
  if (manifest.format > FORMAT) fail(`${file} uses profile format ${manifest.format}; update your checkout of the dev tool`);
  return manifest;
}

/** Rejects absolute paths, `..`, links and devices; `allowed` limits the first path segment. */
function validateArchive(file: string, allowedTop: string[] | null, label: string): void {
  const names = tar(['-tzf', file]).split('\n').filter(Boolean);
  const types = tar(['-tvzf', file]).split('\n').filter(Boolean).map((line) => line[0]);
  validateEntries(names, types, allowedTop, label);
}

function validateTarArchive(file: string, label: string): void {
  const names = tar(['-tf', file]).split('\n').filter(Boolean);
  const types = tar(['-tvf', file]).split('\n').filter(Boolean).map((line) => line[0]);
  validateEntries(names, types, null, label, (top) => WORLD_DIRS(top));
}

function validateEntries(
  names: string[],
  types: (string | undefined)[],
  allowedTop: string[] | null,
  label: string,
  topOk?: (top: string) => boolean,
): void {
  names.forEach((rawName, i) => {
    const name = rawName.replace(/^\.\//, '');
    if (name === '' || name === '.') return;
    const segments = name.split('/').filter(Boolean);
    if (name.startsWith('/') || segments.includes('..')) throw new DevError(`${label}: unsafe path "${rawName}"`);
    if (types[i] !== '-' && types[i] !== 'd') throw new DevError(`${label}: "${rawName}" is a link or special file`);
    const top = segments[0]!;
    if (allowedTop && !allowedTop.includes(top)) throw new DevError(`${label}: unexpected entry "${rawName}"`);
    if (topOk && !topOk(top)) throw new DevError(`${label}: unexpected entry "${rawName}"`);
  });
}

export async function inspectProfile(file: string): Promise<Manifest> {
  validateArchive(file, ALLOWED_TOP, 'profile');
  const manifest = readManifest(file);
  log(stringify(manifest as unknown as Json));
  log('Contents:');
  log(tar(['-tvzf', file]).trimEnd());
  return manifest;
}

/** Changelog numbers in this checkout, read from migrations.xml (no Gradle build needed). */
function localChangelogs(): string[] {
  const master = readFileSync(join(ROOT, 'database', 'src', 'main', 'resources', 'db', 'migrations.xml'), 'utf8');
  return [...master.matchAll(/file="[^"]*?([\w-]+)\.xml"/g)].map((match) => match[1]!);
}

function hostOf(url: string): string {
  try {
    return new URL(url).host;
  } catch {
    return url;
  }
}

function reviewText(manifest: Manifest, settings: Json | undefined, extractDir: string): string {
  const lines = [
    `Profile "${manifest.name}" by ${manifest.created_by} (${manifest.created_at.slice(0, 10)}, commit ${manifest.source_commit})`,
  ];
  if (manifest.description) lines.push(`  ${manifest.description}`);
  lines.push(`It will replace: ${manifest.includes.join(', ') || 'nothing'}`);
  if (manifest.servers) {
    lines.push(`Servers: Minecraft ${manifest.servers.minecraft}, Purpur build ${manifest.servers.purpur_build}, Velocity ${manifest.servers.velocity_version} build ${manifest.servers.velocity_build}`);
  }
  if (manifest.plugins) {
    const hosts = new Set(manifest.plugins.filter((p) => !p.embedded && p.url).map((p) => hostOf(p.url!)));
    lines.push(`Plugins: ${manifest.plugins.length} jars; downloads from ${[...hosts].join(', ') || 'nowhere'}; ${manifest.plugins.filter((p) => p.embedded).length} embedded in the archive`);
  }
  const flags = (settings?.jvm as { extra_flags?: string[] } | undefined)?.extra_flags ?? [];
  if (flags.length > 0) lines.push(`JVM flags that will be used: ${flags.join(' ')}`);
  const commands: string[] = [];
  const collect = (owner: string, value: unknown) => {
    const list = (value as { first_start_commands?: string[] } | undefined)?.first_start_commands ?? [];
    for (const command of list) commands.push(`${owner}: ${command}`);
  };
  collect('proxy', settings?.proxy);
  collect('limbo', settings?.limbo);
  for (const server of (settings?.servers as { name: string }[] | undefined) ?? []) collect(server.name, server);
  if (commands.length > 0) lines.push(`First-start console commands:\n    ${commands.join('\n    ')}`);
  if (manifest.db) lines.push(`Database: ${manifest.db.databases.join(', ')} at changelogs ${manifest.db.changelogs.join(', ')} (replaces the current data)`);
  if (manifest.worlds) lines.push(`Worlds: ${manifest.worlds.map((w) => `${w.server} (${w.dirs.join(', ')})`).join('; ')} (replaces the current worlds)`);
  void extractDir;
  return lines.join('\n');
}

export interface ImportOptions {
  include?: Section[];
  yes: boolean;
  force: boolean;
}

export async function importProfile(ctx: Ctx, file: string, options: ImportOptions): Promise<void> {
  if (!existsSync(file)) fail(`No such file: ${file}`);
  validateArchive(file, ALLOWED_TOP, 'profile');
  const manifest = readManifest(file);
  const wanted = new Set<Section>(options.include ?? manifest.includes);
  for (const section of wanted) {
    if (!manifest.includes.includes(section)) fail(`The profile does not include "${section}" (it has: ${manifest.includes.join(', ')})`);
  }

  const extract = join(CACHE, `import-${stamp()}`);
  mkdirSync(extract, { recursive: true });
  tar(['-xzf', file, '-C', extract, '--no-same-owner', '--no-same-permissions']);
  try {
    const settingsPath = join(extract, 'config', 'dev.local.toml');
    const settings = existsSync(settingsPath) ? (parse(readFileSync(settingsPath, 'utf8')) as Json) : undefined;
    for (const world of manifest.worlds ?? []) {
      if (!/^[A-Za-z0-9_-]+$/.test(world.server)) fail(`Unsafe server name "${world.server}" in the profile`);
      validateTarArchive(join(extract, world.file), `worlds/${world.server}`);
    }

    log(reviewText(manifest, settings, extract));
    if (wanted.has('db') && manifest.db) {
      const local = new Set(localChangelogs());
      const missing = manifest.db.changelogs.filter((c) => !local.has(c));
      if (missing.length > 0 && !options.force) {
        fail(`The profile's database has changelogs this checkout lacks (${missing.join(', ')}). Update your checkout, or pass --force.`);
      }
    }
    if (!(await confirmAction('Apply this profile? Your current settings are backed up first.', options.yes))) {
      log('Aborted');
      return;
    }
    await ctx.stopAll();

    // Back up what is about to be replaced
    const backup = join(DEV, '.profile-backup', stamp());
    mkdirSync(backup, { recursive: true });
    if (existsSync(LOCAL_TOML)) cpSync(LOCAL_TOML, join(backup, 'dev.local.toml'));
    if (existsSync(TEMPLATES_LOCAL)) cpSync(TEMPLATES_LOCAL, join(backup, 'templates.local'), { recursive: true });

    const touchesSettings = wanted.has('servers') || wanted.has('plugins');
    if (touchesSettings && settings) {
      const current = existsSync(LOCAL_TOML) ? (parse(readFileSync(LOCAL_TOML, 'utf8')) as Json) : {};
      const next: Json = {};
      // Settings about this machine stay as they are
      for (const key of ['accept_eula', 'public_host', 'web', 'share']) {
        if (key in current) next[key] = current[key];
      }
      next.accept_eula ??= ctx.config.accept_eula;
      for (const [key, value] of Object.entries(settings)) {
        if (key === 'plugins' && !wanted.has('plugins')) continue;
        if (key !== 'plugins' && !wanted.has('servers')) continue;
        next[key] = value;
      }
      writeFileSync(LOCAL_TOML, stringify(next));
    }

    if (wanted.has('plugins')) {
      const jars = join(extract, 'plugins');
      if (existsSync(jars)) {
        rmSync(join(ROOT, JAR_DIR), { recursive: true, force: true });
        cpSync(jars, join(ROOT, JAR_DIR), { recursive: true });
      }
    }

    if (wanted.has('overrides')) {
      rmSync(TEMPLATES_LOCAL, { recursive: true, force: true });
      const overrides = join(extract, 'config', 'templates.local');
      if (existsSync(overrides)) cpSync(overrides, TEMPLATES_LOCAL, { recursive: true });
    }

    if (wanted.has('db') && manifest.db) {
      for (const database of manifest.db.databases) {
        if (!/^[A-Za-z0-9_]+$/.test(database)) fail(`Unsafe database name "${database}" in the profile`);
        log(`Restoring database ${database}`);
        restoreDump(join(extract, 'db', `${database}.dump`), database);
      }
    }

    if (wanted.has('worlds') && manifest.worlds) {
      for (const world of manifest.worlds) {
        const dir = join(RUN, world.server);
        mkdirSync(dir, { recursive: true });
        for (const existing of worldDirsOf(world.server)) rmSync(join(dir, existing), { recursive: true, force: true });
        rmSync(join(dir, '.dev-first-start-done'), { force: true });
        tar(['-xf', join(extract, world.file), '-C', dir, '--no-same-owner', '--no-same-permissions']);
        log(`Restored world of ${world.server}`);
      }
    }

    if (touchesSettings) {
      await setup(await loadConfig(), false);
    }
    log(`Imported "${manifest.name}". Run \`dev start\`. Your previous settings are in ${backup}`);
  } finally {
    rmSync(extract, { recursive: true, force: true });
  }
}

// ---------------------------------------------------------------------------------------------------------------
// dev override
// ---------------------------------------------------------------------------------------------------------------

const SKIP_DIRS = new Set(['world', 'logs', 'libraries', 'cache', 'versions', '.dev', 'crash-reports']);

function listFiles(root: string, dir = '', depth = 0): string[] {
  if (depth > 4) return [];
  return readdirSync(join(root, dir), { withFileTypes: true }).flatMap((entry) => {
    const relative = join(dir, entry.name);
    if (entry.isDirectory()) return SKIP_DIRS.has(entry.name) || entry.name.startsWith('world_') ? [] : listFiles(root, relative, depth + 1);
    return /\.(jar|dat|dat_old|lock|log|gz|mca)$/.test(entry.name) || entry.name.startsWith('.') ? [] : [relative];
  });
}

/** Where a file of an instance lives in dev/templates.local so the next start re-applies it. */
function overrideTarget(config: DevConfig, instanceName: string, path: string): string {
  const instance = instancesOf(config).find((candidate) => candidate.name === instanceName) ?? fail(`Unknown instance "${instanceName}"`);
  const plugin = /^plugins\/([^/]+)\/(.+)$/.exec(path);
  if (plugin) {
    const owner = config.plugins.find((candidate) => candidate.config_dir?.[instance.platform] === plugin[1]);
    if (owner) return join(TEMPLATES_LOCAL, 'plugins', owner.key, instance.platform, plugin[2]!);
  }
  return join(TEMPLATES_LOCAL, 'servers', instance.name, path);
}

async function overrideCommand(config: DevConfig, args: string[]): Promise<void> {
  const instances = instancesOf(config);
  const name = args[0] ?? (await pickFrom('Which instance?', instances.map((i) => ({ name: i.name, value: i.name })), 'An instance name'));
  const instance = instances.find((candidate) => candidate.name === name) ?? fail(`Unknown instance "${name}"`);
  if (!existsSync(instance.dir)) fail(`${name} has not been started yet (no ${instance.dir})`);
  const path =
    args[1] ??
    (await searchFrom('Which file?', (term) =>
      listFiles(instance.dir)
        .filter((file) => file.toLowerCase().includes(term.toLowerCase()))
        .slice(0, 30)
        .map((file) => ({ name: file, value: file })),
    ));
  const source = resolve(instance.dir, path);
  if (!source.startsWith(`${instance.dir}/`) || !existsSync(source) || !statSync(source).isFile()) {
    fail(`${path} is not a file inside ${instance.dir}`);
  }
  const relative = source.slice(instance.dir.length + 1);
  if (readFileSync(source, 'utf8').includes('${')) {
    warn(`${relative} contains \${...}; templates substitute those tokens when applied`);
  }
  const target = overrideTarget(config, name, relative);
  mkdirSync(dirname(target), { recursive: true });
  cpSync(source, target);
  log(`Saved to ${target.replace(`${ROOT}/`, '')}; it is applied on every start and included in profile exports`);
}

// ---------------------------------------------------------------------------------------------------------------
// Commands
// ---------------------------------------------------------------------------------------------------------------

function parseSections(value: string | undefined): Section[] | undefined {
  if (!value) return undefined;
  const sections = value.split(',').map((s) => s.trim()).filter(Boolean);
  for (const s of sections) {
    if (!(SECTIONS as readonly string[]).includes(s)) fail(`Unknown section "${s}" (valid: ${SECTIONS.join(', ')})`);
  }
  return sections as Section[];
}

function profileFiles(): string[] {
  return existsSync(PROFILES) ? readdirSync(PROFILES).filter((f) => f.endsWith('.tar.gz')).sort() : [];
}

async function pickProfile(arg: string | undefined): Promise<string> {
  if (arg) {
    const direct = resolve(ROOT, arg);
    if (existsSync(direct)) return direct;
    const named = join(PROFILES, arg.endsWith('.tar.gz') ? arg : `${arg}.tar.gz`);
    if (existsSync(named)) return named;
    fail(`No profile "${arg}"`);
  }
  const files = profileFiles();
  const choice = await pickFrom('Which profile?', files.map((f) => ({ name: f, value: join(PROFILES, f) })), 'A profile file');
  return choice;
}

export const COMMANDS: Command[] = [
  {
    name: 'override',
    usage: '<instance> <path-in-instance>',
    summary: 'Keep a changed runtime config file: re-applied on each start and exported with profiles',
    run: ({ config }, args) => overrideCommand(config, args),
  },
  {
    name: 'profile export',
    usage: '[name] [--include plugins,servers,overrides,db,worlds] [--worlds a,b] [--embed-jars] [--out file]',
    summary: 'Pack the dev setup into a shareable .tar.gz (default: everything except worlds)',
    run: async (ctx, args) => {
      const { values, positionals } = parseArgs({
        args,
        options: { include: { type: 'string' }, worlds: { type: 'string' }, 'embed-jars': { type: 'boolean' }, out: { type: 'string' }, description: { type: 'string' } },
        allowPositionals: true,
      });
      const name = positionals[0] ?? (await askText('Profile name', { default: 'my-profile', validate: (v) => /^[A-Za-z0-9._-]+$/.test(v) || 'Use letters, digits, . _ -' }, 'A profile name'));
      if (!/^[A-Za-z0-9._-]+$/.test(name)) fail('The profile name may only contain letters, digits, . _ -');
      let include = parseSections(values.include);
      let worlds = values.worlds?.split(',').filter(Boolean) ?? [];
      let embedJars = values['embed-jars'] ?? false;
      const withWorlds = instancesOf(ctx.config).filter((i) => i.platform === 'paper' && worldDirsOf(i.name).length > 0);
      if (!include) {
        include = SECTIONS.filter((s) => s !== 'worlds' || worlds.length > 0);
        if (interactive()) {
          include = await pickMany('What should the profile include?', [
            { name: 'plugins   exact plugin versions (re-downloaded on import)', value: 'plugins' as Section, checked: true },
            { name: 'servers   Minecraft/Purpur/Velocity builds and server setup', value: 'servers' as Section, checked: true },
            { name: 'overrides config overrides (dev override)', value: 'overrides' as Section, checked: true },
            { name: 'db        database contents', value: 'db' as Section, checked: true },
            { name: 'worlds    world files (can be large)', value: 'worlds' as Section, checked: false },
          ]);
          if (include.includes('plugins') && !args.includes('--embed-jars')) {
            embedJars = await confirmAction('Embed every plugin jar in the archive (larger, but works if upstream disappears)?', false);
          }
        }
      }
      if (worlds.length > 0 && !include.includes('worlds')) include = [...include, 'worlds'];
      if (include.includes('worlds') && worlds.length === 0) {
        worlds = interactive()
          ? await pickMany('Export the worlds of which servers?', withWorlds.map((i) => ({ name: i.name, value: i.name, checked: true })))
          : withWorlds.map((i) => i.name);
      }
      await exportProfile(ctx.config, { name, include, worlds, embedJars, out: values.out, description: values.description });
    },
  },
  {
    name: 'profile import',
    usage: '<file|name> [--include a,b] [--yes] [--force]',
    summary: 'Apply a profile (reviews it first; backs up your settings to dev/.profile-backup)',
    run: async (ctx, args) => {
      const { values, positionals } = parseArgs({
        args,
        options: { include: { type: 'string' }, yes: { type: 'boolean' }, force: { type: 'boolean' } },
        allowPositionals: true,
      });
      const file = await pickProfile(positionals[0]);
      await importProfile(ctx, file, { include: parseSections(values.include), yes: values.yes ?? false, force: values.force ?? false });
    },
  },
  {
    name: 'profile inspect',
    usage: '<file|name>',
    summary: 'Show what a profile contains without applying it',
    run: async (_ctx, args) => {
      await inspectProfile(await pickProfile(args[0]));
    },
  },
  {
    name: 'profile list',
    usage: '',
    summary: 'List profiles in dev/profiles',
    run: () => {
      const files = profileFiles();
      if (files.length === 0) log('No profiles in dev/profiles');
      for (const file of files) {
        const path = join(PROFILES, file);
        try {
          const manifest = readManifest(path);
          log(`${file}  ${(statSync(path).size / 1024 / 1024).toFixed(1)} MB  ${manifest.includes.join(',')}  ${manifest.created_at.slice(0, 10)} by ${manifest.created_by}`);
        } catch {
          log(`${file}  (unreadable)`);
        }
      }
    },
  },
];

