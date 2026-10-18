package org.tastytrash.spatialGUI.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class ConfigManager {
    private static ConfigProfiles profiles = ConfigProfiles.EMPTY;

    private static final Map<String, SpatialGUIConfig> cache = new HashMap<>();
    private static SpatialGUIConfig lastHooked;

    public static Path profilesPath() {
        return SpatialGUIConfig.configDir().resolve("spatial-gui").resolve("profiles.json5");
    }

    public static SpatialGUIConfig config() {
        SpatialGUIConfig global = SpatialGUI.config;
        if (global == null || profiles.isEmpty()) return global;

        var renderer = SpatialGUIClient.renderer();
        Screen hooked = renderer != null ? renderer.getHookedScreen() : null;

        // while the animation is playing keep the last screen's config so it doesn't revert
        if (hooked != null) {
            lastHooked = configFor(hooked);
            return lastHooked;
        }
        if (renderer != null && renderer.isClosing() && lastHooked != null) {
            return lastHooked;
        }
        lastHooked = null;

        Minecraft client = Minecraft.getInstance();
        Screen screen = client.screen;
        return screen != null ? configFor(screen) : global;
    }

    public static SpatialGUIConfig configFor(Screen screen) {
        SpatialGUIConfig global = SpatialGUI.config;
        if (screen == null || global == null || profiles.isEmpty()) return global;

        return cache.computeIfAbsent(screen.getClass().getName(), id -> {
            SpatialGUIConfig resolved = profiles.resolve(id, getCategory(screen), global);
            return resolved == null ? global : resolved;
        });
    }

    public static void clearCache() {
        cache.clear();
    }

    public static String reload() {
        Path path = profilesPath();
        try {
            ConfigProfiles loaded = ConfigProfiles.load(path);
            profiles = loaded;
            clearCache();
            lastHooked = null;
            SpatialGUI.LOGGER.info("Loaded {}", loaded.summary());
            return "Spatial GUI: Loaded " + loaded.summary();
        } catch (IOException | RuntimeException e) {
            SpatialGUI.LOGGER.error("Couldn't read {}, keeping previous profiles: {}", path, e.getMessage());
            return "Spatial GUI: couldn't read profile file, kept previous (see log)";
        }
    }

    private static String getCategory(Screen screen) {
        if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) return "#inventory";
        if (screen instanceof AbstractContainerScreen<?>) return "#containers";
        if (screen instanceof PauseScreen) return "#pause";
        return "";
    }
}