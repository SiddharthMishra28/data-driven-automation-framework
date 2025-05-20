package utils;

import db.cosmos.CosmosDBClient;
import db.sql.SqlClient;
import rest.RestApiClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-local context to store per-thread resources for parallel test execution.
 * This class manages thread-specific instances of clients and other resources.
 */
public class ThreadLocalContext {
    private static final ThreadLocal<Map<String, Object>> threadContextMap = ThreadLocal.withInitial(HashMap::new);
    
    private static final String REST_CLIENT_KEY = "restClient";
    private static final String SQL_CLIENT_KEY = "sqlClient";
    private static final String COSMOS_CLIENT_KEY = "cosmosClient";
    private static final String TEST_NAME_KEY = "testName";

    private ThreadLocalContext() {
        // Private constructor to prevent instantiation
    }

    /**
     * Stores the REST API client in the thread context
     * @param restApiClient RestApiClient instance
     */
    public static void setRestApiClient(RestApiClient restApiClient) {
        threadContextMap.get().put(REST_CLIENT_KEY, restApiClient);
    }

    /**
     * Retrieves the REST API client from the thread context
     * @return RestApiClient instance
     */
    public static RestApiClient getRestApiClient() {
        return (RestApiClient) threadContextMap.get().get(REST_CLIENT_KEY);
    }

    /**
     * Stores the SQL client in the thread context
     * @param sqlClient SqlClient instance
     */
    public static void setSqlClient(SqlClient sqlClient) {
        threadContextMap.get().put(SQL_CLIENT_KEY, sqlClient);
    }

    /**
     * Retrieves the SQL client from the thread context
     * @return SqlClient instance
     */
    public static SqlClient getSqlClient() {
        return (SqlClient) threadContextMap.get().get(SQL_CLIENT_KEY);
    }

    /**
     * Stores the Cosmos DB client in the thread context
     * @param cosmosDBClient CosmosDBClient instance
     */
    public static void setCosmosDBClient(CosmosDBClient cosmosDBClient) {
        threadContextMap.get().put(COSMOS_CLIENT_KEY, cosmosDBClient);
    }

    /**
     * Retrieves the Cosmos DB client from the thread context
     * @return CosmosDBClient instance
     */
    public static CosmosDBClient getCosmosDBClient() {
        return (CosmosDBClient) threadContextMap.get().get(COSMOS_CLIENT_KEY);
    }

    /**
     * Stores the current test name in the thread context
     * @param testName Name of the test being executed
     */
    public static void setTestName(String testName) {
        threadContextMap.get().put(TEST_NAME_KEY, testName);
    }

    /**
     * Retrieves the current test name from the thread context
     * @return Current test name
     */
    public static String getTestName() {
        return (String) threadContextMap.get().get(TEST_NAME_KEY);
    }

    /**
     * Stores a custom object in the thread context
     * @param key Key to store the object under
     * @param value Object to store
     */
    public static void put(String key, Object value) {
        threadContextMap.get().put(key, value);
    }

    /**
     * Retrieves a custom object from the thread context
     * @param key Key of the object to retrieve
     * @return Object stored under the key
     */
    public static Object get(String key) {
        return threadContextMap.get().get(key);
    }

    /**
     * Clears all resources from the thread context
     */
    public static void clear() {
        // Close any resources that need closing
        RestApiClient restApiClient = getRestApiClient();
        if (restApiClient != null) {
            // If RestApiClient has resources to close, do it here
        }

        SqlClient sqlClient = getSqlClient();
        if (sqlClient != null) {
            sqlClient.close();
        }

        CosmosDBClient cosmosDBClient = getCosmosDBClient();
        if (cosmosDBClient != null) {
            cosmosDBClient.close();
        }

        // Clear all entries
        threadContextMap.get().clear();
    }

    /**
     * Removes the thread-local variable to prevent memory leaks
     */
    public static void remove() {
        threadContextMap.remove();
    }
}
