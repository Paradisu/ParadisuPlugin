import { copyFile, mkdir, readFile, writeFile } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import { join, resolve, sep } from 'node:path';
import type { DevConfig, Platform, PluginConfig } from './config.ts';
import { CACHE, ROOT, download, fail, log, sha256File, warn } from './util.ts';
import { resolvePlugin, resolveServers } from './versions.ts';
import type { Artifact } from './versions.ts';

const RESOLVED = join(CACHE, 'resolved.json');

/** What was downloaded, with enough detail (url, sha256) for a profile to pin and re-download exactly the same files */
export interface ResolvedArtifact {
  file: string;
  url?: string;
  sha256: string;
}

export interface ResolvedFiles {
  minecraft: string;
  purpur: ResolvedArtifact & { build: string };
  velocity: ResolvedArtifact & { version: string; build: string };
  limbo?: ResolvedArtifact & { version: string };
  plugins: (ResolvedArtifact & {
    key: string;
    platform: Platform;
    name: string;
    version: string;
    /** The url keeps pointing at the same bytes (versioned releases); false for "latest" links and local jars */
    immutable: boolean;
  })[];
}

function pinFailureHint(plugin: PluginConfig, platform: Platform, message: string): string {
  return plugin.pinned?.[platform] && /sha256 mismatch/.test(message)
    ? `${message}\n  The upstream file changed since the profile was exported; ask for a profile exported with --embed-jars.`
    : message;
}

export async function readResolved(): Promise<ResolvedFiles> {
  if (!existsSync(RESOLVED)) {
    fail('Nothing downloaded yet. Run `dev setup` first.');
  }
  const resolved = JSON.parse(await readFile(RESOLVED, 'utf8')) as ResolvedFiles;
  // Paths were recorded as absolute; the checkout may have moved since (devcontainer /workspaces vs a host path)
  const marker = `${sep}.cache${sep}`;
  const relocate = (file: string): string => {
    const at = file.lastIndexOf(marker);
    return at === -1 ? file : join(CACHE, file.slice(at + marker.length));
  };
  resolved.purpur.file = relocate(resolved.purpur.file);
  resolved.velocity.file = relocate(resolved.velocity.file);
  if (resolved.limbo) resolved.limbo.file = relocate(resolved.limbo.file);
  for (const plugin of resolved.plugins) plugin.file = relocate(plugin.file);
  return resolved;
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
    const limboFile = await fetchArtifact(servers.limbo, 'servers', servers.limbo.name, false);
    limbo = { version: servers.limbo.version, file: limboFile, url: servers.limbo.url, sha256: sha256File(limboFile) };
  }

  const plugins: ResolvedFiles['plugins'] = [];
  const failures: string[] = [];
  for (const plugin of config.plugins) {
    for (const platform of plugin.platforms) {
      const label = `${plugin.key} (${platform})`;
      try {
        if (plugin.source === 'local' && !plugin.pinned?.[platform]) {
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
          plugins.push({
            key: plugin.key,
            platform,
            file: dest,
            name: `${plugin.key}.jar`,
            version: 'local',
            sha256: sha256File(dest),
            immutable: false,
          });
          log(`  ${label}: local ${plugin.path}`);
          continue;
        }
        const pin = plugin.pinned?.[platform];
        if (pin) {
          const file = pin.file
            ? resolve(ROOT, pin.file)
            : await fetchArtifact(
                { name: `${plugin.key}.jar`, url: pin.url, sha256: pin.sha256 },
                join('plugins', platform),
                `${plugin.key}-${pin.version.replace(/[^A-Za-z0-9._-]/g, '_')}.jar`,
                false,
              );
          if (!existsSync(file)) {
            fail(`pinned jar ${pin.file} does not exist`);
          }
          plugins.push({
            key: plugin.key,
            platform,
            file,
            name: `${plugin.key}.jar`,
            version: pin.version,
            url: pin.url,
            sha256: pin.sha256,
            immutable: true,
          });
          log(`  ${label}: ${pin.version} (pinned)`);
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
        plugins.push({
          key: plugin.key,
          platform,
          file,
          name: `${plugin.key}.jar`,
          version: artifact.version,
          url: artifact.url,
          sha256: sha256File(file),
          immutable: plugin.source !== 'url',
        });
        log(`  ${label}: ${artifact.version}`);
      } catch (error) {
        failures.push(`${label}: ${pinFailureHint(plugin, platform, (error as Error).message)}`);
      }
    }
  }

  await mkdir(CACHE, { recursive: true });
  const resolved: ResolvedFiles = {
    minecraft: servers.minecraft,
    purpur: { build: servers.purpur.build, file: purpurFile, url: servers.purpur.url, sha256: sha256File(purpurFile) },
    velocity: {
      version: servers.velocity.version,
      build: servers.velocity.build,
      file: velocityFile,
      url: servers.velocity.url,
      sha256: sha256File(velocityFile),
    },
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
