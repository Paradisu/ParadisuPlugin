import { parseArgs } from 'node:util';
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import type { Command, DevConfig } from '@mcdevtools/cli/extension';
import { queryJson } from '@mcdevtools/cli/db';
import { bold, dim, keyValue, relPath, table } from '@mcdevtools/cli/output';
import { askText, interactive, pickFrom, pickMany } from '@mcdevtools/cli/prompt';
import { CONFIG_DIR, DevError, ROOT, RUN, fail, log, warn } from '@mcdevtools/cli/util';
import {
  ARMOR_ORDER,
  ARMOR_SLOTS,
  type Format,
  type Tag,
  bytes,
  dataVersion,
  decodeArray,
  decodeItem,
  decodeItemArray,
  describe,
  encodeItem,
  encodeItemArray,
  formatArg,
  get,
  printSlots,
  slotLabel,
  snbtOf,
  stringify,
  summarize,
  toPlainJson,
} from '@mcdevtools/cli/mc';
import { MC_COLUMNS } from './columns.ts';
import { FIXTURE_DIRS, fixtureFiles, loadFixtures, readFixture } from './fixtures.ts';
import { listPlayers, resolvePlayer } from './players.ts';

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

// ---------------------------------------------------------------------------------------------------------------
// dev mc players / inventory / dump
// ---------------------------------------------------------------------------------------------------------------

function playersCommand(args: string[]): void {
  const { values, positionals } = parseArgs({ args, options: { online: { type: 'boolean' } }, allowPositionals: true });
  const rows = listPlayers(positionals[0] ?? '', values.online ?? false);
  if (rows.length === 0) {
    log('No players.');
    return;
  }
  log(
    table(
      ['uuid', 'name', 'online', 'last server', { header: 'playtime', align: 'right' }, { header: 'inventories', align: 'right' }],
      rows.map((p) => [
        p.uuid,
        p.display_name ?? '',
        p.online ? 'yes' : '',
        p.last_server ?? '',
        p.playtime == null ? '' : `${(p.playtime / 3600).toFixed(1)}h`,
        String(p.inventories),
      ]),
    ),
  );
  if (rows.length === 200) log(dim('First 200 shown; narrow the search.'));
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
      log(
        keyValue([
          ['player', `${player.name} (${player.uuid})`],
          ['context', String(row.context)],
          ['created', row.created],
          ['held slot', String(row.held_slot)],
        ]),
      );
      printSlots('Inventory', inventory, slotLabel);
      if (inventory.length !== 41 && inventory.length > 0) warn(`inventory has ${inventory.length} slots (expected 41)`);
      if (armor.length > 0 && armor.length !== ARMOR_SLOTS) warn(`armor has ${armor.length} slots (expected ${ARMOR_SLOTS})`);
      log(`${bold('Off hand')}  ${off ? describe(summarize(decodeItem(off))).filter(Boolean).join(' ') : dim('(empty)')}`);
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
// db fixtures
// ---------------------------------------------------------------------------------------------------------------

async function fixturesCommand(config: DevConfig, args: string[]): Promise<void> {
  const { values, positionals } = parseArgs({ args, options: { replace: { type: 'boolean' } }, allowPositionals: true });
  const action = positionals[0] ?? (await pickFrom('Fixtures', [
    { name: 'load', value: 'load' },
    { name: 'list', value: 'list' },
  ], 'An action: load | list'));
  if (action === 'list') {
    const files = fixtureFiles();
    if (files.length === 0) {
      log('No fixtures.');
      return;
    }
    log(
      table(
        ['file', { header: 'inventories', align: 'right' }, 'requires changelog'],
        files.map((file) => {
          const fixture = readFixture(file);
          return [relPath(fixture.path), String(fixture.inventory.length), fixture.requires_changelog ?? ''];
        }),
      ),
    );
    return;
  }
  if (action !== 'load') fail(`Unknown fixtures action "${action}" (load | list)`);
  let only = positionals.slice(1);
  if (only.length === 0 && interactive()) {
    only = await pickMany('Load which fixtures?', fixtureFiles().map((f) => ({ name: f.replace(`${CONFIG_DIR}/`, 'dev/'), value: f, checked: true })));
  }
  const loaded = await loadFixtures({ config, only, replace: values.replace ?? false });
  if (loaded === 0) log('Nothing loaded.');
}


/** `mc players`, `mc inventory`, `mc dump`, `mc columns` and `db fixtures`: they read the Paradisu schema. */
export function paradisuMcCommands(): Command[] {
  return [
    { name: 'mc players', usage: '[search]', summary: 'List players in the dev database', options: [{ flag: '--online', summary: 'Only online players' }], run: (_c, a) => playersCommand(a) },
    {
      name: 'mc inventory',
      usage: '<player>',
      summary: 'Show a player\'s stored inventory',
      options: [
        { flag: '--context <name>', summary: 'Only this context' },
        { flag: '--index <n>', summary: 'One snapshot (newest is 0)' },
        { flag: '--all', summary: 'Every snapshot' },
        { flag: '--format <name>', summary: 'table, snbt, json, fixture or raw' },
      ],
      run: (_c, a) => inventoryCommand(a),
    },
    {
      name: 'mc dump',
      usage: '',
      summary: 'Write stored inventories as a fixture file',
      options: [
        { flag: '--player <name>', summary: 'Only this player' },
        { flag: '--context <name>', summary: 'Only this context' },
        { flag: '--out <file>', summary: 'Output file' },
        { flag: '--raw', summary: 'Keep the stored bytes undecoded' },
      ],
      run: (_c, a) => dumpCommand(a),
    },
    {
      name: 'mc columns',
      usage: '',
      summary: 'List database columns holding Minecraft data',
      run: () => log(table(['table', 'column', 'codec'], MC_COLUMNS.map((c) => [c.table, c.column, c.codec]))),
    },
    {
      name: 'db fixtures',
      usage: '[load|list] [files...]',
      summary: 'Load or list Minecraft data fixtures',
      options: [
        { flag: '--replace', summary: 'Replace existing inventory snapshots' },
      ],
      run: (ctx, a) => fixturesCommand(ctx.config, a),
      },
  ];
}
