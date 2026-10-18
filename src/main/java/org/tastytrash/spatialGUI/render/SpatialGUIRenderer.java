package org.tastytrash.spatialGUI.render;

import org.tastytrash.spatialGUI.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.compat.ControlifyCompat;
import org.tastytrash.spatialGUI.util.MouseHandlerUtil;
import org.tastytrash.spatialGUI.util.CameraUtil;
import org.tastytrash.spatialGUI.mixin.gui.MouseHandlerAccessor;
import org.tastytrash.spatialGUI.util.RenderUtil;

//? if fabric {
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
//? } else if neoforge {
/*import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
*///? } else if forge {
/*import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
*///?}
//? if >26.2 {
/*import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
 *///?} else if >1.21.1 {
import com.mojang.blaze3d.buffers.GpuBufferSlice;
//?}

public class SpatialGUIRenderer {

    private static long frameCounter;

    public static long frameCounter() {
        return frameCounter;
    }
    public static boolean isExtractingScreen = false;
    public static boolean skipWindowOverride = false;
    public static Object cursorTarget = null;

    private final TextureTargetManager targetManager;
    private final ScreenExtractor screenExtractor;
    private final InventoryRenderer inventoryRenderer;

    private Screen hookedScreen;
    private static boolean isInventoryScreen;
    private Vec3 cameraStartPos;
    private float cameraStartYRot;
    private static boolean wasTrue;
    public boolean poseStarted = false;
    private long removedFrame = -1;
    private boolean prevPoseStarted;
    private float fovProgress = 0f;
    private long lastFovUpdateNano = 0L;

    private float[] preSnapState;

    private boolean rotationAnchored;
    private float anchoredYaw;
    private float anchoredPitch;

    public SpatialGUIRenderer() {
        this.targetManager = new TextureTargetManager();
        this.screenExtractor = new ScreenExtractor();
        this.inventoryRenderer = new InventoryRenderer(targetManager);

        //? if neoforge {
        /*NeoForge.EVENT_BUS.addListener(this::onScreenRenderPre);
        NeoForge.EVENT_BUS.addListener(this::onScreenClosing);
        *///? } else if forge {
        /*MinecraftForge.EVENT_BUS.addListener(this::onScreenRenderPre);
        MinecraftForge.EVENT_BUS.addListener(this::onScreenClosing);
        *///?}
    }

    public void hookScreen(Screen screen) {
        if (hookedScreen == screen) return;
        if (!SpatialGUIClient.isEnabled()) return;
        //? if >=26.2 {
        /*if (screen != Minecraft.getInstance().gui.screen()) return;
         *///?} else {
        if (screen != Minecraft.getInstance().screen) return;
        //?}

        ConfigManager.clearCache();

        boolean swapping = hookedScreen != null || removedFrame == frameCounter;
        boolean keepAnchor = swapping && rotationAnchored;

        if (hookedScreen != null) onScreenRemoved(false);

        if (!SpatialGUIClient.isEnabled()) {
            return;
        }

        var client = Minecraft.getInstance();
        //? if >=26.2 {
        /*if (screen != client.screen) return;
         *///?} else {
        if (screen != client.screen) return;
        //?}

        if (inventoryRenderer.isClosing()) {
            finishClosing();
        }
        var snapPlayer = client.player;
        if (swapping && preSnapState != null && snapPlayer != null) {
            restoreRotation(snapPlayer, preSnapState);
        }
        preSnapState = null;

        skipWindowOverride = false;
        hookedScreen = screen;
        isInventoryScreen = screen instanceof InventoryScreen || screen.getClass().getName().contains("InventoryScreen");
        if (swapping) {
            poseStarted = prevPoseStarted;
        } else {
            inventoryRenderer.setScreenOpenTime(System.currentTimeMillis());
        }

        var player = client.player;
        if (player != null) {
            if (!keepAnchor) {
                anchoredYaw = player.getYRot();
                anchoredPitch = player.getXRot();
            }
            rotationAnchored = true;
        }
        if (player != null && Minecraft.getInstance().level != null) {
            CameraUtil.checkBlockCollision(player);
        }

        boolean isFirstPerson = SpatialGUIClient.shouldUseFirstPersonMode(hookedScreen);
        SpatialGUIClient.setEffectiveFirstPersonMode(isFirstPerson);
        MouseHandlerUtil.resetMouseState();
        if (!isFirstPerson) {
            targetManager.prepareTarget();
        }

        //? if fabric && >=26.1.2 {
        ScreenEvents.afterExtract(screen).register((screenArg, extractor, mouseX, mouseY, tickDelta) -> prepareTarget());
        //?} else if fabric {
        /*ScreenEvents.afterRender(screen).register((screenArg, graphics, mouseX, mouseY, tickDelta) -> prepareTarget());
         *///?}

        //? if fabric {
        ScreenEvents.remove(screen).register(removedScreen -> {
            if (hookedScreen == removedScreen) {
                onScreenRemoved();
            }
        });
        //?}
    }

