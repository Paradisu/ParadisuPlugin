import { spawnSync } from 'node:child_process';
import { createHash } from 'node:crypto';
import { createWriteStream, existsSync, readFileSync } from 'node:fs';
import { mkdir, rename, rm } from 'node:fs/promises';
import net from 'node:net';
import { dirname, join, resolve } from 'node:path';
import { Readable } from 'node:stream';
import { pipeline } from 'node:stream/promises';
import { paint } from './output.ts';

export const ROOT = resolve(import.meta.dirname, '..', '..');
export const DEV = join(ROOT, 'dev');
export const RUN = join(DEV, 'run');
export const CACHE = join(DEV, '.cache');
export const TEMPLATES = join(DEV, 'templates');
/** Personal config overrides (gitignored), laid out like TEMPLATES and applied after it; exported by profiles */
export const TEMPLATES_LOCAL = join(DEV, 'templates.local');
export const USER_AGENT = 'paradisu-dev/1 (github.com/Paradisu/ParadisuPlugin)';

/** An expected failure: printed without a stack trace. */
export class DevError extends Error {}

export function fail(message: string): never {
  throw new DevError(message);
}

export function log(message: string): void {
  console.log(message);
}

export function warn(message: string): void {
  console.warn(`${paint('yellow', 'warning:', process.stderr)} ${message}`);
}

export interface CaptureResult {
  status: number;
  stdout: string;
  stderr: string;
}

export interface RunOptions {
  cwd?: string;
  env?: Record<string, string | undefined>;
  /** Variables to remove from the child's environment */
  unsetEnv?: string[];
  input?: string;
}

function childEnv(options: RunOptions): NodeJS.ProcessEnv {
  const env = { ...process.env, ...options.env };
  for (const name of options.unsetEnv ?? []) {
    delete env[name];
  }
  return env;
}

/** Runs a command with inherited stdio; throws if it exits non-zero. */
export function run(cmd: string, args: string[], options: RunOptions = {}): void {
  const result = spawnSync(cmd, args, {
    stdio: 'inherit',
    cwd: options.cwd ?? ROOT,
    env: childEnv(options),
  });
  if (result.error) {
    fail(`Could not run ${cmd}: ${result.error.message}`);
  }
  if (result.status !== 0) {
    fail(`${cmd} ${args.join(' ')} exited with status ${result.status}`);
  }
}

/** Runs a command and captures its output; never throws for a non-zero exit. */
export function capture(cmd: string, args: string[], options: RunOptions = {}): CaptureResult {
  const result = spawnSync(cmd, args, {
    encoding: 'utf8',
    cwd: options.cwd ?? ROOT,
    env: childEnv(options),
    input: options.input,
  });
  if (result.error) {
    return { status: 127, stdout: '', stderr: result.error.message };
  }
  return { status: result.status ?? 1, stdout: result.stdout, stderr: result.stderr };
}

export function sleep(ms: number): Promise<void> {
  return new Promise((done) => setTimeout(done, ms));
}

export async function waitFor(check: () => boolean | Promise<boolean>, timeoutMs: number, intervalMs = 1000): Promise<boolean> {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    if (await check()) {
      return true;
    }
    await sleep(intervalMs);
  }
  return false;
}

export function portOpen(port: number, host = '127.0.0.1', timeoutMs = 500): Promise<boolean> {
  return new Promise((done) => {
    const socket = net.connect({ port, host });
    const finish = (open: boolean) => {
      socket.destroy();
      done(open);
    };
    socket.setTimeout(timeoutMs);
    socket.once('connect', () => finish(true));
    socket.once('timeout', () => finish(false));
    socket.once('error', () => finish(false));
  });
}

/**
 * Whether something is listening on a local port, without connecting to it. Connecting to a JDWP debug port and
 * hanging up makes the JVM print "Debugger failed to attach" in the server console, so status checks read the
 * kernel's socket table instead (Linux) and only fall back to a connection attempt elsewhere.
 */
export async function portListening(port: number): Promise<boolean> {
  const tables = ['/proc/net/tcp', '/proc/net/tcp6'].filter((table) => existsSync(table));
  if (tables.length === 0) {
    return portOpen(port);
  }
  const LISTEN = '0A';
  return tables.some((table) =>
    readFileSync(table, 'utf8')
      .split('\n')
      .slice(1)
      .some((line) => {
        const columns = line.trim().split(/\s+/);
        return columns[3] === LISTEN && Number.parseInt(columns[1]?.split(':')[1] ?? '', 16) === port;
      }),
  );
}

export async function getJson<T>(url: string, headers: Record<string, string> = {}): Promise<T> {
  const response = await fetch(url, { headers: { 'User-Agent': USER_AGENT, Accept: 'application/json', ...headers } });
  if (!response.ok) {
    fail(`GET ${url} failed: ${response.status} ${response.statusText}`);
  }
  return (await response.json()) as T;
}

export interface Checksums {
  md5?: string;
  sha256?: string;
  sha512?: string;
}

/** Downloads a file (atomically) and verifies the given checksums. */
export async function download(url: string, dest: string, checksums: Checksums = {}): Promise<void> {
  const response = await fetch(url, { headers: { 'User-Agent': USER_AGENT }, redirect: 'follow' });
  if (!response.ok || !response.body) {
    fail(`GET ${url} failed: ${response.status} ${response.statusText}`);
  }
  await mkdir(dirname(dest), { recursive: true });
  const partial = `${dest}.part`;
  const hashes = (['md5', 'sha256', 'sha512'] as const)
    .filter((algorithm) => checksums[algorithm])
    .map((algorithm) => ({ algorithm, hash: createHash(algorithm) }));
  const body = Readable.fromWeb(response.body as import('node:stream/web').ReadableStream);
  body.on('data', (chunk: Buffer) => hashes.forEach(({ hash }) => hash.update(chunk)));
  await pipeline(body, createWriteStream(partial));
  for (const { algorithm, hash } of hashes) {
    const actual = hash.digest('hex');
    if (actual !== checksums[algorithm]!.toLowerCase()) {
      await rm(partial, { force: true });
      fail(`${algorithm} mismatch for ${url}: expected ${checksums[algorithm]}, got ${actual}`);
    }
  }
  await rename(partial, dest);
}

export function sha256File(path: string): string {
  return createHash('sha256').update(readFileSync(path)).digest('hex');
}

/** Replaces ${NAME} tokens; tokens without a value are left untouched. */
export function render(text: string, vars: Record<string, string>): string {
  return text.replace(/\$\{([A-Z0-9_]+)\}/g, (token, name: string) => (name in vars ? vars[name]! : token));
}

type Json = Record<string, unknown>;

function isPlainObject(value: unknown): value is Json {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

/** Tables merge recursively; arrays and scalars in `override` replace the base value. */
export function deepMerge(base: Json, override: Json): Json {
  const merged: Json = { ...base };
  for (const [key, value] of Object.entries(override)) {
    const existing = merged[key];
    merged[key] = isPlainObject(existing) && isPlainObject(value) ? deepMerge(existing, value) : value;
  }
  return merged;
}
