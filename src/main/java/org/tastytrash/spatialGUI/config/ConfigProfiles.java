package org.tastytrash.spatialGUI.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.tastytrash.spatialGUI.SpatialGUI;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfigProfiles {
    public static final ConfigProfiles EMPTY = new ConfigProfiles(Map.of(), new Rules(Map.of()));

    private static final Gson GSON = new Gson();

    private final Map<String, Overrides> profiles;
    private final Rules rules;

    // the Field is an individual config setting, the Object is the value of that config
    record Overrides(Map<Field, Object> setting) {
        void applyTo(SpatialGUIConfig config) throws IllegalAccessException {
            for (var e : setting.entrySet()) e.getKey().set(config, e.getValue());
        }
    }

    // the first String is the target, either an exact class name or a category
    // the second String is the profile name
    record Rules(Map<String, String> rules) {
        String profileFor(String className, String category) {
            String name = rules.get(className);
            if (name == null) name = rules.get(category);
            return name != null ? name : rules.get("#all");
        }

        boolean isEmpty() {
            return rules.isEmpty();
        }
    }

    private ConfigProfiles(Map<String, Overrides> profiles, Rules rules) {
        this.profiles = profiles;
        this.rules = rules;
    }

    public boolean isEmpty() {
        return profiles.isEmpty() || rules.isEmpty();
    }

    public String summary() {
        return profiles.size() + " profile(s)";
    }

    public SpatialGUIConfig resolve(String className, String category, SpatialGUIConfig global) {
        String name = rules.profileFor(className, category);
        if (name == null) return null;

        Overrides values = profiles.get(name);
        if (values == null) return null;

        SpatialGUIConfig copy = new SpatialGUIConfig();
        try {
            for (Field f : SpatialGUIConfig.class.getFields()) {
                // HANDLER is static & final, modifying it would throw
                if (f.getName().equals("HANDLER")) continue;
                f.set(copy, f.get(global));
            }
            values.applyTo(copy);
        } catch (IllegalAccessException e) {
            return null;
        }
        return copy;
    }

    public static ConfigProfiles load(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.writeString(path, TEMPLATE);
        }
        return parse(Files.readString(path));
    }

    public static ConfigProfiles parse(String text) {
        JsonObject obj = JsonParser.parseString(text).getAsJsonObject();

        Map<String, Overrides> profiles = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> e : obj.getAsJsonObject("profiles").entrySet()) {
            profiles.put(e.getKey(), new Overrides(convert(e.getKey(), e.getValue().getAsJsonObject())));
        }

        Map<String, String> rules = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> e : obj.getAsJsonObject("rules").entrySet()) {
            String profileName = e.getValue().getAsString();
            if (!profiles.containsKey(profileName)) {
                SpatialGUI.LOGGER.warn("'{}' is assigned to unknown profile '{}'", e.getKey(), profileName);
            }
            rules.put(e.getKey(), profileName);
        }

        return new ConfigProfiles(profiles, new Rules(rules));
    }

    private static Map<Field, Object> convert(String profile, JsonObject profileObj) {
        if (profileObj.has("guiScale") && !profileObj.has("overrideGuiScale")) {
            profileObj.addProperty("overrideGuiScale", true);
        }

        Map<Field, Object> values = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> e : profileObj.entrySet()) {
            String key = e.getKey();

            try {
                Field field = SpatialGUIConfig.class.getField(key);
                // HANDLER is static & final, modifying it would throw
                if (field.getName().equals("HANDLER")) continue;

                Object value = GSON.fromJson(e.getValue(), field.getType());
                if (value == null) {
                    SpatialGUI.LOGGER.warn("profile '{}': no usable value for '{}'", profile, key);
                    continue;
                }
                values.put(field, value);
            } catch (NoSuchFieldException ex) {
                SpatialGUI.LOGGER.warn("profile '{}': unknown setting '{}'", profile, key);
            } catch (RuntimeException ex) {
                SpatialGUI.LOGGER.warn("profile '{}': couldn't read value for '{}'", profile, key);
            }
        }
        return values;
    }

    private static final String TEMPLATE = """
            // Spatial GUI screen profiles
            //
            // A profile is a group of settings overrides. This is mainly for modpack creators or advanced users.
            // Any other setting not included in a profile uses your normal Spatial GUI config settings.
            //
            // "rules" decides which screens use which profile. For each screen, the first match overrides:
            //   1. exact class name   "net.minecraft.client.gui.screens.inventory.CraftingScreen"
            //   2. category           "#inventory", "#containers" or "#pause"
            //   3. "#all"             every screen not included above
            //
            // Setting names are the same as in spatial-gui.json5. Setting "guiScale" automatically turns on "overrideGuiScale".
            //
            // You can find exact class names in the 'seenScreens' list in the config,
            // either in game under the 'Screens' tab or in spatial-gui.json5
            //
            // Example, remove the // to use:
            // {
            //   profiles: {
            //     compact: { guiScale: 3, firstPersonScreenScale: 1.2 },
            //     wide:    { autoDetectCameraMode: true, guiScale: 4, curvedScreenEnabled: true, curvedScreenArcDegrees: 60 }
            //   },
            //   rules: {
            //     "#containers": "compact",
            //     "net.minecraft.client.gui.screens.inventory.CraftingScreen": "wide"
            //   }
            // }
            
            {
              profiles: {},
              rules: {}
            }
            """;
}