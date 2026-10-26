import { mkdir } from 'node:fs/promises';
import { BreakEvents } from './blocks.ts';
import { i32, varIntWidth } from './bytes.ts';
import { varInt } from './encode.ts';
import { connectionFile, type ExportContext, type Exporter } from './types.ts';
import {
  decodeLoginFinished,
  decodePlayLogin,
  encodeAddEntity,
  encodeLoginFinished,
  encodePositionSync,
  encodeRotateHead,
  LocalPlayer,
} from './play.ts';
import { writeZip } from './zip.ts';
import type { PacketRecord } from '../format.ts';
import type { ProtocolTable } from '../protocol.ts';
import type { ConnMeta } from '../store.ts';

const FILE_FORMAT_VERSION = 14;
const LOCAL_INTERVAL_US = 50_000;

/** Packets ReplayMod does not record (keep-alives and connection management). */
const DROPPED = new Set(['keep_alive', 'ping', 'disconnect', 'transfer', 'store_cookie', 'cookie_request', 'custom_report_details']);

interface SelfPackets {
  playerType: number;
  addEntity: number;
  positionSync: number;
  rotateHead: number;
}

/** One connection's recording: clientbound frames with a millisecond timestamp, plus the synthesized recording player. */
class Session {
  readonly records: Buffer[] = [];
  readonly local = new LocalPlayer();
  readonly breaks = new BreakEvents();
  packets = 0;
  errors = 0;
  firstError: string | undefined;
  broken: string | undefined;
  joined = false;
  firstUs = 0;
  lastMs = 0;
  private started = false;
  private selfSpawned = false;
  private lastLocalUs = Number.NEGATIVE_INFINITY;
  private readonly conn: ConnMeta | undefined;
  private readonly protocol: ProtocolTable;
  private readonly self: SelfPackets | undefined;
  private readonly levelEventId: number | undefined;

  constructor(conn: ConnMeta | undefined, protocol: ProtocolTable, self: SelfPackets | undefined) {
    this.conn = conn;
    this.protocol = protocol;
    this.self = self;
    this.levelEventId = protocol.id(3, 1, 'level_event');
  }

  handle(name: string, packet: PacketRecord): void {
    try {
      this.process(name, packet);
    } catch (error) {
      this.errors++;
      this.firstError ??= `${name}: ${(error as Error).message}`;
    }
  }

  private process(name: string, packet: PacketRecord): void {
    const payload = packet.frame.subarray(varIntWidth(packet.frame));
    if (packet.dir === 0) {
      if (packet.state === 3 && this.joined && name.startsWith('move_player_')) {
        this.local.applyServerbound(name, payload);
        this.spawnSelf(packet.us);
        this.flushLocal(packet.us);
      } else if (packet.state === 3 && this.joined && name === 'player_action') {
        this.breaks.onPacket(0, name, payload, packet.us);
      }
      return;
    }
    if (packet.state === 2) {
      if (name === 'login_finished' && !this.started) {
        const login = decodeLoginFinished(payload);
        this.local.uuid = login.uuid;
        this.local.name = login.name;
        this.start(packet.us, packet.frame);
      }
    } else if (packet.state === 4) {
      if (!this.started && !this.synthesizeLogin(packet.us)) {
        return;
      }
      if (name === 'registry_data') {
        this.breaks.onPacket(1, name, payload, packet.us);
      }
      this.write(name, packet.us, packet.frame);
    } else if (packet.state === 3 && this.started) {
      this.play(name, packet, payload);
    }
  }

  private start(us: number, frame: Buffer): void {
    this.started = true;
    this.firstUs = us;
    this.append(us, frame);
  }

  /** A capture that starts after the login has no `login_finished`; build one from who the connection is. */
  private synthesizeLogin(us: number): boolean {
    const id = this.protocol.id(2, 1, 'login_finished');
    const uuid = this.conn?.uuid;
    const name = this.conn?.name;
    if (id === undefined || !uuid || !name) {
      this.broken = 'the player identity is unknown (no login_finished and no name or uuid in the dump)';
      return false;
    }
    this.local.uuid = uuid;
    this.local.name = name;
    this.start(us, Buffer.concat([varInt(id), encodeLoginFinished(uuid, name)]));
    return true;
  }

  private play(name: string, packet: PacketRecord, payload: Buffer): void {
    if (name === 'login') {
      const login = decodePlayLogin(payload);
      this.local.reset();
      this.local.entityId = login.entityId;
      this.local.dimension = login.dimension;
      this.selfSpawned = false;
      this.joined = true;
    } else if (name === 'respawn') {
      this.local.reset();
      this.selfSpawned = false;
    }
    this.flushLocal(packet.us);
    const found = this.breaks.onPacket(1, name, payload, packet.us);
    if (this.levelEventId !== undefined) {
      for (const event of found.events) {
        this.append(packet.us, Buffer.concat([varInt(this.levelEventId), event]));
        this.packets++;
      }
    }
    if (!found.drop) {
      this.write(name, packet.us, packet.frame);
    }
    if (name === 'player_position') {
      this.local.applyPlayerPosition(payload);
      this.spawnSelf(packet.us);
    }
  }

  private write(name: string, us: number, frame: Buffer): void {
    if (!DROPPED.has(name)) {
      this.append(us, frame);
      this.packets++;
    }
  }

  private append(us: number, frame: Buffer): void {
    const ms = Math.max(this.lastMs, Math.floor((us - this.firstUs) / 1000));
    this.lastMs = ms;
    this.records.push(i32(ms), i32(frame.length), frame);
  }

