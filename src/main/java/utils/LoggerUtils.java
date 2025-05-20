package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

/**
 * Utility class for logging framework operations
 */
public class LoggerUtils {
    private static final Logger logger = LogManager.getLogger(LoggerUtils.class);

    private LoggerUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Set up thread context for logging
     * @param testName Name of the test being executed
     */
    public static void setupThreadContext(String testName) {
        String threadId = String.valueOf(Thread.currentThread().getId());
        ThreadContext.put("testName", testName);
        ThreadContext.put("threadId", threadId);
        logger.info("Setting up logging context for test: {}, thread: {}", testName, threadId);
    }

    /**
     * Clear thread context data
     */
    public static void clearThreadContext() {
        logger.debug("Clearing thread context data");
        ThreadContext.clearAll();
    }

    /**
     * Log REST API request details
     * @param method HTTP method
     * @param url Request URL
     * @param headers Request headers
     * @param body Request body (optional)
     */
    public static void logRequest(String method, String url, String headers, String body) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("\n-------- REQUEST --------\n");
        logBuilder.append("Method: ").append(method).append("\n");
        logBuilder.append("URL: ").append(url).append("\n");
        logBuilder.append("Headers: ").append(headers).append("\n");
        
        if (body != null && !body.isEmpty()) {
            logBuilder.append("Body: ").append(body).append("\n");
        }
        logBuilder.append("-------------------------\n");
        
        logger.info(logBuilder.toString());
    }

    /**
     * Log REST API response details
     * @param statusCode HTTP status code
     * @param headers Response headers
     * @param body Response body
     * @param responseTimeMs Response time in milliseconds
     */
    public static void logResponse(int statusCode, String headers, String body, long responseTimeMs) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("\n-------- RESPONSE --------\n");
        logBuilder.append("Status Code: ").append(statusCode).append("\n");
        logBuilder.append("Response Time: ").append(responseTimeMs).append("ms\n");
        logBuilder.append("Headers: ").append(headers).append("\n");
        
        if (body != null && !body.isEmpty()) {
            // Limit body size in logs if too large
            String truncatedBody = body.length() > 1000 ? body.substring(0, 1000) + "... [truncated]" : body;
            logBuilder.append("Body: ").append(truncatedBody).append("\n");
        }
        logBuilder.append("--------------------------\n");
        
        logger.info(logBuilder.toString());
    }

    /**
     * Log database query execution
     * @param queryType Type of query (SQL, Cosmos, etc.)
     * @param query The query being executed
     * @param parameters Query parameters (optional)
     */
    public static void logDatabaseQuery(String queryType, String query, Object... parameters) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("\n-------- DB QUERY (").append(queryType).append(") --------\n");
        logBuilder.append("Query: ").append(query).append("\n");
        
        if (parameters != null && parameters.length > 0) {
            logBuilder.append("Parameters: ");
            for (int i = 0; i < parameters.length; i++) {
                logBuilder.append("[").append(i).append("] = ");
                logBuilder.append(parameters[i] == null ? "NULL" : parameters[i].toString());
                if (i < parameters.length - 1) {
                    logBuilder.append(", ");
                }
            }
            logBuilder.append("\n");
        }
        logBuilder.append("---------------------------------------\n");
        
        logger.info(logBuilder.toString());
    }

    /**
     * Log database query results
     * @param queryType Type of query (SQL, Cosmos, etc.)
     * @param resultCount Number of results returned
     * @param executionTimeMs Execution time in milliseconds
     */
    public static void logDatabaseResult(String queryType, int resultCount, long executionTimeMs) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("\n-------- DB RESULT (").append(queryType).append(") --------\n");
        logBuilder.append("Results: ").append(resultCount).append(" records\n");
        logBuilder.append("Execution Time: ").append(executionTimeMs).append("ms\n");
        logBuilder.append("----------------------------------------\n");
        
        logger.info(logBuilder.toString());
    }

    /**
     * Log error with custom message
     * @param message Error message
     * @param t Throwable exception
     */
    public static void logError(String message, Throwable t) {
        logger.error(message, t);
    }

    /**
     * Log test step
     * @param stepDescription Description of the test step
     */
    public static void logStep(String stepDescription) {
        logger.info("\n===== STEP: {} =====", stepDescription);
    }
}
