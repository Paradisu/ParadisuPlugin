import { DevError } from '../util.ts';
import { type PacketKey, ProtocolTable, STATES } from './protocol.ts';
import type { ConnMeta } from './store.ts';

/** parseArgs option definitions shared by `watch`, `show` and `export`. */
export const FILTER_OPTIONS = {
  type: { type: 'string', multiple: true },
  dir: { type: 'string' },
  to: { type: 'string', multiple: true },
  player: { type: 'string', multiple: true },
  state: { type: 'string' },
  conn: { type: 'string', multiple: true },
} as const;

export interface FilterValues {
  type?: string[];
  dir?: string;
  to?: string[];
  player?: string[];
  state?: string;
  conn?: string[];
}

export interface PacketFilter {
  types: string[];
  /** 0 serverbound, 1 clientbound */
  dir?: number;
  to: string[];
  players: string[];
  state?: number;
  conns: number[];
}

function split(values: string[] | undefined): string[] {
  return (values ?? []).flatMap((value) => value.split(',')).map((value) => value.trim()).filter(Boolean);
}

export function parseFilter(values: FilterValues): PacketFilter {
  let dir: number | undefined;
  if (values.dir !== undefined) {
    const text = values.dir.toLowerCase();
    if (text === 'sb' || text === 'serverbound') dir = 0;
    else if (text === 'cb' || text === 'clientbound') dir = 1;
    else throw new DevError(`--dir must be sb|cb|serverbound|clientbound, not "${values.dir}"`);
  }
  let state: number | undefined;
  if (values.state !== undefined) {
    state = STATES.indexOf(values.state.toLowerCase() as (typeof STATES)[number]);
    if (state === -1) throw new DevError(`--state must be one of ${STATES.join(', ')}`);
  }
  return {
    types: split(values.type),
    dir,
    to: split(values.to),
    players: split(values.player).map((p) => p.toLowerCase().replaceAll('-', '')),
    state,
    conns: split(values.conn).map(Number),
  };
}

export interface PacketLike {
  conn: number;
  state: number;
  dir: number;
  id: number;
}

/** Turns a filter into a predicate; type patterns that match nothing are an error (usually a typo). */
export function compileFilter(
  filter: PacketFilter,
  table: ProtocolTable,
  conns: Map<number, ConnMeta>,
  receiver: (conn: ConnMeta, dir: number) => string,
): (packet: PacketLike) => boolean {
  let keys: Set<string> | undefined;
  if (filter.types.length > 0) {
    keys = new Set();
    for (const pattern of filter.types) {
      const found = table.keys(pattern);
      if (found.length === 0) {
        throw new DevError(`No packet type matches "${pattern}" (Minecraft ${table.minecraftVersion})`);
      }
      for (const key of found) keys.add(`${key.state}/${key.dir}/${key.id}`);
    }
  }
  const to = new Set(filter.to);
  const players = new Set(filter.players);
  const wantConns = new Set(filter.conns);
  return (packet) => {
    if (filter.dir !== undefined && packet.dir !== filter.dir) return false;
    if (filter.state !== undefined && packet.state !== filter.state) return false;
    if (wantConns.size > 0 && !wantConns.has(packet.conn)) return false;
    if (keys && !keys.has(`${packet.state}/${packet.dir}/${packet.id}`)) return false;
    const conn = conns.get(packet.conn);
    if (to.size > 0 && (!conn || !to.has(receiver(conn, packet.dir)))) return false;
    if (players.size > 0) {
      const name = conn?.name?.toLowerCase();
      const uuid = conn?.uuid?.replaceAll('-', '').toLowerCase();
      if (!((name && players.has(name)) || (uuid && players.has(uuid)))) return false;
    }
    return true;
  };
}

/** The part of a filter the plugin can apply, so a live watch sends less data. The CLI still applies the full filter. */
export function toServerFilter(filter: PacketFilter, table: ProtocolTable): { dirs?: number[]; keys?: PacketKey[]; conns?: number[] } {
  const server: { dirs?: number[]; keys?: PacketKey[]; conns?: number[] } = {};
  if (filter.dir !== undefined) server.dirs = [filter.dir];
  if (filter.conns.length > 0) server.conns = filter.conns;
  if (filter.types.length > 0) {
    server.keys = filter.types.flatMap((pattern) => table.keys(pattern));
  }
  return server;
}
