import { randomBytes } from 'node:crypto';
import { copyFileSync, existsSync, mkdirSync, writeFileSync } from 'node:fs';
import { rm } from 'node:fs/promises';
import net from 'node:net';
import { join } from 'node:path';
import { parseArgs } from 'node:util';
import type { Command, Ctx } from '../command.ts';
import type { DevConfig } from '../config.ts';
import { bold, columns, dim, formatDuration, keyValue, next, red, relPath, sameAs, status as statusWord, streamRow, table } from '../output.ts';
import { askText, confirmAction, interactive, pickFrom, pickInstances } from '../prompt.ts';
import { isRunning } from '../supervisor.ts';
import { type Instance, instancesOf } from '../templates.ts';
import { DevError, fail, log, sleep, warn } from '../util.ts';
import { request, watch } from './control.ts';
import { exporter, EXPORTERS } from './exporters/index.ts';
import { string, varInt } from './exporters/encode.ts';
import { FILTER_DOCS, FILTER_OPTIONS, type FilterValues, compileFilter, parseFilter, toServerFilter } from './filters.ts';
import type { PacketRecord } from './format.ts';
import { controlSocket } from './paths.ts';
import { type ProtocolTable, STATES, ensureProtocol, hintsFile, loadProtocolFile, protocolDir } from './protocol.ts';
import { hexdump } from './hexdump.ts';
import { parseCount, parseSize, parseSpan, split, validator } from './units.ts';
import {
  type RecordingChoices,
  askFilter,
  askRecording,
  filterFlags,
  hasFilterFlags,
  hasRecordingFlags,
  pickCapture,
  pickCaptures,
  pickInstanceOf,
  pickInstancesOf,
  recordingFlags,
} from './wizard.ts';
import { linkOf, receiverOf } from './receivers.ts';
import {
  type Capture,
  type CaptureMeta,
  type ConnMeta,
  captureDir,
  dumpInstances,
  dumpPath,
  formatBytes,
  listCaptures,
  loadCapture,
  newCaptureId,
  protocolCopy,
  readConnections,
  readManifest,
  readPackets,
  removeCapture,
  sizeOnDisk,
  summarize,
} from './store.ts';

// ---------------------------------------------------------------------------------------------------------------
// Small helpers
// ---------------------------------------------------------------------------------------------------------------

function clock(epochMs: number): string {
  const d = new Date(epochMs);
  const p = (n: number, width = 2) => String(n).padStart(width, '0');
  return `${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}.${p(d.getMilliseconds(), 3)}`;
}

function instanceNamed(config: DevConfig, name: string): Instance {
  return instancesOf(config).find((instance) => instance.name === name) ?? fail(`Unknown instance "${name}"`);
}

/** Instances named by --on (they must be running), or every running one. */
async function runningTargets(ctx: Ctx, names: string[], message: string): Promise<Instance[]> {
  const all = instancesOf(ctx.config);
  if (names.length > 0) {
    const picked = names.map((name) => instanceNamed(ctx.config, name));
    const down = picked.filter((instance) => !isRunning(instance));
    if (down.length > 0) {
      fail(`${down.map((instance) => instance.name).join(', ')} ${down.length === 1 ? 'is' : 'are'} not running`);
    }
    return picked;
  }
  const running = all.filter((instance) => isRunning(instance));
  if (running.length === 0) {
    fail('Nothing is running. Start the network with `dev start`.');
  }
  if (interactive() && running.length > 1) {
    const chosen = await pickInstances(ctx.config, message, (instance) => isRunning(instance));
    return all.filter((instance) => chosen.includes(instance.name) && isRunning(instance));
  }
  return running;
}

function socketOf(instance: Instance): string {
  return controlSocket(instance);
}

async function status(instance: Instance): Promise<any> {
  return request(socketOf(instance), instance.name, { cmd: 'status' });
}

/** Names of the players connected to these instances right now (for pickers). */
async function connectedPlayers(targets: Instance[]): Promise<string[]> {
  const names = new Set<string>();
  for (const instance of targets) {
    const state = await status(instance).catch(() => undefined);
    for (const conn of state?.connections ?? []) {
      if (conn.name && !conn.ignored) names.add(conn.name);
    }
  }
  return [...names];
}

/** Everything that can receive a packet in this network. */
function receiverNames(config: DevConfig): string[] {
  return ['client', ...instancesOf(config).map((instance) => instance.name)];
}

/** A capture named on the command line, or chosen from a menu on a terminal (`latest` otherwise, if allowed). */
async function captureArg(id: string | undefined, message: string, latestWhenScripted: boolean): Promise<Capture> {
  if (id) {
    return loadCapture(id);
  }
  if (interactive()) {
    return pickCapture(message);
  }
  if (latestWhenScripted) {
    return loadCapture('latest');
  }
  return fail('Name a capture (see `dev capture list`)');
}

async function tableOf(capture: Capture): Promise<ProtocolTable> {
  return loadProtocolFile(protocolCopy(capture));
}

function metaOf(conn: any): ConnMeta {
  return { id: conn.id, leg: conn.leg, remote: conn.remote ?? '', protocol: conn.protocol, name: conn.name ?? undefined, uuid: conn.uuid ?? undefined };
}

function who(conn: ConnMeta | undefined, id: number): string {
  return conn?.name ?? `conn ${id}`;
}

// ---------------------------------------------------------------------------------------------------------------
// Line formatting shared by `watch` and `show`
// ---------------------------------------------------------------------------------------------------------------

