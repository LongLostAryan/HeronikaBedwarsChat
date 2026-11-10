# BedWars1058 API compatibility notes

The supplied BedWars1058 JARs were inspected with their plugin descriptors and public API signatures. HeronikaBedwarsChat targets these release versions:

- 22.3.4
- 23.2.1
- 23.3
- 23.12.2
- 24.9
- 25.2

## Shared API calls used by this plugin

The following methods were present with matching signatures in the six target APIs:

- `BedWars.getAPI()`
- `BedWars.getServerType()`
- `BedWars.getLobbyWorld()`
- `BedWars.getLevelSupport()`
- `BedWars.getChatSupport()`
- `BedWars.getAPI().getArenaUtil().getArenaByPlayer(Player)`
- `BedWars.getAPI().getPlayerLanguage(Player)`
- `IArena.isSpectator(Player)`
- `IArena.getStatus()`
- `IArena.getPlayers()` and `IArena.getSpectators()`
- `IArena.getTeam(Player)`
- `ITeam.getColor()`, `ITeam.getName()`, `ITeam.getDisplayName(Language)`, and `ITeam.getMembers()`
- `Level.getLevel(Player)`
- `Chat.getPrefix(Player)` and `Chat.getSuffix(Player)`

Later releases add other API methods to `IArena` and `ITeam`, but the methods listed above remain available. The compatibility adapters intentionally use only this shared surface.

## Supplied archive note

The uploaded archive contained two files named `BedWars1058 (5).jar` and `BedWars1058 (6).jar`. Their SHA-256 hashes were identical, and both identify themselves as version 24.9. The 23.2.1 baseline JAR came from the existing project `libs/` folder.

## Java bytecode levels

The inspected `BedWars.class` bytecode versions were:

| BedWars1058 release | Class-file major version | Minimum Java generation |
|---|---:|---:|
| 22.3.4 | 52 | Java 8 |
| 23.2.1 | 52 | Java 8 |
| 23.3 | 55 | Java 11 |
| 23.12.2 | 55 | Java 11 |
| 24.9 | 55 | Java 11 |
| 25.2 | 55 | Java 11 |

## Runtime verification

API signature comparison is not a substitute for starting each BedWars1058 release on its intended Minecraft server version. Before describing a release as production-tested, run a server test for lobby chat, waiting chat, team chat, shout, spectator chat, cooldown, and command behavior.
