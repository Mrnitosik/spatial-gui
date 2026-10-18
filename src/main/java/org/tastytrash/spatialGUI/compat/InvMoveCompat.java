
package org.tastytrash.spatialGUI.compat;

//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
//?} else if neoforge {
/*import net.neoforged.fml.ModList;
 *///?} else {
/*import net.minecraftforge.fml.ModList;
 *///?}

import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

public final class InvMoveCompat {
    private static Boolean isInvMoveLoaded = null;

    public static boolean isInvMoveLoaded() {
        if (isInvMoveLoaded != null) {
            return isInvMoveLoaded;
        }

        //? if fabric {
        isInvMoveLoaded = FabricLoader.getInstance().isModLoaded("invmove");
        //?} else if neoforge {
        /*isInvMoveLoaded = ModList.get().isLoaded("invmove");
         *///?} else {
        /*isInvMoveLoaded = ModList.get().isLoaded("invmove");
         *///?}

        return isInvMoveLoaded;
    }

    public static boolean playerRotates() {
        return isInvMoveLoaded() && SpatialGUIRenderer.isCrosshairModeActive();
    }
}
