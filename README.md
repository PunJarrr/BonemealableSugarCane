# <p align="center">Bonemealable Sugar Cane</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/paper_vector.svg" alt="paper">
  <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/spigot_vector.svg" alt="spigot">
  <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/purpur_vector.svg" alt="purpur">
  <a href="https://modrinth.com/plugin/bonemealable-sugar-cane/">
    <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy-minimal/available/modrinth_vector.svg" alt="modrinth">
  </a>
  <a href="https://www.spigotmc.org/resources/bonemealable-sugar-cane-1-20-5-26-1-2.135245/">
    <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy-minimal/available/spigot_vector.svg" alt="spigot">
  </a>
  <a href="https://hangar.papermc.io/PunJarrr/BonemealableSugarCane/">
    <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy-minimal/available/hangar_vector.svg" alt="hangar">
  </a>
</p>

##

A lightweight Minecraft plugin that allows players to use bone meal on sugar cane.

## ✨ Features

- Bone meal now works on sugar cane, from either hand, and from dispensers
- Feels like vanilla: the real bone meal particles and sound, hand swing, no bone meal used in Creative, not usable in Adventure mode
- Choose between growing by 1 block or instantly to max height
- Respects protection plugins (fires `BlockFertilizeEvent`), so cane in someone else's claim can't be grown
- Lightweight and performance-friendly

## 🧩 Compatibility

- Paper, Spigot and Purpur
- Minecraft 1.20.5 to 26.x

## 🔍 Showcase

![Showcase](https://cdn.modrinth.com/data/9orZw6M7/images/9c046c128ec0e6f60e6f3f5c3df81378f7ff27dd.gif)

## ⚙️ Configuration

- All settings are in ```config.yml```.
- Messages are in the `messages` section of ```config.yml``` and use `&` color codes, so you can reword or translate them.
- When you update the plugin, your settings are kept and a backup of your old ```config.yml``` is saved next to it.

```yaml
# Plugin Settings
settings:

  # Max Height
  # The tallest a sugar cane column can be grown with bone meal. Must be 1 or higher.
  # Defaults to 3 (vanilla). If you've changed max-growth-height.cane in Paper's
  # paper-world-defaults.yml, set this to match.
  max-height: 3

  # Grow-by-one-block
  # If true, each bone meal grows sugar cane by exactly 1 block.
  # If false, one bone meal grows sugar cane straight to max-height.
  grow-by-one-block: false

  # Effects
  # Whether to show the vanilla bone meal particles and sound when sugar cane grows.
  # (Replaces spawn-particles and play-sound from 1.0.x.)
  effects: true

  # Dispenser Support
  # Whether dispensers can bone meal sugar cane.
  # When enabled, a dispenser facing a sugar cane column grows it just like a player would.
  dispenser-support: true

# Messages
# Use & color codes (e.g. &a, &c, &l). Words in {braces} are filled in by the plugin.
# Set a message to "" to turn it off.
messages:
  no-permission: "&cYou don't have permission to do that."
  unknown-subcommand: "&cUnknown subcommand '&f{subcommand}&c'. Use &f/{label} help&c to see all commands."
  reload-success: "&aConfig reloaded."
  reload-failed: "&cCouldn't reload: config.yml has an error, so the previous settings are still active. Check the console for details."
```

## 📚 Command & Permissions

- Commands

| Command                           | Permission                      | Description                     |
| --------------------------------- | ------------------------------- | ------------------------------- |
| `/bonemealablesugarcane help`     | `bonemealablesugarcane.command` | List the commands.              |
| `/bonemealablesugarcane version`  | `bonemealablesugarcane.command` | Show the plugin version.        |
| `/bonemealablesugarcane info`     | `bonemealablesugarcane.command` | Show plugin information.        |
| `/bonemealablesugarcane reload`   | `bonemealablesugarcane.reload`  | Reload `config.yml`.            |

> Alias: `/bsc`

- Permissions

| Permission                          | Description                             | Default   |
| ----------------------------------- | --------------------------------------- | --------- |
| `bonemealablesugarcane.command`     | Allows use of `/bonemealablesugarcane`. | `true`    |
| `bonemealablesugarcane.reload`      | Allows reloading the config.            | `op`      |
