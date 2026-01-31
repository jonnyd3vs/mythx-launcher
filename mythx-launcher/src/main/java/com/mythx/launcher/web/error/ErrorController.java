package com.mythx.launcher.web.error;

import ch.qos.logback.classic.LoggerContext;
import com.mythx.launcher.LauncherSettings;
import com.mythx.launcher.config.Config;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ErrorController {
    private final static Logger LOGGER = LoggerFactory.getLogger(ErrorController.class);
    private final static String SERVICE_URL_DEFINES_IP = "https://api.ipify.org"; // service for determining the IP address
    private final static String ERROR_URL = "https://ecm.legion-ent.com/api/v1/logs/create-client-error-no-auth";
    private static final Path PATH_TO_DEBUG_LOG = Paths.get(System.getProperty("user.home"),
            ".mythx", "logs", "launcher", "debug.log");
    private static final Path PATH_TO_ERROR_LOG = Paths.get(System.getProperty("user.home"),
            ".mythx", "logs", "launcher", "error.log");
    private final static String SERVER_NAME = "MythX";
    private final static String PROJECT_ID = "24";
    private final static int MAX_BYTES = 10 * 1024; // 10KB
    
    /**
     * Get the current client version dynamically from Config.
     * Returns the downloaded client version, or "unknown" if not available.
     */
    private static String getClientVersion() {
        try {
            String serverName = LauncherSettings.getServerName();
            Integer version = Config.get().getClientVersions().get(serverName);
            if (version != null) {
                return String.valueOf(version);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to get client version from Config: {}", e.getMessage());
        }
        return "unknown";
    }


    public static void sendError(String username) {
        sendError(username, null);
    }

    public static void sendError(String username, Throwable exception) {
        // Flush Logback buffers to ensure all logs are written to disk before reading
        flushLogback();

        String errorContent;
        String mainContent = readLastBytes(PATH_TO_DEBUG_LOG, MAX_BYTES);

        if (exception != null) {
            // Convert exception to string directly - this is the key fix!
            // When running from IDE, log files may be buffered and not written yet,
            // so we need to capture the exception directly.
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            pw.flush();
            pw.close();
            errorContent = sw.toString();

            // If stack trace is empty, at least capture the message
            if (errorContent == null || errorContent.trim().isEmpty()) {
                errorContent = exception.getClass().getName() + ": " + exception.getMessage();
            }

            LOGGER.debug("Captured exception content (length={})", 
                    errorContent != null ? errorContent.length() : 0);
        } else {
            // Fall back to reading from file
            errorContent = readLastBytes(PATH_TO_ERROR_LOG, MAX_BYTES);
            LOGGER.debug("Read error content from file (length={})", 
                    errorContent != null ? errorContent.length() : 0);
        }

        LOGGER.debug("Debug log content (length={})", mainContent != null ? mainContent.length() : 0);

        // ALWAYS send to API - even if logs are empty, we want to track closures
        // If both are empty, add a marker so we know the launcher was closed
        if ((mainContent == null || mainContent.trim().isEmpty()) && 
            (errorContent == null || errorContent.trim().isEmpty())) {
            LOGGER.info("No log content found, but still sending close event to API");
            mainContent = "[LAUNCHER_CLOSED] No debug logs found at: " + PATH_TO_DEBUG_LOG;
            errorContent = "[LAUNCHER_CLOSED] No error logs found at: " + PATH_TO_ERROR_LOG;
        }

        System.out.println("[DEBUG] Preparing to send to API: " + ERROR_URL);
        System.out.println("[DEBUG] Error content length: " + (errorContent != null ? errorContent.length() : 0));
        System.out.println("[DEBUG] Main content length: " + (mainContent != null ? mainContent.length() : 0));

        try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            HttpPost error = new HttpPost(ERROR_URL);

            // adding parameters
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("project_id", PROJECT_ID));
            params.add(new BasicNameValuePair("ip_address", getIpAddress())); // maybe will be useful data
            params.add(new BasicNameValuePair("server-name", SERVER_NAME));
            params.add(new BasicNameValuePair("cache_version", getClientVersion()));
            params.add(new BasicNameValuePair("username",
                    username == null || username.isEmpty() ? "unknown" : username));
            params.add(new BasicNameValuePair("error_body", errorContent != null ? errorContent : ""));
            params.add(new BasicNameValuePair("main_body", mainContent != null ? mainContent : ""));

            // set parameters to entity
            HttpEntity entity = new UrlEncodedFormEntity(params, StandardCharsets.UTF_8);

            error.setEntity(entity);  //set entity to HttpPost

            LOGGER.info("Sending logs to API (error: {} chars, main: {} chars)", 
                    errorContent != null ? errorContent.length() : 0,
                    mainContent != null ? mainContent.length() : 0);
            System.out.println("[DEBUG] Executing HTTP POST to: " + ERROR_URL);
            CloseableHttpResponse response = closeableHttpClient.execute(error); // call to API
            int statusCode = response.getStatusLine().getStatusCode();
            LOGGER.info("Logs sent to server. Status: {}", statusCode);
            System.out.println("[DEBUG] API Response status: " + statusCode);
            System.out.println("[DEBUG] API call completed successfully!");

        } catch (IOException e) {
            LOGGER.warn("Couldn't send logs: {}", e.getMessage(), e);
            System.out.println("[DEBUG] API call FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send error asynchronously to avoid blocking the main thread.
     * This is critical for uncaught exception handlers.
     */
    public static void sendErrorAsync(String username, Throwable exception) {
        new Thread(() -> sendError(username, exception), "ErrorSender").start();
    }

    /**
     * Flush all Logback appenders to ensure logs are written to disk.
     * This is critical before reading log files for error reporting.
     */
    private static void flushLogback() {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.getLoggerList().forEach(logger ->
                logger.iteratorForAppenders().forEachRemaining(appender -> {
                    if (appender instanceof ch.qos.logback.core.OutputStreamAppender) {
                        try {
                            ((ch.qos.logback.core.OutputStreamAppender<?>) appender).getOutputStream().flush();
                        } catch (IOException ignored) {
                        }
                    }
                })
            );
        } catch (Exception e) {
            // Silently ignore flush errors - don't want to break error reporting
        }
    }

    /**
     * Read the last N bytes from a file.
     */
    private static String readLastBytes(Path path, int maxBytes) {
        if (!path.toFile().exists()) {
            return null;
        }

        try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")) {
            long fileLength = file.length();

            if (fileLength == 0) {
                return "";
            }

            int bytesToRead = (int) Math.min(fileLength, maxBytes);
            long startPosition = fileLength - bytesToRead;

            file.seek(startPosition);
            byte[] buffer = new byte[bytesToRead];
            file.readFully(buffer);

            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warn("Couldn't read log file {}: {}", path, e.getMessage());
            return null;
        }
    }

    // returns an IP address determined by another API
    private static String getIpAddress() {
        try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(SERVICE_URL_DEFINES_IP);
            CloseableHttpResponse httpResponse = closeableHttpClient.execute(httpGet);

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent()));
            String ip = reader.readLine();
            reader.close();

            return ip;
        } catch (IOException e) {
            LOGGER.warn("Couldn't get IP address: {}", e.getMessage());
            return "unknown";
        }
    }
}
