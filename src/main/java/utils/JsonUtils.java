package utils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility class for working with JSON data using Jayway JsonPath.
 */
public class JsonUtils {
    private static final Logger logger = LogManager.getLogger(JsonUtils.class);
    
    // Configure JsonPath with DEFAULT_PATH_LEAF_TO_NULL to handle missing leaf nodes gracefully
    private static final Configuration JSON_PATH_CONFIG = Configuration.defaultConfiguration()
            .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);

    private JsonUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Parse JSON string into a DocumentContext for efficient traversal
     *
     * @param json JSON string to parse
     * @return DocumentContext for the parsed JSON
     */
    public static DocumentContext parseJson(String json) {
        try {
            return JsonPath.using(JSON_PATH_CONFIG).parse(json);
        } catch (Exception e) {
            logger.error("Error parsing JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    /**
     * Check if a field exists in the JSON
     *
     * @param json JSON string to check
     * @param jsonPath JsonPath expression to evaluate
     * @return true if the field exists, false otherwise
     */
    public static boolean isJsonFieldPresent(String json, String jsonPath) {
        try {
            Object value = JsonPath.using(JSON_PATH_CONFIG).parse(json).read(jsonPath);
            return value != null;
        } catch (PathNotFoundException e) {
            return false;
        } catch (Exception e) {
            logger.error("Error checking JSON field presence: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Extract a string value from JSON
     *
     * @param json JSON string to extract from
     * @param jsonPath JsonPath expression to evaluate
     * @return String value at the specified path, or null if not found
     */
    public static String extractStringFromJson(String json, String jsonPath) {
        try {
            Object value = JsonPath.using(JSON_PATH_CONFIG).parse(json).read(jsonPath);
            return value != null ? value.toString() : null;
        } catch (PathNotFoundException e) {
            logger.debug("JSON path not found: {}", jsonPath);
            return null;
        } catch (Exception e) {
            logger.error("Error extracting string from JSON: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extract an integer value from JSON
     *
     * @param json JSON string to extract from
     * @param jsonPath JsonPath expression to evaluate
     * @return Integer value at the specified path, or null if not found or not an integer
     */
    public static Integer extractIntFromJson(String json, String jsonPath) {
        try {
            return JsonPath.using(JSON_PATH_CONFIG).parse(json).read(jsonPath, Integer.class);
        } catch (PathNotFoundException e) {
            logger.debug("JSON path not found: {}", jsonPath);
            return null;
        } catch (Exception e) {
            logger.error("Error extracting integer from JSON: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extract a boolean value from JSON
     *
     * @param json JSON string to extract from
     * @param jsonPath JsonPath expression to evaluate
     * @return Boolean value at the specified path, or null if not found or not a boolean
     */
    public static Boolean extractBooleanFromJson(String json, String jsonPath) {
        try {
            return JsonPath.using(JSON_PATH_CONFIG).parse(json).read(jsonPath, Boolean.class);
        } catch (PathNotFoundException e) {
            logger.debug("JSON path not found: {}", jsonPath);
            return null;
        } catch (Exception e) {
            logger.error("Error extracting boolean from JSON: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extract a double value from JSON
     *
     * @param json JSON string to extract from
     * @param jsonPath JsonPath expression to evaluate
     * @return Double value at the specified path, or null if not found or not a number
     */
    public static Double extractDoubleFromJson(String json, String jsonPath) {
        try {
            return JsonPath.using(JSON_PATH_CONFIG).parse(json).read(jsonPath, Double.class);
        } catch (PathNotFoundException e) {
            logger.debug("JSON path not found: {}", jsonPath);
            return null;
        } catch (Exception e) {
            logger.error("Error extracting double from JSON: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extract a list of strings from JSON
     *
     * @param json JSON string to extract from
     * @param jsonPath JsonPath expression to evaluate (should point to an array)
     * @return List of strings, or empty list if not found
     */
    public static List<String> extractStringList(String json, String jsonPath) {
        try {
            List<String> result = JsonPath.using(JSON_PATH_CONFIG).parse(json).read(jsonPath);
            return result != null ? result : Collections.emptyList();
        } catch (PathNotFoundException e) {
            logger.debug("JSON path not found: {}", jsonPath);
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error extracting string list from JSON: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Extract a map from JSON
     *
     * @param json JSON string to extract from
     * @param jsonPath JsonPath expression to evaluate (should point to an object)
     * @return Map representing the JSON object, or empty map if not found
     */
    public static Map<String, Object> extractMap(String json, String jsonPath) {
        try {
            Map<String, Object> result = JsonPath.using(JSON_PATH_CONFIG).parse(json).read(jsonPath);
            return result != null ? result : Collections.emptyMap();
        } catch (PathNotFoundException e) {
            logger.debug("JSON path not found: {}", jsonPath);
            return Collections.emptyMap();
        } catch (Exception e) {
            logger.error("Error extracting map from JSON: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * Extract a list of maps from JSON
     *
     * @param json JSON string to extract from
     * @param jsonPath JsonPath expression to evaluate (should point to an array of objects)
     * @return List of maps, or empty list if not found
     */
    public static List<Map<String, Object>> extractMapList(String json, String jsonPath) {
        try {
            List<Map<String, Object>> result = JsonPath.using(JSON_PATH_CONFIG).parse(json).read(jsonPath);
            return result != null ? result : Collections.emptyList();
        } catch (PathNotFoundException e) {
            logger.debug("JSON path not found: {}", jsonPath);
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error extracting map list from JSON: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Compare two JSON strings for equality (ignoring field order)
     *
     * @param expected Expected JSON string
     * @param actual Actual JSON string
     * @return true if the JSON structures are equal, false otherwise
     */
    public static boolean areJsonEqual(String expected, String actual) {
        try {
            Object expectedObj = JsonPath.parse(expected).json();
            Object actualObj = JsonPath.parse(actual).json();
            return expectedObj.equals(actualObj);
        } catch (Exception e) {
            logger.error("Error comparing JSON: {}", e.getMessage(), e);
            return false;
        }
    }
}
