package org.tastytrash.spatialGUI.editor;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;

public final class EditorMesh {
    private static final int[][] FACES = {{0, 1, 3, 2}, {4, 6, 7, 5}, {0, 2, 6, 4}, {1, 5, 7, 3}, {0, 4, 5, 1}, {2, 3, 7, 6}};
    private static final float CUBE_HALF = 0.015f;
    public static final int HANDLE_COLOR = 0xA0FFFFFF;
    public static final int QUADS = 24;

    public static boolean active() {
        var renderer = SpatialGUIClient.renderer();
        return renderer != null && renderer.getHookedScreen() instanceof EditorScreen;
    }

    public static void addHandles(VertexConsumer buffer, Matrix4f pose, float aspect) {
        for (int sx = -1; sx <= 1; sx++) {
            for (int sy = -1; sy <= 1; sy++) {
                if (sx == 0 || sy == 0) continue;
                addCube(buffer, new Matrix4f(pose).translate(sx * aspect * 0.5f, sy * 0.5f, 0), HANDLE_COLOR);
            }
        }
    }

    private static void addCube(VertexConsumer buffer, Matrix4f pose, int argb) {
        for (int[] face : FACES) {
            for (int i : face) {
                float x = (i & 1) == 0 ? -CUBE_HALF : CUBE_HALF;
                float y = (i & 2) == 0 ? -CUBE_HALF : CUBE_HALF;
                float z = (i & 4) == 0 ? -CUBE_HALF : CUBE_HALF;
                //? if >1.20.1 {
                buffer.addVertex(pose, x, y, z).setColor(argb);
                //?} else {
                /*buffer.vertex(pose, x, y, z).color(argb >> 16 & 255, argb >> 8 & 255, argb & 255, argb >>> 24).endVertex();
                 *///?}
            }
        }
    }
}