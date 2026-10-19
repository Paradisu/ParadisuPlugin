import { gunzipSync, gzipSync, inflateSync } from 'node:zlib';
import { fail } from '../util.ts';

export type TagType =
  | 'end'
  | 'byte'
  | 'short'
  | 'int'
  | 'long'
  | 'float'
  | 'double'
  | 'byteArray'
  | 'string'
  | 'list'
  | 'compound'
  | 'intArray'
  | 'longArray';

export type Tag =
  | { type: 'byte' | 'short' | 'int' | 'float' | 'double'; value: number }
  | { type: 'long'; value: bigint }
  | { type: 'string'; value: string }
  | { type: 'byteArray'; value: Int8Array }
  | { type: 'intArray'; value: Int32Array }
  | { type: 'longArray'; value: BigInt64Array }
  | { type: 'list'; elementType: TagType; value: Tag[] }
  | { type: 'compound'; value: Record<string, Tag> };

const IDS: TagType[] = [
  'end',
  'byte',
  'short',
  'int',
  'long',
  'float',
  'double',
  'byteArray',
  'string',
  'list',
  'compound',
  'intArray',
  'longArray',
];

function idOf(type: TagType): number {
  return IDS.indexOf(type);
}

// ---------------------------------------------------------------------------------------------------------------
// Java "modified UTF-8": NUL is two bytes and characters outside the BMP are two 3-byte surrogates.
// ---------------------------------------------------------------------------------------------------------------

export function encodeModifiedUtf8(text: string): Buffer {
  const bytes: number[] = [];
  for (let i = 0; i < text.length; i++) {
    const c = text.charCodeAt(i);
    if (c >= 0x01 && c <= 0x7f) {
      bytes.push(c);
    } else if (c <= 0x7ff) {
      bytes.push(0xc0 | (c >> 6), 0x80 | (c & 0x3f));
    } else {
      bytes.push(0xe0 | (c >> 12), 0x80 | ((c >> 6) & 0x3f), 0x80 | (c & 0x3f));
    }
  }
  return Buffer.from(bytes);
}

export function decodeModifiedUtf8(buf: Buffer): string {
  let out = '';
  for (let i = 0; i < buf.length; ) {
    const a = buf[i]!;
    if (a < 0x80) {
      out += String.fromCharCode(a);
      i += 1;
    } else if ((a & 0xe0) === 0xc0) {
      out += String.fromCharCode(((a & 0x1f) << 6) | (buf[i + 1]! & 0x3f));
      i += 2;
    } else {
      out += String.fromCharCode(((a & 0x0f) << 12) | ((buf[i + 1]! & 0x3f) << 6) | (buf[i + 2]! & 0x3f));
      i += 3;
    }
  }
  return out;
}

// ---------------------------------------------------------------------------------------------------------------
// Reading
// ---------------------------------------------------------------------------------------------------------------

class Reader {
  offset = 0;
  readonly buf: Buffer;
  constructor(buf: Buffer) {
    this.buf = buf;
  }

  need(bytes: number): void {
    if (this.offset + bytes > this.buf.length) {
      fail(`Unexpected end of NBT data at byte ${this.offset}`);
    }
  }
  u8(): number {
    this.need(1);
    return this.buf.readUInt8(this.offset++);
  }
  i8(): number {
    this.need(1);
    return this.buf.readInt8(this.offset++);
  }
  i16(): number {
    this.need(2);
    const v = this.buf.readInt16BE(this.offset);
    this.offset += 2;
    return v;
  }
  u16(): number {
    this.need(2);
    const v = this.buf.readUInt16BE(this.offset);
    this.offset += 2;
    return v;
  }
  i32(): number {
    this.need(4);
    const v = this.buf.readInt32BE(this.offset);
    this.offset += 4;
    return v;
  }
  i64(): bigint {
    this.need(8);
    const v = this.buf.readBigInt64BE(this.offset);
    this.offset += 8;
    return v;
  }
  f32(): number {
    this.need(4);
    const v = this.buf.readFloatBE(this.offset);
    this.offset += 4;
    return v;
  }
  f64(): number {
    this.need(8);
    const v = this.buf.readDoubleBE(this.offset);
    this.offset += 8;
    return v;
  }
  string(): string {
    const length = this.u16();
    this.need(length);
    const text = decodeModifiedUtf8(this.buf.subarray(this.offset, this.offset + length));
    this.offset += length;
    return text;
  }
}

