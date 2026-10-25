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
  /** An exact artifact per platform, written by profile imports; setup downloads it instead of resolving the latest */
  pinned?: Partial<Record<Platform, PinnedArtifact>>;
}

export interface PinnedArtifact {
  url: string;
  sha256: string;
  version: string;
  /** A jar shipped inside a profile, relative to the repository root; used instead of `url` */
  file?: string;
}

export interface UiConfig {
  /** Layout `dev ui` opens: network | tabs | the name of a file in dev/layouts or dev/layouts.local */
  default_layout: string;
  /** Name of the zellij session */
  session: string;
}

export interface WebConfig {
  port: number;
  /** 127.0.0.1 = plain HTTP, reachable through port forwarding; anything else needs TLS (a self-signed certificate is generated) */
  bind: string;
}

export type ShareTarget = 'proxy' | 'limbo' | 'web';

export interface ShareConfig {
  /** Tunnel service: a name from `dev share providers` */
  provider: string;
  /** Targets `dev share start` opens when none are named */
  targets: ShareTarget[];
  /** Renew this long before a provider's tunnel limit so the switch happens on our schedule */
  renew_before_seconds: number;
  /** PROXY protocol sent to the proxy so Velocity sees real player addresses; "off" keeps direct connections working while sharing */
  proxy_protocol: 'off' | 'v1' | 'v2';
  /** Per-provider options: [share.providers.<name>] */
  providers: Record<string, Record<string, unknown>>;
}

export interface CaptureConfig {
  /** Install the capture plugin (devtools/capture) on every instance */
  enabled: boolean;
  /** Dump compression, 1 (fast) to 9 (small); runs on a background thread */
  gzip_level: number;
  /** Default per-instance cap on captured (uncompressed) bytes, e.g. "512M" */
  max_size: string;
  /** Packets buffered for the writer; when full, packets are dropped and counted instead of blocking the server */
  queue_capacity: number;
}

export interface DevConfig {
  accept_eula: boolean;
  minecraft_version: string;
  purpur_build: string;
  velocity_version: string;
  /** "auto" = latest build of velocity_version, or a Fill build id (set by profile imports to pin the exact build) */
  velocity_build: string;
  /** Overrides the DataVersion detected from the server jar (for generated items) */
  data_version?: number;
  limbo_version: string;
  /** Address clients use to reach this machine; the proxy tells players to transfer to it */
  public_host: string;
  online_mode: boolean;
  jvm: JvmConfig;
  proxy: ProxyConfig;
  limbo: LimboConfig;
  ui: UiConfig;
  web: WebConfig;
  share: ShareConfig;
  capture: CaptureConfig;
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
