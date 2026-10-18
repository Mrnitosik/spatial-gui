package org.tastytrash.spatialGUI.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.config.SpatialGUIConfig;
import org.tastytrash.spatialGUI.editor.EditorMode;

//? if fabric {
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//?} else if neoforge {
/*import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
*///?} else if forge {
/*import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;
*///?}
//? if fabric && >=26.1 {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
 //?} else if fabric {
/*import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
*///?}

public class SpatialGUIKeybinds {
    private static KeyMapping openConfig;
    private static KeyMapping reloadProfiles;
    private static KeyMapping openEditor;
    private static boolean registered = false;
    private static final String OPEN_CONFIG_KEY = "key.spatial-gui.open_config";
    private static final String RELOAD_PROFILES_KEY = "key.spatial-gui.reload_profiles";
    private static final String OPEN_EDITOR_KEY = "key.spatial-gui.open_editor";

    //? if fabric && >1.21.1 {
    public static final KeyMapping.Category SPATIAL_GUI_CATEGORY =
            KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("spatial-gui", "main_category"));
    //?} else if neoforge && >1.21.1 {
    /*public static final KeyMapping.Category SPATIAL_GUI_CATEGORY =
            new KeyMapping.Category(net.minecraft.resources.Identifier.fromNamespaceAndPath("spatial-gui", "main_category"));
    *///?}

    private static KeyMapping createKey(String key) {
        //? if >1.21.1 {
        return new KeyMapping(
                key,
                InputConstants.UNKNOWN.getType(),
                InputConstants.UNKNOWN.getValue(),
                SPATIAL_GUI_CATEGORY
        );
        //?} else {
        /*return new KeyMapping(
                key,
                InputConstants.UNKNOWN.getType(),
                InputConstants.UNKNOWN.getValue(),
                "key.category.spatial-gui.main_category"
        );
        *///?}
    }

    private static void tick(Minecraft mc) {
        while (reloadProfiles.consumeClick()) {
            String message = ConfigManager.reload();
            if (mc.player != null) {
                //? if 1.21.11 {
                /*mc.player.displayClientMessage(Component.literal(message), true);
                *///?} else {
                mc.player.sendSystemMessage(Component.literal(message));
                //?}
            }
        }
        while (openConfig.consumeClick()) {
            //? if >=26.2 {
            /*Screen screen = SpatialGUIConfig.HANDLER.generateGui().generateScreen(mc.gui.screen());
            *///?} else {
            Screen screen = SpatialGUIConfig.HANDLER.generateGui().generateScreen(mc.screen);
            //?}
            //? if >=26.1 {
            mc.setScreenAndShow(screen);
            //?} else {
            /*mc.setScreen(screen);
             *///?}
        }
        while (openEditor.consumeClick()) {
            EditorMode.toggle(mc);
        }
    }

    //? if fabric && >=26.1.2 {
    public static void register() {
        openConfig = KeyMappingHelper.registerKeyMapping(createKey(OPEN_CONFIG_KEY));
        reloadProfiles = KeyMappingHelper.registerKeyMapping(createKey(RELOAD_PROFILES_KEY));
        openEditor = KeyMappingHelper.registerKeyMapping(createKey(OPEN_EDITOR_KEY));
        ClientTickEvents.END_CLIENT_TICK.register(SpatialGUIKeybinds::tick);
    }
    //?} else if fabric {
    /*public static void register() {
        openConfig = KeyBindingHelper.registerKeyBinding(createKey(OPEN_CONFIG_KEY));
        reloadProfiles = KeyBindingHelper.registerKeyBinding(createKey(RELOAD_PROFILES_KEY));
        openEditor = KeyBindingHelper.registerKeyBinding(createKey(OPEN_EDITOR_KEY));
        ClientTickEvents.END_CLIENT_TICK.register(SpatialGUIKeybinds::tick);
    }
    *///?} else if neoforge {
    /*public static void register(RegisterKeyMappingsEvent event) {
        if (registered) return;
        registered = true;
        //? if >1.21.1 {
        event.registerCategory(SPATIAL_GUI_CATEGORY);
        //?}
        openConfig = createKey(OPEN_CONFIG_KEY);
        reloadProfiles = createKey(RELOAD_PROFILES_KEY);
        openEditor = createKey(OPEN_EDITOR_KEY);
        event.register(openConfig);
        event.register(reloadProfiles);
        event.register(openEditor);
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> tick(Minecraft.getInstance()));
    }
    *///?} else if forge {
    /*public static void register(RegisterKeyMappingsEvent event) {
        if (registered) return;
        registered = true;
        openConfig = createKey(OPEN_CONFIG_KEY);
        reloadProfiles = createKey(RELOAD_PROFILES_KEY);
        openEditor = createKey(OPEN_EDITOR_KEY);
        event.register(openConfig);
        event.register(reloadProfiles);
        event.register(openEditor);
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent e) -> {
            if (e.phase == TickEvent.Phase.END) tick(Minecraft.getInstance());
        });
    }
    *///?}
}