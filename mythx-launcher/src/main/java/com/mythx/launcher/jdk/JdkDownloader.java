package com.mythx.launcher.jdk;

import com.mythx.launcher.LauncherSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Handles JDK 11 download and installation for client execution
 * Downloads from Google Cloud Storage and extracts to .mythx directory
 */
public class JdkDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdkDownloader.class);

    private static final String JDK_BASE_URL = "https://storage.googleapis.com/flippers/jdk11/";
    private static final String SETTINGS_DIR = LauncherSettings.BASE_DIR;

    // Cached JDK path after first check
    private static String cachedJdkPath = null;

    /**
     * Get the JDK directory name based on OS and architecture
     */
    public String getJdkDirName() {
        String os = getOs();
        String arch = getArch();
        return "java-11-" + os + "-" + arch;
    }

    /**
     * Get the full path to the JDK directory
     */
    public String getJdkDir() {
        return SETTINGS_DIR + getJdkDirName() + File.separator;
    }

    /**
     * Get the JDK ZIP filename for download
     */
    public String getJdkZipName() {
        return getJdkDirName() + ".zip";
    }

    /**
     * Get the download URL for the JDK
     */
    public String getJdkDownloadUrl() {
        return JDK_BASE_URL + getJdkZipName();
    }

    /**
     * Check if JDK needs to be downloaded
     */
    public boolean needsJdkDownload() {
        // First check if system Java is version 11+
        if (isSystemJavaValid()) {
            LOGGER.info("System Java is version 11+, no download needed");
            return false;
        }

        // Check if JDK already exists
        File jdkDir = new File(getJdkDir());
        File javaExe = new File(getJavaExecutable());

        if (jdkDir.exists() && javaExe.exists()) {
            LOGGER.info("JDK 11 already exists at {}", getJdkDir());
            return false;
        }

        return true;
    }

    /**
     * Check if system Java is version 11 or higher
     */
    private boolean isSystemJavaValid() {
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-version");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            process.waitFor();

            if (line != null) {
                // Parse version from output like: java version "11.0.12" or openjdk version "17.0.1"
                int majorVersion = parseJavaMajorVersion(line);
                LOGGER.info("System Java major version: {}", majorVersion);
                return majorVersion >= 11;
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to check system Java version", e);
        }
        return false;
    }

    /**
     * Parse major version from java -version output
     */
    private int parseJavaMajorVersion(String versionLine) {
        try {
            // Look for version number in quotes
            int startQuote = versionLine.indexOf('"');
            int endQuote = versionLine.indexOf('"', startQuote + 1);
            if (startQuote >= 0 && endQuote > startQuote) {
                String version = versionLine.substring(startQuote + 1, endQuote);

                // Handle 1.8.x format (Java 8 and earlier)
                if (version.startsWith("1.")) {
                    return Integer.parseInt(version.substring(2, 3));
                }

                // Handle 11.x.x format (Java 9+)
                int dotIndex = version.indexOf('.');
                if (dotIndex > 0) {
                    return Integer.parseInt(version.substring(0, dotIndex));
                }
                return Integer.parseInt(version);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to parse Java version from: {}", versionLine);
        }
        return 0;
    }

    /**
     * Ensure JDK 11 is installed and return the path to java executable
     * Shows download window if download is needed
     */
    public String ensureJdk11Installed() {
        // Return cached path if available
        if (cachedJdkPath != null) {
            return cachedJdkPath;
        }

        // Check system Java first
        if (isSystemJavaValid()) {
            cachedJdkPath = "java";
            return cachedJdkPath;
        }

        // Check if JDK already downloaded
        File javaExe = new File(getJavaExecutable());
        if (javaExe.exists()) {
            cachedJdkPath = getJavaExecutable();
            LOGGER.info("Using existing JDK at: {}", cachedJdkPath);
            return cachedJdkPath;
        }

        // Download JDK with progress window
        LOGGER.info("Downloading JDK 11 from {}", getJdkDownloadUrl());
        JdkDownloadWindow downloadWindow = new JdkDownloadWindow();
        downloadWindow.setVisible(true);

        try {
            downloadAndExtractJdk(downloadWindow);
            cachedJdkPath = getJavaExecutable();
            LOGGER.info("JDK 11 installed at: {}", cachedJdkPath);
        } catch (Exception e) {
            LOGGER.error("Failed to download JDK", e);
            downloadWindow.setError("Failed to download Java: " + e.getMessage());
            throw new RuntimeException("Failed to download JDK 11", e);
        } finally {
            downloadWindow.dispose();
        }

        return cachedJdkPath;
    }

    /**
     * Download and extract JDK ZIP
     */
    private void downloadAndExtractJdk(JdkDownloadWindow window) throws IOException {
        String downloadUrl = getJdkDownloadUrl();
        String zipPath = SETTINGS_DIR + getJdkZipName();

        // Ensure directory exists
        new File(SETTINGS_DIR).mkdirs();

        // Download ZIP
        URL url = new URL(downloadUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "MythX-Launcher");

        int contentLength = conn.getContentLength();
        if (contentLength == -1) {
            // Try GCS-specific header
            String gcsLength = conn.getHeaderField("x-goog-stored-content-length");
            if (gcsLength != null) {
                contentLength = Integer.parseInt(gcsLength);
            }
        }

        window.setStatus("Downloading Java 11...");
        window.setTotalBytes(contentLength);

        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(zipPath)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalRead = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
                window.setBytesDownloaded(totalRead);
            }
        }

        // Extract ZIP
        window.setStatus("Extracting Java 11...");
        extractZip(zipPath, SETTINGS_DIR);

        // Delete ZIP after extraction
        new File(zipPath).delete();
        window.setStatus("Java 11 installed successfully");
    }

    /**
     * Extract ZIP file to destination directory
     */
    private void extractZip(String zipPath, String destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];

            while ((entry = zis.getNextEntry()) != null) {
                File destFile = new File(destDir + entry.getName());

                if (entry.isDirectory()) {
                    destFile.mkdirs();
                } else {
                    // Ensure parent directory exists
                    destFile.getParentFile().mkdirs();

                    try (FileOutputStream fos = new FileOutputStream(destFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    /**
     * Get the path to the java executable
     */
    public String getJavaExecutable() {
        String os = getOs();
        if ("windows".equals(os)) {
            return getJdkDir() + "bin" + File.separator + "java.exe";
        } else {
            return getJdkDir() + "bin" + File.separator + "java";
        }
    }

    /**
     * Get the current operating system identifier
     */
    private String getOs() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "windows";
        } else if (osName.contains("mac")) {
            return "macos";
        } else {
            return "linux";
        }
    }

    /**
     * Get the current architecture identifier
     */
    private String getArch() {
        String arch = System.getProperty("os.arch").toLowerCase();
        if (arch.contains("aarch64") || arch.contains("arm64")) {
            return "arm64";
        } else if (arch.contains("64")) {
            return "64";
        } else {
            return "32";
        }
    }
}
