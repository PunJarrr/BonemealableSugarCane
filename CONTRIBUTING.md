# Contributing to Bonemealable Sugar Cane

Thanks for helping out! Bug reports and ideas go in the
[issue tracker](https://github.com/PunJarrr/BonemealableSugarCane/issues). This page explains how to
build the plugin, test it and make a release.

## How the project works

It's a Bukkit plugin built with Gradle. One jar runs on Spigot, Paper, Purpur and Folia, Minecraft
1.17 to 26.3.

- The code is compiled against the **Spigot 1.17 API** (the oldest supported version) to **Java 16 class
  files**. Anything that doesn't exist in 1.17 doesn't compile, so the jar can't use it by accident. Newer
  API (like `Effect.BONE_MEAL_USE`) has to be looked up by name at runtime with a fallback.
- No Paper-only API, so it keeps working on Spigot. That rules out Adventure/MiniMessage: messages use `&`
  color codes.
- `src/main/java/.../growth/` holds the bone meal logic, shared by players and dispensers.
  `config/` reads and updates `config.yml`. `listener/` handles right-clicks and dispensers.
- `gradle.properties` has the plugin version and the list of Minecraft versions shown on the store pages.

Gradle downloads any Java version it needs (21 to build; 17, 21 and 25 for the test servers).

## Building

```bash
./gradlew build
```

This compiles the plugin, runs the unit tests and writes `build/libs/BonemealableSugarCane-<version>.jar`.

## Test servers

Each supported version has a test server task. It starts a server in `run/<id>/` with the plugin installed:

| Task | Server |
|---|---|
| `./gradlew runServer` | Paper 26.3 |
| `./gradlew runServer1_21_11` | Paper 1.21.11 |
| `./gradlew runServer1_20_6` | Paper 1.20.6 |
| `./gradlew runServer1_20_4` | Paper 1.20.4 |
| `./gradlew runServer1_19_4` | Paper 1.19.4 |
| `./gradlew runServer1_18_2` | Paper 1.18.2 |
| `./gradlew runServer1_17_1` | Paper 1.17.1 (oldest supported) |
| `./gradlew runFolia26_3` | Folia 26.3 |
| `./gradlew runPurpur26_3` | Purpur 26.3 |

The first start stops at the Minecraft EULA. Read it at <https://aka.ms/MinecraftEULA>, then set
`eula=true` in `run/<id>/eula.txt` to agree. Join at `localhost`.

Spigot has no test server: Spigot can't be downloaded, only built with BuildTools.

## Smoke test

The smoke test checks a server version in a few minutes without anyone joining:

```bash
scripts/smoke-test.sh 26.3 1.17.1
```

```bash
scripts/smoke-test.sh all
```

For each test server it starts a fresh flat world in `build/smoke/<id>/`, builds a sugar cane and dispenser
setup, fires the dispenser with a real redstone block, then checks:

- the plugin enables, and `/bsc version` answers;
- a dispenser grows cane to `max-height` with one bone meal, and fails like vanilla (keeping the bone meal)
  when the cane is already full;
- `grow-by-one-block`, a bigger `max-height` and `dispenser-support: false`, each changed in `config.yml`
  and applied with `/bsc reload`;
- a dispenser uses up its last bone meal;
- a broken `config.yml` is left alone and `/bsc reload` keeps the previous settings;
- the server stops cleanly, and the log has no errors, warnings or exceptions from the plugin.

The setup is built and read by a small test-only plugin in `src/smokeTest/`, because Folia's console
can't run block commands like `/execute if block`. It's never part of the release jar.

Running it accepts the [Minecraft EULA](https://aka.ms/MinecraftEULA) for those throwaway servers.
Logs are in `build/smoke/<id>.log`; `SMOKE_DEBUG=1` prints every console command and reply.
GitHub Actions runs it on every test server for each push.

## In-game checklist

Things the smoke test can't check, because they need a player or eyes and ears. Do these on
`./gradlew runServer` and `./gradlew runServer1_17_1` before a release:

1. **Hand, survival:** right-click a 1-block sugar cane with bone meal. It grows to 3, the hand swings, you
   see green particles and hear the bone meal sound, and one bone meal is used up.
2. **Off hand:** the same with bone meal in the off hand and nothing in the main hand.
3. **Full cane:** right-click a 3-block cane. Nothing happens and no bone meal is used.
4. **Creative:** bone meal grows the cane but isn't used up.
5. **Adventure:** bone meal does nothing.
6. **Dispenser:** a dispenser with bone meal, facing cane, powered with a button. The cane grows, and the
   dispenser runs out after its last bone meal.
7. **Grow by one:** set `grow-by-one-block: true`, `/bsc reload`, then each bone meal adds one block.
8. **Max height:** set `max-height: 5`, `/bsc reload`, then cane grows to 5.
9. **Effects off:** set `effects: false`, `/bsc reload`, then no particles or sound.
10. **Permissions:** a non-op player can run `/bsc version` but not `/bsc reload`.

## Releasing (maintainers)

Releases are uploaded to [Modrinth](https://modrinth.com/plugin/bonemealable-sugar-cane) and
[Hangar](https://hangar.papermc.io/PunJarrr/BonemealableSugarCane) by the `Publish` GitHub Actions
workflow. SpigotMC has no upload API, so it's done by hand.

1. Bump `version` in `gradle.properties` and `config.version` in `src/main/resources/config.yml`.
   If a config option was renamed or changed meaning, add a step to `ConfigUpdater.migrate()`; new options
   need no code.
2. If a new Minecraft version is supported, add it to `minecraftVersions` in `gradle.properties` and add a
   test server for it in `build.gradle.kts`.
3. Add the changes to `CHANGELOG.md`, with the date.
4. Run the smoke test and the in-game checklist. Commit and push, and check the `Build` workflow passes.
5. Create a GitHub release tagged `v<version>` (for example `v1.1.0`), with the changes as its description.
6. The workflow checks the tag matches the version, uploads the jar to Modrinth and Hangar with the
   supported versions and platforms, and attaches it to the GitHub release.
7. Upload the jar to [SpigotMC](https://www.spigotmc.org/resources/135245/) by hand
   (*Post Resource Update*), with the same changes.

It needs two repository secrets (Settings → Secrets and variables → Actions):

- `MODRINTH_TOKEN`: a [Modrinth personal access token](https://modrinth.com/settings/pats) with the
  *Create versions* scope.
- `HANGAR_API_TOKEN`: a Hangar API key (your Hangar profile → *API Keys*) with the `create_version`
  permission.

To check the Modrinth upload settings locally without uploading anything, run `./gradlew publishMods`
with no token set; it does a dry run.

## Pull requests

- Run `./gradlew build` and the smoke test on at least the newest and the oldest version
  (`scripts/smoke-test.sh 26.3 1.17.1`).
- If you add a config option, add it to `config.yml` with a comment, read it in `Settings.from`, and add a
  test. Keep `config.yml` to plain values (no lists or maps).
