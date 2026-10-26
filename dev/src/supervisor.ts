import { dim } from './output.ts';
import { spawn } from 'node:child_process';
import { createHash } from 'node:crypto';
import {
  appendFileSync,
  createWriteStream,
  existsSync,
  mkdirSync,
  readFileSync,
  renameSync,
  rmSync,
  writeFileSync,
} from 'node:fs';
import net from 'node:net';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { createInterface } from 'node:readline';
import type { Instance } from './templates.ts';
import { RUN, ROOT, fail, log, sleep, waitFor } from './util.ts';

/**
 * Every server JVM runs under a small detached supervisor (`dev _supervise <name>`) instead of a terminal
 * multiplexer. The supervisor owns the JVM's stdin/stdout, logs everything to `.dev/console.log`, and serves a
 * console on a unix socket so any number of terminals (zellij panes, plain shells) can attach and detach
 * without affecting the server.
 *
 * Protocol: newline-delimited JSON.
 *   client -> supervisor: {t:'hello',replay:n} | {t:'in',line} | {t:'kill',signal}
 *   supervisor -> client: {t:'out',data} | {t:'exit',code}
 */

export interface LaunchSpec {
  cmd: string;
  args: string[];
  cwd: string;
  /** Console command that makes the server shut down cleanly */
  stopCommand: string;
}

const CLI_PATH = fileURLToPath(new URL('./cli.ts', import.meta.url));
const RING_BYTES = 256 * 1024;

export function dotDir(instance: Instance): string {
  return join(instance.dir, '.dev');
}

/**
 * Unix socket paths are limited to ~107 bytes and checkouts can live deep, so sockets go under the temp dir.
 * `suffix` distinguishes other sockets of the same instance (the capture plugin's control socket).
 */
export function socketPath(instance: Instance, suffix = ''): string {
  const checkout = createHash('sha1').update(RUN).digest('hex').slice(0, 8);
  const dir = join(tmpdir(), `paradisu-dev-${process.getuid?.() ?? 'u'}`);
  mkdirSync(dir, { recursive: true, mode: 0o700 });
  return join(dir, `${checkout}-${instance.name}${suffix}.sock`);
}

function readPid(file: string): number | undefined {
  try {
    const pid = Number.parseInt(readFileSync(file, 'utf8'), 10);
    return Number.isInteger(pid) ? pid : undefined;
  } catch {
    return undefined;
  }
}

/** The supervisor's pid if it is alive and really is our supervisor (pids are reused after a container restart). */
export function supervisorPid(instance: Instance): number | undefined {
  const file = join(dotDir(instance), 'supervisor.pid');
  const pid = readPid(file);
  if (pid === undefined) {
    return undefined;
  }
  let alive = true;
  try {
    process.kill(pid, 0);
  } catch {
    alive = false;
  }
  if (alive && existsSync('/proc/self/cmdline')) {
    try {
      const cmdline = readFileSync(`/proc/${pid}/cmdline`, 'utf8');
      alive = cmdline.includes('_supervise') && cmdline.includes(instance.name);
    } catch {
      alive = false;
    }
  }
  if (!alive) {
    rmSync(file, { force: true });
    rmSync(join(dotDir(instance), 'java.pid'), { force: true });
    return undefined;
  }
  return pid;
}

export function isRunning(instance: Instance): boolean {
  return supervisorPid(instance) !== undefined;
}

export function javaPid(instance: Instance): number | undefined {
  return isRunning(instance) ? readPid(join(dotDir(instance), 'java.pid')) : undefined;
}

// ---------------------------------------------------------------------------------------------------------------
// Supervisor process
// ---------------------------------------------------------------------------------------------------------------

