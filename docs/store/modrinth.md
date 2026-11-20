# <p align="center">Bonemealable Sugar Cane</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/paper_vector.svg" alt="paper">
  <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/spigot_vector.svg" alt="spigot">
  <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/purpur_vector.svg" alt="purpur">
  <br>
  <a href="https://hangar.papermc.io/PunJarrr/BonemealableSugarCane">
    <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy-minimal/available/hangar_vector.svg" alt="hangar">
  </a>
  <a href="https://www.spigotmc.org/resources/135245/">
    <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy-minimal/available/spigot_vector.svg" alt="spigot">
  </a>
  <a href="https://github.com/PunJarrr/BonemealableSugarCane">
    <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy-minimal/available/github_vector.svg" alt="github">
  </a>
</p>

##

In Bedrock Edition you can bone meal sugar cane. **Bonemealable Sugar Cane** brings that to Java Edition servers, and keeps it as close to vanilla bone meal as possible.

## ✨ Features

- **Bone meal sugar cane** from either hand, with the vanilla particles, sound and hand swing
- **Dispenser support** for automatic cane farms
- **Grow to the top** (3 blocks, like vanilla) **or one block at a time**
- **Plays by the rules:** no bone meal used in Creative, none in Adventure mode, and protection plugins can block it in claims
- **One jar** for Paper, Purpur, Spigot and Folia, Minecraft **1.17 – 26.3**
- **Lightweight:** no dependencies, nothing to install for players

## 🔍 Showcase

![Showcase](https://cdn.modrinth.com/data/9orZw6M7/images/9c046c128ec0e6f60e6f3f5c3df81378f7ff27dd.gif)

## 📦 Supported Versions

| Minecraft | Paper | Purpur | Spigot | Folia |
|---|:-:|:-:|:-:|:-:|
| 1.19.4 – 26.3 | ✅ | ✅ | ✅ | ✅ |
| 1.17 – 1.19.3 | ✅ | ✅ | ✅ | ❌ |

Folia only exists from 1.19.4.

<details>
<summary>⚙️ Configuration</summary>

All settings are in `plugins/BonemealableSugarCane/config.yml`. Change them, then run `/bsc reload`.

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

When you update, your settings are kept and a backup of the old `config.yml` is saved first.
</details>

<details>
<summary>📚 Commands & Permissions</summary>

`/bonemealablesugarcane` can be shortened to `/bsc`.

| Command | Permission | Default | Description |
|---|---|---|---|
| `/bsc help` | `bonemealablesugarcane.command` | everyone | List the commands |
| `/bsc version` | `bonemealablesugarcane.command` | everyone | Show the plugin version |
| `/bsc info` | `bonemealablesugarcane.command` | everyone | Show plugin information |
| `/bsc reload` | `bonemealablesugarcane.reload` | operators | Reload `config.yml` |

Using bone meal needs no permission.
</details>

<details>
<summary>❓ FAQ</summary>

**I'm updating from 1.0.x. Do I need to change anything?**
No, your settings carry over. The `language` setting is gone: messages are now in `config.yml`, in English, and you can translate them there. `spawn-particles` and `play-sound` became one `effects` option.

**Why doesn't it work in Adventure mode?**
Vanilla doesn't let you use bone meal in Adventure mode either.

**Does it work with Paper's `max-growth-height.cane`?**
Yes. Set `max-height` to the same number.
</details>

## 💬 Feedback

Found a bug or have an idea? Open an issue on [GitHub](https://github.com/PunJarrr/BonemealableSugarCane/issues).
