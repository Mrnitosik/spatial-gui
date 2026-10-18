package org.tastytrash.spatialGUI.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;

public class EditorMode {
    public static void toggle(Minecraft client) {
        if (client.level == null) return;
        Screen screen = client.screen;
        SpatialGUIClient.showScreen(client, screen instanceof EditorScreen ? null : new EditorScreen());
    }
}
