import type { DevConfig } from '../config.ts';
import type { Instance } from '../templates.ts';
import type { ConnMeta } from './store.ts';

/**
 * Who receives a packet. `dir` is 0 for serverbound, 1 for clientbound.
 *
 * - proxy frontend (client link): serverbound reaches the proxy, clientbound reaches the client
 * - proxy backend link: serverbound reaches the backend (found by its port), clientbound reaches the proxy
 * - paper backend: its peer is the proxy
 * - limbo: players are transferred straight to it, so its peer is the client
 */
export function receiverOf(config: DevConfig, instance: Instance, conn: ConnMeta, dir: number): string {
  if (instance.platform === 'velocity') {
    if (conn.leg === 0) {
      return dir === 0 ? instance.name : 'client';
    }
    return dir === 0 ? backendName(config, conn.remote) : instance.name;
  }
  if (dir === 0) {
    return instance.name;
  }
  return instance.platform === 'paper' ? 'proxy' : 'client';
}

/** `from→to` for a packet; the sender is whoever receives the opposite direction on the same link. */
export function linkOf(config: DevConfig, instance: Instance, conn: ConnMeta, dir: number): string {
  return `${receiverOf(config, instance, conn, 1 - dir)}→${receiverOf(config, instance, conn, dir)}`;
}

function backendName(config: DevConfig, remote: string): string {
  const port = Number(remote.slice(remote.lastIndexOf(':') + 1));
  if (config.limbo.enabled && config.limbo.port === port) {
    return 'limbo';
  }
  return config.servers.find((server) => server.port === port)?.name ?? remote;
}
