import { bool, i32, PayloadReader } from './bytes.ts';
import { decodePlayLogin, decodeRespawn } from './play.ts';

/**
 * Block state tracking for the replay exporters, Minecraft 26.3 layouts (like play.ts, fix them here on a version
 * bump; the chunk format is documented at minecraft.wiki/w/Java_Edition_protocol/Chunk_format).
 *
 * Why: a client predicts its own block breaks, so the server never sends the recording player the break particles and
 * sound (`level_event` 2001, whose data is the broken block's state id). Flashback and ReplayMod record those events on
 * the client, so a replay made from a server-side dump lacks them. BreakEvents rebuilds them: it follows the block
 * states the client was told about and, when the server confirms a dig with a `block_update`, reports the state that
 * was there before.
 */

const BREAK_EVENT = 2001;
const AIR = 0;
/** How long after a dig the server's confirmation (or a server-sent break event for the same block) still counts */
const WINDOW_US = 2_000_000;
const DUPLICATE_US = 500_000;

/** `min_y` of the vanilla dimension types, for registry entries sent without data (they come from the known packs). */
const VANILLA_MIN_Y: Record<string, number> = { 'minecraft:overworld': -64, 'minecraft:overworld_caves': -64 };

/** x, y, z of a Position (26 bit x, 26 bit z, 12 bit y; all signed). */
export function decodePosition(value: bigint): { x: number; y: number; z: number } {
  return { x: Number(BigInt.asIntN(26, value >> 38n)), z: Number(BigInt.asIntN(26, value >> 12n)), y: Number(BigInt.asIntN(12, value)) };
}

export function encodePosition(x: number, y: number, z: number): Buffer {
  const out = Buffer.alloc(8);
  out.writeBigUInt64BE(((BigInt(x) & 0x3ffffffn) << 38n) | ((BigInt(z) & 0x3ffffffn) << 12n) | (BigInt(y) & 0xfffn));
  return out;
}

/** The payload of a `level_event` packet for a block break. */
export function encodeBreakEvent(x: number, y: number, z: number, state: number): Buffer {
  return Buffer.concat([i32(BREAK_EVENT), encodePosition(x, y, z), i32(state), bool(false)]);
}

function skipNbtPayload(r: PayloadReader, type: number): void {
  switch (type) {
    case 1:
      r.bytes(1);
      break;
    case 2:
      r.bytes(2);
      break;
    case 3:
    case 5:
      r.bytes(4);
      break;
    case 4:
    case 6:
      r.bytes(8);
      break;
    case 7:
      r.bytes(r.i32());
      break;
    case 8:
      r.bytes(r.u16());
      break;
    case 9: {
      const element = r.u8();
      for (let n = r.i32(); n > 0; n--) {
        skipNbtPayload(r, element);
      }
      break;
    }
    case 10:
      readCompound(r, () => {});
      break;
    case 11:
      r.bytes(r.i32() * 4);
      break;
    case 12:
      r.bytes(r.i32() * 8);
      break;
    default:
      throw new Error(`unknown NBT tag ${type}`);
  }
}

/** Reads a compound body (after its tag header), reporting each int. */
function readCompound(r: PayloadReader, onInt: (name: string, value: number) => void): void {
  while (true) {
    const type = r.u8();
    if (type === 0) {
      return;
    }
    const name = r.bytes(r.u16()).toString('utf8');
    if (type === 3) {
      onInt(name, r.i32());
    } else {
      skipNbtPayload(r, type);
    }
  }
}

/** A paletted container's values, or undefined when only the stream position matters (`keep` false). */
function readContainer(r: PayloadReader, entries: number, maxIndirectBits: number, keep: boolean): number | Uint16Array | undefined {
  const bits = r.u8();
  let palette: number[] | undefined;
  if (bits === 0) {
    const value = r.varInt();
    return keep ? value : undefined;
  }
  if (bits <= maxIndirectBits) {
    palette = [];
    for (let n = r.varInt(); n > 0; n--) {
      palette.push(r.varInt());
    }
  }
  const perLong = Math.floor(64 / bits);
  const longs = Math.ceil(entries / perLong);
  const data = r.bytes(longs * 8);
  if (!keep) {
    return undefined;
  }
  const out = new Uint16Array(entries);
  const mask = bits >= 32 ? 0xffffffff : (1 << bits) - 1;
  for (let i = 0; i < entries; i++) {
    const at = Math.floor(i / perLong) * 8;
    const shift = (i % perLong) * bits;
    const hi = data.readUInt32BE(at);
    const lo = data.readUInt32BE(at + 4);
    const raw = shift + bits <= 32 ? lo >>> shift : shift >= 32 ? hi >>> (shift - 32) : (lo >>> shift) | (hi << (32 - shift));
    const index = (raw >>> 0) & mask;
    out[i] = palette ? (palette[index] ?? 0) : index;
  }
  return out;
}

