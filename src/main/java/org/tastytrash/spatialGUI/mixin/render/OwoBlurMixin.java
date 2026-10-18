package org.tastytrash.spatialGUI.mixin.render;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

/**
 * BlurProgram binds a new framebuffer and never restores the old one, so
 * during a capture everything drawn after a blurred surface ends up outside
 * our texture, so it's cancelled while capturing (for owo vers prior to 1.21.11)
 */
//? if !forge {
@Mixin(targets = "io.wispforest.owo.shader.BlurProgram", remap = false)
//?} else {
/*@Mixin(Minecraft.class)
*///?}
public class OwoBlurMixin {
    //? if <1.21.11 && !forge{
    /*@Inject(method = "use", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void spatialGUI$cancelDuringCapture(CallbackInfo ci) {
        if (SpatialGUIRenderer.isExtractingScreen) ci.cancel();
    }
    *///?}
}
