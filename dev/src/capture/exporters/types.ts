import { join } from 'node:path';
import type { PacketRecord } from '../format.ts';
import type { ProtocolTable } from '../protocol.ts';
import type { CaptureMeta, ConnMeta } from '../store.ts';

export interface ExportContext {
  capture: CaptureMeta;
  /** One instance per call; the command loops over a capture's instances */
  instance: string;
  protocol: ProtocolTable;
  connections: Map<number, ConnMeta>;
  /** Epoch milliseconds the capture started at */
  startEpochMs: number;
  /** Packets that passed the common filter flags; can be called again to re-read the dump */
  packets(): AsyncIterable<PacketRecord>;
  /** Who receives a packet of this connection (see receivers.ts) */
  receiverOf(conn: ConnMeta, dir: number): string;
  outDir: string;
  /** Exporter-specific `--opt key=value` flags */
  options: Record<string, string | boolean | undefined>;
}

/**
 * An export format. Adding one means a file in this folder plus an entry in index.ts; nothing else in the CLI
 * needs to know about it.
 */
export interface Exporter {
  name: string;
  summary: string;
  /** Writes the export and returns a line per file written, for the command to print */
  export(ctx: ExportContext): Promise<string[]>;
}

export function safeName(text: string): string {
  return text.replace(/[^A-Za-z0-9_.-]+/g, '_');
}

/** `<capture>_<instance>_<leg>_<player>.<ext>` in outDir, plus a label for the result line; shared by the per-connection exporters. */
export function connectionFile(ctx: ExportContext, connId: number, ext: string): { path: string; label: string } {
  const conn = ctx.connections.get(connId);
  const who = conn?.name ?? `conn${connId}`;
  const leg = conn?.leg === 1 ? 'backend' : 'frontend';
  return { path: join(ctx.outDir, `${safeName(`${ctx.capture.id}_${ctx.instance}_${leg}_${who}`)}.${ext}`), label: `${leg} ${who}` };
}
