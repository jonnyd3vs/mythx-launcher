package com.mythx.launcher.web.controller;

import com.google.gson.Gson;
import com.mythx.launcher.exception.WebException;
import com.mythx.launcher.web.config.HttpClientConfig;
import com.mythx.launcher.web.model.response.GeneralLinksResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

// perhaps should rename this controller
public class GeneralProjectController {
    private final static Logger LOGGER = LoggerFactory.getLogger(GeneralProjectController.class);
    private final static String DOWN_LOAD_URL = "https://ecm.legion-ent.com/api/v1/projects/24";
    private final CloseableHttpClient httpClient;
    private final Gson gson;

    public GeneralProjectController() {
        this.httpClient = HttpClientConfig.getHttpClient();
        this.gson = new Gson();
    }

    public GeneralLinksResponse handleGeneralLinks() throws WebException {
        HttpGet httpGet = new HttpGet(DOWN_LOAD_URL);

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            String responseString = EntityUtils.toString(response.getEntity());
            return gson.fromJson(responseString, GeneralLinksResponse.class);
        } catch (IOException e) {
            LOGGER.error("Error handling general links", e);
            throw new WebException("Request failed", e);
        }
    }
}
