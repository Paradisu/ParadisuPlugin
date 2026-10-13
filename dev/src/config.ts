import { existsSync } from 'node:fs';
import { readFile } from 'node:fs/promises';
import { join } from 'node:path';
import { parse } from 'smol-toml';
import { DEV, deepMerge, fail } from './util.ts';

/** paper = the Bukkit-family backends (Purpur), velocity = the proxy, limbo = the standalone Limbo server */
export type Platform = 'paper' | 'velocity' | 'limbo';

export interface JvmConfig {
  enhanced_redefinition: boolean;
  hotswap_agent: boolean;
  extra_flags: string[];
}

export interface ProxyConfig {
  port: number;
  /** Port clients use when it differs from `port` (for example when PARADISU_PROXY_PORT remaps it) */
  public_port?: number;
  debug_port: number;
  memory: string;
  first_start_commands: string[];
}

export interface LimboConfig {
  enabled: boolean;
  port: number;
  /** Port clients use when it differs from `port` (for example when PARADISU_LIMBO_PORT remaps it) */
  public_port?: number;
  debug_port: number;
  memory: string;
  first_start_commands: string[];
}

export interface ServerConfig {
  name: string;
  enabled: boolean;
  port: number;
  debug_port: number;
  memory: string;
  try: boolean;
  context: { server: string; warp: string; inventory: string };
  world_preset: 'redstone_ready' | 'void' | 'default';
  first_start_commands: string[];
}

export interface PluginConfig {
  key: string;
  source: 'modrinth' | 'url' | 'jenkins' | 'github' | 'local';
  platforms: Platform[];
  slug?: string;
  version?: string;
  allow_prerelease?: boolean;
  game_version_fallback?: string[];
  url?: string;
  /** jenkins: job URL, e.g. https://ci.example.com/job/My%20Job */
  job_url?: string;
  /** jenkins: regex for the artifact file name (default: any jar except sources/javadoc) */
  artifact_pattern?: string;
  /** github: owner/name; the latest release is used */
  repo?: string;
  /** github: regex for the release asset name (default: any jar except sources/javadoc) */
  asset_pattern?: string;
  urls?: Partial<Record<Platform, string>>;
  path?: string;
  config_dir?: Partial<Record<Platform, string>>;
}

export interface DevConfig {
  accept_eula: boolean;
  minecraft_version: string;
  purpur_build: string;
  velocity_version: string;
  limbo_version: string;
  /** Address clients use to reach this machine; the proxy tells players to transfer to it */
  public_host: string;
  online_mode: boolean;
  jvm: JvmConfig;
  proxy: ProxyConfig;
  limbo: LimboConfig;
  servers: ServerConfig[];
  plugins: PluginConfig[];
}

export interface DbEnv {
  host: string;
  port: string;
  name: string;
  user: string;
  password: string;
}

export interface Env {
  db: DbEnv;
  redisHost: string;
}

export function env(): Env {
  const e = process.env;
  return {
    db: {
      host: e.PARADISU_DB_HOST ?? 'localhost',
      port: e.PARADISU_DB_PORT ?? '5432',
      name: e.PARADISU_DB_NAME ?? 'paradisu',
      user: e.PARADISU_DB_USER ?? 'paradisu',
      password: e.PARADISU_DB_PASSWORD ?? 'paradisu',
    },
    redisHost: e.PARADISU_REDIS_HOST ?? 'localhost',
  };
}

/** Loads dev/dev.toml and deep-merges dev/dev.local.toml (gitignored) on top. */
export async function loadConfig(): Promise<DevConfig> {
  const basePath = join(DEV, 'dev.toml');
  const localPath = join(DEV, 'dev.local.toml');
  let merged = parse(await readFile(basePath, 'utf8')) as Record<string, unknown>;
  if (existsSync(localPath)) {
    const local = parse(await readFile(localPath, 'utf8')) as Record<string, unknown>;
    merged = mergeNamedLists(merged, local, ['servers', 'plugins']);
    merged = deepMerge(merged, withoutKeys(local, ['servers', 'plugins']));
  }
  const config = merged as unknown as DevConfig;
  const names = new Set<string>();
  for (const server of config.servers) {
    if (server.name === 'proxy' || server.name === 'limbo' || names.has(server.name)) {
      fail(`Invalid or duplicate server name "${server.name}" in dev config`);
    }
    names.add(server.name);
  }
  return config;
}

function withoutKeys(obj: Record<string, unknown>, keys: string[]): Record<string, unknown> {
  return Object.fromEntries(Object.entries(obj).filter(([key]) => !keys.includes(key)));
}

/**
 * Arrays of tables ([[servers]], [[plugins]]) are merged by their name/key so a local file can flip
 * `enabled` on one server without restating it; entries that do not exist in the base are appended.
 */
function mergeNamedLists(
  base: Record<string, unknown>,
  local: Record<string, unknown>,
  lists: string[],
): Record<string, unknown> {
  const result = { ...base };
  for (const list of lists) {
    const overrides = local[list] as Record<string, unknown>[] | undefined;
    if (!overrides) {
      continue;
    }
    const idKey = list === 'servers' ? 'name' : 'key';
    const merged = [...((base[list] as Record<string, unknown>[] | undefined) ?? [])];
    for (const override of overrides) {
      const index = merged.findIndex((entry) => entry[idKey] === override[idKey]);
      if (index === -1) {
        merged.push(override);
      } else {
        merged[index] = deepMerge(merged[index]!, override);
      }
    }
    result[list] = merged;
  }
  return result;
}
