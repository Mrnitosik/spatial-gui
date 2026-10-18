package org.tastytrash.spatialGUI.editor;

//? if >1.21.11 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EditorScreen extends Screen {
    protected EditorScreen() {
        super(Component.literal("Spatial GUI Editor"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    //? if >1.21.11 {
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
    //?} else {
    /*public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
    *///?}
        int gridSize = 18;
        int gray = 0x4A4A4A4A;
        int light_blue = 0xFF33AAFF;
        int red = 0xFFFF2B2B;

        int invWidth = 180;
        int invHeight = 162;
        int invX = (width - invWidth) / 2;
        int invY = (height - invHeight) / 2;

        int startX = invX % gridSize;
        int startY = invY % gridSize;

        // grid
        for (int x = startX; x <= width; x += gridSize) {
            g.verticalLine(x, 0, height, gray);
        }
        for (int y = startY; y <= height; y += gridSize) {
            g.horizontalLine(0, width, y, gray);
        }

        // inventory border
        for (int i = 0; i < 2; i++) {
            g.outline(invX + i, invY + i, invWidth - i * 2, invHeight - i * 2, light_blue);
        }

        // outer border
        for (int i = 0; i < 4; i++) {
            g.outline(i, i, width - i * 2, height - i * 2, red);
        }
    }
}
