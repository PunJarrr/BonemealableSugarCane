# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and test

```bash
./gradlew build      # compiles, runs the unit tests, builds the jar
./gradlew test       # unit tests only
```

Output jar: `build/libs/BonemealableSugarCane-<version>.jar`. There is no lint step.

Unit tests (JUnit 5, test-only dependency) cover the server-free logic: height math, config migration and template splicing, settings validation.

Test servers (run-paper, each in `run/<version>` with the right Java; foojay downloads missing JDKs):

| Task | Paper | Java |
|---|---|---|
| `./gradlew runServer` | 26.2 | 25 |
| `./gradlew runServer1_21_11` | 1.21.11 | 21 |
| `./gradlew runServer1_20_6` | 1.20.6 (oldest supported) | 21 |

Headless smoke test: `(sleep 60; echo stop) | ./gradlew <task> --console=plain > build/smoke/<v>.log`. Give a fresh run dir enough time: Paper 26.2 throws if `stop` arrives before the world has finished generating.

## Project overview

A Paper/Spigot plugin that adds Bedrock-style bone meal support for sugar cane. Players (either hand) and dispensers can bone meal sugar cane to grow it up to a configurable max height, as close to vanilla bone meal as the Spigot API allows.

Supports Spigot, Paper and Purpur 1.20.5 to 26.x. Compiles against Spigot API `1.20.5` (the oldest supported version) with Java 21. No runtime dependencies.

## Architecture

**Entry point.** `BonemealableSugarCane.java` runs `ConfigUpdater`, builds a `Settings` snapshot, creates one `CaneGrower`, registers both listeners once and wires up the command. `/bsc reload` builds a new snapshot and swaps it in; listeners are never re-registered. If config.yml can't be parsed, the previous snapshot (or the bundled defaults at startup) stays active.

**Settings.** `config/Settings` is an immutable record validated on load (wrong types or `max-height < 1` log a warning and use the default). `config/Messages` holds the `messages` section with `&` codes translated. Listeners read the current snapshot through a `Supplier<Settings>`.

**Growth.** `growth/CaneGrower` holds all growth logic for both listeners: find the column's base, count height (capped at max-height), find free air above (stopping at the world height limit), fire `BlockFertilizeEvent`, apply the remaining states bottom-up, play `Effect.BONE_MEAL_USE` (15). `growth/GrowthMath` is the pure, unit-tested height math.

**Listeners.** Both run at `HIGH` with `ignoreCancelled = true`.
- `SugarCaneListener` (`PlayerInteractEvent`) respects `useItemInHand() == DENY`, skips Adventure and Spectator mode, and on success cancels the event (so the other hand isn't used too), swings the hand and uses up bone meal outside Creative.
- `DispenserListener` (`BlockDispenseEvent`) cancels only on success and lowers the amount of the first `isSimilar` stack in place. Never clear the slot with `setItem(null)`: after a cancelled dispense the server writes the dispensed stack back into its slot, which would give the bone meal back. When nothing grows, the event is left alone so vanilla plays its fail click and keeps the bone meal. A cancelled dispense already gets a click and smoke from the server, so don't play those yourself.

**Config updates.** `config/ConfigUpdater` leaves config.yml alone when the version is current and no keys are missing. Otherwise it backs up the file (`config-backup-<old>-<time>.yml`), runs `migrate()`, renders the bundled config.yml with the user's values spliced in (`config/YamlTemplate`, any nesting depth, scalars only), checks that the result parses and writes it atomically. A file that fails to parse, or comes from a newer version, is never written.

## Version bump checklist

When bumping the version (e.g. `1.1.0` → `1.1.1`):

1. `build.gradle.kts`: `version`
2. `src/main/resources/config.yml`: `config.version`
3. `CHANGELOG.md`: add an entry
4. `ConfigUpdater.migrate()`: add an `isOlderThan("x.y.z")` block **only** if an existing value must be renamed or transformed. New keys need no code.

## Adding a new config key

Add the key to `config.yml` with its default value and a comment, then read and validate it in `Settings.from` (or add the message key to `Messages`). No migration code is needed: `ConfigUpdater` keeps existing user values and fills in new keys from the bundled default. Keep the template to scalar values; `YamlTemplate` doesn't support lists or maps. Add a test to `SettingsTest` or `ConfigUpdaterTest`.

## Known differences from vanilla

- No sculk vibration (`World#sendGameEvent` is Paper-only, not in Spigot 1.20.5).
- Adventure mode is blocked entirely; Spigot 1.20.5 has no API for the `can_place_on` component.
- On Paper, a successful dispense plays the fail click's pitch. The server picks the click, and changing it would need packet interception.