interface LineContext {
  config: DevConfig;
  instance: Instance;
  table: ProtocolTable;
  conns: Map<number, ConnMeta>;
  startEpochMs: number;
  prefix: string;
  row: (cells: string[]) => string;
}

/** Column layout shared by the lines of one `show`/`watch`; widths grow, and the seeds avoid most reflowing. */
function packetRow(seed: { player?: number; link?: number } = {}): (cells: string[]) => string {
  return streamRow([
    {},
    { align: 'right', min: 3 },
    { min: seed.player ?? 12 },
    { min: seed.link ?? 14 },
    { min: 13 },
    { min: 20 },
    { align: 'right' },
  ]);
}

function formatPacket(ctx: LineContext, p: { seq: number; conn: number; us: number; dir: number; state: number; id: number; size: number }, hex?: string): string {
  const conn = ctx.conns.get(p.conn);
  const link = conn ? linkOf(ctx.config, ctx.instance, conn, p.dir) : p.dir === 0 ? '→server' : 'server→';
  const line = ctx.row([
    clock(ctx.startEpochMs + p.us / 1000),
    `#${p.seq}`,
    `${ctx.prefix}${who(conn, p.conn)}`,
    link,
    STATES[p.state] ?? '?',
    ctx.table.name(p.state, p.dir, p.id).replace(/^minecraft:/, ''),
    `${p.size} B`,
  ]);
  return hex ? `${line}  ${dim(hex)}` : line;
}

// ---------------------------------------------------------------------------------------------------------------
// start / stop / status
// ---------------------------------------------------------------------------------------------------------------

const START_OPTIONS = {
  on: { type: 'string', multiple: true },
  'next-join': { type: 'boolean' },
  duration: { type: 'string' },
  packets: { type: 'string' },
  until: { type: 'string', multiple: true },
  'stop-on-disconnect': { type: 'boolean' },
  'max-size': { type: 'string' },
  label: { type: 'string' },
  live: { type: 'boolean' },
  watch: { type: 'boolean' },
  hex: { type: 'string' },
  ...FILTER_OPTIONS,
} as const;

async function startCommand(ctx: Ctx, args: string[]): Promise<void> {
  const { values } = parseArgs({ args, options: START_OPTIONS });
  const targets = await runningTargets(ctx, split(values.on), 'Capture on which instances?');
  const table = await ensureProtocol(ctx.config);
  const live = values.live ?? false;
  let rec: RecordingChoices = {
    players: split(values.player),
    nextJoin: values['next-join'] ?? false,
    duration: values.duration,
    packets: values.packets,
    maxSize: values['max-size'],
    until: split(values.until),
    stopOnDisconnect: values['stop-on-disconnect'] ?? false,
    label: values.label,
    watch: values.watch ?? false,
  };
  if (interactive() && !hasRecordingFlags(values)) {
    rec = await askRecording(await connectedPlayers(targets), table);
    sameAs(`dev capture start --on ${targets.map((instance) => instance.name).join(',')} ${recordingFlags(rec)}`.trimEnd());
  }
  const { players, nextJoin } = rec;
  const until = rec.until.flatMap((pattern) => {
    const keys = table.keys(pattern);
    if (keys.length === 0) {
      throw new DevError(`--until: no packet type matches "${pattern}"`);
    }
    return keys;
  });
  const id = live ? `live-${newCaptureId()}` : newCaptureId(rec.label);
  const dir = captureDir(id);
  if (!live) {
    mkdirSync(dir, { recursive: true });
    copyFileSync(join(protocolDir(table.minecraftVersion), 'protocol.json'), join(dir, 'protocol.json'));
    const meta: CaptureMeta = {
      id,
      label: rec.label,
      createdAt: new Date().toISOString(),
      instances: targets.map((instance) => instance.name),
      minecraftVersion: table.minecraftVersion,
      protocolVersion: table.protocolVersion,
      players,
      nextJoin,
    };
    writeFileSync(join(dir, 'capture.json'), JSON.stringify(meta, null, 2));
  }
  const started: Instance[] = [];
  try {
    for (const instance of targets) {
      const spec = {
        id,
        instance: instance.name,
        outputDir: live ? undefined : dir,
        players,
        nextJoin,
        maxPackets: rec.packets ? parseCount(rec.packets, '--packets') : 0,
        maxBytes: parseSize(rec.maxSize ?? ctx.config.capture.max_size),
        durationMs: rec.duration ? parseSpan(rec.duration) : 0,
        until,
        stopOnDisconnect: rec.stopOnDisconnect,
        gzipLevel: ctx.config.capture.gzip_level,
        queueCapacity: ctx.config.capture.queue_capacity,
      };
      await request(socketOf(instance), instance.name, { cmd: 'start', spec });
      started.push(instance);
    }
  } catch (error) {
    for (const instance of started) {
      await request(socketOf(instance), instance.name, { cmd: 'stop', reason: 'aborted' }).catch(() => {});
    }
    if (!live) {
      await rm(dir, { recursive: true, force: true });
    }
    throw error;
  }
  log(`${live ? 'Live session' : `Capture ${id}`} started on ${started.map((i) => i.name).join(', ')}`);
  if (rec.watch || live) {
    await watchInstances(ctx, started, { ...values, player: players }, live ? new Map(started.map((i) => [i.name, id])) : new Map());
  }
}

