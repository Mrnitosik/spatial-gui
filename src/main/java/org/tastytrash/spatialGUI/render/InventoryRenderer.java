package org.tastytrash.spatialGUI.render;

import org.tastytrash.spatialGUI.config.ConfigManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.tastytrash.spatialGUI.editor.EditorMesh;
import org.tastytrash.spatialGUI.util.RenderUtil.QuadBasis;
import org.tastytrash.spatialGUI.util.RenderUtil.CylinderBasis;
//? if >1.21.1 {
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
//?}
//? if >26.2 {
/*import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.minecraft.client.renderer.StagedVertexBuffer;
*///?} else if 26.2 {
/*import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.StagedVertexBuffer;
*///?} else if >1.21.1 {
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
//?} else {
/*import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
//? if 1.21.1 {
/^import com.mojang.blaze3d.vertex.MeshData;
^///?}
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL11;
*///?}
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.util.AnimationUtil;
import org.tastytrash.spatialGUI.util.RenderUtil;

public class InventoryRenderer {
    private static final PoseStack WORLD_POSE_STACK = new PoseStack();
    //? if >1.21.1 {
    private static final RenderPipeline INVENTORY_PIPELINE = RenderPipelines.GUI_TEXTURED;
    private static final RenderPipeline OVERLAY_PIPELINE = RenderPipelines.GUI;
    private static final org.joml.Vector3f ZERO_VECTOR = new org.joml.Vector3f();
    private static final Matrix4f IDENTITY_MATRIX = new Matrix4f();
    private static final org.joml.Vector4f COLOR_MODULATOR = new org.joml.Vector4f();
    //?}
    //? if >1.21.1 && <26.2 {
    private static final ByteBufferBuilder INVENTORY_BYTE_BUFFER = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    //?}
    //? if >=26.2 {
    /*private static final StagedVertexBuffer INVENTORY_BUFFER = new StagedVertexBuffer(
            () -> "Spatial GUI Inventory Buffer",
            RenderType.SMALL_BUFFER_SIZE
    );
    *///?}

    private final TextureTargetManager targetManager;
    private QuadBasis quadBasis;
    private CylinderBasis cylinderBasis;
    private long basisFrame = -1;
    private int meshQuadCount = 1;
    private static final int CURVED_SEGMENTS = 32;
    private long screenOpenTime = 0;
    private static boolean isRecipeBookOpen = false;
    private static int recipeBookCloseDelay = 0;

    //? if >1.21.1 {
    private GpuBufferSlice capturedProjectionBuffer;
    private com.mojang.blaze3d.ProjectionType capturedProjectionType;
    //?} else {
    /*private final Matrix4f capturedProjectionMatrix = new Matrix4f();
    private VertexSorting capturedVertexSorting;
    *///?}
    private final PoseStack capturedPoseStack = new PoseStack();
    private boolean hasCapturedPerspective = false;
    private boolean hasDrawnThisFrame = false;

    private boolean closing = false;
    private long closeStartTime = 0;
    private long closeStartElapsed = 0;
    private long closeDurationMs = 0;
    private boolean closeFirstPerson = false;
    private float closeYaw;
    private float closePitch;

    private float smoothedLight = -1f;

    public InventoryRenderer(TextureTargetManager targetManager) {
        this.targetManager = targetManager;
    }

    private int computeLightingTint() {
        var mc = Minecraft.getInstance();
        if (!ConfigManager.config().enableWorldLightingTint || mc.level == null || mc.player == null) return 255;

        int light = mc.level.getMaxLocalRawBrightness(BlockPos.containing(mc.player.getEyePosition()));
        float target = 255f - (float) ConfigManager.config().lightingTintStrength * (1f - light / 15f);

        smoothedLight = smoothedLight < 0 ? target : smoothedLight + (target - smoothedLight) * 0.1f;
        return Math.round(smoothedLight);
    }