  /** ReplayMod's recorder adds the recording player itself, since the server never sends it its own entity. */
  private spawnSelf(us: number): void {
    const { self, local } = this;
    if (!self || this.selfSpawned || !local.pos || !local.uuid || local.entityId < 0) {
      return;
    }
    const e = { id: local.entityId, type: self.playerType, uuid: local.uuid, ...local.pos };
    this.append(us, Buffer.concat([varInt(self.addEntity), encodeAddEntity(e)]));
    this.selfSpawned = true;
    this.lastLocalUs = us;
    local.dirty = false;
  }

  /** Writes the recording player's position at most every 50 ms, like the tick-based recorder. */
  private flushLocal(us: number, force = false): void {
    const { self, local } = this;
    if (!self || !this.selfSpawned || !local.dirty || !local.pos || (!force && us - this.lastLocalUs < LOCAL_INTERVAL_US)) {
      return;
    }
    const { x, y, z, yaw, pitch, headYaw, onGround } = local.pos;
    this.append(us, Buffer.concat([varInt(self.positionSync), encodePositionSync({ id: local.entityId, x, y, z, yaw, pitch, onGround })]));
    this.append(us, Buffer.concat([varInt(self.rotateHead), encodeRotateHead(local.entityId, headYaw)]));
    this.lastLocalUs = us;
    local.dirty = false;
  }

  finish(lastUs: number): void {
    this.flushLocal(lastUs, true);
  }
}

/**
 * ReplayMod recordings (https://github.com/ReplayMod/ReplayMod), `.mcpr` zips with `recording.tmcpr` and
 * `metaData.json` (file format version 14, as ReplayStudio writes it). The recording is a stream of clientbound
 * packets: i32 time in ms, i32 size, then the frame (id + payload), starting in the login state at `login_finished`
 * and going through configuration to play. The server never sends a player its own entity, which ReplayMod adds
 * itself while recording, so the recording player is spawned and moved from the serverbound movement packets
 * (`add_entity`, then `entity_position_sync` and `rotate_head`). Everything else passes through as sent.
 */
export const replaymod: Exporter = {
  name: 'replaymod',
  summary: 'ReplayMod .mcpr, one per connection',
  async export(ctx: ExportContext): Promise<string[]> {
    await mkdir(ctx.outDir, { recursive: true });
    const lines: string[] = [];
    const playerType = ctx.protocol.playerEntityType ?? (ctx.options['player-type'] ? Number(ctx.options['player-type']) : undefined);
    const ids = {
      addEntity: ctx.protocol.id(3, 1, 'add_entity'),
      positionSync: ctx.protocol.id(3, 1, 'entity_position_sync'),
      rotateHead: ctx.protocol.id(3, 1, 'rotate_head'),
    };
    let self: SelfPackets | undefined;
    if (playerType === undefined || Number.isNaN(playerType)) {
      lines.push('note: recording player not spawned: no player entity type id; run dev capture protocol --refresh and recapture, or pass --opt player-type=<id>');
    } else if (ids.addEntity === undefined || ids.positionSync === undefined || ids.rotateHead === undefined) {
      lines.push('note: recording player not spawned: the packet table lacks add_entity, entity_position_sync or rotate_head');
    } else {
      self = { playerType, addEntity: ids.addEntity, positionSync: ids.positionSync, rotateHead: ids.rotateHead };
    }
    // Sessions are held in memory until the end of the dump; captures are dev-sized.
    const sessions = new Map<number, Session>();
    const lastUs = new Map<number, number>();
    let skipped = 0;
    for await (const packet of ctx.packets()) {
      if (packet.state === 255) {
        skipped++;
        continue;
      }
      let session = sessions.get(packet.conn);
      if (!session) {
        session = new Session(ctx.connections.get(packet.conn), ctx.protocol, self);
        sessions.set(packet.conn, session);
      }
      lastUs.set(packet.conn, packet.us);
      session.handle(ctx.protocol.name(packet.state, packet.dir, packet.id).replace(/^minecraft:/, ''), packet);
    }
    let errors = 0;
    let firstError: string | undefined;
    const written: string[] = [];
    for (const [connId, session] of sessions) {
      session.finish(lastUs.get(connId) ?? session.firstUs);
      errors += session.errors + session.breaks.errors;
      firstError ??= session.firstError ?? session.breaks.firstError;
      const { path, label } = connectionFile(ctx, connId, 'mcpr');
      if (session.broken || !session.joined) {
        written.push(
          `note: ${label}: ${session.broken ?? 'no complete join; start capturing before the player joins'}, skipped`,
        );
        continue;
      }
      const metadata = {
        singleplayer: false,
        serverName: ctx.instance,
        customServerName: `Paradisu dev ${ctx.instance}`,
        duration: session.lastMs,
        date: ctx.startEpochMs + Math.floor(session.firstUs / 1000),
        mcversion: ctx.protocol.minecraftVersion,
        fileFormat: 'MCPR',
        fileFormatVersion: FILE_FORMAT_VERSION,
        protocol: ctx.protocol.protocolVersion,
        generator: 'Paradisu dev capture',
        selfId: session.local.entityId,
        players: session.local.uuid ? [session.local.uuid] : [],
      };
      await writeZip(path, [
        { name: 'recording.tmcpr', data: Buffer.concat(session.records) },
        { name: 'metaData.json', data: Buffer.from(JSON.stringify(metadata)) },
      ]);
      written.push(path);
    }
    lines.push(...written);
    if (sessions.size === 0) {
      lines.push('note: no packets matched; nothing written');
    }
    if (skipped > 0) {
      lines.push(`note: ${skipped} packets skipped (unknown connection state)`);
    }
    if (errors > 0) {
      lines.push(`note: ${errors} packets could not be decoded (first: ${firstError})`);
    }
    return lines;
  },
};