function typeOf(id: number): TagType {
  const type = IDS[id];
  if (!type) {
    fail(`Unknown NBT tag id ${id}`);
  }
  return type;
}

function readPayload(r: Reader, type: TagType, depth: number): Tag {
  if (depth > 512) {
    fail('NBT nested too deeply');
  }
  switch (type) {
    case 'byte':
      return { type, value: r.i8() };
    case 'short':
      return { type, value: r.i16() };
    case 'int':
      return { type, value: r.i32() };
    case 'long':
      return { type, value: r.i64() };
    case 'float':
      return { type, value: r.f32() };
    case 'double':
      return { type, value: r.f64() };
    case 'string':
      return { type, value: r.string() };
    case 'byteArray': {
      const length = r.i32();
      r.need(length);
      const value = new Int8Array(length);
      for (let i = 0; i < length; i++) value[i] = r.i8();
      return { type, value };
    }
    case 'intArray': {
      const length = r.i32();
      r.need(length * 4);
      const value = new Int32Array(length);
      for (let i = 0; i < length; i++) value[i] = r.i32();
      return { type, value };
    }
    case 'longArray': {
      const length = r.i32();
      r.need(length * 8);
      const value = new BigInt64Array(length);
      for (let i = 0; i < length; i++) value[i] = r.i64();
      return { type, value };
    }
    case 'list': {
      const elementType = typeOf(r.u8());
      const length = r.i32();
      const value: Tag[] = [];
      for (let i = 0; i < length; i++) value.push(readPayload(r, elementType, depth + 1));
      return { type, elementType, value };
    }
    case 'compound': {
      const value: Record<string, Tag> = {};
      for (;;) {
        const id = r.u8();
        if (id === 0) break;
        const name = r.string();
        value[name] = readPayload(r, typeOf(id), depth + 1);
      }
      return { type, value };
    }
    default:
      return fail(`Cannot read a ${type} tag`);
  }
}

/** Reads an uncompressed NBT document: a named root tag (Java edition, big-endian). */
export function readNbt(buf: Buffer): { name: string; tag: Tag } {
  const r = new Reader(buf);
  const type = typeOf(r.u8());
  if (type === 'end') {
    fail('NBT document starts with an end tag');
  }
  const name = r.string();
  return { name, tag: readPayload(r, type, 0) };
}

/** Gunzips gzip data and inflates zlib data; anything else is returned as is. */
export function decompress(buf: Buffer): Buffer {
  if (buf[0] === 0x1f && buf[1] === 0x8b) {
    return gunzipSync(buf);
  }
  if (buf[0] === 0x78 && [0x01, 0x5e, 0x9c, 0xda].includes(buf[1]!)) {
    return inflateSync(buf);
  }
  return buf;
}

export function readCompressedNbt(buf: Buffer): { name: string; tag: Tag } {
  return readNbt(decompress(buf));
}

// ---------------------------------------------------------------------------------------------------------------
// Writing
// ---------------------------------------------------------------------------------------------------------------

