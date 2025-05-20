package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import db.sql.SqlClient;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Utility class for handling test data from various sources like CSV, JSON, and Database.
 * Supports data-driven testing by providing TestNG data providers.
 */
public class DataProviderUtils {
    private static final Logger logger = LogManager.getLogger(DataProviderUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final SparkClient sparkClient = new SparkClient();
    
    /**
     * Private constructor to prevent instantiation
     */
    private DataProviderUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Load test data from a CSV file.
     *
     * @param filePath Path to the CSV file
     * @return Object[][] Data array for TestNG data provider
     */
    public static Object[][] loadCsvData(String filePath) {
        logger.info("Loading test data from CSV file: {}", filePath);
        List<Map<String, String>> dataMapList = new ArrayList<>();
        
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : csvParser) {
                Map<String, String> rowMap = new HashMap<>();
                csvParser.getHeaderNames().forEach(header -> 
                    rowMap.put(header, record.get(header))
                );
                dataMapList.add(rowMap);
            }
            
            logger.info("Successfully loaded {} records from CSV file", dataMapList.size());
            return convertToObjectArray(dataMapList);
            
        } catch (IOException e) {
            logger.error("Error loading CSV data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load CSV data", e);
        }
    }
    
    /**
     * Load test data from a JSON file.
     * The JSON file should contain an array of objects.
     *
     * @param filePath Path to the JSON file
     * @return Object[][] Data array for TestNG data provider
     */
    public static Object[][] loadJsonData(String filePath) {
        logger.info("Loading test data from JSON file: {}", filePath);
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JsonNode rootNode = objectMapper.readTree(content);
            
            if (!rootNode.isArray()) {
                throw new IllegalArgumentException("JSON content must be an array of objects");
            }
            
            List<Map<String, Object>> dataMapList = StreamSupport.stream(rootNode.spliterator(), false)
                .map(node -> {
                    try {
                        return objectMapper.convertValue(node, new TypeReference<Map<String, Object>>() {});
                    } catch (Exception e) {
                        logger.error("Error converting JSON node to Map: {}", e.getMessage(), e);
                        return new HashMap<String, Object>();
                    }
                })
                .collect(Collectors.toList());
            
            logger.info("Successfully loaded {} records from JSON file", dataMapList.size());
            return convertToObjectArray(dataMapList);
            
        } catch (IOException e) {
            logger.error("Error loading JSON data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load JSON data", e);
        }
    }
    
    /**
     * Load test data from a database query.
     *
     * @param sqlClient SqlClient instance
     * @param query SQL query to execute
     * @param params Query parameters (optional)
     * @return Object[][] Data array for TestNG data provider
     */
    public static Object[][] loadDatabaseData(SqlClient sqlClient, String query, Object... params) {
        logger.info("Loading test data from database with query: {}", query);
        try {
            if (sqlClient == null) {
                logger.error("SqlClient is null. Database connection is not available.");
                throw new IllegalStateException("Database connection is not available");
            }
            
            List<Map<String, Object>> results = sqlClient.executeQuery(query, params);
            logger.info("Successfully loaded {} records from database", results.size());
            return convertToObjectArray(results);
            
        } catch (Exception e) {
            logger.error("Error loading database data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load database data", e);
        }
    }
    
    /**
     * Convert a list of maps to an Object[][] array for TestNG data provider.
     *
     * @param dataMapList List of maps containing test data
     * @return Object[][] Data array for TestNG data provider
     */
    private static Object[][] convertToObjectArray(List<?> dataMapList) {
        Object[][] data = new Object[dataMapList.size()][1];
        for (int i = 0; i < dataMapList.size(); i++) {
            data[i][0] = dataMapList.get(i);
        }
        return data;
    }

    /**
     * Validate batch file data against database records
     *
     * @param batchFilePath Path to the batch file
     * @param sqlClient SqlClient instance
     * @param query SQL query to fetch comparison data
     * @return boolean indicating if data matches
     */
    public static boolean validateBatchFileAgainstDatabase(String batchFilePath, SqlClient sqlClient, String query) {
        logger.info("Validating batch file against database: {}", batchFilePath);
        
        try {
            // Read batch file based on extension
            Dataset<Row> batchData;
            if (batchFilePath.endsWith(".csv")) {
                batchData = sparkClient.readCsv(batchFilePath);
            } else if (batchFilePath.endsWith(".json")) {
                batchData = sparkClient.readJson(batchFilePath);
            } else if (batchFilePath.endsWith(".parquet")) {
                batchData = sparkClient.readParquet(batchFilePath);
            } else {
                throw new IllegalArgumentException("Unsupported file format");
            }

            // Get database data
            List<Map<String, Object>> dbResults = sqlClient.executeQuery(query);
            Dataset<Row> dbData = sparkClient.convertSqlResultToDataframe(dbResults);

            // Compare datasets
            boolean matches = sparkClient.compareDataframes(batchData, dbData);
            logger.info("Batch file validation result: {}", matches ? "MATCH" : "MISMATCH");
            return matches;

        } catch (Exception e) {
            logger.error("Error validating batch file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to validate batch file", e);
        }
    }

}