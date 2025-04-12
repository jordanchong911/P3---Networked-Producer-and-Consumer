package com.stdiscm.shared;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigHelper {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 12346;

    private Properties properties;

    /**
     * Loads the configuration from a specified properties file.
     * 
     * @param propertiesFilePath the path to the properties file
     */
    public ConfigHelper(String propertiesFilePath) {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(propertiesFilePath)) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("Error loading properties file: " + propertiesFilePath + ". Using default values.");
        }
    }

    /**
     * Returns the host setting from the properties file.
     * If the property is not found, returns the default host.
     * 
     * @return host as a String
     */
    public String getHost() {
        return properties.getProperty("host", DEFAULT_HOST);
    }

    /**
     * Returns the port setting from the properties file.
     * If the property is not found or is invalid, returns the default port.
     * 
     * @return port as an int
     */
    public int getPort() {
        String portStr = properties.getProperty("port", String.valueOf(DEFAULT_PORT));
        try {
            return Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number in properties file. Using default port " + DEFAULT_PORT);
            return DEFAULT_PORT;
        }
    }
}
