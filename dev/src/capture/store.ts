import { existsSync, readdirSync, statSync } from 'node:fs';
import { readFile, rm } from 'node:fs/promises';
import { join } from 'node:path';
import { DevError, fail } from '../util.ts';
import { type PacketRecord, readDump } from './format.ts';
import { CAPTURES } from './paths.ts';

/** capture.json: written by the CLI when a capture starts. */
export interface CaptureMeta {
  id: string;
  label?: string;
  createdAt: string;
  instances: string[];
  minecraftVersion: string;
  protocolVersion: number;
  players: string[];
  nextJoin: boolean;
}

/** The plugin's per-instance manifest (Manifest.java). */
export interface InstanceManifest {
  format: number;
  id: string;
  instance: string;
  platform: string;
  startedAt: string;
  stoppedAt?: string;
  stopReason?: string;
  packets: number;
  bytes: number;
  dropped: number;
  connections: { id: number; leg: number; remote: string; protocol: number; name?: string; uuid?: string }[];
  warnings: string[];
}

export interface ConnMeta {
  id: number;
  /** 0 = frontend, 1 = backend */
  leg: number;
  remote: string;
  protocol: number;
  name?: string;
  uuid?: string;
}

export interface Capture {
  dir: string;
  meta: CaptureMeta;
}

export function captureDir(id: string): string {
  return join(CAPTURES, id);
}

export function dumpPath(capture: Capture, instance: string): string {
  return join(capture.dir, `${instance}.bin.gz`);
}

export function protocolCopy(capture: Capture): string {
  return join(capture.dir, 'protocol.json');
}

export async function readManifest(capture: Capture, instance: string): Promise<InstanceManifest | undefined> {
  const path = join(capture.dir, `${instance}.json`);
  return existsSync(path) ? (JSON.parse(await readFile(path, 'utf8')) as InstanceManifest) : undefined;
}

export function newCaptureId(label?: string): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, '0');
  const stamp = `${d.getFullYear()}${p(d.getMonth() + 1)}${p(d.getDate())}-${p(d.getHours())}${p(d.getMinutes())}${p(d.getSeconds())}`;
  const slug = label?.replace(/[^A-Za-z0-9_-]+/g, '-').replace(/^-+|-+$/g, '');
  return slug ? `${stamp}-${slug}` : stamp;
}

/** All captures, newest first. */
export async function listCaptures(): Promise<Capture[]> {
  if (!existsSync(CAPTURES)) {
    return [];
  }
  const captures: Capture[] = [];
  for (const name of readdirSync(CAPTURES).sort().reverse()) {
    const file = join(CAPTURES, name, 'capture.json');
    if (existsSync(file)) {
      captures.push({ dir: join(CAPTURES, name), meta: JSON.parse(await readFile(file, 'utf8')) as CaptureMeta });
    }
  }
  return captures;
}

/** Resolves `latest`, an exact id or a unique id prefix. */
export async function loadCapture(idOrPrefix: string): Promise<Capture> {
  const all = await listCaptures();
  if (all.length === 0) {
    fail('There are no captures yet. Start one with `dev capture start`.');
  }
  if (idOrPrefix === 'latest') {
    return all[0]!;
  }
  const exact = all.find((capture) => capture.meta.id === idOrPrefix);
  if (exact) {
    return exact;
  }
  const matches = all.filter((capture) => capture.meta.id.startsWith(idOrPrefix));
  if (matches.length === 1) {
    return matches[0]!;
  }
  throw new DevError(
    matches.length === 0
      ? `No capture "${idOrPrefix}". See \`dev capture list\`.`
      : `"${idOrPrefix}" matches ${matches.length} captures: ${matches.map((m) => m.meta.id).join(', ')}`,
  );
}

export async function removeCapture(capture: Capture): Promise<void> {
  await rm(capture.dir, { recursive: true, force: true });
}

/** Instances of a capture that have a dump. */
export function dumpInstances(capture: Capture): string[] {
  return capture.meta.instances.filter((name) => existsSync(dumpPath(capture, name)));
}

export function sizeOnDisk(path: string): number {
  return existsSync(path) ? statSync(path).size : 0;
}

export function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KiB`;
  if (bytes < 1024 ** 3) return `${(bytes / 1024 ** 2).toFixed(1)} MiB`;
  return `${(bytes / 1024 ** 3).toFixed(2)} GiB`;
}

/** First pass over a dump: the connections (from OPEN/IDENTIFY records) and the capture start time. */
export async function readConnections(file: string): Promise<{ conns: Map<number, ConnMeta>; startEpochMs: number }> {
  const conns = new Map<number, ConnMeta>();
  let startEpochMs = 0;
  for await (const record of readDump(file)) {
    if (record.type === 'header') {
      startEpochMs = record.startEpochMs;
    } else if (record.type === 'open') {
      conns.set(record.conn, { id: record.conn, leg: record.leg, remote: record.remote, protocol: record.protocol });
    } else if (record.type === 'identify') {
      const conn = conns.get(record.conn);
      if (conn) {
        conn.name = record.name;
        conn.uuid = record.uuid;
      }
    }
  }
  return { conns, startEpochMs };
}

/** Packets of a dump that pass `keep`, in order. */
export async function* readPackets(file: string, keep: (packet: PacketRecord) => boolean = () => true): AsyncGenerator<PacketRecord> {
  for await (const record of readDump(file)) {
    if (record.type === 'packet' && keep(record)) {
      yield record;
    }
  }
}

export interface CaptureSummary {
  packets: number;
  /** Dump size on disk */
  size: number;
  /** Why each instance's capture ended */
  reasons: string[];
}

/** Totals over a capture's instances, from the plugin's manifests. */
export async function summarize(capture: Capture): Promise<CaptureSummary> {
  let packets = 0;
  let size = 0;
  const reasons = new Set<string>();
  for (const name of capture.meta.instances) {
    const manifest = await readManifest(capture, name);
    packets += manifest?.packets ?? 0;
    size += sizeOnDisk(dumpPath(capture, name));
    reasons.add(manifest?.stopReason ?? (manifest ? 'running or crashed' : 'not started'));
  }
  return { packets, size, reasons: [...reasons] };
}
