import type { DevConfig } from '../../config.ts';
import { fail } from '../../util.ts';
import type { ProviderFactory, TunnelProvider } from '../provider.ts';
import { pinggyFactory } from './pinggy.ts';

const FACTORIES: ProviderFactory[] = [pinggyFactory];

export function providers(): ProviderFactory[] {
  return FACTORIES;
}

export function providerNames(): string[] {
  return FACTORIES.map((factory) => factory.name);
}

export function factory(name: string): ProviderFactory {
  return FACTORIES.find((candidate) => candidate.name === name) ?? fail(`Unknown share provider "${name}". Available: ${providerNames().join(', ')}`);
}

/** The configured provider (or the named override), created from its [share.providers.<name>] table. */
export function createProvider(config: DevConfig, override?: string): Promise<TunnelProvider> {
  const name = override ?? config.share.provider;
  return factory(name).create(config.share.providers?.[name] ?? {});
}
