import { createReadStream } from 'node:fs';
import { constants, createGunzip, gzipSync } from 'node:zlib';
import { string, varInt, varLong } from './exporters/encode.ts';

/**
 * The dump written by devtools/capture (DumpWriter.java): a gzip of
 *   "PDCAP" u8 format varlong startEpochMs, then records
 *   1 OPEN, 2 IDENTIFY, 3 PACKET, 4 CLOSE, 5 NOTE.
 *
 * Format rule: adding a record type or changing a record body requires bumping FORMAT here and in DumpWriter.java;
 * readers must keep reading older formats.
 */
export const FORMAT = 1;
const MAGIC = Buffer.from('PDCAP');

export interface HeaderRecord {
  type: 'header';
  format: number;
  startEpochMs: number;
}
export interface OpenRecord {
  type: 'open';
  conn: number;
  /** 0 = frontend, 1 = backend (a proxy's link to a server) */
  leg: number;
  remote: string;
  /** -1 when unknown */
  protocol: number;
  nextState: number;
}
export interface IdentifyRecord {
  type: 'identify';
  conn: number;
  name: string;
  uuid?: string;
}
export interface PacketRecord {
  type: 'packet';
  /** 0-based index among the file's packets; assigned by the reader */
  seq: number;
  conn: number;
  /** Microseconds since the capture started */
  us: number;
  /** 0 = serverbound, 1 = clientbound */
  dir: number;
  /** 0 handshake, 1 status, 2 login, 3 play, 4 configuration, 255 unknown */
  state: number;
  id: number;
  /** The decoded frame: packet id varint followed by the payload */
  frame: Buffer;
}
export interface CloseRecord {
  type: 'close';
  conn: number;
  us: number;
}
export interface NoteRecord {
  type: 'note';
  json: string;
}
export type DumpRecord = HeaderRecord | OpenRecord | IdentifyRecord | PacketRecord | CloseRecord | NoteRecord;

class NeedMore extends Error {}

/** Incremental reader: parse a whole record or rewind and wait for more bytes. */
class Reader {
  private buf: Buffer = Buffer.alloc(0);
  pos = 0;

  push(chunk: Buffer): void {
    this.buf = Buffer.concat([this.buf.subarray(this.pos), chunk]);
    this.pos = 0;
  }

  private need(n: number): void {
    if (this.pos + n > this.buf.length) {
      throw new NeedMore();
    }
  }

  u8(): number {
    this.need(1);
    return this.buf[this.pos++]!;
  }

  varLong(): bigint {
    let value = 0n;
    for (let i = 0; i < 10; i++) {
      const b = this.u8();
      value |= BigInt(b & 0x7f) << BigInt(7 * i);
      if ((b & 0x80) === 0) {
        return value;
      }
    }
    throw new Error('VarLong too long');
  }

  varInt(): number {
    let value = 0;
    for (let i = 0; i < 5; i++) {
      const b = this.u8();
      value += (b & 0x7f) * 2 ** (7 * i);
      if ((b & 0x80) === 0) {
        return value;
      }
    }
    throw new Error('VarInt too long');
  }

  bytes(n: number): Buffer {
    this.need(n);
    const out = this.buf.subarray(this.pos, this.pos + n);
    this.pos += n;
    return Buffer.from(out);
  }

  string(): string {
    return this.bytes(this.varInt()).toString('utf8');
  }
}

function frameId(frame: Buffer): number {
  let value = 0;
  for (let i = 0; i < 5 && i < frame.length; i++) {
    value += (frame[i]! & 0x7f) * 2 ** (7 * i);
    if ((frame[i]! & 0x80) === 0) {
      return value;
    }
  }
  return -1;
}

