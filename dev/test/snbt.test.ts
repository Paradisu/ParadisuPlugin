import assert from 'node:assert/strict';
import { test } from 'node:test';
import { compound, int, readNbt, str, writeNbt } from '../src/mc/nbt.ts';
import type { Tag } from '../src/mc/nbt.ts';
import { parse, stringify } from '../src/mc/snbt.ts';

const sample: Tag = compound({
  b: { type: 'byte', value: -3 },
  s: { type: 'short', value: 300 },
  i: int(70000),
  l: { type: 'long', value: 9007199254740993n },
  f: { type: 'float', value: 1.5 },
  d: { type: 'double', value: 2 },
  text: str('he said "hi" \\ ok'),
  'minecraft:odd key': int(1),
  bytes: { type: 'byteArray', value: Int8Array.from([1, -2]) },
  ints: { type: 'intArray', value: Int32Array.from([1, 2, 3]) },
  longs: { type: 'longArray', value: BigInt64Array.from([5n, -6n]) },
  list: { type: 'list', elementType: 'int', value: [int(1), int(2)] },
  nested: { type: 'list', elementType: 'compound', value: [compound({ a: int(1) }), compound()] },
  empty: { type: 'list', elementType: 'end', value: [] },
});

test('stringify then parse round-trips every tag type', () => {
  for (const pretty of [false, true]) {
    assert.deepEqual(parse(stringify(sample, { pretty })), sample);
  }
});

test('binary round-trip including modified UTF-8', () => {
  const tag = compound({ nul: str('a\u0000b'), emoji: str('x\u{1F600}y'), accent: str('é') });
  const back = readNbt(writeNbt(tag, 'root'));
  assert.equal(back.name, 'root');
  assert.deepEqual(back.tag, tag);
});

test('parses /give style components', () => {
  const tag = parse('{id:"minecraft:diamond_sword",count:1,components:{"minecraft:enchantments":{"minecraft:sharpness":5}}}');
  assert.equal(tag.type, 'compound');
  assert.equal(stringify(tag), '{id:"minecraft:diamond_sword",count:1,components:{"minecraft:enchantments":{"minecraft:sharpness":5}}}');
});

test('booleans, suffixes and defaults', () => {
  assert.deepEqual(parse('true'), { type: 'byte', value: 1 });
  assert.deepEqual(parse('1.5'), { type: 'double', value: 1.5 });
  assert.deepEqual(parse('12L'), { type: 'long', value: 12n });
  assert.deepEqual(parse('7'), { type: 'int', value: 7 });
});

test('rejects mixed lists, bad ranges and trailing input', () => {
  assert.throws(() => parse('[1,"a"]'), /share a type/);
  assert.throws(() => parse('300b'), /out of range/);
  assert.throws(() => parse('{a:1} x'), /trailing/);
});
