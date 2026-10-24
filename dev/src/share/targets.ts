import type { DevConfig, ShareTarget } from '../config.ts';
import { fail } from '../util.ts';
import type { TunnelAddress, TunnelProtocol } from './provider.ts';

export interface TargetSpec {
  name: ShareTarget;
  protocol: TunnelProtocol;
  /** Players connect here with a Minecraft client, so the real address is worth forwarding (PROXY protocol) */
  clientFacing: boolean;
  port(config: DevConfig): number;
  /** Why it can't be shared right now, or undefined */
  unavailable(config: DevConfig): string | undefined;
  /** Help text printed next to the address */
  describe(address: TunnelAddress, config: DevConfig): string;
}

export const TARGETS: Record<ShareTarget, TargetSpec> = {
  proxy: {
    name: 'proxy',
    protocol: 'tcp',
    clientFacing: true,
    port: (config) => config.proxy.port,
    unavailable: () => undefined,
    describe: (address) => `join: ${address.display}`,
  },
  limbo: {
    name: 'limbo',
    protocol: 'tcp',
    clientFacing: false,
    port: (config) => config.limbo.port,
    unavailable: (config) => (config.limbo.enabled ? undefined : 'limbo is disabled in the dev config'),
    describe: (address) => `${address.display} (maintenance waiting room; players are transferred here)`,
  },
  web: {
    name: 'web',
    protocol: 'http',
    clientFacing: false,
    port: (config) => config.web.port,
    unavailable: (config) =>
      config.web.bind === '127.0.0.1' ? undefined : 'the web target needs web.bind = "127.0.0.1" (the tunnel provides the HTTPS)',
    describe: (address, config) =>
      `${address.url}/${config.ui.session} (log in with a token from \`dev web token --read-only\`)`,
  },
};

/** Validates target names; keeps first occurrences only. */
export function parseTargets(names: string[]): ShareTarget[] {
  const targets: ShareTarget[] = [];
  for (const name of names) {
    if (!(name in TARGETS)) {
      fail(`Unknown share target "${name}". Available: ${Object.keys(TARGETS).join(', ')}`);
    }
    if (!targets.includes(name as ShareTarget)) {
      targets.push(name as ShareTarget);
    }
  }
  return targets;
}
