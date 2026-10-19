import { readFile } from 'node:fs/promises';
import { join } from 'node:path';
import { parse } from 'smol-toml';
import type { DevConfig, Platform, PluginConfig } from './config.ts';
import { ROOT, fail, getJson } from './util.ts';

const FILL = 'https://fill.papermc.io/v3/projects';
const PURPUR = 'https://api.purpurmc.org/v2/purpur';
const MODRINTH = 'https://api.modrinth.com/v2';

export interface Artifact {
  name: string;
  url: string;
  md5?: string;
  sha256?: string;
  sha512?: string;
}

export interface ResolvedServers {
  minecraft: string;
  /** Purpur, a Paper fork; plugins still target the "paper" platform */
  purpur: Artifact & { build: string };
  velocity: Artifact & { version: string; build: string };
  /** Only resolved when limbo.enabled */
  limbo?: Artifact & { version: string };
}

export interface ResolvedPlugin extends Artifact {
  key: string;
  platform: Platform;
  /** Version label for display */
  version: string;
}

interface FillBuild {
  id: number;
  downloads: Record<string, { name: string; url: string; checksums: { sha256: string } }>;
}

async function catalog(): Promise<Record<string, string>> {
  const toml = parse(await readFile(join(ROOT, 'gradle', 'libs.versions.toml'), 'utf8')) as {
    versions: Record<string, string>;
  };
  return toml.versions;
}

function fillArtifact(build: FillBuild): Artifact {
  const download = build.downloads['server:default'];
  if (!download) {
    fail(`Fill build ${build.id} has no server:default download`);
  }
  return { name: download.name, url: download.url, sha256: download.checksums.sha256 };
}

interface PurpurBuilds {
  builds: { latest: string; all: string[] };
}

interface PurpurBuild {
  build: string;
  result: string;
  md5: string;
}

async function resolveLimbo(minecraft: string, wanted: string): Promise<Artifact & { version: string }> {
  // Limbo releases are built for exactly one Minecraft version
  const candidates = await modrinthVersions('limbo-server', ['java-agent'], [minecraft]);
  const pick = wanted === 'auto' ? candidates[0] : candidates.find((candidate) => candidate.version_number === wanted);
  if (!pick) {
    fail(
      `No Limbo build on Modrinth for Minecraft ${minecraft}${wanted === 'auto' ? '' : ` matching "${wanted}"`}. ` +
        'Set limbo.enabled = false, or pin limbo_version to a build you provide.',
    );
  }
  const file = pick.files.find((candidate) => candidate.primary) ?? pick.files[0];
  if (!file) {
    fail(`Limbo ${pick.version_number} has no files`);
  }
  return { name: file.filename, url: file.url, sha512: file.hashes.sha512, version: pick.version_number };
}

async function purpurBuildFor(minecraft: string, wanted: string): Promise<Artifact & { build: string }> {
  let versions: PurpurBuilds;
  try {
    versions = await getJson<PurpurBuilds>(`${PURPUR}/${minecraft}`);
  } catch (error) {
    const index = await getJson<{ versions: string[] }>(PURPUR);
    fail(`${(error as Error).message}\nPurpur has no ${minecraft} yet. Latest versions: ${index.versions.slice(-5).join(', ')}`);
  }
  const build = wanted === 'auto' || wanted === 'latest' ? versions.builds.latest : wanted;
  const info = await getJson<PurpurBuild>(`${PURPUR}/${minecraft}/${build}`);
  if (info.result !== 'SUCCESS') {
    fail(`Purpur ${minecraft} build ${build} did not succeed (${info.result})`);
  }
  return {
    name: `purpur-${minecraft}-${build}.jar`,
    url: `${PURPUR}/${minecraft}/${build}/download`,
    md5: info.md5,
    build,
  };
}

