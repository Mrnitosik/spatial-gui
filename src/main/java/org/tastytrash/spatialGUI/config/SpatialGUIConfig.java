package org.tastytrash.spatialGUI.config;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.ConfigField;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.*;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
//?} else if neoforge {
/*import net.neoforged.fml.loading.FMLPaths;
 *///?} else if forge {
/*import net.minecraftforge.fml.loading.FMLPaths;
 *///?}
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
 *///?}
import java.util.ArrayList;
import java.util.List;

public class SpatialGUIConfig {

    public static final ConfigClassHandler<SpatialGUIConfig> HANDLER =
            ConfigClassHandler.createBuilder(SpatialGUIConfig.class)
                    //? if >=1.21.11 {
                    .id(Identifier.fromNamespaceAndPath("spatial-gui", "config"))
                    //?} else if >1.20.1 {
                    /*.id(ResourceLocation.fromNamespaceAndPath("spatial-gui", "config"))
                     *///?} else {
                    /*.id(new ResourceLocation("spatial-gui", "config"))
                     *///?}
                    .serializer(config -> GsonConfigSerializerBuilder.create(config)
                            .setPath(configDir().resolve("spatial-gui.json5"))
                            .setJson5(true)
                            .build())
                    .build();

    public static SpatialGUIConfig get() {
        return HANDLER.instance();
    }

    // ========
    // general
    // ========
    @SerialEntry
    @AutoGen(category = "general")
    @TickBox
    public boolean enabled = true;

    // viewMode 
    @MasterTickBox(value = {"firstPersonModeInventory", "firstPersonModeContainers"}, invert = true)
    @SerialEntry
    @AutoGen(category = "general", group = "viewMode")
    public boolean autoDetectCameraMode = false;

    @SerialEntry
    @AutoGen(category = "general", group = "viewMode")
    @TickBox
    public boolean firstPersonModeInventory = false;

    @SerialEntry
    @AutoGen(category = "general", group = "viewMode")
    @TickBox
    public boolean firstPersonModeContainers = false;

    @SerialEntry
    @AutoGen(category = "general", group = "viewMode")
    @TickBox
    public boolean useCrosshairForFirstPerson = true;

    // hud 
    @MasterTickBox(value = {"hideHotbar"}, invert = true)
    @SerialEntry
    @AutoGen(category = "general", group = "hud")
    public boolean hideHud = false;

    @SerialEntry
    @AutoGen(category = "general", group = "hud")
    @TickBox
    public boolean hideHotbar = false;

    // guiScale 
    @MasterTickBox(value = {"guiScale"})
    @SerialEntry
    @AutoGen(category = "general", group = "guiScale")
    @TickBox
    public boolean overrideGuiScale = false;

    @SerialEntry
    @AutoGen(category = "general", group = "guiScale")
    @IntSlider(min = 1, max = 6, step = 1)
    public int guiScale = 4;

    // ========
    // screens
    // ========
    @SerialEntry
    @AutoGen(category = "screens")
    @TickBox
    public boolean inventory = true;

    @SerialEntry
    @AutoGen(category = "screens")
    @TickBox
    public boolean containers = true;

    @SerialEntry
    @AutoGen(category = "screens")
    @TickBox
    public boolean pauseScreen = false;

    @SerialEntry
    @AutoGen(category = "screens")
    @TickBox
    public boolean allScreens = false;

    @SerialEntry
    @AutoGen(category = "screens")
    @ListGroup(valueFactory = StringValueFactory.class, controllerFactory = StringControllerFactory.class)
    public List<String> enabledScreens = defaultEnabledScreens();

    @SerialEntry
    @AutoGen(category = "screens")
    @ListGroup(valueFactory = StringValueFactory.class, controllerFactory = StringControllerFactory.class)
    public List<String> disabledScreens = new ArrayList<>();

    @SerialEntry
    @AutoGen(category = "screens")
    @ListGroup(valueFactory = StringValueFactory.class, controllerFactory = StringControllerFactory.class)
    public List<String> seenScreens = new ArrayList<>();

