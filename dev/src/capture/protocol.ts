import { createHash } from 'node:crypto';
import { existsSync } from 'node:fs';
import { mkdir, readFile, readdir, rm, writeFile } from 'node:fs/promises';
import { join } from 'node:path';
import type { DevConfig } from '../config.ts';
import { readResolved } from '../downloads.ts';
import { CACHE, capture, download, fail, getJson, log } from '../util.ts';

export const STATES = ['handshake', 'status', 'login', 'play', 'configuration'] as const;
export const DIRECTIONS = ['serverbound', 'clientbound'] as const;
export type Dir = 0 | 1;

/** Packet names for one Minecraft version, keyed by our numeric state (ProtocolCraft order) and direction. */
export interface ProtocolFile {
  minecraftVersion: string;
  protocolVersion: number;
  /** state -> dir -> id -> name */
  packets: Record<string, Record<string, Record<string, string>>>;
  /** Id of `minecraft:player` in the entity type registry; absent in protocol files made before exporters needed it */
  playerEntityType?: number;
}

export interface PacketKey {
  state: number;
  dir: number;
  id: number;
}

export class ProtocolTable {
  readonly minecraftVersion: string;
  readonly protocolVersion: number;
  readonly playerEntityType: number | undefined;
  private readonly names = new Map<string, string>();
  private readonly ids = new Map<string, number>();
  private readonly entries: { key: PacketKey; name: string }[] = [];

  constructor(file: ProtocolFile) {
    this.minecraftVersion = file.minecraftVersion;
    this.protocolVersion = file.protocolVersion;
    this.playerEntityType = file.playerEntityType;
    for (const [state, dirs] of Object.entries(file.packets)) {
      for (const [dir, ids] of Object.entries(dirs)) {
        for (const [id, name] of Object.entries(ids)) {
          const key = { state: Number(state), dir: Number(dir), id: Number(id) };
          this.names.set(`${key.state}/${key.dir}/${key.id}`, name);
          this.ids.set(`${key.state}/${key.dir}/${name.replace(/^minecraft:/, '')}`, key.id);
          this.entries.push({ key, name });
        }
      }
    }
  }

  /** `minecraft:name`, or `unknown_0x1A` when the id is not in the table. */
  name(state: number, dir: number, id: number): string {
    return this.names.get(`${state}/${dir}/${id}`) ?? `unknown_0x${id.toString(16).toUpperCase()}`;
  }

  /** The id of a packet by name (with or without `minecraft:`), or undefined when this version has no such packet. */
  id(state: number, dir: number, name: string): number | undefined {
    return this.ids.get(`${state}/${dir}/${name.replace(/^minecraft:/, '')}`);
  }

  /** One entry per `state/name` with the directions it exists in (0 serverbound, 1 clientbound), sorted. */
  listing(): { pattern: string; dirs: number[] }[] {
    const byPattern = new Map<string, Set<number>>();
    for (const { key, name } of this.entries) {
      const pattern = `${STATES[key.state]}/${name.replace(/^minecraft:/, '')}`;
      const dirs = byPattern.get(pattern) ?? new Set<number>();
      dirs.add(key.dir);
      byPattern.set(pattern, dirs);
    }
    return [...byPattern].map(([pattern, dirs]) => ({ pattern, dirs: [...dirs].sort() })).sort((a, b) => a.pattern.localeCompare(b.pattern));
  }

  /** Counts of packets per state and direction, for `dev capture protocol`. */
  counts(): { state: string; serverbound: number; clientbound: number }[] {
    return STATES.map((state, index) => ({
      state,
      serverbound: this.entries.filter((e) => e.key.state === index && e.key.dir === 0).length,
      clientbound: this.entries.filter((e) => e.key.state === index && e.key.dir === 1).length,
    }));
  }

  /**
   * Packet types matching a pattern: case-insensitive, optional `minecraft:`, `*` wildcard, optional `state/`
   * prefix (`play/move_player_pos`).
   */
  keys(pattern: string): PacketKey[] {
    let text = pattern.trim().toLowerCase();
    let state: number | undefined;
    const slash = text.indexOf('/');
    if (slash !== -1) {
      const index = STATES.indexOf(text.slice(0, slash) as (typeof STATES)[number]);
      if (index === -1) {
        return [];
      }
      state = index;
      text = text.slice(slash + 1);
    }
    text = text.replace(/^minecraft:/, '');
    const regex = new RegExp(`^${text.replace(/[.+?^${}()|[\]\\]/g, '\\$&').replaceAll('*', '.*')}$`);
    return this.entries
      .filter((e) => (state === undefined || e.key.state === state) && regex.test(e.name.replace(/^minecraft:/, '')))
      .map((e) => e.key);
  }
}

export function protocolDir(minecraft: string): string {
  return join(CACHE, 'protocol', minecraft);
}

export function hintsFile(minecraft: string): string {
  return join(protocolDir(minecraft), 'capture-protocol.properties');
}

export async function loadProtocolFile(path: string): Promise<ProtocolTable> {
  return new ProtocolTable(JSON.parse(await readFile(path, 'utf8')) as ProtocolFile);
}

