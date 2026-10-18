package org.tastytrash.spatialGUI.mixin.render;

import org.tastytrash.spatialGUI.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.mixin.gui.MouseHandlerAccessor;

//? if >1.21.1 {
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?} else if mc1214 {
/*import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?} else {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;
*///?}

//? if >1.21.1 {
@Mixin(AvatarRenderer.class)
//?} else {
/*@Mixin(PlayerRenderer.class)
 *///?}
public class AvatarRendererMixin {
    @Unique private static final float SMOOTHING = 0.15f;

    @Unique private static float smoothedYaw, smoothedPitch;
    @Unique private static float shownBody, shownYaw, shownPitch;
    @Unique private static long openStartNanos;

    @Unique
    private static float[] spatialGUI$computePose(float vanillaBody, float vanillaYaw, float vanillaPitch) {
        Minecraft client = Minecraft.getInstance();
        var renderer = SpatialGUIClient.renderer();
        boolean closing = renderer.isClosing() && !renderer.isClosingFirstPerson();

        if (!(renderer.shouldCapture() || closing) || !SpatialGUIClient.isEnabled()) {
            renderer.poseStarted = false;
            return null;
        }

        if (closing) {
            float blend = renderer.getCloseBlend();
            return new float[] {
                    Mth.rotLerp(blend, shownBody, vanillaBody),
                    Mth.rotLerp(blend, shownYaw, vanillaYaw),
                    Mth.lerp(blend, shownPitch, vanillaPitch)
            };
        }

        var mouse = (MouseHandlerAccessor) client.mouseHandler;
        float normX = Mth.clamp((float) (mouse.getRawXpos() / client.getWindow().getScreenWidth()) * 2f - 1f, -1f, 1f);
        float normY = Mth.clamp((float) (mouse.getRawYpos() / client.getWindow().getScreenHeight()) * 2f - 1f, -1f, 1f);
        if (ConfigManager.config().mirrorHeadMovement) normX = -normX;

        float side = ConfigManager.config().mirrorThirdPerson ? -1f : 1f;
        float targetBody = renderer.anchoredYaw() + side * (float) ConfigManager.config().avatarBodyRotationOffset;
        float targetYaw = normX * (float) ConfigManager.config().avatarMaxYawOffset + side * (float) ConfigManager.config().avatarBaseYawOffset;
        float targetPitch = Mth.clamp(normY * (float) ConfigManager.config().avatarMaxPitchOffset, -60f, 60f);

        long now = System.nanoTime();
        boolean justOpened = !renderer.poseStarted;

        if (justOpened) {
            smoothedYaw = targetYaw;
            smoothedPitch = targetPitch;
            openStartNanos = now;
            renderer.poseStarted = true;
        } else {
            smoothedYaw = Mth.rotLerp(SMOOTHING, smoothedYaw, targetYaw);
            smoothedPitch = Mth.lerp(SMOOTHING, smoothedPitch, targetPitch);
        }

        float t = Mth.clamp((now - openStartNanos) / 1000000f / ConfigManager.config().openAnimationDurationMs, 0f, 1f);
        float open = 1f - (1f - t) * (1f - t) * (1f - t);

        shownBody = Mth.rotLerp(open, vanillaBody, targetBody);
        shownYaw = Mth.rotLerp(open, vanillaYaw, smoothedYaw);
        shownPitch = Mth.lerp(open, vanillaPitch, smoothedPitch);
        return new float[] {shownBody, shownYaw, shownPitch};
    }

    //? if >1.21.1 {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void spatialGUI$overrideHeadLook(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        if (entity != Minecraft.getInstance().player) return;

        float[] pose = spatialGUI$computePose(state.bodyRot, state.yRot, state.xRot);
        if (pose == null) return;

        state.bodyRot = pose[0];
        state.yRot = pose[1];
        state.xRot = pose[2];
    }
//?} else if mc1214 {
    /*@Inject(method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V", at = @At("TAIL"), require = 0)
    private void spatialGUI$overrideHeadLook(AbstractClientPlayer entity, PlayerRenderState state, float partialTicks, CallbackInfo ci) {
        if (entity != Minecraft.getInstance().player) return;

        float[] pose = spatialGUI$computePose(state.bodyRot, state.yRot, state.xRot);
        if (pose == null) return;

        state.bodyRot = pose[0];
        state.yRot = pose[1];
        state.xRot = pose[2];
    }
*///?} else {
    /*@WrapMethod(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    private void spatialGUI$overrideHeadLook(AbstractClientPlayer entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, Operation<Void> original) {
        if (entity != Minecraft.getInstance().player || SpatialGUIRenderer.isExtractingScreen) {
            original.call(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
            return;
        }

        float vanillaBody = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float vanillaHead = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float vanillaPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

        float[] pose = spatialGUI$computePose(vanillaBody, Mth.wrapDegrees(vanillaHead - vanillaBody), vanillaPitch);
        if (pose == null) {
            original.call(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
            return;
        }

        float oldBody = entity.yBodyRot, oldBodyO = entity.yBodyRotO;
        float oldHead = entity.yHeadRot, oldHeadO = entity.yHeadRotO;
        float oldXRot = entity.getXRot(), oldXRotO = entity.xRotO;

        entity.yBodyRot = entity.yBodyRotO = pose[0];
        entity.yHeadRot = entity.yHeadRotO = pose[0] + pose[1];
        entity.setXRot(pose[2]);
        entity.xRotO = pose[2];

        try {
            original.call(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        } finally {
            entity.yBodyRot = oldBody;
            entity.yBodyRotO = oldBodyO;
            entity.yHeadRot = oldHead;
            entity.yHeadRotO = oldHeadO;
            entity.setXRot(oldXRot);
            entity.xRotO = oldXRotO;
        }
    }
    *///?}
}