function uuidOf(bytes: Buffer): string {
  const hex = bytes.toString('hex');
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

/**
 * Streams the records of a dump. A truncated file (crash, or a capture still running) yields the records that are
 * complete; an unknown record type or format is an error.
 */
export async function* readDump(path: string): AsyncGenerator<DumpRecord> {
  const gunzip = createReadStream(path).pipe(createGunzip({ finishFlush: constants.Z_SYNC_FLUSH }));
  const reader = new Reader();
  let header = false;
  let seq = 0;
  for await (const chunk of gunzip as AsyncIterable<Buffer>) {
    reader.push(chunk);
    while (true) {
      const start = reader.pos;
      let record: DumpRecord;
      try {
        if (!header) {
          if (!reader.bytes(MAGIC.length).equals(MAGIC)) {
            throw new Error(`${path} is not a capture dump`);
          }
          const format = reader.u8();
          if (format !== FORMAT) {
            throw new Error(`${path} uses dump format ${format}; this CLI reads format ${FORMAT}`);
          }
          record = { type: 'header', format, startEpochMs: Number(reader.varLong()) };
          header = true;
        } else {
          record = parseRecord(reader, () => seq++);
        }
      } catch (error) {
        if (error instanceof NeedMore) {
          reader.pos = start;
          break;
        }
        throw error;
      }
      yield record;
    }
  }
}

function parseRecord(r: Reader, nextSeq: () => number): DumpRecord {
  const type = r.u8();
  switch (type) {
    case 1: {
      const conn = r.varInt();
      const leg = r.u8();
      const remote = r.string();
      const protocol = r.varInt() - 1;
      const nextState = r.varInt() - 1;
      return { type: 'open', conn, leg, remote, protocol, nextState };
    }
    case 2: {
      const conn = r.varInt();
      const name = r.string();
      const uuid = r.u8() === 1 ? uuidOf(r.bytes(16)) : undefined;
      return { type: 'identify', conn, name, uuid };
    }
    case 3: {
      const conn = r.varInt();
      const us = Number(r.varLong());
      const dir = r.u8();
      const state = r.u8();
      const frame = r.bytes(r.varInt());
      return { type: 'packet', seq: nextSeq(), conn, us, dir, state, id: frameId(frame), frame };
    }
    case 4:
      return { type: 'close', conn: r.varInt(), us: Number(r.varLong()) };
    case 5:
      return { type: 'note', json: r.string() };
    default:
      throw new Error(`Unknown dump record type ${type}`);
  }
}

/** Encodes records as an uncompressed dump stream (gzip it to get a .bin.gz). Used by tests and filtered copies. */
export function encodeDump(startEpochMs: number, records: Exclude<DumpRecord, HeaderRecord>[]): Buffer {
  const parts: Buffer[] = [MAGIC, Buffer.from([FORMAT]), varLong(startEpochMs)];
  for (const record of records) {
    switch (record.type) {
      case 'open':
        parts.push(
          Buffer.from([1]),
          varInt(record.conn),
          Buffer.from([record.leg]),
          string(record.remote),
          varInt(record.protocol + 1),
          varInt(record.nextState + 1),
        );
        break;
      case 'identify': {
        const uuid = record.uuid ? Buffer.from(record.uuid.replaceAll('-', ''), 'hex') : undefined;
        parts.push(Buffer.from([2]), varInt(record.conn), string(record.name), Buffer.from([uuid ? 1 : 0]));
        if (uuid) {
          parts.push(uuid);
        }
        break;
      }
      case 'packet':
        parts.push(
          Buffer.from([3]),
          varInt(record.conn),
          varLong(record.us),
          Buffer.from([record.dir, record.state]),
          varInt(record.frame.length),
          record.frame,
        );
        break;
      case 'close':
        parts.push(Buffer.from([4]), varInt(record.conn), varLong(record.us));
        break;
      case 'note':
        parts.push(Buffer.from([5]), string(record.json));
        break;
    }
  }
  return Buffer.concat(parts);
}

export function gzipDump(startEpochMs: number, records: Exclude<DumpRecord, HeaderRecord>[]): Buffer {
  return gzipSync(encodeDump(startEpochMs, records));
}
