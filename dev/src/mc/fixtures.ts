import { createHash } from 'node:crypto';
import { existsSync, readFileSync, readdirSync, statSync } from 'node:fs';
import { basename, join, resolve } from 'node:path';
import { parse as parseToml } from 'smol-toml';
import { loadConfig } from '../config.ts';
import { queryJson, runSql } from '../db.ts';
import { DEV, DevError, ROOT, fail, log, warn } from '../util.ts';
import { dataVersion } from './dataversion.ts';
import {
  ARMOR_ORDER,
  ARMOR_SLOTS,
  type ArmorPiece,
  ENDER_CHEST_SLOTS,
  INVENTORY_SLOTS,
  OFFHAND_SLOT,
  encodeItem,
  encodeItemArray,
} from './items.ts';
import type { Tag } from './nbt.ts';
import { parse as parseSnbt } from './snbt.ts';
import { offlineUuid, parseUuid, looksLikeUuid } from './uuid.ts';

export const FIXTURE_DIRS = [join(DEV, 'db', 'fixtures'), join(DEV, 'db', 'fixtures.local')];

interface InventoryEntry {
  player: string;
  context?: string;
  /** Relative (`-2h`, `-3d`) or ISO-8601 (UTC); default now */
  created?: string;
  held_slot?: number;
  slots?: Record<string, string>;
  armor?: Partial<Record<ArmorPiece, string>>;
  offhand?: string;
  ender_chest?: Record<string, string>;
  /** Exact base64 column contents (from `dev mc dump`); mutually exclusive with the SNBT fields */
  raw?: { inventory?: string; armor?: string; off_hand?: string; ender_chest?: string };
  /** Create this many snapshots, each `step` older than the previous (default 1h) */
  repeat?: number;
  step?: string;
}

export interface FixtureFile {
  path: string;
  name: string;
  requires_changelog?: string;
  items: Record<string, string>;
  inventory: InventoryEntry[];
}

export function fixtureFiles(): string[] {
  return FIXTURE_DIRS.filter((dir) => existsSync(dir)).flatMap((dir) =>
    readdirSync(dir)
      .filter((file) => file.endsWith('.toml'))
      .sort()
      .map((file) => join(dir, file)),
  );
}

export function readFixture(path: string): FixtureFile {
  const data = parseToml(readFileSync(path, 'utf8')) as Partial<FixtureFile>;
  return {
    path,
    name: basename(path),
    requires_changelog: data.requires_changelog,
    items: data.items ?? {},
    inventory: data.inventory ?? [],
  };
}

const UNITS: Record<string, string> = { s: 'seconds', m: 'minutes', h: 'hours', d: 'days' };

function parseDuration(text: string, where: string): { amount: number; unit: string } {
  const m = /^(\d+)([smhd])$/.exec(text.trim());
  if (!m) throw new DevError(`${where}: "${text}" is not a duration like 30m, 2h or 3d`);
  return { amount: Number(m[1]), unit: UNITS[m[2]!]! };
}

const NOW = "(now() at time zone 'utc')";

/** SQL expression for `created`, offset by `back` further durations (for `repeat`). */
function createdSql(entry: InventoryEntry, repeatIndex: number, where: string): string {
  const step = parseDuration(entry.step ?? '1h', where);
  const back = `interval '${step.amount * repeatIndex} ${step.unit}'`;
  const created = entry.created;
  if (!created) return `${NOW} - ${back}`;
  const relative = /^-(\d+[smhd])$/.exec(created);
  if (relative) {
    const { amount, unit } = parseDuration(relative[1]!, where);
    return `${NOW} - interval '${amount} ${unit}' - ${back}`;
  }
  if (Number.isNaN(Date.parse(created))) throw new DevError(`${where}: created "${created}" is neither -2h style nor ISO-8601`);
  const iso = new Date(created).toISOString().replace('Z', '');
  return `'${iso}'::timestamp - ${back}`;
}

function resolveItem(value: string, items: Record<string, string>, where: string): Tag {
  const source = value.startsWith('@') ? items[value.slice(1)] : value;
  if (source === undefined) throw new DevError(`${where}: unknown item reference ${value}`);
  try {
    return parseSnbt(source);
  } catch (error) {
    throw new DevError(`${where}: ${(error as Error).message}`);
  }
}

