# Spatial GUI -> Minecraft 1.21.4 (Fabric)

Added Stonecutter node `1.21.4-fabric`.

* It is registered with the version tag `1.21.1` (settings.gradle), so every existing
  `1.21.1` code path (immediate-mode GuiGraphics, render-target capture, Matrix4f
  projection) is reused.
* A new constant `mc1214` (stonecutter.gradle.kts) marks the places where 1.21.2-1.21.4
  differ from 1.21.1:
  - player rendering via `PlayerRenderState` (AvatarRendererMixin, PlayerModelMixin)
  - `LocalPlayer.applyInput` instead of `serverAiStep`
  - `GameRenderer.getFov` returns float
  - `processBlurEffect` signature, `PostChain.process` redirect disabled
  - `CoreShaders.POSITION_TEX_COLOR / POSITION_COLOR` instead of `GameRenderer::get...Shader`
  - recipe book hooks target `AbstractRecipeBookScreen`
* Dependencies (build.gradle): Fabric API 0.119.4+1.21.4, YACL 3.6.6+1.21.4-fabric, Mod Menu 13.0.3, Java 21.

Build: `./gradlew "Set active project to 1.21.4-fabric"` then `./gradlew build`
(output: build/libs/spatial-gui-1.8+1.21.4-fabric.jar).

NOTE: not compiled in the authoring environment (no network). See the chat for the list of
risky spots to check if the build or the game complains.