/** The block states of every loaded chunk of the recording player's dimension, as the client has been told them. */
export class BlockTracker {
  private dimensionMinY: number[] = [];
  private minY = -64;
  /** `x,z` of the chunk to its sections bottom to top: one state, or 4096 */
  private readonly chunks = new Map<string, (number | Uint16Array)[]>();

  /** `registry_data` of the configuration phase: remembers the `min_y` of every dimension type. */
  registryData(payload: Buffer): void {
    const r = new PayloadReader(payload);
    if (r.string() !== 'minecraft:dimension_type') {
      return;
    }
    const minYs: number[] = [];
    for (let n = r.varInt(); n > 0; n--) {
      const id = r.string();
      let minY = VANILLA_MIN_Y[id] ?? 0;
      if (r.bool()) {
        if (r.u8() !== 10) {
          throw new Error('registry entry is not a compound');
        }
        readCompound(r, (name, value) => {
          if (name === 'min_y') {
            minY = value;
          }
        });
      }
      minYs.push(minY);
    }
    this.dimensionMinY = minYs;
  }

  /** A new world (login or respawn): forgets the chunks. */
  enterDimension(dimensionType: number): void {
    this.chunks.clear();
    this.minY = this.dimensionMinY[dimensionType] ?? -64;
  }

  chunk(payload: Buffer): void {
    const r = new PayloadReader(payload);
    const x = r.i32();
    const z = r.i32();
    for (let n = r.varInt(); n > 0; n--) {
      r.varInt(); // heightmap type
      r.bytes(r.varInt() * 8);
    }
    const data = new PayloadReader(r.bytes(r.varInt()));
    const sections: (number | Uint16Array)[] = [];
    while (data.remaining() > 0) {
      data.bytes(4); // block count, fluid count
      sections.push(readContainer(data, 4096, 8, true)!);
      readContainer(data, 64, 3, false); // biomes
    }
    this.chunks.set(`${x},${z}`, sections);
  }

  /** `forget_level_chunk`: z comes first on the wire. */
  forget(payload: Buffer): void {
    const r = new PayloadReader(payload);
    const z = r.i32();
    const x = r.i32();
    this.chunks.delete(`${x},${z}`);
  }

  get(x: number, y: number, z: number): number | undefined {
    const section = this.chunks.get(`${x >> 4},${z >> 4}`)?.[(y >> 4) - (this.minY >> 4)];
    if (section === undefined) {
      return undefined;
    }
    return typeof section === 'number' ? section : section[((y & 15) << 8) | ((z & 15) << 4) | (x & 15)];
  }

  /** Sets a block in a loaded chunk; blocks outside the known chunks are ignored. */
  set(x: number, y: number, z: number, state: number): void {
    const sections = this.chunks.get(`${x >> 4},${z >> 4}`);
    const index = (y >> 4) - (this.minY >> 4);
    const section = sections?.[index];
    if (!sections || section === undefined) {
      return;
    }
    if (typeof section === 'number') {
      if (section === state) {
        return;
      }
      const expanded = new Uint16Array(4096).fill(section);
      sections[index] = expanded;
      expanded[((y & 15) << 8) | ((z & 15) << 4) | (x & 15)] = state;
    } else {
      section[((y & 15) << 8) | ((z & 15) << 4) | (x & 15)] = state;
    }
  }

  /** `section_blocks_update` */
  sectionBlocks(payload: Buffer): void {
    const r = new PayloadReader(payload);
    const section = r.i64();
    const sx = Number(BigInt.asIntN(22, section >> 42n));
    const sz = Number(BigInt.asIntN(22, section >> 20n));
    const sy = Number(BigInt.asIntN(20, section));
    for (let n = r.varInt(); n > 0; n--) {
      const entry = r.varLong();
      const local = Number(entry & 0xfffn);
      this.set(sx * 16 + ((local >> 8) & 15), sy * 16 + (local & 15), sz * 16 + ((local >> 4) & 15), Number(entry >> 12n));
    }
  }
}

