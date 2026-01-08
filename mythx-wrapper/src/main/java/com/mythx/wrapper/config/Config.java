package com.mythx.wrapper.config;

import com.mythx.wrapper.model.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URISyntaxException;

@Getter
@Setter
@Slf4j
public class Config {
    private static Config instance;

    private static final String BASE_DIR = "C:" + File.separator + ".mythx" + File.separator;
    private final String SAVE_DIR = System.getProperty("user.home") + File.separator;

    // Single launcher manifest URL (launcher updates are always from production)
    private static final String LAUNCHER_MANIFEST = "https://files.mythxrsps.com/launcher/manifest.json";

    private String launcherVersionUrl;
    private Version localVersion;
    private Version remoteVersion;
    private String defaultGameServer;
    private boolean betaMode = false;

    private Config() {
    }

    public static Config get() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    /**
     * Detects if the wrapper is running in beta mode by checking the JAR filename.
     * Beta mode is enabled if the filename contains "beta" (case-insensitive).
     * Beta mode affects which CLIENT manifest the launcher downloads from, not the launcher itself.
     */
    public void detectBetaMode() {
        try {
            String jarPath = Config.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            String jarName = new File(jarPath).getName().toLowerCase();

            log.info("Wrapper JAR name: {}", jarName);

            // Check if filename contains "beta" (handles: Beta, -beta, _beta, .beta, etc.)
            if (jarName.contains("beta")) {
                betaMode = true;
                log.info("Beta mode detected from filename - launcher will download beta client");
            }
        } catch (URISyntaxException e) {
            log.warn("Could not determine JAR filename, defaulting to production mode", e);
        }
    }

    /**
     * Returns the launcher manifest URL (always production - there's only one launcher).
     */
    public String getLauncherManifestUrl() {
        return launcherVersionUrl != null ? launcherVersionUrl : LAUNCHER_MANIFEST;
    }

    public static String getBaseDir() {
        return BASE_DIR;
    }
}
