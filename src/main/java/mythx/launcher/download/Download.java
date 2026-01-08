package mythx.launcher.download;

import mythx.launcher.Launch;
import mythx.launcher.LauncherSettings;
import mythx.launcher.components.LauncherComponent;
import mythx.launcher.utility.RandomString;
import mythx.launcher.utility.Stopwatch;
import mythx.launcher.utility.Utilities;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Download implements Runnable {

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

    @Override
    public void run() {

       File original = new File(LauncherSettings.SAVE_DIR + serverName);

       if(original != null && original.exists()) {
           original.delete();
           System.out.println("Delete..");
       }

        SPEED_TIMER = new Stopwatch();

        stopped = false;
        paused = false;

        downloadState = DownloadState.DOWNLOADING;

        RandomAccessFile file = null;
        InputStream stream = null;

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Range", "bytes=" + downloaded + "-");
            connection.connect();

            if (connection.getResponseCode() / 100 != 2) {
               // error();
            }

            int contentLength = connection.getContentLength();

            if (contentLength < 1) {
               // error();
            }

            if (size == -1) {
                size = contentLength;
              //  stateChanged();
            }

            file = new RandomAccessFile(LauncherSettings.SAVE_DIR + serverName, "rw");
            file.seek(downloaded);

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
                Utilities.launchClient(serverName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //error();
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
}
