import { randomBytes } from 'node:crypto';
import { copyFileSync, existsSync, mkdirSync, writeFileSync } from 'node:fs';
import { rm } from 'node:fs/promises';
import net from 'node:net';
import { join } from 'node:path';
import { parseArgs } from 'node:util';
import type { Command, Ctx } from '../command.ts';
import type { DevConfig } from '../config.ts';
import { askText, confirmAction, interactive, pickFrom, pickInstances } from '../prompt.ts';
import { isRunning } from '../supervisor.ts';
import { type Instance, instancesOf } from '../templates.ts';
import { DevError, fail, log, sleep, warn } from '../util.ts';
import { request, watch } from './control.ts';
import { exporter, EXPORTERS } from './exporters/index.ts';
import { string, varInt } from './exporters/encode.ts';
import { FILTER_OPTIONS, type FilterValues, compileFilter, parseFilter, toServerFilter } from './filters.ts';
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

function dim(text: string): string {
  return process.stdout.isTTY ? `\x1b[2m${text}\x1b[0m` : text;
}

function table(rows: string[][]): string {
  const widths = rows[0]!.map((_, column) => Math.max(...rows.map((row) => (row[column] ?? '').length)));
  return rows.map((row) => row.map((cell, column) => cell.padEnd(widths[column]!)).join('  ').trimEnd()).join('\n');
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
}

function formatPacket(ctx: LineContext, p: { seq: number; conn: number; us: number; dir: number; state: number; id: number; size: number }, hex?: string): string {
  const conn = ctx.conns.get(p.conn);
  const link = conn ? linkOf(ctx.config, ctx.instance, conn, p.dir) : p.dir === 0 ? '→server' : 'server→';
  const state = STATES[p.state] ?? '?';
  const line =
    `${clock(ctx.startEpochMs + p.us / 1000)}  #${p.seq}  ${ctx.prefix}${who(conn, p.conn)}  ${link}  ${state}  ` +
    `${ctx.table.name(p.state, p.dir, p.id)}  ${p.size} B`;
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
    log(dim(`Same as: dev capture start --on ${targets.map((instance) => instance.name).join(',')} ${recordingFlags(rec)}`.trimEnd()));
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
  const what = nextJoin
    ? `waiting for the next login${players.length > 0 ? ` of ${players.join(', ')}` : ''}`
    : players.length > 0
      ? `recording ${players.join(', ')}`
      : 'recording all players';
  log(`${live ? 'Live session' : `Capture ${id}`} started on ${started.map((i) => i.name).join(', ')}: ${what}`);
  if (!live) {
    log(`  Stop: dev capture stop    Inspect: dev capture show ${id}`);
  }
  if (rec.watch || live) {
    await watchInstances(ctx, started, { ...values, player: players }, live ? new Map(started.map((i) => [i.name, id])) : new Map());
  }
}

async function stopCommand(ctx: Ctx, args: string[]): Promise<void> {
  const { values } = parseArgs({ args, options: { on: { type: 'string', multiple: true } } });
  const names = split(values.on);
  const candidates = names.length > 0 ? names.map((name) => instanceNamed(ctx.config, name)) : instancesOf(ctx.config).filter((i) => isRunning(i));
  let stopped = 0;
  for (const instance of candidates) {
    let state: any;
    try {
      state = await status(instance);
    } catch (error) {
      if (names.length > 0) throw error;
      continue;
    }
    if (!state.session) {
      if (names.length > 0) warn(`${instance.name} has no capture running`);
      continue;
    }
    const outputDir: string | undefined = state.session.spec.outputDir;
    const result = await request(socketOf(instance), instance.name, { cmd: 'stop' });
    stopped++;
    const size = outputDir ? `, ${formatBytes(sizeOnDisk(join(outputDir, `${instance.name}.bin.gz`)))} on disk` : ' (live only, nothing saved)';
    log(`${instance.name}: stopped (${result.reason}): ${result.packets} packets, ${formatBytes(result.bytes)}${size}${result.dropped > 0 ? `, ${result.dropped} DROPPED` : ''}`);
    if (result.dropped > 0) {
      warn(`${instance.name} dropped ${result.dropped} packets because the writer fell behind; raise capture.queue_capacity or narrow the capture`);
    }
  }
  if (stopped === 0) {
    log('No capture was running.');
  }
}

