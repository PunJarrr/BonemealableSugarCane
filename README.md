<div align="center">

<img src="https://cdn.modrinth.com/data/9orZw6M7/bc4c2a18cd6ceec0d4cff5321b012fbfe6c87d28.png" alt="Bonemealable Sugar Cane icon" width="128">

# Bonemealable Sugar Cane

**Bone meal on sugar cane, like Bedrock Edition.**

[![Available on Modrinth](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/plugin/bonemealable-sugar-cane)
[![Available on Hangar](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/available/hangar_vector.svg)](https://hangar.papermc.io/PunJarrr/BonemealableSugarCane)
[![Available on SpigotMC](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/available/spigot_vector.svg)](https://www.spigotmc.org/resources/135245/)

![Supports Paper](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/paper_vector.svg)
![Supports Spigot](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/spigot_vector.svg)
![Supports Purpur](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/purpur_vector.svg)

<img src="https://cdn.modrinth.com/data/9orZw6M7/images/9c046c128ec0e6f60e6f3f5c3df81378f7ff27dd.gif" alt="Bone meal growing sugar cane">

</div>

## About

In Bedrock Edition you can bone meal sugar cane; in Java Edition you can't. This server plugin adds it,
and keeps it as close to vanilla bone meal as possible.

- **Bone meal from either hand**, with the vanilla particles, sound and hand swing.
- **Dispensers** bone meal sugar cane too, so cane farms can be automated.
- **Grow to the top or one block at a time**: one bone meal grows the cane straight to its max height
  (3, like vanilla), or by one block per use.
- **Plays by the rules**: no bone meal used in Creative, none in Adventure mode, and protection plugins
  can stop it in claims they protect.
- **Lightweight**: one small jar, no dependencies, settings in `config.yml`.

## Supported versions

One jar works on every supported version.

| Minecraft | Paper | Purpur | Spigot | Folia |
|---|:-:|:-:|:-:|:-:|
| 26.1 – 26.3 | ✓ | ✓ | ✓ | ✓ |
| 1.21 – 1.21.11 | ✓ | ✓ | ✓ | ✓ |
| 1.20 – 1.20.6 | ✓ | ✓ | ✓ | ✓ |
| 1.19.4 | ✓ | ✓ | ✓ | ✓ |
| 1.17 – 1.19.3 | ✓ | ✓ | ✓ | — |

Folia only exists from 1.19.4. Java 16 or newer is needed, which every supported Minecraft version already requires.

## Configuration

Everything is in `plugins/BonemealableSugarCane/config.yml`. Change it, then run `/bsc reload`.

```yaml
settings:
  # The tallest a sugar cane column can be grown with bone meal. Must be 1 or higher.
  # Defaults to 3 (vanilla). If you've changed max-growth-height.cane in Paper's
  # paper-world-defaults.yml, set this to match.
  max-height: 3

  # If true, each bone meal grows sugar cane by exactly 1 block.
  # If false, one bone meal grows sugar cane straight to max-height.
  grow-by-one-block: false

  # Whether to show the vanilla bone meal particles and sound when sugar cane grows.
  effects: true

  # Whether dispensers can bone meal sugar cane.
  dispenser-support: true

# Use & color codes (e.g. &a, &c, &l). Words in {braces} are filled in by the plugin.
# Set a message to "" to turn it off.
messages:
  no-permission: "&cYou don't have permission to do that."
  unknown-subcommand: "&cUnknown subcommand '&f{subcommand}&c'. Use &f/{label} help&c to see all commands."
  reload-success: "&aConfig reloaded."
  reload-failed: "&cCouldn't reload: config.yml has an error, so the previous settings are still active. Check the console for details."
```

When you update the plugin, your settings are kept: new options are added with their defaults, and a
backup of the old file (`config-backup-<version>-<time>.yml`) is saved first. A `config.yml` with a
mistake in it is never overwritten; the plugin says so in the console and keeps running.

## Commands and permissions

`/bonemealablesugarcane` can be shortened to `/bsc`.

| Command | Permission | Default | What it does |
|---|---|---|---|
| `/bsc help` | `bonemealablesugarcane.command` | everyone | Lists the commands |
| `/bsc version` | `bonemealablesugarcane.command` | everyone | Shows the plugin version |
| `/bsc info` | `bonemealablesugarcane.command` | everyone | Shows the version, author and website |
| `/bsc reload` | `bonemealablesugarcane.reload` | operators | Reloads `config.yml` |

Using bone meal needs no permission.

## Download

Get it from [Modrinth](https://modrinth.com/plugin/bonemealable-sugar-cane),
[Hangar](https://hangar.papermc.io/PunJarrr/BonemealableSugarCane),
[SpigotMC](https://www.spigotmc.org/resources/135245/) or
[GitHub releases](https://github.com/PunJarrr/BonemealableSugarCane/releases), and put the jar in your
server's `plugins` folder. Players don't need to install anything.

## FAQ

**I'm updating from 1.0.x. Do I need to change anything?**
No. Your settings carry over. Two things changed: the `language` setting is gone (messages are now in
`config.yml`, in English, and you can translate them there), and `spawn-particles` and `play-sound` became
one `effects` option. See the [changelog](CHANGELOG.md).

**Why doesn't it work in Adventure mode?**
Vanilla doesn't let you use bone meal in Adventure mode either.

**Can players grow cane in someone else's claim?**
No, as long as your protection plugin protects against bone meal. The plugin asks other plugins first.

**Does it work with Paper's `max-growth-height.cane` setting?**
Yes. Set `max-height` to the same number.

## Contributing

Bug reports, ideas and pull requests are welcome on the
[issue tracker](https://github.com/PunJarrr/BonemealableSugarCane/issues). To build the plugin, run the
test servers or make a release, see [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[MIT](LICENSE). The plugin includes no third-party code or assets; see [credits.txt](credits.txt).
