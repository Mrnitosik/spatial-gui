package org.tastytrash.spatialGUI.mixin.render;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;

//? if >1.21.1 {
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
//?} else if mc1214 {
/*import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
*///?} else {
/*import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
*///?}

@Mixin(PlayerModel.class)
public class PlayerModelMixin {
    @Unique private static final double HIDE_DISTANCE_SQ = 0.6 * 0.6;
    @Unique private boolean spatialGUI$headHidden = false;

    @Unique
    private void spatialGUI$hideHead(ModelPart head, ModelPart hat, double x, double y, double z, float eyeHeight) {
        if (spatialGUI$headHidden) {
            head.visible = true;
            hat.visible = true;
            spatialGUI$headHidden = false;
        }

        var renderer = SpatialGUIClient.renderer();
        Vec3 cameraPos = SpatialGUIClient.getCameraPosition();
        if (renderer == null || cameraPos == null || !SpatialGUIClient.isEnabled() || SpatialGUIClient.getEffectiveFirstPersonMode()) {
            return;
        }
        if (!renderer.shouldCapture() && !renderer.isClosing() && !SpatialGUIClient.getWasClosing()) {
            return;
        }

        double dx = cameraPos.x - x;
        double dy = cameraPos.y - (y + eyeHeight);
        double dz = cameraPos.z - z;
        if (dx * dx + dy * dy + dz * dz < HIDE_DISTANCE_SQ) {
            head.visible = false;
            hat.visible = false;
            spatialGUI$headHidden = true;
        }
    }

    //? if >1.21.1 {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
    private void spatialGUI$hideHeadWhenCameraIsClose(AvatarRenderState state, CallbackInfo ci) {
        PlayerModel model = (PlayerModel) (Object) this;
        spatialGUI$hideHead(model.head, model.hat, state.x, state.y, state.z, state.eyeHeight);
    }
    //?} else if mc1214 {
    /*@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;)V", at = @At("TAIL"), require = 0)
    private void spatialGUI$hideHeadWhenCameraIsClose(PlayerRenderState state, CallbackInfo ci) {
        PlayerModel model = (PlayerModel) (Object) this;
        spatialGUI$hideHead(model.head, model.hat, state.x, state.y, state.z, state.eyeHeight);
    }
    *///?} else {
    /*@Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void spatialGUI$hideHeadWhenCameraIsClose(LivingEntity entity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        PlayerModel<?> model = (PlayerModel<?>) (Object) this;
        spatialGUI$hideHead(model.head, model.hat, entity.getX(), entity.getY(), entity.getZ(), entity.getEyeHeight());
    }
    *///?}
}