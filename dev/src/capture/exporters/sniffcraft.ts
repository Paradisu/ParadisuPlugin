import { createWriteStream } from 'node:fs';
import { mkdir } from 'node:fs/promises';
import { once } from 'node:events';
import { join } from 'node:path';
import type { WriteStream } from 'node:fs';
import { deflateSync } from 'node:zlib';
import { varInt, varLong } from './encode.ts';
import { type ExportContext, type Exporter, safeName } from './types.ts';

interface Output {
  stream: WriteStream;
  path: string;
  firstUs: number;
  packets: number;
  label: string;
}

/**
 * SniffCraft binary captures (.scbin), as written by `sniffcraft --headless` with LogToBinFile. Format (from
 * SniffCraft's Logger.cpp):
 *   header: VarInt protocol, VarLong start epoch ms
 *   item:   Bool compressed, VarInt size, bytes; the body is zlib-compressed when longer than 256 bytes
 *   body:   VarInt state, VarInt origin (0 = from the server, 1 = from the client), VarLong ms since start,
 *           VarLong bandwidth bytes, then the packet: VarInt id + payload (exactly our frame)
 * SniffCraft keeps one session per connection, so every connection in the dump gets its own file.
 */
export const sniffcraft: Exporter = {
  name: 'sniffcraft',
  summary: 'SniffCraft .scbin sessions, one file per connection (open them in SniffCraft)',
  async export(ctx: ExportContext): Promise<string[]> {
    await mkdir(ctx.outDir, { recursive: true });
    const fallbackProtocol = Number(ctx.options.protocol ?? ctx.protocol.protocolVersion);
    const outputs = new Map<number, Output>();
    let skipped = 0;
    for await (const packet of ctx.packets()) {
      if (packet.state === 255) {
        skipped++;
        continue;
      }
      let output = outputs.get(packet.conn);
      if (!output) {
        const conn = ctx.connections.get(packet.conn);
        const who = conn?.name ?? `conn${packet.conn}`;
        const leg = conn?.leg === 1 ? 'backend' : 'frontend';
        const path = join(ctx.outDir, `${safeName(`${ctx.capture.id}_${ctx.instance}_${leg}_${who}`)}.scbin`);
        const stream = createWriteStream(path);
        const protocol = conn && conn.protocol >= 0 ? conn.protocol : fallbackProtocol;
        stream.write(Buffer.concat([varInt(protocol), varLong(ctx.startEpochMs + Math.floor(packet.us / 1000))]));
        output = { stream, path, firstUs: packet.us, packets: 0, label: `${leg} ${who}` };
        outputs.set(packet.conn, output);
      }
      // SniffCraft's origin: Server = a clientbound packet
      const body = Buffer.concat([
        varInt(packet.state),
        varInt(packet.dir === 1 ? 0 : 1),
        varLong(Math.floor((packet.us - output.firstUs) / 1000)),
        varLong(packet.frame.length),
        packet.frame,
      ]);
      const compressed = body.length > 256;
      const data = compressed ? deflateSync(body) : body;
      const ok = output.stream.write(Buffer.concat([Buffer.from([compressed ? 1 : 0]), varInt(data.length), data]));
      output.packets++;
      if (!ok) {
        await once(output.stream, 'drain');
      }
    }
    const lines: string[] = [];
    for (const output of outputs.values()) {
      output.stream.end();
      await once(output.stream, 'finish');
      lines.push(`${output.path}  (${output.label}, ${output.packets} packets)`);
    }
    if (lines.length === 0) {
      lines.push('(no packets matched, nothing written)');
    }
    if (skipped > 0) {
      lines.push(`note: ${skipped} packets with an unknown connection state were skipped (SniffCraft needs the state)`);
    }
    lines.push(
      `note: open these in a SniffCraft build for protocol ${fallbackProtocol} (Minecraft ${ctx.protocol.minecraftVersion}); ` +
        'SniffCraft stops loading at the first packet id it does not know. Bandwidth is the uncompressed frame size.',
    );
    return lines;
  },
};
