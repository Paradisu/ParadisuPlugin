import { parseArgs } from 'node:util';
import { accessSync, constants, existsSync, readdirSync } from 'node:fs';
import { join } from 'node:path';
import type { Command, Ctx } from './command.ts';
import { type DevConfig, loadConfig } from './config.ts';
import * as db from './db.ts';
import { setup } from './downloads.ts';
import * as instances from './instances.ts';
import { supervise } from './supervisor.ts';
import { COMMANDS as ZELLIJ_COMMANDS, offerUi } from './zellij.ts';
import { COMMANDS as PROFILE_COMMANDS } from './profile.ts';
import { configDataVersion, fixtureCommand, mcCommands } from './mc/commands.ts';
import { askOptional, askText, disableInput, interactive, isPromptExit, pickFrom, pickInstance, pickInstances } from './prompt.ts';
import type { Instance } from './templates.ts';
import { DEV, DevError, ROOT, capture, log, warn } from './util.ts';

const noBuildFlag = { 'no-build': { type: 'boolean' } } as const;

function timestamp(): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}${p(d.getMonth() + 1)}${p(d.getDate())}-${p(d.getHours())}${p(d.getMinutes())}`;
}

/** Instance names from the arguments, or a checkbox prompt (stop/start flavoured) when there are none. */
async function namesOrPick(ctx: Ctx, names: string[], message: string, isChecked: (i: Instance) => boolean) {
  if (names.length > 0 || !interactive()) {
    return names;
  }
  const picked = await pickInstances(ctx.config, message, isChecked);
  if (picked.length === 0) {
    throw new DevError('Nothing selected');
  }
  return picked;
}

async function instanceArg(ctx: Ctx, args: string[], message: string, filter?: (i: Instance) => boolean) {
  return args[0] ?? (await pickInstance(ctx.config, message, filter));
}

function snapshotNames(): string[] {
  const dir = join(DEV, 'db', 'snapshots');
  return existsSync(dir)
    ? readdirSync(dir)
        .filter((file) => file.endsWith('.dump'))
        .map((file) => file.slice(0, -'.dump'.length))
    : [];
}

async function changelogChoice(): Promise<string | undefined> {
  const files = await db.changelogFiles();
  const numbers = files.map((file) => file.replace(/^.*\/(\w+)\.xml$/, '$1'));
  return pickFrom(
    'Reset the database through which changelog?',
    numbers.map((n, i) => ({ name: i === numbers.length - 1 ? `${n} (latest)` : n, value: i === numbers.length - 1 ? undefined : n })),
  );
}

const COMMANDS: Command[] = [
  {
    name: '_supervise',
    usage: '<name>',
    summary: 'Internal: run an instance under its console supervisor',
    hidden: true,
    run: ({ config }, args) => supervise(instances.select(config, [args[0] ?? ''])[0]!),
  },
  {
    name: 'bootstrap',
    usage: '',
    summary: 'Install deps, build, download servers/plugins, seed the database (devcontainer postCreate)',
    run: ({ config }) => bootstrap(config),
  },
  {
    name: 'setup',
    usage: '[--refresh]',
    summary: 'Resolve versions and download servers and plugins',
    run: ({ config }, args) => {
      const { values } = parseArgs({ args, options: { refresh: { type: 'boolean' } } });
      return setup(config, values.refresh ?? false);
    },
  },
  { name: 'build', usage: '', summary: 'Build the plugin jar (./gradlew shadowJar)', run: () => instances.build() },
  {
    name: 'start',
    usage: '[names...] [--no-build] [--ui]',
    summary: 'Start the proxy and every enabled server (or only the named ones)',
    run: async (ctx, args) => {
      const { values, positionals } = parseArgs({
        args,
        options: { ...noBuildFlag, ui: { type: 'boolean' } },
        allowPositionals: true,
      });
      const names = await namesOrPick(ctx, positionals, 'Start which instances?', (i) => i.enabled && !instances.isRunning(i));
      await instances.start(ctx.config, names, values['no-build'] ?? false, ctx.config.accept_eula);
      await offerUi(ctx.config, values.ui ?? false);
    },
  },
  {
    name: 'stop',
    usage: '[names...]',
    summary: 'Stop instances',
    run: async (ctx, args) => {
      const { positionals } = parseArgs({ args, allowPositionals: true });
      const names = await namesOrPick(ctx, positionals, 'Stop which instances?', (i) => instances.isRunning(i));
      return instances.stop(ctx.config, names);
    },
  },
  {
    name: 'restart',
    usage: '[names...] [--no-build]',
    summary: 'Stop, then start',
    run: async (ctx, args) => {
      const { values, positionals } = parseArgs({ args, options: noBuildFlag, allowPositionals: true });
      const names = await namesOrPick(ctx, positionals, 'Restart which instances?', (i) => instances.isRunning(i));
      await instances.stop(ctx.config, names);
      return instances.start(ctx.config, names, values['no-build'] ?? false, ctx.config.accept_eula);
    },
  },
  { name: 'status', usage: '', summary: 'Show what is running', run: ({ config }) => instances.status(config) },
  {
    name: 'console',
    usage: '<name> [--read-only] [--wait]',
    summary: 'Attach to an instance console (Ctrl-C detaches; the server keeps running)',
    run: async (ctx, args) => {
      const { values, positionals } = parseArgs({
        args,
        options: { 'read-only': { type: 'boolean' }, wait: { type: 'boolean' } },
        allowPositionals: true,
      });
      const wait = values.wait ?? false;
      const name = await instanceArg(ctx, positionals, 'Attach to which console?', (i) => wait || instances.isRunning(i));
      return instances.console_(ctx.config, name, { readOnly: values['read-only'] ?? false, wait });
    },
  },
  {
    name: 'logs',
    usage: '<name> [-f] [--console]',
    summary: 'Show an instance log (--console: the raw console output captured by the supervisor)',
    run: async (ctx, args) => {
      const { values, positionals } = parseArgs({
        args,
        options: { follow: { type: 'boolean', short: 'f' }, console: { type: 'boolean' } },
        allowPositionals: true,
      });
      const name = await instanceArg(ctx, positionals, 'Show the log of which instance?');
      return instances.logs(ctx.config, name, values.follow ?? false, values.console ?? false);
    },
  },
  {
    name: 'reset-world',
    usage: '<name>',
    summary: "Delete an instance's worlds",
    run: async (ctx, args) =>
      instances.resetWorld(ctx.config, await instanceArg(ctx, args, 'Reset the worlds of which instance?', (i) => i.platform === 'paper')),
  },
  {
    name: 'clean',
    usage: '[--all]',
    summary: 'Delete dev/run (and dev/.cache with --all)',
    run: (_ctx, args) => {
      const { values } = parseArgs({ args, options: { all: { type: 'boolean' } } });
      return instances.clean(values.all ?? false);
    },
  },
  {
    name: 'db reset',
    usage: '[--through 00N] [--all] [--no-fixtures] [--yes]',
    summary: 'Rebuild the database up to a changelog, with seed data and fixtures',
    run: async (ctx, args) => {
      const { values } = parseArgs({
        args,
        options: {
          through: { type: 'string' },
          all: { type: 'boolean' },
          yes: { type: 'boolean' },
          'no-fixtures': { type: 'boolean' },
        },
      });
      let through = values.through;
      let all = values.all ?? false;
      if (args.length === 0 && interactive()) {
        through = await changelogChoice();
        all = await askOptional('Also reset the LuckPerms database?', false);
      }
      return db.reset({ through, all, yes: values.yes ?? false, noFixtures: values['no-fixtures'] ?? false, stopAll: ctx.stopAll });
    },
  },
  {
    name: 'db seed',
    usage: '[--yes]',
    summary: 'Reseed at the current changelog position',
    run: (ctx, args) => {
      const { values } = parseArgs({ args, options: { yes: { type: 'boolean' } } });
      return db.reseed(values.yes ?? false, ctx.stopAll);
    },
  },
  {
    name: 'db snapshot',
    usage: '<name>',
    summary: 'Save the database to dev/db/snapshots',
    run: async (_ctx, args) =>
      db.snapshot(args[0] ?? (await askText('Snapshot name', { default: `snap-${timestamp()}` }, 'A snapshot name'))),
  },
  {
    name: 'db restore',
    usage: '<name> [--yes]',
    summary: 'Restore a snapshot',
    run: async (ctx, args) => {
      const { values, positionals } = parseArgs({ args, options: { yes: { type: 'boolean' } }, allowPositionals: true });
      const name =
        positionals[0] ??
        (await pickFrom(
          'Restore which snapshot?',
          snapshotNames().map((n) => ({ name: n, value: n })),
          'A snapshot name',
        ));
      return db.restore(name, values.yes ?? false, ctx.stopAll);
    },
  },
  {
    name: 'db psql',
    usage: '[psql args...]',
    summary: 'Open psql (extra args are passed through)',
    run: (_ctx, args) => db.psql(args),
  },
];

COMMANDS.push(...ZELLIJ_COMMANDS, ...PROFILE_COMMANDS);

/** Commands that need the loaded config are registered lazily in main(). */
function registerConfigCommands(config: DevConfig): void {
  COMMANDS.push(
    fixtureCommand(),
    ...mcCommands(
      () => configDataVersion(config),
      () => config.servers.filter((server) => server.enabled).map((server) => server.name),
    ),
  );
}

function visible(): Command[] {
  return COMMANDS.filter((command) => !command.hidden);
}

function groupOf(command: Command): string | undefined {
  return command.name.includes(' ') ? command.name.split(' ')[0] : undefined;
}

function helpLines(commands: Command[]): string {
  return commands
    .map((command) => {
      const left = `${command.name} ${command.usage}`.trim();
      return left.length <= 40 ? `  ${left.padEnd(40)} ${command.summary}` : `  ${left}\n  ${' '.repeat(41)}${command.summary}`;
    })
    .join('\n');
}

function usage(group?: string): string {
  const commands = visible().filter((command) => !group || groupOf(command) === group);
  return `Usage: dev <command>${group ? ` (group: ${group})` : ''}\n\n${helpLines(commands)}\n`;
}

function groups(): string[] {
  return [...new Set(visible().map(groupOf).filter((g): g is string => Boolean(g)))];
}

function find(argv: string[]): { command: Command; args: string[] } | undefined {
  if (argv.length >= 2) {
    const two = COMMANDS.find((command) => command.name === `${argv[0]} ${argv[1]}`);
    if (two) {
      return { command: two, args: argv.slice(2) };
    }
  }
  const one = COMMANDS.find((command) => command.name === argv[0]);
  return one ? { command: one, args: argv.slice(1) } : undefined;
}

/** `dev` with no arguments on a terminal: a menu of top-level commands and groups. */
async function menu(group?: string): Promise<string[]> {
  const commands = visible().filter((command) => (group ? groupOf(command) === group : true));
  const entries = new Map<string, string>();
  for (const command of commands) {
    const g = groupOf(command);
    if (!group && g) {
      entries.set(g, `${g}…`);
    } else {
      entries.set(command.name, `${command.name.padEnd(14)} ${command.summary}`);
    }
  }
  const chosen = await pickFrom(
    group ? `dev ${group}` : 'What do you want to do?',
    [...entries].map(([value, name]) => ({ name, value })),
  );
  return groups().includes(chosen) && !group ? menu(chosen) : chosen.split(' ');
}

async function main(argv: string[]): Promise<void> {
  if (argv.includes('--no-input')) {
    disableInput();
    argv = argv.filter((arg) => arg !== '--no-input');
  }
  const config = await loadConfig();
  registerConfigCommands(config);
  if (argv[0] === 'help' || argv[0] === '--help' || argv[0] === '-h') {
    log(usage(argv[1]));
    return;
  }
  if (argv.length === 0 || (argv.length === 1 && groups().includes(argv[0]!))) {
    if (!interactive()) {
      log(usage(argv[0]));
      return;
    }
    argv = await menu(argv[0]);
  }
  const found = find(argv);
  if (!found) {
    throw new DevError(`Unknown command "${argv.join(' ')}"\n\n${usage()}`);
  }
  const ctx: Ctx = { config, stopAll: () => instances.stop(config, []) };
  await found.command.run(ctx, found.args);
}

/**
 * The Gradle project caches are Docker volumes (see docker-compose.yml). One created for a different user ID than
 * the one running now (the CLI renumbers the container user to the host's) is not writable, so Gradle fails to start.
 */
function ensureGradleCachesWritable(): void {
  for (const relative of ['.gradle', join('build-logic', '.gradle')]) {
    const dir = join(ROOT, relative);
    if (!existsSync(dir)) {
      continue;
    }
    try {
      accessSync(dir, constants.W_OK);
    } catch {
      const owner = `${process.getuid?.()}:${process.getgid?.()}`;
      const result = capture('sudo', ['-n', 'chown', '-R', owner, dir]);
      if (result.status === 0) {
        log(`Fixed ownership of ${relative} (it belonged to another user ID)`);
      } else {
        warn(`${relative} is not writable and could not be fixed: ${result.stderr.trim()}. Remove the container's gradle volumes and reopen.`);
      }
    }
  }
}

