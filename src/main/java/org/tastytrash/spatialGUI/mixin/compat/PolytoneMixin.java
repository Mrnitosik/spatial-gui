package org.tastytrash.spatialGUI.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;

@Pseudo
@Mixin(targets = "net.mehvahdjukaar.polytone.content.lightmap.LightmapsManager", remap = false)
public class PolytoneMixin {

    @Inject(method = "setupForGUI", at = @At("HEAD"), cancellable = true, remap = false)
    private void spatialGUI$skipGuiLightmap(boolean gui, CallbackInfo ci) {
        if (gui && SpatialGUIClient.isEnabled() && SpatialGUIClient.getEffectiveFirstPersonMode()) {
            var renderer = SpatialGUIClient.renderer();
            if (renderer != null && renderer.shouldCapture()) {
                ci.cancel();
            }
        }
    }
}
