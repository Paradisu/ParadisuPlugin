import { existsSync } from 'node:fs';
import { mkdir, readdir } from 'node:fs/promises';
import { basename, join } from 'node:path';
import { env } from './config.ts';
import { DEV, ROOT, capture, confirm, fail, log, run } from './util.ts';

const SEEDS = join(DEV, 'db', 'seed');
const SNAPSHOTS = join(DEV, 'db', 'snapshots');
const DEVTOOLS = join(ROOT, 'devtools', 'build', 'install', 'devtools', 'bin', 'devtools');

function pgEnv(database?: string): Record<string, string> {
  const { db } = env();
  return {
    PGHOST: db.host,
    PGPORT: db.port,
    PGUSER: db.user,
    PGPASSWORD: db.password,
    PGDATABASE: database ?? db.name,
    PGOPTIONS: '-c client_min_messages=warning',
    PARADISU_DB_HOST: db.host,
    PARADISU_DB_PORT: db.port,
    PARADISU_DB_NAME: db.name,
    PARADISU_DB_USER: db.user,
    PARADISU_DB_PASSWORD: db.password,
  };
}

export function dbReachable(): boolean {
  return capture('pg_isready', [], { env: pgEnv() }).status === 0;
}

/** Runs psql with inherited stdio; extra args are passed straight through (`dev db psql -c '...'`). */
export function psql(args: string[], database?: string): void {
  run('psql', args, { env: pgEnv(database) });
}

function query(sql: string, database?: string): string {
  const result = capture('psql', ['-X', '-tA', '-c', sql], { env: pgEnv(database) });
  if (result.status !== 0) {
    fail(`psql failed: ${result.stderr.trim()}`);
  }
  return result.stdout.trim();
}

/** Whether Liquibase has ever run against the dev database. */
export function isInitialised(): boolean {
  return query(`select to_regclass('public.databasechangelog') is not null`) === 't';
}

function appliedChangelogs(): string[] {
  if (!isInitialised()) {
    return [];
  }
  return query(`select distinct filename from databasechangelog order by 1`).split('\n').filter(Boolean);
}

function devtools(args: string[]): string {
  const result = capture(DEVTOOLS, args, { env: pgEnv() });
  if (result.status !== 0) {
    fail(`devtools ${args.join(' ')} failed:\n${result.stderr || result.stdout}`);
  }
  return result.stdout;
}

function changelogNumber(file: string): string {
  return basename(file, '.xml');
}

export interface ResetOptions {
  through?: string;
  yes: boolean;
  all: boolean;
  stopAll: () => Promise<void>;
}

/**
 * Rebuilds the dev database: drops the schema, applies changelogs up to `through` (default: all) with
 * the matching seed data, then checks that only the changelogs that were not applied remain.
 */
export async function reset(options: ResetOptions): Promise<void> {
  const target = options.through ? `changelog ${options.through}` : 'the latest changelog';
  if (!(await confirm(`This DROPS the dev database${options.all ? 's (including LuckPerms)' : ''} and reseeds it through ${target}. Continue?`, options.yes))) {
    log('Aborted');
    return;
  }
  await options.stopAll();

  log('Building migration tool');
  run('./gradlew', ['-q', ':devtools:installDist']);
  const files = devtools(['files']).split('\n').filter(Boolean);
  if (options.through && !files.some((file) => changelogNumber(file) === options.through)) {
    fail(`Unknown changelog ${options.through}. Available: ${files.map(changelogNumber).join(", ")}`);
  }
  const applying = files.filter((file) => !options.through || changelogNumber(file) <= options.through);

  const reset = `drop schema public cascade; create schema public;`;
  run('psql', ['-X', '-q', '-v', 'ON_ERROR_STOP=1', '-c', reset], { env: pgEnv() });
  if (options.all) {
    run('psql', ['-X', '-q', '-v', 'ON_ERROR_STOP=1', '-c', reset], { env: pgEnv('luckperms') });
  }

  for (const file of applying) {
    const number = changelogNumber(file);
    log(`Applying ${file}`);
    devtools(['update-file', file]);
    const seed = join(SEEDS, `${number}.sql`);
    if (existsSync(seed)) {
      log(`  seeding ${number}.sql`);
      run('psql', ['-X', '-v', 'ON_ERROR_STOP=1', '-q', '-f', seed], { env: pgEnv() });
    }
  }
  const local = join(SEEDS, 'local.sql');
  if (existsSync(local)) {
    log('  seeding local.sql');
    run('psql', ['-X', '-v', 'ON_ERROR_STOP=1', '-q', '-f', local], { env: pgEnv() });
  }

  const remaining = new Set(files.filter((file) => !applying.includes(file)));
  const unrun = devtools(['unrun']).split('\n').filter(Boolean);
  const unexpected = unrun.filter((line) => !remaining.has(line.slice(line.indexOf(':') + 1)));
  if (unexpected.length > 0) {
    fail(
      `After migrating, Liquibase still wants to run changesets that are not in a later changelog:\n  ${unexpected.join('\n  ')}\n` +
        'Their recorded file path differs from the one in migrations.xml, so the proxy would re-run them.',
    );
  }
  const last = changelogNumber(applying[applying.length - 1]!);
  log(`Database is at changelog ${last}${remaining.size > 0 ? `; start the proxy to apply ${[...remaining].map(changelogNumber).join(', ')} against the seeded data` : ''}.`);
}

/** Recreates the database at its current changelog position with fresh seed data. */
export async function reseed(yes: boolean, stopAll: () => Promise<void>): Promise<void> {
  const applied = appliedChangelogs();
  const through = applied.length > 0 ? changelogNumber(applied[applied.length - 1]!) : undefined;
  await reset({ through, yes, all: false, stopAll });
}

export async function snapshot(name: string): Promise<void> {
  await mkdir(SNAPSHOTS, { recursive: true });
  const file = join(SNAPSHOTS, `${name}.dump`);
  run('pg_dump', ['-Fc', '-f', file], { env: pgEnv() });
  log(`Saved ${file}`);
}

export async function restore(name: string, yes: boolean, stopAll: () => Promise<void>): Promise<void> {
  const file = join(SNAPSHOTS, `${name}.dump`);
  if (!existsSync(file)) {
    const available = existsSync(SNAPSHOTS) ? (await readdir(SNAPSHOTS)).join(', ') : 'none';
    fail(`No snapshot "${name}" (available: ${available})`);
  }
  if (!(await confirm(`Restore snapshot "${name}" over the dev database?`, yes))) {
    log('Aborted');
    return;
  }
  await stopAll();
  run('pg_restore', ['--clean', '--if-exists', '--no-owner', '-d', env().db.name, file], { env: pgEnv() });
  log(`Restored ${name}`);
}