async function statusCommand(ctx: Ctx): Promise<void> {
  const running = instancesOf(ctx.config).filter((instance) => isRunning(instance));
  if (running.length === 0) {
    log('Nothing is running.');
    return;
  }
  for (const instance of running) {
    try {
      const state = await status(instance);
      const hints = state.protocolHints.complete ? `protocol ${state.protocolHints.protocol}` : 'protocol hints missing (run `dev capture protocol`, then restart)';
      log(`${instance.name} (${state.platform}): ready, ${hints}`);
      if (state.session) {
        const s = state.session;
        log(
          `  capture ${s.id}: ${s.waitingForJoin ? 'waiting for the next login, ' : ''}${s.packets} packets, ${formatBytes(s.bytes)}` +
            `${s.dropped > 0 ? `, ${s.dropped} dropped` : ''}${s.spec.outputDir ? '' : ' (live only)'}`,
        );
      }
      for (const c of state.connections.filter((conn: any) => !conn.ignored)) {
        log(`  #${c.id} ${c.leg === 1 ? 'backend ' : ''}${c.name ?? '(unidentified)'} ${STATES[c.state] ?? '?'} ${c.remote ?? ''}${c.recording ? '  [recording]' : ''}`);
      }
    } catch (error) {
      log(`${instance.name}: ${(error as Error).message.split('\n')[0]}`);
    }
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
    if (flags) log(dim(`Same as: dev capture watch --on ${targets.map((instance) => instance.name).join(',')} ${flags}`));
  }
  await watchInstances(ctx, targets, values, new Map());
}

// ---------------------------------------------------------------------------------------------------------------
// list / show / packet / export / rm
// ---------------------------------------------------------------------------------------------------------------

