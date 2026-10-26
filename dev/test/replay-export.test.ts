import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { mkdtempSync, readFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { test } from 'node:test';
import { BlockTracker, BreakEvents, decodePosition, encodePosition } from '../src/capture/exporters/blocks.ts';
import { angle, bool, f32, f64, i32, PayloadReader, u8, uuidBytes } from '../src/capture/exporters/bytes.ts';
import { string, varInt } from '../src/capture/exporters/encode.ts';
import { flashback } from '../src/capture/exporters/flashback.ts';
import {
  decodeLoginFinished,
  decodeMovePos,
  decodePlayerPosition,
  decodePlayLogin,
  decodePositionSync,
  EntityTracker,
  encodeLoginFinished,
  LocalPlayer,
  skipLpVec3,
} from '../src/capture/exporters/play.ts';
import { replaymod } from '../src/capture/exporters/replaymod.ts';
import type { ExportContext } from '../src/capture/exporters/types.ts';
import { readZip, writeZip } from '../src/capture/exporters/zip.ts';
import type { PacketRecord } from '../src/capture/format.ts';
import { buildProtocol, ProtocolTable } from '../src/capture/protocol.ts';
import type { CaptureMeta, ConnMeta } from '../src/capture/store.ts';

const PLAYER_TYPE = 155;
const UUID = '11111111-2222-3333-4444-555555555555';
const REPORT = {
  handshake: { serverbound: { 'minecraft:intention': { protocol_id: 0 } } },
  status: { serverbound: { 'minecraft:status_request': { protocol_id: 0 } } },
  login: {
    serverbound: { 'minecraft:login_acknowledged': { protocol_id: 3 } },
    clientbound: { 'minecraft:login_finished': { protocol_id: 2 } },
  },
  configuration: {
    serverbound: { 'minecraft:finish_configuration': { protocol_id: 3 } },
    clientbound: { 'minecraft:registry_data': { protocol_id: 7 }, 'minecraft:finish_configuration': { protocol_id: 3 } },
  },
  play: {
    serverbound: { 'minecraft:configuration_acknowledged': { protocol_id: 15 }, 'minecraft:move_player_pos': { protocol_id: 30 }, 'minecraft:player_action': { protocol_id: 41 } },
    clientbound: {
      'minecraft:bundle_delimiter': { protocol_id: 0 },
      'minecraft:add_entity': { protocol_id: 1 },
      'minecraft:entity_position_sync': { protocol_id: 35 },
      'minecraft:keep_alive': { protocol_id: 43 },
      'minecraft:login': { protocol_id: 50 },
      'minecraft:move_entity_pos': { protocol_id: 54 },
      'minecraft:player_position': { protocol_id: 73 },
      'minecraft:rotate_head': { protocol_id: 85 },
      'minecraft:respawn': { protocol_id: 84 },
      'minecraft:start_configuration': { protocol_id: 120 },
      'minecraft:level_chunk_with_light': { protocol_id: 44 },
      'minecraft:block_update': { protocol_id: 8 },
      'minecraft:block_changed_ack': { protocol_id: 4 },
      'minecraft:level_event': { protocol_id: 46 },
    },
  },
};
const REGISTRIES = { 'minecraft:entity_type': { entries: { 'minecraft:player': { protocol_id: PLAYER_TYPE } } } };
const ID = { loginFinished: 2, registryData: 7, finishConfiguration: 3, bundle: 0, addEntity: 1, sync: 35, keepAlive: 43, login: 50, move: 54, playerPosition: 73, rotateHead: 85, startConfiguration: 120, sbMove: 30, chunk: 44, blockUpdate: 8, ack: 4, levelEvent: 46, playerAction: 41 };

function table(withPlayerType = true): ProtocolTable {
  return new ProtocolTable(buildProtocol(REPORT, '26.3', 777, withPlayerType ? REGISTRIES : undefined).file);
}

function frame(id: number, ...parts: Buffer[]): Buffer {
  return Buffer.concat([varInt(id), ...parts]);
}

function pkt(seq: number, conn: number, us: number, dir: number, state: number, data: Buffer): PacketRecord {
  return { type: 'packet', seq, conn, us, dir, state, id: data[0]!, frame: data };
}

const playLoginPayload = (entityId: number) =>
  Buffer.concat([
    i32(entityId),
    bool(false),
    varInt(1),
    string('minecraft:overworld'),
    varInt(20),
    varInt(8),
    varInt(8),
    bool(false),
    bool(true),
    bool(false),
    varInt(0),
    string('minecraft:overworld'),
    Buffer.alloc(8),
    varInt(1),
    bool(false), // trailing fields the decoder ignores
    varInt(0),
  ]);

const addEntityPayload = (id: number, x: number, y: number, z: number) =>
  Buffer.concat([varInt(id), uuidBytes(UUID), varInt(5), f64(x), f64(y), f64(z), Buffer.from([0]), angle(0), angle(90), angle(90), varInt(0)]);

const movePosPayload = (id: number, dx: number, dy: number, dz: number, onGround = true) => {
  const short = (v: number) => {
    const b = Buffer.alloc(2);
    b.writeInt16BE(v);
    return b;
  };
  return Buffer.concat([varInt(id), varInt(onGround ? 1 : 0), short(dx), short(dy), short(dz)]);
};

const sbMovePayload = (x: number, y: number, z: number) => Buffer.concat([f64(x), f64(y), f64(z), u8(1)]);

const connections = new Map<number, ConnMeta>([[1, { id: 1, leg: 0, remote: 'a:1', protocol: 777, name: 'Steve', uuid: UUID }]]);

function context(packets: PacketRecord[], protocol: ProtocolTable, options: ExportContext['options'] = {}): ExportContext & { outDir: string } {
  const outDir = mkdtempSync(join(tmpdir(), 'replay-export-'));
  return {
    capture: { id: 'cap' } as CaptureMeta,
    instance: 'proxy',
    protocol,
    connections,
    startEpochMs: 1_700_000_000_000,
    packets: async function* () {
      yield* packets;
    },
    receiverOf: () => 'x',
    outDir,
    options,
  };
}

/** A join followed by some movement, as the proxy sees the client's connection. */
function session(withLogin = true): PacketRecord[] {
  let seq = 0;
  const p = (us: number, dir: number, state: number, data: Buffer) => pkt(seq++, 1, us, dir, state, data);
  return [
    ...(withLogin ? [p(0, 1, 2, frame(ID.loginFinished, encodeLoginFinished(UUID, 'Steve')))] : []),
    p(1000, 1, 4, frame(ID.registryData, Buffer.from([1, 2, 3]))),
    p(2000, 1, 4, frame(ID.registryData, Buffer.from([4, 5]))),
    p(3000, 1, 4, frame(ID.finishConfiguration)),
    p(4000, 1, 3, frame(ID.login, playLoginPayload(7))),
    p(124_000, 0, 3, frame(ID.sbMove, sbMovePayload(10, 64, 20))),
    p(164_000, 1, 3, frame(ID.bundle)),
    p(165_000, 1, 3, frame(ID.addEntity, addEntityPayload(9, 1, 2, 3))),
    p(170_000, 1, 3, frame(ID.move, movePosPayload(9, 4096, 0, 0))),
    p(190_000, 0, 3, frame(ID.sbMove, sbMovePayload(11, 64, 20))),
    p(200_000, 1, 3, frame(ID.keepAlive, Buffer.from([0, 0, 0, 1]))),
  ];
}

/** Flashback and ReplayMod chunks/records: the readers mirror what the mods do. */
function readChunk(buf: Buffer) {
  const r = new PayloadReader(buf);
  assert.equal(r.i32() >>> 0, 0xd780e884);
  const names: string[] = [];
  for (let n = r.varInt(); n > 0; n--) names.push(r.string());
  const snapshotSize = r.i32();
  const readActions = (end: number) => {
    const out: { name: string; payload: Buffer }[] = [];
    while (r.pos < end) {
      const name = names[r.varInt()]!;
      out.push({ name, payload: r.bytes(r.i32()) });
    }
    return out;
  };
  const snapshot = readActions(r.pos + snapshotSize);
  const actions = readActions(buf.length);
  return { snapshot, actions };
}

function readMoves(payload: Buffer) {
  const r = new PayloadReader(payload);
  assert.equal(r.varInt(), 1);
  const dimension = r.string();
  const moves: { id: number; x: number; y: number; z: number; yaw: number; pitch: number; headYaw: number; onGround: boolean }[] = [];
  for (let n = r.varInt(); n > 0; n--) {
    moves.push({ id: r.varInt(), x: r.f64(), y: r.f64(), z: r.f64(), yaw: r.f32(), pitch: r.f32(), headYaw: r.f32(), onGround: r.bool() });
  }
  assert.equal(r.remaining(), 0);
  return { dimension, moves };
}

function readRecords(buf: Buffer) {
  const r = new PayloadReader(buf);
  const out: { ms: number; frame: Buffer }[] = [];
  while (r.remaining() > 0) {
    const ms = r.i32();
    out.push({ ms, frame: r.bytes(r.i32()) });
  }
  return out;
}

test('zip round trip, readable by unzip', async () => {
  const dir = mkdtempSync(join(tmpdir(), 'zip-test-'));
  const path = join(dir, 'x.zip');
  const entries = [
    { name: 'a.txt', data: Buffer.from('hello hello hello') },
    { name: 'dir/b.bin', data: Buffer.alloc(5000, 7) },
    { name: 'empty', data: Buffer.alloc(0) },
  ];
  await writeZip(path, entries);
  const read = readZip(readFileSync(path));
  assert.deepEqual([...read.keys()], entries.map((e) => e.name));
  for (const entry of entries) assert.ok(read.get(entry.name)!.equals(entry.data));
  const unzip = spawnSync('unzip', ['-t', path], { encoding: 'utf8' });
  if (unzip.error === undefined) assert.equal(unzip.status, 0, unzip.stdout + unzip.stderr);
});

test('play decoders', () => {
  const finished = decodeLoginFinished(
    Buffer.concat([uuidBytes(UUID), string('Steve'), varInt(1), string('textures'), string('abc'), bool(true), string('sig'), uuidBytes(UUID)]),
  );
  assert.equal(finished.uuid, UUID);
  assert.equal(finished.name, 'Steve');
  assert.equal(finished.profile.length, 16 + 6 + 1 + 9 + 4 + 1 + 4);

  assert.deepEqual(decodePlayLogin(playLoginPayload(7)), { entityId: 7, dimensionType: 0, dimension: 'minecraft:overworld', gameMode: 1 });

  const linear = decodeMovePos(movePosPayload(9, 2048, -4096, 1, false), false);
  assert.deepEqual([linear.id, linear.dx, linear.dy, linear.dz, linear.onGround], [9, 0.5, -1, 1 / 4096, false]);
  const stepped = decodeMovePos(Buffer.concat([varInt(9), varInt((2 << 1) | 1), varInt(0), Buffer.from([0x10, 0, 0, 0, 0, 0]), varInt(1), Buffer.from([0x10, 0, 0, 0, 0, 0])]), false);
  assert.deepEqual([stepped.dx, stepped.dy, stepped.dz, stepped.onGround], [2, 0, 0, true]);

  const linearSync = decodePositionSync(Buffer.concat([varInt(4), varInt(0), f64(1), f64(2), f64(3), f32(90), f32(-10), bool(true)]));
  assert.deepEqual(linearSync, { id: 4, x: 1, y: 2, z: 3, yaw: 90, pitch: -10, onGround: true });
  const pathSync = decodePositionSync(
    Buffer.concat([varInt(4), varInt(1), varInt(2), f64(1), f64(1), f64(1), varInt(0), f64(5), f64(6), f64(7), varInt(1), f32(0), f32(0), bool(false)]),
  );
  assert.deepEqual([pathSync.x, pathSync.y, pathSync.z], [5, 6, 7]);

  const position = Buffer.concat([varInt(1), f64(1), f64(2), f64(3), f64(0), f64(0), f64(0), f32(10), f32(20), i32(0x01 | 0x08)]);
  assert.deepEqual(decodePlayerPosition(position), { x: 1, y: 2, z: 3, yaw: 10, pitch: 20, flags: 9 });
  const local = new LocalPlayer();
  local.applyServerbound('move_player_pos_rot', Buffer.concat([f64(100), f64(64), f64(100), f32(30), f32(5), u8(0)]));
  local.applyPlayerPosition(position); // x and yaw relative
  assert.deepEqual([local.pos!.x, local.pos!.y, local.pos!.yaw, local.pos!.pitch], [101, 2, 40, 20]);

  for (const bytes of [[0], [0x02, 0, 1, 2, 3, 4], [0x06, 0, 1, 2, 3, 4, 5]]) {
    const r = new PayloadReader(Buffer.from(bytes));
    skipLpVec3(r);
    assert.equal(r.remaining(), 0);
  }
});

test('entity tracker applies deltas to spawned entities only', () => {
  const tracker = new EntityTracker();
  assert.ok(tracker.apply('move_entity_pos', movePosPayload(9, 4096, 0, 0)));
  assert.equal(tracker.dirty.size, 0, 'unknown entity is ignored');
  tracker.apply('add_entity', addEntityPayload(9, 1, 2, 3));
  assert.equal(tracker.dirty.size, 0, 'a spawn is already in the stream');
  tracker.apply('move_entity_pos', movePosPayload(9, 4096, 0, 0));
  assert.deepEqual(tracker.takeDirty().map(([id, at]) => [id, at.x]), [[9, 2]]);
  assert.equal(tracker.dirty.size, 0);
  assert.equal(tracker.apply('system_chat', Buffer.alloc(0)), false);
});

test('flashback export: snapshot, filtered packets and movement per tick', async () => {
  const packets = [
    ...session(),
    pkt(50, 1, 300_000, 1, 3, frame(ID.startConfiguration)),
    pkt(51, 1, 301_000, 1, 4, frame(ID.registryData, Buffer.from([9]))),
    pkt(52, 1, 302_000, 1, 3, frame(ID.login, playLoginPayload(8))),
  ];
  const ctx = context(packets, table());
  const lines = await flashback.export(ctx);
  assert.ok(lines.some((line) => line.includes('cap_proxy_frontend_Steve.zip')), lines.join('\n'));
  const zip = readZip(readFileSync(join(ctx.outDir, 'cap_proxy_frontend_Steve.zip')));
  const meta = JSON.parse(zip.get('metadata.json')!.toString());
  assert.equal(meta.protocol_version, 777);
  assert.deepEqual(Object.keys(meta.chunks), ['c0.flashback', 'c1.flashback']);
  assert.equal(meta.chunks['c0.flashback'].forcePlaySnapshot, false);
  assert.equal(meta.chunks['c1.flashback'].forcePlaySnapshot, true);

  const c0 = readChunk(zip.get('c0.flashback')!);
  assert.deepEqual(
    c0.snapshot.map((a) => a.name),
    ['flashback:action/configuration_packet', 'flashback:action/configuration_packet', 'flashback:action/game_packet', 'flashback:action/create_local_player'],
  );
  const created = new PayloadReader(c0.snapshot[3]!.payload);
  assert.equal(created.uuid().toString('hex'), UUID.replaceAll('-', ''));
  const gamePackets = c0.actions.filter((a) => a.name === 'flashback:action/game_packet').map((a) => a.payload[0]);
  assert.deepEqual(gamePackets, [ID.addEntity], 'bundle_delimiter, move_entity_pos and keep_alive are not recorded as packets');
  const ticks = c0.actions.filter((a) => a.name === 'flashback:action/next_tick').length;
  assert.equal(ticks, meta.chunks['c0.flashback'].duration);
  const moves = c0.actions.filter((a) => a.name === 'flashback:action/move_entities').map((a) => readMoves(a.payload));
  assert.equal(moves[0]!.dimension, 'minecraft:overworld');
  assert.deepEqual(moves[0]!.moves.map((m) => [m.id, m.x, m.y, m.z]), [[7, 10, 64, 20]]);
  const last = moves[moves.length - 1]!.moves;
  assert.equal(last.find((m) => m.id === 9)!.x, 2);
  assert.equal(last.find((m) => m.id === 7)!.x, 11);

  const c1 = readChunk(zip.get('c1.flashback')!);
  assert.equal(c1.snapshot.length, 3);
  assert.equal(meta.chunks['c1.flashback'].duration, 1, 'an empty chunk gets one tick');
  assert.equal(meta.total_ticks, meta.chunks['c0.flashback'].duration + 1);
});

test('flashback export skips connections without a join and takes the gamemode option', async () => {
  const noJoin = [pkt(0, 1, 0, 1, 3, frame(ID.keepAlive))];
  const skipped = await flashback.export(context(noJoin, table()));
  assert.ok(skipped.some((line) => line.includes('no complete join')), skipped.join('\n'));
  assert.ok(!skipped.some((line) => line.includes('.zip  (')));

  const ctx = context(session(), table(), { gamemode: '3' });
  await flashback.export(ctx);
  const zip = readZip(readFileSync(join(ctx.outDir, 'cap_proxy_frontend_Steve.zip')));
  const created = readChunk(zip.get('c0.flashback')!).snapshot[3]!.payload;
  assert.equal(created[created.length - 1], 3);
});

test('replaymod export: login, passthrough, self entity and metadata', async () => {
  const ctx = context(session(), table());
  const lines = await replaymod.export(ctx);
  assert.ok(lines.some((line) => line.includes('cap_proxy_frontend_Steve.mcpr')), lines.join('\n'));
  const zip = readZip(readFileSync(join(ctx.outDir, 'cap_proxy_frontend_Steve.mcpr')));
  const records = readRecords(zip.get('recording.tmcpr')!);
  assert.equal(records[0]!.frame[0], ID.loginFinished);
  assert.equal(records[0]!.ms, 0);
  assert.deepEqual(records.map((r) => r.ms), [...records.map((r) => r.ms)].sort((a, b) => a - b));
  const ids = records.map((r) => r.frame[0]);
  assert.ok(!ids.includes(ID.keepAlive));
  assert.ok(ids.includes(ID.bundle));
  assert.ok(ids.includes(ID.finishConfiguration));
  const spawn = records.find((r) => r.frame[0] === ID.addEntity && new PayloadReader(r.frame.subarray(1)).varInt() === 7)!;
  assert.equal(spawn.ms, 124);
  const spawned = new PayloadReader(spawn.frame.subarray(1));
  spawned.varInt();
  spawned.uuid();
  assert.equal(spawned.varInt(), PLAYER_TYPE);
  const syncs = records.filter((r) => r.frame[0] === ID.sync).map((r) => decodePositionSync(r.frame.subarray(1)));
  assert.deepEqual(syncs.map((s) => [s.id, s.x]), [[7, 11]]);
  assert.ok(records.some((r) => r.frame[0] === ID.rotateHead));

  const meta = JSON.parse(zip.get('metaData.json')!.toString());
  assert.equal(meta.fileFormatVersion, 14);
  assert.equal(meta.fileFormat, 'MCPR');
  assert.equal(meta.protocol, 777);
  assert.equal(meta.selfId, 7);
  assert.deepEqual(meta.players, [UUID]);
  assert.equal(meta.duration, records[records.length - 1]!.ms);
});

test('replaymod export synthesizes login_finished, and explains a missing player type', async () => {
  const ctx = context(session(false), table(false));
  const lines = await replaymod.export(ctx);
  assert.ok(lines.some((line) => line.includes('no player entity type')), lines.join('\n'));
  const zip = readZip(readFileSync(join(ctx.outDir, 'cap_proxy_frontend_Steve.mcpr')));
  const records = readRecords(zip.get('recording.tmcpr')!);
  assert.equal(records[0]!.frame[0], ID.loginFinished);
  assert.equal(decodeLoginFinished(records[0]!.frame.subarray(1)).name, 'Steve');
  assert.ok(!records.some((r) => r.frame[0] === ID.addEntity && new PayloadReader(r.frame.subarray(1)).varInt() === 7));

  const withOption = context(session(false), table(false), { 'player-type': '99' });
  await replaymod.export(withOption);
  const again = readRecords(readZip(readFileSync(join(withOption.outDir, 'cap_proxy_frontend_Steve.mcpr'))).get('recording.tmcpr')!);
  assert.ok(again.some((r) => r.frame[0] === ID.addEntity && new PayloadReader(r.frame.subarray(1)).varInt() === 7));

  const noJoin = await replaymod.export(context([pkt(0, 1, 0, 1, 3, frame(ID.keepAlive))], table()));
  assert.ok(noJoin.some((line) => line.includes('skipped')), noJoin.join('\n'));
});

test('buildProtocol reads the player entity type and ProtocolTable finds ids by name', () => {
  const built = table();
  assert.equal(built.playerEntityType, PLAYER_TYPE);
  assert.equal(table(false).playerEntityType, undefined);
  assert.equal(built.id(3, 1, 'minecraft:login'), 50);
  assert.equal(built.id(3, 1, 'login'), 50);
  assert.equal(built.id(3, 0, 'login'), undefined);
});

const STONE = 5;
const WOOL = 2423;

/** A chunk packet whose section for y 64..79 (index 8 from -64) has an indirect palette with `block` at (x, 64, z) */
function chunkPayload(cx: number, cz: number, block: { x: number; z: number; state: number }): Buffer {
  const section = (blockStates: Buffer) => Buffer.concat([Buffer.from([0, 1, 0, 0]), blockStates, Buffer.from([0, 0])]);
  const air = section(Buffer.from([0, 0]));
  const longs = Buffer.alloc(256 * 8);
  const index = (0 << 8) | (block.z << 4) | block.x; // y 64 is local y 0 of the section
  const at = Math.floor(index / 16) * 8;
  longs.writeBigUInt64BE(1n << BigInt((index % 16) * 4), at);
  const indirect = section(Buffer.concat([Buffer.from([4]), varInt(2), varInt(0), varInt(block.state), longs]));
  const data = Buffer.concat([...Array(8).fill(air), indirect]);
  return Buffer.concat([i32(cx), i32(cz), varInt(1), varInt(1), varInt(1), Buffer.alloc(8), varInt(data.length), data, varInt(0)]);
}

const blockUpdatePayload = (x: number, y: number, z: number, state: number) => Buffer.concat([encodePosition(x, y, z), varInt(state)]);
const playerActionPayload = (status: number, x: number, y: number, z: number, sequence: number) =>
  Buffer.concat([varInt(status), encodePosition(x, y, z), u8(1), varInt(sequence)]);
const levelEventPayload = (x: number, y: number, z: number, data: number) => Buffer.concat([i32(2001), encodePosition(x, y, z), i32(data), bool(false)]);

test('block tracker reads chunks and follows updates', () => {
  assert.deepEqual(decodePosition(BigInt.asIntN(64, BigInt(`0x${encodePosition(-166, 56, -24).toString('hex')}`))), { x: -166, y: 56, z: -24 });
  const tracker = new BlockTracker();
  tracker.chunk(chunkPayload(0, 1, { x: 3, z: 4, state: STONE }));
  assert.equal(tracker.get(3, 64, 20), STONE);
  assert.equal(tracker.get(4, 64, 20), 0);
  assert.equal(tracker.get(3, 0, 20), 0, 'single valued section');
  assert.equal(tracker.get(3, 64, 100), undefined, 'unloaded chunk');
  tracker.set(3, 10, 20, WOOL);
  assert.equal(tracker.get(3, 10, 20), WOOL);
  assert.equal(tracker.get(4, 10, 20), 0);
  tracker.forget(Buffer.concat([i32(1), i32(0)]));
  assert.equal(tracker.get(3, 64, 20), undefined);
});

test('break events are rebuilt for confirmed digs only, and not twice', () => {
  const breaks = new BreakEvents();
  breaks.onPacket(1, 'level_chunk_with_light', chunkPayload(0, 0, { x: 3, z: 4, state: WOOL }), 0);
  breaks.blocks.set(5, 64, 5, STONE);
  // an update nobody dug is not a break
  assert.deepEqual(breaks.onPacket(1, 'block_update', blockUpdatePayload(5, 64, 5, 0), 1000).events, []);
  breaks.blocks.set(5, 64, 5, STONE);
  breaks.onPacket(0, 'player_action', playerActionPayload(2, 3, 64, 4, 7), 2000);
  const found = breaks.onPacket(1, 'block_update', blockUpdatePayload(3, 64, 4, 0), 3000);
  assert.equal(found.events.length, 1);
  assert.ok(found.events[0]!.equals(levelEventPayload(3, 64, 4, WOOL)));
  assert.equal(breaks.count, 1);
  // the server's own copy of the same event right after is dropped
  assert.equal(breaks.onPacket(1, 'level_event', levelEventPayload(3, 64, 4, WOOL), 4000).drop, true);
  // a dig the server already announced is not rebuilt
  breaks.onPacket(0, 'player_action', playerActionPayload(0, 5, 64, 5, 8), 10_000);
  assert.equal(breaks.onPacket(1, 'level_event', levelEventPayload(5, 64, 5, STONE), 11_000).drop, false);
  assert.deepEqual(breaks.onPacket(1, 'block_update', blockUpdatePayload(5, 64, 5, 0), 12_000).events, []);
  // an unconfirmed dig expires with its ack
  breaks.blocks.set(5, 64, 5, STONE);
  breaks.onPacket(0, 'player_action', playerActionPayload(2, 5, 64, 5, 9), 20_000);
  breaks.onPacket(1, 'block_changed_ack', varInt(9), 21_000);
  assert.deepEqual(breaks.onPacket(1, 'block_update', blockUpdatePayload(5, 64, 5, 0), 22_000).events, []);
  assert.equal(breaks.errors, 0, breaks.firstError);
});

test('both exporters record the recording player\'s own block breaks', async () => {
  const base = session();
  const extra = (n: number, us: number, dir: number, state: number, data: Buffer) => pkt(100 + n, 1, us, dir, state, data);
  const packets = [
    ...base.slice(0, 5),
    extra(0, 5000, 1, 3, frame(ID.chunk, chunkPayload(0, 0, { x: 3, z: 4, state: WOOL }))),
    ...base.slice(5, 7),
    extra(1, 170_000, 0, 3, frame(ID.playerAction, playerActionPayload(2, 3, 64, 4, 1))),
    extra(2, 175_000, 1, 3, frame(ID.blockUpdate, blockUpdatePayload(3, 64, 4, 0))),
    extra(3, 176_000, 1, 3, frame(ID.ack, varInt(1))),
    ...base.slice(7),
  ];
  const events = (frames: Buffer[]) =>
    frames.filter((f) => f[0] === ID.levelEvent).map((f) => f.subarray(1)).filter((p) => p.readInt32BE(0) === 2001);

  const fb = context(packets, table());
  const lines = await flashback.export(fb);
  const zip = readZip(readFileSync(join(fb.outDir, 'cap_proxy_frontend_Steve.zip')));
  const chunk = readChunk(zip.get('c0.flashback')!);
  const fbFrames = chunk.actions.filter((a) => a.name === 'flashback:action/game_packet').map((a) => a.payload);
  const fbEvents = events(fbFrames);
  assert.equal(fbEvents.length, 1);
  assert.ok(fbEvents[0]!.equals(levelEventPayload(3, 64, 4, WOOL)));
  const order = fbFrames.map((f) => f[0]);
  assert.ok(order.indexOf(ID.levelEvent) < order.indexOf(ID.blockUpdate), 'the break event comes before the block update');

  const rm = context(packets, table());
  const rmLines = await replaymod.export(rm);
  const records = readRecords(readZip(readFileSync(join(rm.outDir, 'cap_proxy_frontend_Steve.mcpr'))).get('recording.tmcpr')!);
  const rmEvents = events(records.map((r) => r.frame));
  assert.equal(rmEvents.length, 1);
  assert.ok(rmEvents[0]!.equals(levelEventPayload(3, 64, 4, WOOL)));
});
