import assert from 'node:assert/strict';
import { test } from 'node:test';
import { AGENT_PLUGINS, hotswapFlags } from '../src/hotswap.ts';

function disabled(flags: string[]): string[] {
  const flag = flags.find((candidate) => candidate.startsWith('-Dhotswapagent.disablePlugin='));
  return flag ? flag.slice(flag.indexOf('=') + 1).split(',') : [];
}

test('keeps the always-on plugins and the ones the project libraries need', () => {
  const off = disabled(hotswapFlags('paper', ['org.hibernate.orm', 'net.kyori'], []));
  assert.ok(!off.includes('HibernateJakarta') && !off.includes('AnonymousClassPatch') && !off.includes('JdkPlugin'));
  assert.ok(off.includes('Hibernate') && off.includes('Spring') && off.includes('Log4j2'));
});

test('without a matching library only the always-on plugins stay', () => {
  assert.equal(AGENT_PLUGINS.length - disabled(hotswapFlags('velocity', [], [])).length, 2);
});

test('extra plugins are kept case-insensitively, and limbo gets no agent', () => {
  assert.ok(!disabled(hotswapFlags('paper', [], ['log4j2'])).includes('Log4j2'));
  assert.deepEqual(hotswapFlags('limbo', ['org.hibernate.orm'], []), []);
});
