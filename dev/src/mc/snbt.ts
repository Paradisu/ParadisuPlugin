import { DevError } from '../util.ts';
import type { Tag, TagType } from './nbt.ts';

const UNQUOTED_KEY = /^[A-Za-z0-9._+-]+$/;

function quote(text: string): string {
  return `"${text.replaceAll('\\', '\\\\').replaceAll('"', '\\"')}"`;
}

function number(value: number, suffix: string): string {
  if (Number.isNaN(value) || !Number.isFinite(value)) {
    return `${value > 0 ? '' : value < 0 ? '-' : ''}${Number.isNaN(value) ? 'NaN' : 'Infinity'}${suffix}`;
  }
  const text = String(value);
  return `${/[.eE]/.test(text) ? text : `${text}.0`}${suffix}`;
}

export interface StringifyOptions {
  pretty?: boolean;
}

/** Formats a tag as SNBT, the syntax of `/give` and `/data`. */
export function stringify(tag: Tag, options: StringifyOptions = {}, indent = ''): string {
  const pretty = options.pretty ?? false;
  const inner = `${indent}  `;
  switch (tag.type) {
    case 'byte':
      return `${tag.value}b`;
    case 'short':
      return `${tag.value}s`;
    case 'int':
      return String(tag.value);
    case 'long':
      return `${tag.value}L`;
    case 'float':
      return number(tag.value, 'f');
    case 'double':
      return number(tag.value, 'd');
    case 'string':
      return quote(tag.value);
    case 'byteArray':
      return `[B;${Array.from(tag.value, (v) => `${v}b`).join(pretty ? ', ' : ',')}]`;
    case 'intArray':
      return `[I;${Array.from(tag.value).join(pretty ? ', ' : ',')}]`;
    case 'longArray':
      return `[L;${Array.from(tag.value, (v) => `${v}L`).join(pretty ? ', ' : ',')}]`;
    case 'list': {
      const parts = tag.value.map((child) => stringify(child, options, inner));
      const scalar = tag.value.every((child) => child.type !== 'compound' && child.type !== 'list');
      if (!pretty || scalar || parts.length === 0) {
        return `[${parts.join(pretty ? ', ' : ',')}]`;
      }
      return `[\n${parts.map((part) => `${inner}${part}`).join(',\n')}\n${indent}]`;
    }
    case 'compound': {
      const entries = Object.entries(tag.value).map(
        ([key, child]) => `${UNQUOTED_KEY.test(key) ? key : quote(key)}:${pretty ? ' ' : ''}${stringify(child, options, inner)}`,
      );
      if (!pretty || entries.length === 0) {
        return `{${entries.join(',')}}`;
      }
      return `{\n${entries.map((entry) => `${inner}${entry}`).join(',\n')}\n${indent}}`;
    }
  }
}

// ---------------------------------------------------------------------------------------------------------------
// Parsing
// ---------------------------------------------------------------------------------------------------------------

const UNQUOTED = /[0-9A-Za-z_\-.+]/;

class Parser {
  pos = 0;
  readonly text: string;
  constructor(text: string) {
    this.text = text;
  }

  error(message: string): never {
    throw new DevError(`Invalid SNBT at column ${this.pos + 1}: ${message}\n  ${this.text}\n  ${' '.repeat(this.pos)}^`);
  }

  ws(): void {
    while (/\s/.test(this.text[this.pos] ?? '')) this.pos++;
  }

  peek(): string | undefined {
    this.ws();
    return this.text[this.pos];
  }

  expect(char: string): void {
    if (this.peek() !== char) this.error(`expected "${char}"`);
    this.pos++;
  }

  value(): Tag {
    const c = this.peek();
    if (c === undefined) this.error('unexpected end of input');
    if (c === '{') return this.compound();
    if (c === '[') return this.list();
    if (c === '"' || c === "'") return { type: 'string', value: this.quoted() };
    return this.scalar();
  }

  quoted(): string {
    const quoteChar = this.text[this.pos++]!;
    let out = '';
    for (;;) {
      const c = this.text[this.pos++];
      if (c === undefined) this.error('unterminated string');
      if (c === quoteChar) return out;
      if (c === '\\') {
        const next = this.text[this.pos++];
        if (next === undefined) this.error('unterminated escape');
        out += { n: '\n', t: '\t', r: '\r', b: '\b', f: '\f' }[next as 'n'] ?? next;
      } else {
        out += c;
      }
    }
  }