    private record ScreenPose(float scale, float aspect) {}

    public void updateQuadBasis() {
        if (Minecraft.getInstance().player == null) return;
        boolean isFirstPerson = SpatialGUIClient.getEffectiveFirstPersonMode();
        updateScreenBases(computeScreenPose(null, isFirstPerson), isFirstPerson);
    }

    private ScreenPose computeScreenPose(PoseStack matrices, boolean isFirstPerson) {
        var renderer = SpatialGUIClient.renderer();
        float yaw = closing ? closeYaw : renderer.anchoredYaw();
        float pitch = isFirstPerson ? (closing ? closePitch : renderer.anchoredPitch()) : 0;
        float pitchClamp = (float) ConfigManager.config().firstPersonPitchClamp;
        //? if >=26.2 {
        /*pitch = Math.clamp(pitch, -pitchClamp, pitchClamp);
         *///?} else {
        pitch = Mth.clamp(pitch, -pitchClamp, pitchClamp);
        //?}
        float yawRadians = (float) Math.toRadians(yaw);
        float pitchRadians = (float) Math.toRadians(pitch);

        RenderUtil.ScreenTransformConfig config = RenderUtil.getScreenTransformConfig(isFirstPerson);

        double lookX = -Math.sin(yawRadians) * Math.cos(pitchRadians);
        double lookY = -Math.sin(pitchRadians);
        double lookZ = Math.cos(yawRadians) * Math.cos(pitchRadians);

        float scale = AnimationUtil.scaleForElapsed(config.scale(), getAnimationElapsedMs(), isRecipeBookOpen, recipeBookCloseDelay);

        if (matrices != null) {
            RenderUtil.applyScreenTransform(matrices, isFirstPerson, yawRadians, pitchRadians, config, lookX, lookY, lookZ);
            matrices.scale(scale, scale, scale);
        }

        WORLD_POSE_STACK.setIdentity();
        RenderUtil.applyScreenTransform(WORLD_POSE_STACK, isFirstPerson, yawRadians, pitchRadians, config, lookX, lookY, lookZ);
        WORLD_POSE_STACK.scale(scale, scale, scale);

        var target = targetManager.getInventoryTarget();
        float aspect = target != null && target.height > 0
                ? (float) target.width / (float) target.height
                : 1.0f;
        return new ScreenPose(scale, aspect);
    }

    private void updateScreenBases(ScreenPose pose, boolean isFirstPerson) {
        Matrix4f worldPose = WORLD_POSE_STACK.last().pose();
        quadBasis = RenderUtil.computeQuadBasis(worldPose, pose.aspect(), pose.scale());
        cylinderBasis = isCurvedScreenActive(isFirstPerson)
                ? RenderUtil.computeCylinderBasis(worldPose, pose.aspect(), pose.scale(), (float) Math.toRadians(ConfigManager.config().curvedScreenArcDegrees))
                : null;
    }

    public QuadBasis getQuadBasis() {
        refreshBasisIfStale();
        return quadBasis;
    }

    public CylinderBasis getCylinderBasis() {
        refreshBasisIfStale();
        return cylinderBasis;
    }

    private void refreshBasisIfStale() {
        long frame = SpatialGUIRenderer.frameCounter();
        if (basisFrame != frame) {
            updateQuadBasis();
            basisFrame = frame;
        }
    }

    private static boolean isCurvedScreenActive(boolean isFirstPerson) {
        return ConfigManager.config().curvedScreenEnabled && isFirstPerson
                && ConfigManager.config().curvedScreenArcDegrees >= 5
                && ConfigManager.config().curvedScreenArcDegrees <= 150;
    }