async function stopCommand(ctx: Ctx, args: string[]): Promise<void> {
  const { values } = parseArgs({ args, options: { on: { type: 'string', multiple: true } } });
  const names = split(values.on);
  const candidates = names.length > 0 ? names.map((name) => instanceNamed(ctx.config, name)) : instancesOf(ctx.config).filter((i) => isRunning(i));
  const rows: string[][] = [];
  const dropped: number[] = [];
  const warnings: string[] = [];
  for (const instance of candidates) {
    let state: any;
    try {
      state = await status(instance);
    } catch (error) {
      if (names.length > 0) throw error;
      continue;
    }
    if (!state.session) {
      if (names.length > 0) warnings.push(`${instance.name} has no capture running`);
      continue;
    }
    const outputDir: string | undefined = state.session.spec.outputDir;
    const result = await request(socketOf(instance), instance.name, { cmd: 'stop' });
    rows.push([
      instance.name,
      result.reason,
      String(result.packets),
      formatBytes(result.bytes),
      outputDir ? formatBytes(sizeOnDisk(join(outputDir, `${instance.name}.bin.gz`))) : 'live only',
    ]);
    dropped.push(result.dropped);
    if (result.dropped > 0) {
      warnings.push(`${instance.name} dropped ${result.dropped} packets; raise capture.queue_capacity or narrow the capture`);
    }
  }
  if (rows.length === 0) {
    log('No capture was running.');
  } else {
    const anyDropped = dropped.some((count) => count > 0);
    const cols = ['instance', 'reason', { header: 'packets', align: 'right' as const }, { header: 'data', align: 'right' as const }, { header: 'on disk', align: 'right' as const }];
    log(
      table(
        anyDropped ? [...cols, { header: 'dropped', align: 'right' as const }] : cols,
        rows.map((row, i) => (anyDropped ? [...row, dropped[i]! > 0 ? red(String(dropped[i])) : '0'] : row)),
      ),
    );
  }
  for (const warning of warnings) {
    warn(warning);
  }
}

async function statusCommand(ctx: Ctx): Promise<void> {
  const running = instancesOf(ctx.config).filter((instance) => isRunning(instance));
  if (running.length === 0) {
    log('Nothing is running.');
    return;
  }
  const rows: string[][] = [];
  const connRows: string[][] = [];
  let hintsMissing = false;
  for (const instance of running) {
    try {
      const state = await status(instance);
      const complete: boolean = state.protocolHints.complete;
      hintsMissing ||= !complete;
      const s = state.session;
      const summary = s
        ? [
            s.id,
            s.waitingForJoin ? 'waiting for login' : '',
            `${s.packets} packets`,
            formatBytes(s.bytes),
            s.dropped > 0 ? `${s.dropped} dropped` : '',
            s.spec.outputDir ? '' : 'live only',
          ]
            .filter(Boolean)
            .join(', ')
        : '';
      rows.push([instance.name, state.platform, complete ? String(state.protocolHints.protocol) : statusWord('missing'), summary]);
      for (const c of state.connections.filter((conn: any) => !conn.ignored)) {
        connRows.push([instance.name, String(c.id), c.name ?? '(unidentified)', c.leg === 1 ? 'backend' : 'client', STATES[c.state] ?? '?', c.remote ?? '', c.recording ? 'yes' : '']);
      }
    } catch (error) {
      rows.push([instance.name, instance.platform, statusWord('failed'), (error as Error).message.split('\n')[0]!]);
    }
  }
  log(table(['instance', 'platform', 'protocol', 'capture'], rows));
  if (connRows.length > 0) {
    log('');
    log(table(['instance', { header: '#', align: 'right' }, 'player', 'link', 'state', 'remote', 'rec'], connRows));
  }
  if (hintsMissing) {
    next('dev capture protocol', 'restart the instance');
  }
}

// ---------------------------------------------------------------------------------------------------------------
// watch
// ---------------------------------------------------------------------------------------------------------------

/**
 * The instance's active session, starting a live-only one when there is none. A session can appear or vanish between
 * the status and the start (a capture replacing a live session), so a lost race is retried.
 */
async function sessionFor(instance: Instance, players: string[], own: Map<string, string>): Promise<any> {
  for (let attempt = 0; ; attempt++) {
    const state = await status(instance);
    if (state.session) {
      return state;
    }
    const id = `live-${newCaptureId()}`;
    try {
      await request(socketOf(instance), instance.name, { cmd: 'start', spec: { id, instance: instance.name, players, nextJoin: false } });
      own.set(instance.name, id);
    } catch (error) {
      if (attempt >= 4 || !/already running/.test((error as Error).message)) {
        throw error;
      }
      await sleep(200);
    }
  }
}

/**
 * Follows instances live. `ownSessions` maps instance -> the live-only session id this process started, which it
 * stops on exit unless a real capture has replaced it meanwhile.
 */
