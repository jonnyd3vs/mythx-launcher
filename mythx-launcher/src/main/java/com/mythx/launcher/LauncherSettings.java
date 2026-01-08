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
    public static final String BASE_DIR = "C:" + File.separator + ".mythx" + File.separator;

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

    // Production client URLs
    public static final String PROD_CLIENT_MANIFEST = "https://files.mythxrsps.com/client/manifest.json";
    public static final String PROD_CLIENT_URL = "https://files.mythxrsps.com/client/mythx_client.jar";

    // Beta client URLs
    public static final String BETA_CLIENT_MANIFEST = "https://files.mythxrsps.com/beta/client/manifest.json";
    public static final String BETA_CLIENT_URL = "https://files.mythxrsps.com/beta/client/mythx_client.jar";

    public static final String SERVER_NAME = "MythX";

    /**
     * Get the appropriate client manifest URL based on beta mode
     */
    public static String getClientManifestUrl() {
        return BETA_MODE ? BETA_CLIENT_MANIFEST : PROD_CLIENT_MANIFEST;
    }

    /**
     * Get the appropriate client download URL based on beta mode
     */
    public static String getClientDownloadUrl() {
        return BETA_MODE ? BETA_CLIENT_URL : PROD_CLIENT_URL;
    }
}
