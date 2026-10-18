package org.tastytrash.spatialGUI.mixin.gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

//? if >=1.21.11 {
import com.mojang.blaze3d.platform.cursor.CursorType;
//? if >=26.1.2 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
 *///?}


//? if >=26.1.2 {
@Mixin(GuiGraphicsExtractor.class)
//?} else {
/*@Mixin(GuiGraphics.class)
*///?}
public class GuiGraphicsCursorMixin {
    @Inject(method = "requestCursor", at = @At("HEAD"))
    private void spatialGUI$relayCursor(CursorType cursorType, CallbackInfo ci) {
        if (SpatialGUIRenderer.cursorTarget instanceof
                //? if >=26.1.2 {
                GuiGraphicsExtractor
                        //?} else {
                        /*GuiGraphics
                         *///?}
                        target && target != (Object) this) {
            target.requestCursor(cursorType);
        }
    }
}
//?} else {
/*@Mixin(net.minecraft.client.gui.Gui.class)
public class GuiGraphicsCursorMixin {}
*///?}