class Writer {
  private chunks: Buffer[] = [];
  push(buf: Buffer): void {
    this.chunks.push(buf);
  }
  u8(v: number): void {
    this.push(Buffer.from([v & 0xff]));
  }
  i16(v: number): void {
    const b = Buffer.alloc(2);
    b.writeInt16BE(v);
    this.push(b);
  }
  i32(v: number): void {
    const b = Buffer.alloc(4);
    b.writeInt32BE(v);
    this.push(b);
  }
  i64(v: bigint): void {
    const b = Buffer.alloc(8);
    b.writeBigInt64BE(v);
    this.push(b);
  }
  string(text: string): void {
    const encoded = encodeModifiedUtf8(text);
    if (encoded.length > 0xffff) {
      fail('NBT string is longer than 65535 bytes');
    }
    const b = Buffer.alloc(2);
    b.writeUInt16BE(encoded.length);
    this.push(b);
    this.push(encoded);
  }
  done(): Buffer {
    return Buffer.concat(this.chunks);
  }
}

function writePayload(w: Writer, tag: Tag): void {
  switch (tag.type) {
    case 'byte':
      w.u8(tag.value);
      break;
    case 'short':
      w.i16(tag.value);
      break;
    case 'int':
      w.i32(tag.value);
      break;
    case 'long':
      w.i64(tag.value);
      break;
    case 'float': {
      const b = Buffer.alloc(4);
      b.writeFloatBE(tag.value);
      w.push(b);
      break;
    }
    case 'double': {
      const b = Buffer.alloc(8);
      b.writeDoubleBE(tag.value);
      w.push(b);
      break;
    }
    case 'string':
      w.string(tag.value);
      break;
    case 'byteArray':
      w.i32(tag.value.length);
      for (const v of tag.value) w.u8(v);
      break;
    case 'intArray':
      w.i32(tag.value.length);
      for (const v of tag.value) w.i32(v);
      break;
    case 'longArray':
      w.i32(tag.value.length);
      for (const v of tag.value) w.i64(v);
      break;
    case 'list':
      w.u8(idOf(tag.value.length === 0 ? 'end' : tag.elementType));
      w.i32(tag.value.length);
      for (const element of tag.value) writePayload(w, element);
      break;
    case 'compound':
      for (const [name, child] of Object.entries(tag.value)) {
        w.u8(idOf(child.type));
        w.string(name);
        writePayload(w, child);
      }
      w.u8(0);
      break;
  }
}

/** Writes an uncompressed NBT document with a named root. */
export function writeNbt(tag: Tag, name = ''): Buffer {
  const w = new Writer();
  w.u8(idOf(tag.type));
  w.string(name);
  writePayload(w, tag);
  return w.done();
}

export function writeCompressedNbt(tag: Tag, name = ''): Buffer {
  return gzipSync(writeNbt(tag, name));
}

// ---------------------------------------------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------------------------------------------

export function compound(value: Record<string, Tag> = {}): Tag {
  return { type: 'compound', value };
}
export const str = (value: string): Tag => ({ type: 'string', value });
export const int = (value: number): Tag => ({ type: 'int', value });
export const byte = (value: number): Tag => ({ type: 'byte', value });

/** Follows `a.b[0].c` through compounds and lists; undefined when anything is missing. */
export function get(tag: Tag, path: string): Tag | undefined {
  let current: Tag | undefined = tag;
  for (const part of path.match(/[^.[\]]+|\[\d+\]/g) ?? []) {
    if (!current) return undefined;
    if (part.startsWith('[')) {
      current = current.type === 'list' ? current.value[Number(part.slice(1, -1))] : undefined;
    } else {
      current = current.type === 'compound' ? current.value[part] : undefined;
    }
  }
  return current;
}

/** Lossy conversion for tools like jq: longs become strings, typed arrays become number arrays. */
export function toPlainJson(tag: Tag): unknown {
  switch (tag.type) {
    case 'long':
      return tag.value.toString();
    case 'byteArray':
    case 'intArray':
      return Array.from(tag.value);
    case 'longArray':
      return Array.from(tag.value, (v) => v.toString());
    case 'list':
      return tag.value.map(toPlainJson);
    case 'compound':
      return Object.fromEntries(Object.entries(tag.value).map(([k, v]) => [k, toPlainJson(v)]));
    default:
      return tag.value;
  }
}
