import { parseArgs } from 'node:util';
import { existsSync, readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import type { Command } from '../command.ts';
import { queryJson } from '../db.ts';
import { askText, interactive, pickFrom, pickMany } from '../prompt.ts';
import { DEV, DevError, ROOT, RUN, fail, log, warn } from '../util.ts';
import { dataVersion } from './dataversion.ts';
import { FIXTURE_DIRS, fixtureFiles, loadFixtures, readFixture } from './fixtures.ts';
import {
  ARMOR_ORDER,
  ARMOR_SLOTS,
  type ItemSummary,
  decodeItem,
  decodeItemArray,
  encodeItem,
  encodeItemArray,
  looksLikeItemArray,
  slotLabel,
  summarize,
} from './items.ts';
import { MC_COLUMNS } from './columns.ts';
import { type Tag, decompress, get, readNbt, toPlainJson } from './nbt.ts';
import { listPlayers, resolvePlayer } from './players.ts';
import { listChunks, readChunk } from './region.ts';
import { parse as parseSnbt, stringify } from './snbt.ts';
import { looksLikeUuid, mojangByName, mojangByUuid, offlineUuid, parseUuid, toIntArray, toMostLeast, uuidVersion } from './uuid.ts';

// ---------------------------------------------------------------------------------------------------------------
// Shared output helpers
// ---------------------------------------------------------------------------------------------------------------

type Format = 'table' | 'snbt' | 'json' | 'fixture' | 'raw';

function formatArg(value: string | undefined, allowed: Format[], fallback: Format): Format {
  const format = (value ?? fallback) as Format;
  if (!allowed.includes(format)) fail(`--format must be one of ${allowed.join(', ')}`);
  return format;
}

function table(rows: string[][]): string {
  const widths = rows[0]!.map((_, c) => Math.max(...rows.map((r) => (r[c] ?? '').length)));
  return rows.map((r) => r.map((cell, c) => cell.padEnd(widths[c]!)).join('  ').trimEnd()).join('\n');
}

function describe(summary: ItemSummary): string[] {
  return [summary.id, String(summary.count), summary.name ?? '', summary.enchantments ?? ''];
}

interface InventoryRow {
  id: string;
  context: string | null;
  created: string;
  held_slot: number | null;
  inventory: string | null;
  armor: string | null;
  off_hand: string | null;
  ender_chest: string | null;
}

function inventoryRows(uuid: string, context: string | undefined): InventoryRow[] {
  return queryJson<InventoryRow>(
    `select id::text, context, to_char(created, 'YYYY-MM-DD"T"HH24:MI:SS') as created, held_slot,
            -- encode() wraps base64 at 76 columns; strip the newlines
            translate(encode(inventory, 'base64'), E'\n', '') as inventory,
            translate(encode(armor, 'base64'), E'\n', '') as armor,
            translate(encode(off_hand, 'base64'), E'\n', '') as off_hand,
            translate(encode(ender_chest, 'base64'), E'\n', '') as ender_chest
     from player_inventories
     where id_player::text = :'u' and (:'c' = '' or context = :'c')
     order by created desc`,
    { u: uuid, c: context ?? '' },
  );
}

function bytes(value: string | null): Buffer | undefined {
  return value ? Buffer.from(value, 'base64') : undefined;
}

function decodeArray(value: string | null): (Tag | null)[] {
  const buf = bytes(value);
  return buf ? decodeItemArray(buf) : [];
}

function snbtOf(tag: Tag): string {
  return stringify(tag, { pretty: true });
}

function printSlots(title: string, items: (Tag | null)[], label: (i: number) => string): void {
  const rows = items.flatMap((item, i) => (item ? [[label(i), ...describe(summarize(item))]] : []));
  log(`${title}`);
  log(rows.length === 0 ? '  (empty)' : table([['slot', 'item', 'count', 'name', 'enchantments'], ...rows]).replace(/^/gm, '  '));
}

// ---------------------------------------------------------------------------------------------------------------
// dev mc players / inventory / dump
// ---------------------------------------------------------------------------------------------------------------

function playersCommand(args: string[]): void {
  const { values, positionals } = parseArgs({ args, options: { online: { type: 'boolean' } }, allowPositionals: true });
  const rows = listPlayers(positionals[0] ?? '', values.online ?? false);
  if (rows.length === 0) {
    log('No players');
    return;
  }
  log(
    table([
      ['uuid', 'name', 'online', 'last server', 'playtime', 'inventories'],
      ...rows.map((p) => [
        p.uuid,
        p.display_name ?? '',
        p.online ? 'yes' : '',
        p.last_server ?? '',
        p.playtime == null ? '' : `${(p.playtime / 3600).toFixed(1)}h`,
        String(p.inventories),
      ]),
    ]),
  );
  if (rows.length === 200) log('(first 200 shown; narrow the search)');
}

function fixtureToml(player: string, row: InventoryRow, raw: boolean, index: number): string {
  const lines = ['[[inventory]]', `player = ${JSON.stringify(player)}`, `context = ${JSON.stringify(row.context ?? 'paradisu')}`];
  lines.push(`created = ${JSON.stringify(`${row.created}Z`)}`, `held_slot = ${row.held_slot ?? 0}`);
  if (raw) {
    const cols = (['inventory', 'armor', 'off_hand', 'ender_chest'] as const).filter((c) => row[c]);
    lines.push(`raw = { ${cols.map((c) => `${c} = "${row[c]}"`).join(', ')} }`);
    return lines.join('\n');
  }
  const slotMap = (items: (Tag | null)[], max: number) =>
    items
      .slice(0, max)
      .flatMap((item, i) => (item ? [`${i} = '${stripVersion(item)}'`] : []))
      .join(', ');
  const inventory = decodeArray(row.inventory);
  lines.push(`slots = { ${slotMap(inventory, 36)} }`);
  const armor = decodeArray(row.armor);
  const pieces = ARMOR_ORDER.flatMap((piece, i) => (armor[i] ? [`${piece} = '${stripVersion(armor[i]!)}'`] : []));
  if (pieces.length > 0) lines.push(`armor = { ${pieces.join(', ')} }`);
  const off = bytes(row.off_hand);
  if (off) lines.push(`offhand = '${stripVersion(decodeItem(off))}'`);
  const ender = slotMap(decodeArray(row.ender_chest), 27);
  if (ender) lines.push(`ender_chest = { ${ender} }`);
  void index;
  return lines.join('\n');
}

/** SNBT for a fixture value: drops DataVersion (re-added on load) and the single quotes TOML literals cannot hold. */
function stripVersion(item: Tag): string {
  if (item.type !== 'compound') return stringify(item);
  const { DataVersion: _ignored, ...rest } = item.value;
  const text = stringify({ type: 'compound', value: rest });
  if (text.includes("'")) throw new DevError('An item contains a single quote and cannot be written as a TOML literal; use --raw');
  return text;
}

async function inventoryCommand(args: string[]): Promise<void> {
  const { values, positionals } = parseArgs({
    args,
    options: {
      context: { type: 'string' },
      index: { type: 'string' },
      all: { type: 'boolean' },
      format: { type: 'string' },
      raw: { type: 'boolean' },
    },
    allowPositionals: true,
  });
  const player = await resolvePlayer(positionals[0]);
  const format = formatArg(values.raw ? 'fixture' : values.format, ['table', 'snbt', 'json', 'fixture', 'raw'], 'table');
  let rows = inventoryRows(player.uuid, values.context);
  if (rows.length === 0) throw new DevError(`${player.name} has no stored inventories${values.context ? ` in context ${values.context}` : ''}`);
  if (values.index !== undefined) {
    const row = rows[Number(values.index)];
    if (!row) throw new DevError(`--index ${values.index} is out of range (0-${rows.length - 1}, newest first)`);
    rows = [row];
  } else if (!values.all) {
    const seen = new Set<string>();
    rows = rows.filter((row) => !seen.has(row.context ?? '') && seen.add(row.context ?? ''));
  }
  for (const [n, row] of rows.entries()) {
    if (format === 'fixture') {
      log(fixtureToml(player.name, row, values.raw ?? false, n));
      continue;
    }
    if (format === 'raw') {
      log(JSON.stringify(row, null, 2));
      continue;
    }
    const inventory = decodeArray(row.inventory);
    const armor = decodeArray(row.armor);
    const off = bytes(row.off_hand);
    const ender = decodeArray(row.ender_chest);
    if (format === 'json') {
      log(
        JSON.stringify(
          {
            player: player.name,
            uuid: player.uuid,
            context: row.context,
            created: row.created,
            held_slot: row.held_slot,
            inventory: inventory.map((t) => (t ? toPlainJson(t) : null)),
            armor: armor.map((t) => (t ? toPlainJson(t) : null)),
            off_hand: off ? toPlainJson(decodeItem(off)) : null,
            ender_chest: ender.map((t) => (t ? toPlainJson(t) : null)),
          },
          null,
          2,
        ),
      );
    } else if (format === 'snbt') {
      const named = (items: (Tag | null)[]) =>
        items.flatMap((item, i) => (item ? [`  ${i}: ${snbtOf(item).replace(/\n/g, '\n  ')}`] : []));
      log(`# ${player.name} (${player.uuid}) context=${row.context} created=${row.created} held_slot=${row.held_slot}`);
      log(`inventory:\n${named(inventory).join('\n') || '  (empty)'}`);
      log(`armor:\n${named(armor).join('\n') || '  (empty)'}`);
      log(`off_hand: ${off ? snbtOf(decodeItem(off)) : '(empty)'}`);
      log(`ender_chest:\n${named(ender).join('\n') || '  (empty)'}`);
    } else {
      log(`${player.name} (${player.uuid})  context=${row.context}  created=${row.created}  held slot ${row.held_slot}`);
      printSlots('Inventory', inventory, slotLabel);
      if (inventory.length !== 41 && inventory.length > 0) warn(`inventory has ${inventory.length} slots (expected 41)`);
      if (armor.length > 0 && armor.length !== ARMOR_SLOTS) warn(`armor has ${armor.length} slots (expected ${ARMOR_SLOTS})`);
      log(`Off hand: ${off ? describe(summarize(decodeItem(off))).filter(Boolean).join(' ') : '(empty)'}`);
      printSlots('Ender chest', ender, (i) => `ender ${i}`);
    }
    if (n < rows.length - 1) log('');
  }
}

async function dumpCommand(args: string[]): Promise<void> {
  const { values } = parseArgs({
    args,
    options: { player: { type: 'string' }, context: { type: 'string' }, out: { type: 'string' }, raw: { type: 'boolean' } },
  });
  const entries: string[] = [];
  const players = values.player
    ? [await resolvePlayer(values.player)]
    : listPlayers('').filter((p) => p.inventories > 0).map((p) => ({ uuid: p.uuid, name: p.display_name ?? p.uuid, missing: false }));
  for (const player of players) {
    const rows = inventoryRows(player.uuid, values.context).reverse();
    rows.forEach((row, i) => entries.push(fixtureToml(player.uuid, row, values.raw ?? false, i)));
  }
  if (entries.length === 0) throw new DevError('No inventories matched');
  const header = `# Captured by \`dev mc dump\` on ${new Date().toISOString()}. Load with \`dev db fixtures load <file>\`.\n\n`;
  const out = resolve(ROOT, values.out ?? join(FIXTURE_DIRS[1]!, `dump-${new Date().toISOString().replace(/[:.]/g, '-')}.toml`));
  mkdirSync(dirname(out), { recursive: true });
  writeFileSync(out, header + entries.join('\n\n') + '\n');
  log(`Wrote ${entries.length} inventory snapshot(s) to ${out}`);
}

// ---------------------------------------------------------------------------------------------------------------
// dev mc nbt / item / encode / uuid
// ---------------------------------------------------------------------------------------------------------------

async function readStdin(): Promise<Buffer> {
  const chunks: Buffer[] = [];
  for await (const chunk of process.stdin) chunks.push(chunk as Buffer);
  return Buffer.concat(chunks);
}

async function readInput(source: string): Promise<Buffer> {
  if (source === '-') return readStdin();
  const file = source.startsWith('@') ? source.slice(1) : source;
  if (!existsSync(file)) throw new DevError(`No such file: ${file}`);
  return readFileSync(file);
}

/** Maps `playerdata:<player>[@server]` and `level:<server>` to files in the dev run directory. */
async function nbtSource(source: string, serverNames: string[]): Promise<string> {
  const player = /^playerdata:(.+?)(?:@(.+))?$/.exec(source);
  if (player) {
    const resolved = await resolvePlayer(player[1]);
    const server = player[2] ?? (await chooseServer(serverNames));
    // Newer Minecraft versions keep player files in world/players/data, older ones in world/playerdata
    const candidates = [join(RUN, server, 'world', 'players', 'data'), join(RUN, server, 'world', 'playerdata')].map((dir) =>
      join(dir, `${resolved.uuid}.dat`),
    );
    return candidates.find((candidate) => existsSync(candidate)) ?? candidates[0]!;
  }
  const level = /^level:(.+)$/.exec(source);
  if (level) return join(RUN, level[1]!, 'world', 'level.dat');
  return source;
}

async function chooseServer(names: string[]): Promise<string> {
  if (names.length === 1) return names[0]!;
  return pickFrom('Which server?', names.map((n) => ({ name: n, value: n })), 'A server (use playerdata:<player>@<server>)');
}

async function nbtCommand(serverNames: string[], args: string[]): Promise<void> {
  const { values, positionals } = parseArgs({
    args,
    options: { path: { type: 'string' }, format: { type: 'string' }, chunk: { type: 'string' }, compact: { type: 'boolean' } },
    allowPositionals: true,
  });
  const source = positionals[0] ?? (await askText('NBT file (or playerdata:<player>@<server>, level:<server>, - for stdin)', {}, 'An NBT file'));
  const file = await nbtSource(source, serverNames);
  let data: Buffer;
  if (file.endsWith('.mca')) {
    let chunk = values.chunk;
    if (!chunk) {
      const chunks = listChunks(file);
      chunk = await pickFrom('Which chunk?', chunks.map((c) => ({ name: `${c.x},${c.z}`, value: `${c.x},${c.z}` })), '--chunk x,z');
    }
    const [x, z] = chunk.split(',').map(Number);
    if (x === undefined || z === undefined || Number.isNaN(x) || Number.isNaN(z)) fail('--chunk must look like 3,14');
    data = readChunk(file, x, z);
  } else {
    data = decompress(await readInput(file));
  }
  const root = readNbt(data);
  const tag = values.path ? get(root.tag, values.path) : root.tag;
  if (!tag) throw new DevError(`Nothing at path ${values.path}`);
  const format = values.format ?? 'snbt';
  if (format === 'json') log(JSON.stringify(toPlainJson(tag), null, 2));
  else if (format === 'snbt') log(stringify(tag, { pretty: !values.compact }));
  else fail('--format must be snbt or json');
}

function looksLikeHex(text: string): boolean {
  return text.length % 2 === 0 && /^[0-9a-fA-F]+$/.test(text);
}

async function itemCommand(args: string[]): Promise<void> {
  const { values, positionals } = parseArgs({
    args,
    options: { format: { type: 'string' }, compact: { type: 'boolean' } },
    allowPositionals: true,
  });
  const source = positionals[0] ?? fail('Usage: dev mc item <base64|hex|@file|-> [--format snbt|json|table]');
  const raw = source === '-' || source.startsWith('@') ? await readInput(source) : Buffer.from(source);
  const trimmed = raw.toString('utf8').trim();
  // Text input is base64 or hex; a file may also hold the raw bytes
  const buf = raw[0] === 0x1f || raw[0] === 0x01
    ? raw
    : looksLikeHex(trimmed)
      ? Buffer.from(trimmed, 'hex')
      : Buffer.from(trimmed, 'base64');
  const format = values.format ?? 'snbt';
  const show = (tag: Tag | null) => (tag === null ? 'empty' : format === 'json' ? JSON.stringify(toPlainJson(tag)) : stringify(tag, { pretty: !values.compact }));
  if (looksLikeItemArray(buf)) {
    const items = decodeItemArray(buf);
    if (format === 'table') {
      printSlots(`Item array (${items.length} slots)`, items, (i) => String(i));
      return;
    }
    items.forEach((item, i) => log(`${i}: ${show(item)}`));
  } else {
    const item = decodeItem(buf);
    log(format === 'table' ? describe(summarize(item)).join('  ') : show(item));
  }
}

async function encodeCommand(configVersion: () => Promise<number>, args: string[]): Promise<void> {
  const { values, positionals } = parseArgs({
    args,
    options: { array: { type: 'boolean' }, hex: { type: 'boolean' } },
    allowPositionals: true,
  });
  const text = positionals[0] ?? (await askText('SNBT item', {}, 'An SNBT item like {id:"minecraft:stone",count:1}'));
  const tag = parseSnbt(text);
  const version = await configVersion();
  let out: Buffer;
  if (values.array) {
    if (tag.type !== 'list') fail('--array expects a list of items, using {} for empty slots');
    out = encodeItemArray(tag.value.map((t) => (t.type === 'compound' && Object.keys(t.value).length === 0 ? null : t)), version);
  } else {
    out = encodeItem(tag, version);
  }
  log(values.hex ? out.toString('hex') : out.toString('base64'));
}

async function uuidCommand(args: string[]): Promise<void> {
  const { values, positionals } = parseArgs({ args, options: { offline: { type: 'boolean' } }, allowPositionals: true });
  const input = positionals.join(' ') || (await askText('Name or UUID', {}, 'A player name or UUID'));
  const rows: [string, string][] = [];
  const addUuid = (uuid: string) => {
    const { most, least } = toMostLeast(uuid);
    rows.push(['uuid', uuid], ['undashed', uuid.replaceAll('-', '')], ['int array', `[I;${toIntArray(uuid).join(',')}]`]);
    rows.push(['most/least', `${most}L / ${least}L`], ['version', `${uuidVersion(uuid)} (${uuidVersion(uuid) === 3 ? 'offline mode' : uuidVersion(uuid) === 4 ? 'online/random' : 'other'})`]);
  };
  let uuid: string | undefined;
  if (looksLikeUuid(input)) {
    uuid = parseUuid(input);
    addUuid(uuid);
  } else {
    rows.push(['name', input], ['offline uuid', offlineUuid(input)]);
  }
  const lookup = uuid ?? offlineUuid(input);
  try {
    const dbRows = queryJson<{ uuid: string; display_name: string | null }>(
      `select uuid::text, display_name from players where uuid::text = :'u' or lower(display_name) = lower(:'n')`,
      { u: lookup, n: uuid ? '' : input },
    );
    for (const p of dbRows) rows.push(['dev database', `${p.display_name ?? '(unnamed)'}  ${p.uuid}`]);
    if (dbRows.length === 0) rows.push(['dev database', '(not found)']);
  } catch {
    rows.push(['dev database', '(unavailable)']);
  }
  if (!values.offline) {
    try {
      const profile = uuid ? await mojangByUuid(uuid) : await mojangByName(input);
      rows.push(['mojang', profile ? `${profile.name}  ${profile.id}` : '(no such account)']);
    } catch (error) {
      warn(`Mojang lookup failed: ${(error as Error).message}`);
    }
  }
  const width = Math.max(...rows.map(([k]) => k.length));
  for (const [key, value] of rows) log(`${key.padEnd(width)}  ${value}`);
}

// ---------------------------------------------------------------------------------------------------------------
// db fixtures
// ---------------------------------------------------------------------------------------------------------------

async function fixturesCommand(args: string[]): Promise<void> {
  const { values, positionals } = parseArgs({ args, options: { replace: { type: 'boolean' } }, allowPositionals: true });
  const action = positionals[0] ?? (await pickFrom('Fixtures', [
    { name: 'load', value: 'load' },
    { name: 'list', value: 'list' },
  ], 'An action: load | list'));
  if (action === 'list') {
    const files = fixtureFiles();
    if (files.length === 0) log('No fixtures');
    for (const file of files) {
      const fixture = readFixture(file);
      log(`${fixture.path.replace(`${DEV}/`, 'dev/')}  inventories=${fixture.inventory.length}${fixture.requires_changelog ? `  requires changelog ${fixture.requires_changelog}` : ''}`);
    }
    return;
  }
  if (action !== 'load') fail(`Unknown fixtures action "${action}" (load | list)`);
  let only = positionals.slice(1);
  if (only.length === 0 && interactive()) {
    only = await pickMany('Load which fixtures?', fixtureFiles().map((f) => ({ name: f.replace(`${DEV}/`, 'dev/'), value: f, checked: true })));
  }
  const loaded = await loadFixtures({ only, replace: values.replace ?? false });
  if (loaded === 0) log('Nothing loaded');
}

// ---------------------------------------------------------------------------------------------------------------

export function mcCommands(getVersion: () => Promise<number>, serverNames: () => string[]): Command[] {
  return [
    { name: 'mc players', usage: '[search] [--online]', summary: 'List players in the dev database', run: (_c, a) => playersCommand(a) },
    {
      name: 'mc inventory',
      usage: '<player> [--context c] [--index n|--all] [--format table|snbt|json|fixture|raw]',
      summary: 'Show a player\'s stored inventory, decoded (fixture: as a loadable fixture entry)',
      run: (_c, a) => inventoryCommand(a),
    },
    {
      name: 'mc dump',
      usage: '[--player p] [--context c] [--out file] [--raw]',
      summary: 'Write stored inventories as a fixture file you can reload',
      run: (_c, a) => dumpCommand(a),
    },
    {
      name: 'mc nbt',
      usage: '<file|-|playerdata:<player>@<server>|level:<server>> [--path a.b[0]] [--format snbt|json] [--chunk x,z]',
      summary: 'Decode an NBT file, region chunk, player data file or level.dat',
      run: (_c, a) => nbtCommand(serverNames(), a),
    },
    { name: 'mc item', usage: '<base64|hex|@file|-> [--format snbt|json|table]', summary: 'Decode a Paper item or item-array blob', run: (_c, a) => itemCommand(a) },
    { name: 'mc encode', usage: '<snbt> [--array] [--hex]', summary: 'Encode SNBT into the Paper blob format (base64)', run: (_c, a) => encodeCommand(getVersion, a) },
    { name: 'mc uuid', usage: '<name|uuid|[I;…]> [--offline]', summary: 'Convert UUID forms, compute the offline UUID, look the player up', run: (_c, a) => uuidCommand(a) },
    {
      name: 'mc columns',
      usage: '',
      summary: 'List the database columns that hold Minecraft binary data',
      run: () => log(table([['table', 'column', 'codec'], ...MC_COLUMNS.map((c) => [c.table, c.column, c.codec])])),
    },
  ];
}

export function fixtureCommand(): Command {
  return {
    name: 'db fixtures',
    usage: '[load|list] [files...] [--replace]',
    summary: 'Load Minecraft data fixtures (inventories) into the dev database',
    run: (_c, a) => fixturesCommand(a),
  };
}

export async function configDataVersion(config: Parameters<typeof dataVersion>[0]): Promise<number> {
  return dataVersion(config);
}
