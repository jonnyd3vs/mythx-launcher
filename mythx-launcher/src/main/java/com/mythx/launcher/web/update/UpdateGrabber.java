package com.mythx.launcher.web.update;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.mythx.launcher.Launch;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Jonny
 */
public class UpdateGrabber {

    public static UpdateRequest updateRequest = null;

    public UpdateGrabber() {
        load();
    }

    public void load() {

        URL url;
        InputStream is = null;

        try {

            url = new URL("http://ecm.legion-ent.com/api/v1/game/latest-updates/2");

            URLConnection hc = url.openConnection();
            hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

            is = hc.getInputStream();  // throws an IOException

            BufferedReader in = new BufferedReader(new InputStreamReader(hc.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine).append("\n");
            }

            try (BufferedReader br = new BufferedReader(new StringReader(content.toString()))) {
                Gson gson = new Gson();
                JsonParser fileParser = new JsonParser();

                // Create JsonReader from BufferedReader
                JsonReader jsonReader = new JsonReader(br);
                jsonReader.setLenient(true); // Set lenient mode

                // Parse the JSON using JsonParser with the lenient JsonReader
                JsonObject object = (JsonObject) fileParser.parse(jsonReader);

                // Deserialize JsonObject to UpdateRequest class
                updateRequest = gson.fromJson(object, UpdateRequest.class);

            } catch (JsonSyntaxException | IOException e) {
                e.printStackTrace();
            }

        } catch (IOException mue) {
            mue.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ioe) {
                // nothing to see here
            }
        }
    }

}
