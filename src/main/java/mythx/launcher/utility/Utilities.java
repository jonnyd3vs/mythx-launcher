package mythx.launcher.utility;

import mythx.launcher.Launch;
import mythx.launcher.LauncherSettings;
import mythx.launcher.components.LauncherComponent;

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
        String cacheLoc = System.getProperty("user.home") + "/mythx/";
        File cacheDir = new File(cacheLoc);
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
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
            Process p = Runtime.getRuntime().exec(new String[]{ "java", "-Xms512m", "-Xmx1024m", "-jar", LauncherSettings.SAVE_DIR + serverName, String.valueOf(LauncherSettings.BETA_MODE)});
           // LauncherComponent.LAUNCH_MESSAGE.getComponent().setText("<html>Welcome to <font color =#90ee90>The Realm</font>, click <font color=#90ee90>'Play Now'</font> on any server to open the client</html>");
            //LauncherComponent.PERCENTAGE_COMPLETE.getComponent().setText("");
            LauncherComponent.LOADING_BAR.getComponent().load(0);
            Launch.download = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
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
