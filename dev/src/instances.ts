import { copyFile, mkdir, readFile, readdir, rm, stat, writeFile } from 'node:fs/promises';
import { existsSync, readdirSync, statSync } from 'node:fs';
import { join } from 'node:path';
import type { DevConfig } from './config.ts';
import { readResolved, type ResolvedFiles } from './downloads.ts';
import { type Instance, applyTemplates, instancesOf } from './templates.ts';
import { RUN, ROOT, capture, fail, log, portListening, portOpen, run, sleep, waitFor, warn } from './util.ts';
import { dbReachable } from './db.ts';
import { readState, shareTable } from './share/state.ts';
import { dim, next, status as statusWord, step, streamRow, table, yellow } from './output.ts';
import { attach, dotDir, isRunning, javaPid, kill, runLine, sendLine, spawnSupervisor } from './supervisor.ts';
import { controlSocket } from './capture/paths.ts';
import { hintsFile } from './capture/protocol.ts';
import { catalogGroups, hotswapFlags } from './hotswap.ts';

const PLUGIN_JAR = join(ROOT, 'build', 'libs', 'ParadisuPlugin.jar');
const CAPTURE_JAR = join(ROOT, 'devtools', 'capture', 'build', 'libs', 'ParadisuCapture.jar');

export { isRunning };

/** Selects instances by name; no names means every enabled instance. */
export function select(config: DevConfig, names: string[]): Instance[] {
  const all = instancesOf(config);
  if (names.length === 0) {
    return all.filter((instance) => instance.enabled);
  }
  return names.map((name) => all.find((instance) => instance.name === name) ?? fail(`Unknown instance "${name}"`));
}

/** Finds JVM flags the installed java accepts: enhanced redefinition needs JetBrains Runtime. */
function jvmFlags(config: DevConfig): string[] {
  const flags: string[] = [];
  if (config.jvm.enhanced_redefinition) {
    const attempts = [
      ['-XX:+AllowEnhancedClassRedefinition'],
      // Some JBR builds reject enhanced redefinition with the default collector
      ['-XX:+AllowEnhancedClassRedefinition', '-XX:+UseParallelGC'],
    ];
    const working = attempts.find((attempt) => capture('java', [...attempt, '-version']).status === 0);
    if (working) {
      flags.push(...working);
    } else {
      warn('This java does not support -XX:+AllowEnhancedClassRedefinition (is it JetBrains Runtime?); hot reload is limited');
    }
  }
  flags.push(...config.jvm.extra_flags);
  return [...new Set(flags)];
}

async function prepare(config: DevConfig, resolved: ResolvedFiles, instance: Instance): Promise<void> {
  await mkdir(join(instance.dir, 'plugins'), { recursive: true });
  const serverJar = {
    paper: resolved.purpur.file,
    velocity: resolved.velocity.file,
    limbo: resolved.limbo?.file ?? fail('Limbo has not been downloaded; run `dev setup`'),
  }[instance.platform];
  await copyFile(serverJar, join(instance.dir, 'server.jar'));
  if (instance.platform === 'paper') {
    await writeFile(join(instance.dir, 'eula.txt'), 'eula=true\n');
  }

  // Copy (never symlink) jars: rebuilding a jar under a running JVM corrupts its open zip
  const manifestPath = join(instance.dir, 'plugins', '.dev-managed.json');
  const previous: string[] = existsSync(manifestPath) ? JSON.parse(await readFile(manifestPath, 'utf8')) : [];
  const wanted = new Map<string, string>();
  const installedKeys: string[] = [];
  for (const plugin of resolved.plugins.filter((candidate) => candidate.platform === instance.platform)) {
    wanted.set(`${plugin.key}.jar`, plugin.file);
    installedKeys.push(plugin.key);
  }
  if (instance.platform !== 'limbo') {
    wanted.set('ParadisuPlugin.jar', PLUGIN_JAR);
  }
  if (config.capture.enabled) {
    if (!existsSync(CAPTURE_JAR)) {
      fail(`${CAPTURE_JAR} does not exist; run \`dev build\` (or set capture.enabled = false)`);
    }
    wanted.set('ParadisuCapture.jar', CAPTURE_JAR);
  }
  for (const stale of previous.filter((name) => !wanted.has(name))) {
    await rm(join(instance.dir, 'plugins', stale), { force: true });
  }
  for (const [name, source] of wanted) {
    await copyFile(source, join(instance.dir, 'plugins', name));
  }
  await writeFile(manifestPath, JSON.stringify([...wanted.keys()]));

  await applyTemplates(config, instance, installedKeys);
}

