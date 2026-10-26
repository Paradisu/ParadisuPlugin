import { mkdir } from 'node:fs/promises';
import { randomUUID } from 'node:crypto';
import { bool, f32, f64, i32, uuidBytes, varIntWidth } from './bytes.ts';
import { string, varInt } from './encode.ts';
import { BreakEvents } from './blocks.ts';
import { connectionFile, type ExportContext, type Exporter } from './types.ts';
import { decodeLoginFinished, decodePlayLogin, decodeRespawn, EntityTracker, type EntityPos, LocalPlayer } from './play.ts';
import { writeZip } from './zip.ts';
import type { PacketRecord } from '../format.ts';
import type { ConnMeta } from '../store.ts';

const MAGIC = 0xd780e884;
const ACTIONS = [
  'flashback:action/next_tick',
  'flashback:action/game_packet',
  'flashback:action/configuration_packet',
  'flashback:action/create_local_player',
  'flashback:action/move_entities',
];
const NEXT_TICK = 0;
const GAME_PACKET = 1;
const CONFIGURATION_PACKET = 2;
const CREATE_LOCAL_PLAYER = 3;
const MOVE_ENTITIES = 4;
const TICK_US = 50_000;

/** Configuration packets Flashback rejects or does not want (mirrors its `IgnoredPacketSet`). */
const DROPPED_CONFIGURATION = new Set([
  'finish_configuration',
  'keep_alive',
  'ping',
  'disconnect',
  'transfer',
  'store_cookie',
  'cookie_request',
  'custom_report_details',
  'server_links',
  'clear_dialog',
  'show_dialog',
  'code_of_conduct',
]);

/**
 * Play packets Flashback rejects (its handlers throw) or records differently, from `IgnoredPacketSet.IGNORED`. Sounds
 * and level events are not in here: Flashback re-records those on the client, but a server capture only has these.
 * Entity movement is handled separately (it becomes `move_entities` actions).
 */
const DROPPED_PLAY = new Set([
  'bundle_delimiter',
  'keep_alive',
  'ping',
  'pong_response',
  'disconnect',
  'transfer',
  'store_cookie',
  'cookie_request',
  'custom_report_details',
  'server_links',
  'clear_dialog',
  'show_dialog',
  'start_configuration',
  'award_stats',
  'recipe_book_add',
  'recipe_book_remove',
  'recipe_book_settings',
  'open_sign_editor',
  'player_chat',
  'delete_chat',
  'move_minecart_along_track',
  'container_close',
  'container_set_content',
  'mount_screen_open',
  'container_set_data',
  'container_set_slot',
  'forget_level_chunk',
  'player_abilities',
  'set_cursor_item',
  'set_experience',
  'set_health',
  'set_player_inventory',
  'ticking_state',
  'ticking_step',
  'player_combat_end',
  'player_combat_enter',
  'player_combat_kill',
  'set_camera',
  'cooldown',
  'update_advancements',
  'select_advancements_tab',
  'place_ghost_recipe',
  'commands',
  'command_suggestions',
  'update_recipes',
  'tag_query',
  'open_book',
  'open_screen',
  'merchant_offers',
  'set_chunk_cache_radius',
  'set_simulation_distance',
  'set_chunk_cache_center',
  'block_changed_ack',
  'custom_chat_completions',
  'chunk_batch_start',
  'chunk_batch_finished',
  'debug_sample',
  'test_instance_block_status',
  'waypoint',
  'game_rule_values',
  'low_disk_space_warning',
]);

const MOVEMENT_PLAY = new Set(['move_entity_pos', 'move_entity_pos_rot', 'move_entity_rot', 'rotate_head']);

function action(index: number, payload: Buffer = Buffer.alloc(0)): Buffer {
  return Buffer.concat([varInt(index), i32(payload.length), payload]);
}

interface Chunk {
  snapshot: Buffer[];
  actions: Buffer[];
  ticks: number;
  /** Whether a play `login` reached this chunk; a chunk cut off during configuration is dropped */
  joined: boolean;
  forceSnapshot: boolean;
}

function newChunk(forceSnapshot: boolean): Chunk {
  return { snapshot: [], actions: [], ticks: 0, joined: false, forceSnapshot };
}

function chunkBytes(chunk: Chunk): Buffer {
  const snapshot = Buffer.concat(chunk.snapshot);
  const header = [i32(MAGIC), varInt(ACTIONS.length), ...ACTIONS.map((name) => string(name))];
  return Buffer.concat([...header, i32(snapshot.length), snapshot, ...chunk.actions]);
}

/** One connection's recording: a chunk per configuration phase, ticks derived from the packet timestamps. */
class Session {
  readonly chunks: Chunk[] = [];
  readonly tracker = new EntityTracker();
  readonly local = new LocalPlayer();
  readonly breaks = new BreakEvents();
  packets = 0;
  errors = 0;
  firstError: string | undefined;
  broken: string | undefined;
  private phase: 'waiting' | 'config' | 'play' = 'waiting';
  private chunk: Chunk | undefined;
  private tickStartUs = 0;
  private globalTicks = 0;
  private lastUs = 0;
  private readonly conn: ConnMeta | undefined;
  private readonly gameMode: number | undefined;
  private readonly levelEventId: number | undefined;

