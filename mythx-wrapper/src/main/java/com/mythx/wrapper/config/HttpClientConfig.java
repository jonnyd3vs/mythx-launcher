package com.mythx.wrapper.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

// Class does one configuration Http client on whole app
public class HttpClientConfig {
    private static CloseableHttpClient client;

    // maybe add custom configuration   ???
    static {
        client = HttpClients.createDefault();
    }

    public static CloseableHttpClient getHttpClient() {
        return client;
    }
}