    //? if neoforge || forge {
    /*private void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        if (event.getScreen() == hookedScreen) {
            event.setCanceled(true);
            SpatialGUIRenderer.skipWindowOverride = false;
            screenExtractor.extractIsolatedScreen(hookedScreen, event.getPartialTick(), inventoryRenderer.getQuadBasis(), inventoryRenderer.getCylinderBasis(), targetManager);
            SpatialGUIRenderer.skipWindowOverride = true;
        }
    }

    private void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() == hookedScreen) {
            onScreenRemoved();
        }
    }
    *///? }

    private void onScreenRemoved() {
        onScreenRemoved(true);
    }

    private void onScreenRemoved(boolean animate) {
        if (hookedScreen == null) return;
        skipWindowOverride = false;
        var player = Minecraft.getInstance().player;
        boolean wasEffectiveFirstPerson = SpatialGUIClient.getEffectiveFirstPersonMode();
        MouseHandlerUtil.releaseMouseFromFirstPerson();
        boolean startClosing = animate
                && ConfigManager.config().enableCloseAnimation
                && Minecraft.getInstance().level != null
                && inventoryRenderer.beginClosing();
        if (!startClosing) {
            inventoryRenderer.resetRecipeBookState();
            inventoryRenderer.resetPerspectiveState();
            releaseRotationAnchor();
        }
        hookedScreen = null;
        isInventoryScreen = false;
        removedFrame = frameCounter;
        prevPoseStarted = poseStarted;
        poseStarted = false;
        SpatialGUIClient.setSwitchedToFirstPersonDueToBlock(false);

        var mc = Minecraft.getInstance();
        preSnapState = null;
        if (wasEffectiveFirstPerson && ConfigManager.config().keepFirstPersonCameraAngle && player != null) {
            //? if >=26.2 {
            /*float cameraYaw = mc.gameRenderer.getMainCamera().yRot();
            float cameraPitch = mc.gameRenderer.getMainCamera().xRot();
            *///?} else if >=1.21.11 {
                    float cameraYaw = mc.gameRenderer.getMainCamera().yRot();
                    float cameraPitch = mc.gameRenderer.getMainCamera().xRot();
                    //?} else {
            /*float cameraYaw = mc.gameRenderer.getMainCamera().getYRot();
            float cameraPitch = mc.gameRenderer.getMainCamera().getXRot();
            *///?}
            preSnapState = captureRotation(player);
            applyCameraSnap(player, cameraYaw, cameraPitch);
        }
    }

    private static float[] captureRotation(LocalPlayer player) {
        return new float[]{player.getYRot(), player.getXRot(), player.yRotO, player.xRotO, player.yBob, player.xBob, player.yBobO, player.xBobO};
    }

    private static void restoreRotation(LocalPlayer player, float[] s) {
        player.setYRot(s[0]);
        player.setXRot(s[1]);
        player.yRotO = s[2];
        player.xRotO = s[3];
        player.yBob = s[4];
        player.xBob = s[5];
        player.yBobO = s[6];
        player.xBobO = s[7];
    }

    private static void applyCameraSnap(net.minecraft.client.player.LocalPlayer player, float cameraYaw, float cameraPitch) {
        player.setYRot(cameraYaw);
        player.setXRot(cameraPitch);
        player.yRotO = cameraYaw;
        player.xRotO = cameraPitch;
        player.yBob = cameraYaw;
        player.xBob = cameraPitch;
        player.yBobO = cameraYaw;
        player.xBobO = cameraPitch;
    }

    private void finishClosing() {
        if (!inventoryRenderer.isClosing()) return;
        inventoryRenderer.endClosing();
        inventoryRenderer.resetRecipeBookState();
        inventoryRenderer.resetPerspectiveState();
        targetManager.clearTarget();
        releaseRotationAnchor();
    }

    public boolean isClosing() {
        return inventoryRenderer.isClosing();
    }

    public boolean isClosingFirstPerson() {
        return inventoryRenderer.isClosing() && inventoryRenderer.isClosingFirstPerson();
    }

    public boolean isClosingThirdToFirstPerson() {
        return inventoryRenderer.isClosing()
                && !inventoryRenderer.isClosingFirstPerson()
                && !ConfigManager.config().autoDetectCameraMode
                && !SpatialGUIClient.getWasThirdPersonCamera();
    }

