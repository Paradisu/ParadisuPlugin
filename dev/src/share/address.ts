import { DevError } from '../util.ts';
import type { TunnelAddress, TunnelProtocol } from './provider.ts';

/** Picks the public address out of the URLs a provider returned for a tunnel. */
export function parseTunnelUrls(protocol: TunnelProtocol, urls: string[]): TunnelAddress {
  if (protocol === 'tcp') {
    const raw = urls.find((url) => url.startsWith('tcp://'));
    if (raw) {
      const parsed = new URL(raw);
      const port = Number.parseInt(parsed.port, 10);
      if (parsed.hostname && Number.isInteger(port)) {
        return { display: `${parsed.hostname}:${port}`, host: parsed.hostname, port };
      }
    }
  } else {
    const raw = urls.find((url) => url.startsWith('https://')) ?? urls.find((url) => url.startsWith('http://'));
    if (raw) {
      const parsed = new URL(raw);
      const secure = parsed.protocol === 'https:';
      const port = parsed.port ? Number.parseInt(parsed.port, 10) : secure ? 443 : 80;
      const url = `${parsed.protocol}//${parsed.host}`;
      return { display: url, host: parsed.hostname, port, url };
    }
  }
  throw new DevError(`The tunnel provider returned no usable ${protocol} address: ${JSON.stringify(urls)}`);
}

/** Milliseconds until a tunnel should be renewed, or undefined when it has no expiry. */
export function renewDelay(expiresAt: number | undefined, now: number, renewBeforeMs: number): number | undefined {
  return expiresAt === undefined ? undefined : Math.max(0, expiresAt - renewBeforeMs - now);
}

/** Retry delay after the nth consecutive failure (0-based): 5 s, 10 s, 20 s ... capped at 5 minutes. */
export function backoffMs(attempt: number): number {
  return Math.min(5000 * 2 ** attempt, 300_000);
}
