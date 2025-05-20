package config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class to load properties from resource files.
 */
public class PropertyManager {
    private static final Logger logger = LogManager.getLogger(PropertyManager.class);

    private PropertyManager() {
        // Private constructor to prevent instantiation
    }

    /**
     * Loads properties from a resource file
     *
     * @param resourcePath Path to the properties file in the resources directory
     * @return Properties object containing loaded properties
     */
    public static Properties loadProperties(String resourcePath) {
        Properties properties = new Properties();
        try (InputStream inputStream = PropertyManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream != null) {
                properties.load(inputStream);
                logger.debug("Successfully loaded properties from: {}", resourcePath);
            } else {
                logger.warn("Could not find properties file: {}", resourcePath);
            }
        } catch (IOException e) {
            logger.error("Error loading properties from: {}", resourcePath, e);
        }
        return properties;
    }
}
