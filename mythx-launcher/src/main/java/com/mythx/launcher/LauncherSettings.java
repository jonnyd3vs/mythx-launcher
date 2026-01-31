package com.mythx.launcher;

import java.io.File;

/**
 * Settings for the launcher
 * @author Jonny
 */
public class LauncherSettings {

    /**
     * Base directory for all MythX files
     */
    public static final String BASE_DIR = System.getProperty("user.home") + File.separator + ".mythx" + File.separator;

    /**
     * Enables developer only settings
     */
    public static boolean DEVELOPER_MODE = false;

    /**
     * Beta mode - set via --beta command line argument
     */
    public static boolean BETA_MODE = false;

    /**
     * The directory the client is saved to
     */
    public static final String SAVE_DIR = BASE_DIR + "client" + File.separator;

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

    // Google Cloud Storage client URLs
    public static final String PROD_CLIENT_URL = "https://storage.googleapis.com/flippers/mythx_client.jar";
    // Beta mode transforms the URL by replacing "flippers" with "rsps-beta"
    public static final String BETA_CLIENT_URL = "https://storage.googleapis.com/rsps-beta/mythx_client.jar";

    public static final String SERVER_NAME = "MythX";

    /**
     * Get the appropriate client download URL based on beta mode
     */
    public static String getClientDownloadUrl() {
        return BETA_MODE ? BETA_CLIENT_URL : PROD_CLIENT_URL;
    }

    /**
     * Get the server name for file storage (used in client-version.json)
     */
    public static String getServerName() {
        return BETA_MODE ? "Beta_mythx" : "mythx";
    }

    /**
     * Get the client filename for local storage
     * Using .jar extension to avoid Windows Defender blocking (previously .dat triggered security)
     */
    public static String getClientFilename() {
        return getServerName() + ".jar";
    }
}
