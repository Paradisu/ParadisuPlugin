import type { DevConfig } from './config.ts';

export interface Ctx {
  config: DevConfig;
  /** Stops every running instance; used before anything that replaces data under them */
  stopAll(): Promise<void>;
}

export interface Command {
  /** One or two words: `start`, `db reset` */
  name: string;
  /** Arguments part of the help line */
  usage: string;
  summary: string;
  /** Not listed in help or the menu */
  hidden?: boolean;
  run(ctx: Ctx, args: string[]): Promise<void> | void;
}
