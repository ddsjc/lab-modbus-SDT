package org.example.modbus;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static final String CONFIG_FILE_PATH = "src/main/resources/application.properties";
    private static Properties properties;

    static {
        properties = loadProperties();
    }
    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE_PATH)) {
            props.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return props;
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
