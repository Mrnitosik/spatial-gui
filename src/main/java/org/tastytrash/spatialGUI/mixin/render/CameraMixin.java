package org.tastytrash.spatialGUI.mixin.render;

import org.tastytrash.spatialGUI.config.ConfigManager;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.compat.InvMoveCompat;
import org.tastytrash.spatialGUI.mixin.gui.MouseHandlerAccessor;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.util.AnimationUtil;
import org.tastytrash.spatialGUI.util.MathUtil;
import org.tastytrash.spatialGUI.util.MouseHandlerUtil;
import org.tastytrash.spatialGUI.util.CameraUtil;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow private Entity entity;
    @Shadow private Vec3 position;
    @Shadow private float xRot;
    @Shadow private float yRot;
    @Final @Shadow private Quaternionf rotation;

    @Unique private static Vec3 startPos;
    @Unique private static float startYRot;
    @Unique private static float startXRot;
    @Unique private static long transitionStartTime, TRANSITION_DURATION_MS = 1;
    @Unique private static boolean wasCapturing, isTransitioning;
    @Unique private static final float MAX_YAW_OFFSET = 90f;
    @Unique private static float smoothedCameraYaw, smoothedCameraPitch;
    @Unique private static float baseXRot;

    @Unique private static float parallaxYawOffset, parallaxPitchOffset;
    @Unique private static long lastParallaxNanos;
    @Unique private static final float PARALLAX_TAU_MS = 50.0f;

    @Unique private static float freeLookYaw = 0f, freeLookPitch = 0f;
    @Unique private static boolean wasCrosshairMode;

    @Unique private static Vec3 lastOffset;
    @Unique private static float lastYRot, lastXRot;
    @Unique private static boolean closingCameraActive;

    @Unique
    private record CameraTransform(Vec3 pos, float yaw, float pitch) {}

    //? if >=26.1.2 {
    @ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
    private float spatialGUI$overrideFov(float original) {
        var renderer = SpatialGUIClient.renderer();
        if (renderer == null) return original;
        float factor = renderer.getFovOverrideFactor();
        if (factor <= 0f) return original;
        return MathUtil.lerp(original, ConfigManager.config().targetFov, factor);
    }
    //?}

    //? if >=26.1.2 {
    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void spatialGUI$modifyCamera(float partialTicks, CallbackInfo ci) {
        //?} else if >1.21.1 {
    /*@Inject(method = "setup", at = @At("TAIL"))
    private void spatialGUI$modifyCamera(net.minecraft.world.level.Level level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTicks, CallbackInfo ci) {
    *///?} else {
    /*@Inject(method = "setup", at = @At("TAIL"))
    private void spatialGUI$modifyCamera(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTicks, CallbackInfo ci) {
        *///?}
        var renderer = SpatialGUIClient.renderer();
        boolean isCapturing = renderer.shouldCapture();

        if (isCapturing && this.entity != null && SpatialGUIClient.isEnabled() && Minecraft.getInstance().level != null && this.entity.level() != null) {
            boolean resumedFromClosing = closingCameraActive;
            closingCameraActive = false;
            TRANSITION_DURATION_MS = ConfigManager.config().transitionDurationMs;
            boolean isFirstPerson = SpatialGUIClient.shouldUseFirstPersonMode(renderer.getHookedScreen());
            SpatialGUIClient.setEffectiveFirstPersonMode(isFirstPerson);
            MouseHandlerUtil.updateMouseGrabForFirstPerson(isFirstPerson);

            if (!wasCapturing) {
                baseXRot = Mth.clamp(xRot, -ConfigManager.config().firstPersonPitchClamp, ConfigManager.config().firstPersonPitchClamp);
            }

            boolean crosshairActive = SpatialGUIRenderer.isCrosshairModeActive();
            if (crosshairActive && !wasCrosshairMode) {
                freeLookYaw = 0f;
                freeLookPitch = 0f;
            }
            wasCrosshairMode = crosshairActive;

            if (!isFirstPerson && !SpatialGUIClient.getSwitchedToFirstPersonDueToBlock()) {
                CameraUtil.checkBlockCollision(this.entity);
            }

            CameraTransform transform = this.calculateCameraTransform(isFirstPerson, partialTicks);
            Vec3 newTargetPos = transform.pos();
            float newTargetYRot = transform.yaw();
            float newTargetXRot = transform.pitch();

            if (!wasCapturing || !isTransitioning || resumedFromClosing) {
                this.startTransition(renderer, newTargetYRot, newTargetXRot, isFirstPerson);
            }

            this.applyCameraUpdate(newTargetPos, newTargetYRot, newTargetXRot, isFirstPerson);
            lastOffset = position.subtract(MathUtil.lerpEntityPosition(this.entity, partialTicks));
            lastYRot = yRot;
            lastXRot = xRot;
            this.rotation.rotationYXZ((float) Math.PI - this.yRot * ((float) Math.PI / 180F), -this.xRot * ((float) Math.PI / 180F), 0.0F);
            wasCapturing = true;
        } else if (renderer.isClosing() && !renderer.isClosingFirstPerson() && lastOffset != null && this.entity != null && SpatialGUIClient.isEnabled()) {
            closingCameraActive = true;
            SpatialGUIClient.setWasClosing(true);
            float easedProgress = renderer.getCloseBlend();

            Vec3 screenCameraPos = MathUtil.lerpEntityPosition(this.entity, partialTicks).add(lastOffset);
            boolean toFirstPerson = renderer.isClosingThirdToFirstPerson();
            Vec3 normalPos = toFirstPerson ? MathUtil.lerpEntityPosition(this.entity, partialTicks).add(0.0, this.entity.getEyeHeight(), 0.0) : position;
            float normalYRot = toFirstPerson ? this.entity.getYRot() : yRot;
            float normalXRot = toFirstPerson ? this.entity.getXRot() : xRot;

            position = new Vec3(MathUtil.lerp(screenCameraPos.x, normalPos.x, easedProgress), MathUtil.lerp(screenCameraPos.y, normalPos.y, easedProgress), MathUtil.lerp(screenCameraPos.z, normalPos.z, easedProgress));
            boolean isThirdPersonFront = SpatialGUIClient.getOriginalCameraType() == net.minecraft.client.CameraType.THIRD_PERSON_FRONT;
            yRot = isThirdPersonFront ? normalYRot : MathUtil.rotLerp(lastYRot, normalYRot, easedProgress);
            xRot = (toFirstPerson || (ConfigManager.config().lerpCameraPitch && SpatialGUIClient.getWasThirdPersonCamera())) ? MathUtil.lerp(lastXRot, normalXRot, easedProgress) : normalXRot;
            this.rotation.rotationYXZ((float) Math.PI - this.yRot * ((float) Math.PI / 180F), -this.xRot * ((float) Math.PI / 180F), 0.0F);
            SpatialGUIClient.setCameraPosition(position);
        } else {
            this.resetCameraState();
        }
    }


    @Unique
    private CameraTransform calculateCameraTransform(boolean isFirstPerson, float partialTicks) {
        float distance = CameraUtil.calculateAutoFovDistance((float) ConfigManager.config().cameraDistance, isFirstPerson);
        float sideOffset = CameraUtil.calculateAutoFovSideOffset((float) ConfigManager.config().cameraSideOffset, isFirstPerson);
        float heightOffset = isFirstPerson ? entity.getEyeHeight() : Mth.clamp((float) ConfigManager.config().cameraHeightOffset, -4f, 4f);
        float yaw, pitch;
        float positionYaw;

        float baseYaw = spatialGUI$baseYaw();

        Minecraft client = Minecraft.getInstance();

        if (!isFirstPerson && ConfigManager.config().mirrorThirdPerson) {
            sideOffset = -sideOffset;
        }

        var acc = (MouseHandlerAccessor) client.mouseHandler;
        double rawMouseX = MouseHandlerUtil.getPhysicalX(acc.getRawXpos());
        double rawMouseY = MouseHandlerUtil.getPhysicalY(acc.getRawYpos());

        float normX = Mth.clamp((float) rawMouseX / client.getWindow().getScreenWidth() * 2f - 1f, -1f, 1f);
        float normY = Mth.clamp((float) rawMouseY / client.getWindow().getScreenHeight() * 2f - 1f, -1f, 1f);

        if (isFirstPerson) {
            float maxPitch = 90;
            float entityXRot = spatialGUI$basePitch();

            if (SpatialGUIRenderer.isCrosshairModeActive()) {
                if (InvMoveCompat.playerRotates()) {
                    MouseHandlerUtil.resetFreeLookDelta();
                    freeLookYaw = 0f;
                    freeLookPitch = 0f;
                    smoothedCameraYaw = 0f;
                    smoothedCameraPitch = entityXRot;
                } else {
                    double[] rel = MouseHandlerUtil.resetFreeLookDelta();
                    double deltaX = rel[0];
                    double deltaY = rel[1];

                    double sens = CameraUtil.calculateMouseSensitivity();
                    //? if >1.21.1 {
                    double xOffset = deltaX * sens * (client.options.invertMouseX().get() ? -1 : 1);
                    //?} else {
                    /*double xOffset = deltaX * sens;
                     *///?}
                    double yOffset = deltaY * sens * (client.options.invertMouseY().get() ? -1 : 1);

                    freeLookYaw += (float) xOffset;
                    if (ConfigManager.config().lockFirstPersonYaw) {
                        freeLookYaw = Mth.clamp(freeLookYaw, -MAX_YAW_OFFSET, MAX_YAW_OFFSET);
                    }
                    freeLookPitch = Mth.clamp(freeLookPitch + (float) yOffset, -maxPitch - entityXRot, maxPitch - entityXRot);

                    smoothedCameraYaw = freeLookYaw;
                    smoothedCameraPitch = entityXRot + freeLookPitch;
                }
            } else if (ConfigManager.config().disableFirstPersonParallax) {
                this.smoothParallaxOffsets(0f, 0f);
                smoothedCameraYaw = parallaxYawOffset;
                smoothedCameraPitch = baseXRot + parallaxPitchOffset;
            } else {
                this.smoothParallaxOffsets(
                        normX * MAX_YAW_OFFSET * (float) ConfigManager.config().firstPersonMouseSensitivityYaw,
                        normY * 180f * (float) ConfigManager.config().firstPersonMouseSensitivityPitch);
                smoothedCameraYaw = parallaxYawOffset;
                smoothedCameraPitch = baseXRot + parallaxPitchOffset;
            }

            smoothedCameraPitch = Mth.clamp(smoothedCameraPitch, -maxPitch, maxPitch);
            yaw = baseYaw + smoothedCameraYaw;
            pitch = smoothedCameraPitch;
            positionYaw = yaw;
        } else {
            if (ConfigManager.config().disableThirdPersonParallax) {
                this.smoothParallaxOffsets(0f, 0f);
            } else {
                this.smoothParallaxOffsets(
                        normX * MAX_YAW_OFFSET * (float) ConfigManager.config().thirdPersonMouseSensitivityYaw,
                        normY * 180f * (float) ConfigManager.config().thirdPersonMouseSensitivityPitch);
            }
            smoothedCameraYaw = parallaxYawOffset;
            smoothedCameraPitch = parallaxPitchOffset;

            yaw = baseYaw + smoothedCameraYaw;
            pitch = (float) ConfigManager.config().cameraTargetPitch + smoothedCameraPitch;
            positionYaw = baseYaw;
        }

        float yawRadians = (float) Math.toRadians(positionYaw);
        Vec3 entityPos = MathUtil.lerpEntityPosition(entity, partialTicks);

        double camX = entityPos.x + Math.sin(yawRadians) * distance + Math.cos(yawRadians) * sideOffset;
        double camY = entityPos.y + heightOffset;
        double camZ = entityPos.z - Math.cos(yawRadians) * distance + Math.sin(yawRadians) * sideOffset;

        Vec3 targetCamPos = new Vec3(camX, camY, camZ);
        if (!isFirstPerson) {
            Vec3 playerEyePos = new Vec3(entityPos.x, entityPos.y + entity.getEyeHeight(), entityPos.z);
            targetCamPos = CameraUtil.adjustCameraPositionForCollision(entity, playerEyePos, targetCamPos);
        }

        return new CameraTransform(targetCamPos, yaw, pitch);
    }

    @Unique
    private void smoothParallaxOffsets(float targetYaw, float targetPitch) {
        long now = System.nanoTime();
        float dt = lastParallaxNanos == 0 ? 0.016f : Math.min((now - lastParallaxNanos) / 1_000_000_000f, 0.1f);
        lastParallaxNanos = now;
        float alpha = 1f - (float) Math.exp(-dt / (PARALLAX_TAU_MS / 1000f));
        parallaxYawOffset += (targetYaw - parallaxYawOffset) * alpha;
        parallaxPitchOffset += (targetPitch - parallaxPitchOffset) * alpha;
    }

    @Unique
    private float spatialGUI$baseYaw() {
        if (InvMoveCompat.playerRotates()) return entity.getYRot();
        return SpatialGUIClient.renderer().anchoredYaw();
    }

    @Unique
    private float spatialGUI$basePitch() {
        if (InvMoveCompat.playerRotates()) return entity.getXRot();
        return SpatialGUIClient.renderer().anchoredPitch();
    }

    @Unique
    private void startTransition(SpatialGUIRenderer renderer, float newTargetYRot, float newTargetXRot, boolean isFirstPerson) {
        Vec3 savedStartPos = renderer.getCameraStartPos();
        if (savedStartPos != null) {
            startPos = savedStartPos;
            startYRot = renderer.getCameraStartYRot();
        } else {
            startPos = new Vec3(position.x, position.y, position.z);
            startYRot = yRot;
        }
        startXRot = xRot;
        transitionStartTime = System.currentTimeMillis();
        isTransitioning = true;

        if (isFirstPerson) {
            smoothedCameraYaw = newTargetYRot - spatialGUI$baseYaw();
            smoothedCameraPitch = newTargetXRot;
        } else {
            smoothedCameraYaw = 0f;
            smoothedCameraPitch = 0f;
        }
    }

    @Unique
    private void applyCameraUpdate(Vec3 newTargetPos, float newTargetYRot, float newTargetXRot, boolean isFirstPerson) {
        if (isFirstPerson) {
            position = newTargetPos;
            yRot = newTargetYRot;
            xRot = newTargetXRot;
        } else {
            long elapsed = System.currentTimeMillis() - transitionStartTime;
            float skipFraction = ConfigManager.config().transitionSkipPercentage / 100.0f;
            float adjustedElapsed = elapsed + (skipFraction * TRANSITION_DURATION_MS);
            float progress = Math.min(adjustedElapsed / TRANSITION_DURATION_MS, 1.0f);
            float easedProgress = AnimationUtil.easeInOutSine(progress);

            position = new Vec3(MathUtil.lerp(startPos.x, newTargetPos.x, easedProgress), MathUtil.lerp(startPos.y, newTargetPos.y, easedProgress), MathUtil.lerp(startPos.z, newTargetPos.z, easedProgress));
            boolean isThirdPersonFront = SpatialGUIClient.getOriginalCameraType() == net.minecraft.client.CameraType.THIRD_PERSON_FRONT;
            yRot = (progress >= 1.0f || isThirdPersonFront) ? newTargetYRot : MathUtil.rotLerp(startYRot, newTargetYRot, easedProgress);
            xRot = (ConfigManager.config().lerpCameraPitch && SpatialGUIClient.getWasThirdPersonCamera() && progress < 1.0f) ? MathUtil.lerp(startXRot, newTargetXRot, easedProgress) : newTargetXRot;
        }
        SpatialGUIClient.setCameraPosition(position);
    }

    @Unique
    private void resetCameraState() {
        startPos = null;
        lastOffset = null;
        closingCameraActive = false;
        wasCapturing = false;
        isTransitioning = false;
        smoothedCameraYaw = 0f;
        smoothedCameraPitch = 0f;
        freeLookYaw = 0f;
        freeLookPitch = 0f;
        parallaxYawOffset = 0f;
        parallaxPitchOffset = 0f;
        lastParallaxNanos = 0;
        baseXRot = 0f;
        wasCrosshairMode = false;
        SpatialGUIClient.setWasThirdPersonCamera(false);
        SpatialGUIClient.setOriginalCameraType(null);
        SpatialGUIClient.setSwitchedToFirstPersonDueToBlock(false);
        SpatialGUIClient.setEffectiveFirstPersonMode(false);
        SpatialGUIClient.setCameraPosition(null);
        SpatialGUIClient.setWasClosing(false);
        MouseHandlerUtil.updateMouseGrabForFirstPerson(false);
    }
}