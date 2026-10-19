import assert from 'node:assert/strict';
import { test } from 'node:test';
import { gunzipSync } from 'node:zlib';
import { decodeItem, decodeItemArray, encodeItem, encodeItemArray, summarize } from '../src/mc/items.ts';
import { readNbt } from '../src/mc/nbt.ts';
import { parse } from '../src/mc/snbt.ts';
import { looksLikeUuid, offlineUuid, parseUuid, toIntArray, toMostLeast, uuidVersion } from '../src/mc/uuid.ts';

const sword = parse('{id:"minecraft:diamond_sword",count:1,components:{"minecraft:custom_name":\'"Excalibur"\',"minecraft:enchantments":{"minecraft:sharpness":5}}}');

test('single item is gzip of a named-root compound with DataVersion', () => {
  const bytes = encodeItem(sword, 5023);
  assert.equal(bytes[0], 0x1f);
  const root = readNbt(gunzipSync(bytes));
  assert.equal(root.name, '');
  assert.equal(root.tag.type, 'compound');
  assert.deepEqual((root.tag as { value: Record<string, unknown> }).value.DataVersion, { type: 'int', value: 5023 });
  assert.deepEqual(summarize(decodeItem(bytes)), {
    id: 'diamond_sword',
    count: 1,
    name: 'Excalibur',
    enchantments: 'sharpness 5',
  });
});

test('item array keeps empty slots and order', () => {
  const stone = parse('{id:"minecraft:stone",count:64}');
  const bytes = encodeItemArray([stone, null, sword], 5023);
  assert.equal(bytes[0], 1);
  assert.equal(bytes.readInt32BE(1), 3);
  const items = decodeItemArray(bytes);
  assert.equal(items.length, 3);
  assert.equal(items[1], null);
  assert.equal(summarize(items[0]!).count, 64);
  assert.equal(summarize(items[2]!).id, 'diamond_sword');
});

test('items without an id are rejected', () => {
  assert.throws(() => encodeItem(parse('{count:1}'), 1), /id/);
});

test('uuid forms and offline uuids', () => {
  const u = '069a79f4-44e9-4726-a5be-fca90e38aaf5';
  assert.equal(parseUuid(u.replaceAll('-', '').toUpperCase()), u);
  assert.equal(parseUuid(`[I;${toIntArray(u).join(',')}]`), u);
  const { most, least } = toMostLeast(u);
  assert.equal(most, 0x069a79f444e94726n);
  assert.ok(least < 0n);
  assert.equal(looksLikeUuid('not-a-uuid'), false);
  // Offline UUIDs are version 3; this value was computed with Java's UUID.nameUUIDFromBytes("OfflinePlayer:Notch")
  const offline = offlineUuid('Notch');
  assert.equal(uuidVersion(offline), 3);
  assert.equal(offline, 'b50ad385-829d-3141-a216-7e7d7539ba7f');
  assert.notEqual(offline, offlineUuid('notch'));
});
