import type { DevConfig, ShareTarget } from '../config.ts';
import { isRunning, rerender, select } from '../instances.ts';
import { sendLine } from '../supervisor.ts';
import { sleep } from '../util.ts';
import { backoffMs, renewDelay } from './address.ts';
import type { TunnelHandle, TunnelProvider } from './provider.ts';
import { createProvider } from './providers/index.ts';
import { shareProxyProtocol, type ShareState, type TunnelState, clearState, logLine, rotateLog, setForeground, writePid, writeState } from './state.ts';
import { TARGETS } from './targets.ts';

/** Consecutive failures after which a tunnel is reported as failed (it keeps retrying). */
const FAILED_AFTER = 5;

interface Slot {
  handle?: TunnelHandle;
  /** Bumped whenever the tunnel is replaced or stopped on purpose, so late close events are ignored */
  generation: number;
  attempt: number;
  retryTimer?: NodeJS.Timeout;
}

/**
 * Entry point of `dev _share`: owns the tunnels until it receives SIGTERM/SIGINT. It reopens tunnels that drop,
 * renews them before the provider's limit, and keeps the proxy's public addresses in step (re-render + reload).
 */
export async function runDaemon(config: DevConfig, targets: ShareTarget[], providerName: string, foreground: boolean): Promise<void> {
  setForeground(foreground);
  rotateLog();
  process.on('uncaughtException', (error) => {
    logLine(`crashed: ${error.stack ?? error}`);
    process.exit(1);
  });
  process.on('SIGHUP', () => {});

  const provider: TunnelProvider = await createProvider(config, providerName);
  const state: ShareState = { provider: provider.name, pid: process.pid, startedAt: new Date().toISOString(), tunnels: {} };
  const slots = new Map<ShareTarget, Slot>(targets.map((name) => [name, { generation: 0, attempt: 0 }]));
  for (const name of targets) {
    state.tunnels[name] = { status: 'starting' };
  }
  writePid();
  writeState(state);

  let stopping = false;
  let renewTimer: NodeJS.Timeout | undefined;
  let applied = '';

  const update = (name: ShareTarget, tunnel: TunnelState) => {
    state.tunnels[name] = tunnel;
    writeState(state);
  };

  /** Writes the state and, when the proxy/limbo addresses changed, re-renders the proxy config and reloads it. */
  const applyAddresses = async (): Promise<void> => {
    writeState(state);
    const live = (name: ShareTarget) => (state.tunnels[name]?.status === 'live' ? state.tunnels[name]?.address?.display : undefined);
    const protocol = shareProxyProtocol();
    const key = `${live('proxy')}|${live('limbo')}|${protocol}`;
    if (key === applied) {
      return;
    }
    applied = key;
    try {
      if (!(await rerender(config, 'proxy'))) {
        return;
      }
      const proxy = select(config, ['proxy'])[0]!;
      if (isRunning(proxy)) {
        await sendLine(proxy, 'vparadisu reload');
        // haproxy-protocol is read by Velocity's own reload; without it the listener keeps expecting the old framing
        await sendLine(proxy, 'velocity reload');
        logLine('proxy reloaded with the new transfer addresses');
      }
    } catch (error) {
      logLine(`could not update the proxy: ${(error as Error).message}`);
    }
  };

  const openTarget = async (name: ShareTarget): Promise<void> => {
    const slot = slots.get(name)!;
    const generation = ++slot.generation;
    update(name, { status: 'starting' });
    const spec = TARGETS[name];
    const proxyProtocol =
      spec.clientFacing && config.share.proxy_protocol !== 'off' && provider.capabilities.proxyProtocol ? config.share.proxy_protocol : undefined;
    try {
      const handle = await provider.open(
        { name, protocol: spec.protocol, localHost: '127.0.0.1', localPort: spec.port(config), proxyProtocol },
        {
          onClosed: (reason) => {
            if (!stopping && generation === slot.generation) {
              slot.handle = undefined;
              scheduleReopen(name, reason);
            }
          },
        },
      );
      if (stopping || generation !== slot.generation) {
        await handle.stop();
        return;
      }
      slot.handle = handle;
      slot.attempt = 0;
      update(name, {
        status: 'live',
        address: handle.address,
        proxyProtocol,
        openedAt: new Date().toISOString(),
        expiresAt: handle.expiresAt === undefined ? undefined : new Date(handle.expiresAt).toISOString(),
      });
      logLine(`${name}: ${handle.address.display}`);
      scheduleRenewal();
    } catch (error) {
      if (generation === slot.generation && !stopping) {
        scheduleReopen(name, (error as Error).message);
      }
    }
  };

  const scheduleReopen = (name: ShareTarget, reason: string): void => {
    const slot = slots.get(name)!;
    const delay = backoffMs(slot.attempt);
    slot.attempt++;
    update(name, { status: slot.attempt >= FAILED_AFTER ? 'failed' : 'retrying', error: reason });
    logLine(`${name} closed: ${reason}; retrying in ${Math.round(delay / 1000)}s`);
    void applyAddresses();
    slot.generation++;
    slot.retryTimer = setTimeout(async () => {
      slot.retryTimer = undefined;
      if (!stopping) {
        await openTarget(name);
        await applyAddresses();
      }
    }, delay);
  };

  const renewAll = async (): Promise<void> => {
    if (stopping) {
      return;
    }
    logLine(
      provider.capabilities.stableAddresses
        ? 'renewing tunnels (provider limit)'
        : 'renewing tunnels (provider limit); the addresses will change',
    );
    // Break before make: free tiers may allow only one tunnel per IP
    for (const name of targets) {
      const slot = slots.get(name)!;
      slot.generation++;
      const handle = slot.handle;
      slot.handle = undefined;
      await handle?.stop();
    }
    for (const name of targets) {
      await openTarget(name);
    }
    await applyAddresses();
  };

  const scheduleRenewal = (): void => {
    clearTimeout(renewTimer);
    const expiries = [...slots.values()].flatMap((slot) => (slot.handle?.expiresAt === undefined ? [] : [slot.handle.expiresAt]));
    const delay = renewDelay(expiries.length > 0 ? Math.min(...expiries) : undefined, Date.now(), config.share.renew_before_seconds * 1000);
    if (delay !== undefined) {
      renewTimer = setTimeout(() => void renewAll(), delay);
    }
  };

  const shutdown = async (): Promise<void> => {
    if (stopping) {
      return;
    }
    stopping = true;
    clearTimeout(renewTimer);
    await Promise.allSettled(
      [...slots.values()].map(async (slot) => {
        clearTimeout(slot.retryTimer);
        slot.generation++;
        await slot.handle?.stop();
      }),
    );
    // With no live tunnels left the proxy goes back to the configured public_host
    state.tunnels = {};
    await applyAddresses();
    clearState();
    logLine('stopped');
    process.exit(0);
  };
  process.on('SIGTERM', () => void shutdown());
  process.on('SIGINT', () => void shutdown());

  logLine(`sharing ${targets.join(', ')} through ${provider.name}`);
  // Sequential and in this order, so if a concurrency limit hits, the most important target is already up
  for (const name of targets) {
    await openTarget(name);
  }
  await applyAddresses();
  // Keep the event loop alive even if every tunnel is down and no timer is pending
  for (;;) {
    await sleep(60_000);
  }
}
