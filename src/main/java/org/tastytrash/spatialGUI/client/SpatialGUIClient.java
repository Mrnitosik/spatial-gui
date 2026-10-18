package org.tastytrash.spatialGUI.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.world.phys.Vec3;
import org.tastytrash.spatialGUI.compat.VisorCompat;
import org.tastytrash.spatialGUI.config.ConfigManager;
import org.tastytrash.spatialGUI.editor.EditorScreen;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.config.SpatialGUIConfig;
//? if fabric {
 import net.fabricmc.api.ClientModInitializer;
 import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
//? } else if neoforge {
/*import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
*///? } else if forge {
/*import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
*///? }

//? if fabric {
 public class SpatialGUIClient implements ClientModInitializer {
//? } else if neoforge{
/*public class SpatialGUIClient {
*///?} else if forge {
/*@Mod(value = SpatialGUI.MOD_ID)
public class SpatialGUIClient {
*///?}

    private static SpatialGUIRenderer renderer;
    private static boolean effectiveFirstPersonMode = false;
    private static boolean switchedToFirstPersonDueToBlock = false;
    private static boolean wasThirdPersonCamera = false;
    private static CameraType originalCameraType;
    private static boolean capturingPanorama = false;
    private static Vec3 cameraPosition;
    private static boolean wasClosing = false;
    private static boolean inShouldHookScreen = false;

    //? if fabric {
    @Override
    public void onInitializeClient() {
        SpatialGUI.initCommon();
        renderer = new SpatialGUIRenderer();
        SpatialGUIKeybinds.register();

        ScreenEvents.BEFORE_INIT.register((clientArg, screen, scaledWidth, scaledHeight) -> {
            if (SpatialGUIClient.shouldHookScreen(screen)) {
                renderer.hookScreen(screen);
            }
        });
    }
    //? } else if neoforge {
    /*public SpatialGUIClient(net.neoforged.bus.api.IEventBus modBus) {
        renderer = new SpatialGUIRenderer();

        modBus.addListener((RegisterKeyMappingsEvent e) -> SpatialGUIKeybinds.register(e));

        NeoForge.EVENT_BUS.addListener(this::onScreenInit);
    }

    private void onScreenInit(ScreenEvent.Init.Pre event) {
        var screen = event.getScreen();

        if (SpatialGUIClient.shouldHookScreen(screen)) {
            renderer.hookScreen(screen);
        }
    }
    *///?} else if forge {
    /*public SpatialGUIClient() {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        SpatialGUI.initCommon();
        renderer = new SpatialGUIRenderer();

        ModLoadingContext.get().registerExtensionPoint(
        ConfigScreenHandler.ConfigScreenFactory.class,
        () -> new ConfigScreenHandler.ConfigScreenFactory(
                (mc, parent) -> SpatialGUIConfig.HANDLER.generateGui().generateScreen(parent))
        );

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(SpatialGUIKeybinds::register);
        MinecraftForge.EVENT_BUS.addListener(this::onScreenInit);
    }

    private void onScreenInit(ScreenEvent.Init.Pre event) {
        var screen = event.getScreen();
        if (shouldHookScreen(screen)) renderer.hookScreen(screen);
    }
    *///?}

    public static boolean isEnabled() {
        if (ConfigManager.config() == null) return false;
        if (VisorCompat.isActive()) return false;
        if (capturingPanorama) return false;
        return ConfigManager.config().enabled;
    }

    public static boolean shouldHideHud() {
        return isEnabled() && ConfigManager.config().hideHud
                && renderer() != null && renderer().getHookedScreen() != null;
    }

    public static boolean shouldHideHotbar() {
        return isEnabled() && ConfigManager.config().hideHotbar
                && renderer() != null && renderer().getHookedScreen() != null;
    }

    public static boolean shouldHideCrosshair() {
        return isEnabled() && getEffectiveFirstPersonMode()
                && !ConfigManager.config().useCrosshairForFirstPerson
                && renderer() != null && renderer().getHookedScreen() != null;
    }

    public static boolean shouldHookScreen(Screen screen) {
        if (screen == null) return false;
        if (screen instanceof EditorScreen) return true;
        if (ConfigManager.config() == null) return false;
        if (Minecraft.getInstance().level == null) return false;
        var client = Minecraft.getInstance();
        boolean isFirstPerson;

        if (inShouldHookScreen) {
            isFirstPerson = true;
        } else {
            inShouldHookScreen = true;
            try {
                isFirstPerson = client.options.getCameraType() == CameraType.FIRST_PERSON;
            } finally {
                inShouldHookScreen = false;
            }
        }

        if (isFirstPerson && ConfigManager.config().disableFirstPerson) return false;
        if (!isFirstPerson && ConfigManager.config().disableThirdPerson) return false;

        String id = screen.getClass().getName();
        if (id.contains("TitleScreen") || id.contains("ReceivingLevelScreen")
                || id.contains("LevelLoadingScreen") || id.contains("ChatScreen")
                || id.contains("yacl") || id.contains("Reconfig")
                || id.contains("Progress") || id.contains("GenericMessage")
                || id.contains("Disconnected") || id.contains("ConnectScreen")
                || id.contains("GenericDirtMessage") || id.contains("GenericWaiting")
                || id.contains("WaitingForResponse") || id.contains("JoinMultiplayer")
                || id.contains("SelectWorld") || id.contains("ErrorScreen")
                || id.contains("OutOfMemory") || id.contains("DatapackLoadFailure")
                || id.contains("WinScreen") || id.contains("FileFixer"))
            return false;

        if (!id.contains("$") && !ConfigManager.config().seenScreens.contains(id)) {
            try {
                ConfigManager.config().seenScreens.add(id);
            } catch (UnsupportedOperationException e) {
                ConfigManager.config().seenScreens = new java.util.ArrayList<>(ConfigManager.config().seenScreens);
                ConfigManager.config().seenScreens.add(id);
            }
        }

        if (ConfigManager.config().disabledScreens.contains(id)) return false;

        boolean categorized = true;
        boolean toggle;
        if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) {
            toggle = ConfigManager.config().inventory;
        } else if (screen instanceof AbstractContainerScreen<?>) {
            toggle = ConfigManager.config().containers;
        } else if (screen instanceof PauseScreen) {
            toggle = ConfigManager.config().pauseScreen;
        } else {
            categorized = false;
            toggle = false;
        }

        if (categorized) return toggle;
        if (ConfigManager.config().enabledScreens.contains(id)) return true;
        return ConfigManager.config().allScreens && Minecraft.getInstance().level != null;
    }

    public static void showScreen(Minecraft client, Screen screen) {
        //? if >1.21.11 {
        client.setScreenAndShow(screen);
        //?} else {
        /*client.setScreen(screen);
        *///?}
    }

    public static SpatialGUIRenderer renderer() {
        return renderer;
    }

    public static boolean getEffectiveFirstPersonMode() {
        return effectiveFirstPersonMode;
    }

    public static void setEffectiveFirstPersonMode(boolean value) {
        effectiveFirstPersonMode = value;
    }

    public static boolean getSwitchedToFirstPersonDueToBlock() {
        return switchedToFirstPersonDueToBlock;
    }

    public static void setSwitchedToFirstPersonDueToBlock(boolean value) {
        switchedToFirstPersonDueToBlock = value;
    }

    public static boolean getWasThirdPersonCamera() {
        return wasThirdPersonCamera;
    }

    public static void setWasThirdPersonCamera(boolean value) {
        wasThirdPersonCamera = value;
    }

    public static boolean shouldUseFirstPersonMode(Screen screen) {
        if (ConfigManager.config().autoDetectCameraMode) {
            var client = Minecraft.getInstance();
            return client.options.getCameraType() == net.minecraft.client.CameraType.FIRST_PERSON;
        } else {
            boolean isInventoryScreen = screen instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen
                || screen.getClass().getName().contains("InventoryScreen");
            return (isInventoryScreen ? ConfigManager.config().firstPersonModeInventory : ConfigManager.config().firstPersonModeContainers)
                || switchedToFirstPersonDueToBlock;
        }
    }

    public static void setCapturingPanorama(boolean value) {
        capturingPanorama = value;
    }

    public static Vec3 getCameraPosition() {
        return cameraPosition;
    }

    public static void setCameraPosition(Vec3 position) {
        cameraPosition = position;
    }

    public static boolean getWasClosing() {
        return wasClosing;
    }

    public static void setWasClosing(boolean value) {
        wasClosing = value;
    }

    public static CameraType getOriginalCameraType() {
        return originalCameraType;
    }

    public static void setOriginalCameraType(CameraType value) {
        originalCameraType = value;
    }
 }