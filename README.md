# ExcellentCrates-Plus

[![Build](https://github.com/Faboit1/ExcellentCrates-Plus/actions/workflows/build.yml/badge.svg)](https://github.com/Faboit1/ExcellentCrates-Plus/actions/workflows/build.yml)

A fork of [ExcellentCrates](https://github.com/nulli0n/ExcellentCrates) with **Folia support** and quality-of-life improvements.

## What's Changed

### Folia Support
All Bukkit scheduler calls have been replaced with Folia's region-aware scheduler APIs:
- `GlobalRegionScheduler` for world-tick tasks
- `RegionScheduler` for location-bound operations (holograms, block checks)
- `entity.getScheduler()` for per-player operations (crate openings)
- `AsyncScheduler` for async database operations

Works with **stock NightCore 2.15.2** — no custom NightCore build required on the server.

### No Message Prefixes
The `[ExcellentCrates]` prefix has been fully removed from all plugin messages. Clean output with no configuration needed.

### Removed Premium Dependencies
Integration code for premium/paid plugins (Nexo, ExecutableItems, MMOItems, etc.) has been removed so the plugin compiles without proprietary stubs.

## Requirements

| | Version |
|---|---|
| Server | [Paper](https://papermc.io/downloads/paper) or [Folia](https://papermc.io/software/folia) |
| Minecraft | 1.21.8+ |
| Java | 21+ |
| NightCore | [2.15.2+](https://nightexpressdev.com/nightcore/) |

**Optional:** [PacketEvents](https://spigotmc.org/resources/80279/) or [ProtocolLib](https://ci.dmulloy2.net/job/ProtocolLib/) for crate holograms.

## Downloads

Grab the latest JAR from [Releases](https://github.com/Faboit1/ExcellentCrates-Plus/releases) — every push to `main` auto-builds and publishes a release.

## Building

```bash
mvn clean package -DskipTests
```

NightCore 2.10.0 is bundled in `libs/` and installed to the local Maven repo automatically during CI. For local builds, run this first:

```bash
mvn install:install-file \
  -Dfile=libs/nightcore-2.10.0-shaded.jar \
  -DgroupId=su.nightexpress.nightcore \
  -DartifactId=main \
  -Dversion=2.10.0 \
  -Dclassifier=shaded \
  -Dpackaging=jar \
  -DgeneratePom=true
```

## Credits

- [NightExpress](https://github.com/nulli0n) — original ExcellentCrates plugin
- [NightCore](https://github.com/nulli0n/nightcore-spigot) — plugin engine
- [ExcellentCrates Wiki](https://nightexpressdev.com/excellentcrates/) — documentation