    private void addScreenQuadMesh(VertexConsumer buffer, Matrix4f pose, float aspect, boolean isFirstPerson) {
        int lightingTint = computeLightingTint();
        if (isCurvedScreenActive(isFirstPerson)) {
            float arcRadians = (float) Math.toRadians(ConfigManager.config().curvedScreenArcDegrees);
            int layers = ConfigManager.config().dropShadowEnabled ? ConfigManager.config().dropShadowLayers : 0;
            for (int i = 0; i < layers; i++) {
                float offsetX = (float) (ConfigManager.config().dropShadowHorizontalOffset * (1 + i * 0.5));
                float offsetY = (float) (ConfigManager.config().dropShadowVerticalOffset * (1 + i * 0.5));
                float offsetZ = (float) -ConfigManager.config().dropShadowDepthOffset;
                float scale = (float) (1.0 + (ConfigManager.config().dropShadowScaleMultiplier - 1.0) * (i + 1) / layers);
                Matrix4f layerPose = new Matrix4f(pose).translate(offsetX, offsetY, offsetZ).scale(scale, scale, scale);
                RenderUtil.addCurvedScreenQuad(buffer, layerPose, aspect, arcRadians, CURVED_SEGMENTS, 0, ConfigManager.config().dropShadowAlpha / layers);
            }
            RenderUtil.addCurvedScreenQuad(buffer, pose, aspect, arcRadians, CURVED_SEGMENTS, lightingTint, ConfigManager.config().screenAlpha);
            meshQuadCount = (layers + 1) * CURVED_SEGMENTS;
        } else {
            int layers = ConfigManager.config().dropShadowEnabled ? ConfigManager.config().dropShadowLayers : 0;
            for (int i = 0; i < layers; i++) {
                float offsetX = (float) (ConfigManager.config().dropShadowHorizontalOffset * (1 + i * 0.5));
                float offsetY = (float) (ConfigManager.config().dropShadowVerticalOffset * (1 + i * 0.5));
                float offsetZ = (float) -ConfigManager.config().dropShadowDepthOffset;
                float scale = (float) (1.0 + (ConfigManager.config().dropShadowScaleMultiplier - 1.0) * (i + 1) / layers);
                Matrix4f layerPose = new Matrix4f(pose).translate(offsetX, offsetY, offsetZ).scale(scale, scale, scale);
                RenderUtil.addScreenQuad(buffer, layerPose, aspect, 0, ConfigManager.config().dropShadowAlpha / layers);
            }
            RenderUtil.addScreenQuad(buffer, pose, aspect, lightingTint, ConfigManager.config().screenAlpha);
            meshQuadCount = layers + 1;
        }
    }

    //? if >1.21.1 {
    public void capturePerspectiveState(GpuBufferSlice buffer, com.mojang.blaze3d.ProjectionType type, PoseStack poseStack) {
        this.capturedProjectionBuffer = buffer;
        this.capturedProjectionType = type;
        this.capturedPoseStack.setIdentity();
        if (poseStack != null) {
            this.capturedPoseStack.last().pose().set(poseStack.last().pose());
            this.capturedPoseStack.last().normal().set(poseStack.last().normal());
        }
        this.hasCapturedPerspective = true;
    }

    public GpuBufferSlice getCapturedProjectionBuffer() {
        return capturedProjectionBuffer;
    }

    public com.mojang.blaze3d.ProjectionType getCapturedProjectionType() {
        return capturedProjectionType;
    }
    //?} else {
    /*public void capturePerspectiveState(Matrix4f projectionMatrix, VertexSorting vertexSorting, PoseStack poseStack) {
        if (projectionMatrix != null) {
            this.capturedProjectionMatrix.set(projectionMatrix);
        }
        this.capturedVertexSorting = vertexSorting;
        this.capturedPoseStack.setIdentity();
        if (poseStack != null) {
            this.capturedPoseStack.last().pose().set(poseStack.last().pose());
            this.capturedPoseStack.last().normal().set(poseStack.last().normal());
        }
        this.hasCapturedPerspective = true;
    }
    *///?}

