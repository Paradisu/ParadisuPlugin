# AGENTS.md

Guidance for AI coding agents. Humans: see [CONTRIBUTING.md](CONTRIBUTING.md) (longer, with diagrams) and [dev/docs/](dev/docs/environment.md). Keep AGENTS.md and CONTRIBUTING.md consistent when changing conventions.

## Project

Paradisu Plugin: one jar containing a **Velocity** plugin and a **Paper** plugin for the Paradisu Minecraft network. Java 25, Gradle (Kotlin DSL), Lombok, Cloud v2 commands, Adventure/MiniMessage text, Configurate YAML configs, Hibernate + Liquibase on PostgreSQL, ConnectorPlugin for proxy<->server messaging.

## Modules

| Module | Role | Depends on |
| --- | --- | --- |
| `database` | JPA entities (`models/`), `Models` registry, Liquibase files in `src/main/resources/db/` | none (no platform code) |
| `core` | Shared code: `ParadisuPlugin` interface, `DatabaseSession`, config loading, `TranslationManager`, `Messages`, `HelpManager`, `PackManager`, `messaging/` base classes, `utils/`; default lang file `messages_en.properties` | `database` |
| `paper` | `ParadisuPaper`, commands, parsers, listeners, `sync/` async DB jobs, `warps/WarpManager`, `messaging/` (heartbeat, cluster state, messages), configs | `core` |
| `velocity` | `ParadisuVelocity` (runs migrations), commands, listeners, teleport utils, `messaging/VelocityMessagingManager`, configs | `core` |
| `build-logic` | Convention plugin `net.paradisu.java-conventions` (repos, toolchain, Spotless, resource expansion) | - |
| `dev/` | **Git submodule**: [mcdevtools](https://github.com/Paradisu/mcdevtools) (the `dev` CLI, capture plugin, Liquibase runner as the included Gradle build `mcdevtools`, dev container image). Change it in that repository (see its AGENTS.md) and move the pinned version with `dev update` | - |
| `.mcdevtools/` | Paradisu's dev config: `config.toml` (network), templates, seeds, fixtures, layouts, profiles and `extensions/paradisu` (TypeScript). See [.mcdevtools/README.md](.mcdevtools/README.md) | - |

Root project shades everything into `build/libs/ParadisuPlugin.jar`. Dependency versions live only in `gradle/libs.versions.toml`.

## Workflow

1. **Read first.** Find the matching section in [CONTRIBUTING.md](CONTRIBUTING.md) (database, commands, text, messaging, utilities) and the sections below. Look at a similar existing class and copy its structure.
2. **Change** the code, matching the surrounding style.
3. **Check statically:** `./gradlew spotlessApply` then `./gradlew build` for Java; `pnpm -r typecheck` for the Paradisu extension in `.mcdevtools/extensions/` (changes to the dev tool itself are made and tested in the mcdevtools repository).
4. **Check at runtime** when the change has runtime behaviour: see [Runtime testing](#runtime-testing-with-dev).
5. **Update the docs** ([Documentation](#documentation)).
6. **Report** what you ran and its result, and what you could not verify.

```bash
./gradlew spotlessApply          # format; run before finishing any change
./gradlew build                  # compile + spotlessCheck + shadow jar
./gradlew :velocity:compileJava  # fast single-module compile check
```

There is no Java test suite: compiling proves it builds, not that it works. Never claim runtime behavior or a `dev` flow worked unless you ran it.

## Runtime testing with `dev`

The devcontainer runs a full network (Velocity proxy `proxy`, `limbo`, Paper backends `lobby` and `creative`, PostgreSQL, Redis) from your working copy. Use it to verify startup, migrations, config/message changes, console-reachable commands and messaging. Full reference: [dev/docs/environment.md](dev/docs/environment.md) (the `dev` command is `mcdev`; the devcontainer adds the `dev` alias).

Rules for running `dev`:
- Pass names and arguments; add `--no-input` so nothing can prompt; add `--yes` only to the destructive commands that take it (`db reset`, `db seed`, `db restore`, `capture rm`, `profile import`).
- **Never run** `dev ui`, `dev web start`, `dev console <name>` without `--send`, `dev logs ... -f`, or `dev share start --foreground`: they take over the terminal or never exit. Read output with `dev logs <name>` and `dev console <name> --send`.
- `dev start` and `dev restart` build the jar first and can take minutes; give the command a long timeout or run it in the background and check `dev status`.

The loop:

```bash
dev status                              # what is running
dev start --no-input                    # build, start limbo, proxy, lobby, creative; prints "Ready. Join at ..."
dev restart --no-input lobby            # after a code or resource change (add --no-build to skip the build)
dev console lobby --send "plugins"      # run a console command and print its output (--timeout <s>, default 3)
dev console proxy --send "vparadisu about"
dev logs lobby                          # last 200 lines of logs/latest.log  (--console: raw console output)
grep -nE "ERROR|Exception" .mcdevtools/run/lobby/logs/latest.log
dev stop --no-input
```

Caveats:
- `dev start` exits 0 even if a backend did not finish starting (it waits 240 s per server); always confirm with `dev status` and the logs. After a crash `.mcdevtools/run/<name>/.dev/exit.json` has the exit code.
- No player is connected, so player-only commands, listeners and inventory sync cannot be exercised; the console is not a player. Say so in your report instead of claiming they work.
- Paper and Velocity commands are `paradisu ...` and `vparadisu ...` on the matching console. Edit `.mcdevtools/templates`, never files in `.mcdevtools/run`.

What to use for what:

| Change | Verify with |
| --- | --- |
| Startup, plugin wiring | `dev restart <name>`, then `dev logs <name>` for errors and `dev console <name> --send "plugins"` |
| Config / message keys | `dev restart <name>` (resources are not hot reloaded), then `--send "paradisu reload"` (Paper) or `--send "vparadisu reload"` (proxy) |
| Console-capable command | `dev console <name> --send "<command>"` |
| Liquibase changelog | `dev db reset --yes --through <previous>`, `dev start`, then the proxy log for the migration and `dev db psql -c "<sql>"` for the result |
| Item/NBT blob columns | `dev mc inventory <player>`, `dev mc item`, `dev mc nbt` (the first is from the Paradisu extension) |
| Proxy<->backend packets, transfers | `dev capture start --no-input --duration 30s`, trigger it, `dev capture show latest --type <pattern>`; `dev capture doctor` after any Minecraft/Paper/Velocity/Limbo update |

## Dev environment rules

- **Changelogs:** a changelog that changes data shape needs `.mcdevtools/db/seed/00N.sql` for its new columns/tables. Seeds target the schema as of that changelog; never make an older seed depend on a later schema. Verify: `dev db reset --through <previous>`, then `dev start`.
- **Config and templates:** the network and plugin list are in `.mcdevtools/config.toml` (personal overrides in the gitignored `.mcdevtools/config.local.toml`); preset configs in `.mcdevtools/templates/`. Config worth sharing that only you changed at runtime goes in `.mcdevtools/templates.local/` via `dev override`, never into `.mcdevtools/run`. Tokens live in `config.local.toml` or the environment, never in `config.toml` or profiles.
- **Minecraft data:** fixtures are `.mcdevtools/db/fixtures/*.toml` (SNBT items; `requires_changelog` gates them like seeds). A new item/NBT blob column needs an entry in `.mcdevtools/extensions/paradisu/src/mc/columns.ts`.
- **The Paradisu extension** (`.mcdevtools/extensions/paradisu`, TypeScript, [.mcdevtools/README.md](.mcdevtools/README.md)): erasable TypeScript only (no `enum`, `namespace`, parameter properties), relative imports end in `.ts`, print through `@mcdevtools/cli/output`, register everything through the `api` object. Check with `pnpm -r typecheck`.
- **The `dev` tool itself** is the `dev/` submodule ([mcdevtools](https://github.com/Paradisu/mcdevtools)). Its rules (command conventions, versioned formats, capture, share providers) are in `dev/AGENTS.md`. Change it in that repository, commit there, then move the pin here with `dev update` (or `dev update --ref <sha>`) and commit the submodule pointer. Never edit files under `dev/` in this repository. If something Paradisu needs is missing from the extension API, add it in mcdevtools (`dev/docs/extensions.md`).

## Rules

- Always run `./gradlew spotlessApply` then `./gradlew build` after editing Java. Do not hand-format or add license headers (Spotless adds them; new files only need `package`).
- Use Lombok in the existing style (`@Accessors(fluent = true)`, `@Getter`, `@Builder(toBuilder = true)`, `@RequiredArgsConstructor`). Accessors are fluent: `model.name()`, not `getName()`.
- Match surrounding code: Javadoc on public methods, fluent builders, `paradisu.logger()` for logging (never `System.out`).
- The build uses the configuration cache, parallel execution and the build cache (`gradle.properties`). Keep build scripts compatible: read values with `providers.gradleProperty(...)` / `providers.environmentVariable(...)`, never capture `project` or script functions in task actions, and only the root project applies Shadow.
- Keep `database` free of Paper/Velocity/`core` imports.
- Don't hard-code dependency versions in module `build.gradle.kts`; use the version catalog.
- `*.properties`, `plugin.yml`, `velocity-plugin.json` are processed with Gradle `expand`: `${...}` is substituted; write a literal `$` as `\$`.

## Database changes

Hibernate does **not** manage the schema; Liquibase does. A model change always needs both sides.

1. Edit/add the entity in `database/.../models/` (Jakarta annotations; `@Table`/`@Column` names must match the changelog; keep `@NoArgsConstructor` + `@AllArgsConstructor` + `@Builder(toBuilder = true)`; relations lazy).
2. **Register new entities in `Models.models()`**, or Hibernate will not map them.
3. Add a changeSet in a **new** file `database/src/main/resources/db/changelog/00N.xml` (the next number after the highest file in that folder; changeSet ids continue the sequence; `author="paradisu"`) and `<include>` it in `db/migrations.xml`. **Never edit an existing changeSet**; add a new one instead. Tables are `UPPER_SNAKE`, columns `lower_snake`, join columns `id_<entity>`, FKs `fk_<table>_<ref>`.
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
4. Add messages (below), and update the README features/permissions.

## Messages and text

Never hard-code player-facing strings or legacy `&`/`§` codes. Pipeline: language key in `core/src/main/resources/messages_en.properties` -> referenced as `<lang:key>` in a MiniMessage default string in the platform `MessagesConfig` (`paper|velocity/.../config/configs/MessagesConfig.java`) -> deserialized with `MiniMessage` (+ `Placeholder.component(...)`) -> wrapped in `Messages.prefixed(...)` -> `sendMessage`.

For a new command `foo`:
- Properties keys: `paradisu.command.help.foo`, `paradisu.command.help.foo.<n>` (argument n), `paradisu.command.help.foo.<sub>` (subcommand), `paradisu.command.output.foo` or `.foo.<n>` for variants. Group a command's keys together, help first then output.
- Values are `MessageFormat`: args `{0}`, `{1}`; **double apostrophes** (`player''s`). Pass args through the template, e.g. `<lang:paradisu.command.output.foo.0:'<gold><player>'>`.
- In `MessagesConfig`: add a `@Getter @ConfigSerializable public static final class Foo` with `@Setting("help-msg") String helpMsg`, optional `@Setting("help-args") List<String> helpArgs`, `@Setting("output") List<String> output`, whose defaults are `<lang:...>` strings, and add `private Foo foo = new Foo();` to `Commands`. Output list order must match the `.n` key order; read via `paradisu.messagesConfig().commands().foo().output().get(n)`.
- New config files: write a `@ConfigSerializable` class and register a `ConfigEntry` in `PaperConfigManager` / `VelocityConfigManager`.
- Extra bundled locales: `messages_<locale>.properties` + `TranslationManager.BUNDLED_LOCALES` (empty today; English is always the default).

## Cross-server messaging

Paradisu's own server<->server/proxy messages run on ConnectorPlugin's `Connector` under the plugin's own name (`core/.../messaging/MessagingManager`). Use the bridge (`paradisu.connector().getBridge()`) for teleports/commands; use messaging for notifications and shared state.

- **Never extend or instantiate ConnectorPlugin's `Bridge`**: a second instance re-registers ConnectorPlugin's handlers/listeners and breaks its teleports and responses.
- Adding a message: `XMessage extends RegisteredMessage<BukkitConnectorPlugin, Player>` (or the Velocity types) in `paper|velocity/.../messaging/messages/` with a unique `ACTION` constant; implement `handle(receiver, Message message)` (payload: `message.getData()`, sender: `message.getSendingServer()`, `receiver` may be null); add a nested `record Payload(...) implements SerialMessage` with `serialize()` (never return null; default is an empty array) and `static deserialize(byte[])` using Guava `ByteStreams`, with fields in the same order. **Register it in `registerMessages()`** of `PaperMessagingManager`/`VelocityMessagingManager` (Velocity has none yet); unregistered actions are dropped. Send with `paradisu.messagingManager().send(MessageTarget.X, payload)`.
- Paper handlers run on the main thread: keep them fast, no DB work (schedule async). Send from the main thread on Paper (the plugin-message messenger is not thread-safe); hop back with `runTask` from async code.
- Targets: `*_QUEUE` for messages still valid when delivered late (e.g. `SYNC_WARPS`); `*_WITH_PLAYERS` for periodic/time-sensitive state (e.g. `SERVER_HEARTBEAT`). `OTHERS_*` skips the sender, so apply the change locally too.
- Changing a payload breaks mixed-version servers; add a new action instead of changing an existing payload incompatibly.
- Cluster state (`PaperMessagingManager.clusterState()`, `findPlayer`, `findServerForContext`) comes from 5 s heartbeats with a 15 s timeout; handle missing servers or players.

## Reusable utilities (`core/.../utils/`)

- `ExponentialBackoffLimiter<K>`: per-key burst + exponential lockout rate limiting (`tryAcquire(key)` -> `Result.allowed()`/`retryAfterSeconds()`). Use it for any player-triggered command that is expensive or spammable. Check it before the expensive work, read the settings from config (see `ParadisuConfig.Warps.RateLimit`), and add a bypass permission.
- `FuzzyMatcher<T>`: closest-name lookup. Build it on data change, not per call; check exact matches with a map first; never call it from suggestion providers.

## Updating dependencies

- Edit `gradle/libs.versions.toml` `[versions]` only.
- Repositories in `net.paradisu.java-conventions` (except Maven Central) are filtered to the groups they serve (`includeGroup`). A new dependency from Paper, OpenCollab, Minebench or LoohpJames needs its group added there, or it will fail to resolve. Keep shared `version.ref`s together (Hibernate artifacts, Adventure, Cloud minecraft artifacts).
- Look up latest versions from the real source, don't guess:
  - Most libs/plugins: `curl -s https://repo.maven.apache.org/maven2/<group/as/path>/<artifact>/maven-metadata.xml` (Maven Central) or the Gradle Plugin Portal.
  - `paper-api`, `velocity-api`: `https://repo.papermc.io/repository/maven-public/<group path>/<artifact>/maven-metadata.xml` (Velocity is a timestamped SNAPSHOT; Paper is a `build.N-beta`-style version).
  - `connectorplugin-*`: `https://repo.minebench.de/de/themoep/connectorplugin/` (snapshot).
  - `fastutil-object-object-maps`: `https://repo.opencollab.dev/main/com/nukkitx/fastutil/`.
- New Minecraft version: bump `paper-api` and `velocity-api` (and the same two in `dev/java/gradle/libs.versions.toml` in mcdevtools, which the capture plugin compiles against; `limbo` is only there), then check `connectorplugin`, `luckperms-api`, `cloud-*` compatibility; `plugin.yml` `api-version` derives from `paper-api` automatically; `dev` follows `paper-api` and `velocity-api` (then `dev setup --refresh`, `dev capture protocol --refresh`, `dev capture doctor`). Also compare `_version` in mcdevtools' `dev/packages/cli/templates/paper-common/config/paper-global.yml` with the file Paper generates. Update JDK in `build-logic` (`java-conventions` and `build-logic/build.gradle.kts`), `.github/workflows`, and the mcdevtools devcontainer image (`dev/devcontainer/Dockerfile`) if required. Run `./gradlew build`.
- Upgrade Hibernate/Liquibase/PostgreSQL together and flag that migrations need testing on a database copy.
- The Palantir Java Format version (`2.83.0`) is pinned in `net.paradisu.java-conventions.gradle.kts`, not the catalog.

## Documentation

**Documentation is part of the change.** A task is not finished while a doc that describes the changed behavior is wrong or missing. Keep docs short and factual; fix inaccuracies you notice on the way, even outside your change.

| Change | Update |
| --- | --- |
| Command, permission, config key, user-visible feature | `README.md` (features, permissions, configuration) |
| New pattern others will repeat (message kind, config, utility) or a changed convention | The matching section of `CONTRIBUTING.md` **and** the rule here |
| `dev` command, flag, config key or workflow | In the mcdevtools repository: its `summary`/`options` and the page in its `docs/`; then `dev update` here, and the [Runtime testing](#runtime-testing-with-dev) section if agents should use it. Paradisu's own config and extension: [.mcdevtools/README.md](.mcdevtools/README.md) |
| New module or major class | Module tables in `CONTRIBUTING.md` and here |
| Liquibase changelog | `.mcdevtools/db/seed/00N.sql` when data shape changes; say in your summary that the proxy must be deployed first |

Ownership: `README.md` users/admins · `CONTRIBUTING.md` contributor workflow and code guides · `dev/docs/` (mcdevtools) the dev environment, `.mcdevtools/README.md` Paradisu's dev setup · `AGENTS.md` agent rules. Edit the page that owns the topic; don't create new doc files unless asked. Public methods get Javadoc; non-obvious `dev/src` modules get a header comment.

## Before you finish

1. `./gradlew spotlessApply build` passes (and `pnpm --dir dev typecheck && pnpm --dir dev test` if you touched `dev/`).
2. New entity -> in `Models` + changelog included in `migrations.xml` (+ seed if data shape changed).
3. New command -> in a registrar, with properties keys and `MessagesConfig` entries.
4. No blocking DB/network work on the main thread.
5. New message -> registered in `registerMessages()`, sent from the main thread on Paper.
6. Runtime checks done where applicable ([Runtime testing](#runtime-testing-with-dev)), or the reason they were not.
7. Docs updated ([Documentation](#documentation)).
8. Your summary states what you could not verify (no automated Java tests, no player connected, anything you did not run).
