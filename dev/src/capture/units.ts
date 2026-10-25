import { DevError } from '../util.ts';

/** A duration such as `30s`, `5m`, `1h` or `250ms` (plain numbers are seconds), in milliseconds. */
export function parseSpan(text: string): number {
  const match = /^(\d+)(ms|s|m|h)?$/.exec(text.trim());
  if (!match) {
    throw new DevError(`"${text}" is not a duration (examples: 30s, 5m, 1h)`);
  }
  const unit = { ms: 1, s: 1000, m: 60_000, h: 3_600_000 }[(match[2] ?? 's') as 'ms' | 's' | 'm' | 'h'];
  return Number(match[1]) * unit;
}

/** A size such as `512K`, `64M` or `2G` (plain numbers are bytes), in bytes. */
export function parseSize(text: string): number {
  const match = /^(\d+)([KMG])?B?$/i.exec(text.trim());
  if (!match) {
    throw new DevError(`"${text}" is not a size (examples: 512K, 64M, 2G)`);
  }
  const unit = { '': 1, K: 1024, M: 1024 ** 2, G: 1024 ** 3 }[(match[2] ?? '').toUpperCase() as '' | 'K' | 'M' | 'G'];
  return Number(match[1]) * unit;
}

export function parseCount(text: string, flag: string): number {
  const value = Number(text);
  if (!Number.isInteger(value) || value <= 0) {
    throw new DevError(`${flag} must be a positive whole number`);
  }
  return value;
}

/** Repeatable and comma separated flag values, flattened. */
export function split(values: string[] | undefined): string[] {
  return (values ?? []).flatMap((value) => value.split(',')).map((value) => value.trim()).filter(Boolean);
}

/** `true` for a prompt's validate callback, or the reason the text is not accepted. */
export function validator(check: (text: string) => unknown): (text: string) => true | string {
  return (text) => {
    try {
      check(text);
      return true;
    } catch (error) {
      return (error as Error).message;
    }
  };
}
