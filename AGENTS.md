# AGENTS.md

Guidance for AI coding agents. Humans: see [CONTRIBUTING.md](CONTRIBUTING.md) (longer, with diagrams). Keep both consistent when changing conventions.

## Project

Paradisu Plugin: one jar containing a **Velocity** plugin and a **Paper** plugin for the Paradisu Minecraft network. Java 25, Gradle (Kotlin DSL), Lombok, Cloud v2 commands, Adventure/MiniMessage text, Configurate YAML configs, Hibernate + Liquibase on PostgreSQL, ConnectorPlugin for proxy<->server messaging.

## Modules

| Module | Role | Depends on |
| --- | --- | --- |
| `database` | JPA entities (`models/`), `Models` registry, Liquibase files in `src/main/resources/db/` | none (no platform code) |
| `core` | Shared code: `ParadisuPlugin` interface, `DatabaseSession`, config loading, `TranslationManager`, `Messages`, `HelpManager`, `PackManager`, `messaging/` base classes; default lang file `messages_en.properties` | `database` |
| `paper` | `ParadisuPaper`, commands, parsers, listeners, `sync/` async DB jobs, `warps/WarpManager`, `messaging/` (heartbeat, cluster state, messages), configs | `core` |
| `velocity` | `ParadisuVelocity` (runs migrations), commands, listeners, teleport utils, `messaging/VelocityMessagingManager`, configs | `core` |
| `devtools/migrate` | Dev-only Liquibase runner used by `dev db` (never shaded into the jar) | `database` |
| `devtools/capture` | Dev-only packet capture plugin (`ParadisuCapture.jar`, one jar for Paper, Velocity and Limbo; never shaded into `ParadisuPlugin.jar`). No Paradisu dependencies | none (compileOnly platform APIs) |
| `build-logic` | Convention plugins (repos, toolchain, Spotless, resource expansion) | - |

Root project shades everything into `build/libs/ParadisuPlugin.jar`. Dependency versions live only in `gradle/libs.versions.toml`.

## Commands

```bash
./gradlew spotlessApply          # format; run before finishing any change
./gradlew build                  # compile + spotlessCheck + shadow jar
./gradlew :velocity:compileJava  # fast single-module compile check
```

There is no test suite; verify by compiling (`build`). Do not claim runtime behavior was tested unless it was.

The `dev/` tool (TypeScript run directly by Node 24, pnpm) has its own checks: `pnpm --dir dev typecheck` and `pnpm --dir dev test` (NBT/SNBT/item/UUID codecs). Only erasable TS syntax is allowed (no `enum`, `namespace`, parameter properties); relative imports end in `.ts`.

## Dev environment

The devcontainer (`.devcontainer/`) provides JetBrains Runtime 25, PostgreSQL 17 with seed data, Redis and the `dev` command. Full guide: CONTRIBUTING.md.

```bash
dev setup                         # download Purpur, Velocity and plugins (versions follow libs.versions.toml)
dev start / stop / status         # proxy + backends under per-instance supervisors; debug ports 5005+ (VS Code: Debug network)
dev db reset --through 00N        # rebuild the database up to changelog 00N with seed data
```

