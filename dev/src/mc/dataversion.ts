import { existsSync } from 'node:fs';
import { basename, join } from 'node:path';
import { readResolved } from '../downloads.ts';
import type { DevConfig } from '../config.ts';
import { CACHE, capture, fail } from '../util.ts';

/** `world_version` from the top-level version.json of a server jar (Paper, Purpur and Velocity-free). */
export function dataVersionOfJar(jar: string): number | undefined {
  if (!existsSync(jar)) return undefined;
  const result = capture('unzip', ['-p', jar, 'version.json']);
  if (result.status !== 0) return undefined;
  try {
    const version = (JSON.parse(result.stdout) as { world_version?: number }).world_version;
    return typeof version === 'number' ? version : undefined;
  } catch {
    return undefined;
  }
}

/**
 * DataVersion stamped on generated items: `data_version` in dev config, else the downloaded Purpur jar's. Items
 * written with an older version are upgraded by the server's data fixers; a newer one is refused, so don't guess.
 */
export async function dataVersion(config: DevConfig & { data_version?: number }): Promise<number> {
  if (config.data_version) return config.data_version;
  const resolved = await readResolved().catch(() => undefined);
  const found = resolved
    ? // resolved.json records absolute paths; the checkout may have moved since (devcontainer vs host)
      (dataVersionOfJar(resolved.purpur.file) ?? dataVersionOfJar(join(CACHE, 'servers', basename(resolved.purpur.file))))
    : undefined;
  return found ?? fail('Cannot determine the Minecraft DataVersion: run `dev setup`, or set data_version in dev/dev.local.toml');
}
