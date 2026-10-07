# Changelog

All notable changes to BonemealableSugarCane are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses [Semantic Versioning](https://semver.org/).

## [1.1.0] - 2026-10-08

### Added

- Bone meal works from the off hand (the main hand still goes first, like vanilla) and while sneaking.
- The hand swings when bone meal is used, like vanilla.
- `/bsc help` lists the commands.
- A `messages` section in `config.yml` for the few player-facing messages (`&` color codes).
- Supports protection plugins: the plugin fires `BlockFertilizeEvent` and respects other plugins' decisions, so cane in someone else's claim can't be grown.

### Changed

- Particles and sound now use the vanilla bone meal effect: the real particle spread, and the sound follows the Blocks volume slider instead of Master.
- `spawn-particles` and `play-sound` are replaced by one `effects` option, because the vanilla effect always plays both. Your config is migrated automatically: `effects` is on if either old option was on.
- Bone meal can't be used in Adventure mode, like vanilla.
- A dispenser facing sugar cane that is already at max height now fails like vanilla (fail click, bone meal kept).
- `config.yml` is only rewritten when something needs updating, and a backup (`config-backup-<version>-<time>.yml`) is saved first.
- `max-height` below 1 and values of the wrong type are reported in the console, and the default is used.
- Version, name and author in `/bsc version` and `/bsc info` come from `plugin.yml`.

### Removed

- Multi-language support and the `language` setting. Messages are now English and can be edited or translated in `config.yml`. An existing `translations` folder is left alone; the console says once that it's no longer used.

### Fixed

- **A `config.yml` with a YAML error was replaced by the defaults, wiping every setting.** It's now never touched, and the plugin runs on the defaults until the error is fixed and `/bsc reload` is run.
- **Dispensers never used up their last bone meal**, giving unlimited bone meal.
- Dispensers now use up the kind of bone meal they actually fired (for example a renamed one), not just the first bone meal in the inventory.
- After growing cane, the item in the other hand (such as a torch) could also be used in the same click.
- `/bsc reload` with a broken `config.yml` silently switched to the default settings. It now keeps the previous settings and says so.
- The help text pointed to `/bsc help`, which didn't exist.
- Servers on 1.20 to 1.20.4 loaded the plugin and then hit errors; the plugin now declares 1.20.5 as its minimum, so those servers refuse to load it with a clear message.
- Growth stops at the world's height limit.
- Tab completion no longer suggests `reload` to players who can't use it.
- Console messages no longer contain color codes.

## [1.0.2]

### Added

- `max-height` setting.

## [1.0.1]

### Added

- Dispenser support (`dispenser-support`).

## [1.0.0]

- First release.
