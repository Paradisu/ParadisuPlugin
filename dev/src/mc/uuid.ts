import { createHash } from 'node:crypto';
import { DevError, USER_AGENT } from '../util.ts';

const DASHED = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/;

function dash(hex: string): string {
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

/** Accepts dashed, undashed and `[I;a,b,c,d]` forms; returns the dashed lowercase form. */
export function parseUuid(input: string): string {
  const text = input.trim().toLowerCase();
  if (DASHED.test(text)) return text;
  if (/^[0-9a-f]{32}$/.test(text)) return dash(text);
  const ints = /^\[i;\s*(-?\d+)\s*,\s*(-?\d+)\s*,\s*(-?\d+)\s*,\s*(-?\d+)\s*\]$/.exec(text);
  if (ints) {
    const hex = ints
      .slice(1, 5)
      .map((n) => (Number(n) >>> 0).toString(16).padStart(8, '0'))
      .join('');
    return dash(hex);
  }
  throw new DevError(`"${input}" is not a UUID (dashed, undashed or [I;a,b,c,d])`);
}

export function looksLikeUuid(input: string): boolean {
  try {
    parseUuid(input);
    return true;
  } catch {
    return false;
  }
}

export function toIntArray(uuid: string): [number, number, number, number] {
  const hex = parseUuid(uuid).replaceAll('-', '');
  return [0, 8, 16, 24].map((i) => Number.parseInt(hex.slice(i, i + 8), 16) | 0) as [number, number, number, number];
}

export function toMostLeast(uuid: string): { most: bigint; least: bigint } {
  const hex = parseUuid(uuid).replaceAll('-', '');
  return { most: BigInt.asIntN(64, BigInt(`0x${hex.slice(0, 16)}`)), least: BigInt.asIntN(64, BigInt(`0x${hex.slice(16)}`)) };
}

export function uuidVersion(uuid: string): number {
  return Number.parseInt(parseUuid(uuid)[14]!, 16);
}

/** UUID the server assigns in offline mode: a version 3 UUID of "OfflinePlayer:<name>". */
export function offlineUuid(name: string): string {
  const bytes = createHash('md5').update(`OfflinePlayer:${name}`, 'utf8').digest();
  bytes[6] = (bytes[6]! & 0x0f) | 0x30;
  bytes[8] = (bytes[8]! & 0x3f) | 0x80;
  return dash(bytes.toString('hex'));
}

export interface MojangProfile {
  id: string;
  name: string;
}

async function mojang(url: string): Promise<MojangProfile | undefined> {
  const response = await fetch(url, { headers: { 'User-Agent': USER_AGENT } });
  if (response.status === 204 || response.status === 404) return undefined;
  if (!response.ok) throw new DevError(`GET ${url} failed: ${response.status} ${response.statusText}`);
  const body = (await response.json()) as { id: string; name: string };
  return { id: parseUuid(body.id), name: body.name };
}

export function mojangByName(name: string): Promise<MojangProfile | undefined> {
  return mojang(`https://api.mojang.com/users/profiles/minecraft/${encodeURIComponent(name)}`);
}

export function mojangByUuid(uuid: string): Promise<MojangProfile | undefined> {
  return mojang(`https://sessionserver.mojang.com/session/minecraft/profile/${parseUuid(uuid).replaceAll('-', '')}`);
}
