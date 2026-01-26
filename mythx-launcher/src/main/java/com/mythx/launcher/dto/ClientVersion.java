package com.mythx.launcher.dto;

/**
 * DTO for ECM API client version response
 * API endpoint: https://ecm.legion-ent.com/api/v1/projects/client-version?projectId=24
 * Response format: {"value": "42"}
 */
public class ClientVersion {
    private String value;

    public ClientVersion() {
    }

    public ClientVersion(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getVersionAsInt() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "ClientVersion{value='" + value + "'}";
    }
}
