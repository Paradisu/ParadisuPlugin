/** Reading and writing the primitive types of Minecraft payloads (big-endian), for the exporters that decode packets. */

export class PayloadReader {
  pos = 0;
  readonly buf: Buffer;

  constructor(buf: Buffer) {
    this.buf = buf;
  }

  private need(n: number): void {
    if (this.pos + n > this.buf.length) {
      throw new Error('payload too short');
    }
  }

  remaining(): number {
    return this.buf.length - this.pos;
  }

  u8(): number {
    this.need(1);
    return this.buf[this.pos++]!;
  }

  i8(): number {
    return (this.u8() << 24) >> 24;
  }

  bool(): boolean {
    return this.u8() !== 0;
  }

  u16(): number {
    this.need(2);
    const v = this.buf.readUInt16BE(this.pos);
    this.pos += 2;
    return v;
  }

  i16(): number {
    this.need(2);
    const v = this.buf.readInt16BE(this.pos);
    this.pos += 2;
    return v;
  }

  i32(): number {
    this.need(4);
    const v = this.buf.readInt32BE(this.pos);
    this.pos += 4;
    return v;
  }

  i64(): bigint {
    this.need(8);
    const v = this.buf.readBigInt64BE(this.pos);
    this.pos += 8;
    return v;
  }

  f32(): number {
    this.need(4);
    const v = this.buf.readFloatBE(this.pos);
    this.pos += 4;
    return v;
  }

  f64(): number {
    this.need(8);
    const v = this.buf.readDoubleBE(this.pos);
    this.pos += 8;
    return v;
  }

  /** Signed 32-bit, like Minecraft's. */
  varInt(): number {
    let value = 0;
    for (let i = 0; i < 5; i++) {
      const b = this.u8();
      value |= (b & 0x7f) << (7 * i);
      if ((b & 0x80) === 0) {
        return value | 0;
      }
    }
    throw new Error('VarInt too long');
  }

  varLong(): bigint {
    let value = 0n;
    for (let i = 0; i < 10; i++) {
      const b = this.u8();
      value |= BigInt(b & 0x7f) << BigInt(7 * i);
      if ((b & 0x80) === 0) {
        return BigInt.asIntN(64, value);
      }
    }
    throw new Error('VarLong too long');
  }

  bytes(n: number): Buffer {
    this.need(n);
    const out = this.buf.subarray(this.pos, this.pos + n);
    this.pos += n;
    return out;
  }

  uuid(): Buffer {
    return this.bytes(16);
  }

  string(): string {
    return this.bytes(this.varInt()).toString('utf8');
  }

  rest(): Buffer {
    return this.bytes(this.remaining());
  }
}

/** Bytes the leading VarInt (the packet id) of a frame occupies. */
export function varIntWidth(buf: Buffer): number {
  let i = 0;
  while (i < buf.length && (buf[i]! & 0x80) !== 0) {
    i++;
  }
  return i + 1;
}

export function i32(value: number): Buffer {
  const out = Buffer.alloc(4);
  out.writeInt32BE(value | 0);
  return out;
}

export function f32(value: number): Buffer {
  const out = Buffer.alloc(4);
  out.writeFloatBE(value);
  return out;
}

export function f64(value: number): Buffer {
  const out = Buffer.alloc(8);
  out.writeDoubleBE(value);
  return out;
}

export function bool(value: boolean): Buffer {
  return Buffer.from([value ? 1 : 0]);
}

export function u8(value: number): Buffer {
  return Buffer.from([value & 0xff]);
}

/** Minecraft's Angle: 1/256 of a turn in one byte. */
export function angle(degrees: number): Buffer {
  return u8(Math.floor((degrees * 256) / 360));
}

/** A dashed UUID string as its 16 bytes. */
export function uuidBytes(text: string): Buffer {
  return Buffer.from(text.replaceAll('-', ''), 'hex');
}