interface VersionManifest {
  versions: { id: string; url: string }[];
}
interface VersionJson {
  downloads: { server?: { url: string; sha1: string } };
}
type RegistryReport = Record<string, { entries: Record<string, { protocol_id: number }> }>;
type PacketReport = Record<string, Partial<Record<(typeof DIRECTIONS)[number], Record<string, { protocol_id: number }>>>>;

/** The id of a packet by Mojang name; fails loudly so a renamed packet is noticed. */
function idOf(report: PacketReport, state: string, dir: (typeof DIRECTIONS)[number], name: string): number {
  const id = report[state]?.[dir]?.[`minecraft:${name}`]?.protocol_id;
  return id ?? fail(`Mojang's packet report has no ${state}/${dir}/${name}; update the transition names in capture/protocol.ts`);
}

/** Builds protocol.json and capture-protocol.properties from Mojang's packet report. Exported for tests. */
export function buildProtocol(
  report: PacketReport,
  minecraftVersion: string,
  protocolVersion: number,
  registries?: RegistryReport,
): { file: ProtocolFile; properties: string } {
  const packets: ProtocolFile['packets'] = {};
  STATES.forEach((state, stateIndex) => {
    packets[stateIndex] = { 0: {}, 1: {} };
    DIRECTIONS.forEach((dir, dirIndex) => {
      for (const [name, info] of Object.entries(report[state]?.[dir] ?? {})) {
        packets[stateIndex]![dirIndex]![info.protocol_id] = name;
      }
    });
  });
  const properties = [
    `protocol=${protocolVersion}`,
    `login.serverbound.login_acknowledged=${idOf(report, 'login', 'serverbound', 'login_acknowledged')}`,
    `configuration.serverbound.finish_configuration=${idOf(report, 'configuration', 'serverbound', 'finish_configuration')}`,
    `play.serverbound.configuration_acknowledged=${idOf(report, 'play', 'serverbound', 'configuration_acknowledged')}`,
    '',
  ].join('\n');
  const playerEntityType = registries?.['minecraft:entity_type']?.entries['minecraft:player']?.protocol_id;
  return { file: { minecraftVersion, protocolVersion, packets, playerEntityType }, properties };
}

/**
 * Packet names and the plugin's state-transition ids come from Mojang's own data generator, so a Minecraft update
 * only needs this regenerated (`dev capture protocol --refresh`), never a Java change.
 */
export async function ensureProtocol(config: DevConfig, refresh = false): Promise<ProtocolTable> {
  void config;
  const resolved = await readResolved();
  const minecraft = resolved.minecraft;
  const dir = protocolDir(minecraft);
  const jsonPath = join(dir, 'protocol.json');
  if (!refresh && existsSync(jsonPath) && existsSync(hintsFile(minecraft))) {
    const cached = await loadProtocolFile(jsonPath);
    if (cached.playerEntityType !== undefined) {
      return cached;
    }
    // Made before the exporters needed the player entity type; regenerate once.
  }
  log(`Generating the packet table for Minecraft ${minecraft}`);
  await mkdir(dir, { recursive: true });
  const manifest = await getJson<VersionManifest>('https://piston-meta.mojang.com/mc/game/version_manifest_v2.json');
  const entry = manifest.versions.find((version) => version.id === minecraft) ?? fail(`Mojang has no Minecraft ${minecraft}`);
  const server = (await getJson<VersionJson>(entry.url)).downloads.server ?? fail(`Minecraft ${minecraft} has no server download`);
  const jar = join(dir, 'server.jar');
  await download(server.url, jar);
  const sha1 = createHash('sha1')
    .update(await readFile(jar))
    .digest('hex');
  if (sha1 !== server.sha1) {
    await rm(jar, { force: true });
    fail(`sha1 mismatch for the Minecraft ${minecraft} server jar`);
  }
  const generated = capture('java', ['-DbundlerMainClass=net.minecraft.data.Main', '-jar', 'server.jar', '--reports', '--output', 'generated'], {
    cwd: dir,
  });
  try {
    if (generated.status !== 0) {
      fail(`The data generator failed:\n${generated.stderr || generated.stdout}`);
    }
    const report = JSON.parse(await readFile(join(dir, 'generated', 'reports', 'packets.json'), 'utf8')) as PacketReport;
    const versionJson = capture('unzip', ['-p', jar, 'version.json']);
    const protocolVersion = (JSON.parse(versionJson.stdout) as { protocol_version?: number }).protocol_version;
    if (typeof protocolVersion !== 'number') {
      fail('Could not read protocol_version from the server jar');
    }
    const registries = JSON.parse(await readFile(join(dir, 'generated', 'reports', 'registries.json'), 'utf8')) as RegistryReport;
    const { file, properties } = buildProtocol(report, minecraft, protocolVersion, registries);
    await writeFile(jsonPath, JSON.stringify(file));
    await writeFile(hintsFile(minecraft), properties);
  } finally {
    for (const leftover of await readdir(dir)) {
      if (leftover !== 'protocol.json' && leftover !== 'capture-protocol.properties') {
        await rm(join(dir, leftover), { recursive: true, force: true });
      }
    }
  }
  return loadProtocolFile(jsonPath);
}