async function listCommand(): Promise<void> {
  const captures = await listCaptures();
  if (captures.length === 0) {
    log('No captures yet. Start one with `dev capture start`.');
    return;
  }
  const rows = [['ID', 'INSTANCES', 'PACKETS', 'SIZE', 'STOPPED BY']];
  for (const capture of captures) {
    const summary = await summarize(capture);
    rows.push([capture.meta.id, capture.meta.instances.join(','), String(summary.packets), formatBytes(summary.size), summary.reasons.join(',')]);
  }
  log(table(rows));
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
    if (flags) log(dim(`Same as: dev capture show ${capture.meta.id} ${flags}`));
  }
  const filter = parseFilter(filterValues);
  const limit = values.limit ? parseCount(values.limit, '--limit') : Infinity;
  const hexBytes = values.hex ? Math.min(256, parseCount(values.hex, '--hex')) : 0;
  for (const name of names) {
    const { file, instance, conns, startEpochMs, manifest } = await openDump(ctx, capture, name);
    if (names.length > 1) {
      log(`== ${name} ==`);
    }
    if (manifest?.dropped) {
      warn(`${name}: ${manifest.dropped} packets were dropped while capturing`);
    }
    const keep = compileFilter(filter, table_, conns, (conn, dir) => receiverOf(ctx.config, instance, conn, dir));
    const lineCtx: LineContext = { config: ctx.config, instance, table: table_, conns, startEpochMs, prefix: '' };
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
  const byType = new Map<string, { count: number; bytes: number }>();
  const byConn = new Map<number, { count: number; bytes: number }>();
  for (const p of packets) {
    const receiver = lineCtx.conns.get(p.conn) ? receiverOf(ctx.config, lineCtx.instance, lineCtx.conns.get(p.conn)!, p.dir) : '?';
    const key = `→${receiver}  ${lineCtx.table.name(p.state, p.dir, p.id)}`;
    for (const [map, k] of [[byType, key], [byConn, p.conn]] as const) {
      const entry = (map as Map<string | number, { count: number; bytes: number }>).get(k) ?? { count: 0, bytes: 0 };
      entry.count++;
      entry.bytes += p.frame.length;
      (map as Map<string | number, { count: number; bytes: number }>).set(k, entry);
    }
  }
  const rows = [['RECEIVER  PACKET', 'COUNT', 'BYTES', 'AVG']];
  for (const [key, e] of [...byType].sort((a, b) => b[1].bytes - a[1].bytes).slice(0, 60)) {
    rows.push([key, String(e.count), formatBytes(e.bytes), formatBytes(Math.round(e.bytes / e.count))]);
  }
  log(table(rows));
  log('');
  const connRows = [['CONNECTION', 'PACKETS', 'BYTES']];
  for (const [id, e] of byConn) {
    connRows.push([who(lineCtx.conns.get(id), id), String(e.count), formatBytes(e.bytes)]);
  }
  log(table(connRows));
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
      ? await askText('Packet number (the #n that `dev capture show` prints)', {
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
    log(formatPacket({ config: ctx.config, instance, table: table_, conns, startEpochMs, prefix: '' }, { ...p, size: p.frame.length }));
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
          Object.values(EXPORTERS).map((candidate) => ({ name: `${candidate.name}  ${candidate.summary}`, value: candidate.name })),
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
    log(dim(`Same as: dev capture export ${capture.meta.id} --format ${format.name}${instanceFlag} ${filterFlags(picked)}`.trimEnd()));
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
    log(`${name}:`);
    for (const line of lines) {
      log(`  ${line}`);
    }
  }
}

function formatsCommand(): void {
  log(table([['FORMAT', 'DESCRIPTION'], ...Object.values(EXPORTERS).map((e) => [e.name, e.summary])]));
}

async function rmCommand(ctx: Ctx, args: string[]): Promise<void> {
  const { values, positionals } = parseArgs({ args, options: { yes: { type: 'boolean' } }, allowPositionals: true });
  const captures = positionals.length > 0 ? await Promise.all(positionals.map((id) => loadCapture(id))) : await pickCaptures('Delete which captures? (space selects, enter confirms)');
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
  log(table([['STATE', 'SERVERBOUND', 'CLIENTBOUND'], ...protocol.counts().map((c) => [c.state, String(c.serverbound), String(c.clientbound)])]));
  log(`Hints for the plugin: ${hintsFile(protocol.minecraftVersion)} (read at server start)`);
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

function checkPlacement(handlers: string[]): string | undefined {
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
  return problems.length > 0 ? problems.join('; ') : 'OK: taps sit directly before the decoder and encoder';
}

async function doctorCommand(ctx: Ctx, args: string[]): Promise<void> {
  const { positionals } = parseArgs({ args, allowPositionals: true });
  const targets = await runningTargets(ctx, positionals, 'Check which instances?');
  const protocol = await ensureProtocol(ctx.config);
  let failed = false;
  for (const instance of targets) {
    log(`${instance.name} (${instance.platform})`);
    try {
      const state = await status(instance);
      const hints = state.protocolHints;
      log(`  control socket: ok, plugin on ${state.platform}`);
      if (!hints.complete) {
        log('  protocol hints: MISSING; run `dev capture protocol`, then restart this instance (states after login will not be tracked)');
        failed = true;
      } else if (hints.protocol !== protocol.protocolVersion) {
        log(`  protocol hints: STALE (plugin has ${hints.protocol}, current is ${protocol.protocolVersion}); restart this instance`);
        failed = true;
      } else {
        log(`  protocol hints: ok (${hints.protocol})`);
      }
      const result = await probe(instance, protocol);
      log(`  login probe: ${result.ok ? 'ok' : 'FAILED'}: ${result.detail}`);
      if (!result.ok) {
        failed = true;
        log('  fix: see PipelineLayout.java (paper, velocity), CapturePaper.java / CaptureVelocity.java (hooks), CaptureLimbo.java (limbo)');
      }
      if (result.handlers) {
        log(`  pipeline: ${result.handlers.join(' > ')}`);
        const placement = checkPlacement(result.handlers);
        if (placement) {
          log(`  placement: ${placement}`);
          failed ||= !placement.startsWith('OK');
        }
      }
    } catch (error) {
      failed = true;
      log(`  FAILED: ${(error as Error).message}`);
    }
  }
  if (failed) {
    fail('Some checks failed (see above).');
  }
  log('All checks passed.');
}

export const CAPTURE_COMMANDS: Command[] = [
  {
    name: 'capture start',
    usage: '[--on names] [--player p] [--next-join] [--duration 5m] [--packets N] [--until type] [--stop-on-disconnect] [--max-size 512M] [--label x] [--live|--watch]',
    summary: 'Start capturing packets on running instances; guided without options',
    run: startCommand,
  },
  { name: 'capture stop', usage: '[--on names]', summary: 'Stop the running captures', run: stopCommand },
  { name: 'capture status', usage: '', summary: 'Show capture state and live connections', run: statusCommand },
  {
    name: 'capture watch',
    usage: '[--on names] [--type t] [--dir sb|cb] [--to receiver] [--player p] [--state s] [--conn n] [--hex N]',
    summary: 'Follow packets live (guided filter without options; Ctrl-C ends)',
    run: watchCommand,
  },
  { name: 'capture list', usage: '', summary: 'List captures', run: listCommand },
  {
    name: 'capture show',
    usage: '[<id|latest>] [--instance x] [filters] [--limit N] [--hex N] [--stats]',
    summary: 'Print the packets of a capture, or statistics (guided without options)',
    run: showCommand,
  },
  {
    name: 'capture packet',
    usage: '[<id|latest> [<seq>]] [--instance x] [--raw file] [--no-id]',
    summary: 'Hex dump one packet, or write its bytes to a file',
    run: packetCommand,
  },
  {
    name: 'capture export',
    usage: '[<id|latest>] [--format name] [--instance x[,y]] [--out dir] [filters] [--opt k=v]',
    summary: 'Export a capture for other tools (asks for the format without --format)',
    run: exportCommand,
  },
  { name: 'capture formats', usage: '', summary: 'List export formats', run: formatsCommand },
  { name: 'capture rm', usage: '[<id...>] [--yes]', summary: 'Delete captures (pick from a menu without ids)', run: rmCommand },
  { name: 'capture protocol', usage: '[--refresh]', summary: 'Generate the packet table for the current Minecraft version', run: protocolCommand },
  {
    name: 'capture doctor',
    usage: '[names...]',
    summary: 'Check that capture works on each instance (login probe, handler placement)',
    run: doctorCommand,
  },
];

