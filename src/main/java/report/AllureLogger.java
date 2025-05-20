package report;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

/**
 * Utility class for logging information to Allure reports.
 */
public class AllureLogger {
    private static final Logger logger = LogManager.getLogger(AllureLogger.class);
    
    private AllureLogger() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Logs a step in the Allure report.
     *
     * @param stepName Name of the step
     */
    public static void logStep(String stepName) {
        logger.debug("Logging step to Allure: {}", stepName);
        Allure.step(stepName);
    }
    
    /**
     * Logs a step with result status in the Allure report.
     *
     * @param stepName Name of the step
     * @param status Status of the step
     */
    public static void logStep(String stepName, Status status) {
        logger.debug("Logging step to Allure: {} with status: {}", stepName, status);
        
        StepResult result = new StepResult()
                .setName(stepName)
                .setStatus(status);
        
        Allure.getLifecycle().startStep(UUID.randomUUID().toString(), result);
        Allure.getLifecycle().stopStep();
    }
    
    /**
     * Logs an API request in the Allure report.
     *
     * @param method HTTP method
     * @param url Request URL
     * @param headers Request headers
     * @param body Request body
     */
    public static void logRequest(String method, String url, String headers, String body) {
        logStep("Request: " + method + " " + url);
        
        StringBuilder content = new StringBuilder();
        content.append("Method: ").append(method).append("\n");
        content.append("URL: ").append(url).append("\n");
        content.append("Headers: ").append(headers).append("\n");
        
        if (body != null && !body.isEmpty()) {
            content.append("Body: ").append(body).append("\n");
        }
        
        Allure.addAttachment("Request Details", "text/plain", content.toString());
        logger.debug("Logged request to Allure");
    }
    
    /**
     * Logs an API response in the Allure report.
     *
     * @param statusCode HTTP status code
     * @param headers Response headers
     * @param body Response body
     * @param responseTimeMs Response time in milliseconds
     */
    public static void logResponse(int statusCode, String headers, String body, long responseTimeMs) {
        logStep("Response: Status " + statusCode + " (" + responseTimeMs + "ms)");
        
        StringBuilder content = new StringBuilder();
        content.append("Status Code: ").append(statusCode).append("\n");
        content.append("Response Time: ").append(responseTimeMs).append("ms\n");
        content.append("Headers: ").append(headers).append("\n");
        
        if (body != null && !body.isEmpty()) {
            content.append("Body: ").append(body).append("\n");
            
            // Determine appropriate content type for the body attachment
            String contentType = "text/plain";
            if (body.trim().startsWith("{") || body.trim().startsWith("[")) {
                contentType = "application/json";
            } else if (body.trim().startsWith("<")) {
                contentType = "application/xml";
            }
            
            // Attach the body separately with appropriate content type
            Allure.addAttachment("Response Body", contentType, body);
        }
        
        Allure.addAttachment("Response Details", "text/plain", content.toString());
        logger.debug("Logged response to Allure");
    }
    
    /**
     * Logs a database query in the Allure report.
     *
     * @param queryType Type of database query
     * @param query Query string
     * @param params Query parameters
     */
    public static void logDatabaseQuery(String queryType, String query, Object... params) {
        logStep("Database Query: " + queryType);
        
        StringBuilder content = new StringBuilder();
        content.append("Query Type: ").append(queryType).append("\n");
        content.append("Query: ").append(query).append("\n");
        
        if (params != null && params.length > 0) {
            content.append("Parameters: ");
            for (int i = 0; i < params.length; i++) {
                content.append("[").append(i).append("] = ");
                content.append(params[i] == null ? "NULL" : params[i].toString());
                if (i < params.length - 1) {
                    content.append(", ");
                }
            }
            content.append("\n");
        }
        
        Allure.addAttachment("Database Query", "text/plain", content.toString());
        logger.debug("Logged database query to Allure");
    }
    
    /**
     * Logs database query results in the Allure report.
     *
     * @param queryType Type of database query
     * @param resultCount Number of results returned
     * @param resultDetails Result details
     */
    public static void logDatabaseResults(String queryType, int resultCount, String resultDetails) {
        logStep("Database Results: " + queryType + " (" + resultCount + " records)");
        
        StringBuilder content = new StringBuilder();
        content.append("Result Count: ").append(resultCount).append("\n");
        
        if (resultDetails != null && !resultDetails.isEmpty()) {
            content.append("Results: \n").append(resultDetails);
        }
        
        Allure.addAttachment("Database Results", "text/plain", content.toString());
        logger.debug("Logged database results to Allure");
    }
    
    /**
     * Logs an error in the Allure report.
     *
     * @param errorMessage Error message
     * @param throwable Exception or error
     */
    public static void logError(String errorMessage, Throwable throwable) {
        logStep("Error: " + errorMessage, Status.FAILED);
        
        StringBuilder content = new StringBuilder();
        content.append("Error: ").append(errorMessage).append("\n");
        
        if (throwable != null) {
            content.append("Exception: ").append(throwable.getClass().getName()).append("\n");
            content.append("Message: ").append(throwable.getMessage()).append("\n");
            
            content.append("Stack Trace: \n");
            for (StackTraceElement element : throwable.getStackTrace()) {
                content.append("    ").append(element.toString()).append("\n");
            }
        }
        
        Allure.addAttachment("Error Details", "text/plain", content.toString());
        logger.debug("Logged error to Allure");
    }
    
    /**
     * Logs environment information in the Allure report.
     *
     * @param environment Environment name
     * @param apiBaseUrl API base URL
     * @param additionalInfo Additional environment information
     */
    public static void logEnvironmentInfo(String environment, String apiBaseUrl, String additionalInfo) {
        // Log environment info as an attachment instead
        StringBuilder envInfo = new StringBuilder();
        envInfo.append("Environment: ").append(environment).append("\n");
        envInfo.append("API Base URL: ").append(apiBaseUrl).append("\n");
        envInfo.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        envInfo.append("OS: ").append(System.getProperty("os.name")).append(" ")
               .append(System.getProperty("os.version")).append("\n");
        
        Allure.addAttachment("Environment Information", "text/plain", envInfo.toString());
        
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            Allure.addAttachment("Additional Environment Info", "text/plain", additionalInfo);
        }
        
        logger.debug("Logged environment info to Allure");
    }
}