/** Entry point of `dev _supervise <name>`: runs until the JVM exits. */
export async function supervise(instance: Instance): Promise<void> {
  const dir = dotDir(instance);
  const crashLog = join(dir, 'supervisor.log');
  process.on('uncaughtException', (error) => {
    appendFileSync(crashLog, `${new Date().toISOString()} ${error.stack ?? error}\n`);
    process.exit(1);
  });
  process.on('SIGHUP', () => {});

  const spec = JSON.parse(readFileSync(join(dir, 'launch.json'), 'utf8')) as LaunchSpec;
  const consoleLog = join(dir, 'console.log');
  if (existsSync(consoleLog)) {
    renameSync(consoleLog, join(dir, 'console.prev.log'));
  }
  rmSync(join(dir, 'exit.json'), { force: true });
  const logStream = createWriteStream(consoleLog, { flags: 'a' });
  writeFileSync(join(dir, 'supervisor.pid'), String(process.pid));

  const child = spawn(spec.cmd, spec.args, { cwd: spec.cwd, stdio: ['pipe', 'pipe', 'pipe'] });
  if (child.pid !== undefined) {
    writeFileSync(join(dir, 'java.pid'), String(child.pid));
  }
  child.stdin.on('error', () => {});

  let ring = '';
  const clients = new Set<net.Socket>();
  const broadcast = (message: object) => {
    const line = `${JSON.stringify(message)}\n`;
    for (const client of clients) {
      client.write(line);
    }
  };
  const onOutput = (chunk: Buffer) => {
    const data = chunk.toString('utf8');
    logStream.write(chunk);
    ring = (ring + data).slice(-RING_BYTES);
    broadcast({ t: 'out', data });
  };
  child.stdout.on('data', onOutput);
  child.stderr.on('data', onOutput);

  const sock = socketPath(instance);
  rmSync(sock, { force: true });
  const server = net.createServer((client) => {
    let pending = '';
    client.on('error', () => clients.delete(client));
    client.on('close', () => clients.delete(client));
    client.on('data', (chunk) => {
      pending += chunk.toString('utf8');
      for (let newline = pending.indexOf('\n'); newline !== -1; newline = pending.indexOf('\n')) {
        const line = pending.slice(0, newline);
        pending = pending.slice(newline + 1);
        let message: { t: string; replay?: number; line?: string; signal?: NodeJS.Signals };
        try {
          message = JSON.parse(line);
        } catch {
          continue;
        }
        if (message.t === 'hello') {
          const replay = ring.split('\n').slice(-(message.replay ?? 200)).join('\n');
          client.write(`${JSON.stringify({ t: 'out', data: replay })}\n`);
          clients.add(client);
        } else if (message.t === 'in' && typeof message.line === 'string') {
          child.stdin.write(`${message.line}\n`);
        } else if (message.t === 'kill') {
          child.kill(message.signal === 'SIGKILL' ? 'SIGKILL' : 'SIGTERM');
        }
      }
    });
  });
  await new Promise<void>((done) => server.listen(sock, done));

  process.on('SIGTERM', () => child.stdin.write(`${spec.stopCommand}\n`));

  child.on('exit', (code, signal) => {
    writeFileSync(join(dir, 'exit.json'), JSON.stringify({ code, signal, at: new Date().toISOString() }));
    broadcast({ t: 'exit', code: code ?? (signal ? 128 : 1) });
    for (const client of clients) {
      client.end();
    }
    server.close();
    rmSync(sock, { force: true });
    rmSync(join(dir, 'supervisor.pid'), { force: true });
    rmSync(join(dir, 'java.pid'), { force: true });
    logStream.end(() => process.exit(0));
  });
}

// ---------------------------------------------------------------------------------------------------------------
// Controlling side
// ---------------------------------------------------------------------------------------------------------------

/** Starts a detached supervisor for the instance and waits until its console socket exists. */
export async function spawnSupervisor(instance: Instance, spec: LaunchSpec): Promise<void> {
  const dir = dotDir(instance);
  mkdirSync(dir, { recursive: true });
  writeFileSync(join(dir, 'launch.json'), JSON.stringify(spec));
  const child = spawn(
    process.execPath,
    ['--disable-warning=ExperimentalWarning', CLI_PATH, '_supervise', instance.name],
    { detached: true, stdio: 'ignore', cwd: ROOT, env: process.env },
  );
  child.unref();
  const sock = socketPath(instance);
  if (!(await waitFor(() => existsSync(sock), 5000, 100))) {
    const crash = join(dir, 'supervisor.log');
    fail(`The supervisor for ${instance.name} did not start.${existsSync(crash) ? `\n${readFileSync(crash, 'utf8')}` : ''}`);
  }
}

function connect(instance: Instance): Promise<net.Socket> {
  return new Promise((resolve, reject) => {
    const socket = net.connect(socketPath(instance));
    socket.once('connect', () => resolve(socket));
    socket.once('error', reject);
  });
}