/** Devcontainer postCreate hook: never fails container creation, just warns so it can be re-run. */
async function bootstrap(config: DevConfig): Promise<void> {
  const { run } = await import('./util.ts');
  const steps: [string, () => Promise<void> | void][] = [
    ['Building the plugin jar', () => run('./gradlew', ['-q', 'shadowJar'])],
    ['Downloading servers and plugins', () => setup(config, false)],
    [
      'Preparing the database',
      async () => {
        if (!db.dbReachable()) {
          throw new DevError('PostgreSQL is not reachable');
        }
        if (!db.isInitialised()) {
          await db.reset({ yes: true, all: false, stopAll: async () => {} });
        }
      },
    ],
  ];
  ensureGradleCachesWritable();
  let failed = false;
  for (const [label, step] of steps) {
    log(`\n==> ${label}`);
    try {
      await step();
    } catch (error) {
      failed = true;
      warn(`${label} failed: ${(error as Error).message}`);
    }
  }
  if (failed) {
    warn('Bootstrap finished with errors. Fix the cause (network?) and run `dev bootstrap` again.');
  }
}

main(process.argv.slice(2)).catch((error: unknown) => {
  if (isPromptExit(error)) {
    process.exit(130);
  }
  if (error instanceof DevError) {
    console.error(`Error: ${error.message}`);
  } else {
    console.error(error);
  }
  process.exit(1);
});
