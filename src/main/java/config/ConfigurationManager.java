package config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages application configuration and provides access to configuration properties
 * from different sources with appropriate fallbacks.
 */
public class ConfigurationManager {
    private static final Logger logger = LogManager.getLogger(ConfigurationManager.class);
    private static final Map<String, String> cliProperties = new ConcurrentHashMap<>();
    private static final ConfigurationManager instance = new ConfigurationManager();
    private final Properties properties = new Properties();
    private String environment;

    private ConfigurationManager() {
        loadDefaultProperties();
        detectEnvironment();
        loadEnvironmentProperties();
        loadCLIProperties();
    }

    public static ConfigurationManager getInstance() {
        return instance;
    }

    /**
     * Sets command line properties passed via Maven or system properties
     */
    public static void setCliProperties(Map<String, String> cliProps) {
        cliProperties.putAll(cliProps);
    }

    /**
     * Gets property value with fallback mechanism
     * Order of lookup: CLI properties > environment-specific properties > default properties
     */
    public String getProperty(String propertyName) {
        // First check CLI overrides
        if (cliProperties.containsKey(propertyName)) {
            return cliProperties.get(propertyName);
        }

        // Then check if a CLI override with 'override.' prefix exists
        if (cliProperties.containsKey("override." + propertyName)) {
            return cliProperties.get("override." + propertyName);
        }

        // Then check loaded properties (which include environment-specific ones)
        if (properties.containsKey(propertyName)) {
            return properties.getProperty(propertyName);
        }

        logger.warn("Property '{}' not found in any configuration source", propertyName);
        return null;
    }

    /**
     * Gets property value with a default fallback value
     */
    public String getProperty(String propertyName, String defaultValue) {
        String value = getProperty(propertyName);
        return (value != null) ? value : defaultValue;
    }

    /**
     * Get integer property value
     */
    public int getIntProperty(String propertyName, int defaultValue) {
        String value = getProperty(propertyName);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.error("Error parsing integer property: {}, using default value: {}", propertyName, defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Get boolean property value
     */
    public boolean getBooleanProperty(String propertyName, boolean defaultValue) {
        String value = getProperty(propertyName);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Get the current environment name
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * Loads default properties from application.properties
     */
    private void loadDefaultProperties() {
        try {
            properties.putAll(PropertyManager.loadProperties("config/application.properties"));
            logger.debug("Loaded default properties");
        } catch (Exception e) {
            logger.error("Failed to load default properties", e);
        }
    }

    /**
     * Detects environment from system property or falls back to "qa"
     */
    private void detectEnvironment() {
        // Check CLI arguments first
        if (cliProperties.containsKey("env")) {
            environment = cliProperties.get("env");
        } else {
            // Then check system properties
            environment = System.getProperty("env", "qa");
        }
        logger.info("Using environment: {}", environment);
    }

    /**
     * Loads environment-specific properties from application-{env}.properties
     */
    private void loadEnvironmentProperties() {
        String envPropFile = "config/application-" + environment + ".properties";
        try {
            Properties envProperties = PropertyManager.loadProperties(envPropFile);
            properties.putAll(envProperties);
            logger.debug("Loaded environment-specific properties for: {}", environment);
        } catch (Exception e) {
            logger.info("No environment-specific properties found for: {}", environment);
        }
    }

    /**
     * Loads system properties as CLI properties
     */
    private void loadCLIProperties() {
        Properties systemProps = System.getProperties();
        for (String propName : systemProps.stringPropertyNames()) {
            if (propName.startsWith("override.")) {
                cliProperties.put(propName, systemProps.getProperty(propName));
            }
        }
        logger.debug("Loaded command-line properties");
    }
}
