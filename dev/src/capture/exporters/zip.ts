import { writeFile } from 'node:fs/promises';
import { crc32, deflateRawSync, inflateRawSync } from 'node:zlib';
import { fail } from '../../util.ts';

export interface ZipEntry {
  name: string;
  data: Buffer;
}

const LIMIT = 0xffffffff;

/** DOS time/date for "now"; replay tools ignore it. */
function dosStamp(): { time: number; date: number } {
  const d = new Date();
  return {
    time: (d.getHours() << 11) | (d.getMinutes() << 5) | (d.getSeconds() >> 1),
    date: ((Math.max(d.getFullYear(), 1980) - 1980) << 9) | ((d.getMonth() + 1) << 5) | d.getDate(),
  };
}

/** Encodes a zip (deflate, no zip64) that java.util.zip and Java's zip FileSystem read. */
export function encodeZip(entries: ZipEntry[]): Buffer {
  const { time, date } = dosStamp();
  const locals: Buffer[] = [];
  const central: Buffer[] = [];
  let offset = 0;
  for (const entry of entries) {
    const name = Buffer.from(entry.name, 'utf8');
    const packed = deflateRawSync(entry.data);
    const crc = crc32(entry.data);
    if (packed.length > LIMIT || entry.data.length > LIMIT || offset > LIMIT) {
      fail(`${entry.name} is too large for a zip without zip64`);
    }
    const local = Buffer.alloc(30);
    local.writeUInt32LE(0x04034b50, 0);
    local.writeUInt16LE(20, 4);
    local.writeUInt16LE(0x0800, 6);
    local.writeUInt16LE(8, 8);
    local.writeUInt16LE(time, 10);
    local.writeUInt16LE(date, 12);
    local.writeUInt32LE(crc, 14);
    local.writeUInt32LE(packed.length, 18);
    local.writeUInt32LE(entry.data.length, 22);
    local.writeUInt16LE(name.length, 26);
    local.writeUInt16LE(0, 28);
    const header = Buffer.alloc(46);
    header.writeUInt32LE(0x02014b50, 0);
    header.writeUInt16LE(20, 4);
    local.copy(header, 6, 4, 30); // version needed .. extra length: same fields as the local header
    header.writeUInt16LE(0, 32); // comment length
    header.writeUInt16LE(0, 34); // disk number
    header.writeUInt16LE(0, 36); // internal attributes
    header.writeUInt32LE(0, 38); // external attributes
    header.writeUInt32LE(offset, 42);
    locals.push(local, name, packed);
    central.push(header, name);
    offset += local.length + name.length + packed.length;
  }
  const directory = Buffer.concat(central);
  const end = Buffer.alloc(22);
  end.writeUInt32LE(0x06054b50, 0);
  end.writeUInt16LE(entries.length, 8);
  end.writeUInt16LE(entries.length, 10);
  end.writeUInt32LE(directory.length, 12);
  end.writeUInt32LE(offset, 16);
  return Buffer.concat([...locals, directory, end]);
}

export async function writeZip(path: string, entries: ZipEntry[]): Promise<void> {
  await writeFile(path, encodeZip(entries));
}

/** Reads a zip written by encodeZip (the local headers carry the sizes). Used by tests. */
export function readZip(buf: Buffer): Map<string, Buffer> {
  const out = new Map<string, Buffer>();
  let pos = 0;
  while (pos + 30 <= buf.length && buf.readUInt32LE(pos) === 0x04034b50) {
    const packedSize = buf.readUInt32LE(pos + 18);
    const nameLength = buf.readUInt16LE(pos + 26);
    const extraLength = buf.readUInt16LE(pos + 28);
    const name = buf.toString('utf8', pos + 30, pos + 30 + nameLength);
    const start = pos + 30 + nameLength + extraLength;
    out.set(name, inflateRawSync(buf.subarray(start, start + packedSize)));
    pos = start + packedSize;
  }
  return out;
}
