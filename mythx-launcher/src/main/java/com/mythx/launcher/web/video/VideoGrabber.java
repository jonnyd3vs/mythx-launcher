package com.mythx.launcher.web.video;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


/**
 * @author Jonny
 */
public class VideoGrabber {

    public static final String LATEST_VIDEO_URL = "http://ecm.legion-ent.com/api/v1/game/latest-youtube-video/2";

    private final static Logger LOGGER = LoggerFactory.getLogger(VideoGrabber.class);

    public static String getVideoId() {
        try {
            URL url = new URL(LATEST_VIDEO_URL);

            URLConnection hc = url.openConnection();
            hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

            InputStream is = hc.getInputStream();  // throws an IOException
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            Gson gson = new Gson();
            JsonParser fileParser = new JsonParser();
            JsonObject object = (JsonObject) fileParser.parse(br);
            VideoRequest videoRequest = gson.fromJson(object, VideoRequest.class);
            return videoRequest.getData().video_id;

        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
            return null;
        }
    }

}
