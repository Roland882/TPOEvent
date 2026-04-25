# 🎄 TPOEvent

> **Christmas Grinch event plugin for the TPO SMP server**  
> Created: December 2025 | Version: `2025.12`

---

## 📖 Description

**TPOEvent** is a custom [Paper](https://papermc.io/) Minecraft plugin developed for the [TPO SMP](https://site.tposmp.online) server's Christmas event in December 2025. The plugin allows server administrators to spawn Grinch-themed NPC minions that hunt and attack players during the event.

---

## ✨ Features

- 🧟 **NPC Minion system** – Citizens API-based player-skinned NPCs
- ⚔️ **Automatic targeting** – NPCs locate and chase the nearest players
- 🍎 **Smart healing** – NPCs automatically eat golden apples when low on health
- 📯 **Attack Horn** – Special goat horn item used to trigger the event
- 💀 **Custom death messages** – The Grinch and his helpers appear by name in kill messages
- 🛡️ **Totem support** – NPCs can use Totem of Undying from their inventory
- 🎮 **Simple command system** – Easy-to-use `/mmm` commands

---

## 🔧 Requirements

| Requirement   | Version                 |
|---------------|-------------------------|
| Minecraft     | `1.21`                  |
| Paper API     | `1.21.10-R0.1-SNAPSHOT` |
| Java          | `21+`                   |
| Citizens      | `2.0.32-SNAPSHOT`       |
| LibsDisguises | `11.0.13` *(optional)*  |


---

## 📦 Installation

1. Download the latest `.jar` from the [Releases](../../releases) page
2. Place it in your server's `plugins/` folder
3. Make sure the **Citizens** plugin is installed and enabled
4. Restart the server

---

## 🕹️ Commands

The plugin provides a single command: `/mmm`

> ⚠️ **Only Roland882 can use these commands.**


| Command        | Description                                                                                               |
|----------------|-----------------------------------------------------------------------------------------------------------|
| `/mmm spawn`   | Spawns the Grinch minions at the player's location (2 in front + 4 behind) and grants the **Attack Horn** |
| `/mmm stop`    | Stops the NPCs from attacking and puts them into protected mode                                           |
| `/mmm despawn` | Removes all active minions                                                                                |

---

## 📯 Attack Horn

When `/mmm spawn` is executed, the player receives a special **Attack Horn** (goat horn) item.  
Right-clicking it activates all NPCs, causing them to begin hunting nearby players.

**NPC stats while attacking:**
- Speed III, Strength III, Fire Resistance
- Max HP: 50
- Detection range: 30 blocks
- Navigation updates every 40 ticks

---

## 🧑‍💻 Development

```bash
# Build
./gradlew shadowJar
 
# Test on a local server
./gradlew runServer
```

The build output is located in the `build/libs/` folder.

**Project structure:**
```
src/main/java/me/roland882/tpoevent/
├── TPOEvent.java               # Main plugin class
├── commands/
│   └── MinionCommand.java      # /mmm command handler
└── managers/
    └── MinionManager.java      # NPC logic and event handlers
```
 
---

## 👤 Author

**Roland882**

---

## 📄 License

This plugin was developed for internal use on the TPO SMP server.