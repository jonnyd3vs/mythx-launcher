package com.mythx.wrapper.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ErrorController {
    private static final String BASE_DIR = "C:" + File.separator + ".mythx" + File.separator;
    private static final String LOGS_DIR = BASE_DIR + "logs" + File.separator + "wrapper" + File.separator;
    private static final String PROJECT_ID = "12";
    private static final String SERVICE_URL_DEFINES_IP = "https://api.ipify.org";
    private static final String ERROR_URL = "https://ecm.legion-ent.com/api/v1/logs/create-client-error-no-auth";
    private static final Path PATH_TO_ERRORS_FILE = Paths.get(LOGS_DIR, "error.log");
    private static final String CLIENT_VERSION = "1";

    public static void sendError(String username) {
        sendError(username, null);
    }

    public static void sendError(String username, Throwable exception) {
        String errorContent;

        if (exception != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            errorContent = sw.toString();
        } else {
            errorContent = readFileToString(PATH_TO_ERRORS_FILE);
        }

        if (errorContent == null || errorContent.trim().isEmpty()) {
            log.info("No errors to send");
            return;
        }

        try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            HttpPost error = new HttpPost(ERROR_URL);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("project_id", PROJECT_ID));
            params.add(new BasicNameValuePair("ip_address", getIpAddress()));
            params.add(new BasicNameValuePair("server-name", "mythx-wrapper"));
            params.add(new BasicNameValuePair("cache_version", CLIENT_VERSION));
            params.add(new BasicNameValuePair("username",
                    username == null || username.isEmpty() ? "unknown" : username));
            params.add(new BasicNameValuePair("error_body", errorContent));

            HttpEntity entity = new UrlEncodedFormEntity(params, StandardCharsets.UTF_8);
            error.setEntity(entity);

            CloseableHttpResponse response = closeableHttpClient.execute(error);
            log.info("Error sent to server. Status: " + response.getStatusLine().getStatusCode());

        } catch (IOException e) {
            log.warn("Couldn't send error to API", e);
        }
    }

    public static void sendErrorAsync(String username, Throwable exception) {
        new Thread(() -> sendError(username, exception)).start();
    }

    private static String getIpAddress() {
        try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(SERVICE_URL_DEFINES_IP);
            CloseableHttpResponse httpResponse = closeableHttpClient.execute(httpGet);

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent()));
            String ip = reader.readLine();
            reader.close();

            return ip;
        } catch (IOException e) {
            log.warn("Couldn't get IP address", e);
            return "unknown";
        }
    }

    private static String readFileToString(Path path) {
        try {
            if (!Files.exists(path)) {
                return null;
            }
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            log.warn("Couldn't read error file", e);
            return null;
        }
    }
}
