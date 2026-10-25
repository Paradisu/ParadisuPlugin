/** Minecraft VarInt / VarLong encoding (little-endian base-128), shared by the dump writer and the exporters. */

export function varInt(value: number): Buffer {
  let v = value >>> 0;
  const out: number[] = [];
  while (v >= 0x80) {
    out.push((v & 0x7f) | 0x80);
    v >>>= 7;
  }
  out.push(v);
  return Buffer.from(out);
}

/** Negative values encode as 64-bit two's complement (10 bytes), like Minecraft's. */
export function varLong(value: number | bigint): Buffer {
  let v = BigInt.asUintN(64, BigInt(value));
  const out: number[] = [];
  while (v >= 0x80n) {
    out.push(Number(v & 0x7fn) | 0x80);
    v >>= 7n;
  }
  out.push(Number(v));
  return Buffer.from(out);
}

export function string(value: string): Buffer {
  const bytes = Buffer.from(value, 'utf8');
  return Buffer.concat([varInt(bytes.length), bytes]);
}