function slotIndex(key: string, max: number, where: string, hint = ''): number {
  const n = Number(key);
  if (!Number.isInteger(n) || n < 0 || n >= max) {
    throw new DevError(`${where}: slot "${key}" must be 0-${max - 1}${hint}`);
  }
  return n;
}

interface Row {
  id: string;
  inventory: string | null;
  armor: string | null;
  off_hand: string | null;
  ender_chest: string | null;
}

function b64(buf: Buffer | undefined): string | null {
  return buf ? buf.toString('base64') : null;
}

function buildRow(file: FixtureFile, entry: InventoryEntry, index: number, repeat: number, version: number): Row {
  const where = `${file.name} inventory #${index + 1}`;
  const id = createHash('md5').update(`fixture:${file.name}:${index}:${repeat}`).digest('hex');
  const row: Row = {
    id: `${id.slice(0, 8)}-${id.slice(8, 12)}-${id.slice(12, 16)}-${id.slice(16, 20)}-${id.slice(20)}`,
    inventory: null,
    armor: null,
    off_hand: null,
    ender_chest: null,
  };
  if (entry.raw) {
    if (entry.slots || entry.armor || entry.offhand || entry.ender_chest) {
      throw new DevError(`${where}: use either raw or slots/armor/offhand/ender_chest, not both`);
    }
    for (const [key, value] of Object.entries(entry.raw)) {
      if (!['inventory', 'armor', 'off_hand', 'ender_chest'].includes(key)) {
        throw new DevError(`${where}: unknown raw column "${key}"`);
      }
      row[key as keyof Omit<Row, 'id'>] = value;
    }
    return row;
  }
  const inventory: (Tag | null)[] = Array.from({ length: INVENTORY_SLOTS }, () => null);
  for (const [key, value] of Object.entries(entry.slots ?? {})) {
    inventory[slotIndex(key, 36, where, ' (armor and offhand have their own fields)')] = resolveItem(value, file.items, where);
  }
  const armor: (Tag | null)[] = Array.from({ length: ARMOR_SLOTS }, () => null);
  for (const [piece, value] of Object.entries(entry.armor ?? {})) {
    const at = ARMOR_ORDER.indexOf(piece as ArmorPiece);
    if (at === -1) throw new DevError(`${where}: armor piece "${piece}" must be one of ${ARMOR_ORDER.join(', ')}`);
    armor[at] = resolveItem(value, file.items, where);
    inventory[36 + at] = armor[at]!;
  }
  const offhand = entry.offhand ? resolveItem(entry.offhand, file.items, where) : null;
  inventory[OFFHAND_SLOT] = offhand;
  const ender: (Tag | null)[] = Array.from({ length: ENDER_CHEST_SLOTS }, () => null);
  for (const [key, value] of Object.entries(entry.ender_chest ?? {})) {
    ender[slotIndex(key, ENDER_CHEST_SLOTS, where)] = resolveItem(value, file.items, where);
  }
  row.inventory = b64(encodeItemArray(inventory, version));
  row.armor = b64(encodeItemArray(armor, version));
  row.off_hand = offhand ? b64(encodeItem(offhand, version)) : null;
  row.ender_chest = b64(encodeItemArray(ender, version));
  return row;
}

function bytea(value: string | null): string {
  return value === null ? 'NULL' : `decode('${value}', 'base64')`;
}

function literal(value: string): string {
  return `'${value.replaceAll("'", "''")}'`;
}

export interface LoadOptions {
  /** Changelog the database was reset through; fixtures requiring a later one are skipped */
  through?: string;
  /** Only these fixture files (paths or names) */
  only?: string[];
  /** Delete the rows these fixtures previously created before inserting */
  replace: boolean;
}

interface PlayerLookup {
  uuid: string;
  display_name: string | null;
}

