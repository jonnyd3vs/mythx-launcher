package com.mythx.launcher;

import com.mythx.launcher.cache.CacheDownloader;
import com.mythx.launcher.download.Download;
import com.mythx.launcher.fonts.LauncherFont;
import com.mythx.launcher.handler.GlobalExceptionHandler;
import com.mythx.launcher.service.ClientVersionService;
import com.mythx.launcher.utility.SSLTool;
import com.mythx.launcher.utility.Utilities;
import com.mythx.launcher.web.update.UpdateGrabber;
import com.mythx.launcher.web.video.VideoGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

public class Launch {
    private static final Logger LOGGER = LoggerFactory.getLogger(Launch.class);

    public static Download download;
    private static LauncherFrame launcherFrame;
    private static LauncherSplash launcherSplash;
    private static VideoGrabber VIDEO_GRABBER;
    private static UpdateGrabber UPDATE_GRABBER;

    public static boolean USE_SECONDARY_WEBSITE = false;

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());

        // Parse command line arguments
        parseArguments(args);

        LOGGER.info("Starting MythX Launcher");
        LOGGER.info("Beta mode: {}", LauncherSettings.BETA_MODE);

        // Create necessary directories
        createDirectories();

        // Load local client versions
        ClientVersionService.loadLocalVersions();

        launcherSplash = new LauncherSplash();
        launcherSplash.start();

        try {
            VIDEO_GRABBER = new VideoGrabber();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize video grabber", e);
        }

        try {
            UPDATE_GRABBER = new UpdateGrabber();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize update grabber", e);
        }

        launcherFrame = new LauncherFrame();

        // Download cache if needed
        try {
            CacheDownloader.init();
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed URL in cache downloader", e);
        } catch (IOException e) {
            LOGGER.error("IO error in cache downloader", e);
        }

        start();
    }

    private static void parseArguments(String[] args) {
        for (String arg : args) {
            if ("--beta".equalsIgnoreCase(arg) || "-beta".equalsIgnoreCase(arg)) {
                LauncherSettings.BETA_MODE = true;
                LOGGER.info("Beta mode enabled via command line argument");
            }
        }
    }

    private static void createDirectories() {
        // Create base directory
        File baseDir = new File(LauncherSettings.BASE_DIR);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }

        // Create cache directory
        File cacheDir = new File(Utilities.getCacheDirectory());
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        // Create client directory
        File clientDir = new File(LauncherSettings.SAVE_DIR);
        if (!clientDir.exists()) {
            clientDir.mkdirs();
        }

        // Create logs directory
        File logsDir = new File(LauncherSettings.BASE_DIR + "logs" + File.separator + "launcher");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
    }

    public static void start() {
        SSLTool.disableCertificateValidation();
        LauncherFont.loadFonts();

        launcherFrame = new LauncherFrame();
        launcherFrame.start();

        launcherSplash.dispose();
    }

    public static Download getDownload() {
        return download;
    }

    public static Download resetDownload(String downloadUrl, String serverName) {
        return download = new Download(downloadUrl, serverName);
    }

    public static void clearDownload() {
        download = null;
    }

    public static LauncherFrame getLauncherFrame() {
        return launcherFrame;
    }
}
