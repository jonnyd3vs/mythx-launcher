package com.mythx.wrapper.service;

import com.mythx.wrapper.config.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RunJarService {

    private static final String BASE_DIR = System.getProperty("user.home") + File.separator + ".mythx" + File.separator;
    private static final String LAUNCHER_DIR = BASE_DIR + "launcher" + File.separator;

    /**
     * Runs the specified JAR file in a new process.
     *
     * @param jarFileName the name of the JAR file to run (must be located in LAUNCHER_DIR)
     * @throws IllegalArgumentException if the JAR file does not exist
     */
    public static void runJar(String jarFileName) {
        log.info("Starting launcher JAR");
        File jarFile = new File(LAUNCHER_DIR + jarFileName);

        if (!jarFile.exists() || !jarFile.isFile()) {
            throw new IllegalArgumentException("JAR file not found: " + jarFile.getAbsolutePath());
        }

        boolean betaMode = Config.get().isBetaMode();
        log.info("Launching with beta mode: {}", betaMode);

        // Build the command: "java -jar mythx-launcher.jar [--beta]"
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(jarFile.getAbsolutePath());

        // Pass beta flag to launcher if in beta mode
        if (betaMode) {
            command.add("--beta");
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        // Set the working directory
        processBuilder.directory(jarFile.getParentFile());

        // Register a shutdown hook for cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Wrapper is shutting down after launching the JAR...");
        }));

        // Start the process
        try {
            processBuilder.inheritIO().start(); // Use inheritIO to forward output to console

            // Terminate the wrapper after starting the JAR
            System.exit(0);
        } catch (IOException e) {
            log.warn("Could not start jar", e);
        }
    }
}