/** Loads inventory fixtures into the dev database. Rows get deterministic ids, so loading twice is harmless. */
export async function loadFixtures(options: LoadOptions): Promise<number> {
  let files = fixtureFiles();
  if (options.only && options.only.length > 0) {
    const known = files;
    files = options.only.map(
      (name) =>
        known.find((f) => f === name || basename(f) === name || basename(f, '.toml') === name) ??
        // Any file on disk works too (for example the output of `dev mc dump`)
        [resolve(name), resolve(ROOT, name)].find((candidate) => existsSync(candidate) && statSync(candidate).isFile()) ??
        fail(`No fixture "${name}"`),
    );
  }
  const loadable = files.map(readFixture).filter((file) => {
    if (options.through && file.requires_changelog && file.requires_changelog > options.through) {
      log(`  fixtures: skipping ${file.name} (needs changelog ${file.requires_changelog})`);
      return false;
    }
    return file.inventory.length > 0;
  });
  if (loadable.length === 0) return 0;

  let version: number;
  try {
    version = await dataVersion(await loadConfig());
  } catch (error) {
    warn(`Skipping fixtures: ${(error as Error).message}`);
    return 0;
  }

  const statements: string[] = [];
  const ids: string[] = [];
  let count = 0;
  for (const file of loadable) {
    file.inventory.forEach((entry, index) => {
      const where = `${file.name} inventory #${index + 1}`;
      const player = resolveFixturePlayer(entry.player, where);
      if (player.create) {
        statements.push(
          `INSERT INTO players (uuid, display_name, online, first_joined, last_joined, last_seen, playtime) ` +
            `VALUES ('${player.uuid}', ${literal(player.name)}, false, ${NOW}, ${NOW}, ${NOW}, 0) ON CONFLICT (uuid) DO NOTHING;`,
        );
      }
      for (let r = 0; r < (entry.repeat ?? 1); r++) {
        const row = buildRow(file, entry, index, r, version);
        ids.push(row.id);
        statements.push(
          `INSERT INTO player_inventories (id, id_player, inventory, armor, ender_chest, off_hand, held_slot, context, created) ` +
            `VALUES ('${row.id}', '${player.uuid}', ${bytea(row.inventory)}, ${bytea(row.armor)}, ${bytea(row.ender_chest)}, ` +
            `${bytea(row.off_hand)}, ${entry.held_slot ?? 0}, ${literal(entry.context ?? 'paradisu')}, ${createdSql(entry, r, where)}) ` +
            `ON CONFLICT (id) DO UPDATE SET id_player = EXCLUDED.id_player, inventory = EXCLUDED.inventory, armor = EXCLUDED.armor, ` +
            `ender_chest = EXCLUDED.ender_chest, off_hand = EXCLUDED.off_hand, held_slot = EXCLUDED.held_slot, ` +
            `context = EXCLUDED.context, created = EXCLUDED.created;`,
        );
        count++;
      }
    });
  }
  const prefix = options.replace ? [`DELETE FROM player_inventories WHERE id IN (${ids.map((id) => `'${id}'`).join(', ')});`] : [];
  runSql(`BEGIN;\n${[...prefix, ...statements].join('\n')}\nCOMMIT;\n`);
  log(`  fixtures: loaded ${count} inventory snapshot(s) from ${loadable.map((f) => f.name).join(', ')}`);
  return count;
}

interface FixturePlayer {
  uuid: string;
  name: string;
  /** Insert a PLAYERS row first (offline:Name players that do not exist yet) */
  create: boolean;
}

function resolveFixturePlayer(player: string, where: string): FixturePlayer {
  if (player.toLowerCase().startsWith('offline:')) {
    const name = player.slice('offline:'.length);
    return { uuid: offlineUuid(name), name, create: true };
  }
  if (looksLikeUuid(player)) {
    const uuid = parseUuid(player);
    const row = queryJson<PlayerLookup>(`select uuid::text, display_name from players where uuid::text = :'u'`, { u: uuid })[0];
    if (!row) throw new DevError(`${where}: no player with uuid ${uuid} (use offline:Name to create one)`);
    return { uuid, name: row.display_name ?? uuid, create: false };
  }
  const rows = queryJson<PlayerLookup>(
    `select uuid::text, display_name from players where lower(display_name) = lower(:'n') order by uuid`,
    { n: player },
  );
  if (rows.length === 0) throw new DevError(`${where}: no player named "${player}" (use offline:${player} to create one)`);
  if (rows.length > 1) throw new DevError(`${where}: ${rows.length} players are named "${player}"; use a uuid`);
  return { uuid: rows[0]!.uuid, name: rows[0]!.display_name ?? player, create: false };
}
