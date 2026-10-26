import type { DevConfig } from './config.ts';

export interface Ctx {
  config: DevConfig;
  /** Stops every running instance; used before anything that replaces data under them */
  stopAll(): Promise<void>;
}

export interface OptionDoc {
  /** `--through <00N>`, `-f, --follow` */
  flag: string;
  summary: string;
}

export interface Command {
  /** One or two words: `start`, `db reset` */
  name: string;
  /** Positionals only: `<name>`, `[names...]`. `[options]` is appended automatically when `options` is set */
  usage: string;
  /** At most 60 characters, imperative, no trailing period */
  summary: string;
  /** Extra paragraph for `dev help <command>` */
  details?: string;
  options?: OptionDoc[];
  /** Arguments go to another program untouched (`db psql`), so `--help` is not intercepted */
  passthrough?: boolean;
  /** Not listed in help or the menu */
  hidden?: boolean;
  run(ctx: Ctx, args: string[]): Promise<void> | void;
}
