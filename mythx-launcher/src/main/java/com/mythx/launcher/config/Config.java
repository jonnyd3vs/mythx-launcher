package com.mythx.launcher.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton configuration for the launcher
 * Stores client version information and API endpoints
 */
public class Config {
    private static Config instance;

    private static final String CLIENT_VERSION_URL = "https://ecm.legion-ent.com/api/v1/projects/client-version";
    private static final int PROJECT_ID = 24;

    private Map<String, Integer> clientVersions = new HashMap<>();

    private Config() {
        // Private constructor for singleton
    }

    public static Config get() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public String getClientVersionUrl() {
        return CLIENT_VERSION_URL;
    }

    public int getProjectId() {
        return PROJECT_ID;
    }

    public Map<String, Integer> getClientVersions() {
        return clientVersions;
    }

    public void setClientVersions(Map<String, Integer> clientVersions) {
        this.clientVersions = clientVersions;
    }
}
