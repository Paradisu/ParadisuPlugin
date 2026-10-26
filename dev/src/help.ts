import type { Command } from './command.ts';
import { bold, columns, dim } from './output.ts';

interface Section {
  title: string;
  prefixes: string[];
}

const SECTIONS: Section[] = [
  { title: 'Network', prefixes: ['bootstrap', 'setup', 'build', 'start', 'stop', 'restart', 'status', 'console', 'logs', 'reset-world', 'clean'] },
  { title: 'Consoles', prefixes: ['ui', 'web'] },
  { title: 'Database', prefixes: ['db'] },
  { title: 'Sharing', prefixes: ['share'] },
  { title: 'Packet capture', prefixes: ['capture'] },
  { title: 'Profiles', prefixes: ['profile', 'override'] },
  { title: 'Minecraft data', prefixes: ['mc'] },
];

const OTHER = 'Other';

/** First word of a command name: its group when the name has two words. */
export function groupOf(command: Command): string | undefined {
  return command.name.includes(' ') ? command.name.split(' ')[0] : undefined;
}

function sectionOf(command: Command): string {
  const first = command.name.split(' ')[0]!;
  return SECTIONS.find((section) => section.prefixes.includes(first))?.title ?? OTHER;
}

function signature(command: Command): string {
  return [command.name, command.usage, command.options?.length ? '[options]' : ''].filter(Boolean).join(' ');
}

/** `dev help`: commands under section headings, or only the commands of one group. */
export function usage(commands: Command[], group?: string): string {
  const shown = commands.filter((command) => !group || groupOf(command) === group);
  const titles = [...SECTIONS.map((section) => section.title), OTHER];
  const blocks: string[] = [];
  for (const title of titles) {
    const inSection = shown.filter((command) => sectionOf(command) === title);
    if (inSection.length === 0) continue;
    const lines = columns(inSection.map((command) => [signature(command), dim(command.summary)]), [], 2);
    blocks.push(`${bold(title)}\n${lines.join('\n')}`);
  }
  const heading = `Usage: dev <command> [options]${group ? ` (${group})` : ''}`;
  return `${heading}\n\n${blocks.join('\n\n')}\n\n${dim('Run dev help <command> for options.')}\n`;
}

/** `dev help <command>` and `dev <command> --help`. */
export function commandHelp(command: Command): string {
  const lines = [`Usage: dev ${signature(command)}`, '', command.summary];
  if (command.details) lines.push(command.details);
  const rows = [...(command.options ?? []), ...(command.passthrough ? [] : [{ flag: '--no-input', summary: 'Never prompt' }])];
  if (rows.length > 0) {
    lines.push('', bold('Options'), ...columns(rows.map((option) => [option.flag, dim(option.summary)]), [], 2));
  }
  return `${lines.join('\n')}\n`;
}

/** Aligned menu labels: top level shows commands and groups, a group shows its own commands. */
export function menuChoices(commands: Command[], group?: string): { name: string; value: string }[] {
  const entries: { value: string; label: string; summary: string }[] = [];
  const seen = new Set<string>();
  for (const command of commands.filter((candidate) => !group || groupOf(candidate) === group)) {
    const owner = groupOf(command);
    if (!group && owner) {
      if (seen.has(owner)) continue;
      seen.add(owner);
      const count = commands.filter((candidate) => groupOf(candidate) === owner).length;
      entries.push({ value: owner, label: `${owner}…`, summary: `${sectionOf(command)}, ${count} commands` });
    } else {
      entries.push({ value: command.name, label: group ? command.name.slice(group.length + 1) : command.name, summary: command.summary });
    }
  }
  const labels = columns(entries.map((entry) => [entry.label, dim(entry.summary)]));
  return entries.map((entry, i) => ({ name: labels[i]!, value: entry.value }));
}
