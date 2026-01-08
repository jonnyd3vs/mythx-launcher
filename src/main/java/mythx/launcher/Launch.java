package mythx.launcher;

import com.google.gson.stream.JsonReader;
import mythx.launcher.cache.CacheDownloader;
import mythx.launcher.components.LauncherComponent;
import mythx.launcher.components.impl.AnnouncementBar;
import mythx.launcher.components.impl.AnnouncementOpen;
import mythx.launcher.components.impl.AnnouncementTime;
import mythx.launcher.components.impl.AnnouncementTitle;
import mythx.launcher.download.Download;
import mythx.launcher.fonts.LauncherFont;
import mythx.launcher.handler.GlobalExceptionHandler;
import mythx.launcher.utility.SSLTool;
import mythx.launcher.utility.Utilities;
import mythx.launcher.web.server.Server;
import mythx.launcher.web.update.Update;
import mythx.launcher.web.update.UpdateGrabber;
import mythx.launcher.web.video.VideoGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

public class Launch {
    private final static Logger LOGGER = LoggerFactory.getLogger(Launch.class);
    public static Download download;

    private static  LauncherFrame launcherFrame;
    private static  LauncherSplash launcherSplash;

    private static VideoGrabber VIDEO_GRABBER;

    private static UpdateGrabber UPDATE_GRABBER;


    public static boolean USE_SECONDARY_WEBSITE = false;

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());

        launcherSplash = new LauncherSplash();
        launcherSplash.start();

        try {
            VIDEO_GRABBER = new VideoGrabber();
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            UPDATE_GRABBER = new UpdateGrabber();
        } catch(Exception e) {
            e.printStackTrace();
        }

        launcherFrame = new LauncherFrame();

        if(!new File(Utilities.getCacheDirectory()).exists()) {
            new File(Utilities.getCacheDirectory()).mkdir();
        }

        /**
         * DOWNLOADING CACHE *
         */
        try {
            CacheDownloader.init();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        start();
    }

    public static void start() {
        SSLTool.disableCertificateValidation();
        LauncherFont.loadFonts();

        launcherFrame = new LauncherFrame();
        launcherFrame.start();

        launcherSplash.dispose();
    }

    public static Download getDownload() {
        return download;
    }


    public static Download resetDownload(String downloadUrl, String serverName) {
        return download = new Download(downloadUrl, serverName);
    }

    public static void clearDownload() {
        download = null;
    }

    public static LauncherFrame getLauncherFrame() {
        return launcherFrame;
    }

}
