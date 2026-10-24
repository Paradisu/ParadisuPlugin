import assert from 'node:assert/strict';
import { test } from 'node:test';
import { backoffMs, parseTunnelUrls, renewDelay } from '../src/share/address.ts';

test('parses a tcp address', () => {
  const address = parseTunnelUrls('tcp', ['tcp://abc.a.free.pinggy.link:41234']);
  assert.deepEqual(address, { display: 'abc.a.free.pinggy.link:41234', host: 'abc.a.free.pinggy.link', port: 41234 });
});

test('prefers https for http tunnels', () => {
  const address = parseTunnelUrls('http', ['http://x.pinggy.link', 'https://x.pinggy.link']);
  assert.equal(address.url, 'https://x.pinggy.link');
  assert.equal(address.port, 443);
});

test('rejects urls of the wrong kind', () => {
  assert.throws(() => parseTunnelUrls('tcp', ['https://x.pinggy.link']), /no usable tcp address/);
  assert.throws(() => parseTunnelUrls('http', []), /no usable http address/);
});

test('renew delay', () => {
  assert.equal(renewDelay(undefined, 0, 30_000), undefined);
  assert.equal(renewDelay(100_000, 0, 30_000), 70_000);
  assert.equal(renewDelay(10_000, 0, 30_000), 0);
});

test('backoff doubles up to five minutes', () => {
  assert.equal(backoffMs(0), 5000);
  assert.equal(backoffMs(2), 20_000);
  assert.equal(backoffMs(20), 300_000);
});
