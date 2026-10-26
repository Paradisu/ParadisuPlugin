# Paradisu Plugin [![GitHub release](https://img.shields.io/github/release/Paradisu/ParadisuPlugin.svg)](https://GitHub.com/Paradisu/ParadisuPlugin/releases/) [![Discord](https://badgen.net/badge/icon/discord?icon=discord&label)](https://discord.gg/5aSZPDnt7n)

The official plugin for the Paradisu server. It provides essential network utilities as well as features specific to the Paradisu project. For more information about Paradisu, join the [Discord](https://discord.gg/5aSZPDnt7n).

The plugin is in early development and is heavily customized for Paradisu, so it is **not recommended for use on other servers**; extensive modification would likely be required.

## Overview

Paradisu Plugin is a single jar (`ParadisuPlugin.jar`) containing **two plugins**:

| Platform | Main class | Role |
| --- | --- | --- |
| Velocity (proxy) | `net.paradisu.velocity.ParadisuVelocity` | Network-wide commands (teleportation, player lists, resource packs), database migrations, session tracking |
| Paper (backend server) | `net.paradisu.paper.ParadisuPaper` | Per-server commands, warps, inventory/session sync with the database |

The two sides communicate through [ConnectorPlugin](https://github.com/Phoenix616/ConnectorPlugin), and share a PostgreSQL database. Paper servers also message each other through ConnectorPlugin: each one broadcasts a heartbeat with its warp context and online players, and tells the others to reload warps when they change.

## Requirements

- Java 25
- [Velocity](https://papermc.io/software/velocity) and [Paper](https://papermc.io/software/paper) (versions tracked in [gradle/libs.versions.toml](gradle/libs.versions.toml))
- [ConnectorPlugin](https://github.com/Phoenix616/ConnectorPlugin) on the proxy and on every backend server (required dependency). The Redis or MQTT messenger is recommended: with the plugin message messenger, a server with no players online cannot send heartbeats, so it is not used as a warp destination until someone joins it
- [LuckPerms](https://github.com/LuckPerms/LuckPerms) (used for permissions and rank prefixes)
- A PostgreSQL database

Cloud, Hibernate, Liquibase, Configurate, and the other libraries are bundled into the jar.

## Features

**Proxy (Velocity)**
- Cross-server teleportation: `/tp`, `/tppos`, `/tph`, and the request flow `/tpr`, `/tprh`, `/tpa`, `/tpd`, `/tpc`
- `/back` to return to a previous teleport location
- `/locate` to find the server and position of a player
- `/ls` for a network-wide player list with rank prefixes
- `/packs` to manage, reload, resend and clear resource packs; the default pack is sent to players on first connect
- Limbo/maintenance handling: players are transferred out of the limbo server once the proxy is ready
- Player and proxy session tracking (first join, last seen, playtime)
- Automatic database migrations on startup (Liquibase)
- `/vparadisu` (`about`, `reload`)

**Backend (Paper)**
- `/hat` to wear the item in your hand
- Network-wide warps:
  - `/warp <warp>` teleports you to a warp. Names may contain spaces. Exact names and aliases (case-insensitive) win; otherwise the closest name you can use is chosen, so partial names (`cind`, `castle`), squashed names (`mainstreet`) and small typos (`cindrella`) work. A warp in the current server's warp context is preferred when several match equally
  - `/warp` is rate limited per player: a few warps in quick succession are fine, but spamming it locks the player out for 10 s, then 20 s, 40 s, … up to 10 minutes. The limits are set in `paradisu.yml` under `warps.rate-limit`
  - `/paradisu warp send <player> <warp>` teleports any player on the network to a warp
  - Warps belong to a *warp context*. Warping to a warp in another context sends the player to a server in that context: the one they are on if it serves the context, otherwise the least populated one
  - `/warps` lists the warps you can use; click a name to warp
  - `/paradisu warp create|update|delete|permission|aliases` manages the warps of the current server's context. Quote names that contain spaces (`/paradisu warp create "Main Street"`). A warp may require a permission node
  - Changes apply on every server immediately
- Network-wide inventory, armor, off-hand and ender chest persistence, loaded on join and saved on quit, scoped by a configurable *inventory context*
- Server session tracking
- `/paradisu` (`about`, `reload`, `warp`)

**General**
- Fully translatable and configurable messages (Adventure + MiniMessage, with `messages.yml` and `translations/*.properties` overrides)
- Commands built with the [Cloud](https://github.com/Incendo/cloud) framework, with generated help

## Installation

1. Download the latest `ParadisuPlugin.jar` from the [Releases page](https://github.com/Paradisu/ParadisuPlugin/releases/latest).
2. Install the same jar in the `plugins` folder of your Velocity proxy **and** your Paper servers.
3. Install ConnectorPlugin and LuckPerms alongside it.
4. Start the proxy first. Fill in the `database` section of the generated `paradisu.yml` (`url`, `username`, `password`) on the proxy and on each Paper server, then restart. The proxy applies database migrations on startup, so it must be running a current version before Paper servers use new tables.

Paper servers also read `context` settings from `paradisu.yml`:

| Key | Purpose |
| --- | --- |
| `context.server` | Name recorded in session history |
| `context.inventory` | Servers sharing the same value share saved inventories |
| `context.warp` | Warp context: warps created on this server belong to it, and players warping to a warp in this context can be sent to this server |

`warps.rate-limit` in `paradisu.yml` controls the `/warp` rate limit: `burst` (warps allowed in quick succession, default 3), `refill-seconds` (time to regain one, 5), `base-timeout-seconds` (first lockout, 10; doubles with every repeat), `max-timeout-seconds` (600) and `strike-decay-seconds` (rest time after which one repeat is forgiven, 300). Changes apply on `/paradisu reload`, which also clears current lockouts.

### Warp permissions

| Permission | Allows |
| --- | --- |
| `paradisu.warp` | `/warp` and `/warps` |
| `paradisu.warp.other` | `/paradisu warp send` |
| `paradisu.warp.bypass-ratelimit` | Using `/warp` without the rate limit |
| `paradisu.warp.create` | `/paradisu warp create` |
| `paradisu.warp.update` | `/paradisu warp update`, `permission` and `aliases` |
| `paradisu.warp.delete` | `/paradisu warp delete` |
| *(per warp)* | A warp created with a permission (or given one with `/paradisu warp permission`) can only be used by players who have that node. When warping someone else, the sender needs the node |

## Building

```bash
git clone https://github.com/Paradisu/ParadisuPlugin
cd ParadisuPlugin
./gradlew build
```

The jar is written to `build/libs/ParadisuPlugin.jar`. See [CONTRIBUTING.md](CONTRIBUTING.md) for the development guide (project structure, formatting, database changes, commands, messages, and dependency updates). A ready-to-run development environment (devcontainer with a Velocity + Paper network and a seeded PostgreSQL database) is documented in [docs/dev-environment.md](docs/dev-environment.md).

## Contributing

Contributions are welcome and appreciated! Please read [CONTRIBUTING.md](CONTRIBUTING.md) first. AI coding agents should read [AGENTS.md](AGENTS.md).

## Reporting Bugs

- Minor bugs may be reported as GitHub Issues.
- Please report major or game-breaking bugs privately to any of the developers.

## License

Licensed under the [GNU Affero General Public License v3.0](LICENSE).
