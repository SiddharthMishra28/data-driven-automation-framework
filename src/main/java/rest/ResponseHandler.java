package rest;

import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.JsonUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Handles response processing and provides utility methods for working with REST API responses.
 */
public class ResponseHandler {
    private static final Logger logger = LogManager.getLogger(ResponseHandler.class);
    private final Response response;

    /**
     * Creates a ResponseHandler for the given Response.
     *
     * @param response Response object from REST Assured
     */
    public ResponseHandler(Response response) {
        this.response = response;
    }

    /**
     * Gets the HTTP status code from the response.
     *
     * @return HTTP status code
     */
    public int getStatusCode() {
        return response.getStatusCode();
    }

    /**
     * Gets the response body as a string.
     *
     * @return Response body as string
     */
    public String getBodyAsString() {
        return response.getBody().asString();
    }

    /**
     * Gets a header value from the response.
     *
     * @param headerName Header name
     * @return Header value or null if not found
     */
    public String getHeader(String headerName) {
        return response.getHeader(headerName);
    }

    /**
     * Gets all headers from the response.
     *
     * @return Map of header names to values
     */
    public Map<String, String> getHeaders() {
        return response.getHeaders().asList().stream()
                .collect(java.util.stream.Collectors.toMap(
                        io.restassured.http.Header::getName,
                        io.restassured.http.Header::getValue,
                        (existingValue, newValue) -> existingValue + ", " + newValue // Handle duplicate headers
                ));
    }

    /**
     * Gets the response time in milliseconds.
     *
     * @return Response time in ms
     */
    public long getResponseTime() {
        return response.getTime();
    }

    /**
     * Gets the content type of the response.
     *
     * @return Content type
     */
    public String getContentType() {
        return response.getContentType();
    }

    /**
     * Extracts a string value from the JSON response body using JsonPath.
     *
     * @param jsonPath JsonPath expression
     * @return String value or null if not found
     */
    public String getJsonValue(String jsonPath) {
        return JsonUtils.extractStringFromJson(getBodyAsString(), jsonPath);
    }

    /**
     * Extracts an integer value from the JSON response body using JsonPath.
     *
     * @param jsonPath JsonPath expression
     * @return Integer value or null if not found
     */
    public Integer getJsonInteger(String jsonPath) {
        return JsonUtils.extractIntFromJson(getBodyAsString(), jsonPath);
    }

    /**
     * Extracts a boolean value from the JSON response body using JsonPath.
     *
     * @param jsonPath JsonPath expression
     * @return Boolean value or null if not found
     */
    public Boolean getJsonBoolean(String jsonPath) {
        return JsonUtils.extractBooleanFromJson(getBodyAsString(), jsonPath);
    }

    /**
     * Extracts a list of strings from the JSON response body using JsonPath.
     *
     * @param jsonPath JsonPath expression
     * @return List of strings or empty list if not found
     */
    public List<String> getJsonStringList(String jsonPath) {
        return JsonUtils.extractStringList(getBodyAsString(), jsonPath);
    }

    /**
     * Extracts a map from the JSON response body using JsonPath.
     *
     * @param jsonPath JsonPath expression
     * @return Map or empty map if not found
     */
    public Map<String, Object> getJsonMap(String jsonPath) {
        return JsonUtils.extractMap(getBodyAsString(), jsonPath);
    }

    /**
     * Checks if a field exists in the JSON response body.
     *
     * @param jsonPath JsonPath expression
     * @return true if the field exists, false otherwise
     */
    public boolean hasJsonField(String jsonPath) {
        return JsonUtils.isJsonFieldPresent(getBodyAsString(), jsonPath);
    }

    /**
     * Saves the response body to a file.
     *
     * @param directoryPath Directory path to save to
     * @param fileNamePrefix Prefix for the file name
     * @return File object for the saved file
     */
    public File saveResponseToFile(String directoryPath, String fileNamePrefix) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = fileNamePrefix + "_" + timestamp + ".json";
            Path filePath = Paths.get(directoryPath, fileName);
            
            // Create directory if it doesn't exist
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            File outputFile = filePath.toFile();
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(getBodyAsString().getBytes());
            }
            
            logger.info("Response saved to file: {}", outputFile.getAbsolutePath());
            return outputFile;
        } catch (IOException e) {
            logger.error("Failed to save response to file", e);
            throw new RuntimeException("Failed to save response to file", e);
        }
    }

    /**
     * Gets the underlying Response object.
     *
     * @return Response object
     */
    public Response getResponse() {
        return response;
    }
}
