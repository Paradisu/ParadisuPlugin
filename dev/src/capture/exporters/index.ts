import { fail } from '../../util.ts';
import { jsonl } from './jsonl.ts';
import { sniffcraft } from './sniffcraft.ts';
import type { Exporter } from './types.ts';

/** Registered export formats; add one here after writing its file. */
export const EXPORTERS: Record<string, Exporter> = {
  [sniffcraft.name]: sniffcraft,
  [jsonl.name]: jsonl,
};

export function exporter(name: string): Exporter {
  return EXPORTERS[name] ?? fail(`Unknown export format "${name}". Available: ${Object.keys(EXPORTERS).join(', ')}`);
}
