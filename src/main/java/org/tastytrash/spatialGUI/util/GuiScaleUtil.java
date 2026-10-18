package org.tastytrash.spatialGUI.util;

import org.tastytrash.spatialGUI.config.ConfigManager;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;

public final class GuiScaleUtil {
    public static int effectiveScale(Window w) {
        return ConfigManager.config().getEffectiveGuiScale();
    }

    public static int effectiveScale(Screen screen) {
        return ConfigManager.configFor(screen).getEffectiveGuiScale();
    }

    public static int scaledWidth(Window w)  {
        return Mth.ceil(w.getWidth()  / (double) effectiveScale(w));
    }

    public static int scaledHeight(Window w) {
        return Mth.ceil(w.getHeight() / (double) effectiveScale(w));
    }

    public static int scaledWidth(Window w, Screen screen)  {
        return Mth.ceil(w.getWidth()  / (double) effectiveScale(screen));
    }

    public static int scaledHeight(Window w, Screen screen) {
        return Mth.ceil(w.getHeight() / (double) effectiveScale(screen));
    }
}