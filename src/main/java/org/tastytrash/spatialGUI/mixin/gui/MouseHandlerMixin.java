package org.tastytrash.spatialGUI.mixin.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Vector2d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.compat.ControlifyCompat;
import org.tastytrash.spatialGUI.compat.InvMoveCompat;
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;
import org.tastytrash.spatialGUI.util.MouseHandlerUtil;

@Mixin(value = MouseHandler.class, priority = 1100)
public class MouseHandlerMixin {

    //? if <26.3 {
    @Shadow private double accumulatedDX;
    @Shadow private double accumulatedDY;
    //?}

    @Unique private double spatialGUI$preReleaseX;
    @Unique private double spatialGUI$preReleaseY;

    @Unique
    private static boolean shouldApplyMouseOverride() {
        if (!SpatialGUIClient.isEnabled()) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;
        //? if >=26.2 {
        /*Screen screen = mc.gui.screen();
         *///?} else {
        Screen screen = mc.screen;
        //?}
        if (SpatialGUIClient.shouldHookScreen(screen)) return true;
        var renderer = SpatialGUIClient.renderer();
        return renderer != null && SpatialGUIClient.shouldHookScreen(renderer.getHookedScreen());
    }

    //? if >1.21.1 {
    @ModifyReturnValue(method = "getScaledXPos(Lcom/mojang/blaze3d/platform/Window;)D", at = @At("RETURN"))
    private double spatialGUI$overrideScaledX(double original) {
        return shouldApplyMouseOverride() ? MouseHandlerUtil.getLastPos(true, original) : original;
    }

    @ModifyReturnValue(method = "getScaledYPos(Lcom/mojang/blaze3d/platform/Window;)D", at = @At("RETURN"))
    private double spatialGUI$overrideScaledY(double original) {
        return shouldApplyMouseOverride() ? MouseHandlerUtil.getLastPos(false, original) : original;
    }
    //?} else {
    /*@Unique
    private static double spatialGUI$override(boolean x, double original) {
        if (!shouldApplyMouseOverride()) return original;
        var w = Minecraft.getInstance().getWindow();
        double scaled = MouseHandlerUtil.getLastPos(x, original);
        return x ? scaled * w.getScreenWidth() / (double) w.getGuiScaledWidth()
                : scaled * w.getScreenHeight() / (double) w.getGuiScaledHeight();
    }

    @ModifyExpressionValue(
        //? if >1.20.1 {
        method = {"onPress", "onScroll", "handleAccumulatedMovement"},
        //?} else {
        /^method = {"onPress", "onScroll"},
        ^///?}
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;xpos:D"))
    private double spatialGUI$overrideX(double original) {
        return spatialGUI$override(true, original);
    }

    @ModifyExpressionValue(
        //? if >1.20.1 {
        method = {"onPress", "onScroll", "handleAccumulatedMovement"},
        //?} else {
        /^method = {"onPress", "onScroll"},
        ^///?}
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;ypos:D"))
    private double spatialGUI$overrideY(double original) {
        return spatialGUI$override(false, original);
    }
    *///?}

    @WrapMethod(method = "onMove")
            //? if >=26.3 {
    /*private void spatialGUI$onMove(long handle, double x, double y, double xrel, double yrel, Operation<Void> original) {
     *///?} else {
    private void spatialGUI$onMove(long handle, double x, double y, Operation<Void> original) {
        //?}
        Minecraft mc = Minecraft.getInstance();
        //? if >1.21.1 {
        if (handle != mc.getWindow().handle()) return;
        //?} else {
        /*if (handle != mc.getWindow().getWindow()) return;
         *///?}

        MouseHandlerAccessor acc = (MouseHandlerAccessor) this;
        boolean grabbed = acc.getMouseGrabbed();
        boolean ignoreFirst = acc.getIgnoreFirstMove();
        double outX = x;
        double outY = y;
        double[] delta = null;
        boolean restoreTurn = false;

        if (!(x == -50.0 && y == -50.0 && !grabbed)) {
            delta = MouseHandlerUtil.captureMove(x, y, grabbed);
            //? if >=26.3 {
            /*delta[0] = xrel;
            delta[1] = yrel;
            *///?}

            var renderer = SpatialGUIClient.renderer();
            if (renderer != null && shouldApplyMouseOverride()) {
                if (SpatialGUIRenderer.isCrosshairModeActive() && !ignoreFirst && !ControlifyCompat.inVirtualMove) {
                    MouseHandlerUtil.addFreeLookDelta(delta[0], delta[1]);
                }
                Vector2d mouse = renderer.updateMousePosition(
                        MouseHandlerUtil.getSourceX(), MouseHandlerUtil.getSourceY());
                outX = mouse != null ? mouse.x : acc.getRawXpos();
                outY = mouse != null ? mouse.y : acc.getRawYpos();
                restoreTurn = InvMoveCompat.playerRotates();
            }
        }

        //? if >=26.3 {
        /*original.call(handle, outX, outY, xrel, yrel);
         *///?} else {
        double beforeX = this.accumulatedDX;
        double beforeY = this.accumulatedDY;
        original.call(handle, outX, outY);
        if (restoreTurn) {
            boolean accumulate = grabbed && !ignoreFirst;
            this.accumulatedDX = beforeX + (accumulate ? delta[0] : 0.0);
            this.accumulatedDY = beforeY + (accumulate ? delta[1] : 0.0);
        }
        //?}
    }

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
            //? if >1.20.1 {
    private void spatialGUI$cancelPlayerRotation(double mousea, CallbackInfo ci) {
        //?} else {
        /*private void spatialGUI$cancelPlayerRotation(CallbackInfo ci) {
         *///?}
        if (!SpatialGUIRenderer.isCrosshairModeActive() || InvMoveCompat.playerRotates()) return;
        //? if <=1.21.1 {
        /*if (!shouldApplyMouseOverride()) {
            MouseHandlerUtil.addFreeLookDelta(this.accumulatedDX, this.accumulatedDY);
        }
        this.accumulatedDX = 0.0;
        this.accumulatedDY = 0.0;
        *///?}
        ci.cancel();
    }

    @Inject(method = "releaseMouse", at = @At("HEAD"))
    private void spatialGUI$capturePreRelease(CallbackInfo ci) {
        var acc = (MouseHandlerAccessor) (Object) this;
        spatialGUI$preReleaseX = acc.getRawXpos();
        spatialGUI$preReleaseY = acc.getRawYpos();
    }

    @Inject(method = "releaseMouse", at = @At("TAIL"))
    private void spatialGUI$restoreCursorFields(CallbackInfo ci) {
        boolean crosshairIncoming = ConfigManager.config() != null
                && ConfigManager.config().useCrosshairForFirstPerson
                && SpatialGUIClient.getEffectiveFirstPersonMode();
        Minecraft mc = Minecraft.getInstance();
        //? if >=26.2 {
        /*Screen screen = mc.gui.screen();
         *///?} else {
        Screen screen = mc.screen;
        //?}
        if (crosshairIncoming || screen == null || !SpatialGUIClient.shouldHookScreen(screen)) return;
        var window = mc.getWindow();
        if (spatialGUI$preReleaseX < 0 || spatialGUI$preReleaseY < 0
                || spatialGUI$preReleaseX > window.getScreenWidth()
                || spatialGUI$preReleaseY > window.getScreenHeight()) return;
        var acc = (MouseHandlerAccessor) (Object) this;
        acc.setRawXpos(spatialGUI$preReleaseX);
        acc.setRawYpos(spatialGUI$preReleaseY);
    }
}