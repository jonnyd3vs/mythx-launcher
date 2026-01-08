package com.mythx.wrapper.service;

import com.mythx.wrapper.component.LauncherComponent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class JavaManager {
    private static final String BASE_DIR = System.getProperty("user.home") + "/.mythx/jdks/";
    private static final String JRE_WIN_URL = "https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u352-b08/OpenJDK8U-jre_x64_windows_hotspot_8u352b08.zip";
    private static final String JRE_MAC_AMD64_URL = "https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u352-b08/OpenJDK8U-jre_x64_mac_hotspot_8u352b08.tar.gz";
    private static final String JRE_MAC_ARM64_URL = "https://download.bell-sw.com/java/8u452+11/bellsoft-jre8u452+11-macos-aarch64-full.tar.gz";

    public static String manageJava() throws IOException {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);
        String jreURL, jreInstallPath, javaPath;

        if (os.contains("win")) {
            jreURL = JRE_WIN_URL;
            jreInstallPath = BASE_DIR + "windows_64/";
            javaPath = jreInstallPath + "jdk8u352-b08-jre/bin/java.exe";
        } else if (os.contains("mac") && arch.contains("x86")) {
            jreURL = JRE_MAC_AMD64_URL;
            jreInstallPath = BASE_DIR + "mac/";
            javaPath = jreInstallPath + "jdk8u352-b08-jre/Contents/Home/bin/java";
        } else if (os.contains("mac") && arch.contains("aarch64")) {
            jreURL = JRE_MAC_ARM64_URL;
            jreInstallPath = BASE_DIR + "mac/";
            javaPath = jreInstallPath + "jre8u452-full.jre/bin/java";
        } else {
            throw new UnsupportedOperationException("Unsupported platform: " + os + " " + arch);
        }

        Path javaBin = Paths.get(javaPath);
        if (Files.exists(javaBin)) {
            log.info("JRE already exists: " + javaPath);
            return javaPath;
        }

        Files.createDirectories(Paths.get(jreInstallPath));
        Path downloaded = downloadFile(jreURL, Paths.get(jreInstallPath, "jre_download"));

        if (jreURL.endsWith(".zip")) {
            extractZip(downloaded, Paths.get(jreInstallPath));
        } else if (jreURL.endsWith(".tar.gz")) {
            extractTarGz(downloaded, Paths.get(jreInstallPath));
        } else {
            throw new IllegalArgumentException("Unknown archive format: " + jreURL);
        }

        if (os.contains("mac")) {
            fixPermissions(javaBin);
        }

        return javaBin.toString();
    }


    private void showProgress(int progress) {
        LauncherComponent.LOADING_BAR.getComponent().load(progress);
    }

    public static Path downloadFile(String urlString, Path outputPath) throws IOException {
        LauncherComponent.LOADING_BAR_BACKGROUND.getComponent().setVisible(true);
        LauncherComponent.SPLASH_BACKGROUND.getComponent().setVisible(true);
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        int contentLength = conn.getContentLength();

        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(outputPath.toFile())) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalRead = 0;
            int lastPercent = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalRead += bytesRead;

                if (contentLength > 0) {
                    int percent = (int) (totalRead * 100 / contentLength);
                    if (percent != lastPercent) {
                        lastPercent = percent;
                        LauncherComponent.LOADING_BAR.getComponent().load(percent / 2); // Download is 0-50%
                    }
                }
            }
        }

        return outputPath;
    }

    public static void extractZip(Path zipFilePath, Path targetDir) throws IOException {
        long totalSize = Files.size(zipFilePath);
        long extractedBytes = 0;
        int lastPercent = 50;

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFilePath))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];

            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = targetDir.resolve(entry.getName()).normalize();
                if (!entryPath.startsWith(targetDir)) {
                    throw new IOException("Bad zip entry: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                    continue;
                }

                Files.createDirectories(entryPath.getParent());
                try (OutputStream out = Files.newOutputStream(entryPath)) {
                    int len;
                    while ((len = zis.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                        extractedBytes += len;

                        if (totalSize > 0) {
                            int percent = 50 + (int) (extractedBytes * 50 / totalSize);
                            if (percent != lastPercent) {
                                lastPercent = percent;
                                LauncherComponent.LOADING_BAR.getComponent().load(percent);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void extractTarGz(Path archivePath, Path targetDir) throws IOException {
        long totalSize = Files.size(archivePath);
        long extractedBytes = 0;
        int lastPercent = 50;

        try (InputStream fileIn = Files.newInputStream(archivePath);
             GZIPInputStream gzipIn = new GZIPInputStream(fileIn);
             TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {

            TarArchiveEntry entry;
            byte[] buffer = new byte[8192];

            while ((entry = tarIn.getNextTarEntry()) != null) {
                Path entryPath = targetDir.resolve(entry.getName()).normalize();
                if (!entryPath.startsWith(targetDir)) {
                    throw new IOException("Bad tar entry: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                    continue;
                }

                Files.createDirectories(entryPath.getParent());
                try (OutputStream out = Files.newOutputStream(entryPath)) {
                    int len;
                    while ((len = tarIn.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                        extractedBytes += len;

                        // crude but functional progress estimate
                        if (totalSize > 0) {
                            int percent = 50 + (int) (extractedBytes * 50 / totalSize);
                            if (percent != lastPercent) {
                                lastPercent = percent;
                                LauncherComponent.LOADING_BAR.getComponent().load(percent);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void fixPermissions(Path javaBin) throws IOException {
        if (!Files.isExecutable(javaBin)) {
            log.info("Making Java binary executable: " + javaBin);
            Files.setPosixFilePermissions(javaBin, PosixFilePermissions.fromString("rwxr-xr-x"));
        }
    }
}
