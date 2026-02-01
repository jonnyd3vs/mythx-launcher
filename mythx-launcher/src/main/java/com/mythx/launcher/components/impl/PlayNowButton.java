package com.mythx.launcher.components.impl;

import com.mythx.launcher.Launch;
import com.mythx.launcher.LauncherSettings;
import com.mythx.launcher.components.CreativeComponent;
import com.mythx.launcher.config.Config;
import com.mythx.launcher.download.Download;
import com.mythx.launcher.dto.ClientVersion;
import com.mythx.launcher.jdk.JdkDownloader;
import com.mythx.launcher.jdk.JdkDownloadWindow;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Play Now button that handles JDK verification and client version checking
 * @author Jonny
 */
public class PlayNowButton extends CreativeComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayNowButton.class);
    private String serverName;

    // Prevent spam-clicking from launching multiple clients
    private static volatile boolean isLaunching = false;

    // Background pre-fetching of client version to reduce Play Now wait time
    private static volatile Integer cachedRemoteVersion = null;
    private static volatile boolean versionFetchInProgress = false;
    private static volatile boolean versionFetchComplete = false;
    private static volatile boolean launchPending = false;

    // Periodic version refresh scheduler (refreshes every 15 seconds)
    private static ScheduledExecutorService versionRefreshScheduler = null;

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
        LOGGER.info("=== PLAY NOW BUTTON CLICKED ===");
        
        // Check if JDK download is in progress
        if (JdkDownloadWindow.isDownloadInProgress()) {
            LOGGER.warn("!!! PLAY NOW BLOCKED - JDK download in progress !!!");
            LOGGER.warn("User must wait for JDK download to complete");
            return;
        }

        // Check if already launching to prevent spam-clicking
        if (isLaunching) {
            LOGGER.warn("!!! LAUNCH BLOCKED - Already launching a client !!!");
            LOGGER.warn("Ignoring Play Now click to prevent multiple clients");
            return;
        }

        // Set launching flag immediately to prevent double-clicks during JDK download
        isLaunching = true;
        LOGGER.info("Set isLaunching flag to true");

        // Run in background thread to not block UI
        new Thread(() -> {
            try {
                // First ensure JDK 11 is available
                JdkDownloader downloader = new JdkDownloader();
                if (downloader.needsJdkDownload()) {
                    LOGGER.info("JDK 11 download required");
                    downloader.ensureJdk11Installed();
                }

                // Reset flag before calling launch() since launch() will handle its own state
                isLaunching = false;
                LOGGER.info("Reset isLaunching flag before launch()");

                // Then check version and launch
                launch();
            } catch (Exception e) {
                LOGGER.error("Failed to check JDK or launch client", e);
                isLaunching = false; // Reset flag on error
                LOGGER.info("Reset isLaunching flag due to error");
            }
        }, "PlayNowJdkCheckThread").start();
    }

    /**
     * Check client version and launch
     */
    private void launch() {
        // Check if a download is in progress
        if (Launch.getDownload() != null) {
            LOGGER.warn("!!! LAUNCH BLOCKED - Download already in progress !!!");
            LOGGER.warn("Cannot start new client launch while download is active");
            return;
        }

        // Set launching flag to prevent concurrent launches
        isLaunching = true;
        LOGGER.info("Set isLaunching flag to true (launch entry)");

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

        // Check if we have cached version data available from background fetch
        Integer remoteVersion;
        if (cachedRemoteVersion != null) {
            if (versionFetchComplete) {
                LOGGER.info("*** FAST PATH - Using pre-fetched version data ***");
                LOGGER.info("Saved API request time by using background cache");
            } else {
                LOGGER.info("*** FAST PATH - Using old cached version (refresh in progress) ***");
                LOGGER.info("Refresh is happening in background, using previous version data");
            }
            remoteVersion = cachedRemoteVersion;
        } else if (versionFetchInProgress) {
            // If version fetch is still in progress AND we have NO cached data, queue this launch
            LOGGER.info("*** PENDING LAUNCH - First version fetch still in progress, no cached data available ***");
            LOGGER.info("Queuing launch to execute automatically when background fetch completes");
            launchPending = true;
            return;
        } else {
            // Fallback: No cached data available, fetch inline
            LOGGER.info("No cached version data available - fetching inline");
            remoteVersion = fetchRemoteVersion();
        }

        executeLaunchWithVersion(remoteVersion, clientUrl, serverName, clientFilename);
    }

    /**
     * Execute the launch with a given version (either cached or freshly fetched)
     */
    private void executeLaunchWithVersion(Integer remoteVersion, String clientUrl, String serverName, String clientFilename) {
        try {
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

                // Clear any existing download before starting new one
                Launch.clearDownload();

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
        } finally {
            // Always reset the flag after launch attempt completes
            // Use a delay to allow the client process to fully start
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Wait 2 seconds before allowing another launch
                    isLaunching = false;
                    LOGGER.info("Reset isLaunching flag to false after 2s delay");
                } catch (InterruptedException e) {
                    LOGGER.error("Interrupted while waiting to reset isLaunching flag", e);
                    isLaunching = false;
                }
            }, "LaunchFlagResetThread").start();
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

    /**
     * Starts background pre-fetching of client version to reduce wait time when Play Now is clicked.
     * Should be called during launcher initialization.
     */
    public static void startBackgroundVersionFetch() {
        // Allow re-entry for periodic refreshes
        if (versionFetchInProgress) {
            LOGGER.debug("Version fetch already in progress, skipping");
            return;
        }

        versionFetchInProgress = true;
        versionFetchComplete = false; // Reset to indicate fresh fetch
        LOGGER.debug("=== STARTING BACKGROUND VERSION FETCH ===");
        LOGGER.debug("Pre-fetching client version data to optimize Play Now response time");

        new Thread(() -> {
            try {
                String versionUrl = Config.get().getClientVersionUrl() + "?projectId=" + Config.get().getProjectId();
                LOGGER.debug("Background fetch - connecting to: {}", versionUrl);

                long startTime = System.currentTimeMillis();

                // Force TLS 1.2 for older Java versions (Java 8 may default to older TLS)
                System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");
                
                URL url = new URL(versionUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "MythX-Launcher");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    StringBuilder response = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    }

                    // Parse version from JSON response
                    String json = response.toString();
                    Integer version = parseVersionFromJsonStatic(json);
                    if (version != null) {
                        cachedRemoteVersion = version;
                        long duration = System.currentTimeMillis() - startTime;
                        LOGGER.debug("Background fetch complete in {}ms - Remote version: {}", duration, cachedRemoteVersion);
                        versionFetchComplete = true;

                        // If user clicked Play Now while we were fetching, execute the pending launch
                        if (launchPending) {
                            LOGGER.info("=== EXECUTING PENDING LAUNCH ===");
                            LOGGER.info("User clicked Play Now during background fetch - executing now");
                            launchPending = false;

                            // Execute the launch on the Swing EDT
                            SwingUtilities.invokeLater(() -> {
                                String clientUrl = LauncherSettings.getClientDownloadUrl();
                                String serverName = LauncherSettings.getServerName();
                                String clientFilename = LauncherSettings.getClientFilename();
                                new PlayNowButton().executeLaunchWithVersion(cachedRemoteVersion, clientUrl, serverName, clientFilename);
                            });
                        }
                    } else {
                        LOGGER.warn("Background fetch - failed to parse version from response");
                        versionFetchComplete = true;
                    }
                } else {
                    LOGGER.warn("Background fetch failed with response code: {}", responseCode);
                    versionFetchComplete = true;
                }
            } catch (Exception e) {
                LOGGER.error("Background version fetch failed", e);
                versionFetchComplete = true; // Mark as complete even on error so launch can proceed
            } finally {
                versionFetchInProgress = false;
                LOGGER.debug("Background version fetch thread completed");
            }
        }, "VersionPreFetchThread").start();
    }

    /**
     * Parse version from JSON (static version for background thread)
     */
    private static Integer parseVersionFromJsonStatic(String json) {
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

    /**
     * Starts periodic background version refresh every 15 seconds.
     * This ensures the launcher always has up-to-date version information.
     * Should be called once during launcher initialization.
     */
    public static void startPeriodicVersionRefresh() {
        if (versionRefreshScheduler != null) {
            LOGGER.warn("Periodic version refresh already started, ignoring");
            return;
        }

        LOGGER.info("=== STARTING PERIODIC VERSION REFRESH ===");
        LOGGER.info("Client version will be refreshed every 15 seconds");

        versionRefreshScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "VersionRefreshScheduler");
            t.setDaemon(true); // Daemon thread won't prevent JVM shutdown
            return t;
        });

        // Schedule periodic refresh every 15 seconds, starting after initial 15 second delay
        versionRefreshScheduler.scheduleAtFixedRate(() -> {
            try {
                LOGGER.debug("Periodic version refresh triggered");
                startBackgroundVersionFetch();
            } catch (Exception e) {
                LOGGER.error("Error during periodic version refresh", e);
            }
        }, 15, 15, TimeUnit.SECONDS);

        LOGGER.info("Periodic version refresh scheduler started successfully");
    }

    /**
     * Stops the periodic version refresh scheduler.
     * Called during launcher shutdown (if needed).
     */
    public static void stopPeriodicVersionRefresh() {
        if (versionRefreshScheduler != null) {
            LOGGER.info("Stopping periodic version refresh");
            versionRefreshScheduler.shutdown();
            versionRefreshScheduler = null;
        }
    }

    /**
     * Resets the launching flag to allow a new launch.
     * Called when user switches modes (e.g., Ctrl+B for beta mode).
     */
    public static void resetLaunchingFlag() {
        if (isLaunching) {
            LOGGER.info("Resetting isLaunching flag (was true, now false)");
            isLaunching = false;
        }
    }

    /**
     * Get the cached remote version (for testing/debugging)
     */
    public static Integer getCachedRemoteVersion() {
        return cachedRemoteVersion;
    }
}
