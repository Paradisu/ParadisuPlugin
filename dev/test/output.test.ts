import assert from 'node:assert/strict';
import { test } from 'node:test';
import { columns, formatBytes, formatDuration, keyValue, pad, streamRow, table, width } from '../src/output.ts';

const GREEN = '\x1b[32mok\x1b[0m';

test('width ignores escapes and counts code points', () => {
  assert.equal(width(GREEN), 2);
  assert.equal(width('→x'), 2);
});

test('table aligns columns, right-aligns numbers and trims trailing space', () => {
  const out = table(['name', { header: 'n', align: 'right' }, 'state'], [
    ['a', '5', GREEN],
    ['longer', '120', 'x'],
  ]).split('\n');
  assert.equal(out[1], 'a         5  \x1b[32mok\x1b[0m');
  assert.equal(out[2], 'longer  120  x');
  assert.ok(out.every((line) => line === line.trimEnd()));
  assert.ok(out[0]!.includes('NAME'));
});

test('table indent and columns', () => {
  assert.equal(table(['a'], [['b']], { indent: 2 }).split('\n')[1], '  b');
  assert.deepEqual(columns([['a', 'x'], ['bb', 'y']]), ['a   x', 'bb  y']);
});

test('keyValue pads labels', () => {
  assert.equal(keyValue([['a', '1'], ['abc', '2']]), 'a    1\nabc  2');
});

test('streamRow grows with the widest cell and honours min', () => {
  const row = streamRow([{ min: 4 }, { align: 'right' }]);
  assert.equal(row(['a', '1']), 'a     1');
  assert.equal(row(['a', '100']), 'a     100');
  assert.equal(pad('x', 3, 'right'), '  x');
});

test('formatBytes and formatDuration boundaries', () => {
  assert.equal(formatBytes(1023), '1023 B');
  assert.equal(formatBytes(1024), '1.0 KiB');
  assert.equal(formatBytes(1024 ** 3), '1.00 GiB');
  assert.equal(formatDuration(45_000), '45s');
  assert.equal(formatDuration(12 * 60_000), '12m');
  assert.equal(formatDuration(65 * 60_000), '1h 05m');
});
