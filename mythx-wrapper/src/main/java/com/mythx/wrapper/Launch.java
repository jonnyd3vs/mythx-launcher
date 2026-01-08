package com.mythx.wrapper;

import com.mythx.wrapper.config.Config;
import com.mythx.wrapper.controller.ErrorController;
import com.mythx.wrapper.handler.GlobalExceptionHandler;
import com.mythx.wrapper.service.JavaManager;
import com.mythx.wrapper.service.RunJarService;
import com.mythx.wrapper.service.UpdateService;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class Launch {
    private static final String BASE_DIR = "C:" + File.separator + ".mythx" + File.separator;
    private static LauncherSplash launcherSplash;

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());

        // Ensure logs directory exists
        createLogsDirectory();

        // Detect beta mode from wrapper filename
        Config.get().detectBetaMode();
        log.info("Beta mode: {}", Config.get().isBetaMode());

        PropertiesReader.loadProperties(); // load properties from application.properties file

        launcherSplash = new LauncherSplash();
        launcherSplash.start();

        try {
            UpdateService updateService = new UpdateService();
            updateService.init(); // load local and remote version, check updates and download jar if necessary
            RunJarService.runJar(Config.get().getLocalVersion().getFilename() + ".jar"); // run launcher
        } catch (Exception ex) {
            log.error("Failed to start launcher", ex);

            // Send error to API
            ErrorController.sendErrorAsync("unknown", ex);

            // Show error message on splash
            launcherSplash.setProgressText("Error: Failed to start. Check logs.");
        }
    }

    private static void createLogsDirectory() {
        File logsDir = new File(BASE_DIR + "logs" + File.separator + "wrapper");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
        File launcherLogsDir = new File(BASE_DIR + "logs" + File.separator + "launcher");
        if (!launcherLogsDir.exists()) {
            launcherLogsDir.mkdirs();
        }
    }

    public static LauncherSplash getSplash() {
        return launcherSplash;
    }
}
