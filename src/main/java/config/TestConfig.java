package config;

/**
 * Centralized class for accessing configuration properties used throughout the framework.
 * This acts as a facade to the ConfigurationManager providing more readable property access.
 */
public class TestConfig {
    private static final ConfigurationManager configManager = ConfigurationManager.getInstance();

    // API configuration
    public static String getApiBaseUrl() {
        return configManager.getProperty("api.base.url", "http://localhost:8080");
    }

    public static int getApiRequestTimeout() {
        return configManager.getIntProperty("api.request.timeout", 30000);
    }

    public static int getApiConnectionTimeout() {
        return configManager.getIntProperty("api.connection.timeout", 10000);
    }

    public static int getApiRetryCount() {
        return configManager.getIntProperty("api.retry.count", 3);
    }

    public static boolean isApiLoggingEnabled() {
        return configManager.getBooleanProperty("api.logging.enabled", true);
    }

    // SQL Database configuration
    public static String getSqlJdbcUrl() {
        return configManager.getProperty("db.sql.server");
    }

    public static String getSqlUsername() {
        return configManager.getProperty("db.sql.username");
    }

    public static String getSqlPassword() {
        return configManager.getProperty("db.sql.password");
    }

    public static int getSqlConnectionPoolSize() {
        return configManager.getIntProperty("db.sql.pool.size", 10);
    }

    public static int getSqlConnectionTimeout() {
        return configManager.getIntProperty("db.sql.connection.timeout", 30000);
    }

    public static int getSqlMaxLifetime() {
        return configManager.getIntProperty("db.sql.max.lifetime", 1800000);
    }

    // Cosmos DB configuration
    public static String getCosmosConnectionString() {
        return configManager.getProperty("db.cosmos.connection.string");
    }

    public static String getCosmosDatabaseName() {
        return configManager.getProperty("db.cosmos.database");
    }
    
    public static String getCosmosContainerName() {
        return configManager.getProperty("db.cosmos.container");
    }

    public static int getCosmosRequestTimeout() {
        return configManager.getIntProperty("db.cosmos.request.timeout", 60000);
    }

    // Allure reporting configuration
    public static String getAllureResultsDir() {
        return configManager.getProperty("report.allure.output.dir", "target/allure-results");
    }

    // Logging configuration
    public static String getLogLevel() {
        return configManager.getProperty("log.level", "INFO");
    }

    // Environment configuration
    public static String getEnvironment() {
        return configManager.getEnvironment();
    }

    // Authentication configuration
    public static String getApiAuthToken() {
        return configManager.getProperty("api.auth.token");
    }

    public static String getApiUsername() {
        return configManager.getProperty("api.auth.username");
    }

    public static String getApiPassword() {
        return configManager.getProperty("api.auth.password");
    }

    public static String getOAuthClientId() {
        return configManager.getProperty("api.oauth.client.id");
    }

    public static String getOAuthClientSecret() {
        return configManager.getProperty("api.oauth.client.secret");
    }

    public static String getOAuthTokenUrl() {
        return configManager.getProperty("api.oauth.token.url");
    }
}