/** Resolves the Purpur and Velocity builds that match gradle/libs.versions.toml (or the config overrides). */
export async function resolveServers(config: DevConfig): Promise<ResolvedServers> {
  const versions = await catalog();

  const paperApi = versions['paper-api'] ?? fail('paper-api is missing from libs.versions.toml');
  const paperMatch = /^(?<mc>.+?)\.build\.\d+/.exec(paperApi);
  const minecraft = config.minecraft_version === 'auto' ? paperMatch?.groups?.mc : config.minecraft_version;
  if (!minecraft) {
    fail(`Could not derive the Minecraft version from paper-api "${paperApi}"`);
  }
  const purpurBuild = await purpurBuildFor(minecraft, config.purpur_build);

  const velocityApi = versions['velocity-api'] ?? fail('velocity-api is missing from libs.versions.toml');
  const velocityVersion =
    config.velocity_version === 'auto' ? `${velocityApi.split('-')[0]}-SNAPSHOT` : config.velocity_version;
  let velocity: FillBuild;
  try {
    velocity = await getJson<FillBuild>(
      `${FILL}/velocity/versions/${velocityVersion}/builds/${config.velocity_build === 'auto' ? 'latest' : config.velocity_build}`,
    );
  } catch (error) {
    const index = await getJson<{ versions: Record<string, string[]> }>(`${FILL}/velocity`);
    const available = Object.values(index.versions).flat().join(', ');
    fail(`${(error as Error).message}\nVelocity ${velocityVersion} not found. Available: ${available}`);
  }

  const limbo = config.limbo.enabled ? await resolveLimbo(minecraft, config.limbo_version) : undefined;

  return {
    minecraft,
    limbo,
    purpur: purpurBuild,
    velocity: { ...fillArtifact(velocity), version: velocityVersion, build: String(velocity.id) },
  };
}

interface ModrinthVersion {
  version_number: string;
  version_type: 'release' | 'beta' | 'alpha';
  files: { url: string; filename: string; primary: boolean; hashes: { sha512: string } }[];
}

async function modrinthVersions(slug: string, loaders: string[], gameVersions?: string[]): Promise<ModrinthVersion[]> {
  const params = new URLSearchParams({ loaders: JSON.stringify(loaders) });
  if (gameVersions) {
    params.set('game_versions', JSON.stringify(gameVersions));
  }
  return getJson<ModrinthVersion[]>(`${MODRINTH}/project/${encodeURIComponent(slug)}/version?${params}`);
}

async function resolveModrinth(plugin: PluginConfig, platform: Platform, minecraft: string): Promise<ResolvedPlugin> {
  const slug = plugin.slug ?? plugin.key;
  // Modrinth ORs values inside an array, so paper plugins that only tag spigot/bukkit are found too
  const loaders =
    platform === 'paper' ? ['purpur', 'paper', 'spigot', 'bukkit'] : platform === 'limbo' ? ['java-agent'] : ['velocity'];

  let candidates = await modrinthVersions(slug, loaders, [minecraft]);
  if (candidates.length === 0 && platform === 'paper') {
    for (const fallback of plugin.game_version_fallback ?? []) {
      candidates = await modrinthVersions(slug, loaders, [fallback]);
      if (candidates.length > 0) {
        break;
      }
    }
  }
  if (candidates.length === 0 && platform !== 'paper') {
    // Many Velocity and Limbo plugins do not tag Minecraft versions at all
    candidates = await modrinthVersions(slug, loaders);
  }
  if (candidates.length === 0) {
    fail(
      `No ${platform} build of "${slug}" on Modrinth for Minecraft ${minecraft}` +
        (platform === 'paper' ? ' (add game_version_fallback = ["..."] to allow an older tag)' : ''),
    );
  }

  const pick = plugin.version
    ? candidates.find((candidate) => candidate.version_number === plugin.version)
    : candidates.find((candidate) => plugin.allow_prerelease || candidate.version_type === 'release');
  if (!pick) {
    fail(`No matching ${plugin.version ? `version ${plugin.version}` : 'release'} of "${slug}" for ${platform}`);
  }
  const file = pick.files.find((candidate) => candidate.primary) ?? pick.files[0];
  if (!file) {
    fail(`Modrinth version ${pick.version_number} of "${slug}" has no files`);
  }
  return {
    key: plugin.key,
    platform,
    name: file.filename,
    url: file.url,
    sha512: file.hashes.sha512,
    version: pick.version_number,
  };
}