async function launch(config: DevConfig, minecraft: string, instance: Instance, flags: string[]): Promise<void> {
  const args = [
    `-Xms${instance.memory}`,
    `-Xmx${instance.memory}`,
    ...flags,
    // The capture plugin does nothing without its control socket
    ...(config.capture.enabled
      ? [`-Dparadisu.capture.socket=${controlSocket(instance)}`, `-Dparadisu.capture.protocol=${hintsFile(minecraft)}`]
      : []),
    // No X11 libraries in the image, and a forwarded DISPLAY (WSLg) would otherwise make AWT load them (BKCommonLib fails)
    '-Djava.awt.headless=true',
    `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${instance.debugPort}`,
    // stdin is a pipe owned by the supervisor, not a terminal: use the plain console reader, but keep colours
    ...(instance.platform === 'limbo' ? [] : ['-Dterminal.jline=false', '-Dterminal.ansi=true']),
    '-jar',
    'server.jar',
    ...(instance.platform === 'velocity' ? [] : ['--nogui']),
  ];
  await spawnSupervisor(instance, {
    cmd: 'java',
    args,
    cwd: instance.dir,
    stopCommand: stopCommand(instance),
  });
}

function stopCommand(instance: Instance): string {
  return instance.platform === 'velocity' ? 'shutdown' : 'stop';
}

/** The current log file: Velocity and Paper write logs/latest.log, Limbo names logs after its start time. */
function logFile(instance: Instance): string | undefined {
  const dir = join(instance.dir, 'logs');
  if (!existsSync(dir)) {
    return undefined;
  }
  if (instance.platform !== 'limbo') {
    const path = join(dir, 'latest.log');
    return existsSync(path) ? path : undefined;
  }
  return readdirSync(dir)
    .filter((name) => name.endsWith('.log'))
    .map((name) => join(dir, name))
    .sort((a, b) => statSync(b).mtimeMs - statSync(a).mtimeMs)[0];
}

function logTail(instance: Instance, lines = 40): string {
  const path = logFile(instance);
  return path ? capture('tail', ['-n', String(lines), path]).stdout : '(no log yet)';
}

