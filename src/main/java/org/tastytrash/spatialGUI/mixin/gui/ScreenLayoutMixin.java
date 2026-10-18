package org.tastytrash.spatialGUI.mixin.gui;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.util.GuiScaleUtil;

@Mixin(Screen.class)
public abstract class ScreenLayoutMixin {

    @ModifyVariable(
            //? if >1.21.1 {
            method = "init(II)V",
            //? } else {
            /*method = "init(Lnet/minecraft/client/Minecraft;II)V",
            *///?}
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int spatialGUI$initWidth(int width) {
        return spatialGUI$fix(width, true);
    }

    @ModifyVariable(
            //? if >1.21.1 {
            method = "init(II)V",
            //? } else {
            /*method = "init(Lnet/minecraft/client/Minecraft;II)V",
            *///?}
            at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int spatialGUI$initHeight(int height) {
        return spatialGUI$fix(height, false);
    }

    @ModifyVariable(method = "resize", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int spatialGUI$resizeWidth(int width) {
        return spatialGUI$fix(width, true);
    }

    @ModifyVariable(method = "resize", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int spatialGUI$resizeHeight(int height) {
        return spatialGUI$fix(height, false);
    }

    @Unique
    private int spatialGUI$fix(int original, boolean isWidth) {
        Minecraft mc = Minecraft.getInstance();
        if (ConfigManager.config() == null || mc.level == null || !SpatialGUIClient.isEnabled()) return original;
        if (!SpatialGUIClient.shouldHookScreen((Screen) (Object) this)) return original;
        Window w = mc.getWindow();
        Screen self = (Screen) (Object) this;
        return isWidth ? GuiScaleUtil.scaledWidth(w, self) : GuiScaleUtil.scaledHeight(w, self);
    }
}