interface Dig {
  us: number;
  sequence: number;
}

function key(x: number, y: number, z: number): string {
  return `${x},${y},${z}`;
}

/**
 * Finds the block breaks of the recording player that the server does not announce. Feed it every packet of the play
 * state in order (and `registry_data` of configuration); it returns the `level_event` payloads to write before the
 * packet it was given. A dig counts when the server answers it with a `block_update` that changes a block it knew.
 */
export class BreakEvents {
  readonly blocks = new BlockTracker();
  /** Break events rebuilt so far */
  count = 0;
  errors = 0;
  firstError: string | undefined;
  private readonly digs = new Map<string, Dig>();
  private readonly rebuilt = new Map<string, number>();
  private readonly announced = new Map<string, number>();

  /**
   * `dir` is 0 for serverbound, 1 for clientbound; `name` is the packet name without `minecraft:`. `drop` is set for
   * a server-sent break event that was already rebuilt, so the replay does not play it twice.
   */
  onPacket(dir: number, name: string, payload: Buffer, us: number): { events: Buffer[]; drop: boolean } {
    try {
      return this.process(dir, name, payload, us);
    } catch (error) {
      this.errors++;
      this.firstError ??= `${name}: ${(error as Error).message}`;
      return { events: [], drop: false };
    }
  }

  private process(dir: number, name: string, payload: Buffer, us: number): { events: Buffer[]; drop: boolean } {
    const none = { events: [], drop: false };
    if (dir === 0) {
      if (name === 'player_action') {
        const r = new PayloadReader(payload);
        const status = r.varInt();
        const { x, y, z } = decodePosition(r.i64());
        r.u8(); // face
        // 0 starts digging (this is also the only packet of an instant break), 2 finishes it
        if (status === 0 || status === 2) {
          this.expire(us);
          this.digs.set(key(x, y, z), { us, sequence: r.varInt() });
        }
      }
      return none;
    }
    switch (name) {
      case 'registry_data':
        this.blocks.registryData(payload);
        break;
      case 'login':
        this.reset(decodePlayLogin(payload).dimensionType);
        break;
      case 'respawn':
        this.reset(decodeRespawn(payload).dimensionType);
        break;
      case 'level_chunk_with_light':
        this.blocks.chunk(payload);
        break;
      case 'forget_level_chunk':
        this.blocks.forget(payload);
        break;
      case 'section_blocks_update':
        this.blocks.sectionBlocks(payload);
        break;
      case 'block_changed_ack': {
        const sequence = new PayloadReader(payload).varInt();
        for (const [at, dig] of this.digs) {
          if (dig.sequence <= sequence) {
            this.digs.delete(at);
          }
        }
        break;
      }
      case 'block_update':
        return this.blockUpdate(payload, us);
      case 'level_event': {
        const r = new PayloadReader(payload);
        if (r.i32() === BREAK_EVENT) {
          const { x, y, z } = decodePosition(r.i64());
          const at = key(x, y, z);
          const earlier = this.rebuilt.get(at);
          if (earlier !== undefined && us - earlier < DUPLICATE_US) {
            return { events: [], drop: true };
          }
          this.announced.set(at, us);
        }
        break;
      }
    }
    return none;
  }

  private reset(dimensionType: number): void {
    this.blocks.enterDimension(dimensionType);
    this.digs.clear();
    this.rebuilt.clear();
    this.announced.clear();
  }

  private blockUpdate(payload: Buffer, us: number): { events: Buffer[]; drop: boolean } {
    const r = new PayloadReader(payload);
    const { x, y, z } = decodePosition(r.i64());
    const state = r.varInt();
    const old = this.blocks.get(x, y, z);
    this.blocks.set(x, y, z, state);
    const at = key(x, y, z);
    const dig = this.digs.get(at);
    if (!dig || us - dig.us > WINDOW_US || old === undefined || old === AIR || old === state) {
      return { events: [], drop: false };
    }
    this.digs.delete(at);
    const announced = this.announced.get(at);
    if (announced !== undefined && us - announced < DUPLICATE_US) {
      return { events: [], drop: false };
    }
    this.rebuilt.set(at, us);
    this.count++;
    return { events: [encodeBreakEvent(x, y, z, old)], drop: false };
  }

  private expire(us: number): void {
    for (const [at, dig] of this.digs) {
      if (us - dig.us > WINDOW_US) {
        this.digs.delete(at);
      }
    }
  }
}
