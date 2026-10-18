package org.tastytrash.spatialGUI.util;

import org.tastytrash.spatialGUI.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

public class CameraUtil {
    public static void checkBlockCollision(Entity entity) {
        if (Minecraft.getInstance().level == null || !ConfigManager.config().fallbackToFirstPersonOnBlockCollision) {
            SpatialGUIClient.setSwitchedToFirstPersonDueToBlock(false);
            return;
        }

        var renderer = SpatialGUIClient.renderer();
        float yawRadians = (float) Math.toRadians(renderer.anchoredYaw());
        double sideOffset = ConfigManager.config().cameraSideOffset;
        if (ConfigManager.config().mirrorThirdPerson) {
            sideOffset = -sideOffset;
        }
        double camX = entity.getX() + Math.sin(yawRadians) * ConfigManager.config().cameraDistance + Math.cos(yawRadians) * sideOffset;
        double camY = entity.getY() + ConfigManager.config().cameraHeightOffset;
        double camZ = entity.getZ() - Math.cos(yawRadians) * ConfigManager.config().cameraDistance + Math.sin(yawRadians) * sideOffset;

        Vec3 playerEyePos = new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
        Vec3 targetCamPos = new Vec3(camX, camY, camZ);
        BlockPos blockPos = BlockPos.containing(camX, camY, camZ);

        var blockState = Minecraft.getInstance().level.getBlockState(blockPos);
        var collisionShape = blockState.getCollisionShape(Minecraft.getInstance().level, blockPos);
        boolean isInsideBlock = !blockState.isAir() && !collisionShape.isEmpty() && collisionShape.bounds().move(blockPos).inflate(0.001).contains(targetCamPos);

        var clipContext = new ClipContext(playerEyePos, targetCamPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
        boolean pathBlocked = Minecraft.getInstance().level.clip(clipContext).getType() != HitResult.Type.MISS;

        SpatialGUIClient.setSwitchedToFirstPersonDueToBlock(isInsideBlock || pathBlocked);
    }

    public static Vec3 adjustCameraPositionForCollision(Entity entity, Vec3 eye, Vec3 target) {
        var level = Minecraft.getInstance().level;
        if (level == null) return target;

        Vec3 delta = target.subtract(eye);
        double maxDist = delta.length();
        if (maxDist < 0.001) return target;

        final double clearance = 0.2;
        double cameraDist = maxDist;

        for (int i = 0; i < 8; i++) {
            double offsetX = (i & 1) * 2 - 1;
            double offsetY = (i >> 1 & 1) * 2 - 1;
            double offsetZ = (i >> 2 & 1) * 2 - 1;
            Vec3 offset = new Vec3((offsetX * clearance), (offsetY * clearance), (offsetZ * clearance));

            Vec3 from = eye.add(offset);
            HitResult hit = level.clip(new ClipContext(from, target.add(offset), ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));

            if (hit.getType() != HitResult.Type.MISS) {
                cameraDist = Math.min(cameraDist, hit.getLocation().distanceTo(from));
            }
        }

        return eye.add(delta.scale(cameraDist / maxDist));
    }

    public static float getCurrentFov() {
        //? if >26.2 {
        /*return Minecraft.getInstance().gameRenderer.getMainCamera().getFov();
        *///?} else {
        return ConfigManager.config().overrideFov
                ? (float) ConfigManager.config().targetFov
                : (float) (int) Minecraft.getInstance().options.fov().get();
        //?}
    }

    public static float calculateFovMultiplier() {
        if (!ConfigManager.config().autoScaleByFov) return 1.0f;

        float currentFov = getCurrentFov();
        float baselineFov = (float) ConfigManager.config().autoScaleBaselineFov;

        return currentFov / baselineFov;
    }

    public static float calculateAutoFovDistance(float baseDistance, boolean isFirstPerson) {
        if (isFirstPerson) return 0.0f;
        
        float fovMultiplier = calculateFovMultiplier();
        float distanceMultiplier = (float) ConfigManager.config().autoScaleDistanceMultiplier;
        float fovAdjustment = (1.0f / fovMultiplier) - 1.0f;
        float distanceFovAdjustment = fovAdjustment * distanceMultiplier;
        
        return Mth.clamp(baseDistance * (1.0f + distanceFovAdjustment), -4, 4);
    }

    public static float calculateAutoFovSideOffset(float baseSideOffset, boolean isFirstPerson) {
        if (isFirstPerson) return 0.0f;
        
        float fovMultiplier = calculateFovMultiplier();
        float sideOffsetMultiplier = (float) ConfigManager.config().autoScaleSideOffsetMultiplier;
        float fovAdjustment = (1.0f / fovMultiplier) - 1.0f;
        float sideOffsetFovAdjustment = fovAdjustment * sideOffsetMultiplier;
        
        return Mth.clamp(baseSideOffset * (1.0f + sideOffsetFovAdjustment), -4, 4);
    }

    public static double calculateMouseSensitivity() {
        double ss = Minecraft.getInstance().options.sensitivity().get() * 0.6 + 0.2;
        return ss * ss * ss;
    }
}
