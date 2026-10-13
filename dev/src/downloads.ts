import { copyFile, mkdir, readFile, writeFile } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import { join, resolve } from 'node:path';
import type { DevConfig, Platform } from './config.ts';
import { CACHE, ROOT, download, fail, log, warn } from './util.ts';
import { resolvePlugin, resolveServers } from './versions.ts';
import type { Artifact } from './versions.ts';

const RESOLVED = join(CACHE, 'resolved.json');

export interface ResolvedFiles {
  minecraft: string;
  purpur: { build: string; file: string };
  velocity: { version: string; build: string; file: string };
  limbo?: { version: string; file: string };
  plugins: { key: string; platform: Platform; file: string; name: string; version: string }[];
}

export async function readResolved(): Promise<ResolvedFiles> {
  if (!existsSync(RESOLVED)) {
    fail('Nothing downloaded yet. Run `dev setup` first.');
  }
  return JSON.parse(await readFile(RESOLVED, 'utf8')) as ResolvedFiles;
}

async function fetchArtifact(artifact: Artifact, subdir: string, fileName: string, force: boolean): Promise<string> {
  const dest = join(CACHE, subdir, fileName);
  if (force || !existsSync(dest)) {
    log(`  downloading ${artifact.name}`);
    await download(artifact.url, dest, { md5: artifact.md5, sha256: artifact.sha256, sha512: artifact.sha512 });
  }
  return dest;
}

/** Resolves versions, downloads everything into dev/.cache and records the result in resolved.json. */
export async function setup(config: DevConfig, refresh: boolean): Promise<void> {
  log('Resolving server versions');
  const servers = await resolveServers(config);
  log(`  Minecraft ${servers.minecraft}, Purpur build ${servers.purpur.build}, Velocity ${servers.velocity.version} build ${servers.velocity.build}`);

  const purpurFile = await fetchArtifact(servers.purpur, 'servers', servers.purpur.name, false);
  const velocityFile = await fetchArtifact(
    servers.velocity,
    'servers',
    `velocity-${servers.velocity.version}-${servers.velocity.build}.jar`,
    false,
  );

  let limbo: ResolvedFiles['limbo'];
  if (servers.limbo) {
    log(`  Limbo ${servers.limbo.version}`);
    limbo = {
      version: servers.limbo.version,
      file: await fetchArtifact(servers.limbo, 'servers', servers.limbo.name, false),
    };
  }

  const plugins: ResolvedFiles['plugins'] = [];
  const failures: string[] = [];
  for (const plugin of config.plugins) {
    for (const platform of plugin.platforms) {
      const label = `${plugin.key} (${platform})`;
      try {
        if (plugin.source === 'local') {
          if (!plugin.path) {
            fail('source = "local" requires path');
          }
          const source = resolve(ROOT, plugin.path);
          if (!existsSync(source)) {
            fail(`${plugin.path} does not exist`);
          }
          const dest = join(CACHE, 'plugins', platform, `${plugin.key}.jar`);
          await mkdir(join(CACHE, 'plugins', platform), { recursive: true });
          await copyFile(source, dest);
          plugins.push({ key: plugin.key, platform, file: dest, name: `${plugin.key}.jar`, version: 'local' });
          log(`  ${label}: local ${plugin.path}`);
          continue;
        }
        const artifact = await resolvePlugin(plugin, platform, servers.minecraft);
        // URL sources have no checksum, so they are re-downloaded on --refresh only
        const file = await fetchArtifact(
          artifact,
          join('plugins', platform),
          `${plugin.key}-${artifact.version.replace(/[^A-Za-z0-9._-]/g, '_')}.jar`,
          refresh && plugin.source === 'url',
        );
        plugins.push({ key: plugin.key, platform, file, name: `${plugin.key}.jar`, version: artifact.version });
        log(`  ${label}: ${artifact.version}`);
      } catch (error) {
        failures.push(`${label}: ${(error as Error).message}`);
      }
    }
  }

  await mkdir(CACHE, { recursive: true });
  const resolved: ResolvedFiles = {
    minecraft: servers.minecraft,
    purpur: { build: servers.purpur.build, file: purpurFile },
    velocity: { version: servers.velocity.version, build: servers.velocity.build, file: velocityFile },
    limbo,
    plugins,
  };
  await writeFile(RESOLVED, JSON.stringify(resolved, null, 2));

  if (failures.length > 0) {
    for (const failure of failures) {
      warn(failure);
    }
    fail(`${failures.length} plugin download(s) failed (the rest were downloaded)`);
  }
  log('Setup complete');
}
