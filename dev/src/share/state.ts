import { appendFileSync, existsSync, mkdirSync, readFileSync, renameSync, rmSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import type { ShareTarget } from '../config.ts';
import { RUN, log } from '../util.ts';
import type { TunnelAddress } from './provider.ts';

export const DIR = join(RUN, '.share');
export const LOG_FILE = join(DIR, 'share.log');
const PID_FILE = join(DIR, 'daemon.pid');
const STATE_FILE = join(DIR, 'state.json');

export interface TunnelState {
  status: 'starting' | 'live' | 'retrying' | 'failed';
  address?: TunnelAddress;
  /** PROXY protocol version the tunnel sends to its target */
  proxyProtocol?: string;
  openedAt?: string;
  expiresAt?: string;
  error?: string;
}

export interface ShareState {
  provider: string;
  pid: number;
  startedAt: string;
  tunnels: Partial<Record<ShareTarget, TunnelState>>;
}

/** The daemon's pid if it is alive and really is our daemon (pids are reused after a container restart). */
export function daemonPid(): number | undefined {
  let pid: number | undefined;
  try {
    pid = Number.parseInt(readFileSync(PID_FILE, 'utf8'), 10);
  } catch {
    return undefined;
  }
  let alive = Number.isInteger(pid);
  if (alive) {
    try {
      process.kill(pid, 0);
    } catch {
      alive = false;
    }
  }
  if (alive && existsSync('/proc/self/cmdline')) {
    try {
      // `_share` when detached, `share start --foreground` otherwise
      alive = readFileSync(`/proc/${pid}/cmdline`, 'utf8').includes('share');
    } catch {
      alive = false;
    }
  }
  if (!alive) {
    clearState();
    return undefined;
  }
  return pid;
}

export function writePid(): void {
  mkdirSync(DIR, { recursive: true });
  writeFileSync(PID_FILE, String(process.pid));
}

export function clearState(): void {
  rmSync(PID_FILE, { force: true });
  rmSync(STATE_FILE, { force: true });
}

/** The current state, or undefined when no daemon is running. */
export function readState(): ShareState | undefined {
  if (daemonPid() === undefined) {
    return undefined;
  }
  try {
    return JSON.parse(readFileSync(STATE_FILE, 'utf8')) as ShareState;
  } catch {
    return undefined;
  }
}

export function writeState(state: ShareState): void {
  mkdirSync(DIR, { recursive: true });
  writeFileSync(`${STATE_FILE}.tmp`, JSON.stringify(state));
  renameSync(`${STATE_FILE}.tmp`, STATE_FILE);
}

/** Whether the live proxy tunnel sends PROXY protocol headers, so velocity.toml must expect them. */
export function shareProxyProtocol(): boolean {
  const tunnel = readState()?.tunnels.proxy;
  return tunnel?.status === 'live' && tunnel.proxyProtocol !== undefined;
}

/** Tunnel addresses currently live for the targets that appear in proxy templates. */
export function shareAddresses(): { proxy?: TunnelAddress; limbo?: TunnelAddress } {
  const tunnels = readState()?.tunnels ?? {};
  return {
    proxy: tunnels.proxy?.status === 'live' ? tunnels.proxy.address : undefined,
    limbo: tunnels.limbo?.status === 'live' ? tunnels.limbo.address : undefined,
  };
}

export function rotateLog(): void {
  mkdirSync(DIR, { recursive: true });
  if (existsSync(LOG_FILE)) {
    renameSync(LOG_FILE, join(DIR, 'share.prev.log'));
  }
}

let foreground = false;

export function setForeground(value: boolean): void {
  foreground = value;
}

export function logLine(message: string): void {
  mkdirSync(DIR, { recursive: true });
  appendFileSync(LOG_FILE, `${new Date().toISOString()} ${message}\n`);
  if (foreground) {
    log(message);
  }
}

/** "renews in 12m" for a timestamp, or undefined. */
function relative(iso: string | undefined): string | undefined {
  if (!iso) {
    return undefined;
  }
  const minutes = Math.round((Date.parse(iso) - Date.now()) / 60_000);
  return minutes <= 0 ? 'renewing now' : `renews in ${minutes}m`;
}

/** One line per tunnel for `dev share status` and `dev status`. */
export function summaryLines(state: ShareState | undefined): string[] {
  if (!state) {
    return ['share      off'];
  }
  return Object.entries(state.tunnels).map(([name, tunnel]) => {
    const parts = [tunnel.address?.display ?? tunnel.status, relative(tunnel.expiresAt), tunnel.error && `error: ${tunnel.error}`];
    return `share ${name.padEnd(5)} ${tunnel.status.padEnd(8)} ${parts.filter(Boolean).join(', ')} (${state.provider})`;
  });
}
