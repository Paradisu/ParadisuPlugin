import { readFileSync } from 'node:fs';
import { DevError } from '../util.ts';
import { decompress } from './nbt.ts';

export interface ChunkRef {
  x: number;
  z: number;
}

/** Chunks present in a region file (local coordinates 0-31). */
export function listChunks(mcaPath: string): ChunkRef[] {
  const header = readFileSync(mcaPath).subarray(0, 4096);
  const chunks: ChunkRef[] = [];
  for (let i = 0; i < 1024; i++) {
    if (header.readUIntBE(i * 4, 3) !== 0) {
      chunks.push({ x: i % 32, z: Math.floor(i / 32) });
    }
  }
  return chunks;
}

/** Returns the uncompressed NBT bytes of one chunk (local coordinates 0-31). */
export function readChunk(mcaPath: string, x: number, z: number): Buffer {
  const file = readFileSync(mcaPath);
  const index = (x & 31) + (z & 31) * 32;
  const sector = file.readUIntBE(index * 4, 3);
  if (sector === 0) {
    throw new DevError(`Chunk ${x},${z} is not present in ${mcaPath}`);
  }
  const offset = sector * 4096;
  const length = file.readInt32BE(offset);
  const compression = file.readUInt8(offset + 4);
  const data = file.subarray(offset + 5, offset + 4 + length);
  if (compression >= 128) throw new DevError('External (.mcc) chunks are not supported');
  if (compression === 4) throw new DevError('LZ4-compressed chunks are not supported');
  if (compression === 3) return data;
  if (compression !== 1 && compression !== 2) throw new DevError(`Unknown chunk compression ${compression}`);
  return decompress(data);
}
