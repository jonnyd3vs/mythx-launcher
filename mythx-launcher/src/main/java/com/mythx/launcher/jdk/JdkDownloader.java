package com.mythx.launcher.jdk;

import com.mythx.launcher.LauncherSettings;
import com.mythx.launcher.web.config.HttpClientConfig;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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

        // Check if download is already in progress (prevent duplicate downloads)
        if (JdkDownloadWindow.isDownloadInProgress()) {
            LOGGER.warn("JDK download already in progress, waiting...");
            // Wait for existing download to complete
            while (JdkDownloadWindow.isDownloadInProgress()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            // After waiting, check if JDK was installed
            javaExe = new File(getJavaExecutable());
            if (javaExe.exists()) {
                cachedJdkPath = getJavaExecutable();
                LOGGER.info("JDK installed by other thread at: {}", cachedJdkPath);
                return cachedJdkPath;
            }
        }

        // Download JDK with progress window (use singleton)
        LOGGER.info("Downloading JDK 11 from {}", getJdkDownloadUrl());
        JdkDownloadWindow downloadWindow = JdkDownloadWindow.getInstance();
        downloadWindow.reset();
        downloadWindow.showWindow();

        try {
            downloadAndExtractJdk(downloadWindow);
            cachedJdkPath = getJavaExecutable();
            LOGGER.info("JDK 11 installed at: {}", cachedJdkPath);
            
            // Wait a moment before hiding window so user sees completion
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
            
        } catch (Exception e) {
            LOGGER.error("Failed to download JDK", e);
            downloadWindow.setError("Failed to download Java: " + e.getMessage());
            throw new RuntimeException("Failed to download JDK 11", e);
        } finally {
            downloadWindow.hideWindow();
        }

        return cachedJdkPath;
    }

    /**
     * Download and extract JDK ZIP
     */
    private void downloadAndExtractJdk(JdkDownloadWindow window) throws IOException {
        String downloadUrl = getJdkDownloadUrl();
        String zipPath = SETTINGS_DIR + getJdkZipName();

        // Create target directory for JDK (e.g., ~/.mythx/java-11-windows-64/)
        String targetDir = getJdkDir();
        new File(targetDir).mkdirs();
        LOGGER.info("Created JDK target directory: {}", targetDir);

        // Ensure settings dir exists for ZIP download
        new File(SETTINGS_DIR).mkdirs();

        // Download ZIP with cache-busting using Apache HttpClient (more reliable than HttpURLConnection)
        String cacheBustedUrl = downloadUrl + "?t=" + System.currentTimeMillis();
        LOGGER.info("Downloading JDK from: {}", cacheBustedUrl);
        
        CloseableHttpClient httpClient = HttpClientConfig.getHttpClient();
        HttpGet request = new HttpGet(cacheBustedUrl);
        request.setHeader("User-Agent", "MythX-Launcher");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            LOGGER.info("HTTP response code: {}", statusCode);
            
            if (statusCode != 200) {
                throw new IOException("Failed to download JDK: HTTP " + statusCode + " - " + response.getStatusLine().getReasonPhrase());
            }

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new IOException("Empty response from server");
            }

            long contentLength = entity.getContentLength();
            LOGGER.info("Content length: {} bytes", contentLength);

            window.setStatus("Downloading Java 11...");
            window.setTotalBytes(contentLength > 0 ? contentLength : 70_000_000); // Estimate ~70MB if unknown

            File zipFile = new File(zipPath);
            try (InputStream in = entity.getContent();
                 FileOutputStream out = new FileOutputStream(zipFile)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalRead = 0;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                    window.setBytesDownloaded(totalRead);
                }
                LOGGER.info("Downloaded {} bytes to {}", totalRead, zipPath);
            }

            // Verify ZIP file exists and has content
            if (!zipFile.exists() || zipFile.length() == 0) {
                throw new IOException("ZIP file is empty or missing: " + zipPath);
            }
            LOGGER.info("ZIP file size: {} bytes", zipFile.length());

            // Extract ZIP to target directory (not SETTINGS_DIR!)
            window.setStatus("Extracting Java 11...");
            LOGGER.info("Extracting JDK to: {}", targetDir);
            extractZip(zipPath, targetDir);

            // Verify extraction succeeded
            File javaExe = new File(getJavaExecutable());
            LOGGER.info("Checking for java executable at: {}", javaExe.getAbsolutePath());
            if (!javaExe.exists()) {
                LOGGER.error("Extraction failed - java executable not found!");
                // List what was extracted for debugging
                File[] extracted = new File(targetDir).listFiles();
                if (extracted != null) {
                    LOGGER.error("Contents of {}: ", targetDir);
                    for (File f : extracted) {
                        LOGGER.error("  - {}", f.getName());
                    }
                }
                throw new IOException("Extraction failed - java.exe not found at: " + javaExe.getAbsolutePath());
            }

            // Delete ZIP after successful extraction
            if (zipFile.delete()) {
                LOGGER.info("Deleted temp ZIP file");
            } else {
                LOGGER.warn("Could not delete temp ZIP file: {}", zipPath);
            }
            window.setStatus("Java 11 installed successfully");
        }
    }

    /**
     * Extract ZIP file to destination directory
     */
    private void extractZip(String zipPath, String destDir) throws IOException {
        File destDirFile = new File(destDir);
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            int filesExtracted = 0;

            while ((entry = zis.getNextEntry()) != null) {
                // Use proper path construction with Zip Slip protection
                File destFile = newFile(destDirFile, entry);

                if (entry.isDirectory()) {
                    if (!destFile.isDirectory() && !destFile.mkdirs()) {
                        throw new IOException("Failed to create directory: " + destFile);
                    }
                } else {
                    // Ensure parent directory exists
                    File parent = destFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create parent directory: " + parent);
                    }

                    try (FileOutputStream fos = new FileOutputStream(destFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }

                    // Set executable permissions for bin/ files on Unix-like systems
                    if (entry.getName().contains("/bin/") || entry.getName().contains("\\bin\\")) {
                        destFile.setExecutable(true, false);
                    }
                    filesExtracted++;
                }
                zis.closeEntry();
            }
            LOGGER.info("Extracted {} files to {}", filesExtracted, destDir);
        }
    }

    /**
     * Safely create destination file with Zip Slip vulnerability protection
     */
    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator) && !destFilePath.equals(destDirPath)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
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
