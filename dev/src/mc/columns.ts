/**
 * Database columns that hold Minecraft binary data, with how to decode them. Used by `dev mc` and the fixture
 * loader. Add an entry here whenever a new item/NBT blob column is created.
 */
export type Codec = 'item-array' | 'item';

export interface McColumn {
  table: string;
  column: string;
  codec: Codec;
  /** Expected slot count for item arrays */
  slots?: number;
}

export const MC_COLUMNS: readonly McColumn[] = [
  { table: 'item', column: 'nbt', codec: 'item' },
  { table: 'player_inventories', column: 'inventory', codec: 'item-array', slots: 41 },
  { table: 'player_inventories', column: 'armor', codec: 'item-array', slots: 4 },
  { table: 'player_inventories', column: 'off_hand', codec: 'item' },
  { table: 'player_inventories', column: 'ender_chest', codec: 'item-array', slots: 27 },
];
