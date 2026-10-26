# Working on the `dev` tool

For changes to the `dev` CLI itself (`dev/`). To *use* it, see the [dev environment](dev-environment.md).

- [Layout](#layout)
- [Rules](#rules)
- [Adding a command](#adding-a-command)
- [Output style](#output-style)
- [Extension points](#extension-points)

## Layout

TypeScript in `dev/src`, run directly by Node 24 (`dev/dev` is the bash wrapper installed as `dev`).

| Path | Role |
| --- | --- |
| `cli.ts` | Command registry and `main()`; network commands (`start`, `console`, `logs`, ...) |
| `command.ts` | The `Command` and `OptionDoc` types |
| `help.ts` | `dev help` text, generated from the registry; `SECTIONS` groups commands by name prefix |
| `prompt.ts` | Every prompt; decides when to stay silent (`--no-input`, `CI`, `PARADISU_DEV_NO_INPUT`, no TTY) |
| `output.ts` | All printing helpers ([Output style](#output-style)) |
| `util.ts` | Paths (`ROOT`, `DEV`, `RUN`), `fail`, `log`, `warn`, process and download helpers |
| `config.ts`, `versions.ts`, `downloads.ts` | `dev.toml` loading and merging, version resolution, plugin downloads |
| `templates.ts`, `instances.ts` | Rendering `dev/templates` into `dev/run`, starting, stopping and listing instances |
| `supervisor.ts` | Per-instance supervisor and console socket (protocol in the header comment) |
| `zellij.ts` | `dev ui` and `dev web` |
| `hotswap.ts` | JVM flags for enhanced redefinition and HotswapAgent |
| `db.ts` | `dev db`: Liquibase runs, seeds, snapshots |
| `profile.ts` | `dev profile` and `dev override` |
| `mc/` | Minecraft data codecs (NBT, SNBT, items, UUIDs), fixtures and the `dev mc` commands |
| `capture/` | `dev capture` ([details](dev-capture.md#how-it-works-and-extending-it)) |
| `share/` | `dev share` and the tunnel providers |

## Rules

- Only erasable TypeScript syntax (no `enum`, `namespace`, parameter properties). Relative imports end in `.ts`.
- Check with `pnpm --dir dev typecheck` and `pnpm --dir dev test` (`node --test "dev/test/*.test.ts"`). The tests cover the NBT/SNBT/item/UUID codecs, capture (dump format, filters, wizards, SniffCraft export), replay export (Flashback, ReplayMod, block tracker), share (addresses, renewal), the output helpers and hotswap plugin selection. Add a test beside the module you change when it is pure logic.
- Keep `PNPM_VERSION` in `.devcontainer/Dockerfile` equal to `packageManager` in `dev/package.json`.

## Adding a command

1. Add a `Command` (see `command.ts`) to the array of the right module and make sure `cli.ts` includes that array. `summary` is at most 60 characters, imperative, without a trailing period; `usage` holds positionals only; every flag goes in `options` (one line each); extra explanation goes in `details`.
2. Make sure the command's name starts with a prefix listed in `SECTIONS` in [help.ts](../dev/src/help.ts), otherwise it is listed under "Other".
3. Parse arguments with `parseArgs` from `node:util`, as the `console` and `logs` commands in [cli.ts](../dev/src/cli.ts) do. Pick an instance, a changelog or another id with the shared helpers (`instanceArg`, the pickers in `capture/wizard.ts`) so a terminal gets a menu and a script gets an error.
4. Prompt only through [prompt.ts](../dev/src/prompt.ts). A destructive command takes `--yes` and refuses without it when it cannot ask. With `--no-input` nothing may ever prompt.
5. Print through [output.ts](../dev/src/output.ts) ([Output style](#output-style)); fail with `fail(...)` from `util.ts` for user errors.
6. Document the command in [dev-environment.md](dev-environment.md) (or the page of its group) and, if it is commonly used, in the cheat sheet in [CONTRIBUTING.md](../CONTRIBUTING.md#development-environment).

## Output style

Everything `dev` prints goes through [output.ts](../dev/src/output.ts) so that menus and tables look the same everywhere:

1. Tabular data uses `table()` (header row), `columns()` (menu labels), `keyValue()` (label/value blocks) or `streamRow()` (lines that arrive one at a time). Never align with `padEnd`/`padStart`; they break on colors and wide names.
2. Table headers are written lower case in code and shown UPPERCASE, columns are separated by two spaces and numbers are right-aligned. An empty result is one plain sentence (`No captures.`), never an empty table.
3. Color is minimal and never the only signal: bold for headings and table headers, dim for secondary text (hints, `Same as:`), green/yellow/red only for status words (`status()`) and the `warning:`/`error:` prefixes. It switches off for pipes, `NO_COLOR` and `TERM=dumb`.
4. Status words are lower case: `running`, `stopped`, `disabled`, `starting`, `ok`, `failed`, `missing`, `stale`, `live`, `off`.
5. Command summaries are at most 60 characters, imperative, without parentheticals. Put flags in `options` (one line each) and detail in `details`; `usage` holds positional arguments only.
6. Print only what the user cannot already know: the result (a new path or id) and problems. Never echo what they just chose, counts they just saw in a picker, how-to-use hints or reminders after success, or details of work that is expected to just work. A warning is the problem, a semicolon and the fix. `next(...)` is only for the fix to a problem, never a tour of follow-up commands.
7. Show paths repo-relative (`relPath()`), sizes with `formatBytes()` and durations with `formatDuration()`. `step()` prints a bold phase heading, `sameAs()` the dim `Same as:` line of a guided run.
8. Machine-readable output (`--format json|snbt|raw`, `mc encode`) stays unstyled.

```ts
log(table(['id', { header: 'size', align: 'right' }], rows));
next('dev capture show latest');
```

## Extension points

- **Share providers:** implement `ProviderFactory` (header of `dev/src/share/provider.ts`), import the service's SDK lazily (it is an optional dependency), register it in `dev/src/share/providers/index.ts`, and select it with `share.provider` or `dev share start --provider <name>`. Tokens live in `dev.local.toml` or the environment, never in `dev.toml` or profiles.
- **Capture export formats:** implement `Exporter` (`dev/src/capture/exporters/types.ts`) in a new file and register it in `exporters/index.ts`; `jsonl.ts` is the smallest example. Packet-layout code for Flashback and ReplayMod lives in `exporters/play.ts` and `exporters/blocks.ts`.
- **Dump format:** changing a record needs a `FORMAT` bump in both `DumpWriter.java` and `dev/src/capture/format.ts`; readers keep reading old formats.
- **Profiles:** the archive layout is versioned by `format` in `profile.toml` (`FORMAT` in `dev/src/profile.ts`). Changing the layout means bumping it and still reading older ones. Imports treat the archive as untrusted; keep the validation and review screen intact. Config worth sharing goes in `dev/templates.local/` (via `dev override`), never into `dev/run`.
- **Minecraft blob columns:** a new item/NBT column needs an entry in `dev/src/mc/columns.ts`; a new inventory-like table needs its own fixture section in `fixtures.ts`.