async function send(instance: Instance, message: object): Promise<void> {
  const socket = await connect(instance);
  await new Promise<void>((done) => {
    socket.once('close', () => done());
    socket.end(`${JSON.stringify(message)}\n`);
  });
}

/** Writes a line to the server console. */
export function sendLine(instance: Instance, line: string): Promise<void> {
  return send(instance, { t: 'in', line });
}

/** Kills the JVM (SIGKILL); falls back to signalling it directly if the socket is gone. */
export async function kill(instance: Instance, signal: 'SIGTERM' | 'SIGKILL'): Promise<void> {
  try {
    await send(instance, { t: 'kill', signal });
  } catch {
    const pid = javaPid(instance);
    if (pid !== undefined) {
      process.kill(pid, signal);
    }
  }
}

export interface AttachOptions {
  readOnly: boolean;
  /** Keep waiting for the server (and reconnect after it restarts) instead of failing when it is not running */
  wait: boolean;
}

/** Attaches the terminal to an instance console. Ctrl-C and Ctrl-D detach; the server keeps running. */
export async function attach(instance: Instance, options: AttachOptions): Promise<void> {
  let announced = false;
  for (;;) {
    if (!isRunning(instance)) {
      if (!options.wait) {
        fail(`${instance.name} is not running`);
      }
      if (!announced) {
        log(dim(`Waiting for ${instance.name}…`));
        announced = true;
      }
      await sleep(1000);
      continue;
    }
    announced = false;
    const outcome = await attachOnce(instance, options);
    if (outcome === 'detached' || !options.wait) {
      return;
    }
    await sleep(500);
  }
}

function attachOnce(instance: Instance, options: AttachOptions): Promise<'detached' | 'exited'> {
  const historyFile = join(dotDir(instance), 'history');
  return new Promise((resolve) => {
    connect(instance).then(
      (socket) => {
        const interactive = !options.readOnly && Boolean(process.stdin.isTTY);
        const rl = interactive
          ? createInterface({
              input: process.stdin,
              output: process.stdout,
              terminal: true,
              prompt: '> ',
              historySize: 500,
              history: existsSync(historyFile) ? readFileSync(historyFile, 'utf8').split('\n').filter(Boolean).reverse() : [],
            })
          : undefined;
        const history: string[] = [];
        let finished = false;
        const finish = (outcome: 'detached' | 'exited') => {
          if (finished) {
            return;
          }
          finished = true;
          if (history.length > 0) {
            try {
              appendFileSync(historyFile, `${history.join('\n')}\n`);
            } catch {}
          }
          socket.destroy();
          rl?.close();
          resolve(outcome);
        };

        let pending = '';
        socket.on('data', (chunk) => {
          pending += chunk.toString('utf8');
          for (let newline = pending.indexOf('\n'); newline !== -1; newline = pending.indexOf('\n')) {
            const line = pending.slice(0, newline);
            pending = pending.slice(newline + 1);
            const message = JSON.parse(line) as { t: string; data?: string; code?: number };
            if (message.t === 'out' && message.data) {
              process.stdout.write(rl ? `\r\x1b[K${message.data}` : message.data);
              if (rl && message.data.endsWith('\n')) {
                rl.prompt(true);
              }
            } else if (message.t === 'exit') {
              process.stdout.write(`\n[${instance.name} exited with code ${message.code}]\n`);
              finish('exited');
            }
          }
        });
        socket.on('close', () => finish('exited'));
        socket.on('error', () => finish('exited'));
        socket.write(`${JSON.stringify({ t: 'hello', replay: 200 })}\n`);

        if (rl) {
          rl.on('line', (line) => {
            if (line.trim()) {
              history.push(line);
              socket.write(`${JSON.stringify({ t: 'in', line })}\n`);
            }
            rl.prompt(true);
          });
          rl.on('SIGINT', () => {
            process.stdout.write('\ndetached (server still running)\n');
            finish('detached');
          });
          rl.on('close', () => {
            if (!finished) {
              process.stdout.write('\ndetached (server still running)\n');
              finish('detached');
            }
          });
          rl.prompt();
        }
      },
      () => resolve('exited'),
    );
  });
}
