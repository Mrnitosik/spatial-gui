# Welcome to the Spatial GUI GitHub!

Have a question, idea, or general feedback? Go to [GitHub Discussions](https://github.com/tastytrash/Spatial-GUI/discussions)!

For reproducible bugs and crashes, please open a [GitHub Issue](https://github.com/tastytrash/Spatial-GUI/issues).
___
<br><br>

# Spatial GUI

**A client-side Minecraft mod that turns screens into immersive 3D displays, with fully configurable first-person and third-person modes.**

[![Spatial GUI](https://modfolio.creeperkatze.dev/modrinth/project/spatial-gui/downloads)](https://modrinth.com/mod/spatial-gui)
[![Spatial GUI](https://modfolio.creeperkatze.dev/curseforge/project/1676741/downloads)](https://www.curseforge.com/minecraft/mc-mods/spatial-gui)
___
## Features

### General

* Per-screen enable and disable lists for mod compatibility
* Optional world blur behind the screen
* Screen drop shadow
* Configurable HUD and hotbar visibility
* Adjustable GUI scale, render scale, screen opacity, and FOV-aware scaling
* Screen opening animation
* FOV override
* Auto-detect camera mode, switches the camera mode based on perspective

### First-person Mode
![First-person menu crafting GIF](https://cdn.modrinth.com/data/cached_images/961b53d75867806bf45fb6f7df0e5e34b242b322.gif)

*   Crosshair and mouse modes
*   Screen position, distance, scale, and rotation are all configurable
*   Configurable screen opening animation
*   Curved screen option, especially good with mods that add large GUI extensions, such as JEI

### Third-person Mode
![third-person screenshot](https://cdn.modrinth.com/data/cached_images/74d080ca00474431a2d1fed3da972c82ef43b7c1.png)

*   Camera and screen position, distance, scale, and rotation are all configurable
*   Configurable avatar body rotation
*   Smooth camera transition and configurable screen opening animation
*   Optional fallback to first person when the camera is blocked

## Dependencies

| Loader | Mod                   |
|--------|-----------------------|
| All    | YACL                  |
| Fabric | Fabric API & Mod Menu |

## Configuration

Open the config through the keybind (unbound by default), Mod Menu, or the config file.
Spatial GUI has many options, so it is worth looking through them to tailor the mod to your preferences.

For advanced users and modpack creators, per-screen configuration profiles are supported via `config/spatial-gui/profiles.json5`. This file contains documentation.

## Compatibility

Spatial GUI is a client-side mod and does not need to be installed on the server.

However, compatibility isn't guaranteed with every mod. Spatial GUI modifies GUI and camera rendering, which may conflict with mods that modify these as well.

If you find a reproducible incompatibility with another mod, please report it on GitHub so it can be investigated. Reporting an issue helps make the mod better for everyone!

[Punchy](https://modrinth.com/mod/punchy-fpa) has built-in compatibility with Spatial GUI in first-person mode! This can be toggled in Punchy's config. 

# FAQ
| Question                                        | Answer                                                                                                                                                                                                           |
|-------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Does Spatial GUI work in multiplayer?**       | Yes. Other players and the server do not need to have Spatial GUI installed.                                                                                                                                     |
| **Does Spatial GUI give a gameplay advantage?** | Spatial GUI is primarily visual and does not add or automate gameplay mechanics. Some settings can change how the camera behaves, so whether it is allowed on a particular server is up to that server's rules.  |
| **Is Spatial GUI compatible with _____?**       | Probably! However, compatibility isn't guaranteed, especially with mods that modify GUIs, cameras, or rendering. If you encounter an issue with a specific mod, feel free to report it on GitHub.                |
| **Why is my GUI behaving strangely?**           | Another installed mod may be incompatible. Try to identify the mod involved, then check existing GitHub issues or open a new issue with steps to reproduce the problem.                                          |
| **Can I use Spatial GUI in a modpack?**         | Yes. Spatial GUI is welcome in modpacks and no additional permission is required. Compatibility with other mods is not guaranteed.                                                                               |
| **Is Spatial GUI purely visual?**               | Almost entirely. Spatial GUI changes how existing screens and the camera are presented. One setting intentionally allows your rotation to persist after closing a GUI, which can turn your character's view.     |

## Notes
*   Feel free to suggest features or report bugs on the GitHub.
*   Spatial GUI is welcome in modpacks, no additional permission required. Be aware that compatibility with some mods may vary.

### Special thanks to:
vibing for all of their GitHub contributions  
Dizabanik for tons of code fixes and improvements  
BUILDLCS for the Brazilian Portuguese translation  
suheckii for the Russian translation  
22SSendo & jankeverse for particularly awesome feature suggestions  
Anyone else who reported bugs or suggested features :)
