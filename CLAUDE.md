# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and test

```bash
./gradlew build      # compiles, runs the unit tests, builds the jar
./gradlew test       # unit tests only
```

Output jar: `build/libs/BonemealableSugarCane-<version>.jar`. There is no lint step.

Unit tests (JUnit 5, test-only dependency) cover the server-free logic: height math, config migration and template splicing, settings validation.

Test servers (run-paper, each in `run/<id>` with the right Java; foojay downloads missing JDKs). `./gradlew -q testServers` lists them: Paper 26.3 (`runServer`), 1.21.11, 1.20.6, 1.20.4, 1.19.4, 1.18.2, 1.17.1 (`runServer1_17_1`), Folia 26.3 (`runFolia26_3`), Purpur 26.3 (`runPurpur26_3`, jar fetched from the Purpur API). The EULA is not accepted for `run/`; the user does that. `-PserverDir=<dir>` runs a server elsewhere.

Smoke test: `scripts/smoke-test.sh <id>... | all` boots each server in `build/smoke/<id>/` (fresh world, accepts the EULA there, RCON on 25591) and checks dispenser growth, both grow modes, max-height, last bone meal used, dispenser-support, reload and a broken config, then scans the log. `SMOKE_DEBUG=1` prints every command. The test site is built and read by a test-only helper plugin (`src/smokeTest`, `BscSmokeTest`, jar in `build/smoke-plugin/`, loaded only with `-PsmokeTest`) because Folia's console can't run block commands; it logs `BSCTEST <id> <result>` and the script reads the log. Gotchas: the site must stay inside chunk 0,0, which the script force-loads with `/forceload` (Folia only allows that on its global thread); `pause-when-empty-seconds=-1` is needed on 1.21.2+ or the empty server stops ticking.

CI: `.github/workflows/build.yml` builds, then runs the smoke test per server in a matrix. `publish.yml` runs on a published GitHub release (tag must be `v<version>`), uploads to Modrinth (mod-publish-plugin, `publishMods`, dry run without `MODRINTH_TOKEN`) and Hangar (`publishPluginPublicationToHangar`, `HANGAR_API_TOKEN`), and attaches the jar. SpigotMC is manual. Store page texts are in `docs/store/`.

## Project overview

A Paper/Spigot plugin that adds Bedrock-style bone meal support for sugar cane. Players (either hand) and dispensers can bone meal sugar cane to grow it up to a configurable max height, as close to vanilla bone meal as the Spigot API allows.

Supports Spigot, Paper, Purpur and Folia, Minecraft 1.17 to 26.3, from one jar. Compiles against Spigot API `1.17` (the oldest supported version) to Java 16 class files (`options.release = 16`, built with a Java 21 toolchain). Newer API must be looked up by name with a fallback, like `Effect.BONE_MEAL_USE` in `CaneGrower` (from 1.19.4; older servers get `VILLAGER_PLANT_GROW` plus the bone meal sound). No runtime dependencies. `folia-supported: true` is safe because the plugin only changes blocks inside block events and schedules nothing.

## Architecture

**Entry point.** `BonemealableSugarCane.java` runs `ConfigUpdater`, builds a `Settings` snapshot, creates one `CaneGrower`, registers both listeners once and wires up the command. `/bsc reload` builds a new snapshot and swaps it in; listeners are never re-registered. If config.yml can't be parsed, the previous snapshot (or the bundled defaults at startup) stays active.

**Settings.** `config/Settings` is an immutable record validated on load (wrong types or `max-height < 1` log a warning and use the default). `config/Messages` holds the `messages` section with `&` codes translated. Listeners read the current snapshot through a `Supplier<Settings>`.

**Growth.** `growth/CaneGrower` holds all growth logic for both listeners: find the column's base, count height (capped at max-height), find free air above (stopping at the world height limit), fire `BlockFertilizeEvent`, apply the remaining states bottom-up, play the bone meal effect (15 particles). `growth/GrowthMath` is the pure, unit-tested height math.

**Listeners.** Both run at `HIGH` with `ignoreCancelled = true`.
- `SugarCaneListener` (`PlayerInteractEvent`) respects `useItemInHand() == DENY`, skips Adventure and Spectator mode, and on success cancels the event (so the other hand isn't used too), swings the hand and uses up bone meal outside Creative.
- `DispenserListener` (`BlockDispenseEvent`) cancels only on success and lowers the amount of the first `isSimilar` stack in place. Never clear the slot with `setItem(null)`: after a cancelled dispense the server writes the dispensed stack back into its slot, which would give the bone meal back. When nothing grows, the event is left alone so vanilla plays its fail click and keeps the bone meal. A cancelled dispense already gets a click and smoke from the server, so don't play those yourself.

**Config updates.** `config/ConfigUpdater` leaves config.yml alone when the version is current and no keys are missing. Otherwise it backs up the file (`config-backup-<old>-<time>.yml`), runs `migrate()`, renders the bundled config.yml with the user's values spliced in (`config/YamlTemplate`, any nesting depth, scalars only), checks that the result parses and writes it atomically. A file that fails to parse, or comes from a newer version, is never written.

## Version bump checklist

When bumping the version (e.g. `1.1.0` → `1.1.1`):

1. `gradle.properties`: `version` (and `minecraftVersions` if a new Minecraft version is supported; add its test server in `build.gradle.kts`)
2. `src/main/resources/config.yml`: `config.version`
3. `CHANGELOG.md`: add an entry
4. `ConfigUpdater.migrate()`: add an `isOlderThan("x.y.z")` block **only** if an existing value must be renamed or transformed. New keys need no code.

## Adding a new config key

Add the key to `config.yml` with its default value and a comment, then read and validate it in `Settings.from` (or add the message key to `Messages`). No migration code is needed: `ConfigUpdater` keeps existing user values and fills in new keys from the bundled default. Keep the template to scalar values; `YamlTemplate` doesn't support lists or maps. Add a test to `SettingsTest` or `ConfigUpdaterTest`.

## Known differences from vanilla

- No sculk vibration (`World#sendGameEvent` is Paper-only).
- Adventure mode is blocked entirely; the Spigot API has no way to check `can_place_on`.
- On Paper, a successful dispense plays the fail click's pitch. The server picks the click, and changing it would need packet interception.
- On 1.17 to 1.19.3 the particles and the sound are sent separately (the API has no combined bone meal effect there); they look and sound the same.