async function watchInstances(
  ctx: Ctx,
  targets: Instance[],
  values: FilterValues & { hex?: string },
  ownSessions: Map<string, string>,
): Promise<void> {
  const table = await ensureProtocol(ctx.config);
  const filter = parseFilter(values);
  const hexBytes = values.hex ? Math.min(256, parseCount(values.hex, '--hex')) : 0;
  const abort = new AbortController();
  const onSigint = () => abort.abort();
  process.once('SIGINT', onSigint);
  try {
    await Promise.all(
      targets.map(async (instance) => {
        let replaced = true;
        while (replaced && !abort.signal.aborted) {
          replaced = false;
          const state = await sessionFor(instance, filter.players, ownSessions);
          const conns = new Map<number, ConnMeta>(state.connections.map((c: any) => [c.id, metaOf(c)]));
          const lineCtx: LineContext = {
            config: ctx.config,
            instance,
            table,
            conns,
            startEpochMs: Date.parse(state.session.startedAt),
            prefix: targets.length > 1 ? `[${instance.name}] ` : '',
            row: packetRow(),
          };
          const keep = compileFilter(filter, table, conns, (conn, dir) => receiverOf(ctx.config, instance, conn, dir));
          log(dim(`watching ${instance.name} (Ctrl-C to stop)`));
          await watch(
            socketOf(instance),
            instance.name,
            { ...toServerFilter(filter, table), payloadBytes: hexBytes },
            (line) => {
              switch (line.t) {
                case 'open':
                  conns.set(line.conn, { id: line.conn, leg: line.leg, remote: line.remote, protocol: line.protocol });
                  log(dim(`${lineCtx.prefix}-- #${line.conn} ${line.leg === 1 ? 'backend link' : 'connection'} opened (${line.remote})`));
                  break;
                case 'identify': {
                  const conn = conns.get(line.conn);
                  if (conn) {
                    conn.name = line.name;
                    conn.uuid = line.uuid ?? undefined;
                  }
                  log(dim(`${lineCtx.prefix}-- #${line.conn} is ${line.name}`));
                  break;
                }
                case 'close':
                  log(dim(`${lineCtx.prefix}-- #${line.conn} closed`));
                  break;
                case 'dropped':
                  warn(`${instance.name}: the watch fell behind and lost ${line.n} lines`);
                  break;
                case 'stopped':
                  if (line.reason === 'replaced') {
                    replaced = true;
                    log(dim(`${lineCtx.prefix}-- the live session was replaced by a capture; following that`));
                    
                  } else {
                    log(dim(`${lineCtx.prefix}-- capture stopped (${line.reason}): ${line.packets} packets`));
                  }
                  break;
                case 'packet':
                  if (keep(line)) {
                    log(formatPacket(lineCtx, line, line.hex));
                  }
                  break;
              }
            },
            abort.signal,
          );
        }
      }),
    );
  } finally {
    process.off('SIGINT', onSigint);
    for (const [name, id] of ownSessions) {
      const instance = instanceNamed(ctx.config, name);
      const state = await status(instance).catch(() => undefined);
      if (state?.session?.id === id) {
        await request(socketOf(instance), name, { cmd: 'stop' }).catch(() => {});
      }
    }
  }
}

async function watchCommand(ctx: Ctx, args: string[]): Promise<void> {
  const parsed = parseArgs({ args, options: { on: { type: 'string', multiple: true }, hex: { type: 'string' }, ...FILTER_OPTIONS } });
  const targets = await runningTargets(ctx, split(parsed.values.on), 'Watch which instances?');
  let values: FilterValues & { hex?: string } = parsed.values;
  if (interactive() && !hasFilterFlags(values)) {
    const picked = await askFilter({
      table: await ensureProtocol(ctx.config),
      receivers: receiverNames(ctx.config),
      players: await connectedPlayers(targets),
    });
    values = { ...values, ...picked };
    const flags = filterFlags(picked);
    if (flags) sameAs(`dev capture watch --on ${targets.map((instance) => instance.name).join(',')} ${flags}`);
  }
  await watchInstances(ctx, targets, values, new Map());
}

// ---------------------------------------------------------------------------------------------------------------
// list / show / packet / export / rm
// ---------------------------------------------------------------------------------------------------------------

async function listCommand(): Promise<void> {
  const captures = await listCaptures();
  if (captures.length === 0) {
    log('No captures.');
    return;
  }
  const rows: string[][] = [];
  for (const capture of captures) {
    const summary = await summarize(capture);
    rows.push([capture.meta.id, capture.meta.instances.join(','), String(summary.packets), formatBytes(summary.size), summary.reasons.join(',')]);
  }
  log(table(['id', 'instances', { header: 'packets', align: 'right' }, { header: 'size', align: 'right' }, 'stopped by'], rows));
}

async function openDump(ctx: Ctx, capture: Capture, instanceName: string) {
  const file = dumpPath(capture, instanceName);
  if (!existsSync(file)) {
    fail(`${instanceName} has no dump in ${capture.meta.id}`);
  }
  const manifest = await readManifest(capture, instanceName);
  const known = instancesOf(ctx.config).find((instance) => instance.name === instanceName);
  const instance = known ?? ({ name: instanceName, platform: manifest?.platform ?? 'paper' } as Instance);
  const { conns, startEpochMs } = await readConnections(file);
  return { file, instance, conns, startEpochMs, manifest };
}

/** `--instance a,b`, or every instance with a dump. */
function chooseInstances(capture: Capture, flag: string | undefined): string[] {
  const available = dumpInstances(capture);
  const wanted = split(flag ? [flag] : []);
  if (wanted.length > 0) {
    for (const name of wanted) {
      if (!available.includes(name)) {
        fail(`${name} has no dump in ${capture.meta.id} (has: ${available.join(', ') || 'none'})`);
      }
    }
    return wanted;
  }
  if (available.length === 0) {
    fail(`${capture.meta.id} has no dumps yet`);
  }
  return available;
}

