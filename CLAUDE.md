# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build

```bash
./gradlew build
```

Output jar: `build/libs/BonemealableSugarCane-<version>.jar`

No test suite exists. There is no lint step.

## Project overview

A Paper/Spigot plugin that adds Bedrock-style bone meal support for sugar cane. Players (and dispensers) can right-click sugar cane with bone meal to grow it up to a configurable max height.

Targets Spigot API `1.20.5`. Built with Java 21.

## Architecture

**Entry point** — `BonemealableSugarCane.java` loads config, runs `ConfigUpdater.migrate()`, loads translations via `LanguageManager`, registers both listeners, and wires up the command. `/bsc reload` unregisters listeners, reloads config + lang, then re-registers them.

**Listeners** — `SugarCaneListener` handles `PlayerInteractEvent`; `DispenserListener` handles `BlockDispenseEvent`. Both walk down to the base of the sugar cane column, count its height, compare against `settings.max-height`, then grow upward. Dispenser events are always cancelled to prevent bone meal ejection as an item entity; bone meal is consumed manually from the dispenser inventory.

**Config versioning** — `ConfigUpdater.migrate()` snapshots the user's current values, rewrites `config.yml` from the bundled default (preserving comments/ASCII art), then splices the user values back in. Each version bump gets an `isOlderThan` block. New keys need no migration code — they inherit the default.

**Translations** — `LanguageManager` loads all `.properties` files from `src/main/resources/translations/` into an `EnumMap<Language, Properties>` cache. Placeholders use `{key}` syntax; `send()` splits on `\n` for multi-line messages. Falls back to `en_US` for missing keys.

## Version bump checklist

When bumping the version (e.g. `1.0.2` → `1.0.3`):

1. `build.gradle.kts` — `version`
2. `src/main/resources/config.yml` — `config.version`
3. All `src/main/resources/translations/*.properties` — `meta.version`
4. `ConfigUpdater.java` — add an `isOlderThan("x.y.z")` migration block

## Adding a new config key

Add the key to `config.yml` with its default value. No migration code needed — `ConfigUpdater` automatically preserves existing user values and fills in new keys from the bundled default. Update the `grow-by-one-block` description comment if the new key relates to growth behaviour.

## Adding a new language

1. Add an entry to the `Language` enum (`lang/Language.java`)
2. Create `src/main/resources/translations/<code>.properties` (copy `en_US.properties` as a template)
3. Add the locale code to the supported languages comment in `config.yml`
