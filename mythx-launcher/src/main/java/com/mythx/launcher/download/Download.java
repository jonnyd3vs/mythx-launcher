package com.mythx.launcher.download;

import com.mythx.launcher.Launch;
import com.mythx.launcher.LauncherSettings;
import com.mythx.launcher.components.LauncherComponent;
import com.mythx.launcher.utility.RandomString;
import com.mythx.launcher.utility.Stopwatch;
import com.mythx.launcher.utility.Utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Download implements Runnable {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Download.class);

    Stopwatch SPEED_TIMER;

    private int secondsElapsed = 0;

    private URL url;
    private int size;
    private int downloaded;
    private String serverName;

    private DownloadState downloadState;

    private static final int MAX_BUFFER_SIZE = 1024;

    private static int bytesWritten = 0;
    private static int kbPerSec = 0;

    private boolean paused;
    private boolean stopped;
    
    private Runnable onComplete;

    @Override
    public void run() {
        LOGGER.info("Starting download: {} -> {}", url, LauncherSettings.SAVE_DIR + serverName);

       File original = new File(LauncherSettings.SAVE_DIR + serverName);

       if(original != null && original.exists()) {
           original.delete();
           LOGGER.info("Deleted existing file: {}", original.getAbsolutePath());
       }

        SPEED_TIMER = new Stopwatch();

        stopped = false;
        paused = false;

        downloadState = DownloadState.DOWNLOADING;

        FileOutputStream file = null;
        InputStream stream = null;

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // Always request full file - no resume/Range header
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            LOGGER.info("Download response code: {}", responseCode);
            
            if (responseCode / 100 != 2) {
                LOGGER.error("Download failed with response code: {}", responseCode);
                downloadState = DownloadState.ERROR;
                return;
            }

            int contentLength = connection.getContentLength();
            LOGGER.info("Content-Length header: {}", contentLength);

            // Fallback for GCS which may not return Content-Length directly
            if (contentLength < 1) {
                String gcsLength = connection.getHeaderField("x-goog-stored-content-length");
                LOGGER.info("GCS x-goog-stored-content-length header: {}", gcsLength);
                if (gcsLength != null) {
                    try {
                        contentLength = Integer.parseInt(gcsLength);
                    } catch (NumberFormatException e) {
                        LOGGER.warn("Failed to parse GCS content length: {}", gcsLength);
                    }
                }
            }

            if (contentLength < 1) {
                LOGGER.error("Could not determine content length, aborting download");
                downloadState = DownloadState.ERROR;
                return;
            }

            if (size == -1) {
                size = contentLength;
              //  stateChanged();
            }

            // Create parent directories if they don't exist
            File outputFile = new File(LauncherSettings.SAVE_DIR + serverName);
            if (outputFile.getParentFile() != null) {
                boolean created = outputFile.getParentFile().mkdirs();
                LOGGER.info("Created directories: {} for path: {}", created, outputFile.getParentFile().getAbsolutePath());
            }
            
            // Delete existing file if it exists (might be locked by previous process)
            if (outputFile.exists()) {
                boolean deleted = outputFile.delete();
                LOGGER.info("Deleted existing file: {} (success: {})", outputFile.getAbsolutePath(), deleted);
                if (!deleted) {
                    LOGGER.warn("Could not delete existing file - may be locked by another process");
                    // Try garbage collection to release any file handles
                    System.gc();
                    Thread.sleep(100);
                    deleted = outputFile.delete();
                    LOGGER.info("Retry delete after GC: {}", deleted);
                }
            }
            
            // Log the full path we're about to write to
            LOGGER.info("Opening file for writing: {}", outputFile.getAbsolutePath());

            // Always write fresh file from beginning (no resume)
            file = new FileOutputStream(LauncherSettings.SAVE_DIR + serverName);

            stream = connection.getInputStream();

            int lastNum = 0;

            kbPerSec = 0;
            bytesWritten = 0;

            while (downloadState == DownloadState.DOWNLOADING) {

                byte buffer[];

                if (size - downloaded > MAX_BUFFER_SIZE) {
                    buffer = new byte[MAX_BUFFER_SIZE];
                } else {
                    buffer = new byte[size - downloaded];
                }

                if(stopped) {
                    downloadState = DownloadState.CANCELLED;
                    //LauncherComponent.LAUNCH_MESSAGE.getComponent().setText("<html>Welcome to <font color =#90ee90>The Realm</font>, click <font color=#90ee90>'Play Now'</font> on any server to open the client</html>");
                   // LauncherComponent.PERCENTAGE_COMPLETE.getComponent().setText("");
                    LauncherComponent.LOADING_BAR.getComponent().load(0);
                    Launch.getLauncherFrame().repaint();
                    Thread.currentThread().interrupt();
                    Launch.clearDownload();
                    break;
                }

                if(paused) {
                    Thread.sleep(100);
                    continue;
                }

                int read = stream.read(buffer);

                if (read == -1)
                    break;

                file.write(buffer, 0, read);

                int progress = (int) getProgress();

                if (progress > lastNum) {

                    lastNum = progress;

                    LauncherComponent.LOADING_BAR.getComponent().load(progress);

                    kbPerSec = (bytesWritten / secondsElapsed) / 1000;

                    int mbPerSec = kbPerSec / 1000;

                    int kbLeft = (size - bytesWritten) / 1000;

                    int estimatedTime = kbLeft / kbPerSec;

                    if(estimatedTime == 0) {
                        estimatedTime = 1;
                    }

                  //  LauncherComponent.LAUNCH_MESSAGE.getComponent().setText("<html>Download Speed - <font color=#006E00>"+mbPerSec+"MB</font> | Time Remaining: <font color=#8b0000>"+Utilities.formatTime(estimatedTime * 1000)+"</font><html>");
                   // LauncherComponent.PERCENTAGE_COMPLETE.getComponent().setText("<html><font color=#006E00>"+progress+"%</font> COMPLETED<html>");
                    Launch.getLauncherFrame().repaint();
                }

                if(SPEED_TIMER.elapsed(1000)) {
                    secondsElapsed++;
                    SPEED_TIMER.reset();
                }

                bytesWritten += read;
                downloaded += read;
            }

            if (downloadState == DownloadState.DOWNLOADING) {
                downloadState = DownloadState.COMPLETE;
                LOGGER.info("Download complete, file saved to: {}", LauncherSettings.SAVE_DIR + serverName);
                
                // Verify the file was written
                File downloadedFile = new File(LauncherSettings.SAVE_DIR + serverName);
                if (!downloadedFile.exists()) {
                    LOGGER.error("Download complete but file does not exist: {}", downloadedFile.getAbsolutePath());
                    return;
                }
                LOGGER.info("Downloaded file size: {} bytes", downloadedFile.length());
                
                // Call completion callback (saves version)
                if (onComplete != null) {
                    try {
                        onComplete.run();
                    } catch (Exception e) {
                        LOGGER.error("Error in download completion callback", e);
                    }
                }
                
                // Now launch the client
                LOGGER.info("Launching client after download...");
                Utilities.launchClient(serverName);
            }
        } catch (java.io.FileNotFoundException e) {
            LOGGER.error("File access denied - path: {}, error: {}", LauncherSettings.SAVE_DIR + serverName, e.getMessage());
            LOGGER.error("This is likely a permissions issue or antivirus blocking. Check if {} is writable.", LauncherSettings.SAVE_DIR);
            downloadState = DownloadState.ERROR;
            e.printStackTrace();
        } catch (java.security.AccessControlException e) {
            LOGGER.error("Security/permissions denied writing to: {}", LauncherSettings.SAVE_DIR + serverName);
            downloadState = DownloadState.ERROR;
            e.printStackTrace();
        } catch (Exception e) {
            LOGGER.error("Download failed with exception: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            downloadState = DownloadState.ERROR;
            e.printStackTrace();
        } finally {
            if (file != null)
                try { file.close(); } catch (Exception e) {e.printStackTrace(); }
            if (stream != null)
                try { stream.close();  } catch (Exception e) {e.printStackTrace(); }
        }
    }

    public float getProgress() {
        return ((float) downloaded / size) * 100;
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public Download(String downloadUrl, String serverName) {
        try {
            url = new URL(downloadUrl + "?"+antiCache());
            this.serverName = serverName;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        size = -1;
        downloaded = 0;
        downloadState = DownloadState.DOWNLOADING;
    }

    private static String antiCache() {
        return RandomString.getAlphaNumericString(10);
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public void setOnComplete(Runnable onComplete) {
        this.onComplete = onComplete;
    }
}