- Verify a new changelog: `dev db reset --through <previous>`, then `dev start`; the proxy applies the new changelog to seeded data.
- A changelog that changes data shape needs `dev/db/seed/00N.sql` for its new columns/tables. Seeds target the schema as of that changelog; never make an older seed depend on a later schema.
- Preset configs for the servers and plugins are templates in `dev/templates/`; the network and plugin list are in `dev/dev.toml`.
- Run `dev` non-interactively: pass names and arguments, add `--yes` to destructive commands and `--no-input` to anything that could prompt. Never run `dev console` or `dev ui` (they take over the terminal); read output with `dev logs <name>` or `dev logs <name> --console`.
- Minecraft binary data: fixtures live in `dev/db/fixtures/*.toml` (SNBT items; `requires_changelog` gates them by schema like seeds do). A new item/NBT blob column needs an entry in `dev/src/mc/columns.ts`. Inspect with `dev mc inventory|nbt|item|uuid`; round-trip with `dev mc dump` then `dev db fixtures load`.
- Packet capture: `dev capture start|stop|status|watch|list|show|packet|export|formats|rm|protocol|doctor`. The plugin is `devtools/capture`: platform-free `core/` (engine, `ProtocolSniffer`, writer thread, `DumpWriter`), `control/ControlServer` (JSON lines on a Unix socket, path from `-Dparadisu.capture.socket`, set by `dev start`) and one hook per platform. **If a Minecraft/Paper/Velocity/Limbo update breaks injection**, the places to fix are `netty/PipelineLayout.java` (handler names; Paper and Velocity), `CapturePaper.register()` (Paper's `ChannelInitializeListenerHolder`, reflective), `CaptureVelocity.hook()` (Velocity's `ConnectionManager` initializer holders, reflective) and `CaptureLimbo.onConnect()` (Limbo's `Channel.input/output` and `ClientConnection.setChannel`, reflective); `dev capture doctor` runs a login probe on every instance and prints the live pipeline. Packet names and the few state-transition ids the plugin needs come from Mojang's data generator (`dev/src/capture/protocol.ts`, cached in `dev/.cache/protocol/<mc>/`), so a Minecraft bump needs `dev capture protocol --refresh` and an instance restart, not Java changes. Dumps are `dev/captures/<id>/<instance>.bin.gz` (format in `DumpWriter.java` / `dev/src/capture/format.ts`: changing a record needs a `FORMAT` bump on both sides, and readers keep reading old formats). Export formats are `Exporter`s in `dev/src/capture/exporters/`, registered in `exporters/index.ts` (SniffCraft `.scbin`, `jsonl`, Flashback `.zip` and ReplayMod `.mcpr` exist; the last two decode packet payloads, and those layouts live only in `exporters/play.ts` and `exporters/blocks.ts` (chunk and block state tracking, used to rebuild the recording player's own block-break particles and sound): fix them there on a Minecraft bump, comparing with ReplayStudio's `protocol/packets/*`). `dev capture` commands ask on a terminal when options are omitted (`dev/src/capture/wizard.ts`: capture menus, filter builder, recording steps, export format) and print a `Same as:` line with the flags; pass flags or `--no-input` and they never prompt. A new command that needs an id, format or filter should reuse those helpers. Capture must never block or break a connection: keep the hot path (`CaptureEngine.onFrame`) allocation-light, never block, and catch everything.
- Public tunnels: `dev share start|stop|status` (daemon in `dev/src/share/daemon.ts`). Providers implement `ProviderFactory` (`dev/src/share/provider.ts`) and are registered in `dev/src/share/providers/index.ts`; their SDKs are imported lazily and are optional dependencies. Tunnel addresses reach the proxy through the `PROXY_PUBLIC_HOST`/`LIMBO_PUBLIC_HOST` template vars and `vparadisu reload`. Tokens live in `dev.local.toml` or the environment, never in `dev.toml` or profiles.
- Profiles (`dev profile export|import`) are a versioned archive format (`format` in `profile.toml`, see `dev/src/profile.ts`); changing the layout means bumping `format` and still reading older ones. Imports treat the archive as untrusted; keep the validation and review screen intact. Config worth sharing goes in `dev/templates.local/` (via `dev override`), never into `dev/run`.
- Output: print through `dev/src/output.ts` (`table`, `columns`, `keyValue`, `streamRow`, `status`, `next`); never pad columns by hand. Command summaries are at most 60 characters with flags listed in `options` and `usage` holding positionals only (see "Output style" in CONTRIBUTING.md).
- Do not claim a dev-environment flow worked unless you ran it.

## Rules

- Always run `./gradlew spotlessApply` then `./gradlew build` after editing Java. Do not hand-format or add license headers (Spotless adds them; new files only need `package`).
- Use Lombok in the existing style (`@Accessors(fluent = true)`, `@Getter`, `@Builder(toBuilder = true)`, `@RequiredArgsConstructor`). Accessors are fluent: `model.name()`, not `getName()`.
- Match surrounding code: Javadoc on public methods, fluent builders, `paradisu.logger()` for logging (never `System.out`).
- Keep `database` free of Paper/Velocity/`core` imports.
- Don't hard-code dependency versions in module `build.gradle.kts`; use the version catalog.
- `*.properties`, `plugin.yml`, `velocity-plugin.json` are processed with Gradle `expand`: `${...}` is substituted; write a literal `$` as `\$`.

## Database changes

Hibernate does **not** manage the schema; Liquibase does. A model change always needs both sides.

1. Edit/add the entity in `database/.../models/` (Jakarta annotations; `@Table`/`@Column` names must match the changelog; keep `@NoArgsConstructor` + `@AllArgsConstructor` + `@Builder(toBuilder = true)`; relations lazy).
2. **Register new entities in `Models.models()`**, or Hibernate will not map them.
3. Add a changeSet in a **new** file `database/src/main/resources/db/changelog/00N.xml` (id continues the sequence, `author="paradisu"`) and `<include>` it in `db/migrations.xml`. **Never edit an existing changeSet**; add a new one instead. Tables are `UPPER_SNAKE`, columns `lower_snake`, join columns `id_<entity>`, FKs `fk_<table>_<ref>`.
4. Migrations run only on Velocity startup, so note in your summary that the proxy must be deployed first.

Querying/persistence:
- Create a short-lived `EntityManager` per unit of work from `paradisu.databaseSession().factory()` in try-with-resources; never share or store one.
- Explicit transactions for writes; handle and log failures; parameterized typed JPQL only.
- **Never block the main thread.** Paper: run DB work with `Bukkit.getScheduler().runTaskAsynchronously`, and return to `runTask` for any Bukkit API use. Capture/serialize Bukkit data on the main thread first and pass plain values (bytes, UUIDs, Instants) to the async task. See `paper/.../sync/PlayerJoinSync` and `PlayerQuitSync`. Velocity: no heavy work in command handlers; compose `CompletableFuture`s (`whenComplete`) for connector bridge calls.
- Don't touch lazy collections after the `EntityManager` closes; don't hold transactions open across network or Bukkit calls.

## Adding a command

1. Create `XCommand` in `paper/.../commands/command/` (extends `AbstractPaperCommand`) or `velocity/.../commands/command/` (extends `AbstractVelocityCommand`). Network-wide/cross-server -> Velocity (use `paradisu.connector().getBridge()`); Bukkit API -> Paper.
2. In `register()`: `commandManager.commandBuilder(name, aliases...)`, `.permission(...)` (Paper `paradisu.*`, Velocity `vparadisu.*`), `.commandDescription(Description.of(messagesConfig...helpMsg()))`, `.required/.optional(name, parser, Description.of(...helpArgs...))`, `.handler(this::method)`, then `commandManager.command(builder)`. Model on `velocity/.../BackCommand` or `paper/.../HatCommand`.
3. **Add `new XCommand(paradisu)` to `PaperCommandRegistrar` or `VelocityCommandRegistrar`**; unlisted commands are never registered.
4. Add messages (below).

## Messages and text

Never hard-code player-facing strings or legacy `&`/`§` codes. Pipeline: language key in `core/src/main/resources/messages_en.properties` -> referenced as `<lang:key>` in a MiniMessage default string in the platform `MessagesConfig` (`paper|velocity/.../config/configs/MessagesConfig.java`) -> deserialized with `MiniMessage` (+ `Placeholder.component(...)`) -> wrapped in `Messages.prefixed(...)` -> `sendMessage`.

For a new command `foo`:
- Properties keys: `paradisu.command.help.foo`, `paradisu.command.help.foo.<n>` (argument n), `paradisu.command.help.foo.<sub>` (subcommand), `paradisu.command.output.foo` or `.foo.<n>` for variants. Group a command's keys together, help first then output.
- Values are `MessageFormat`: args `{0}`, `{1}`; **double apostrophes** (`player''s`). Pass args through the template, e.g. `<lang:paradisu.command.output.foo.0:'<gold><player>'>`.
- In `MessagesConfig`: add a `@Getter @ConfigSerializable public static final class Foo` with `@Setting("help-msg") String helpMsg`, optional `@Setting("help-args") List<String> helpArgs`, `@Setting("output") List<String> output`, whose defaults are `<lang:...>` strings, and add `private Foo foo = new Foo();` to `Commands`. Output list order must match the `.n` key order; read via `paradisu.messagesConfig().commands().foo().output().get(n)`.
- New config files: write a `@ConfigSerializable` class and register a `ConfigEntry` in `PaperConfigManager` / `VelocityConfigManager`.
- Bundled extra locales: `messages_<locale>.properties` + `TranslationManager.BUNDLED_LOCALES`.

## Cross-server messaging

Paradisu's own server<->server/proxy messages run on ConnectorPlugin's `Connector` under the plugin's own name (`core/.../messaging/MessagingManager`). Use the bridge (`paradisu.connector().getBridge()`) for teleports/commands; use messaging for notifications and shared state.

- **Never extend or instantiate ConnectorPlugin's `Bridge`**: a second instance re-registers ConnectorPlugin's handlers/listeners and breaks its teleports and responses.
- Adding a message: `XMessage extends RegisteredMessage<BukkitConnectorPlugin, Player>` (or the Velocity types) in `paper|velocity/.../messaging/messages/` with a unique `ACTION` constant; implement `handle(receiver, Message message)` (payload: `message.getData()`, sender: `message.getSendingServer()`, `receiver` may be null); add a nested `record Payload(...) implements SerialMessage` with `serialize()` (never return null; default is an empty array) and `static deserialize(byte[])` using Guava `ByteStreams`, with fields in the same order. **Register it in `registerMessages()`** of `PaperMessagingManager`/`VelocityMessagingManager`; unregistered actions are dropped. Send with `paradisu.messagingManager().send(MessageTarget.X, payload)`.
- Paper handlers run on the main thread: keep them fast, no DB work (schedule async). Send from the main thread on Paper (the plugin-message messenger is not thread-safe); hop back with `runTask` from async code.
- Targets: `*_QUEUE` for messages still valid when delivered late (e.g. `SYNC_WARPS`); `*_WITH_PLAYERS` for periodic/time-sensitive state (e.g. `SERVER_HEARTBEAT`). `OTHERS_*` skips the sender, so apply the change locally too.
- Changing a payload breaks mixed-version servers; add a new action instead of changing an existing payload incompatibly.
- Cluster state (`PaperMessagingManager.clusterState()`, `findPlayer`, `findServerForContext`) comes from 5 s heartbeats with a 15 s timeout; handle missing servers or players.

## Reusable utilities (`core/.../utils/`)

- `ExponentialBackoffLimiter<K>`: per-key burst + exponential lockout rate limiting (`tryAcquire(key)` -> `Result.allowed()`/`retryAfterSeconds()`). Use it for any player-triggered command that is expensive or spammable. Check it before the expensive work, read the settings from config (see `ParadisuConfig.Warps.RateLimit`), and add a bypass permission.
- `FuzzyMatcher<T>`: closest-name lookup. Build it on data change, not per call; check exact matches with a map first; never call it from suggestion providers.

## Updating dependencies

- Edit `gradle/libs.versions.toml` `[versions]` only. Keep shared `version.ref`s together (Hibernate artifacts, Adventure, Cloud minecraft artifacts).
- Look up latest versions from the real source, don't guess:
  - Most libs/plugins: `curl -s https://repo.maven.apache.org/maven2/<group/as/path>/<artifact>/maven-metadata.xml` (Maven Central) or the Gradle Plugin Portal.
  - `paper-api`, `velocity-api`: `https://repo.papermc.io/repository/maven-public/<group path>/<artifact>/maven-metadata.xml` (Velocity is a timestamped SNAPSHOT; Paper is a `build.N-beta`-style version).
  - `connectorplugin-*`: `https://repo.minebench.de/de/themoep/connectorplugin/` (snapshot).
  - `fastutil-object-object-maps`: `https://repo.opencollab.dev/main/com/nukkitx/fastutil/`.
  - `limbo`: `https://repo.loohpjames.com/repository/com/loohp/Limbo/` has no `maven-metadata.xml`; read `<version>` in https://github.com/LOOHP/Limbo/blob/master/pom.xml and check that `.../Limbo/<version>/Limbo-<version>.pom` exists. Only `devtools/capture` compiles against it, and only a few API types.
- New Minecraft version: bump `paper-api` and `velocity-api`, then check `connectorplugin`, `luckperms-api`, `cloud-*` compatibility; `plugin.yml` `api-version` derives from `paper-api` automatically; `dev` follows the same versions. Also compare `_version` in `dev/templates/paper-common/config/paper-global.yml` with the file Paper generates. Update JDK in `build-logic` (`java-conventions` and `build-logic/build.gradle.kts`), `.github/workflows`, and the devcontainer if required. Run `./gradlew build`.
- Upgrade Hibernate/Liquibase/PostgreSQL together and flag that migrations need testing on a database copy.
- The Palantir Java Format version (`2.83.0`) is pinned in `net.paradisu.java-conventions.gradle.kts`, not the catalog.

## Before you finish

1. `./gradlew spotlessApply build` passes.
2. New entity -> in `Models` + changelog included in `migrations.xml`.
3. New command -> in a registrar, with properties keys and `MessagesConfig` entries.
4. No blocking DB/network work on the main thread.
5. New message -> registered in `registerMessages()`, sent from the main thread on Paper.
6. Summarize what you could not verify (no automated tests, no live server run).
