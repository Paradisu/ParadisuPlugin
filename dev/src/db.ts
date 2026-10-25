import { existsSync } from 'node:fs';
import { mkdir, readdir } from 'node:fs/promises';
import { basename, join } from 'node:path';
import { env } from './config.ts';
import { confirmAction } from './prompt.ts';
import { DEV, ROOT, capture, fail, log, run } from './util.ts';

const SEEDS = join(DEV, 'db', 'seed');
const SNAPSHOTS = join(DEV, 'db', 'snapshots');
const DEVTOOLS = join(ROOT, 'devtools', 'migrate', 'build', 'install', 'migrate', 'bin', 'migrate');

export function pgEnv(database?: string): Record<string, string> {
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

/**
 * Runs a query and returns its rows as objects. The SQL is sent on stdin so psql interpolates variables:
 * reference user input as :'name' and pass it in `vars`, never by string concatenation. Binary columns should be
 * selected as translate(encode(col, 'base64'), E'\\n', '') (encode wraps lines).
 */
export function queryJson<T>(sql: string, vars: Record<string, string> = {}): T[] {
  const args = ['-X', '-q', '-tA', '-v', 'ON_ERROR_STOP=1'];
  for (const [name, value] of Object.entries(vars)) {
    args.push('-v', `${name}=${value}`);
  }
  const wrapped = `select coalesce(json_agg(t), '[]'::json) from (${sql.trim().replace(/;$/, '')}) t;`;
  const result = capture('psql', args, { env: pgEnv(), input: wrapped });
  if (result.status !== 0) {
    fail(`psql failed: ${result.stderr.trim()}`);
  }
  return JSON.parse(result.stdout.trim() || '[]') as T[];
}

/** Runs a SQL script (stdin, so :'var' works) and fails on the first error. */
export function runSql(sql: string, vars: Record<string, string> = {}): void {
  const args = ['-X', '-q', '-v', 'ON_ERROR_STOP=1'];
  for (const [name, value] of Object.entries(vars)) {
    args.push('-v', `${name}=${value}`);
  }
  const result = capture('psql', args, { env: pgEnv(), input: sql });
  if (result.status !== 0) {
    fail(`psql failed: ${result.stderr.trim()}`);
  }
}

/** Whether Liquibase has ever run against the dev database. */
export function isInitialised(): boolean {
  return query(`select to_regclass('public.databasechangelog') is not null`) === 't';
}

export function appliedChangelogs(): string[] {
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

/** Changelog files in migrations.xml order; builds the devtools runner first. */
export async function changelogFiles(): Promise<string[]> {
  buildDevtools();
  return devtools(['files']).split('\n').filter(Boolean);
}

function buildDevtools(): void {
  log('Building migration tool');
  run('./gradlew', ['-q', ':devtools:migrate:installDist']);
}

function changelogNumber(file: string): string {
  return basename(file, '.xml');
}

export interface ResetOptions {
  through?: string;
  yes: boolean;
  all: boolean;
  /** Skip loading dev/db/fixtures */
  noFixtures?: boolean;
  stopAll: () => Promise<void>;
}

/**
 * Rebuilds the dev database: drops the schema, applies changelogs up to `through` (default: all) with
 * the matching seed data, then checks that only the changelogs that were not applied remain.
 */
export async function reset(options: ResetOptions): Promise<void> {
  const target = options.through ? `changelog ${options.through}` : 'the latest changelog';
  if (!(await confirmAction(`This DROPS the dev database${options.all ? 's (including LuckPerms)' : ''} and reseeds it through ${target}. Continue?`, options.yes))) {
    log('Aborted');
    return;
  }
  await options.stopAll();

  const files = await changelogFiles();
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

  if (!options.noFixtures) {
    const { loadFixtures } = await import('./mc/fixtures.ts');
    await loadFixtures({ through: options.through, replace: false });
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

/** pg_restore of a custom-format dump into `database` (existing objects are dropped first). */
export function restoreDump(file: string, database: string): void {
  run('pg_restore', ['--clean', '--if-exists', '--no-owner', '-d', database, file], { env: pgEnv(database) });
}

/** pg_dump of `database` in custom format. */
export function dumpDatabase(file: string, database: string): void {
  run('pg_dump', ['-Fc', '-f', file], { env: pgEnv(database) });
}

export async function restore(name: string, yes: boolean, stopAll: () => Promise<void>): Promise<void> {
  const file = join(SNAPSHOTS, `${name}.dump`);
  if (!existsSync(file)) {
    const available = existsSync(SNAPSHOTS) ? (await readdir(SNAPSHOTS)).join(', ') : 'none';
    fail(`No snapshot "${name}" (available: ${available})`);
  }
  if (!(await confirmAction(`Restore snapshot "${name}" over the dev database?`, yes))) {
    log('Aborted');
    return;
  }
  await stopAll();
  restoreDump(file, env().db.name);
  log(`Restored ${name}`);
}