  constructor(conn: ConnMeta | undefined, gameMode: number | undefined, levelEventId: number | undefined) {
    this.conn = conn;
    this.gameMode = gameMode;
    this.levelEventId = levelEventId;
  }

  get ticks(): number {
    return this.globalTicks;
  }

  get joined(): boolean {
    return this.chunks.some((chunk) => chunk.joined);
  }

  handle(name: string, packet: PacketRecord): void {
    this.lastUs = packet.us;
    try {
      this.process(name, packet);
    } catch (error) {
      this.errors++;
      this.firstError ??= `${name}: ${(error as Error).message}`;
    }
  }

  private process(name: string, packet: PacketRecord): void {
    const payload = packet.frame.subarray(varIntWidth(packet.frame));
    if (packet.state === 2 && packet.dir === 1) {
      if (name === 'login_finished') {
        const login = decodeLoginFinished(payload);
        this.local.uuid = login.uuid;
        this.local.name = login.name;
        this.local.profile = login.profile;
      }
    } else if (packet.state === 4 && packet.dir === 1) {
      if (name === 'registry_data') {
        this.breaks.onPacket(1, name, payload, packet.us);
      }
      this.configuration(name, packet);
    } else if (packet.state === 3 && packet.dir === 1 && name === 'login' && this.phase === 'config') {
      this.play(name, packet, payload);
    } else if (packet.state === 3 && this.phase === 'play') {
      if (packet.dir === 0) {
        if (name.startsWith('move_player_')) {
          this.advance(packet.us);
          this.local.applyServerbound(name, payload);
        } else if (name === 'player_action') {
          this.breaks.onPacket(0, name, payload, packet.us);
        }
      } else {
        this.advance(packet.us);
        this.play(name, packet, payload);
      }
    }
  }

  private configuration(name: string, packet: PacketRecord): void {
    if (this.phase !== 'config') {
      this.endChunk();
      this.chunk = newChunk(this.chunks.length > 0);
      this.chunks.push(this.chunk);
      this.phase = 'config';
    }
    if (!DROPPED_CONFIGURATION.has(name)) {
      this.chunk!.snapshot.push(action(CONFIGURATION_PACKET, packet.frame));
      this.packets++;
    }
  }

  private play(name: string, packet: PacketRecord, payload: Buffer): void {
    const chunk = this.chunk!;
    const found = this.breaks.onPacket(1, name, payload, packet.us);
    if (this.levelEventId !== undefined) {
      for (const event of found.events) {
        chunk.actions.push(action(GAME_PACKET, Buffer.concat([varInt(this.levelEventId), event])));
        this.packets++;
      }
    }
    if (found.drop) {
      return;
    }
    if (name === 'login') {
      const login = decodePlayLogin(payload);
      this.local.reset();
      this.local.entityId = login.entityId;
      this.local.dimension = login.dimension;
      this.local.gameMode = this.gameMode ?? login.gameMode;
      this.tracker.clear();
      this.tickStartUs = packet.us - this.globalTicks * TICK_US;
      this.createLocalPlayer(chunk, packet.frame);
      return;
    }
    if (name === 'respawn') {
      this.local.dimension = decodeRespawn(payload).dimension;
      this.tracker.clear();
    } else if (name === 'player_position') {
      this.local.applyPlayerPosition(payload);
      return;
    } else if (MOVEMENT_PLAY.has(name)) {
      this.tracker.apply(name, payload);
      return;
    } else if (name === 'add_entity' || name === 'entity_position_sync' || name === 'teleport_entity' || name === 'remove_entities') {
      this.tracker.apply(name, payload);
    } else if (DROPPED_PLAY.has(name) || name.startsWith('debug')) {
      return;
    }
    chunk.actions.push(action(GAME_PACKET, packet.frame));
    this.packets++;
  }

  /** Writes the play `login` and the `create_local_player` that Flashback expects right after it. */
  private createLocalPlayer(chunk: Chunk, loginFrame: Buffer): void {
    const local = this.local;
    const uuid = local.uuid ?? this.conn?.uuid;
    if (!uuid) {
      this.broken = 'the player identity is unknown (no login_finished and no uuid in the dump)';
      return;
    }
    const profile = local.profile ?? Buffer.concat([uuidBytes(uuid), string(local.name ?? this.conn?.name ?? 'unknown'), varInt(0)]);
    const at: EntityPos = local.pos ?? { x: 0, y: 0, z: 0, yaw: 0, pitch: 0, headYaw: 0, onGround: false };
    chunk.snapshot.push(
      action(GAME_PACKET, loginFrame),
      action(
        CREATE_LOCAL_PLAYER,
        Buffer.concat([
          uuidBytes(uuid),
          f64(at.x),
          f64(at.y),
          f64(at.z),
          f32(at.pitch),
          f32(at.yaw),
          f32(at.headYaw),
          f64(0),
          f64(0),
          f64(0),
          profile,
          varInt(local.gameMode),
        ]),
      ),
    );
    chunk.joined = true;
    this.phase = 'play';
    this.packets++;
  }

