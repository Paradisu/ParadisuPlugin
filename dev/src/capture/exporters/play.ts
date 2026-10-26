import { angle, bool, f32, f64, PayloadReader, uuidBytes } from './bytes.ts';
import { string, varInt } from './encode.ts';

/**
 * Payload layouts of the packets the replay exporters (Flashback, ReplayMod) have to understand, for Minecraft 26.3
 * (protocol 777). This is the only exporter file that depends on the protocol version: when an update changes a
 * layout, compare with ReplayStudio's `protocol/packets/*` (github.com/ReplayMod/ReplayStudio) and fix it here.
 *
 * Every decoder takes a payload (the packet id already removed) and throws on malformed input; callers count the
 * failure and carry on. Names are the Mojang names without `minecraft:`.
 */

/** Angle byte to degrees. */
function degrees(reader: PayloadReader): number {
  return (reader.i8() * 360) / 256;
}

function uuidText(bytes: Buffer): string {
  const hex = bytes.toString('hex');
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

/** Skips an LpVec3 (low-precision velocity vector). */
export function skipLpVec3(r: PayloadReader): void {
  const first = r.u8();
  if (first === 0) {
    return;
  }
  r.u8();
  r.bytes(4);
  if ((first & 0x04) !== 0) {
    r.varInt();
  }
}

export interface LoginFinished {
  uuid: string;
  name: string;
  /** The GameProfile bytes (uuid, name, properties) without the trailing session id; what Flashback stores */
  profile: Buffer;
}

export function decodeLoginFinished(payload: Buffer): LoginFinished {
  const r = new PayloadReader(payload);
  const uuid = uuidText(r.uuid());
  const name = r.string();
  for (let n = r.varInt(); n > 0; n--) {
    r.string();
    r.string();
    if (r.bool()) {
      r.string();
    }
  }
  return { uuid, name, profile: Buffer.from(payload.subarray(0, r.pos)) };
}

/** What `login_finished` carries, built from the player's identity (when a capture lacks the packet). */
export function encodeLoginFinished(uuid: string, name: string): Buffer {
  return Buffer.concat([uuidBytes(uuid), string(name), varInt(0), uuidBytes(uuid)]);
}

export interface PlayLogin {
  entityId: number;
  /** Index into the `dimension_type` registry sent during configuration */
  dimensionType: number;
  dimension: string;
  gameMode: number;
}

export function decodePlayLogin(payload: Buffer): PlayLogin {
  const r = new PayloadReader(payload);
  const entityId = r.i32();
  r.bool(); // hardcore
  for (let n = r.varInt(); n > 0; n--) {
    r.string();
  }
  r.varInt(); // max players
  r.varInt(); // view distance
  r.varInt(); // simulation distance
  r.bool(); // reduced debug info
  r.bool(); // respawn screen
  r.bool(); // limited crafting
  const dimensionType = r.varInt();
  const dimension = r.string();
  r.i64(); // hashed seed
  const gameMode = r.varInt();
  return { entityId, dimensionType, dimension, gameMode };
}

export interface Respawn {
  dimensionType: number;
  dimension: string;
  gameMode: number;
}

export function decodeRespawn(payload: Buffer): Respawn {
  const r = new PayloadReader(payload);
  const dimensionType = r.varInt();
  const dimension = r.string();
  r.i64(); // hashed seed
  const gameMode = r.varInt();
  return { dimensionType, dimension, gameMode };
}

export interface AddEntity {
  id: number;
  type: number;
  x: number;
  y: number;
  z: number;
  yaw: number;
  pitch: number;
  headYaw: number;
}

export function decodeAddEntity(payload: Buffer): AddEntity {
  const r = new PayloadReader(payload);
  const id = r.varInt();
  r.uuid();
  const type = r.varInt();
  const x = r.f64();
  const y = r.f64();
  const z = r.f64();
  skipLpVec3(r);
  const pitch = degrees(r);
  const yaw = degrees(r);
  const headYaw = degrees(r);
  return { id, type, x, y, z, yaw, pitch, headYaw };
}

export function encodeAddEntity(e: AddEntity & { uuid: string }): Buffer {
  return Buffer.concat([
    varInt(e.id),
    uuidBytes(e.uuid),
    varInt(e.type),
    f64(e.x),
    f64(e.y),
    f64(e.z),
    Buffer.from([0]), // no velocity
    angle(e.pitch),
    angle(e.yaw),
    angle(e.headYaw),
    varInt(0),
  ]);
}

export interface EntityDelta {
  id: number;
  dx: number;
  dy: number;
  dz: number;
  onGround: boolean;
  yaw?: number;
  pitch?: number;
}

/** `move_entity_pos` and `move_entity_pos_rot`: a linear delta, or the sum of the stepped deltas. */
export function decodeMovePos(payload: Buffer, withRotation: boolean): EntityDelta {
  const r = new PayloadReader(payload);
  const id = r.varInt();
  const props = r.varInt();
  const steps = props >>> 1;
  let dx = 0;
  let dy = 0;
  let dz = 0;
  if (steps === 0) {
    dx = r.i16();
    dy = r.i16();
    dz = r.i16();
  } else {
    for (let i = 0; i < steps; i++) {
      r.varInt(); // tick offset
      dx += r.i16();
      dy += r.i16();
      dz += r.i16();
    }
  }
  const delta: EntityDelta = { id, dx: dx / 4096, dy: dy / 4096, dz: dz / 4096, onGround: (props & 1) !== 0 };
  if (withRotation) {
    delta.yaw = degrees(r);
    delta.pitch = degrees(r);
  }
  return delta;
}

export function decodeMoveRot(payload: Buffer): { id: number; yaw: number; pitch: number; onGround: boolean } {
  const r = new PayloadReader(payload);
  const id = r.varInt();
  const onGround = r.bool();
  return { id, onGround, yaw: degrees(r), pitch: degrees(r) };
}

export function decodeRotateHead(payload: Buffer): { id: number; headYaw: number } {
  const r = new PayloadReader(payload);
  return { id: r.varInt(), headYaw: degrees(r) };
}

export interface AbsolutePos {
  id: number;
  x: number;
  y: number;
  z: number;
  yaw: number;
  pitch: number;
  onGround: boolean;
}

/** `entity_position_sync`: one absolute position, or a path of them of which the last counts. */
export function decodePositionSync(payload: Buffer): AbsolutePos {
  const r = new PayloadReader(payload);
  const id = r.varInt();
  const pathType = r.varInt();
  let x = 0;
  let y = 0;
  let z = 0;
  if (pathType === 0) {
    x = r.f64();
    y = r.f64();
    z = r.f64();
  } else {
    const count = r.varInt();
    if (count === 0) {
      throw new Error('empty position path');
    }
    for (let i = 0; i < count; i++) {
      x = r.f64();
      y = r.f64();
      z = r.f64();
      r.varInt(); // tick offset
    }
  }
  const yaw = r.f32();
  const pitch = r.f32();
  return { id, x, y, z, yaw, pitch, onGround: r.bool() };
}

export function encodePositionSync(p: AbsolutePos): Buffer {
  return Buffer.concat([varInt(p.id), varInt(0), f64(p.x), f64(p.y), f64(p.z), f32(p.yaw), f32(p.pitch), bool(p.onGround)]);
}

export function encodeRotateHead(id: number, headYaw: number): Buffer {
  return Buffer.concat([varInt(id), angle(headYaw)]);
}

/** `teleport_entity`: absolute or relative (flags) position and rotation. */
export function decodeTeleport(payload: Buffer): AbsolutePos & { flags: number } {
  const r = new PayloadReader(payload);
  const id = r.varInt();
  const x = r.f64();
  const y = r.f64();
  const z = r.f64();
  r.bytes(24); // velocity
  const yaw = r.f32();
  const pitch = r.f32();
  const flags = r.i32();
  return { id, x, y, z, yaw, pitch, flags, onGround: r.bool() };
}

/** `player_position`: where the server puts the recording player. */
export function decodePlayerPosition(payload: Buffer): { x: number; y: number; z: number; yaw: number; pitch: number; flags: number } {
  const r = new PayloadReader(payload);
  r.varInt(); // teleport id
  const x = r.f64();
  const y = r.f64();
  const z = r.f64();
  r.bytes(24); // velocity
  const yaw = r.f32();
  const pitch = r.f32();
  return { x, y, z, yaw, pitch, flags: r.i32() };
}

export function decodeRemoveEntities(payload: Buffer): number[] {
  const r = new PayloadReader(payload);
  const ids: number[] = [];
  for (let n = r.varInt(); n > 0; n--) {
    ids.push(r.varInt());
  }
  return ids;
}

const REL_X = 0x01;
const REL_Y = 0x02;
const REL_Z = 0x04;
const REL_YAW = 0x08;
const REL_PITCH = 0x10;

export interface EntityPos {
  x: number;
  y: number;
  z: number;
  yaw: number;
  pitch: number;
  headYaw: number;
  onGround: boolean;
}

/** Applies a position that is partly relative (teleport flags) to the current one. */
function resolve(
  current: EntityPos,
  next: { x: number; y: number; z: number; yaw: number; pitch: number },
  flags: number,
): Pick<EntityPos, 'x' | 'y' | 'z' | 'yaw' | 'pitch'> {
  return {
    x: (flags & REL_X ? current.x : 0) + next.x,
    y: (flags & REL_Y ? current.y : 0) + next.y,
    z: (flags & REL_Z ? current.z : 0) + next.z,
    yaw: (flags & REL_YAW ? current.yaw : 0) + next.yaw,
    pitch: (flags & REL_PITCH ? current.pitch : 0) + next.pitch,
  };
}

/**
 * The absolute positions of the entities the client knows, from the spawn, move, sync and teleport packets. The
 * recording player is not in here (see LocalPlayer); its packets are sent as spawn/sync by the exporters.
 */
export class EntityTracker {
  readonly entities = new Map<number, EntityPos>();
  readonly dirty = new Set<number>();

  /** Applies a clientbound play packet; returns false when it is not a spawn, move or remove packet. */
  apply(name: string, payload: Buffer): boolean {
    switch (name) {
      case 'add_entity': {
        const e = decodeAddEntity(payload);
        this.entities.set(e.id, { x: e.x, y: e.y, z: e.z, yaw: e.yaw, pitch: e.pitch, headYaw: e.headYaw, onGround: true });
        return true;
      }
      case 'move_entity_pos':
      case 'move_entity_pos_rot': {
        const d = decodeMovePos(payload, name === 'move_entity_pos_rot');
        const at = this.entities.get(d.id);
        if (at) {
          at.x += d.dx;
          at.y += d.dy;
          at.z += d.dz;
          at.onGround = d.onGround;
          if (d.yaw !== undefined && d.pitch !== undefined) {
            at.yaw = d.yaw;
            at.pitch = d.pitch;
          }
          this.dirty.add(d.id);
        }
        return true;
      }
      case 'move_entity_rot': {
        const d = decodeMoveRot(payload);
        const at = this.entities.get(d.id);
        if (at) {
          at.yaw = d.yaw;
          at.pitch = d.pitch;
          at.onGround = d.onGround;
          this.dirty.add(d.id);
        }
        return true;
      }
      case 'rotate_head': {
        const d = decodeRotateHead(payload);
        const at = this.entities.get(d.id);
        if (at) {
          at.headYaw = d.headYaw;
          this.dirty.add(d.id);
        }
        return true;
      }
      case 'entity_position_sync': {
        const d = decodePositionSync(payload);
        const at = this.entities.get(d.id);
        if (at) {
          Object.assign(at, { x: d.x, y: d.y, z: d.z, yaw: d.yaw, pitch: d.pitch, onGround: d.onGround });
          this.dirty.add(d.id);
        }
        return true;
      }
      case 'teleport_entity': {
        const d = decodeTeleport(payload);
        const at = this.entities.get(d.id);
        if (at) {
          Object.assign(at, resolve(at, d, d.flags), { onGround: d.onGround });
          this.dirty.add(d.id);
        }
        return true;
      }
      case 'remove_entities':
        for (const id of decodeRemoveEntities(payload)) {
          this.entities.delete(id);
          this.dirty.delete(id);
        }
        return true;
      default:
        return false;
    }
  }

  clear(): void {
    this.entities.clear();
    this.dirty.clear();
  }

  /** The entities that changed since the last call. */
  takeDirty(): [number, EntityPos][] {
    const out: [number, EntityPos][] = [];
    for (const id of this.dirty) {
      const at = this.entities.get(id);
      if (at) {
        out.push([id, { ...at }]);
      }
    }
    this.dirty.clear();
    return out;
  }
}

/** The recording player, followed from its serverbound movement packets and the server's `player_position`. */
export class LocalPlayer {
  entityId = -1;
  uuid: string | undefined;
  name: string | undefined;
  profile: Buffer | undefined;
  gameMode = 0;
  dimension = 'minecraft:overworld';
  /** Undefined until the first absolute position is known */
  pos: EntityPos | undefined;
  /** Moved since the exporter last wrote it */
  dirty = false;
  private yaw = 0;
  private pitch = 0;

  /** Starts over for a new login or respawn, keeping who the player is. */
  reset(): void {
    this.pos = undefined;
    this.dirty = false;
    this.yaw = 0;
    this.pitch = 0;
  }

  applyServerbound(name: string, payload: Buffer): void {
    const r = new PayloadReader(payload);
    switch (name) {
      case 'move_player_pos': {
        const x = r.f64();
        const y = r.f64();
        const z = r.f64();
        this.set({ x, y, z }, undefined, (r.u8() & 1) !== 0);
        break;
      }
      case 'move_player_pos_rot': {
        const x = r.f64();
        const y = r.f64();
        const z = r.f64();
        const yaw = r.f32();
        const pitch = r.f32();
        this.set({ x, y, z }, { yaw, pitch }, (r.u8() & 1) !== 0);
        break;
      }
      case 'move_player_rot': {
        const yaw = r.f32();
        const pitch = r.f32();
        this.set(undefined, { yaw, pitch }, (r.u8() & 1) !== 0);
        break;
      }
      case 'move_player_status_only':
        if (this.pos) {
          this.pos.onGround = (r.u8() & 1) !== 0;
        }
        break;
    }
  }

  applyPlayerPosition(payload: Buffer): void {
    const p = decodePlayerPosition(payload);
    const base: EntityPos = this.pos ?? { x: 0, y: 0, z: 0, yaw: this.yaw, pitch: this.pitch, headYaw: this.yaw, onGround: false };
    const next = resolve(base, p, p.flags);
    this.set({ x: next.x, y: next.y, z: next.z }, { yaw: next.yaw, pitch: next.pitch }, base.onGround);
  }

  private set(position: { x: number; y: number; z: number } | undefined, look: { yaw: number; pitch: number } | undefined, onGround: boolean): void {
    if (look) {
      this.yaw = look.yaw;
      this.pitch = look.pitch;
    }
    if (position) {
      this.pos = { ...position, yaw: this.yaw, pitch: this.pitch, headYaw: this.yaw, onGround };
      this.dirty = true;
    } else if (this.pos) {
      Object.assign(this.pos, { yaw: this.yaw, pitch: this.pitch, headYaw: this.yaw, onGround });
      this.dirty = true;
    }
  }
}
