package mythx.launcher;

import java.io.File;

/**
 * Settings for the launcher
 * @author Jonny
 */
public class LauncherSettings {

    /**
     * Enables developer only settings
     */
    public static boolean DEVELOPER_MODE = false;

    public static boolean BETA_MODE = false;

    /**
     * The directory the client is saved to
     */
    public static final String SAVE_DIR = System.getProperty("user.home") + File.separator;

    /**
     * The directory in which fonts are loaded from
     */
    public static String FONTS_DIRECTORY = "/fonts/";

    /**
     * The file that we load to get the current game feed from
     */
    public static String SERVERS_FILE = "http://runelinks.com/servers";

    /**
     * The file that we load to get the current game feed from
     */
    public static String SECONDARY_SERVERS_FILE = "http://therealmrsps.com/servers";
}
