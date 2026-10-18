package org.tastytrash.spatialGUI;

import org.tastytrash.spatialGUI.config.SpatialGUIConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tastytrash.spatialGUI.config.ConfigManager;

//? if neoforge {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.bus.api.IEventBus;
*///?}

//? if neoforge {
/*@Mod(value = SpatialGUI.MOD_ID, dist = Dist.CLIENT)
 *///?}
public class SpatialGUI {
    //? if fabric {
    public static final String MOD_ID = "spatial-gui";
    //? } else {
    /*public static final String MOD_ID = "spatial_gui";
     *///? }
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static SpatialGUIConfig config;

    //? if neoforge {
    /*public SpatialGUI(ModContainer container, IEventBus modBus) {
        initCommon();
        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                (modContainer, parentScreen) -> SpatialGUIConfig.HANDLER.generateGui().generateScreen(parentScreen)
        );
        new org.tastytrash.spatialGUI.client.SpatialGUIClient(modBus);
    }
    *///?}

    public static void initCommon() {
        LOGGER.info("Initializing Spatial GUI");
        SpatialGUIConfig.HANDLER.load();
        config = SpatialGUIConfig.get();
        ConfigManager.reload();
    }
}