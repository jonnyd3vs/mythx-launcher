package com.mythx.launcher.web.error;

import com.mythx.launcher.utility.FileOperations;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ErrorController {
    private final static Logger LOGGER = LoggerFactory.getLogger(ErrorController.class);
    private final static String SERVICE_URL_DEFINES_IP = "https://api.ipify.org"; // service for determining the IP address
    private final static String ERROR_URL = "https://ecm.legion-ent.com/api/v1/logs/create-client-error-no-auth";
    private static final Path PATH_TO_ERRORS_FILE = Paths.get(System.getProperty("user.home"),
            ".mythx", "logs", "launcher", "error.log");
    private final static String SERVER_NAME = "MythX";
    private final static String PROJECT_ID = "24";
    private final static String CLIENT_VERSION = "2";


    public static void sendError(String username) {
        String errorContent = FileOperations.readFileToString(PATH_TO_ERRORS_FILE); // read error logs from file

        try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            HttpPost error = new HttpPost(ERROR_URL);

            // adding parameters
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("project_id", PROJECT_ID));
            params.add(new BasicNameValuePair("ip_address", getIpAddress())); // maybe will be useful data
            params.add(new BasicNameValuePair("server-name", SERVER_NAME));
            params.add(new BasicNameValuePair("cache_version", CLIENT_VERSION));
            params.add(new BasicNameValuePair("username",
                    username == null || username.isEmpty() ? "unknown" : username));
            params.add(new BasicNameValuePair("error_body", errorContent));

            // set parameters to entity
            HttpEntity entity = new UrlEncodedFormEntity(params, StandardCharsets.UTF_8);

            error.setEntity(entity);  //set entity to HttpPost

            CloseableHttpResponse response = closeableHttpClient.execute(error); // call to API
            LOGGER.info("ERROR was sent to server");
            LOGGER.info("Response status code:  " + response.getStatusLine().getStatusCode());

        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), "Couldn't send error", e);
        }
    }

    // returns an IP address determined by another API
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
            System.out.println("Couldn't send error " + e);
            return "undefined IP address";
        }
    }
}
