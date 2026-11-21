import type { ExtensionApi } from '@mcdevtools/cli/extension';

/**
 * Template variables the Paradisu preset configs use (`.mcdevtools/templates/**`): the per-server context
 * (server / warp / inventory), the proxy's and Limbo's public addresses, and the project name for the MOTDs.
 */
export function registerContextVars(api: ExtensionApi): void {
  api.registerTemplateVars((config, instance, base) => {
    const context = (instance.config.extra.context ?? {}) as { server?: string; warp?: string; inventory?: string };
    const named = (name: string) => config.instances.find((candidate) => candidate.name === name);
    const proxy = named('proxy');
    const limbo = named('limbo');
    const publicOf = (name: string, fallbackPort: number | undefined) => ({
      host: base[`PUBLIC_HOST_${name.toUpperCase()}`] ?? config.public_host,
      port: base[`PUBLIC_PORT_${name.toUpperCase()}`] ?? String(fallbackPort ?? ''),
    });
    const proxyAddress = publicOf('proxy', proxy?.public_port ?? proxy?.port);
    const limboAddress = publicOf('limbo', limbo?.public_port ?? limbo?.port);
    return {
      PROJECT_NAME: 'Paradisu',
      CONTEXT_SERVER: context.server ?? '',
      CONTEXT_WARP: context.warp ?? '',
      CONTEXT_INVENTORY: context.inventory ?? '',
      PROXY_PUBLIC_HOST: proxyAddress.host,
      PROXY_PUBLIC_PORT: proxyAddress.port,
      LIMBO_PORT: String(limbo?.port ?? ''),
      LIMBO_PUBLIC_HOST: limboAddress.host,
      LIMBO_PUBLIC_PORT: limboAddress.port,
    };
  });
}
