package com.mythx.launcher.components.impl;

import com.mythx.launcher.Launch;
import com.mythx.launcher.LauncherSettings;
import com.mythx.launcher.components.CreativeComponent;
import com.mythx.launcher.config.Config;
import com.mythx.launcher.download.Download;
import com.mythx.launcher.dto.ClientVersion;
import com.mythx.launcher.jdk.JdkDownloader;
import com.mythx.launcher.service.ClientVersionService;
import com.mythx.launcher.utility.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Play Now button that handles JDK verification and client version checking
 * @author Jonny
 */
public class PlayNowButton extends CreativeComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayNowButton.class);
    private String serverName;

    public PlayNowButton() {
        serverName = "MythX";

        setIcon(new ImageIcon(getClass().getResource("/Play-ACTIVE.png")));
        setBounds(getRectangle());

        addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent evt) {
                checkJdkAndLaunch();
            }

            public void mouseEntered(MouseEvent evt) {
                setIcon(new ImageIcon(getClass().getResource("/Play-ACTIVE-HOVER.png")));
            }

            public void mouseExited(MouseEvent evt) {
                setIcon(new ImageIcon(getClass().getResource("/Play-ACTIVE.png")));
            }
        });
    }

    public Rectangle getRectangle() {
        return new Rectangle(615, 403, 248, 64);
    }

    /**
     * Check JDK installation and then launch the client
     */
    private void checkJdkAndLaunch() {
        // Run in background thread to not block UI
        new Thread(() -> {
            try {
                // First ensure JDK 11 is available
                JdkDownloader downloader = new JdkDownloader();
                if (downloader.needsJdkDownload()) {
                    LOGGER.info("JDK 11 download required");
                    downloader.ensureJdk11Installed();
                }

                // Then check version and launch
                launch();
            } catch (Exception e) {
                LOGGER.error("Failed to check JDK or launch client", e);
            }
        }).start();
    }

    /**
     * Check client version and launch
     */
    private void launch() {
        if (Launch.getDownload() != null) {
            LOGGER.info("Download already in progress");
            return;
        }

        // Ensure save directory exists before any file checks
        // Prevents "Access denied" errors on Windows when checking non-existent paths
        File saveDir = new File(LauncherSettings.SAVE_DIR);
        if (!saveDir.exists()) {
            boolean created = saveDir.mkdirs();
            LOGGER.info("Created save directory in launch(): {} (success: {})", saveDir.getAbsolutePath(), created);
        }

        String clientUrl = LauncherSettings.getClientDownloadUrl();
        String serverName = LauncherSettings.getServerName();
        String clientFilename = LauncherSettings.getClientFilename();

        LOGGER.info("Beta mode: {}", LauncherSettings.BETA_MODE);
        LOGGER.info("Server name: {}, Client URL: {}", serverName, clientUrl);

        // Reload local versions from file FRESH before comparing
        // This ensures manual edits to client-version.json are detected
        ClientVersionService.loadLocalVersions();
        LOGGER.info("Reloaded local versions from file");

        // Fetch remote version from ECM API
        Integer remoteVersion = fetchRemoteVersion();
        if (remoteVersion == null) {
            LOGGER.warn("Failed to fetch remote version, checking if client exists");
            // If we can't get remote version, just try to launch existing client
            File clientFile = new File(LauncherSettings.SAVE_DIR + clientFilename);
            if (clientFile.exists()) {
                Utilities.launchClient(clientFilename);
                return;
            }
            // No existing client, try to download anyway
            remoteVersion = 0;
        }

        // Get local version
        Integer localVersion = Config.get().getClientVersions().get(serverName);
        LOGGER.info("Local version: {}, Remote version: {}", localVersion, remoteVersion);

        // Check if we need to download
        File clientFile = new File(LauncherSettings.SAVE_DIR + clientFilename);
        boolean needsDownload = !clientFile.exists() ||
                localVersion == null ||
                localVersion < remoteVersion;

        if (needsDownload) {
            LOGGER.info("Downloading new client version (file exists: {}, local version: {}, remote version: {})", 
                    clientFile.exists(), localVersion, remoteVersion);
            final int versionToSave = remoteVersion;
            final String finalServerName = serverName;

            // Start download - version is saved AFTER successful download, not before
            Download download = Launch.resetDownload(clientUrl, clientFilename);
            download.setOnComplete(() -> {
                // Save version only after successful download
                Config.get().getClientVersions().put(finalServerName, versionToSave);
                ClientVersionService.saveVersion();
                LOGGER.info("Download complete, saved version {} for {}", versionToSave, finalServerName);
            });
            download.start();
        } else {
            LOGGER.info("Client is up to date, launching existing (file: {})", clientFile.getAbsolutePath());
            Utilities.launchClient(clientFilename);
        }
    }

    /**
     * Fetch the remote client version from ECM API
     * @return version number or null if failed
     */
    private Integer fetchRemoteVersion() {
        try {
            String versionUrl = Config.get().getClientVersionUrl() + "?projectId=" + Config.get().getProjectId();
            LOGGER.info("Fetching version from: {}", versionUrl);

            URL url = new URL(versionUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "MythX-Launcher");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                LOGGER.warn("Failed to fetch version. Response code: {}", responseCode);
                return null;
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            // Parse version from JSON response
            String json = response.toString();
            LOGGER.debug("Version API response: {}", json);

            Integer version = parseVersionFromJson(json);
            LOGGER.info("Remote version: {}", version);
            return version;

        } catch (Exception e) {
            LOGGER.error("Error fetching remote version", e);
            return null;
        }
    }

    /**
     * Parse version number from ECM API JSON response
     * Expected format: {"value": "42"}
     */
    private Integer parseVersionFromJson(String json) {
        try {
            String searchKey = "\"value\"";
            int keyIndex = json.indexOf(searchKey);
            if (keyIndex == -1) return null;

            int colonIndex = json.indexOf(":", keyIndex);
            if (colonIndex == -1) return null;

            // Find the quoted value
            int quoteStart = json.indexOf("\"", colonIndex + 1);
            if (quoteStart == -1) return null;

            int quoteEnd = json.indexOf("\"", quoteStart + 1);
            if (quoteEnd == -1) return null;

            String valueStr = json.substring(quoteStart + 1, quoteEnd);
            return Integer.parseInt(valueStr);
        } catch (Exception e) {
            LOGGER.error("Failed to parse version from JSON: {}", json, e);
        }
        return null;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}
