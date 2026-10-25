import { createWriteStream } from 'node:fs';
import { mkdir } from 'node:fs/promises';
import { once } from 'node:events';
import { join } from 'node:path';
import { type ExportContext, type Exporter, safeName } from './types.ts';

/** One JSON object per line (jq friendly), with the frame as hex. */
export const jsonl: Exporter = {
  name: 'jsonl',
  summary: 'JSON lines, one packet per line, for jq and scripts',
  async export(ctx: ExportContext): Promise<string[]> {
    await mkdir(ctx.outDir, { recursive: true });
    const path = join(ctx.outDir, `${safeName(`${ctx.capture.id}_${ctx.instance}`)}.jsonl`);
    const stream = createWriteStream(path);
    let count = 0;
    for await (const packet of ctx.packets()) {
      const conn = ctx.connections.get(packet.conn);
      const line = JSON.stringify({
        seq: packet.seq,
        conn: packet.conn,
        player: conn?.name ?? null,
        leg: conn?.leg === 1 ? 'backend' : 'frontend',
        receiver: conn ? ctx.receiverOf(conn, packet.dir) : null,
        us: packet.us,
        dir: packet.dir === 0 ? 'serverbound' : 'clientbound',
        state: packet.state,
        name: ctx.protocol.name(packet.state, packet.dir, packet.id),
        id: packet.id,
        size: packet.frame.length,
        hex: packet.frame.toString('hex'),
      });
      count++;
      if (!stream.write(`${line}\n`)) {
        await once(stream, 'drain');
      }
    }
    stream.end();
    await once(stream, 'finish');
    return [`${path}  (${count} packets)`];
  },
};
