import { fail } from '../../util.ts';
import { parseTunnelUrls } from '../address.ts';
import type { ProviderFactory, TunnelEvents, TunnelHandle, TunnelRequest } from '../provider.ts';

/** Without a token Pinggy ends every tunnel after 60 minutes. */
const FREE_LIFETIME_MINUTES = 60;

function optional<T>(options: Record<string, unknown>, key: string, type: 'string' | 'number'): T | undefined {
  const value = options[key];
  if (value === undefined || value === '') {
    return undefined;
  }
  if (typeof value !== type) {
    fail(`share.providers.pinggy.${key} must be a ${type}`);
  }
  return value as T;
}

export const pinggyFactory: ProviderFactory = {
  name: 'pinggy',
  summary: 'Pinggy (pinggy.io): TCP and HTTPS tunnels; the free tier lasts 60 minutes and gets random addresses',
  async create(options) {
    const token = optional<string>(options, 'token', 'string') ?? (process.env.PINGGY_TOKEN || undefined);
    const serverAddress = optional<string>(options, 'server_address', 'string');
    const lifetime = optional<number>(options, 'max_lifetime_minutes', 'number') ?? (token ? 0 : FREE_LIFETIME_MINUTES);
    // Third-party docs claim one free tunnel per IP; two concurrent tunnels were verified to work, so no default limit
    const maxConcurrent = optional<number>(options, 'max_concurrent', 'number');

    let sdk: typeof import('@pinggy/pinggy');
    try {
      sdk = await import('@pinggy/pinggy');
    } catch (error) {
      fail(
        `The Pinggy SDK could not be loaded (${(error as Error).message}). It is an optional dependency with a native ` +
          'addon: run `pnpm --dir dev install` and check its output.',
      );
    }
    sdk.pinggy.setDebugLogging(false);

    return {
      name: 'pinggy',
      capabilities: { protocols: ['tcp', 'http'], proxyProtocol: true, maxConcurrent, stableAddresses: Boolean(token) },
      async open(request: TunnelRequest, events: TunnelEvents): Promise<TunnelHandle> {
        const local = `${request.localHost}:${request.localPort}`;
        const tunnel = await sdk.pinggy.createTunnel({
          // The structured form is required for tcp when haProxy is set (the "tcp://" string prefix fails its validation)
          forwarding: request.protocol === 'tcp' ? [{ address: local, type: 'tcp' as never }] : local,
          ...(token ? { token } : {}),
          ...(serverAddress ? { serverAddress } : {}),
          // The share daemon owns reconnection: a reconnect would silently change the address
          autoReconnect: false,
          ...(request.protocol === 'http' ? { httpsOnly: true } : {}),
          // HaProxyVersion is a const enum in the typings; its values are the plain strings "v1" and "v2"
          ...(request.proxyProtocol ? { haProxy: request.proxyProtocol as never } : {}),
        });
        let stopped = false;
        const closed = (reason: string) => {
          if (!stopped) {
            stopped = true;
            events.onClosed(reason);
          }
        };
        tunnel.setTunnelDisconnectedCallback(({ error, messages }) => closed(error || messages.join('; ') || 'disconnected'));
        tunnel.setTunnelErrorCallback(({ error, recoverable }) => {
          if (!recoverable) {
            closed(error);
          }
        });
        const address = parseTunnelUrls(request.protocol, await tunnel.start());
        return {
          address,
          expiresAt: lifetime > 0 ? Date.now() + lifetime * 60_000 : undefined,
          async stop() {
            stopped = true;
            await tunnel.stop().catch(() => {});
          },
        };
      },
    };
  },
};
