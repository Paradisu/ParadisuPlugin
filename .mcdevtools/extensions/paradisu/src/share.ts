import type { ExtensionApi } from '@mcdevtools/cli/extension';
import { warn } from '@mcdevtools/cli/util';

/** What the Paradisu proxy needs when `mcdev share` changes the public addresses. */
export function registerShareHooks(api: ExtensionApi): void {
  // The proxy re-reads its (re-rendered) Paradisu config; mcdevtools then runs `velocity reload` itself
  api.on('shareAddressesChanged', async (_config, proxy) => {
    await api.sendConsole(proxy, 'vparadisu reload');
  });
  api.on('afterShareStart', (config, state) => {
    const limbo = config.instances.find((i) => i.type === 'limbo' && i.enabled);
    const proxyShared = config.instances.some((i) => i.type === 'velocity' && state.tunnels[i.name]);
    if (limbo && proxyShared && !state.tunnels[limbo.name]) {
      warn(`${limbo.name} is not shared; maintenance mode will drop remote players. Add the ${limbo.name} target.`);
    }
  });
}
