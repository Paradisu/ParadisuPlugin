# Paradisu's dev setup

The dev network is run by [mcdevtools](https://github.com/Paradisu/mcdevtools) (the `dev/` submodule; reference in [dev/docs/](../dev/docs/environment.md)). This folder holds what is specific to Paradisu.

| Path | What it is |
| --- | --- |
| `config.toml` | The network: a Velocity `proxy`, a standalone `limbo` (the `/vparadisu maintenance` waiting room), Purpur backends `lobby` and `creative`, the plugins, the build (`./gradlew -q shadowJar`, artifact `build/libs/ParadisuPlugin.jar`), PostgreSQL (`paradisu` and `luckperms`) with Liquibase, Redis. Personal overrides: `config.local.toml` (gitignored; start from `config.local.toml.example`) |
| `templates/` | Preset configs rendered into every instance on each start: `plugins/<key>/<paper\|velocity\|limbo>/` for ConnectorPlugin and LuckPerms, `proxy/plugins/paradisu/paradisu.yml` and `paper-common/plugins/ParadisuPlugin/paradisu.yml` for the plugin itself. The generic base configs (`velocity.toml`, `paper-global.yml`, `server.properties`) come from mcdevtools |
| `db/seed/` | SQL seeds, one per changelog (`001.sql`, `002.sql`; the deliberate NULLs in `002.sql` exist so that changelog 003 has something to backfill). Personal rows: `db/seed/local.sql` (gitignored) |
| `db/fixtures/` | Inventory fixtures in SNBT, loaded after a reset (`requires_changelog` skips a file when the reset stops earlier). Private ones: `db/fixtures.local/` |
| `layouts/` | Extra zellij layouts (`dev ui backends`) |
| `tests/manual/` | Manual test suites for `dev manual run <suite> --player <name>` (TOML: `[[tests]]` with `setup`, `steps`, `expect`, `cleanup`); `smoke.toml` is the starter. Reference: [dev/docs/testing.md](../dev/docs/testing.md#manual-tests) |
| `test-results/` | Results of `dev test run` and `dev manual run` (gitignored) |
| `profiles/` | Exported setups (gitignored) |
| `extensions/paradisu/` | The Paradisu extension (TypeScript), see below |
| `run/`, `.cache/`, `captures/`, ... | Runtime data (gitignored). Never edit files in `run/`: edit the templates |

## Tests

`config.toml` has a `[test]` table: `dev test run` runs `./gradlew :integration-tests:integrationTest` (the JUnit tests in `integration-tests/`, which use the mcdevtools testkit through the included build), keeps a Flashback replay of every failing test (`capture = "on-failure"`), fails a test when a server logs an error, and sets `in_game = true` so the Paper backends carry the in-game manual test runner (`/mcdevtest`). Bots join the backends directly with forged Velocity forwarding because the proxy runs in online mode; see [CONTRIBUTING.md](../CONTRIBUTING.md#integration-tests).

## Instances

`proxy` (25565), `limbo` (30000, not in the proxy's server list: the Paradisu proxy config's `limbo-server` points at it and ConnectorPlugin makes it `transferall` players back when the proxy starts, so `dev restart proxy` returns players from Limbo), `lobby` (30066) and `creative` (30067). Each backend has a `context` table (`server`, `warp`, `inventory`) that fills `${CONTEXT_*}` in the plugin's config. Set `enabled = false` on `creative` in `config.local.toml` to save memory; set `limbo` to disabled when no Limbo build exists yet for the Minecraft version in use.

## The Paradisu extension

[`extensions/paradisu`](extensions/paradisu/src/index.ts) adds, on top of mcdevtools:

- template variables: `CONTEXT_SERVER` / `_WARP` / `_INVENTORY`, `PROXY_PUBLIC_HOST` / `_PORT`, `LIMBO_PORT` / `LIMBO_PUBLIC_HOST` / `_PORT`, and `PROJECT_NAME` (`Paradisu`, for the MOTDs);
- sharing: after `dev share` changes the public addresses it runs `vparadisu reload` on the proxy (mcdevtools then runs `velocity reload`), and warns when the proxy is shared but Limbo is not (maintenance mode would drop remote players);
- commands that read the Paradisu schema: `dev mc players`, `dev mc inventory`, `dev mc dump`, `dev mc columns` and `dev db fixtures` (a new item/NBT blob column needs an entry in `src/mc/columns.ts`; a new inventory-like table needs its own section in `src/mc/fixtures.ts`);
- a player resolver, so `dev mc nbt playerdata:<name>@<server>` finds players by name in the dev database;
- an `afterDbReset` handler that loads the fixtures.

Check it with `pnpm -r typecheck`. Seeds and fixtures target the schema as of their changelog; never make an older seed depend on a later schema. After adding a changelog that changes data shape, add `db/seed/<NNN>.sql` and verify with `dev db reset --through <previous>`, then `dev start`.
