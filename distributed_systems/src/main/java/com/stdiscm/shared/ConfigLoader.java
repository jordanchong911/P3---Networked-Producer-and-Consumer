package com.stdiscm.shared;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private String consumerHost;
    private int consumerPort;

    public ConfigLoader(String configFilePath) {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(configFilePath)) {
            properties.load(input);
            consumerHost = properties.getProperty("consumer.host", "defaultHost");
            consumerPort = Integer.parseInt(properties.getProperty("consumer.port", "12345"));
        } catch (IOException e) {
            e.printStackTrace();
            // Provide fallback defaults or handle the exception as needed.
            consumerHost = "localhost";
            consumerPort = 12345;
        }
    }

    public String getConsumerHost() {
        return consumerHost;
    }

    public int getConsumerPort() {
        return consumerPort;
    }
}

