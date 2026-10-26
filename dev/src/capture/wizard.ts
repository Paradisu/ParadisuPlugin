import { type Choice, askOptional, askText, pickFrom, pickMany, searchFrom } from '../prompt.ts';
import { fail } from '../util.ts';
import { FILTER_OPTIONS, type FilterValues } from './filters.ts';
import { type ProtocolTable, STATES } from './protocol.ts';
import { type Capture, formatBytes, listCaptures, dumpInstances, summarize } from './store.ts';
import { parseCount, parseSize, parseSpan, split, validator } from './units.ts';

/**
 * Interactive builders for `dev capture`: pick a capture, build a filter, describe a recording. They only run on a
 * terminal, and only when the command was given none of the options they ask for, so scripts and anyone who knows
 * the flags are never prompted. Each returns the same values the flags would, so the command can print the
 * equivalent command line.
 */

// ---------------------------------------------------------------------------------------------------------------
// Captures and instances
// ---------------------------------------------------------------------------------------------------------------

async function captureChoices(): Promise<Choice<Capture>[]> {
  const choices: Choice<Capture>[] = [];
  for (const capture of await listCaptures()) {
    const summary = await summarize(capture);
    const who = capture.meta.nextJoin ? `next join of ${capture.meta.players.join(', ') || 'anyone'}` : capture.meta.players.join(', ') || 'all players';
    choices.push({
      name: `${capture.meta.id}  ${capture.meta.instances.join(',')}  ${summary.packets} packets  ${formatBytes(summary.size)}  (${who})`,
      value: capture,
    });
  }
  return choices;
}

export async function pickCapture(message: string): Promise<Capture> {
  const choices = await captureChoices();
  if (choices.length === 0) {
    fail('There are no captures yet. Start one with `dev capture start`.');
  }
  return pickFrom(message, choices, 'A capture id (see `dev capture list`)');
}

export async function pickCaptures(message: string): Promise<Capture[]> {
  const choices = await captureChoices();
  if (choices.length === 0) {
    fail('There are no captures yet. Start one with `dev capture start`.');
  }
  return pickMany(message, choices.map((choice) => ({ ...choice, checked: false })), 'A capture id (see `dev capture list`)');
}

export async function pickInstanceOf(capture: Capture, message: string): Promise<string> {
  return pickFrom(
    message,
    dumpInstances(capture).map((name) => ({ name, value: name })),
    'An --instance (this capture has several)',
  );
}

export async function pickInstancesOf(capture: Capture, message: string): Promise<string[]> {
  const picked = await pickMany(
    message,
    dumpInstances(capture).map((name) => ({ name, value: name, checked: true })),
    'An --instance',
  );
  if (picked.length === 0) {
    fail('No instance selected');
  }
  return picked;
}

// ---------------------------------------------------------------------------------------------------------------
// Filters
// ---------------------------------------------------------------------------------------------------------------

export const FILTER_KEYS = Object.keys(FILTER_OPTIONS) as (keyof typeof FILTER_OPTIONS)[];

/** Whether any filter flag was given, so the wizard stays out of the way. */
export function hasFilterFlags(values: FilterValues): boolean {
  return FILTER_KEYS.some((key) => {
    const value = values[key];
    return Array.isArray(value) ? value.length > 0 : value !== undefined;
  });
}

export interface FilterSource {
  table: ProtocolTable;
  /** Who can receive packets here: `client`, `proxy`, instance names */
  receivers: string[];
  /** Players known right now (connected, or in the capture) */
  players: string[];
  /** Connections to choose from, when they are unambiguous (one instance) */
  conns?: { id: number; label: string }[];
}

function quoted(text: string): string {
  return /^[\w.,:/@=+-]+$/.test(text) ? text : `'${text.replaceAll("'", "'\\''")}'`;
}