/** What `--player`, `--to` and `--conn` can offer for a capture. */
async function captureFilterSource(ctx: Ctx, capture: Capture, names: string[], table_: ProtocolTable) {
  const players = new Set<string>();
  const receivers = new Set<string>();
  let conns: { id: number; label: string }[] | undefined;
  for (const name of names) {
    const { instance, conns: found } = await openDump(ctx, capture, name);
    for (const conn of found.values()) {
      if (conn.name) players.add(conn.name);
      for (const dir of [0, 1]) receivers.add(receiverOf(ctx.config, instance, conn, dir));
    }
    if (names.length === 1) {
      conns = [...found.values()].map((conn) => ({ id: conn.id, label: `#${conn.id} ${conn.name ?? '(unidentified)'}, ${conn.leg === 1 ? 'backend link' : 'connection'}` }));
    }
  }
  // client and proxy first, then the servers
  const order = (name: string) => (name === 'client' ? 0 : name === 'proxy' ? 1 : 2);
  const sorted = [...receivers].sort((a, b) => order(a) - order(b) || a.localeCompare(b));
  return { table: table_, receivers: sorted, players: [...players], conns };
}

async function showCommand(ctx: Ctx, args: string[]): Promise<void> {
  const { values, positionals } = parseArgs({
    args,
    options: { instance: { type: 'string' }, limit: { type: 'string' }, stats: { type: 'boolean' }, hex: { type: 'string' }, ...FILTER_OPTIONS },
    allowPositionals: true,
  });
  const capture = await captureArg(positionals[0], 'Show which capture?', true);
  const table_ = await tableOf(capture);
  const names = chooseInstances(capture, values.instance);
  let filterValues: FilterValues = values;
  if (interactive() && !hasFilterFlags(values)) {
    const picked = await askFilter(await captureFilterSource(ctx, capture, names, table_));
    filterValues = { ...values, ...picked };
    const flags = filterFlags(picked);
    if (flags) sameAs(`dev capture show ${capture.meta.id} ${flags}`);
  }
  const filter = parseFilter(filterValues);
  const limit = values.limit ? parseCount(values.limit, '--limit') : Infinity;
  const hexBytes = values.hex ? Math.min(256, parseCount(values.hex, '--hex')) : 0;
  for (const name of names) {
    const { file, instance, conns, startEpochMs, manifest } = await openDump(ctx, capture, name);
    if (names.length > 1) {
      log(bold(name));
    }
    if (manifest?.dropped) {
      warn(`${name}: ${manifest.dropped} packets were dropped while capturing`);
    }
    const keep = compileFilter(filter, table_, conns, (conn, dir) => receiverOf(ctx.config, instance, conn, dir));
    const lineCtx: LineContext = {
      config: ctx.config,
      instance,
      table: table_,
      conns,
      startEpochMs,
      prefix: '',
      row: packetRow({ player: Math.max(0, ...[...conns.values()].map((conn) => (conn.name ?? '').length)) }),
    };
    if (values.stats) {
      printStats(ctx, lineCtx, await collect(file, keep));
      continue;
    }
    let shown = 0;
    for await (const p of readPackets(file, keep)) {
      const hex = hexBytes ? p.frame.subarray(widthOf(p), widthOf(p) + hexBytes).toString('hex') : undefined;
      log(formatPacket(lineCtx, { ...p, size: p.frame.length }, hex));
      if (++shown >= limit) {
        log(dim(`(stopped at --limit ${limit})`));
        break;
      }
    }
    if (shown === 0) {
      log('(no packets matched)');
    }
  }
}

function widthOf(p: PacketRecord): number {
  let i = 0;
  while (i < p.frame.length && (p.frame[i]! & 0x80) !== 0) i++;
  return i + 1;
}

async function collect(file: string, keep: (p: PacketRecord) => boolean): Promise<PacketRecord[]> {
  const out: PacketRecord[] = [];
  for await (const p of readPackets(file, keep)) out.push(p);
  return out;
}

function printStats(ctx: Ctx, lineCtx: LineContext, packets: PacketRecord[]): void {
  type Totals = { count: number; bytes: number };
  const byType = new Map<string, Totals & { receiver: string; name: string }>();
  const byConn = new Map<number, Totals>();
  for (const p of packets) {
    const conn = lineCtx.conns.get(p.conn);
    const receiver = conn ? receiverOf(ctx.config, lineCtx.instance, conn, p.dir) : '?';
    const name = lineCtx.table.name(p.state, p.dir, p.id).replace(/^minecraft:/, '');
    const key = `${receiver}\u0000${name}`;
    const type = byType.get(key) ?? { receiver, name, count: 0, bytes: 0 };
    type.count++;
    type.bytes += p.frame.length;
    byType.set(key, type);
    const total = byConn.get(p.conn) ?? { count: 0, bytes: 0 };
    total.count++;
    total.bytes += p.frame.length;
    byConn.set(p.conn, total);
  }
  const right = (header: string) => ({ header, align: 'right' as const });
  const rows = [...byType.values()]
    .sort((a, b) => b.bytes - a.bytes)
    .slice(0, 60)
    .map((e) => [e.receiver, e.name, String(e.count), formatBytes(e.bytes), formatBytes(Math.round(e.bytes / e.count))]);
  log(table(['receiver', 'packet', right('count'), right('bytes'), right('avg')], rows));
  log('');
  log(
    table(
      ['connection', right('packets'), right('bytes')],
      [...byConn].map(([id, e]) => [who(lineCtx.conns.get(id), id), String(e.count), formatBytes(e.bytes)]),
    ),
  );
}

