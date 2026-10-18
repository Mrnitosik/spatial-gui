package org.tastytrash.spatialGUI.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.compat.ControlifyCompat;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;
import org.tastytrash.spatialGUI.util.MouseHandlerUtil;

@Pseudo
@Mixin(targets = "dev.isxander.controlify.virtualmouse.VirtualMouseHandler", remap = false)
public class ControlifyMixin {
    @Unique
    private static boolean spatialGUI$active() {
        if (!SpatialGUIClient.isEnabled()) return false;
        var renderer = SpatialGUIClient.renderer();
        return renderer != null && SpatialGUIClient.shouldHookScreen(renderer.getHookedScreen());
    }

    @ModifyVariable(method = "renderVirtualMouse", at = @At("STORE"), ordinal = 0, require = 0, remap = false)
    private double spatialGUI$cursorX(double original) {
        return spatialGUI$active() ? MouseHandlerUtil.getLastPos(true, original) : original;
    }

    @ModifyVariable(method = "renderVirtualMouse", at = @At("STORE"), ordinal = 1, require = 0, remap = false)
    private double spatialGUI$cursorY(double original) {
        return spatialGUI$active() ? MouseHandlerUtil.getLastPos(false, original) : original;
    }

    @Inject(method = "renderVirtualMouse", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void spatialGUI$hideInCrosshair(CallbackInfo ci) {
        if (SpatialGUIRenderer.isCrosshairModeActive()) ci.cancel();
    }

    //? if neoforge {
    /*@Inject(method = "renderVirtualMouse", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void spatialGUI$onlyDrawInIsolated(CallbackInfo ci) {
        if (spatialGUI$active() && !SpatialGUIRenderer.isExtractingScreen) ci.cancel();
    }
    *///?}

    @Inject(method = {"updateMouse", "snapToPoint"}, at = @At("HEAD"), require = 0, remap = false)
    private void spatialGUI$moveStart(CallbackInfo ci) {
        ControlifyCompat.inVirtualMove = true;
    }

    @Inject(method = {"updateMouse", "snapToPoint"}, at = @At("RETURN"), require = 0, remap = false)
    private void spatialGUI$moveEnd(CallbackInfo ci) {
        ControlifyCompat.inVirtualMove = false;
    }

    @Inject(method = "handleScroll", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void spatialGUI$stopScrollInCrosshair(CallbackInfo ci) {
        if (SpatialGUIRenderer.isCrosshairModeActive()) ci.cancel();
    }
}