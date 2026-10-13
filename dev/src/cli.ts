import { parseArgs } from 'node:util';
import { type DevConfig, loadConfig } from './config.ts';
import * as db from './db.ts';
import { setup } from './downloads.ts';
import * as instances from './instances.ts';
import { accessSync, constants, existsSync } from 'node:fs';
import { join } from 'node:path';
import { DevError, ROOT, capture, log, warn } from './util.ts';

const USAGE = `Usage: dev <command>

  bootstrap                     Install deps, build, download servers/plugins, seed the database (devcontainer postCreate)
  setup [--refresh]             Resolve versions and download servers and plugins
  build                         Build the plugin jar (./gradlew shadowJar)
  start [names...] [--no-build] Start the proxy and every enabled server (or only the named ones)
  stop [names...]               Stop instances
  restart [names...]            Stop, then start
  status                        Show what is running
  console <name>                Attach to an instance console (detach: Ctrl-b d)
  logs <name> [-f]              Show an instance log
  reset-world <name>            Delete an instance's worlds
  clean [--all]                 Delete dev/run (and dev/.cache with --all)
  db reset [--through 00N] [--all] [--yes]
                                Rebuild the database up to a changelog, with seed data
  db seed [--yes]               Reseed at the current changelog position
  db snapshot <name>            Save the database to dev/db/snapshots
  db restore <name> [--yes]     Restore a snapshot
  db psql [psql args...]        Open psql (extra args are passed through)
`;

async function main(argv: string[]): Promise<void> {
  const [command, ...rest] = argv;
  if (!command || command === 'help' || command === '--help') {
    log(USAGE);
    return;
  }
  const config = await loadConfig();
  const stopAll = () => instances.stop(config, []);

  switch (command) {
    case 'bootstrap':
      return bootstrap(config);
    case 'setup': {
      const { values } = parseArgs({ args: rest, options: { refresh: { type: 'boolean' } } });
      return setup(config, values.refresh ?? false);
    }
    case 'build':
      return instances.build();
    case 'start': {
      const { values, positionals } = parseArgs({
        args: rest,
        options: { 'no-build': { type: 'boolean' } },
        allowPositionals: true,
      });
      return instances.start(config, positionals, values['no-build'] ?? false, config.accept_eula);
    }
    case 'stop': {
      const { positionals } = parseArgs({ args: rest, allowPositionals: true });
      return instances.stop(config, positionals);
    }
    case 'restart': {
      const { values, positionals } = parseArgs({
        args: rest,
        options: { 'no-build': { type: 'boolean' } },
        allowPositionals: true,
      });
      await instances.stop(config, positionals);
      return instances.start(config, positionals, values['no-build'] ?? false, config.accept_eula);
    }
    case 'status':
      return instances.status(config);
    case 'console':
      return instances.console_(config, requireName(rest));
    case 'logs': {
      const { values, positionals } = parseArgs({
        args: rest,
        options: { follow: { type: 'boolean', short: 'f' } },
        allowPositionals: true,
      });
      return instances.logs(config, requireName(positionals), values.follow ?? false);
    }
    case 'reset-world':
      return instances.resetWorld(config, requireName(rest));
    case 'clean': {
      const { values } = parseArgs({ args: rest, options: { all: { type: 'boolean' } } });
      return instances.clean(values.all ?? false);
    }
    case 'db':
      return dbCommand(config, rest, stopAll);
    default:
      throw new DevError(`Unknown command "${command}"\n\n${USAGE}`);
  }
}

function requireName(args: string[]): string {
  const name = args[0];
  if (!name) {
    throw new DevError('An instance name is required (proxy or a server name)');
  }
  return name;
}

async function dbCommand(config: DevConfig, args: string[], stopAll: () => Promise<void>): Promise<void> {
  const [sub, ...rest] = args;
  switch (sub) {
    case 'psql':
      return db.psql(rest);
    case 'reset': {
      const { values } = parseArgs({
        args: rest,
        options: { through: { type: 'string' }, all: { type: 'boolean' }, yes: { type: 'boolean' } },
      });
      return db.reset({ through: values.through, all: values.all ?? false, yes: values.yes ?? false, stopAll });
    }
    case 'seed': {
      const { values } = parseArgs({ args: rest, options: { yes: { type: 'boolean' } } });
      return db.reseed(values.yes ?? false, stopAll);
    }
    case 'snapshot':
      return db.snapshot(requireName(rest));
    case 'restore': {
      const { values, positionals } = parseArgs({
        args: rest,
        options: { yes: { type: 'boolean' } },
        allowPositionals: true,
      });
      return db.restore(requireName(positionals), values.yes ?? false, stopAll);
    }
    default:
      throw new DevError(`Unknown db command "${sub ?? ''}"\n\n${USAGE}`);
  }
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
  if (error instanceof DevError) {
    console.error(`Error: ${error.message}`);
  } else {
    console.error(error);
  }
  process.exit(1);
});
