import { checkbox, confirm, input, search, select } from '@inquirer/prompts';
import type { DevConfig } from './config.ts';
import { type Instance, instancesOf } from './templates.ts';
import { fail } from './util.ts';

let noInput = process.env.PARADISU_DEV_NO_INPUT === '1';

/** Called once by the CLI when `--no-input` is passed. */
export function disableInput(): void {
  noInput = true;
}

/** Prompts are only used on a real terminal, never in CI, scripts or with `--no-input`. */
export function interactive(): boolean {
  return !noInput && !process.env.CI && Boolean(process.stdin.isTTY && process.stdout.isTTY);
}

function requireInteractive(missing: string): void {
  if (!interactive()) {
    fail(`${missing} is required (prompts are disabled here: no terminal, CI, or --no-input)`);
  }
}

export interface Choice<T> {
  name: string;
  value: T;
  description?: string;
}

export async function pickFrom<T>(message: string, choices: Choice<T>[], missing = message): Promise<T> {
  requireInteractive(missing);
  if (choices.length === 0) {
    fail(`Nothing to choose from for: ${message}`);
  }
  return select({ message, choices });
}

export async function pickMany<T>(
  message: string,
  choices: (Choice<T> & { checked?: boolean })[],
  missing = message,
): Promise<T[]> {
  requireInteractive(missing);
  return checkbox({ message, choices });
}

export async function askText(
  message: string,
  options: { default?: string; validate?: (value: string) => true | string } = {},
  missing = message,
): Promise<string> {
  requireInteractive(missing);
  return input({ message, default: options.default, validate: options.validate });
}

/** Free-text search over a list that may be large (players, files). */
export async function searchFrom<T>(
  message: string,
  source: (term: string) => Choice<T>[] | Promise<Choice<T>[]>,
  missing = message,
): Promise<T> {
  requireInteractive(missing);
  return search({ message, source: async (term) => source(term ?? '') });
}

/** Asks for confirmation; without a terminal it refuses unless `--yes` was given. */
export async function confirmAction(question: string, assumeYes: boolean, fallback = false): Promise<boolean> {
  if (assumeYes) {
    return true;
  }
  if (!interactive()) {
    fail(`${question}\nRefusing to continue without confirmation; pass --yes.`);
  }
  return confirm({ message: question, default: fallback });
}

/** A yes/no question that defaults silently to `fallback` when no terminal is available. */
export async function askOptional(question: string, fallback: boolean): Promise<boolean> {
  return interactive() ? confirm({ message: question, default: fallback }) : fallback;
}

export function instanceChoices(config: DevConfig, filter: (instance: Instance) => boolean = () => true) {
  return instancesOf(config)
    .filter(filter)
    .map((instance) => ({ name: instance.name, value: instance.name }));
}

export async function pickInstance(
  config: DevConfig,
  message: string,
  filter?: (instance: Instance) => boolean,
): Promise<string> {
  return pickFrom(message, instanceChoices(config, filter), 'An instance name (proxy, limbo or a server name)');
}

export async function pickInstances(
  config: DevConfig,
  message: string,
  isChecked: (instance: Instance) => boolean,
): Promise<string[]> {
  return pickMany(
    message,
    instancesOf(config).map((instance) => ({ name: instance.name, value: instance.name, checked: isChecked(instance) })),
  );
}

/** Ctrl-C in a prompt rejects with this error; the CLI exits quietly on it. */
export function isPromptExit(error: unknown): boolean {
  return error instanceof Error && error.name === 'ExitPromptError';
}