    @SerialEntry
    @AutoGen(category = "screens")
    @TickBox
    public boolean disableFirstPerson = false;

    @SerialEntry
    @AutoGen(category = "screens")
    @TickBox
    public boolean disableThirdPerson = false;

    // ==========
    // appearance
    // ==========
    // size 
    @SerialEntry
    @AutoGen(category = "appearance", group = "size")
    @IntSlider(min = 25, max = 100, step = 1)
    public int renderScalePercent = 100;

    @SerialEntry
    @AutoGen(category = "appearance", group = "size")
    @DoubleSlider(min = 1.0, max = 3.0, step = 0.05)
    public double recipeBookShrinkFactor = 1.5;

    // dropShadow
    @MasterTickBox(value = {"dropShadowSoftness", "dropShadowAlpha", "dropShadowScaleMultiplier", "dropShadowHorizontalOffset", "dropShadowVerticalOffset", "dropShadowDepthOffset"})
    @SerialEntry
    @AutoGen(category = "appearance", group = "dropShadow")
    public boolean dropShadowEnabled = true;

    @SerialEntry
    @AutoGen(category = "appearance", group = "dropShadow")
    @IntSlider(min = 1, max = 6, step = 1)
    public int dropShadowLayers = 1;

    @SerialEntry
    @AutoGen(category = "appearance", group = "dropShadow")
    @IntSlider(min = 0, max = 255, step = 1)
    public int dropShadowAlpha = 120;

    @SerialEntry
    @AutoGen(category = "appearance", group = "dropShadow")
    @DoubleSlider(min = 0.8, max = 1.2, step = 0.01)
    public double dropShadowScaleMultiplier = 1.0;

    @SerialEntry
    @AutoGen(category = "appearance", group = "dropShadow")
    @DoubleSlider(min = -0.05, max = 0.05, step = 0.001)
    public double dropShadowHorizontalOffset = 0.008;

    @SerialEntry
    @AutoGen(category = "appearance", group = "dropShadow")
    @DoubleSlider(min = -0.05, max = 0.05, step = 0.001)
    public double dropShadowVerticalOffset = -0.012;

    @SerialEntry
    @AutoGen(category = "appearance", group = "dropShadow")
    @DoubleSlider(min = 0.0, max = 0.05, step = 0.001)
    public double dropShadowDepthOffset = 0.002;

    // lighting
    @MasterTickBox(value = {"lightingTintStrength"})
    @SerialEntry
    @AutoGen(category = "appearance", group = "lighting")
    public boolean enableWorldLightingTint = false;

    @SerialEntry
    @AutoGen(category = "appearance", group = "lighting")
    @DoubleSlider(min = 0.0, max = 120.0, step = 1.0)
    public double lightingTintStrength = 50.0;

    // firstPersonHands
    @MasterTickBox(value = {"hideShieldInFirstPerson"}, invert = true)
    @SerialEntry
    @AutoGen(category = "appearance", group = "firstPersonHands")
    public boolean hideHandsInFirstPerson = false;

    @SerialEntry
    @AutoGen(category = "appearance", group = "firstPersonHands")
    @TickBox
    public boolean hideShieldInFirstPerson = true;

    @SerialEntry
    @AutoGen(category = "appearance", group = "firstPersonHands")
    @TickBox
    public boolean swingArmOnFirstPersonClick = false;

    // texture
    @SerialEntry
    @AutoGen(category = "appearance", group = "texture")
    @IntSlider(min = 0, max = 255, step = 1)
    public int screenAlpha = 255;

    //? if >=26.1.2 {
    @MasterTickBox(value = {"worldBlurStrength"})
    @SerialEntry
    @AutoGen(category = "appearance", group = "texture")
    public boolean blurWorldBackground = false;

    @SerialEntry
    @AutoGen(category = "appearance", group = "texture")
    @IntSlider(min = 1, max = 10, step = 1)
    public int worldBlurStrength = 5;
    //?}

    @SerialEntry
    @AutoGen(category = "appearance", group = "texture")
    @TickBox
    public boolean useLinearFiltering = true;

