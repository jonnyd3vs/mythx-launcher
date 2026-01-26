package com.mythx.launcher.service;

import com.mythx.launcher.LauncherSettings;
import com.mythx.launcher.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Service to manage local client version storage
 * Stores versions in C:\.mythx\client-version\client-version.json
 * Format: {"mythx": 42, "Beta_mythx": 43}
 */
public class ClientVersionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientVersionService.class);
    private static final String VERSION_DIR = LauncherSettings.BASE_DIR + "client-version" + File.separator;
    private static final String VERSION_FILE = VERSION_DIR + "client-version.json";

    /**
     * Load local versions from client-version.json into Config
     */
    public static void loadLocalVersions() {
        File versionFile = new File(VERSION_FILE);
        if (!versionFile.exists()) {
            LOGGER.info("No local version file found at {}", VERSION_FILE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(versionFile))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            String json = sb.toString().trim();
            Map<String, Integer> versions = parseVersionsJson(json);
            Config.get().setClientVersions(versions);
            LOGGER.info("Loaded local versions: {}", versions);
        } catch (Exception e) {
            LOGGER.error("Failed to load local versions", e);
        }
    }

    /**
     * Save current versions from Config to client-version.json
     */
    public static void saveVersion() {
        // Ensure directory exists
        File versionDir = new File(VERSION_DIR);
        if (!versionDir.exists()) {
            versionDir.mkdirs();
        }

        Map<String, Integer> versions = Config.get().getClientVersions();
        String json = buildVersionsJson(versions);

        try (PrintWriter writer = new PrintWriter(new FileWriter(VERSION_FILE))) {
            writer.print(json);
            LOGGER.info("Saved versions to {}: {}", VERSION_FILE, json);
        } catch (Exception e) {
            LOGGER.error("Failed to save versions", e);
        }
    }

    /**
     * Parse JSON like {"mythx": 42, "Beta_mythx": 43} into a Map
     */
    private static Map<String, Integer> parseVersionsJson(String json) {
        Map<String, Integer> versions = new HashMap<>();

        // Remove braces and whitespace
        json = json.trim();
        if (json.startsWith("{")) {
            json = json.substring(1);
        }
        if (json.endsWith("}")) {
            json = json.substring(0, json.length() - 1);
        }

        if (json.trim().isEmpty()) {
            return versions;
        }

        // Split by comma and parse each key-value pair
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replace("\"", "");
                String value = keyValue[1].trim();
                try {
                    versions.put(key, Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    LOGGER.warn("Failed to parse version number for key {}: {}", key, value);
                }
            }
        }

        return versions;
    }

    /**
     * Build JSON string from versions map
     */
    private static String buildVersionsJson(Map<String, Integer> versions) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : versions.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append("\"").append(entry.getKey()).append("\": ").append(entry.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