async function packetCommand(ctx: Ctx, args: string[]): Promise<void> {
  const { values, positionals } = parseArgs({
    args,
    options: { instance: { type: 'string' }, hex: { type: 'boolean' }, raw: { type: 'string' }, 'no-id': { type: 'boolean' } },
    allowPositionals: true,
  });
  const capture = await captureArg(positionals[0], 'Which capture?', false);
  const available = dumpInstances(capture);
  const name =
    values.instance || available.length <= 1
      ? chooseInstances(capture, values.instance)[0]!
      : interactive()
        ? await pickInstanceOf(capture, 'Which instance?')
        : fail(`${capture.meta.id} has several instances (${available.join(', ')}); pass --instance`);
  const seqText =
    positionals[1] ??
    (interactive()
      ? await askText('Packet number (#n from dev capture show)', {
          validate: validator((text) => {
            if (!/^\d+$/.test(text.trim())) throw new DevError('Enter a whole number');
          }),
        })
      : fail('Usage: dev capture packet <capture> <seq> [--instance x] [--raw <file>] [--no-id]'));
  const seq = Number(seqText);
  if (!Number.isInteger(seq) || seq < 0) {
    fail('<seq> must be a packet number from `dev capture show`');
  }
  const table_ = await tableOf(capture);
  const { file, instance, conns, startEpochMs } = await openDump(ctx, capture, name);
  for await (const p of readPackets(file, (packet) => packet.seq === seq)) {
    const body = values['no-id'] ? p.frame.subarray(widthOf(p)) : p.frame;
    if (values.raw) {
      writeFileSync(values.raw, body);
      log(`Wrote ${body.length} bytes to ${values.raw}`);
      return;
    }
    log(formatPacket({ config: ctx.config, instance, table: table_, conns, startEpochMs, prefix: '', row: packetRow() }, { ...p, size: p.frame.length }));
    log('');
    log(hexdump(body));
    return;
  }
  fail(`${name} has no packet #${seq} in ${capture.meta.id}`);
}

async function exportCommand(ctx: Ctx, args: string[]): Promise<void> {
  const { values, positionals } = parseArgs({
    args,
    options: { format: { type: 'string' }, instance: { type: 'string' }, out: { type: 'string' }, opt: { type: 'string', multiple: true }, ...FILTER_OPTIONS },
    allowPositionals: true,
  });
  const capture = await captureArg(positionals[0], 'Export which capture?', true);
  const formatName =
    values.format ??
    (interactive()
      ? await pickFrom(
          'Export format',
          (() => {
            const exporters = Object.values(EXPORTERS);
            const labels = columns(exporters.map((candidate) => [candidate.name, dim(candidate.summary)]));
            return exporters.map((candidate, i) => ({ name: labels[i]!, value: candidate.name }));
          })(),
        )
      : 'sniffcraft');
  const format = exporter(formatName);
  const table_ = await tableOf(capture);
  const names =
    !values.instance && interactive() && dumpInstances(capture).length > 1
      ? await pickInstancesOf(capture, 'Export which instances?')
      : chooseInstances(capture, values.instance);
  let filterValues: FilterValues = values;
  let picked: FilterValues = {};
  if (interactive() && !hasFilterFlags(values)) {
    picked = await askFilter(await captureFilterSource(ctx, capture, names, table_));
    filterValues = { ...values, ...picked };
  }
  if (interactive() && (!values.format || !hasFilterFlags(values))) {
    const instanceFlag = names.length < dumpInstances(capture).length ? ` --instance ${names.join(',')}` : '';
    sameAs(`dev capture export ${capture.meta.id} --format ${format.name}${instanceFlag} ${filterFlags(picked)}`.trimEnd());
  }
  const filter = parseFilter(filterValues);
  const options: Record<string, string | boolean | undefined> = {};
  for (const entry of values.opt ?? []) {
    const [key, ...rest] = entry.split('=');
    options[key!] = rest.length > 0 ? rest.join('=') : true;
  }
  const outDir = values.out ?? join(capture.dir, 'export', format.name);
  for (const name of names) {
    const { file, instance, conns, startEpochMs } = await openDump(ctx, capture, name);
    const receiver = (conn: ConnMeta, dir: number) => receiverOf(ctx.config, instance, conn, dir);
    const keep = compileFilter(filter, table_, conns, receiver);
    const lines = await format.export({
      capture: capture.meta,
      instance: name,
      protocol: table_,
      connections: conns,
      startEpochMs,
      packets: () => readPackets(file, keep),
      receiverOf: receiver,
      outDir,
      options,
    });
    printExport(name, names.length > 1, lines);
  }
}

/** The files written, one path per line; notes are problems, so they are warnings. */
function printExport(name: string, heading: boolean, lines: string[]): void {
  if (heading) log(bold(name));
  for (const line of lines) {
    if (line.startsWith('note: ')) warn(line.slice('note: '.length));
    else log(relPath(line));
  }
}

function formatsCommand(): void {
  log(table(['format', 'description'], Object.values(EXPORTERS).map((e) => [e.name, e.summary])));
}

