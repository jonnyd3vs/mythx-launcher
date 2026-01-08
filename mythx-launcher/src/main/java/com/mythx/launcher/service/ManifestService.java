package com.mythx.launcher.service;

import com.mythx.launcher.LauncherSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Service to fetch client download URL from manifest.json
 */
public class ManifestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManifestService.class);
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;

    private String clientUrl;
    private String clientFilename;
    private String version;

    /**
     * Fetches the manifest and extracts client download info with retry logic
     * @return true if successful, false otherwise
     */
    public boolean fetchManifest() {
        String manifestUrl = LauncherSettings.getClientManifestUrl();
        LOGGER.info("Fetching manifest from: {}", manifestUrl);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                if (attempt > 1) {
                    LOGGER.info("Retry attempt {} of {}", attempt, MAX_RETRIES);
                    Thread.sleep(RETRY_DELAY_MS);
                }

                URL url = new URL(manifestUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "MythX-Launcher");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    LOGGER.warn("Failed to fetch manifest. Response code: {}", responseCode);
                    continue;
                }

                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                // Simple JSON parsing (avoiding extra dependencies)
                String json = response.toString();
                clientUrl = extractJsonValue(json, "clientUrl");
                version = extractJsonValue(json, "version");

                if (clientUrl != null) {
                    // Extract filename from URL
                    clientFilename = clientUrl.substring(clientUrl.lastIndexOf('/') + 1);
                    LOGGER.info("Manifest loaded - Version: {}, Client URL: {}", version, clientUrl);
                    return true;
                }

            } catch (Exception e) {
                LOGGER.error("Error fetching manifest (attempt {})", attempt, e);
            }
        }

        LOGGER.error("Failed to fetch manifest after {} attempts", MAX_RETRIES);
        return false;
    }

    /**
     * Simple JSON value extractor for a given key
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;

        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return null;

        int valueStart = json.indexOf("\"", colonIndex) + 1;
        int valueEnd = json.indexOf("\"", valueStart);

        if (valueStart > 0 && valueEnd > valueStart) {
            return json.substring(valueStart, valueEnd);
        }
        return null;
    }

    public String getClientUrl() {
        return clientUrl;
    }

    public String getClientFilename() {
        return clientFilename;
    }

    public String getVersion() {
        return version;
    }
}
