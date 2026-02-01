package com.mythx.launcher.utility;

import com.mythx.launcher.Launch;
import com.mythx.launcher.LauncherSettings;
import com.mythx.launcher.components.LauncherComponent;
import com.mythx.launcher.jdk.JdkDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;

/**
 * Misc methods that operate random things
 * @author Jonny
 */
public class Utilities {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utilities.class);

    // Cached JDK path
    private static String cachedJavaPath = null;
    
    // Client output capture (last 5 seconds after launch)
    private static final StringBuilder clientOutput = new StringBuilder();
    private static final int CLIENT_OUTPUT_CAPTURE_SECONDS = 5;
    private static final int CLIENT_OUTPUT_MAX_CHARS = 50000;
    
    /**
     * Get captured client output (for error reporting)
     */
    public static String getClientOutput() {
        synchronized (clientOutput) {
            return clientOutput.toString();
        }
    }
    
    /**
     * Clear captured client output
     */
    public static void clearClientOutput() {
        synchronized (clientOutput) {
            clientOutput.setLength(0);
        }
    }

    public static int getPercent(int current, int pixels) {
        return (int) ((pixels) * .01 * current);
    }


    public static ImageIcon getImageIconFromFile(String fileDirectory) {
        return new ImageIcon(FileOperations.ReadFile(getCacheDirectory()+ fileDirectory));
    }

    public static ImageIcon getImageIconForURL(String rawUrl) {

        URL url = null;
        try {
            url = new URL(rawUrl);

            Image image = ImageIO.read(url);

            //image.getScaledInstance(240, 200,  java.awt.Image.SCALE_SMOOTH)

            return new ImageIcon(image);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getCacheDirectory() {
        String cacheLoc = LauncherSettings.BASE_DIR + "cache" + File.separator;
        File cacheDir = new File(cacheLoc);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheLoc;
    }

    public static Image getResizedImage(int width, int height, Image image) {
        return image.getScaledInstance(width, height,  java.awt.Image.SCALE_SMOOTH);
    }

    public static void openUrl(String url) {
        String osName = System.getProperty("os.name");
        try {
            if (osName.startsWith("Mac OS")) {
                Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL",
                        new Class[] { String.class });
                openURL.invoke(null, new Object[] { url });
            } else if (osName.startsWith("Windows"))
                Runtime.getRuntime().exec(
                        "rundll32 url.dll,FileProtocolHandler " + url);
            else {
                String[] browsers = { "firefox", "opera", "konqueror",
                        "epiphany", "mozilla", "netscape", "safari" };
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++)
                    if (Runtime.getRuntime()
                            .exec(new String[] { "which", browsers[count] })
                            .waitFor() == 0)
                        browser = browsers[count];
                if (browser == null) {
                    throw new Exception("Could not find web browser");
                } else
                    Runtime.getRuntime().exec(new String[] { browser, url });
            }
        } catch (Exception e) {
        }
    }

    public static void launchClient(String serverName) {
        try {
            // Verify the JAR file exists before attempting to launch
            File jarFile = new File(LauncherSettings.SAVE_DIR + serverName);
            if (!jarFile.exists()) {
                LOGGER.error("Cannot launch client - JAR file does not exist: {}", jarFile.getAbsolutePath());
                return;
            }
            LOGGER.info("JAR file exists: {} ({} bytes)", jarFile.getAbsolutePath(), jarFile.length());

            // Get JDK path (uses cached path or downloads if needed)
            String javaPath = ensureJdk11AndGetPath();
            if (javaPath == null || javaPath.isEmpty()) {
                LOGGER.error("Cannot launch client - Java path is null or empty");
                return;
            }
            LOGGER.info("Launching client with Java: {}", javaPath);

            String[] command = new String[]{
                javaPath,
                "-Xms512m",
                "-Xmx1024m",
                "-jar",
                jarFile.getAbsolutePath(),
                String.valueOf(LauncherSettings.BETA_MODE)
            };

            LOGGER.info("Launch command: {} -Xms512m -Xmx1024m -jar {} {}",
                    javaPath, jarFile.getAbsolutePath(), LauncherSettings.BETA_MODE);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(LauncherSettings.SAVE_DIR));
            pb.redirectErrorStream(true);
            
            Process p = pb.start();
            LOGGER.info("Client process started successfully (PID available: {})", p.isAlive());
            
            // Clear previous client output
            clearClientOutput();
            
            // Start background thread to capture client output for 5 seconds
            final Process process = p;
            new Thread(() -> {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()))) {
                    long endTime = System.currentTimeMillis() + (CLIENT_OUTPUT_CAPTURE_SECONDS * 1000);
                    String line;
                    
                    while (System.currentTimeMillis() < endTime) {
                        // Check if there's data available (non-blocking check)
                        if (reader.ready()) {
                            line = reader.readLine();
                            if (line != null) {
                                synchronized (clientOutput) {
                                    if (clientOutput.length() < CLIENT_OUTPUT_MAX_CHARS) {
                                        clientOutput.append(line).append("\n");
                                        LOGGER.debug("Client: {}", line);
                                    }
                                }
                            }
                        } else {
                            // No data available, sleep briefly
                            Thread.sleep(100);
                        }
                        
                        // If process died, read remaining output and exit
                        if (!process.isAlive()) {
                            while (reader.ready() && (line = reader.readLine()) != null) {
                                synchronized (clientOutput) {
                                    if (clientOutput.length() < CLIENT_OUTPUT_MAX_CHARS) {
                                        clientOutput.append(line).append("\n");
                                    }
                                }
                            }
                            int exitCode = process.exitValue();
                            LOGGER.error("Client process exited with code: {}", exitCode);
                            break;
                        }
                    }
                    
                    LOGGER.info("Client output capture complete ({} chars)", clientOutput.length());
                } catch (Exception e) {
                    LOGGER.warn("Error capturing client output: {}", e.getMessage());
                }
            }, "ClientOutputCapture").start();
            
            // Give the process a moment to fail if it's going to fail immediately
            Thread.sleep(500);
            if (!p.isAlive()) {
                int exitCode = p.exitValue();
                LOGGER.error("Client process exited immediately with code: {}", exitCode);
            } else {
                LOGGER.info("Client is running successfully");
            }
            
            LauncherComponent.LOADING_BAR.getComponent().load(0);
            Launch.download = null;
        } catch (Exception e) {
            LOGGER.error("Failed to launch client", e);
            e.printStackTrace();
        }
    }

    /**
     * Ensure JDK 11 is installed and return the path to java executable
     */
    private static String ensureJdk11AndGetPath() {
        if (cachedJavaPath != null) {
            return cachedJavaPath;
        }

        JdkDownloader downloader = new JdkDownloader();
        cachedJavaPath = downloader.ensureJdk11Installed();
        return cachedJavaPath;
    }

    /**
     *
     * @param colorStr e.g. "#FFFFFF"
     * @return
     */
    public static Color hex2Rgb(String colorStr) {
        return new Color(
                Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
    }

    /**
     * provides a String representation of the given time
     * @return {@code millis} in hh:mm:ss format
     */
    public static final String formatTime(long millis) {
        long secs = millis / 1000;
        return String.format("%02d:%02d:%02d", secs / 3600, (secs % 3600) / 60, secs % 60);
    }

    public static long getFileSize(URL url) {
        URLConnection hc = null;
        try {
            hc = url.openConnection();
            hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            return hc.getContentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long getLastModified(URL url) {
        URLConnection hc = null;
        try {
            hc = url.openConnection();
            hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
             return hc.getLastModified();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
