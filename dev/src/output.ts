import { resolve } from 'node:path';
import { stripVTControlCharacters, styleText } from 'node:util';

type Paint = Parameters<typeof styleText>[0];

/** Styles text; escapes are dropped automatically for non-TTY streams, NO_COLOR and TERM=dumb. */
export function paint(format: Paint, text: string, stream: NodeJS.WriteStream = process.stdout): string {
  return styleText(format, text, { stream });
}

export const dim = (text: string): string => paint('dim', text);
export const bold = (text: string): string => paint('bold', text);
export const green = (text: string): string => paint('green', text);
export const yellow = (text: string): string => paint('yellow', text);
export const red = (text: string): string => paint('red', text);

/** Visible width: ignores ANSI escapes, counts code points. */
export function width(text: string): number {
  return [...stripVTControlCharacters(text)].length;
}

export type Align = 'left' | 'right';

/** Pads by visible width. */
export function pad(text: string, target: number, align: Align = 'left'): string {
  const fill = ' '.repeat(Math.max(0, target - width(text)));
  return align === 'right' ? fill + text : text + fill;
}

export interface Column {
  header: string;
  align?: Align;
}

function layout(rows: string[][], aligns: Align[], indent: number): string[] {
  const count = Math.max(0, ...rows.map((row) => row.length));
  const widths = Array.from({ length: count }, (_, c) => Math.max(0, ...rows.map((row) => width(row[c] ?? ''))));
  const prefix = ' '.repeat(indent);
  return rows.map((row) =>
    (prefix + widths.map((w, c) => pad(row[c] ?? '', w, aligns[c] ?? 'left')).join('  ')).trimEnd(),
  );
}

/** Aligned table with a bold uppercase header; `indent` prefixes every line. */
export function table(cols: (string | Column)[], rows: string[][], options: { indent?: number } = {}): string {
  const specs = cols.map((col) => (typeof col === 'string' ? { header: col } : col) as Column);
  const aligns = specs.map((col) => col.align ?? 'left');
  const headerCells = specs.map((col) => col.header.toUpperCase());
  const lines = layout([headerCells, ...rows], aligns, options.indent ?? 0);
  // layout pads by visible width, so bolding after layout keeps columns aligned
  lines[0] = bold(lines[0]!);
  return lines.join('\n');
}

/** Aligned rows without a header, one string per row (also used for prompt choice labels). */
export function columns(rows: string[][], aligns: Align[] = [], indent = 0): string[] {
  return layout(rows, aligns, indent);
}

/** `label  value` block with dim labels padded to the longest. */
export function keyValue(rows: [string, string][], options: { indent?: number } = {}): string {
  const labelWidth = Math.max(0, ...rows.map(([label]) => width(label)));
  const prefix = ' '.repeat(options.indent ?? 0);
  return rows.map(([label, value]) => `${prefix}${dim(pad(label, labelWidth))}  ${value}`.trimEnd()).join('\n');
}

/** Column widths that grow as rows stream in (packet lines, progress). Pre-seed with known widths via `min`. */
export function streamRow(cols: { min?: number; align?: Align }[]): (cells: string[]) => string {
  const widths = cols.map((col) => col.min ?? 0);
  return (cells) => {
    cells.forEach((cell, c) => {
      widths[c] = Math.max(widths[c] ?? 0, width(cell));
    });
    return cells
      .map((cell, c) => pad(cell, widths[c] ?? 0, cols[c]?.align ?? 'left'))
      .join('  ')
      .trimEnd();
  };
}

const STATUS_COLORS: Record<string, (text: string) => string> = {
  running: green,
  ok: green,
  live: green,
  open: green,
  starting: yellow,
  stale: yellow,
  missing: yellow,
  failed: red,
  stopped: dim,
  disabled: dim,
  off: dim,
  closed: dim,
};

/** Colors a status word (running, ok, failed, ...); unknown words are unchanged. */
export function status(word: string): string {
  return (STATUS_COLORS[word] ?? ((text: string) => text))(word);
}

/** Heading for a phase of work. */
export function step(text: string): void {
  console.log(bold(text));
}

/** One dim line of suggested follow-up commands. */
export function next(...commands: string[]): void {
  console.log(dim(`Next: ${commands.join(' · ')}`));
}

/** Teaches the flags equivalent to a guided choice. */
export function sameAs(command: string): void {
  console.log(dim(`Same as: ${command}`));
}

export function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KiB`;
  if (bytes < 1024 ** 3) return `${(bytes / 1024 ** 2).toFixed(1)} MiB`;
  return `${(bytes / 1024 ** 3).toFixed(2)} GiB`;
}

/** 45s, 12m, 1h 05m */
export function formatDuration(ms: number): string {
  const seconds = Math.max(0, Math.round(ms / 1000));
  if (seconds < 60) return `${seconds}s`;
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m`;
  return `${Math.floor(minutes / 60)}h ${String(minutes % 60).padStart(2, '0')}m`;
}

const ROOT_DIR = resolve(import.meta.dirname, '..', '..');

/** Repo-relative path for display. */
export function relPath(absolute: string): string {
  return absolute.startsWith(`${ROOT_DIR}/`) ? absolute.slice(ROOT_DIR.length + 1) : absolute;
}
