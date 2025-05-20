package db.cosmos;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.LoggerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Client for interacting with Azure Cosmos DB.
 * Provides methods for querying and manipulating data in Cosmos DB containers.
 */
public class CosmosDBClient {
    private static final Logger logger = LogManager.getLogger(CosmosDBClient.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final CosmosClient cosmosClient;
    private final CosmosDatabase database;
    private final CosmosContainer container;
    private final int queryTimeout;
    
    /**
     * Creates a CosmosDBClient with the specified connection properties.
     *
     * @param connectionString Azure Cosmos DB connection string
     * @param databaseName Database name
     * @param containerName Container name
     * @param queryTimeout Query timeout in milliseconds
     */
    public CosmosDBClient(String connectionString, String databaseName, String containerName, int queryTimeout) {
        logger.info("Initializing Cosmos DB client for database: {}, container: {}", databaseName, containerName);
        this.queryTimeout = queryTimeout;
        
        // Since direct connectionString method is not available,
        // we'll handle it in a way compatible with the Azure SDK version
        // used in the project
        logger.info("Initializing CosmosDB client with connection params");
        
        // Using a different approach that's compatible with the SDK version
        this.cosmosClient = new CosmosClientBuilder()
                .endpoint(extractEndpoint(connectionString))
                .key(extractKey(connectionString))
                .consistencyLevel(ConsistencyLevel.SESSION)
                .buildClient();
        
        this.database = cosmosClient.getDatabase(databaseName);
        this.container = database.getContainer(containerName);
        logger.info("Cosmos DB client initialized successfully");
    }
    
    /**
     * Creates a CosmosDBClient with the specified connection properties.
     *
     * @param connectionString Azure Cosmos DB connection string
     * @param databaseName Database name
     * @param containerName Container name
     */
    public CosmosDBClient(String connectionString, String databaseName, String containerName) {
        this(connectionString, databaseName, containerName, 60000);
    }
    
    /**
     * Executes a SQL query against the Cosmos DB container.
     *
     * @param query SQL query string
     * @return List of JSON objects representing the query results
     */
    @Step("Execute Cosmos DB query: {query}")
    public List<ObjectNode> executeQuery(String query) {
        logger.debug("Executing Cosmos DB query: {}", query);
        LoggerUtils.logDatabaseQuery("COSMOS", query);
        
        long startTime = System.currentTimeMillis();
        
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setMaxBufferedItemCount(100);
        
        List<ObjectNode> results = new ArrayList<>();
        CosmosPagedIterable<ObjectNode> queryResults = container.queryItems(query, options, ObjectNode.class);
        
        // Collect results
        for (ObjectNode item : queryResults) {
            results.add(item);
        }
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        logger.debug("Cosmos DB query returned {} results in {}ms", results.size(), executionTime);
        LoggerUtils.logDatabaseResult("COSMOS", results.size(), executionTime);
        
        return results;
    }
    
    /**
     * Executes a SQL query and returns the results as a JSON array.
     *
     * @param query SQL query string
     * @return JSON array containing the query results
     */
    public ArrayNode executeQueryAsJsonArray(String query) {
        List<ObjectNode> results = executeQuery(query);
        ArrayNode arrayNode = objectMapper.createArrayNode();
        
        for (ObjectNode item : results) {
            arrayNode.add(item);
        }
        
        return arrayNode;
    }
    
    /**
     * Retrieves a document by its ID.
     *
     * @param id Document ID
     * @return JSON object representing the document, or null if not found
     */
    @Step("Get Cosmos DB document by ID: {id}")
    public ObjectNode getDocumentById(String id) {
        logger.debug("Getting Cosmos DB document with ID: {}", id);
        
        String query = "SELECT * FROM c WHERE c.id = '" + id + "'";
        List<ObjectNode> results = executeQuery(query);
        
        if (results.isEmpty()) {
            logger.debug("No document found with ID: {}", id);
            return null;
        }
        
        return results.get(0);
    }
    
    /**
     * Compares the query results against an expected JSON array.
     *
     * @param query SQL query string
     * @param expectedJson JSON array string containing the expected results
     * @return true if the results match the expected JSON, false otherwise
     */
    public boolean assertQueryResultsMatchExpected(String query, String expectedJson) {
        try {
            ArrayNode actualResults = executeQueryAsJsonArray(query);
            JsonNode expectedResults = objectMapper.readTree(expectedJson);
            
            if (!expectedResults.isArray()) {
                logger.error("Expected JSON is not an array");
                return false;
            }
            
            if (actualResults.size() != expectedResults.size()) {
                logger.error("Result count mismatch. Expected: {}, Actual: {}", 
                        expectedResults.size(), actualResults.size());
                return false;
            }
            
            // Compare each element in the arrays
            // Note: This is a simple comparison and may not handle cases where the order of results differs
            for (int i = 0; i < expectedResults.size(); i++) {
                if (!actualResults.get(i).equals(expectedResults.get(i))) {
                    logger.error("Result at index {} does not match expected", i);
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Error comparing query results with expected JSON", e);
            return false;
        }
    }
    
    /**
     * Closes the Cosmos DB client and releases resources.
     */
    public void close() {
        if (cosmosClient != null) {
            logger.info("Closing Cosmos DB client");
            cosmosClient.close();
        }
    }
    
    /**
     * Extracts the endpoint from a Cosmos DB connection string.
     *
     * @param connectionString Connection string in format "AccountEndpoint=...;AccountKey=..."
     * @return Extracted endpoint URL
     */
    private String extractEndpoint(String connectionString) {
        String endpoint = "";
        if (connectionString != null && connectionString.contains("AccountEndpoint=")) {
            int startIndex = connectionString.indexOf("AccountEndpoint=") + "AccountEndpoint=".length();
            int endIndex = connectionString.indexOf(';', startIndex);
            if (endIndex > startIndex) {
                endpoint = connectionString.substring(startIndex, endIndex);
            } else {
                endpoint = connectionString.substring(startIndex);
            }
        }
        return endpoint;
    }
    
    /**
     * Extracts the key from a Cosmos DB connection string.
     *
     * @param connectionString Connection string in format "AccountEndpoint=...;AccountKey=..."
     * @return Extracted account key
     */
    private String extractKey(String connectionString) {
        String key = "";
        if (connectionString != null && connectionString.contains("AccountKey=")) {
            int startIndex = connectionString.indexOf("AccountKey=") + "AccountKey=".length();
            int endIndex = connectionString.indexOf(';', startIndex);
            if (endIndex > startIndex) {
                key = connectionString.substring(startIndex, endIndex);
            } else {
                key = connectionString.substring(startIndex);
            }
        }
        return key;
    }
}