  private advance(us: number): void {
    while (us >= this.tickStartUs + (this.globalTicks + 1) * TICK_US) {
      this.emitTick();
    }
  }

  /** One game tick: the entities that moved, then `next_tick`. Time passes even when nothing moved. */
  private emitTick(): void {
    const chunk = this.chunk!;
    const moves: [number, EntityPos][] = [];
    if (this.local.dirty && this.local.pos) {
      moves.push([this.local.entityId, { ...this.local.pos }]);
      this.local.dirty = false;
    }
    moves.push(...this.tracker.takeDirty());
    if (moves.length > 0) {
      const parts = [varInt(1), string(this.local.dimension), varInt(moves.length)];
      for (const [id, at] of moves) {
        parts.push(varInt(id), f64(at.x), f64(at.y), f64(at.z), f32(at.yaw), f32(at.pitch), f32(at.headYaw), bool(at.onGround));
      }
      chunk.actions.push(action(MOVE_ENTITIES, Buffer.concat(parts)));
    }
    chunk.actions.push(action(NEXT_TICK));
    chunk.ticks++;
    this.globalTicks++;
  }

  private endChunk(): void {
    if (this.chunk && this.chunk.joined && this.chunk.ticks === 0) {
      // Flashback does not cope with zero-length chunks
      this.emitTick();
    }
  }

  /** Flushes the movement up to the last packet and closes the last chunk; drops a chunk that never got to play. */
  finish(): void {
    if (this.phase === 'play') {
      this.advance(this.lastUs);
    }
    if (this.phase === 'play' && (this.local.dirty || this.tracker.dirty.size > 0)) {
      this.emitTick();
    }
    this.endChunk();
    while (this.chunks.length > 0 && !this.chunks[this.chunks.length - 1]!.joined) {
      this.chunks.pop();
    }
  }
}

/**
 * Flashback replays (https://github.com/Moulberry/Flashback), a zip per connection with `metadata.json` and
 * `c<N>.flashback` chunks. A chunk is: i32 magic 0xD780E884, the action names (VarInt count, Strings), an i32
 * snapshot size, the snapshot actions, then the actions; an action is VarInt index, i32 size, payload. A Flashback
 * recording is what the client saw, so this rebuilds that from the server side of the connection:
 *   - configuration and play packets pass through, minus those Flashback rejects (DROPPED_*);
 *   - entity movement packets become `move_entities` actions once per 50 ms, from the tracked absolute positions;
 *   - the recording player is created from the serverbound movement packets (`create_local_player`);
 *   - a new chunk starts at each configuration phase and replays its snapshot (`forcePlaySnapshot`).
 * The capture must include the join (login and configuration), and the client must run the same protocol.
 */
export const flashback: Exporter = {
  name: 'flashback',
  summary: 'Flashback replay .zip, one per connection',
  async export(ctx: ExportContext): Promise<string[]> {
    await mkdir(ctx.outDir, { recursive: true });
    const override = ctx.options.gamemode === undefined || ctx.options.gamemode === true ? undefined : Number(ctx.options.gamemode);
    // Sessions are held in memory until the end of the dump; captures are dev-sized.
    const sessions = new Map<number, Session>();
    let skipped = 0;
    for await (const packet of ctx.packets()) {
      if (packet.state === 255) {
        skipped++;
        continue;
      }
      let session = sessions.get(packet.conn);
      if (!session) {
        session = new Session(ctx.connections.get(packet.conn), override, ctx.protocol.id(3, 1, 'level_event'));
        sessions.set(packet.conn, session);
      }
      session.handle(ctx.protocol.name(packet.state, packet.dir, packet.id).replace(/^minecraft:/, ''), packet);
    }
    const lines: string[] = [];
    let errors = 0;
    let firstError: string | undefined;
    for (const [connId, session] of sessions) {
      session.finish();
      errors += session.errors + session.breaks.errors;
      firstError ??= session.firstError ?? session.breaks.firstError;
      const { path, label } = connectionFile(ctx, connId, 'zip');
      if (session.broken || !session.joined) {
        lines.push(
          `note: ${label}: ${session.broken ?? 'no complete join; start capturing before the player joins'}, skipped`,
        );
        continue;
      }
      const chunks: Record<string, { duration: number; forcePlaySnapshot: boolean }> = {};
      const entries = session.chunks.map((chunk, index) => {
        chunks[`c${index}.flashback`] = { duration: chunk.ticks, forcePlaySnapshot: chunk.forceSnapshot };
        return { name: `c${index}.flashback`, data: chunkBytes(chunk) };
      });
      const metadata = {
        uuid: randomUUID(),
        name: `${ctx.capture.id} ${session.local.name ?? label}`,
        version_string: ctx.protocol.minecraftVersion,
        world_name: ctx.instance,
        protocol_version: ctx.protocol.protocolVersion,
        total_ticks: session.ticks,
        chunks,
      };
      await writeZip(path, [{ name: 'metadata.json', data: Buffer.from(JSON.stringify(metadata, null, 2)) }, ...entries]);
      lines.push(path);
    }
    if (lines.length === 0) {
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