/** The flags that make the same filter, to teach them. */
export function filterFlags(values: FilterValues): string {
  const parts: string[] = [];
  if (values.type?.length) parts.push(`--type ${quoted(values.type.join(','))}`);
  if (values.dir) parts.push(`--dir ${values.dir}`);
  if (values.to?.length) parts.push(`--to ${quoted(values.to.join(','))}`);
  if (values.player?.length) parts.push(`--player ${quoted(values.player.join(','))}`);
  if (values.state) parts.push(`--state ${values.state}`);
  if (values.conn?.length) parts.push(`--conn ${values.conn.join(',')}`);
  return parts.join(' ');
}

const DONE = '\u0000done';

/** Search-as-you-type picker for packet types; repeats until "Done". Wildcards (`move_*`) can be picked as a pattern. */
async function askTypes(table: ProtocolTable): Promise<string[]> {
  const listing = table.listing();
  const picked: string[] = [];
  while (true) {
    const choice = await searchFrom<string>(
      picked.length > 0
        ? `Packet types, ${picked.length} chosen (${picked.slice(-3).join(', ')}): search for more, or pick Done`
        : 'Packet types: type to search (a * works as a wildcard, for example move_*)',
      (term) => {
        const needle = term.trim().toLowerCase().replace(/^minecraft:/, '');
        const choices: Choice<string>[] = [];
        if (picked.length > 0) {
          choices.push({ name: `✔ Done (${picked.length} chosen)`, value: DONE });
        }
        if (needle.includes('*')) {
          const matches = table.keys(needle).length;
          if (matches > 0) {
            choices.push({ name: `pattern ${needle}  (${matches} packet types)`, value: needle });
          }
        }
        for (const entry of listing) {
          if (choices.length >= 30) break;
          if (!picked.includes(entry.pattern) && entry.pattern.includes(needle)) {
            const dirs = entry.dirs.length === 2 ? 'both ways' : entry.dirs[0] === 0 ? 'serverbound' : 'clientbound';
            choices.push({ name: `${entry.pattern}  (${dirs})`, value: entry.pattern });
          }
        }
        return choices;
      },
    );
    if (choice === DONE) {
      return picked;
    }
    picked.push(choice);
  }
}

/** Players to pick from the known ones, plus anything typed. */
export async function askPlayers(message: string, known: string[], required: boolean): Promise<string[]> {
  const picked = known.length > 0 ? await pickMany(message, known.map((name) => ({ name, value: name }))) : [];
  const typed = await askText(
    known.length > 0 ? 'Other players, names or UUIDs separated by commas (empty for none)' : `${message} (names or UUIDs, separated by commas)`,
    { default: '', validate: (text) => (required && picked.length === 0 && !text.trim() ? 'Enter at least one player' : true) },
  );
  return [...new Set([...picked, ...split([typed])])];
}

/**
 * Asks which filters to apply and returns them as flag values (empty when declined). `watch`, `show` and `export`
 * share it.
 */
export async function askFilter(source: FilterSource): Promise<FilterValues> {
  if (!(await askOptional('Narrow down the packets with a filter?', false))) {
    return {};
  }
  const parts = await pickMany(
    'Filter by (space toggles, enter confirms)',
    [
      { name: 'Packet type', value: 'type' },
      { name: 'Direction (serverbound or clientbound)', value: 'dir' },
      { name: 'Receiver (client, proxy or a server)', value: 'to' },
      { name: 'Player', value: 'player' },
      { name: 'Connection state (handshake, login, configuration, play)', value: 'state' },
      ...(source.conns && source.conns.length > 0 ? [{ name: 'One connection', value: 'conn' }] : []),
    ],
    'Filter flags',
  );
  const values: FilterValues = {};
  if (parts.includes('type')) {
    values.type = await askTypes(source.table);
  }
  if (parts.includes('dir')) {
    values.dir = await pickFrom('Direction', [
      { name: 'Serverbound (towards the server)', value: 'sb' },
      { name: 'Clientbound (towards the client)', value: 'cb' },
    ]);
  }
  if (parts.includes('to')) {
    values.to = await pickMany('Only packets received by', source.receivers.map((name) => ({ name, value: name })));
  }
  if (parts.includes('player')) {
    values.player = await askPlayers('Only these players', source.players, true);
  }
  if (parts.includes('state')) {
    values.state = await pickFrom('Connection state', STATES.map((name) => ({ name, value: name })));
  }
  if (parts.includes('conn') && source.conns) {
    values.conn = (await pickMany('Which connections', source.conns.map((conn) => ({ name: conn.label, value: String(conn.id) })))).map(String);
  }
  return values;
}