async function rmCommand(ctx: Ctx, args: string[]): Promise<void> {
  const { values, positionals } = parseArgs({ args, options: { yes: { type: 'boolean' } }, allowPositionals: true });
  const captures = positionals.length > 0 ? await Promise.all(positionals.map((id) => loadCapture(id))) : await pickCaptures('Delete which captures?');
  if (captures.length === 0) {
    log('Nothing selected.');
    return;
  }
  for (const capture of captures) {
    for (const instance of instancesOf(ctx.config).filter((candidate) => isRunning(candidate) && capture.meta.instances.includes(candidate.name))) {
      const state = await status(instance).catch(() => undefined);
      if (state?.session?.id === capture.meta.id) {
        fail(`${capture.meta.id} is still recording on ${instance.name}; run \`dev capture stop\` first`);
      }
    }
  }
  const total = (await Promise.all(captures.map((capture) => summarize(capture)))).reduce((sum, summary) => sum + summary.size, 0);
  const what = captures.length === 1 ? captures[0]!.meta.id : `${captures.length} captures`;
  if (await confirmAction(`Delete ${what} (${formatBytes(total)})?`, values.yes ?? false)) {
    for (const capture of captures) {
      await removeCapture(capture);
      log(`Deleted ${capture.meta.id}`);
    }
  }
}

// ---------------------------------------------------------------------------------------------------------------
// protocol / doctor
// ---------------------------------------------------------------------------------------------------------------

async function protocolCommand(ctx: Ctx, args: string[]): Promise<void> {
  const { values } = parseArgs({ args, options: { refresh: { type: 'boolean' } } });
  const protocol = await ensureProtocol(ctx.config, values.refresh ?? false);
  log(`Minecraft ${protocol.minecraftVersion}, protocol ${protocol.protocolVersion}`);
}

const PROBE_NAME = 'CaptureProbe';

/** Logs in without a client: handshake + login hello are enough for the plugin to see and identify a connection. */
async function probe(instance: Instance, protocol: ProtocolTable): Promise<{ ok: boolean; detail: string; handlers?: string[] }> {
  const framed = (body: Buffer) => Buffer.concat([varInt(body.length), body]);
  const port = Buffer.alloc(2);
  port.writeUInt16BE(instance.port);
  const socket = net.createConnection({ host: '127.0.0.1', port: instance.port });
  socket.on('error', () => {});
  try {
    await new Promise<void>((resolve, reject) => {
      socket.once('connect', resolve);
      socket.once('error', reject);
    });
    socket.write(framed(Buffer.concat([varInt(0), varInt(protocol.protocolVersion), string('localhost'), port, varInt(2)])));
    socket.write(framed(Buffer.concat([varInt(0), string(PROBE_NAME), randomBytes(16)])));
    for (let attempt = 0; attempt < 25; attempt++) {
      await sleep(200);
      const state = await status(instance);
      const found = state.connections.find((conn: any) => conn.name === PROBE_NAME);
      if (found) {
        const pipeline = await request(socketOf(instance), instance.name, { cmd: 'pipeline' });
        const handlers: string[] | undefined = pipeline.connections.find((conn: any) => conn.id === found.id)?.handlers ?? undefined;
        return { ok: true, detail: `identified ${PROBE_NAME} (state ${STATES[found.state] ?? found.state})`, handlers };
      }
    }
    return { ok: false, detail: 'connected, but the plugin never saw the login (injection did not attach?)' };
  } catch (error) {
    return { ok: false, detail: `could not connect to port ${instance.port}: ${(error as Error).message} (if \`dev share\` is active the proxy requires the PROXY protocol)` };
  } finally {
    socket.destroy();
  }
}

function checkPlacement(handlers: string[]): { ok: boolean; text: string } | undefined {
  const inIndex = handlers.indexOf('paradisu_capture_in');
  const outIndex = handlers.indexOf('paradisu_capture_out');
  if (inIndex === -1 || outIndex === -1) {
    return undefined;
  }
  // Paper names the codec slots inbound_config/outbound_config until the connection reaches play
  const decoders = ['decoder', 'inbound_config', 'minecraft-decoder'];
  const encoders = ['encoder', 'outbound_config', 'minecraft-encoder'];
  const problems: string[] = [];
  if (!decoders.includes(handlers[inIndex + 1] ?? '')) problems.push(`inbound tap is followed by "${handlers[inIndex + 1]}", not a decoder`);
  if (!encoders.includes(handlers[outIndex + 1] ?? '')) problems.push(`outbound tap is followed by "${handlers[outIndex + 1]}", not an encoder`);
  return problems.length > 0 ? { ok: false, text: problems.join('; ') } : { ok: true, text: 'taps sit before the decoder and encoder' };
}

