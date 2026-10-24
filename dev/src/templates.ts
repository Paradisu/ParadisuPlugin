import { randomBytes } from 'node:crypto';
import { existsSync } from 'node:fs';
import { mkdir, readFile, readdir, writeFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { type DevConfig, type Env, type Platform, type ServerConfig, env } from './config.ts';
import { shareAddresses, shareProxyProtocol } from './share/state.ts';
import { RUN, TEMPLATES, TEMPLATES_LOCAL, render } from './util.ts';

export interface Instance {
  /** `proxy` or the server name */
  name: string;
  platform: Platform;
  dir: string;
  port: number;
  debugPort: number;
  memory: string;
  firstStartCommands: string[];
  enabled: boolean;
  server?: ServerConfig;
}

export function instancesOf(config: DevConfig): Instance[] {
  const proxy: Instance = {
    name: 'proxy',
    platform: 'velocity',
    dir: join(RUN, 'proxy'),
    port: config.proxy.port,
    debugPort: config.proxy.debug_port,
    memory: config.proxy.memory,
    firstStartCommands: config.proxy.first_start_commands,
    enabled: true,
  };
  const limbo: Instance = {
    name: 'limbo',
    platform: 'limbo',
    dir: join(RUN, 'limbo'),
    port: config.limbo.port,
    debugPort: config.limbo.debug_port,
    memory: config.limbo.memory,
    firstStartCommands: config.limbo.first_start_commands,
    enabled: config.limbo.enabled,
  };
  const servers = config.servers.map<Instance>((server) => ({
    name: server.name,
    platform: 'paper',
    dir: join(RUN, server.name),
    port: server.port,
    debugPort: server.debug_port,
    memory: server.memory,
    firstStartCommands: server.first_start_commands,
    enabled: server.enabled,
    server,
  }));
  return [proxy, limbo, ...servers];
}

const WORLD_PRESETS = {
  redstone_ready: {
    levelType: 'minecraft\\:flat',
    generatorSettings:
      '{"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:stone","height":3},{"block":"minecraft:sandstone","height":116}],"biome":"minecraft:desert","structure_overrides":[]}',
  },
  void: {
    levelType: 'minecraft\\:flat',
    generatorSettings: '{"layers":[{"block":"minecraft:air","height":1}],"biome":"minecraft:the_void","structure_overrides":[]}',
  },
  default: { levelType: 'minecraft\\:normal', generatorSettings: '' },
} as const;

async function forwardingSecret(): Promise<string> {
  const path = join(RUN, 'forwarding.secret');
  if (existsSync(path)) {
    return (await readFile(path, 'utf8')).trim();
  }
  const secret = randomBytes(24).toString('base64url');
  await mkdir(RUN, { recursive: true });
  await writeFile(path, secret);
  return secret;
}

/** Variables available to every template as ${NAME}. */
export async function templateVars(config: DevConfig, instance: Instance): Promise<Record<string, string>> {
  const e: Env = env();
  const preset = WORLD_PRESETS[instance.server?.world_preset ?? 'default'];
  const velocityServers = config.servers
    .filter((server) => server.enabled)
    .map((server) => `${server.name} = "127.0.0.1:${server.port}"`)
    .join('\n');
  const velocityTry = config.servers.filter((server) => server.enabled && server.try).map((server) => `"${server.name}"`);
  // Live `dev share` tunnels replace the configured public address
  const shared = shareAddresses();
  return {
    PROXY_PORT: String(config.proxy.port),
    HAPROXY_PROTOCOL: String(shareProxyProtocol()),
    PROXY_PUBLIC_HOST: shared.proxy?.host ?? config.public_host,
    PROXY_PUBLIC_PORT: String(shared.proxy?.port ?? config.proxy.public_port ?? config.proxy.port),
    PUBLIC_HOST: config.public_host,
    LIMBO_PORT: String(config.limbo.port),
    LIMBO_PUBLIC_HOST: shared.limbo?.host ?? config.public_host,
    LIMBO_PUBLIC_PORT: String(shared.limbo?.port ?? config.limbo.public_port ?? config.limbo.port),
    SERVER_NAME: instance.name,
    SERVER_PORT: String(instance.port),
    ONLINE_MODE: String(config.online_mode),
    FORWARDING_SECRET: await forwardingSecret(),
    DB_HOST: e.db.host,
    DB_NAME: e.db.name,
    DB_USER: e.db.user,
    DB_PASSWORD: e.db.password,
    DB_URL: `jdbc:postgresql://${e.db.host}:${e.db.port}/${e.db.name}`,
    REDIS_HOST: e.redisHost,
    CONTEXT_SERVER: instance.server?.context.server ?? '',
    CONTEXT_WARP: instance.server?.context.warp ?? '',
    CONTEXT_INVENTORY: instance.server?.context.inventory ?? '',
    LEVEL_TYPE: preset.levelType,
    GENERATOR_SETTINGS: preset.generatorSettings,
    VELOCITY_SERVERS: velocityServers,
    VELOCITY_TRY: `[${velocityTry.join(', ')}]`,
  };
}

async function overlay(from: string, to: string, vars: Record<string, string>): Promise<void> {
  if (!existsSync(from)) {
    return;
  }
  for (const entry of await readdir(from, { withFileTypes: true, recursive: true })) {
    if (!entry.isFile()) {
      continue;
    }
    const source = join(entry.parentPath, entry.name);
    const relative = source.slice(from.length + 1);
    const dest = join(to, relative);
    await mkdir(dirname(dest), { recursive: true });
    await writeFile(dest, render(await readFile(source, 'utf8'), vars));
  }
}

/**
 * Renders the preset configs into the instance directory, always overwriting: base platform template,
 * then servers/<name>, then each installed plugin's preset. Files no template names are never touched.
 */
export async function applyTemplates(
  config: DevConfig,
  instance: Instance,
  installedPlugins: string[],
): Promise<void> {
  const vars = await templateVars(config, instance);
  const base = { velocity: 'proxy', paper: 'paper-common', limbo: 'limbo' }[instance.platform];
  await overlay(join(TEMPLATES, base), instance.dir, vars);
  await overlay(join(TEMPLATES, 'servers', instance.name), instance.dir, vars);
  for (const key of installedPlugins) {
    const plugin = config.plugins.find((candidate) => candidate.key === key);
    const folder = plugin?.config_dir?.[instance.platform] ?? key;
    await overlay(
      join(TEMPLATES, 'plugins', key, instance.platform),
      join(instance.dir, 'plugins', folder),
      vars,
    );
  }
  // Personal overrides win over everything above (same layout as dev/templates); profiles carry them
  await overlay(join(TEMPLATES_LOCAL, base), instance.dir, vars);
  await overlay(join(TEMPLATES_LOCAL, 'servers', instance.name), instance.dir, vars);
  for (const key of installedPlugins) {
    const plugin = config.plugins.find((candidate) => candidate.key === key);
    const folder = plugin?.config_dir?.[instance.platform] ?? key;
    await overlay(join(TEMPLATES_LOCAL, 'plugins', key, instance.platform), join(instance.dir, 'plugins', folder), vars);
  }
}
