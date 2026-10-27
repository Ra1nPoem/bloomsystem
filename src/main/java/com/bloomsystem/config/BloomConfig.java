package com.bloomsystem.config;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * BloomSystem renderer configuration.
 *
 * <p>YACL writes these fields through {@link #save()} and the screen; the values are
 * also persisted to {@code config/bloomsystem.properties} so the static fallback works
 * without opening the GUI.</p>
 */
public final class BloomConfig {
    private static final Path PATH = Path.of("config", "bloomsystem.properties");

    public static boolean enabled = true;
    public static boolean hudEnabled = true;
    public static float bloomStrength = 1.0F;
    public static float dispersionStrength = 0.72F;
    public static int maxPrismShards = 48;

    private BloomConfig() {
    }

    public static void load() {
        if (!Files.isRegularFile(PATH)) {
            return;
        }
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(PATH)) {
            properties.load(in);
            enabled = Boolean.parseBoolean(properties.getProperty("enabled", "true"));
            hudEnabled = Boolean.parseBoolean(properties.getProperty("hudEnabled", "true"));
            bloomStrength = Float.parseFloat(properties.getProperty("bloomStrength", "1.0"));
            dispersionStrength = Float.parseFloat(properties.getProperty("dispersionStrength", "0.72"));
            maxPrismShards = Integer.parseInt(properties.getProperty("maxPrismShards", "48"));
        } catch (Exception ignored) {
            // Corrupt/partial config falls back to defaults.
        }
    }

    public static void save() {
        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(enabled));
        properties.setProperty("hudEnabled", Boolean.toString(hudEnabled));
        properties.setProperty("bloomStrength", Float.toString(bloomStrength));
        properties.setProperty("dispersionStrength", Float.toString(dispersionStrength));
        properties.setProperty("maxPrismShards", Integer.toString(maxPrismShards));
        try {
            Files.createDirectories(PATH.getParent());
            try (OutputStream out = Files.newOutputStream(PATH)) {
                properties.store(out, "BloomSystem renderer config");
            }
        } catch (Exception ignored) {
            // Rendering still works with the in-memory values.
        }
    }
}