async function doctorCommand(ctx: Ctx, args: string[]): Promise<void> {
  const { positionals } = parseArgs({ args, allowPositionals: true });
  const targets = await runningTargets(ctx, positionals, 'Check which instances?');
  const protocol = await ensureProtocol(ctx.config);
  let failed = false;
  let probeFailed = false;
  const verdict = (ok: boolean, text: string) => `${statusWord(ok ? 'ok' : 'failed')}  ${text}`;
  for (const instance of targets) {
    log(bold(`${instance.name} (${instance.platform})`));
    const rows: [string, string][] = [];
    try {
      const state = await status(instance);
      const hints = state.protocolHints;
      rows.push(['control socket', `${statusWord('ok')}  plugin on ${state.platform}`]);
      if (!hints.complete) {
        rows.push(['protocol hints', `${statusWord('missing')}  run dev capture protocol, then restart this instance`]);
        failed = true;
      } else if (hints.protocol !== protocol.protocolVersion) {
        rows.push(['protocol hints', `${statusWord('stale')}  plugin has ${hints.protocol}, current is ${protocol.protocolVersion}; restart this instance`]);
        failed = true;
      } else {
        rows.push(['protocol hints', `${statusWord('ok')}  protocol ${hints.protocol}`]);
      }
      const result = await probe(instance, protocol);
      rows.push(['login probe', verdict(result.ok, result.detail)]);
      if (!result.ok) {
        failed = true;
        probeFailed = true;
      }
      if (result.handlers) {
        const placement = checkPlacement(result.handlers);
        if (placement) {
          rows.push(['placement', verdict(placement.ok, placement.text)]);
          failed ||= !placement.ok;
        }
        if (!result.ok || (placement && !placement.ok)) rows.push(['pipeline', dim(result.handlers.join(' > '))]);
      }
    } catch (error) {
      failed = true;
      rows.push(['error', verdict(false, (error as Error).message)]);
    }
    log(keyValue(rows, { indent: 2 }));
  }
  if (probeFailed) {
    log(dim('Fix: PipelineLayout.java (paper, velocity), CapturePaper.java / CaptureVelocity.java (hooks), CaptureLimbo.java (limbo)'));
  }
  if (failed) {
    fail('Some checks failed.');
  }
  log('All checks passed.');
}

export const CAPTURE_COMMANDS: Command[] = [
  {
    name: 'capture start',
    usage: '',
    summary: 'Start a packet capture',
    details: 'Guided when no options are given.',
    options: [
      { flag: '--on <names>', summary: 'Instances to use (default: all running)' },
      { flag: '--player <names>', summary: 'Only these players' },
      { flag: '--next-join', summary: 'Wait for the next login' },
      { flag: '--duration <span>', summary: 'Stop after a time, e.g. 5m' },
      { flag: '--packets <n>', summary: 'Stop after n packets' },
      { flag: '--until <types>', summary: 'Stop when a packet type shows up' },
      { flag: '--stop-on-disconnect', summary: 'Stop when the last player disconnects' },
      { flag: '--max-size <size>', summary: 'Stop at this size, e.g. 512M' },
      { flag: '--label <text>', summary: 'Label in the capture id' },
      { flag: '--live', summary: 'Stream without saving' },
      { flag: '--watch', summary: 'Watch packets while recording' },
    ],
    run: startCommand,
  },
  { name: 'capture stop', usage: '', summary: 'Stop the running captures', options: [{ flag: '--on <names>', summary: 'Instances to use (default: all running)' }], run: stopCommand },
  { name: 'capture status', usage: '', summary: 'Show capture state and connections', run: statusCommand },
  {
    name: 'capture watch',
    usage: '',
    summary: 'Follow packets live',
    details: 'Guided filter when no options are given; Ctrl-C ends.',
    options: [
      { flag: '--on <names>', summary: 'Instances to use (default: all running)' },
      ...FILTER_DOCS,
      { flag: '--hex <n>', summary: 'Show n payload bytes' },
    ],
    run: watchCommand,
  },
  { name: 'capture list', usage: '', summary: 'List captures', run: listCommand },
  {
    name: 'capture show',
    usage: '[<id|latest>]',
    summary: 'Print a capture\'s packets or statistics',
    details: 'Guided when no options are given.',
    options: [
      { flag: '--instance <name>', summary: 'Instance dump to read' },
      ...FILTER_DOCS,
      { flag: '--limit <n>', summary: 'Stop after n packets' },
      { flag: '--hex <n>', summary: 'Show n payload bytes' },
      { flag: '--stats', summary: 'Print statistics instead of packets' },
    ],
    run: showCommand,
  },
  {
    name: 'capture packet',
    usage: '[<id|latest> [<seq>]]',
    summary: 'Hex dump or save one packet',
    options: [
      { flag: '--instance <name>', summary: 'Instance dump to read' },
      { flag: '--raw <file>', summary: 'Write the packet bytes to a file' },
      { flag: '--no-id', summary: 'Leave out the packet id' },
    ],
    run: packetCommand,
  },
  {
    name: 'capture export',
    usage: '[<id|latest>]',
    summary: 'Export a capture for other tools',
    details: 'Asks for the format when --format is missing.',
    options: [
      { flag: '--format <name>', summary: 'Export format (see dev capture formats)' },
      { flag: '--instance <names>', summary: 'Instance dumps to export' },
      { flag: '--out <dir>', summary: 'Output directory' },
      ...FILTER_DOCS,
      { flag: '--opt <key=value>', summary: 'Exporter option' },
    ],
    run: exportCommand,
  },
  { name: 'capture formats', usage: '', summary: 'List export formats', run: formatsCommand },
  { name: 'capture rm', usage: '[<id...>]', summary: 'Delete captures', details: 'Pick from a menu when no ids are given.', options: [{ flag: '--yes', summary: 'Do not ask for confirmation' }], run: rmCommand },
  { name: 'capture protocol', usage: '', summary: 'Generate the packet table for this Minecraft version', options: [{ flag: '--refresh', summary: 'Regenerate even when cached' }], run: protocolCommand },
  {
    name: 'capture doctor',
    usage: '[names...]',
    summary: 'Check capture works on each instance',
    details: 'Runs a login probe and checks handler placement.',
    run: doctorCommand,
  },
];