    private static long animationTotalMs() {
        long total = 0;
        if (ConfigManager.config().enableFadeAnimation) total = Math.max(total, ConfigManager.config().fadeDurationMs);
        if (ConfigManager.config().enableScaleAnimation) total = Math.max(total, ConfigManager.config().openAnimationDurationMs);
        return total;
    }

    public boolean beginClosing() {
        var player = Minecraft.getInstance().player;
        if (!hasCapturedPerspective || player == null || targetManager.getInventoryTarget() == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        boolean firstPerson = SpatialGUIClient.getEffectiveFirstPersonMode();
        long animationMs = Math.min(animationTotalMs(), Math.max(0L, now - screenOpenTime));

        long cameraMs = firstPerson ? 0L : Math.round(Math.max(0, ConfigManager.config().transitionDurationMs) * (1.0f - ConfigManager.config().transitionSkipPercentage / 100.0f));
        long duration = Math.max(animationMs, cameraMs);
        if (duration <= 0L) {
            return false;
        }
        closing = true;
        closeStartTime = now;
        closeStartElapsed = animationMs;
        closeDurationMs = duration;
        closeFirstPerson = firstPerson;
        var renderer = SpatialGUIClient.renderer();
        closeYaw = renderer.anchoredYaw();
        closePitch = renderer.anchoredPitch();
        hasDrawnThisFrame = false;
        return true;
    }

    public boolean isClosingFirstPerson() {
        return closeFirstPerson;
    }

    public long getCloseElapsedMs() {
        return Math.max(0L, System.currentTimeMillis() - closeStartTime);
    }

    private boolean isFirstPersonNow() {
        return closing ? closeFirstPerson : SpatialGUIClient.getEffectiveFirstPersonMode();
    }

    public boolean isClosing() {
        return closing;
    }

    public boolean isCloseFinished() {
        return System.currentTimeMillis() - closeStartTime >= closeDurationMs;
    }

    public void endClosing() {
        closing = false;
    }

    private boolean canDraw() {
        return closing ? getAnimationElapsedMs() > 0L : SpatialGUIClient.renderer().shouldCapture();
    }

    private long getAnimationElapsedMs() {
        long now = System.currentTimeMillis();
        if (!closing) {
            return now - screenOpenTime;
        }
        return Math.max(0L, closeStartElapsed - (now - closeStartTime));
    }

    private float getFadeAlpha() {
        if (!ConfigManager.config().enableFadeAnimation) {
            return 1.0F;
        }
        return Math.min(1.0F, getAnimationElapsedMs() / (float) Math.max(1, ConfigManager.config().fadeDurationMs));
    }

    public void resetPerspectiveState() {
        this.hasCapturedPerspective = false;
        this.hasDrawnThisFrame = false;
    }

    public void onFrameStart() {
        this.hasDrawnThisFrame = false;
    }

    public void setScreenOpenTime(long time) {
        this.screenOpenTime = time;
    }

    public void setRecipeBookOpen(boolean open) {
        if (isRecipeBookOpen && !open) {
            recipeBookCloseDelay = -2;
        }
        isRecipeBookOpen = open;
    }

    public void resetRecipeBookState() {
        isRecipeBookOpen = false;
        recipeBookCloseDelay = 0;
    }

    private boolean hasTexture() {
        RenderTarget target = targetManager.getInventoryTarget();
        if (target == null) {
            return false;
        }
        //? if >1.21.1 {
        return target.getColorTextureView() != null;
        //?} else {
        /*return target.getColorTextureId() > 0;
         *///?}
    }

    public void renderInWorld(PoseStack matrices) {
        Minecraft client = Minecraft.getInstance();

        if (!hasTexture() || !canDraw() || client.player == null) {
            return;
        }

        RenderTarget target = targetManager.getInventoryTarget();
        //? if >1.21.1 {
        var texture = target.getColorTextureView();
        //?} else {
        /*int texture = target.getColorTextureId();
         *///?}

        //? if >=26.2 {
        /*VertexFormat format = INVENTORY_PIPELINE.getVertexFormatBinding(0);
        if (format == null) {
            return;
        }

        StagedVertexBuffer.Draw draw = INVENTORY_BUFFER.appendDraw(format, INVENTORY_PIPELINE.getPrimitiveTopology(), null);
        StagedVertexBuffer.Draw overlayDraw = EditorMesh.active()
                ? INVENTORY_BUFFER.appendDraw(OVERLAY_PIPELINE.getVertexFormatBinding(0), OVERLAY_PIPELINE.getPrimitiveTopology(), null)
                : null;
        VertexConsumer buffer = INVENTORY_BUFFER.getVertexBuilder(draw);
        *///?} else if >1.21.1 {
        BufferBuilder buffer = new BufferBuilder(INVENTORY_BYTE_BUFFER, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        //?} else if 1.21.1 {
        /*BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
         *///?} else {
        /*BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        *///?}

        matrices.pushPose();

        boolean isFirstPerson = isFirstPersonNow();
        ScreenPose screenPose = computeScreenPose(matrices, isFirstPerson);
        recipeBookCloseDelay = isRecipeBookOpen ? -2 : Math.min(0, recipeBookCloseDelay + 1);

        Matrix4f pose = matrices.last().pose();

        updateScreenBases(screenPose, isFirstPerson);
        addScreenQuadMesh(buffer, pose, screenPose.aspect(), isFirstPerson);

        //? if >=26.2 {
        /*if (overlayDraw != null) {
            EditorMesh.addHandles(INVENTORY_BUFFER.getVertexBuilder(overlayDraw), pose, screenPose.aspect());
        }
        *///?} else {
        Matrix4f handlePose = EditorMesh.active() ? new Matrix4f(pose) : null;
        //?}

        matrices.popPose();

        //? if >=26.2 {
        /*INVENTORY_BUFFER.upload();
        StagedVertexBuffer.ExecuteInfo info = INVENTORY_BUFFER.getExecuteInfo(draw);
        if (info != null) {
            drawInventory(info, texture);
            if (overlayDraw != null) {
                StagedVertexBuffer.ExecuteInfo overlayInfo = INVENTORY_BUFFER.getExecuteInfo(overlayDraw);
                if (overlayInfo != null) {
                    drawOverlay(overlayInfo);
                }
            }
        }
        INVENTORY_BUFFER.endFrame();
        *///?} else if >1.21.1 {
        MeshData meshData = buffer.build();
        if (meshData != null) {
            try {
                drawInventory(meshData, texture);
            } finally {
                meshData.close();
            }
        }
        //?} else if 1.21.1 {
        /*drawInventory(buffer.buildOrThrow(), texture);
         *///?} else {
        /*drawInventory(buffer.end(), texture);
         *///?}

        //? if <26.2 {
        if (handlePose != null) {
            drawHandles(handlePose, screenPose.aspect());
        }
        //?}
    }

    public void renderInWorldPost() {
        Minecraft client = Minecraft.getInstance();
        if (!hasCapturedPerspective || hasDrawnThisFrame || !hasTexture() || client.player == null) {
            return;
        }

        hasDrawnThisFrame = true;

        //? if >1.21.1 {
        var prevBuffer = RenderSystem.getProjectionMatrixBuffer();
        var prevType = RenderSystem.getProjectionType();
        //?} else {
        /*Matrix4f prevProjection = RenderSystem.getProjectionMatrix();
        VertexSorting prevSorting = RenderSystem.getVertexSorting();
        var modelView = RenderSystem.getModelViewStack();
        *///?}

        try {
            //? if >1.21.1 {
            if (capturedProjectionBuffer != null && capturedProjectionType != null) {
                RenderSystem.setProjectionMatrix(capturedProjectionBuffer, capturedProjectionType);
            }
            //?} else {
            /*RenderSystem.setProjectionMatrix(capturedProjectionMatrix, capturedVertexSorting);
            modelView.pushMatrix();
            //? if >1.20.1 {
            modelView.identity();
            //?} else {
            /^modelView.setIdentity();
            ^///?}
            RenderSystem.applyModelViewMatrix();
            *///?}
            renderInWorld(capturedPoseStack);
        } finally {
            //? if >1.21.1 {
            if (prevBuffer != null && prevType != null) {
                RenderSystem.setProjectionMatrix(prevBuffer, prevType);
            }
            //?} else {
            /*modelView.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(prevProjection, prevSorting);
            *///?}
        }
    }

    //? if >1.21.1 {
    private GpuBufferSlice writeTransforms(float modulate) {
        COLOR_MODULATOR.set(modulate, modulate, modulate, modulate);
        return RenderSystem.getDynamicUniforms().writeTransform(
                IDENTITY_MATRIX,
                COLOR_MODULATOR,
                ZERO_VECTOR,
                IDENTITY_MATRIX
        );
    }

    private static FilterMode filterMode() {
        return ConfigManager.config().useLinearFiltering ? FilterMode.LINEAR : FilterMode.NEAREST;
    }
    //?}

    //? if >=26.2 {
    /*private void drawInventory(StagedVertexBuffer.ExecuteInfo info, GpuTextureView texture) {
        var output = Minecraft.getInstance().gameRenderer.mainRenderTarget().getColorTextureView();
        if (output == null) {
            return;
        }

        GpuBufferSlice dynamicTransforms = writeTransforms(getFadeAlpha());
        FilterMode filterMode = filterMode();

        try (var renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Spatial GUI",
                output,
                java.util.Optional.empty(),
                null,
                java.util.OptionalDouble.empty()
        )) {
            //? if >26.2 {
            /^renderPass.setPipeline(RenderSystem.getCompiledPipeline(INVENTORY_PIPELINE));
            ^///?} else {
            renderPass.setPipeline(InventoryRenderer.INVENTORY_PIPELINE);
            //?}
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            //? if >26.2 {
            /^renderPass.setUniform
            ^///?} else {
            renderPass.bindTexture
            //?}
                    ("Sampler0", texture, RenderSystem.getSamplerCache().getSampler(
                            AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE,
                            filterMode, filterMode, ConfigManager.config().useAnisotropicFiltering
                    ));

            renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
            renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());
            renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
        }
    }

    private void drawOverlay(StagedVertexBuffer.ExecuteInfo info) {
        var output = Minecraft.getInstance().gameRenderer.mainRenderTarget().getColorTextureView();
        if (output == null) {
            return;
        }

        GpuBufferSlice dynamicTransforms = writeTransforms(1f);

        try (var renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Spatial GUI Overlay",
                output,
                java.util.Optional.empty(),
                null,
                java.util.OptionalDouble.empty()
        )) {
            //? if >26.2 {
            /^renderPass.setPipeline(RenderSystem.getCompiledPipeline(OVERLAY_PIPELINE));
            ^///?} else {
            renderPass.setPipeline(OVERLAY_PIPELINE);
            //?}
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
            renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());
            renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
        }
    }
    *///?} else if >1.21.1 {
    private void drawInventory(MeshData meshData, GpuTextureView texture) {
        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
        var output = mainTarget.getColorTextureView();
        if (output == null) {
            return;
        }

        GpuBufferSlice dynamicTransforms = writeTransforms(getFadeAlpha());
        FilterMode filterMode = filterMode();

        var sequentialBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = sequentialBuffer.getBuffer(meshQuadCount * 6);
        VertexFormat.IndexType indexType = sequentialBuffer.type();

        try (GpuBuffer vertexBuffer = RenderSystem.getDevice().createBuffer(
                () -> "Spatial GUI Quad",
                GpuBuffer.USAGE_VERTEX,
                meshData.vertexBuffer()
        )) {
            try (var renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> "Spatial GUI",
                    output,
                    java.util.OptionalInt.empty(),
                    mainTarget.getDepthTextureView(),
                    java.util.OptionalDouble.empty()
            )) {
                renderPass.setPipeline(InventoryRenderer.INVENTORY_PIPELINE);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("DynamicTransforms", dynamicTransforms);
                renderPass.bindTexture("Sampler0", texture, RenderSystem.getSamplerCache().getSampler(
                        AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE,
                        filterMode, filterMode, ConfigManager.config().useAnisotropicFiltering
                ));
                renderPass.setVertexBuffer(0, vertexBuffer);
                renderPass.setIndexBuffer(indexBuffer, indexType);
                renderPass.drawIndexed(0, 0, meshQuadCount * 6, 1);
            }
        }
    }

    private void drawHandles(Matrix4f pose, float aspect) {
        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
        var output = mainTarget.getColorTextureView();
        if (output == null) {
            return;
        }

        BufferBuilder buffer = new BufferBuilder(INVENTORY_BYTE_BUFFER, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        EditorMesh.addHandles(buffer, pose, aspect);
        MeshData meshData = buffer.build();
        if (meshData == null) {
            return;
        }

        GpuBufferSlice dynamicTransforms = writeTransforms(1f);
        var sequentialBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = sequentialBuffer.getBuffer(EditorMesh.QUADS * 6);
        VertexFormat.IndexType indexType = sequentialBuffer.type();

        try (meshData; GpuBuffer vertexBuffer = RenderSystem.getDevice().createBuffer(
                () -> "Spatial GUI Handles",
                GpuBuffer.USAGE_VERTEX,
                meshData.vertexBuffer()
        )) {
            try (var renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> "Spatial GUI Handles",
                    output,
                    java.util.OptionalInt.empty(),
                    mainTarget.getDepthTextureView(),
                    java.util.OptionalDouble.empty()
            )) {
                renderPass.setPipeline(OVERLAY_PIPELINE);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("DynamicTransforms", dynamicTransforms);
                renderPass.setVertexBuffer(0, vertexBuffer);
                renderPass.setIndexBuffer(indexBuffer, indexType);
                renderPass.drawIndexed(0, 0, EditorMesh.QUADS * 6, 1);
            }
        }
    }
    //?} else {
    /*//? if 1.21.1 {
    /^private void drawInventory(MeshData mesh, int textureId) {
    ^///?} else {
    private void drawInventory(BufferBuilder.RenderedBuffer mesh, int textureId) {
    //?}
        float fadeAlpha = getFadeAlpha();
        int filter = ConfigManager.config().useLinearFiltering ? GL11.GL_LINEAR : GL11.GL_NEAREST;

        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);

        //? if mc1214 {
        /^RenderSystem.setShader(net.minecraft.client.renderer.CoreShaders.POSITION_TEX_COLOR);
        ^///?} else {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        //?}
        RenderSystem.setShaderTexture(0, textureId);
        RenderSystem.bindTexture(textureId);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 33071);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 33071);

        RenderSystem.setShaderColor(fadeAlpha, fadeAlpha, fadeAlpha, fadeAlpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        try {
            BufferUploader.drawWithShader(mesh);
        } finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

        private void drawHandles(Matrix4f pose, float aspect) {
        //? if 1.21.1 {
        /^BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        ^///?} else {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        //?}
        EditorMesh.addHandles(buffer, pose, aspect);

        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);

        //? if mc1214 {
        /^RenderSystem.setShader(net.minecraft.client.renderer.CoreShaders.POSITION_COLOR);
        ^///?} else {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        //?}
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        try {
            //? if 1.21.1 {
            /^BufferUploader.drawWithShader(buffer.buildOrThrow());
            ^///?} else {
            BufferUploader.drawWithShader(buffer.end());
            //?}
        } finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    *///?}
}