    public long getCloseElapsedMs() {
        return inventoryRenderer.getCloseElapsedMs();
    }

    public float getCloseBlend() {
        float duration = Math.max(1, ConfigManager.config().transitionDurationMs);
        float skipFraction = ConfigManager.config().transitionSkipPercentage / 100.0f;
        float openProgress = Math.max(skipFraction, 1.0f - inventoryRenderer.getCloseElapsedMs() / duration);
        return 1.0f - org.tastytrash.spatialGUI.util.AnimationUtil.easeInOutSine(openProgress);
    }

    public void clearTarget() {
        targetManager.clearTarget();
    }

    public void prepareTarget() {
        targetManager.prepareTarget();
    }

    public void updateMousePosition() {
        if (!shouldCapture()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        MouseHandlerAccessor mouseHandler = (MouseHandlerAccessor) client.mouseHandler;
        boolean isFirstPerson = SpatialGUIClient.getEffectiveFirstPersonMode();

        Vector2d mappedMouse = updateMousePosition(MouseHandlerUtil.getSourceX(), MouseHandlerUtil.getSourceY());

        if (mappedMouse == null) {
            return;
        }

        if (!isFirstPerson) {
            mouseHandler.setRawXpos(mappedMouse.x);
            mouseHandler.setRawYpos(mappedMouse.y);
        }
    }

    public Vector2d updateMousePosition(double sourceX, double sourceY) {
        Minecraft client = Minecraft.getInstance();
        RenderUtil.QuadBasis quadBasis = inventoryRenderer.getQuadBasis();
        if (quadBasis == null) {
            return null;
        }

        if (ControlifyCompat.isVirtualMouseActive() && !isCrosshairModeActive()) {
            MouseHandlerUtil.setFlatPosition(sourceX, sourceY);
            return new Vector2d(sourceX, sourceY);
        }

        double guiScale = ConfigManager.config().getEffectiveGuiScale();
        Vector2d mappedMouse = MouseHandlerUtil.mapMousePosition(
                sourceX, sourceY, quadBasis, inventoryRenderer.getCylinderBasis(), guiScale,
                targetManager.getInventoryTarget()
        );

        if (mappedMouse == null) {
            return null;
        }

        double mappedX = mappedMouse.x / guiScale;
        double mappedY = mappedMouse.y / guiScale;
        mappedX *= (double) client.getWindow().getScreenWidth() / client.getWindow().getGuiScaledWidth();
        mappedY *= (double) client.getWindow().getScreenHeight() / client.getWindow().getGuiScaledHeight();

        return new Vector2d(mappedX, mappedY);
    }

    public boolean shouldCapture() {
        Minecraft client = Minecraft.getInstance();
        //? if >=26.2 {
        /*boolean bool = SpatialGUIClient.shouldHookScreen(hookedScreen) && hookedScreen == client.screen;
         *///?} else {
        boolean bool = SpatialGUIClient.shouldHookScreen(hookedScreen) && hookedScreen == client.screen;
        //?}
        if (!bool) {
            wasTrue = false;
            cameraStartPos = null;
            return false;
        }

        if (!wasTrue) {
            var mc = Minecraft.getInstance();
            //? if >=26.2 {
            /*cameraStartPos = mc.gameRenderer.getMainCamera().position();
            cameraStartYRot = mc.gameRenderer.getMainCamera().yRot();
            *///?} else if >=1.21.11 {
            cameraStartPos = mc.gameRenderer.getMainCamera().position();
            cameraStartYRot = mc.gameRenderer.getMainCamera().yRot();
            //?} else {
            /*cameraStartPos = mc.gameRenderer.getMainCamera().getPosition();
            cameraStartYRot = mc.gameRenderer.getMainCamera().getYRot();
            *///?}
            wasTrue = true;
        }

        return true;
    }

    public Vec3 getCameraStartPos() {
        return cameraStartPos;
    }

    public float getCameraStartYRot() {
        return cameraStartYRot;
    }

    public float getFovOverrideFactor() {
        return fovProgress;
    }

    public static boolean isInventoryScreen() {
        return isInventoryScreen;
    }

    public Screen getHookedScreen() {
        return hookedScreen;
    }

    public TextureTargetManager getTargetManager() {
        return targetManager;
    }

    public InventoryRenderer getInventoryRenderer() {
        return inventoryRenderer;
    }

    public static boolean isCrosshairModeActive() {
        return ConfigManager.config() != null
                && ConfigManager.config().useCrosshairForFirstPerson
                && SpatialGUIClient.getEffectiveFirstPersonMode()
                && SpatialGUIClient.renderer() != null
                && SpatialGUIClient.renderer().getHookedScreen() != null;
    }

    public void renderInWorld(com.mojang.blaze3d.vertex.PoseStack matrices) {
        inventoryRenderer.renderInWorld(matrices);
    }

    //? if >1.21.1 {
    public void capturePerspectiveState(GpuBufferSlice buffer, com.mojang.blaze3d.ProjectionType type, com.mojang.blaze3d.vertex.PoseStack poseStack) {
        inventoryRenderer.capturePerspectiveState(buffer, type, poseStack);
    }
    //?} else {
    /*public void capturePerspectiveState(org.joml.Matrix4f projectionMatrix, com.mojang.blaze3d.vertex.VertexSorting vertexSorting, com.mojang.blaze3d.vertex.PoseStack poseStack) {
        inventoryRenderer.capturePerspectiveState(projectionMatrix, vertexSorting, poseStack);
    }
    *///?}

    public void renderInWorldPost() {
        inventoryRenderer.renderInWorldPost();
    }

    public void onFrameStart() {
        inventoryRenderer.onFrameStart();
    }

    public void updateFrame() {
        frameCounter++;
        var mc = Minecraft.getInstance();
        //? if >=26.2 {
        /*Screen screen = mc.gui.screen();
         *///?} else {
        Screen screen = mc.screen;
        //?}
        if (hookedScreen != null && screen != hookedScreen) onScreenRemoved();
        if (inventoryRenderer.isClosing() && (mc.level == null || inventoryRenderer.isCloseFinished())) {
            finishClosing();
        }
        MouseHandlerUtil.syncCursorGrab();
        ControlifyCompat.handleFreeLook(isCrosshairModeActive());
        updateFovProgress();
    }

    public void updateFovProgress() {
        var mc = Minecraft.getInstance();
        if (mc.level == null) {
            fovProgress = 0f;
            lastFovUpdateNano = 0L;
            return;
        }

        //? if >=26.2 {
        /*Screen current = mc.gui.screen();
         *///?} else {
        Screen current = mc.screen;
        //?}

        boolean active = current != null && current == hookedScreen && SpatialGUIClient.shouldHookScreen(current);
        float target = (active && ConfigManager.config().overrideFov) ? 1f : 0f;
        float speed = ConfigManager.config().fovLerpSpeed;

        if (speed <= 0f) {
            fovProgress = target;
            lastFovUpdateNano = 0L;
            return;
        }

        long now = System.nanoTime();
        float dt = lastFovUpdateNano == 0L ? 0f : Math.min((now - lastFovUpdateNano) / 1000000000f, 0.1f);
        lastFovUpdateNano = now;

        float alpha = 1f - (float) Math.exp(-dt * speed);
        fovProgress += (target - fovProgress) * alpha;

        if (Math.abs(target - fovProgress) < 0.001f) fovProgress = target;
    }

    //? if >1.21.1 {
    public net.minecraft.client.gui.render.GuiRenderer getScreenGuiRenderer() {
        return screenExtractor.getScreenGuiRenderer();
    }

    public GpuBufferSlice getCapturedProjectionBuffer() {
        return inventoryRenderer.getCapturedProjectionBuffer();
    }

    public com.mojang.blaze3d.ProjectionType getCapturedProjectionType() {
        return inventoryRenderer.getCapturedProjectionType();
    }
    //?}

    //? if >=26.1.2 {
    public net.minecraft.client.gui.GuiGraphicsExtractor createIsolatedGraphics() {
        return screenExtractor.createIsolatedGraphics();
    }
    //?} else if >1.21.1 {
    /*public net.minecraft.client.gui.GuiGraphics createIsolatedGraphics() {
        return screenExtractor.createIsolatedGraphics();
    }
    *///?}

    public void extractIsolatedScreen(Screen screen, float partialTick) {
        screenExtractor.extractIsolatedScreen(screen, partialTick, inventoryRenderer.getQuadBasis(), inventoryRenderer.getCylinderBasis(), targetManager);
    }

    public void extractIsolatedScreen(Screen screen, float partialTick, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> operation) {
        screenExtractor.extractIsolatedScreen(screen, partialTick, inventoryRenderer.getQuadBasis(), inventoryRenderer.getCylinderBasis(), targetManager, operation);
    }

    public float anchoredYaw() {
        if (rotationAnchored) return anchoredYaw;
        var player = Minecraft.getInstance().player;
        return player != null ? player.getYRot() : 0f;
    }

    public float anchoredPitch() {
        if (rotationAnchored) return anchoredPitch;
        var player = Minecraft.getInstance().player;
        return player != null ? player.getXRot() : 0f;
    }

    private void releaseRotationAnchor() {
        rotationAnchored = false;
    }
}