// ---------------------------------------------------------------------------------------------------------------
// Recording
// ---------------------------------------------------------------------------------------------------------------

export interface RecordingChoices {
  players: string[];
  nextJoin: boolean;
  duration?: string;
  packets?: string;
  maxSize?: string;
  until: string[];
  stopOnDisconnect?: boolean;
  label?: string;
  watch: boolean;
}

const RECORDING_KEYS = ['player', 'next-join', 'duration', 'packets', 'until', 'stop-on-disconnect', 'max-size', 'label', 'live', 'watch'] as const;

/** Whether any `capture start` recording option was given. */
export function hasRecordingFlags(values: Partial<Record<(typeof RECORDING_KEYS)[number], unknown>>): boolean {
  return RECORDING_KEYS.some((key) => {
    const value = values[key];
    return Array.isArray(value) ? value.length > 0 : value !== undefined && value !== false;
  });
}

/** Walks through what to record and when to stop. */
export async function askRecording(knownPlayers: string[], table: ProtocolTable): Promise<RecordingChoices> {
  const who = await pickFrom('Whose packets?', [
    { name: 'Everyone', value: 'all' },
    { name: 'Specific players', value: 'players' },
    { name: 'The next player to log in (just that connection, from its handshake)', value: 'next' },
    { name: 'The next login of a specific player', value: 'next-player' },
  ]);
  const players = who === 'players' || who === 'next-player' ? await askPlayers('Which players', knownPlayers, true) : [];
  const stops = await pickMany('Stop automatically when (none: until `dev capture stop`)', [
    { name: 'a time has passed', value: 'duration' },
    { name: 'a number of packets has been recorded', value: 'packets' },
    { name: 'the capture reaches a size', value: 'max-size' },
    { name: 'a certain packet type shows up', value: 'until' },
    { name: 'the last recorded player disconnects', value: 'stop-on-disconnect' },
  ]);
  const choices: RecordingChoices = { players, nextJoin: who === 'next' || who === 'next-player', until: [], watch: false };
  if (stops.includes('duration')) {
    choices.duration = await askText('How long (for example 30s, 5m, 1h)', { default: '1m', validate: validator(parseSpan) });
  }
  if (stops.includes('packets')) {
    choices.packets = await askText('How many packets', { default: '10000', validate: validator((text) => parseCount(text, 'The packet count')) });
  }
  if (stops.includes('max-size')) {
    choices.maxSize = await askText('Size of the captured data before compression (for example 64M, 2G)', {
      default: '64M',
      validate: validator(parseSize),
    });
  }
  if (stops.includes('until')) {
    choices.until = await askTypes(table);
  }
  choices.stopOnDisconnect = stops.includes('stop-on-disconnect');
  const label = (await askText('Label to put in the capture id (optional)', { default: '' })).trim();
  choices.label = label || undefined;
  choices.watch = await askOptional('Watch the packets live while recording?', false);
  return choices;
}

/** The `capture start` flags that make the same recording. */
export function recordingFlags(choices: RecordingChoices): string {
  const parts: string[] = [];
  if (choices.players.length > 0) parts.push(`--player ${quoted(choices.players.join(','))}`);
  if (choices.nextJoin) parts.push('--next-join');
  if (choices.duration) parts.push(`--duration ${choices.duration}`);
  if (choices.packets) parts.push(`--packets ${choices.packets}`);
  if (choices.maxSize) parts.push(`--max-size ${choices.maxSize}`);
  if (choices.until.length > 0) parts.push(`--until ${quoted(choices.until.join(','))}`);
  if (choices.stopOnDisconnect) parts.push('--stop-on-disconnect');
  if (choices.label) parts.push(`--label ${quoted(choices.label)}`);
  if (choices.watch) parts.push('--watch');
  return parts.join(' ');
}