/** True once the instance logged that it is ready after `since`; the port opens well before it accepts commands. */
async function reportedDone(instance: Instance, since: number): Promise<boolean> {
  const logPath = logFile(instance);
  if (!logPath || (await stat(logPath)).mtimeMs < since) {
    return false;
  }
  const ready = instance.platform === 'limbo' ? /Limbo server listening/ : /Done \(/;
  return ready.test(await readFile(logPath, 'utf8'));
}

async function firstStartCommands(instance: Instance, since: number): Promise<void> {
  const marker = join(instance.dir, '.dev-first-start-done');
  if (existsSync(marker) || instance.firstStartCommands.length === 0) {
    return;
  }
  const ready = await waitFor(() => reportedDone(instance, since), 180_000);
  if (!ready) {
    warn(`${instance.name} did not report "Done" in time; skipping first-start commands`);
    return;
  }
  for (const command of instance.firstStartCommands) {
    await sendLine(instance, command);
    await sleep(500);
  }
  await writeFile(marker, new Date().toISOString());
}

export async function start(config: DevConfig, names: string[], noBuild: boolean, acceptEula: boolean): Promise<void> {
  const instances = select(config, names);
  if (instances.some((instance) => instance.platform === 'paper') && !acceptEula) {
    fail('Set accept_eula = true in dev/dev.local.toml to accept the Minecraft EULA (https://aka.ms/MinecraftEULA).');
  }
  const resolved = await readResolved();
  if (!noBuild) {
    await build();
  }
  if (!existsSync(PLUGIN_JAR)) {
    fail(`${PLUGIN_JAR} does not exist; run \`dev build\``);
  }
  if (!(await waitFor(() => dbReachable(), 60_000, 2000))) {
    fail('PostgreSQL is not reachable (check PARADISU_DB_HOST).');
  }

  step('Starting network');
  const launchedAt = Date.now();
  const started: Instance[] = [];
  const flags = jvmFlags(config);
  const groups = config.jvm.hotswap_agent ? await catalogGroups() : [];
  // Limbo goes first so it is subscribed before the proxy tells it to transfer players back, then the proxy
  // (it runs the database migrations before it binds its port), then the backends
  const ordered = (['limbo', 'velocity', 'paper'] as const).flatMap((platform) =>
    instances.filter((i) => i.platform === platform),
  );
  const row = streamRow([{ min: Math.max(0, ...ordered.map((i) => i.name.length)) }, { min: 8 }]);
  for (const instance of ordered) {
    if (isRunning(instance)) {
      log(`  ${row([instance.name, statusWord('running'), dim('already')])}`);
      continue;
    }
    await prepare(config, resolved, instance);
    const agent = config.jvm.hotswap_agent ? hotswapFlags(instance.platform, groups, config.jvm.hotswap_plugins) : [];
    await launch(config, resolved.minecraft, instance, [...new Set([...flags, ...agent])]);
    started.push(instance);
    log(`  ${row([instance.name, statusWord('starting'), `port ${instance.port}  debug ${instance.debugPort}`])}`);
    if (instance.platform === 'velocity') {
      const up = await waitFor(() => portOpen(instance.port), 120_000);
      if (!up) {
        fail(`The proxy did not open port ${instance.port} within 120s. Last log lines:\n${logTail(instance)}`);
      }
    }
  }
  // Backends take much longer than the proxy to boot; don't claim readiness (or let a debugger task attach) before they are up
  for (const instance of started) {
    if (!(await waitFor(() => reportedDone(instance, launchedAt), 240_000))) {
      warn(`${instance.name} did not finish starting within 240s. Last log lines:\n${logTail(instance)}`);
    }
  }
  await Promise.all(started.map((instance) => firstStartCommands(instance, launchedAt)));

  log(`Ready. Join at localhost:${config.proxy.port}`);
}

export async function stop(config: DevConfig, names: string[]): Promise<void> {
  const instances = select(config, names).reverse();
  const running = instances.filter((candidate) => isRunning(candidate));
  if (running.length > 0) {
    log(`Stopping ${running.map((instance) => instance.name).join(', ')}`);
  }
  for (const instance of running) {
    await sendLine(instance, stopCommand(instance)).catch(() => {});
  }
  for (const instance of instances) {
    const stopped = await waitFor(() => !isRunning(instance), 60_000);
    if (!stopped) {
      warn(`${instance.name} did not stop in 60s; killing it`);
      await kill(instance, 'SIGKILL');
      await waitFor(() => !isRunning(instance), 5000);
    }
  }
}

export async function status(config: DevConfig): Promise<void> {
  const rows: string[][] = [];
  for (const instance of instancesOf(config)) {
    const running = isRunning(instance);
    const port = async (value: number) => {
      const listening = await portListening(value);
      return running && !listening ? `${value} ${dim('closed')}` : !running && listening ? `${value} ${yellow('in use')}` : String(value);
    };
    rows.push([
      instance.name,
      instance.platform,
      statusWord(running ? 'running' : instance.enabled ? 'stopped' : 'disabled'),
      await port(instance.port),
      await port(instance.debugPort),
      running ? String(javaPid(instance) ?? '?') : '',
    ]);
  }
  log(table(['instance', 'platform', 'state', 'port', 'debug', { header: 'pid', align: 'right' }], rows));
  log('');
  log(shareTable(readState()));
}

export function console_(config: DevConfig, name: string, options: { readOnly: boolean; wait: boolean }): Promise<void> {
  return attach(select(config, [name])[0]!, options);
}

/** Runs one console command without a terminal and prints what the console wrote within `seconds`. */
export async function consoleSend(config: DevConfig, name: string, line: string, seconds: number): Promise<void> {
  const instance = select(config, [name])[0]!;
  if (!isRunning(instance)) {
    fail(`${name} is not running`);
  }
  const result = await runLine(instance, line, seconds * 1000);
  const text = process.stdout.isTTY ? result.output : result.output.replace(/\x1b\[[0-9;]*m/g, '');
  process.stdout.write(text.endsWith('\n') || text === '' ? text : `${text}\n`);
  if (result.exited !== undefined) {
    fail(`${name} exited with code ${result.exited}`);
  }
}

export function logs(config: DevConfig, name: string, follow: boolean, consoleLog = false): void {
  const instance = select(config, [name])[0]!;
  const path = consoleLog ? join(dotDir(instance), 'console.log') : logFile(instance);
  if (!path || !existsSync(path)) {
    fail(`No log for ${name} yet`);
  }
  run('tail', follow ? ['-n', '200', '-f', path] : ['-n', '200', path]);
}

export async function resetWorld(config: DevConfig, name: string): Promise<void> {
  const instance = select(config, [name])[0]!;
  if (isRunning(instance)) {
    fail(`Stop ${name} first`);
  }
  if (!existsSync(instance.dir)) {
    return;
  }
  for (const entry of await readdir(instance.dir)) {
    if (entry === 'world' || entry.startsWith('world_') || entry === '.dev-first-start-done') {
      await rm(join(instance.dir, entry), { recursive: true, force: true });
    }
  }
  log(`Reset worlds of ${name}.`);
}

export async function clean(all: boolean): Promise<void> {
  await rm(RUN, { recursive: true, force: true });
  if (all) {
    await rm(join(ROOT, 'dev', '.cache'), { recursive: true, force: true });
  }
}

export async function build(): Promise<void> {
  log(dim('Building plugin jar'));
  run('./gradlew', ['-q', 'shadowJar', ':devtools:capture:jar']);
}

/**
 * Re-renders an instance's preset configs from the current state (for example after `dev share` changed the
 * public addresses). Returns false when the instance has never been prepared by `dev start`.
 */
export async function rerender(config: DevConfig, name: string): Promise<boolean> {
  const instance = select(config, [name])[0]!;
  const manifestPath = join(instance.dir, 'plugins', '.dev-managed.json');
  if (!existsSync(manifestPath)) {
    return false;
  }
  const names: string[] = JSON.parse(await readFile(manifestPath, 'utf8'));
  const keys = names.filter((jar) => jar !== 'ParadisuPlugin.jar' && jar !== 'ParadisuCapture.jar').map((jar) => jar.slice(0, -'.jar'.length));
  await applyTemplates(config, instance, keys);
  return true;
}
