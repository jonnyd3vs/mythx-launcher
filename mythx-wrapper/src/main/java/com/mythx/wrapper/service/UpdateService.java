package com.mythx.wrapper.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mythx.wrapper.Launch;
import com.mythx.wrapper.config.Config;
import com.mythx.wrapper.config.HttpClientConfig;

import com.mythx.wrapper.model.Version;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class UpdateService {

    private static final String BASE_DIR = "C:" + File.separator + ".mythx" + File.separator;
    private static final String LAUNCHER_DIR = BASE_DIR + "launcher" + File.separator;
    private static final String LAUNCHER_VERSION_PATH = LAUNCHER_DIR + "launcher_version.json";
    private static File file = new File(LAUNCHER_VERSION_PATH);

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1500;

    private final CloseableHttpClient httpClient;

    public UpdateService() {
        this.httpClient = HttpClientConfig.getHttpClient();
    }

    public void init() {
        createDirectory();
        loadLocalVersion();
        loadRemoteVersionWithRetry();
        checkUpdates();
    }

    /**
     * Attempts to load remote version with retry logic.
     * Retries help handle transient DNS resolution failures that can occur
     * when double-clicking JAR files before Windows fully initializes networking.
     */
    private void loadRemoteVersionWithRetry() {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            loadRemoteVersion();
            if (Config.get().getRemoteVersion() != null) {
                return; // Success
            }
            if (attempt < MAX_RETRIES) {
                log.info("Retry attempt {} of {} in {}ms...", attempt, MAX_RETRIES, RETRY_DELAY_MS);
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.warn("Failed to load remote version after {} attempts", MAX_RETRIES);
    }


    private void createDirectory() {
        File parentDir = file.getParentFile(); // Get the parent directory of the file

        if (!parentDir.exists()) {
            if (parentDir.mkdirs()) { // Create the directory if it doesn't exist
                log.info("Directory created: " + parentDir.getAbsolutePath());
            } else {
                log.warn("Failed to create directory: " + parentDir.getAbsolutePath());
            }
        }
    }

    private void loadLocalVersion() {
        Gson gson = new Gson();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Type type = new TypeToken<Version>() {
                }.getType();
                Config.get().setLocalVersion(gson.fromJson(reader, type));
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    public void loadRemoteVersion() {
        Gson gson = new Gson();
        String manifestUrl = Config.get().getLauncherManifestUrl();
        HttpGet httpGet = new HttpGet(manifestUrl);

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            String responseString = EntityUtils.toString(response.getEntity());
            // Parse manifest.json format directly
            // { "version": "42", "clientUrl": "...", "timestamp": 123, "versions": [...] }
            com.google.gson.JsonObject manifest = gson.fromJson(responseString, com.google.gson.JsonObject.class);

            String version = manifest.get("version").getAsString();
            String clientUrl = manifest.get("clientUrl").getAsString();

            // Extract filename from URL (e.g., "mythx_client_abc123.jar" -> "mythx_client_abc123")
            String filename = clientUrl.substring(clientUrl.lastIndexOf('/') + 1);
            if (filename.endsWith(".jar")) {
                filename = filename.substring(0, filename.length() - 4);
            }

            Version remoteVersion = new Version(version, "0", filename, clientUrl);
            Config.get().setRemoteVersion(remoteVersion);
            log.info("Remote version loaded: " + remoteVersion);
        } catch (IOException e) {
            log.warn("Error retrieving data from manifest ", e);
        }
    }


    public void checkUpdates() {
        Version localVersion = Config.get().getLocalVersion();
        Version remoteVersion = Config.get().getRemoteVersion();

        // Handle case where remote version couldn't be fetched (network issues)
        if (remoteVersion == null) {
            if (localVersion != null && hasCachedLauncher(localVersion)) {
                log.info("Cannot reach update server. Using cached launcher: {}", localVersion.getFilename());
                return; // Use existing cached launcher
            } else {
                throw new RuntimeException("Cannot reach update server and no cached launcher available. Please check your internet connection.");
            }
        }

        if (localVersion == null) {
            log.info("Local version is missing. Start download jar");
            log.info(remoteVersion.getUrl());
            log.info(remoteVersion.getFilename());
            downloadJar(remoteVersion.getUrl(), remoteVersion.getFilename());
        } else {
            int locVersion = Integer.parseInt(localVersion.getVersion());
            int remVersion = Integer.parseInt(remoteVersion.getVersion());

            if (locVersion < remVersion) {
                log.info("Need update jar");
                downloadJar(remoteVersion.getUrl(), remoteVersion.getFilename());
            } else {
                log.info("Version of jar is up to date");
            }
        }
    }

    /**
     * Checks if a cached launcher JAR exists for the given version.
     */
    private boolean hasCachedLauncher(Version version) {
        if (version == null || version.getFilename() == null) {
            return false;
        }
        File cachedJar = new File(LAUNCHER_DIR + version.getFilename() + ".jar");
        return cachedJar.exists();
    }


    public void downloadJar(String fileURL, String fileName) {
        String savePath = LAUNCHER_DIR + fileName + ".jar"; // Save launcher to launcher directory
        
        

        CloseableHttpClient client = HttpClientConfig.getHttpClient();
        String cacheBustedUrl = fileURL + "?t=" + System.currentTimeMillis();
        HttpGet request = new HttpGet(cacheBustedUrl);

        try (CloseableHttpResponse response = client.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    long contentLength = entity.getContentLength(); // Get the total size of the file
                    try (InputStream inputStream = entity.getContent()) {
                        Path outputPath = Paths.get(savePath);
                        Files.createDirectories(outputPath.getParent()); // Ensure parent directories exist
                        try (FileOutputStream fileOutputStream = new FileOutputStream(savePath)) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            long totalBytesRead = 0;

                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                fileOutputStream.write(buffer, 0, bytesRead);
                                totalBytesRead += bytesRead;

                                // Update progress
                                if (contentLength > 0) {
                                    int progress = (int) ((totalBytesRead * 100) / contentLength);
                                    showProgress(progress);
                                }

                            }
                        }
                        log.info("Download completed: " + savePath);
                        if(isOldJarExist()){
                            deleteOldJar();
                        }
                        saveVersion();

                    }
                }
            } else {
                log.warn("Failed to download file: " + response.getStatusLine());
            }
        } catch (IOException e) {
            log.warn("Error while downloading file: " + e.getMessage());
        }
    }

    /**
     * Displays a simple progress bar in the console.
     *
     * @param progress the current progress percentage (0 to 100)
     */
    private void showProgress(int progress) {
        if (Launch.getSplash() != null) { Launch.getSplash().setProgress(progress); }
    }


    public boolean isOldJarExist() {
        Version localVersion = Config.get().getLocalVersion();
        Version remoteVersion = Config.get().getRemoteVersion();
        if (localVersion != null) {
            if (!localVersion.getFilename().equals(remoteVersion.getFilename())) {
                return new File(LAUNCHER_DIR + localVersion.getFilename() + ".jar").exists();
            }
        }
        return false;
    }

    private void deleteOldJar() {
        // Construct the path to the old JAR file
        String oldJar = LAUNCHER_DIR + Config.get().getLocalVersion().getFilename() + ".jar";

        File oldFile = new File(oldJar);

        if (oldFile.exists()) {
            // Attempt to delete the file
            if (oldFile.delete()) {
                log.info("Old JAR file deleted successfully: " + oldJar);
            } else {
                log.warn("Failed to delete old JAR file: " + oldJar);
            }
        } else {
            log.info("No old JAR file found to delete at: " + oldJar);
        }
    }

    private void saveVersion() {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(file)) {
            Config.get().setLocalVersion(Config.get().getRemoteVersion());
            gson.toJson(Config.get().getLocalVersion(), writer);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
    }
}