interface JenkinsBuild {
  number: number;
  artifacts: { fileName: string; relativePath: string }[];
}

async function resolveJenkins(plugin: PluginConfig, platform: Platform): Promise<ResolvedPlugin> {
  const job = plugin.job_url?.replace(/\/+$/, '') ?? fail(`Plugin "${plugin.key}" needs job_url`);
  const build = await getJson<JenkinsBuild>(
    `${job}/lastSuccessfulBuild/api/json?tree=number,artifacts%5BfileName,relativePath%5D`,
  );
  const pattern = plugin.artifact_pattern ? new RegExp(plugin.artifact_pattern) : /^(?!.*-(sources|javadoc)\.jar$).*\.jar$/;
  const artifact = build.artifacts.find((candidate) => pattern.test(candidate.fileName));
  if (!artifact) {
    const names = build.artifacts.map((candidate) => candidate.fileName).join(', ') || 'none';
    fail(`No artifact of ${job} matches ${pattern} (build ${build.number} has: ${names})`);
  }
  return {
    key: plugin.key,
    platform,
    name: artifact.fileName,
    // The build number, not lastSuccessfulBuild: the URL then keeps meaning the same jar
    url: `${job}/${build.number}/artifact/${artifact.relativePath}`,
    version: `build-${build.number}`,
  };
}

interface GithubRelease {
  tag_name: string;
  assets: { name: string; browser_download_url: string; digest?: string | null }[];
}

async function resolveGithub(plugin: PluginConfig, platform: Platform): Promise<ResolvedPlugin> {
  const repo = plugin.repo ?? fail(`Plugin "${plugin.key}" needs repo = "owner/name"`);
  // Anonymous API calls are limited to 60 per hour; a token lifts that
  const token = process.env.GITHUB_TOKEN;
  const release = await getJson<GithubRelease>(
    `https://api.github.com/repos/${repo}/releases/latest`,
    token ? { Authorization: `Bearer ${token}` } : {},
  );
  const pattern = plugin.asset_pattern ? new RegExp(plugin.asset_pattern) : /^(?!.*-(sources|javadoc)\.jar$).*\.jar$/;
  const asset = release.assets.find((candidate) => pattern.test(candidate.name));
  if (!asset) {
    const names = release.assets.map((candidate) => candidate.name).join(', ') || 'none';
    fail(`No asset of ${repo} ${release.tag_name} matches ${pattern} (it has: ${names})`);
  }
  return {
    key: plugin.key,
    platform,
    name: asset.name,
    url: asset.browser_download_url,
    sha256: asset.digest?.startsWith('sha256:') ? asset.digest.slice('sha256:'.length) : undefined,
    version: release.tag_name,
  };
}

/** Resolves one plugin for one platform. `local` plugins are handled by the caller. */
export async function resolvePlugin(
  plugin: PluginConfig,
  platform: Platform,
  minecraft: string,
): Promise<ResolvedPlugin> {
  if (plugin.source === 'modrinth') {
    return resolveModrinth(plugin, platform, minecraft);
  }
  if (plugin.source === 'jenkins') {
    return resolveJenkins(plugin, platform);
  }
  if (plugin.source === 'github') {
    return resolveGithub(plugin, platform);
  }
  const url = plugin.urls?.[platform] ?? plugin.url;
  if (!url) {
    fail(`Plugin "${plugin.key}" has no url for ${platform}`);
  }
  return { key: plugin.key, platform, name: `${plugin.key}.jar`, url, version: 'url' };
}
