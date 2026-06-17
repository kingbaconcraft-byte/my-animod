# Animation Creator — Fabric Mod for Minecraft 1.21.1

Create, edit, and apply custom character animations in Minecraft using natural-language prompts.  
**No API key required** — uses Pollinations.ai (free) and a built-in offline template engine as fallback.

---

## Features

| Feature | Description |
|---|---|
| **AI Animation Generation** | Type a prompt → get keyframed animation data instantly |
| **Offline Templates** | 12 built-in animations (walk, run, wave, attack, dance, jump, fly, death, sit, swim, idle, victory) — works without internet |
| **In-Game GUI** | Full GUI with prompt field, quick-prompt buttons, and real-time preview |
| **Animation Library** | Browse, search, play, edit, delete, import, and export animations |
| **Animation Editor** | Timeline editor with keyframe add/remove, bone selection, transform controls, loop settings |
| **Looping Controls** | loop / hold / ping_pong modes |
| **Persistent Storage** | Animations saved as JSON in `<minecraft>/animation_creator/` — survives world changes |
| **Multiplayer Support** | Server-side animation syncing via custom network packets |
| **City Hub** | Run `/animation hub` to generate a complete animation testing hub near you |
| **Commands** | `/animation list/play/stop/reload/hub/info` |

---

## Requirements

| | Version |
|---|---|
| Minecraft | 1.21.1 |
| Java | 21+ |
| Fabric Loader | ≥ 0.16.0 |
| Fabric API | 0.103.0+1.21.1 |

---

## Building from Source

### 1. Install prerequisites

```bash
# Java 21
sudo apt install openjdk-21-jdk   # Linux
brew install openjdk@21            # macOS
# Windows: download from https://adoptium.net

# Verify
java -version   # must show 21.x
```

### 2. Clone / download the project

```bash
# Place the minecraft-mod/ folder wherever you like
cd minecraft-mod
```

### 3. Make the Gradle wrapper executable (Linux / macOS)

```bash
chmod +x gradlew
```

On **Windows**, use `gradlew.bat` instead of `./gradlew`.

### 4. Build the mod

```bash
./gradlew build
```

The first build downloads Minecraft mappings and dependencies (~500 MB).  
Subsequent builds are much faster.

### 5. Locate the built JAR

```
build/libs/animation-creator-1.0.0.jar
```

> Ignore any `-sources.jar` file — only the plain jar goes in your mods folder.

---

## Installation

1. Install [Fabric Loader 0.16.x](https://fabricmc.net/use/) for Minecraft 1.21.1.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.1 and place it in your `mods/` folder.
3. Copy `animation-creator-1.0.0.jar` into your `mods/` folder.
4. Launch Minecraft with the Fabric profile.

---

## Usage

### In-Game

| Action | How |
|---|---|
| Open Animation Creator | Press **K** |
| Open Animation Library | Press **J** |
| List animations | `/animation list` |
| Play animation on self | `/animation play <id>` |
| Play animation on player | `/animation play <id> <playerName>` |
| Stop animation | `/animation stop` |
| Generate City Hub | `/animation hub` |
| Reload library from disk | `/animation reload` |
| Show animation info | `/animation info <id>` |

### AI Generation (Quick Start)

1. Press **K** to open the creator.
2. Type your prompt: *"Create a zombie walking animation"*
3. Click **Generate Animation**.
4. The mod first asks the local API server (Pollinations.ai — free), and falls back to the built-in offline template if needed.
5. The animation is saved automatically and appears in your library.

### Import / Export

- **Export**: Open Library → select animation → click **⬇ Export**.  
  File saved as `animation_export_<id>.json` in your Minecraft game directory.
- **Import**: Place any `.json` animation file in the game directory → open Library → click **⬆ Import**.

---

## Optional: AI Backend (API Server)

If you have this project's Express API server running, the mod will use **Pollinations.ai** (completely free, no account needed) to generate richer AI animations.

The server endpoint is:
```
POST http://localhost/api/animation/generate
Body: { "prompt": "your prompt here" }
```

If the server is unreachable, the mod silently falls back to the offline template engine.

---

## Animation JSON Format

Animations are stored as JSON files:

```json
{
  "id": "uuid",
  "name": "Walking",
  "description": "Bipedal walking cycle",
  "prompt": "zombie walking animation",
  "durationTicks": 20,
  "looping": true,
  "loopMode": "loop",
  "tags": ["walk", "locomotion"],
  "createdAt": 1718000000000,
  "timelines": [
    {
      "boneName": "right_leg",
      "keyframes": [
        {
          "tick": 0,
          "easing": "ease_in_out",
          "transform": {
            "rotX": 30, "rotY": 0, "rotZ": 0,
            "posX": 0,  "posY": 0, "posZ": 0,
            "scaleX": 1, "scaleY": 1, "scaleZ": 1
          }
        }
      ]
    }
  ]
}
```

**Supported bone names:** `body`, `head`, `right_arm`, `left_arm`, `right_leg`, `left_leg`, `right_wing`, `left_wing`, `tail`

---

## City Hub Layout

Run `/animation hub` to generate the hub ~10 blocks ahead of you:

```
┌─────────────────────────────────┐
│  Tutorial Signs    Quest Giver  │
│                                 │
│  Workstation A   Workstation B  │
│         ──── Plaza ────         │
│  Workstation C   Workstation D  │
│                                 │
│  Display Screens   Lamp Posts   │
└─────────────────────────────────┘
```

---

## Troubleshooting

| Problem | Fix |
|---|---|
| Build fails: "Could not resolve minecraft" | Run `./gradlew build --refresh-dependencies` |
| Mod not loading | Check Java 21 is installed and Fabric API is present |
| AI generation slow / fails | Normal — falls back to offline templates automatically |
| GUI not opening | Make sure Fabric API mod is installed, check no key conflict with K/J |
| Multiplayer: animations not syncing | Both client and server must have the mod installed |

---

## Project Structure

```
minecraft-mod/
├── build.gradle                    # Fabric Loom build config
├── settings.gradle
├── gradle.properties               # Minecraft & mod versions
├── src/main/java/com/animationcreator/
│   ├── AnimationCreatorMod.java    # Server-side initialiser
│   ├── animation/
│   │   ├── AnimationData.java      # Animation data model
│   │   ├── AnimationKeyframe.java  # Keyframe + easing
│   │   ├── BoneTransform.java      # Position/rotation/scale
│   │   ├── AnimationTimeline.java  # Per-bone keyframe list + sampler
│   │   ├── AnimationManager.java   # In-memory library + active states
│   │   └── AnimationTemplates.java # Offline template generator (12 types)
│   ├── ai/
│   │   └── AIAnimationService.java # Async HTTP → API server → Pollinations.ai
│   ├── client/
│   │   ├── AnimationCreatorClient.java   # Client initialiser + key bindings
│   │   └── gui/
│   │       ├── AnimationCreatorScreen.java  # Main creator GUI
│   │       ├── AnimationLibraryScreen.java  # Library browser
│   │       └── AnimationEditorScreen.java   # Timeline editor
│   ├── network/
│   │   └── NetworkHandler.java     # Custom packet payloads (C2S/S2C)
│   ├── storage/
│   │   └── AnimationStorage.java   # JSON file persistence
│   ├── world/
│   │   └── CityHubFeature.java     # Hub structure generator
│   └── command/
│       └── AnimationCommand.java   # /animation command tree
└── src/main/resources/
    ├── fabric.mod.json
    ├── animationcreator.mixins.json
    └── assets/animationcreator/lang/en_us.json
```

---

## License

MIT — free to use, modify, and distribute.
