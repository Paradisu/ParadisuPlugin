import assert from 'node:assert/strict';
import { mkdtempSync, readFileSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { test } from 'node:test';
import { gzipSync, inflateSync } from 'node:zlib';
import { varInt, varLong } from '../src/capture/exporters/encode.ts';
import { sniffcraft } from '../src/capture/exporters/sniffcraft.ts';
import { compileFilter, parseFilter } from '../src/capture/filters.ts';
import { type DumpRecord, type PacketRecord, encodeDump, readDump } from '../src/capture/format.ts';
import { buildProtocol, ProtocolTable } from '../src/capture/protocol.ts';
import { linkOf, receiverOf } from '../src/capture/receivers.ts';
import type { CaptureMeta, ConnMeta } from '../src/capture/store.ts';
import type { DevConfig } from '../src/config.ts';
import type { Instance } from '../src/templates.ts';
import { parseCount, parseSize, parseSpan, split } from '../src/capture/units.ts';
import { filterFlags, hasFilterFlags, hasRecordingFlags, recordingFlags } from '../src/capture/wizard.ts';

const REPORT = {
  handshake: { serverbound: { 'minecraft:intention': { protocol_id: 0 } } },
  status: { serverbound: { 'minecraft:status_request': { protocol_id: 0 } } },
  login: {
    serverbound: { 'minecraft:hello': { protocol_id: 0 }, 'minecraft:login_acknowledged': { protocol_id: 3 } },
    clientbound: { 'minecraft:login_finished': { protocol_id: 2 } },
  },
  configuration: { serverbound: { 'minecraft:finish_configuration': { protocol_id: 3 } } },
  play: {
    serverbound: { 'minecraft:configuration_acknowledged': { protocol_id: 15 }, 'minecraft:move_player_pos': { protocol_id: 30 } },
    clientbound: { 'minecraft:move_entity_pos': { protocol_id: 52 }, 'minecraft:move_entity_rot': { protocol_id: 53 } },
  },
};

async function roundTrip(records: Exclude<DumpRecord, { type: 'header' }>[], gzip = (b: Buffer) => gzipSync(b)): Promise<DumpRecord[]> {
  const path = join(mkdtempSync(join(tmpdir(), 'capture-test-')), 'x.bin.gz');
  writeFileSync(path, gzip(encodeDump(1_700_000_000_000, records)));
  const out: DumpRecord[] = [];
  for await (const record of readDump(path)) {
    out.push(record);
  }
  return out;
}

test('varint and varlong vectors', () => {
  assert.deepEqual([...varInt(0)], [0]);
  assert.deepEqual([...varInt(127)], [127]);
  assert.deepEqual([...varInt(128)], [0x80, 0x01]);
  assert.deepEqual([...varInt(255)], [0xff, 0x01]);
  assert.deepEqual([...varInt(25565)], [0xdd, 0xc7, 0x01]);
  assert.deepEqual([...varInt(2147483647)], [0xff, 0xff, 0xff, 0xff, 0x07]);
  assert.deepEqual([...varInt(-1)], [0xff, 0xff, 0xff, 0xff, 0x0f]);
  assert.equal(varLong(-1).length, 10);
  assert.deepEqual([...varLong(2147483648)], [0x80, 0x80, 0x80, 0x80, 0x08]);
});

test('dump round trip, including large frames and big timestamps', async () => {
  const big = Buffer.concat([varInt(0x21), Buffer.alloc(70_000, 7)]);
  const records = await roundTrip([
    { type: 'open', conn: 1, leg: 0, remote: '127.0.0.1:5000', protocol: 775, nextState: 2 },
    { type: 'identify', conn: 1, name: 'Steve', uuid: '069a79f4-44e9-4726-a5be-fca90e38aaf5' },
    { type: 'packet', seq: 0, conn: 1, us: 5_000_000_000, dir: 0, state: 2, id: 0, frame: Buffer.from([0, 1, 2]) },
    { type: 'packet', seq: 0, conn: 1, us: 5_000_000_001, dir: 1, state: 3, id: 0x21, frame: big },
    { type: 'close', conn: 1, us: 6_000_000_000 },
  ]);
  assert.equal(records[0]!.type, 'header');
  assert.equal(records[1]!.type, 'open');
  const identify = records[2]!;
  assert.ok(identify.type === 'identify' && identify.uuid === '069a79f4-44e9-4726-a5be-fca90e38aaf5');
  const packets = records.filter((r) => r.type === 'packet');
  assert.deepEqual(packets.map((p) => p.seq), [0, 1]);
  assert.equal(packets[0]!.us, 5_000_000_000);
  assert.ok(packets[1]!.frame.equals(big));
  assert.equal(packets[1]!.id, 0x21);
  assert.equal(records.at(-1)!.type, 'close');
});

test('a truncated dump yields the complete prefix', async () => {
  const records = await roundTrip(
    [
      { type: 'packet', seq: 0, conn: 1, us: 1, dir: 0, state: 3, id: 5, frame: Buffer.from([5, 9, 9]) },
      { type: 'packet', seq: 0, conn: 1, us: 2, dir: 0, state: 3, id: 6, frame: Buffer.from([6, 9, 9]) },
    ],
    // cut mid-record, as a crash would leave a live capture
    (raw) => gzipSync(raw.subarray(0, raw.length - 2)),
  );
  assert.deepEqual(records.filter((r) => r.type === 'packet').map((p) => p.id), [5]);
});

test('protocol table: names, wildcards, state prefixes and properties', () => {
  const { file, properties } = buildProtocol(REPORT, '26.3', 775);
  const table = new ProtocolTable(file);
  assert.equal(table.name(3, 1, 52), 'minecraft:move_entity_pos');
  assert.equal(table.name(3, 1, 99), 'unknown_0x63');
  assert.equal(table.keys('move_entity_*').length, 2);
  assert.equal(table.keys('Minecraft:Move_Player_Pos').length, 1);
  assert.equal(table.keys('play/hello').length, 0);
  assert.equal(table.keys('login/hello').length, 1);
  assert.match(properties, /^protocol=775$/m);
  assert.match(properties, /^login\.serverbound\.login_acknowledged=3$/m);
  assert.match(properties, /^configuration\.serverbound\.finish_configuration=3$/m);
  assert.match(properties, /^play\.serverbound\.configuration_acknowledged=15$/m);
});

/** Mirrors SniffCraft's Logger.cpp loader. */
function readScbin(path: string) {
  const bytes = readFileSync(path);
  let pos = 0;
  const varLong = (): bigint => {
    let value = 0n;
    for (let i = 0; ; i++) {
      const b = bytes[pos++]!;
      value |= BigInt(b & 0x7f) << BigInt(7 * i);
      if ((b & 0x80) === 0) return value;
    }
  };
  const protocol = Number(varLong());
  const startMs = Number(varLong());
  const items: { compressed: boolean; state: number; origin: number; ms: number; bandwidth: number; frame: Buffer }[] = [];
  while (pos < bytes.length) {
    const compressed = bytes[pos++] === 1;
    const size = Number(varLong());
    let body: Buffer = bytes.subarray(pos, pos + size);
    pos += size;
    if (compressed) body = inflateSync(body);
    let at = 0;
    const read = (): number => {
      let value = 0n;
      for (let i = 0; ; i++) {
        const b = body[at++]!;
        value |= BigInt(b & 0x7f) << BigInt(7 * i);
        if ((b & 0x80) === 0) return Number(value);
      }
    };
    items.push({ compressed, state: read(), origin: read(), ms: read(), bandwidth: read(), frame: Buffer.from(body.subarray(at)) });
  }
  return { protocol, startMs, items };
}

function packet(seq: number, conn: number, us: number, dir: number, state: number, frame: Buffer): PacketRecord {
  return { type: 'packet', seq, conn, us, dir, state, id: frame[0]!, frame };
}

test('sniffcraft export writes one readable file per connection', async () => {
  const outDir = mkdtempSync(join(tmpdir(), 'capture-export-'));
  const { file } = buildProtocol(REPORT, '26.3', 775);
  const long = Buffer.concat([Buffer.from([0x21]), Buffer.alloc(400, 3)]);
  const packets = [
    packet(0, 1, 1000, 0, 0, Buffer.from([0, 1, 2])),
    packet(1, 2, 2000, 0, 2, Buffer.from([0, 9, 9])),
    packet(2, 1, 6000, 1, 4, long),
    packet(3, 1, 7000, 1, 255, Buffer.from([1])),
    packet(4, 1, 9000, 0, 3, Buffer.from([30, 5])),
  ];
  const connections = new Map<number, ConnMeta>([
    [1, { id: 1, leg: 0, remote: 'a:1', protocol: 775, name: 'Steve' }],
    [2, { id: 2, leg: 1, remote: 'b:2', protocol: 775, name: 'Alex' }],
  ]);
  const lines = await sniffcraft.export({
    capture: { id: 'cap' } as CaptureMeta,
    instance: 'proxy',
    protocol: new ProtocolTable(file),
    connections,
    startEpochMs: 1_700_000_000_000,
    packets: async function* () {
      yield* packets;
    },
    receiverOf: () => 'x',
    outDir,
    options: {},
  });
  assert.ok(lines.some((line) => line.includes('cap_proxy_frontend_Steve.scbin')));
  const steve = readScbin(join(outDir, 'cap_proxy_frontend_Steve.scbin'));
  assert.equal(steve.protocol, 775);
  assert.equal(steve.startMs, 1_700_000_000_001);
  assert.equal(steve.items.length, 3, 'the unknown-state packet is skipped');
  assert.deepEqual(steve.items.map((i) => [i.state, i.origin, i.ms]), [[0, 1, 0], [4, 0, 5], [3, 1, 8]]);
  assert.deepEqual(steve.items.map((i) => i.compressed), [false, true, false]);
  assert.ok(steve.items[1]!.frame.equals(long));
  assert.equal(steve.items[1]!.bandwidth, long.length);
  const alex = readScbin(join(outDir, 'cap_proxy_backend_Alex.scbin'));
  assert.deepEqual(alex.items.map((i) => [i.state, i.origin]), [[2, 1]]);
});

const CONFIG = {
  limbo: { enabled: true, port: 30000 },
  servers: [{ name: 'lobby', port: 30066 }],
} as unknown as DevConfig;
const instance = (name: string, platform: Instance['platform']) => ({ name, platform }) as Instance;

test('receivers and links', () => {
  const frontend: ConnMeta = { id: 1, leg: 0, remote: '1.2.3.4:5', protocol: 775 };
  const backend: ConnMeta = { id: 2, leg: 1, remote: '127.0.0.1:30066', protocol: 775 };
  const proxy = instance('proxy', 'velocity');
  assert.equal(receiverOf(CONFIG, proxy, frontend, 0), 'proxy');
  assert.equal(receiverOf(CONFIG, proxy, frontend, 1), 'client');
  assert.equal(receiverOf(CONFIG, proxy, backend, 0), 'lobby');
  assert.equal(receiverOf(CONFIG, proxy, backend, 1), 'proxy');
  assert.equal(linkOf(CONFIG, proxy, backend, 1), 'lobby→proxy');
  assert.equal(linkOf(CONFIG, instance('lobby', 'paper'), frontend, 0), 'proxy→lobby');
  assert.equal(linkOf(CONFIG, instance('limbo', 'limbo'), frontend, 1), 'limbo→client');
  assert.equal(receiverOf(CONFIG, proxy, { ...backend, remote: '127.0.0.1:30000' }, 0), 'limbo');
});

test('filters combine type, direction, receiver and player', () => {
  const { file } = buildProtocol(REPORT, '26.3', 775);
  const table = new ProtocolTable(file);
  const conns = new Map<number, ConnMeta>([
    [1, { id: 1, leg: 0, remote: 'c:1', protocol: 775, name: 'Steve' }],
    [2, { id: 2, leg: 1, remote: '127.0.0.1:30066', protocol: 775, name: 'Steve' }],
  ]);
  const proxy = instance('proxy', 'velocity');
  const receiver = (conn: ConnMeta, dir: number) => receiverOf(CONFIG, proxy, conn, dir);
  const make = (values: Parameters<typeof parseFilter>[0]) => compileFilter(parseFilter(values), table, conns, receiver);
  const move = { conn: 1, state: 3, dir: 1, id: 52 };
  assert.ok(make({ type: ['move_entity_*'] })(move));
  assert.ok(!make({ type: ['move_entity_rot'] })(move));
  assert.ok(make({ dir: 'cb', to: ['client'] })(move));
  assert.ok(!make({ to: ['lobby'] })(move));
  assert.ok(make({ to: ['lobby'] })({ conn: 2, state: 3, dir: 0, id: 30 }));
  assert.ok(make({ player: ['steve'] })(move));
  assert.ok(!make({ player: ['alex'] })(move));
  assert.throws(() => make({ type: ['nope'] }));
});

test('durations, sizes and list flags', () => {
  assert.equal(parseSpan('30s'), 30_000);
  assert.equal(parseSpan('5m'), 300_000);
  assert.equal(parseSpan('2'), 2000);
  assert.equal(parseSize('64M'), 64 * 1024 ** 2);
  assert.equal(parseSize('2g'), 2 * 1024 ** 3);
  assert.equal(parseCount('10', '--packets'), 10);
  assert.throws(() => parseSpan('soon'));
  assert.throws(() => parseSize('big'));
  assert.throws(() => parseCount('0', '--packets'));
  assert.deepEqual(split(['a,b', 'c', ' ']), ['a', 'b', 'c']);
});

test('the wizards only run when no matching flag was given, and print equivalent flags', () => {
  assert.equal(hasFilterFlags({}), false);
  assert.equal(hasFilterFlags({ type: [] }), false);
  assert.equal(hasFilterFlags({ dir: 'cb' }), true);
  assert.equal(hasRecordingFlags({}), false);
  assert.equal(hasRecordingFlags({ 'next-join': false }), false);
  assert.equal(hasRecordingFlags({ player: ['Steve'] }), true);
  assert.equal(hasRecordingFlags({ watch: true }), true);
  assert.equal(
    filterFlags({ type: ['play/move_*', 'login/hello'], dir: 'cb', to: ['lobby'], player: ["O'Neil"], state: 'play', conn: ['3', '4'] }),
    "--type 'play/move_*,login/hello' --dir cb --to lobby --player 'O'\\''Neil' --state play --conn 3,4",
  );
  assert.equal(
    recordingFlags({ players: ['Steve'], nextJoin: true, duration: '30s', packets: '100', maxSize: '64M', until: ['finish_configuration'], label: 'join test', watch: true }),
    "--player Steve --next-join --duration 30s --packets 100 --max-size 64M --until finish_configuration --label 'join test' --watch",
  );
  assert.equal(recordingFlags({ players: [], nextJoin: false, until: [], watch: false }), '');
});

test('packet type listing merges directions per state/name', () => {
  const table = new ProtocolTable(buildProtocol(REPORT, '26.3', 775).file);
  const listing = table.listing();
  assert.deepEqual(listing.find((entry) => entry.pattern === 'login/hello')?.dirs, [0]);
  assert.deepEqual(listing.find((entry) => entry.pattern === 'play/move_entity_pos')?.dirs, [1]);
  assert.deepEqual(listing.map((entry) => entry.pattern), [...listing.map((entry) => entry.pattern)].sort((a, b) => a.localeCompare(b)));
});
