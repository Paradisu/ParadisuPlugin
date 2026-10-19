import { gzipSync } from 'node:zlib';
import { DevError, fail } from '../util.ts';
import { type Tag, compound, int, readCompressedNbt, writeNbt } from './nbt.ts';

/**
 * Paper item formats (ItemStack#serializeAsBytes / #serializeItemsAsBytes in paper-api):
 *  - one item: gzip of a named-root ("") NBT compound {id, count, components?, DataVersion}
 *  - item array: byte version (1), int32 count, then per item int32 length (0 = empty) + that many item bytes
 */
const ARRAY_VERSION = 1;

/** Slot counts of PlayerInventory#getContents (36 storage, 4 armor, 1 offhand), armor and the ender chest. */
export const INVENTORY_SLOTS = 41;
export const ARMOR_SLOTS = 4;
export const ENDER_CHEST_SLOTS = 27;
/** Armor order in both getArmorContents() and slots 36-39 of getContents() */
export const ARMOR_ORDER = ['feet', 'legs', 'chest', 'head'] as const;
export type ArmorPiece = (typeof ARMOR_ORDER)[number];
export const OFFHAND_SLOT = 40;

export function decodeItem(buf: Buffer): Tag {
  return readCompressedNbt(buf).tag;
}

export function encodeItem(item: Tag, dataVersion: number): Buffer {
  if (item.type !== 'compound') {
    fail('An item must be a compound like {id:"minecraft:stone",count:1}');
  }
  if (!item.value.id || item.value.id.type !== 'string') {
    fail('An item needs a string "id"');
  }
  const withVersion = item.value.DataVersion ? item : compound({ ...item.value, DataVersion: int(dataVersion) });
  return gzipSync(writeNbt(withVersion, ''));
}

export function looksLikeItemArray(buf: Buffer): boolean {
  return buf.length >= 5 && buf[0] === ARRAY_VERSION;
}

/** Empty slots come back as null. */
export function decodeItemArray(buf: Buffer): (Tag | null)[] {
  if (buf[0] !== ARRAY_VERSION) {
    throw new DevError(`Not an item array (version byte ${buf[0]}, expected ${ARRAY_VERSION})`);
  }
  const count = buf.readInt32BE(1);
  const items: (Tag | null)[] = [];
  let offset = 5;
  for (let i = 0; i < count; i++) {
    const length = buf.readInt32BE(offset);
    offset += 4;
    items.push(length === 0 ? null : decodeItem(buf.subarray(offset, offset + length)));
    offset += length;
  }
  return items;
}

export function encodeItemArray(items: (Tag | null | undefined)[], dataVersion: number): Buffer {
  const parts: Buffer[] = [Buffer.from([ARRAY_VERSION]), int32(items.length)];
  for (const item of items) {
    if (!item) {
      parts.push(int32(0));
      continue;
    }
    const bytes = encodeItem(item, dataVersion);
    parts.push(int32(bytes.length), bytes);
  }
  return Buffer.concat(parts);
}

function int32(value: number): Buffer {
  const b = Buffer.alloc(4);
  b.writeInt32BE(value);
  return b;
}

// ---------------------------------------------------------------------------------------------------------------
// Presentation
// ---------------------------------------------------------------------------------------------------------------

export interface ItemSummary {
  id: string;
  count: number;
  name?: string;
  enchantments?: string;
}

function componentText(tag: Tag | undefined): string | undefined {
  if (!tag) return undefined;
  if (tag.type === 'string') {
    try {
      const json = JSON.parse(tag.value) as unknown;
      if (typeof json === 'string') return json;
      if (json && typeof json === 'object' && 'text' in json) return String((json as { text: unknown }).text);
    } catch {
      // plain text
    }
    return tag.value;
  }
  if (tag.type === 'compound' && tag.value.text?.type === 'string') return tag.value.text.value;
  return undefined;
}

export function summarize(item: Tag): ItemSummary {
  if (item.type !== 'compound') return { id: '?', count: 0 };
  const id = item.value.id?.type === 'string' ? item.value.id.value : '?';
  // 1.20.5+ uses "count", older data used "Count" (a byte)
  const countTag = item.value.count ?? item.value.Count;
  const count = countTag && 'value' in countTag && typeof countTag.value === 'number' ? countTag.value : 1;
  const components = item.value.components?.type === 'compound' ? item.value.components.value : {};
  const summary: ItemSummary = { id: id.replace(/^minecraft:/, ''), count };
  const name = componentText(components['minecraft:custom_name']);
  if (name) summary.name = name;
  const enchants = components['minecraft:enchantments'];
  if (enchants?.type === 'compound') {
    // Before 1.21.5 the enchantments sit in a "levels" compound
    const levels = enchants.value.levels?.type === 'compound' ? enchants.value.levels.value : enchants.value;
    const text = Object.entries(levels)
      .map(([key, level]) => `${key.replace(/^minecraft:/, '')} ${'value' in level ? String(level.value) : ''}`.trim())
      .join(', ');
    if (text) summary.enchantments = text;
  }
  return summary;
}

/** Slot label for the player inventory layout. */
export function slotLabel(slot: number): string {
  if (slot < 9) return `hotbar ${slot}`;
  if (slot < 36) return `main ${slot}`;
  if (slot < 40) return `armor ${ARMOR_ORDER[slot - 36]}`;
  return 'offhand';
}