  key(): string {
    const c = this.peek();
    if (c === '"' || c === "'") return this.quoted();
    const start = this.pos;
    while (UNQUOTED.test(this.text[this.pos] ?? '')) this.pos++;
    if (start === this.pos) this.error('expected a key');
    return this.text.slice(start, this.pos);
  }

  compound(): Tag {
    this.expect('{');
    const value: Record<string, Tag> = {};
    if (this.peek() === '}') {
      this.pos++;
      return { type: 'compound', value };
    }
    for (;;) {
      const name = this.key();
      this.expect(':');
      value[name] = this.value();
      const c = this.peek();
      this.pos++;
      if (c === '}') return { type: 'compound', value };
      if (c !== ',') {
        this.pos--;
        this.error('expected "," or "}"');
      }
    }
  }

  list(): Tag {
    this.expect('[');
    const typed = /^[BIL];/.exec(this.text.slice(this.pos));
    if (typed) {
      this.pos += 2;
      return this.typedArray(typed[0][0] as 'B' | 'I' | 'L');
    }
    const value: Tag[] = [];
    if (this.peek() === ']') {
      this.pos++;
      return { type: 'list', elementType: 'end', value };
    }
    for (;;) {
      const element = this.value();
      if (value.length > 0 && element.type !== value[0]!.type) {
        this.error(`list elements must share a type (found ${element.type} after ${value[0]!.type})`);
      }
      value.push(element);
      const c = this.peek();
      this.pos++;
      if (c === ']') return { type: 'list', elementType: value[0]!.type as TagType, value };
      if (c !== ',') {
        this.pos--;
        this.error('expected "," or "]"');
      }
    }
  }

  typedArray(kind: 'B' | 'I' | 'L'): Tag {
    const wanted = { B: 'byte', I: 'int', L: 'long' }[kind];
    const items: Tag[] = [];
    if (this.peek() === ']') {
      this.pos++;
    } else {
      for (;;) {
        const element = this.value();
        if (element.type !== wanted) this.error(`[${kind};…] elements must be ${wanted}s (found ${element.type})`);
        items.push(element);
        const c = this.peek();
        this.pos++;
        if (c === ']') break;
        if (c !== ',') {
          this.pos--;
          this.error('expected "," or "]"');
        }
      }
    }
    if (kind === 'B') return { type: 'byteArray', value: Int8Array.from(items, (t) => t.value as number) };
    if (kind === 'I') return { type: 'intArray', value: Int32Array.from(items, (t) => t.value as number) };
    return { type: 'longArray', value: BigInt64Array.from(items, (t) => t.value as bigint) };
  }

  scalar(): Tag {
    const start = this.pos;
    while (UNQUOTED.test(this.text[this.pos] ?? '')) this.pos++;
    const token = this.text.slice(start, this.pos);
    if (token === '') this.error('unexpected character');
    if (token === 'true') return { type: 'byte', value: 1 };
    if (token === 'false') return { type: 'byte', value: 0 };
    let m: RegExpExecArray | null;
    if ((m = /^([-+]?\d+)[bB]$/.exec(token))) return { type: 'byte', value: this.ranged(m[1]!, -128, 127, 'byte') };
    if ((m = /^([-+]?\d+)[sS]$/.exec(token))) return { type: 'short', value: this.ranged(m[1]!, -32768, 32767, 'short') };
    if ((m = /^([-+]?\d+)[lL]$/.exec(token))) return { type: 'long', value: BigInt(m[1]!) };
    if (/^[-+]?\d+$/.test(token)) return { type: 'int', value: this.ranged(token, -2147483648, 2147483647, 'int') };
    if ((m = /^([-+]?(?:\d+\.?\d*|\.\d+)(?:[eE][-+]?\d+)?)[fF]$/.exec(token))) return { type: 'float', value: Number(m[1]) };
    if ((m = /^([-+]?(?:\d+\.?\d*|\.\d+)(?:[eE][-+]?\d+)?)[dD]?$/.exec(token))) {
      if (/[.eEdD]/.test(token)) return { type: 'double', value: Number(m[1]) };
    }
    return { type: 'string', value: token };
  }

  ranged(text: string, min: number, max: number, name: string): number {
    const value = Number(text);
    if (value < min || value > max) this.error(`${text} is out of range for a ${name}`);
    return value;
  }
}

/** Parses SNBT (as used by `/give` and `/data`) into a tag tree. */
export function parse(text: string): Tag {
  const parser = new Parser(text);
  const tag = parser.value();
  if (parser.peek() !== undefined) parser.error('unexpected trailing input');
  return tag;
}