    @SerialEntry
    @AutoGen(category = "appearance", group = "texture")
    @TickBox
    public boolean useAnisotropicFiltering = true;

    // ===========
    // thirdPerson
    // ===========
    // screen 
    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "screen")
    @DoubleSlider(min = 0.5, max = 10.0, step = 0.05)
    public double screenDistance = 2.5;

    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "screen")
    @DoubleSlider(min = -5.0, max = 5.0, step = 0.05)
    public double screenSideOffset = -0.6;

    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "screen")
    @DoubleSlider(min = -3.0, max = 3.0, step = 0.05)
    public double screenHeightOffset = -0.4;

    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "screen")
    @DoubleSlider(min = -360.0, max = 360.0, step = 1.0)
    public double screenYawOffset = 160.0;

    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "screen")
    @DoubleSlider(min = -90.0, max = 90.0, step = 1.0)
    public double screenPitchOffset = 0.0;

    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "screen")
    @DoubleSlider(min = 0.5, max = 10.0, step = 0.05)
    public double screenScale = 3.5;

    // camera 
    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "camera")
    @DoubleSlider(min = 0.5, max = 6.0, step = 0.05)
    public double cameraDistance = 1.7;

    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "camera")
    @DoubleSlider(min = -4.0, max = 4.0, step = 0.05)
    public double cameraSideOffset = -1.2;

    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "camera")
    @DoubleSlider(min = -1.0, max = 4.0, step = 0.05)
    public double cameraHeightOffset = 1.5;

    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "camera")
    @DoubleSlider(min = -90.0, max = 90.0, step = 1.0)
    public double cameraTargetPitch = 10.0;

    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "camera")
    @TickBox
    public boolean lerpCameraPitch = true;

    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "camera")
    @TickBox
    public boolean disableThirdPersonParallax = false;

    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "camera")
    @TickBox
    public boolean fallbackToFirstPersonOnBlockCollision = false;

    // avatar 
    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "avatar")
    @TickBox
    public boolean mirrorThirdPerson = false;

    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "avatar")
    @TickBox
    public boolean mirrorHeadMovement = false;

    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "avatar")
    @DoubleSlider(min = -180.0, max = 180.0, step = 1.0)
    public double avatarBaseYawOffset = 30.0;

    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "avatar")
    @DoubleSlider(min = -180.0, max = 180.0, step = 1.0)
    public double avatarBodyRotationOffset = 20.0;

    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "avatar")
    @IntSlider(min = 0, max = 90, step = 1)
    public int avatarMaxYawOffset = 40;

    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "avatar")
    @IntSlider(min = 0, max = 90, step = 1)
    public int avatarMaxPitchOffset = 25;

    // controls 
    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "controls")
    @DoubleSlider(min = 0.01, max = 1.0, step = 0.01)
    public double thirdPersonMouseSensitivityYaw = 0.1;

    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "controls")
    @DoubleSlider(min = 0.01, max = 1.0, step = 0.01)
    public double thirdPersonMouseSensitivityPitch = 0.03;

    // transition 
    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "transition")
    @IntSlider(min = 1, max = 5000, step = 10)
    public int transitionDurationMs = 300;

    @SerialEntry
    @AutoGen(category = "thirdPerson", group = "transition")
    @IntSlider(min = 0, max = 100, step = 1)
    public int transitionSkipPercentage = 0;

    // ===========
    // firstPerson
    // ===========
    // screen 

    @MasterTickBox(value = {"curvedScreenArcDegrees"})
    @SerialEntry
    @AutoGen(category = "firstPerson", group = "screen")
    public boolean curvedScreenEnabled = false;

    @SerialEntry
    @AutoGen(category = "firstPerson", group = "screen")
    @IntSlider(min = 5, max = 150, step = 1)
    public int curvedScreenArcDegrees = 30;

    @SerialEntry
    @AutoGen(category = "firstPerson", group = "screen")
    @DoubleSlider(min = 0.5, max = 6.0, step = 0.05)
    public double firstPersonScreenDistance = 1.5;

    @SerialEntry
    @AutoGen(category = "firstPerson", group = "screen")
    @DoubleSlider(min = -3.0, max = 3.0, step = 0.05)
    public double firstPersonScreenSideOffset = 0.0;

    @SerialEntry
    @AutoGen(category = "firstPerson", group = "screen")
    @DoubleSlider(min = -3.0, max = 3.0, step = 0.05)
    public double firstPersonScreenHeightOffset = 0.0;

    @SerialEntry
    @AutoGen(category = "firstPerson", group = "screen")
    @DoubleSlider(min = -360.0, max = 360.0, step = 1.0)
    public double firstPersonScreenYawOffset = 180.0;

    @SerialEntry
    @AutoGen(category = "firstPerson", group = "screen")
    @DoubleSlider(min = -90.0, max = 90.0, step = 1.0)
    public double firstPersonScreenPitchOffset = 0.0;

    @SerialEntry
    @AutoGen(category = "firstPerson", group = "screen")
    @DoubleSlider(min = 0.5, max = 10.0, step = 0.05)
    public double firstPersonScreenScale = 1.8;

    // camera 
    @SerialEntry
    @AutoGen(category = "firstPerson", group = "camera")
    @TickBox
    public boolean keepFirstPersonCameraAngle = true;

    @SerialEntry
    @AutoGen(category = "firstPerson", group = "camera")
    @TickBox
    public boolean disableFirstPersonParallax = false;

    // controls 
    @SerialEntry
    @AutoGen(category = "firstPerson", group = "controls")
    @DoubleSlider(min = 0.01, max = 1.0, step = 0.01)
    public double firstPersonMouseSensitivityYaw = 0.4;

    @SerialEntry
    @AutoGen(category = "firstPerson", group = "controls")
    @DoubleSlider(min = 0.01, max = 1.0, step = 0.01)
    public double firstPersonMouseSensitivityPitch = 0.12;

    @SerialEntry
    @AutoGen(category = "firstPerson", group = "controls")
    @IntSlider(min = 0, max = 90, step = 1)
    public int firstPersonPitchClamp = 40;

    @SerialEntry
    @AutoGen(category = "firstPerson", group = "controls")
    @TickBox
    public boolean lockFirstPersonYaw = false;

    // ==========
    // animations
    // ==========
    @SerialEntry
    @AutoGen(category = "animations")
    @TickBox
    public boolean enableCloseAnimation = true;

    // fade
    @MasterTickBox(value = {"fadeDurationMs"})
    @SerialEntry
    @AutoGen(category = "animations", group = "fade")
    public boolean enableFadeAnimation = true;

    @SerialEntry
    @AutoGen(category = "animations", group = "fade")
    @IntSlider(min = 0, max = 1000, step = 10)
    public int fadeDurationMs = 150;

    // scale 
    @MasterTickBox(value = {"openAnimationDurationMs", "animationStartScalePercent", "animationEasing"})
    @SerialEntry
    @AutoGen(category = "animations", group = "scale")
    public boolean enableScaleAnimation = true;

    @SerialEntry
    @AutoGen(category = "animations", group = "scale")
    @IntSlider(min = 50, max = 1000, step = 10)
    public int openAnimationDurationMs = 300;

    @SerialEntry
    @AutoGen(category = "animations", group = "scale")
    @IntSlider(min = 1, max = 100, step = 1)
    public int animationStartScalePercent = 75;

    @SerialEntry
    @AutoGen(category = "animations", group = "scale")
    @EnumCycler
    public EasingType animationEasing = EasingType.Back;

    public enum EasingType {
        Cubic,
        Elastic,
        Bounce,
        Back,
        Exponential,
        Quadratic,
        Quartic
    }

    // ====
    // fov
    // ====
    // override 
    @MasterTickBox(value = {"targetFov", "fovLerpSpeed"})
    @SerialEntry
    @AutoGen(category = "fov", group = "override")
    public boolean overrideFov = false;

    @SerialEntry
    @AutoGen(category = "fov", group = "override")
    @IntSlider(min = 30, max = 110, step = 1)
    public int targetFov = 70;

    @SerialEntry
    @AutoGen(category = "fov", group = "override")
    @IntSlider(min = 0, max = 50, step = 1)
    public int fovLerpSpeed = 10;

    // autoScale 
    @MasterTickBox(value = {
            "autoScaleBaselineFov",
            "autoScaleScreenPower",
            "autoScaleSideOffsetMultiplier",
            "autoScaleDistanceMultiplier",
            "autoScaleThirdPersonScreenMultiplier"
    })
    @SerialEntry
    @AutoGen(category = "fov", group = "autoScale")
    public boolean autoScaleByFov = true;

    @SerialEntry
    @AutoGen(category = "fov", group = "autoScale")
    @DoubleSlider(min = 30.0, max = 110.0, step = 1.0)
    public double autoScaleBaselineFov = 70.0;

    @SerialEntry
    @AutoGen(category = "fov", group = "autoScale")
    @DoubleSlider(min = 0.1, max = 3.0, step = 0.05)
    public double autoScaleScreenPower = 1.2;

    @SerialEntry
    @AutoGen(category = "fov", group = "autoScale")
    @DoubleSlider(min = -2.0, max = 2.0, step = 0.05)
    public double autoScaleSideOffsetMultiplier = -0.3;

    @SerialEntry
    @AutoGen(category = "fov", group = "autoScale")
    @DoubleSlider(min = 0.1, max = 3.0, step = 0.05)
    public double autoScaleDistanceMultiplier = 0.8;

    @SerialEntry
    @AutoGen(category = "fov", group = "autoScale")
    @DoubleSlider(min = 0.1, max = 2.0, step = 0.01)
    public double autoScaleThirdPersonScreenMultiplier = 0.82;





    public int getEffectiveGuiScale() {
        if (overrideGuiScale) {
            return Mth.clamp(guiScale, 1, 6);
        }
        Minecraft mc = Minecraft.getInstance();
        return mc.getWindow().calculateScale(mc.options.guiScale().get(), mc.isEnforceUnicode());
    }
    
    public static class StringValueFactory implements ListGroup.ValueFactory<String> {
        @Override
        public String provideNewValue() {
            return "";
        }
    }

    public static class StringControllerFactory implements ListGroup.ControllerFactory<String> {
        @Override
        public ControllerBuilder<String> createController(ListGroup annotation, ConfigField<List<String>> field, OptionAccess storage, Option<String> option) {
            return StringControllerBuilder.create(option);
        }
    }

    private static List<String> defaultEnabledScreens() {
        String pkg = "net.minecraft.client.gui.screens.inventory.";
        List<String> list = new ArrayList<>();
        for (String name : new String[] {
                "BookViewScreen", "BookEditScreen", "BookSignScreen",
                "CraftingScreen",
                "FurnaceScreen", "SmokerScreen", "BlastFurnaceScreen",
                "AnvilScreen", "EnchantmentScreen", "BeaconScreen", "BrewingStandScreen",
                "MerchantScreen", "ContainerScreen", "ShulkerBoxScreen", "HopperScreen",
                "DispenserScreen", "GrindstoneScreen", "SmithingScreen", "CartographyTableScreen",
                "LoomScreen", "StonecutterScreen", "LecternScreen",
                "InventoryScreen", "CreativeModeInventoryScreen"
        }) {
            list.add(pkg + name);
        }
        List<String> modded = List.of(
                "dev.emi.emi.screen.RecipeScreen",
                "mezz.jei.gui.recipes.RecipesGui",
                "me.shedaniel.rei.impl.client.gui.screen.DefaultDisplayViewingScreen",
                "cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen"
        );
        list.addAll(modded);
        return list;
    }

    public static java.nio.file.Path configDir() {
        //? if fabric {
        return FabricLoader.getInstance().getConfigDir();
        //?} else {
        /*return FMLPaths.CONFIGDIR.get();
         *///?}
    }
}