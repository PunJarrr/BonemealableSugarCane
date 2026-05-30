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

- Bone meal now works on sugar cane
- Supported dispenser bone mealing
- Choose between growing by 1 block or instantly to max height
- Particles and sound effects on growth
- 6 languages supported
- Lightweight and performance-friendly

## 🔍 Showcase

![Showcase](https://cdn.modrinth.com/data/9orZw6M7/images/9c046c128ec0e6f60e6f3f5c3df81378f7ff27dd.gif)

## ⚙️ Configuration

- All settings are configurable in ```config.yml```.
- All messages are fully customizable in **translation files** using [MiniMessage](https://docs.papermc.io/adventure/minimessage/format/#standard-tags) format.

```yaml
# Plugin Settings
settings:

  # Language
  # Change the language of the plugin.
  # Supported languages:
  #  - English   (en_US)
  #  - German    (de_DE)
  #  - French    (fr_FR)
  #  - Finnish   (fi_FI)
  #  - Polish    (pl_PL)
  #  - Dutch     (nl_NL)
  language: "en_US"

  # Grow-by-one-block
  # If true, bone meal grows sugar cane by exactly 1 block.
  # If false, bone meal grows sugar cane up to the maximum height of 3 blocks at once.
  grow-by-one-block: false

  # Spawn Particles
  # Whether to spawn particles when sugar cane is bone-mealed.
  spawn-particles: true

  # Play Sound
  # Whether to play a sound effect when sugar cane is bone-mealed.
  play-sound: true

  # Dispenser Support
  # Whether dispensers can bone meal sugar cane.
  # When enabled, a dispenser facing a sugar cane column will grow it just like a player would.
  dispenser-support: true
```

## 📚 Command & Permissions

- Commands

| Command                    | Permission                          | Description                       |
| -------------------------- | ----------------------------------- | --------------------------------- |
| `/bonemealablesugarcane`   | `bonemealablesugarcane.command`     | Base plugin command.              |
| `/bonemealablesugarcane reload` | `bonemealablesugarcane.reload` | Reload config and translations.   |

> Alias: `/bsc`

- Permissions

| Permission                          | Description                          | Default   |
| ----------------------------------- | ------------------------------------ | --------- |
| `bonemealablesugarcane.command`     | Allows use of `/bonemealablesugarcane`. | `true` |
| `bonemealablesugarcane.reload`      | Allows reloading config and translations. | `op` |
