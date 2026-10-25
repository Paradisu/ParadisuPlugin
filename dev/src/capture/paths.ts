import { join } from 'node:path';
import { socketPath } from '../supervisor.ts';
import type { Instance } from '../templates.ts';
import { DEV } from '../util.ts';

/** Captures live here (gitignored): one folder per capture. */
export const CAPTURES = join(DEV, 'captures');

/** The capture plugin's control socket for an instance (passed to the JVM as -Dparadisu.capture.socket). */
export function controlSocket(instance: Instance): string {
  return socketPath(instance, '-capture');
}
