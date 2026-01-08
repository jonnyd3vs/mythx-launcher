package com.mythx.wrapper;

import com.mythx.wrapper.config.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class PropertiesReader {

    public static Properties loadProperties(){
        Properties properties = new Properties();

        // Load the properties file from the classpath
        try (InputStream input = PropertiesReader.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                log.info("Sorry, unable to find application.properties");
                return null;
            }

            // Load properties from the input stream
            properties.load(input);

            // general properties
            String versionUrl = properties.getProperty("launcher.version.url");
            String gameServer = properties.getProperty("game.default.server");
            Config.get().setLauncherVersionUrl(versionUrl);
            Config.get().setDefaultGameServer(gameServer);

        } catch (IOException ex) {
            log.warn(ex.getMessage(), ex);
        }

        return properties;
    }
}
