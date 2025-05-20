package assertions;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Custom assertion library for fluent assertions on API responses.
 * Provides a chainable API for making assertions on REST API responses.
 */
public class ApiAssertion {
    private static final Logger logger = LogManager.getLogger(ApiAssertion.class);
    protected final Response response;
    protected final List<AssertionError> assertionErrors = new ArrayList<>();
    protected boolean failFast = true;
    
    /**
     * Creates an ApiAssertion for the given Response.
     *
     * @param response Response to make assertions on
     */
    protected ApiAssertion(Response response) {
        this.response = response;
    }
    
    /**
     * Factory method to create an ApiAssertion instance.
     *
     * @param response Response to make assertions on
     * @return ApiAssertion instance
     */
    public static ApiAssertion assertThat(Response response) {
        return new ApiAssertion(response);
    }
    
    /**
     * Factory method to create a SoftApiAssertion instance (doesn't fail fast).
     *
     * @param response Response to make assertions on
     * @return SoftApiAssertion instance
     */
    public static SoftApiAssertion softly(Response response) {
        return new SoftApiAssertion(response);
    }
    
    /**
     * Sets the mode to fail fast (stops on first failure).
     *
     * @return This ApiAssertion instance
     */
    public ApiAssertion failFast() {
        this.failFast = true;
        return this;
    }
    
    /**
     * Sets the mode to collect all failures before failing.
     *
     * @return This ApiAssertion instance
     */
    public ApiAssertion collectAllFailures() {
        this.failFast = false;
        return this;
    }
    
    /**
     * Asserts that the response has the expected status code.
     *
     * @param expectedStatusCode Expected HTTP status code
     * @return This ApiAssertion instance
     */
    @Step("Assert status code is {expectedStatusCode}")
    public ApiAssertion hasStatusCode(int expectedStatusCode) {
        try {
            Assert.assertEquals(response.getStatusCode(), expectedStatusCode,
                    "Expected status code " + expectedStatusCode + " but got " + response.getStatusCode());
            logger.debug("Status code assertion passed: {}", expectedStatusCode);
        } catch (AssertionError e) {
            handleAssertionError(e);
        }
        return this;
    }
    
    /**
     * Asserts that the response status code is one of the expected codes.
     *
     * @param expectedStatusCodes Expected HTTP status codes
     * @return This ApiAssertion instance
     */
    @Step("Assert status code is one of {expectedStatusCodes}")
    public ApiAssertion hasStatusCodeIn(int... expectedStatusCodes) {
        int actualStatusCode = response.getStatusCode();
        boolean matched = false;
        
        for (int expectedCode : expectedStatusCodes) {
            if (actualStatusCode == expectedCode) {
                matched = true;
                break;
            }
        }
        
        try {
            Assert.assertTrue(matched, "Expected status code to be one of " + 
                    java.util.Arrays.toString(expectedStatusCodes) + " but got " + actualStatusCode);
            logger.debug("Status code in range assertion passed: {}", actualStatusCode);
        } catch (AssertionError e) {
            handleAssertionError(e);
        }
        
        return this;
    }
    
    /**
     * Asserts that the response has the expected header with the expected value.
     *
     * @param headerName Header name
     * @param expectedValue Expected header value
     * @return This ApiAssertion instance
     */
    @Step("Assert header {headerName} is {expectedValue}")
    public ApiAssertion hasHeader(String headerName, String expectedValue) {
        String actualValue = response.getHeader(headerName);
        
        try {
            Assert.assertEquals(actualValue, expectedValue,
                    "Expected header '" + headerName + "' to be '" + expectedValue + "' but got '" + actualValue + "'");
            logger.debug("Header assertion passed: {} = {}", headerName, expectedValue);
        } catch (AssertionError e) {
            handleAssertionError(e);
        }
        
        return this;
    }
    
    /**
     * Asserts that the response has a header with the given name.
     *
     * @param headerName Header name
     * @return This ApiAssertion instance
     */
    @Step("Assert header {headerName} exists")
    public ApiAssertion hasHeader(String headerName) {
        String actualValue = response.getHeader(headerName);
        
        try {
            Assert.assertNotNull(actualValue, "Expected header '" + headerName + "' to exist but it was not found");
            logger.debug("Header existence assertion passed: {}", headerName);
        } catch (AssertionError e) {
            handleAssertionError(e);
        }
        
        return this;
    }
    
    /**
     * Asserts that the response has a content type containing the expected value.
     *
     * @param expectedContentType Expected content type (substring)
     * @return This ApiAssertion instance
     */
    @Step("Assert content type contains {expectedContentType}")
    public ApiAssertion hasContentType(String expectedContentType) {
        String actualContentType = response.getContentType();
        
        try {
            Assert.assertTrue(actualContentType != null && actualContentType.contains(expectedContentType),
                    "Expected content type to contain '" + expectedContentType + "' but got '" + actualContentType + "'");
            logger.debug("Content type assertion passed: {}", expectedContentType);
        } catch (AssertionError e) {
            handleAssertionError(e);
        }
        
        return this;
    }
    
    /**
     * Asserts that the response time is less than the expected value.
     *
     * @param maxTime Maximum expected response time
     * @param timeUnit Time unit
     * @return This ApiAssertion instance
     */
    @Step("Assert response time is less than {maxTime} {timeUnit}")
    public ApiAssertion hasResponseTimeLessThan(long maxTime, TimeUnit timeUnit) {
        long actualTimeMs = response.getTime();
        long maxTimeMs = timeUnit.toMillis(maxTime);
        
        try {
            Assert.assertTrue(actualTimeMs < maxTimeMs,
                    "Expected response time to be less than " + maxTime + " " + timeUnit.name() + 
                    " but was " + actualTimeMs + " ms");
            logger.debug("Response time assertion passed: {} < {}", actualTimeMs, maxTimeMs);
        } catch (AssertionError e) {
            handleAssertionError(e);
        }
        
        return this;
    }
    
    /**
     * Asserts that the response body contains the expected JSON value at the given path.
     *
     * @param jsonPath JSON path expression
     * @param expectedValue Expected value
     * @return This ApiAssertion instance
     */
    @Step("Assert JSON value at path {jsonPath} equals {expectedValue}")
    public ApiAssertion hasJsonValue(String jsonPath, Object expectedValue) {
        String responseBody = response.getBody().asString();
        
        if (expectedValue instanceof String) {
            String actualValue = JsonUtils.extractStringFromJson(responseBody, jsonPath);
            try {
                Assert.assertEquals(actualValue, expectedValue,
                        "Expected JSON value at path '" + jsonPath + "' to be '" + expectedValue + 
                        "' but got '" + actualValue + "'");
                logger.debug("JSON value assertion passed: {} = {}", jsonPath, expectedValue);
            } catch (AssertionError e) {
                handleAssertionError(e);
            }
        } else if (expectedValue instanceof Integer) {
            Integer actualValue = JsonUtils.extractIntFromJson(responseBody, jsonPath);
            try {
                Assert.assertEquals(actualValue, expectedValue,
                        "Expected JSON value at path '" + jsonPath + "' to be '" + expectedValue + 
                        "' but got '" + actualValue + "'");
                logger.debug("JSON value assertion passed: {} = {}", jsonPath, expectedValue);
            } catch (AssertionError e) {
                handleAssertionError(e);
            }
        } else if (expectedValue instanceof Boolean) {
            Boolean actualValue = JsonUtils.extractBooleanFromJson(responseBody, jsonPath);
            try {
                Assert.assertEquals(actualValue, expectedValue,
                        "Expected JSON value at path '" + jsonPath + "' to be '" + expectedValue + 
                        "' but got '" + actualValue + "'");
                logger.debug("JSON value assertion passed: {} = {}", jsonPath, expectedValue);
            } catch (AssertionError e) {
                handleAssertionError(e);
            }
        } else {
            // Handle other types or use a generic approach
            String actualValueStr = JsonUtils.extractStringFromJson(responseBody, jsonPath);
            String expectedValueStr = String.valueOf(expectedValue);
            
            try {
                Assert.assertEquals(actualValueStr, expectedValueStr,
                        "Expected JSON value at path '" + jsonPath + "' to be '" + expectedValueStr + 
                        "' but got '" + actualValueStr + "'");
                logger.debug("JSON value assertion passed: {} = {}", jsonPath, expectedValue);
            } catch (AssertionError e) {
                handleAssertionError(e);
            }
        }
        
        return this;
    }
    
    /**
     * Asserts that the JSON field exists in the response body.
     *
     * @param jsonPath JSON path expression
     * @return This ApiAssertion instance
     */
    @Step("Assert JSON field at path {jsonPath} exists")
    public ApiAssertion hasJsonField(String jsonPath) {
        String responseBody = response.getBody().asString();
        boolean fieldExists = JsonUtils.isJsonFieldPresent(responseBody, jsonPath);
        
        try {
            Assert.assertTrue(fieldExists, "Expected JSON field at path '" + jsonPath + "' to exist but it was not found");
            logger.debug("JSON field existence assertion passed: {}", jsonPath);
        } catch (AssertionError e) {
            handleAssertionError(e);
        }
        
        return this;
    }
    
    /**
     * Asserts that the JSON field does not exist in the response body.
     *
     * @param jsonPath JSON path expression
     * @return This ApiAssertion instance
     */
    @Step("Assert JSON field at path {jsonPath} does not exist")
    public ApiAssertion hasNoJsonField(String jsonPath) {
        String responseBody = response.getBody().asString();
        boolean fieldExists = JsonUtils.isJsonFieldPresent(responseBody, jsonPath);
        
        try {
            Assert.assertFalse(fieldExists, "Expected JSON field at path '" + jsonPath + "' to not exist but it was found");
            logger.debug("JSON field non-existence assertion passed: {}", jsonPath);
        } catch (AssertionError e) {
            handleAssertionError(e);
        }
        
        return this;
    }
    
    /**
     * Asserts that the response body contains text matching the given substring.
     *
     * @param expectedText Text to find in the response body
     * @return This ApiAssertion instance
     */
    @Step("Assert response body contains text {expectedText}")
    public ApiAssertion bodyContains(String expectedText) {
        String responseBody = response.getBody().asString();
        
        try {
            Assert.assertTrue(responseBody.contains(expectedText),
                    "Expected response body to contain '" + expectedText + "' but it was not found");
            logger.debug("Body contains assertion passed: {}", expectedText);
        } catch (AssertionError e) {
            handleAssertionError(e);
        }
        
        return this;
    }
    
    /**
     * Asserts that the response body does not contain text matching the given substring.
     *
     * @param text Text that should not be in the response body
     * @return This ApiAssertion instance
     */
    @Step("Assert response body does not contain text {text}")
    public ApiAssertion bodyDoesNotContain(String text) {
        String responseBody = response.getBody().asString();
        
        try {
            Assert.assertFalse(responseBody.contains(text),
                    "Expected response body to not contain '" + text + "' but it was found");
            logger.debug("Body does not contain assertion passed: {}", text);
        } catch (AssertionError e) {
            handleAssertionError(e);
        }
        
        return this;
    }
    
    /**
     * Asserts that the response contains a cookie with the given name.
     *
     * @param cookieName Cookie name
     * @return This ApiAssertion instance
     */
    @Step("Assert cookie {cookieName} exists")
    public ApiAssertion hasCookie(String cookieName) {
        try {
            Assert.assertTrue(response.getCookies().containsKey(cookieName),
                    "Expected cookie '" + cookieName + "' to exist but it was not found");
            logger.debug("Cookie existence assertion passed: {}", cookieName);
        } catch (AssertionError e) {
            handleAssertionError(e);
        }
        
        return this;
    }
    
    /**
     * Asserts that the response contains a cookie with the given name and value.
     *
     * @param cookieName Cookie name
     * @param expectedValue Expected cookie value
     * @return This ApiAssertion instance
     */
    @Step("Assert cookie {cookieName} has value {expectedValue}")
    public ApiAssertion hasCookie(String cookieName, String expectedValue) {
        String actualValue = response.getCookie(cookieName);
        
        try {
            Assert.assertEquals(actualValue, expectedValue,
                    "Expected cookie '" + cookieName + "' to have value '" + expectedValue + 
                    "' but got '" + actualValue + "'");
            logger.debug("Cookie value assertion passed: {} = {}", cookieName, expectedValue);
        } catch (AssertionError e) {
            handleAssertionError(e);
        }
        
        return this;
    }
    
    /**
     * Handles assertion errors based on the fail fast setting.
     *
     * @param error AssertionError to handle
     */
    protected void handleAssertionError(AssertionError error) {
        logger.error("Assertion failed: {}", error.getMessage());
        
        if (failFast) {
            throw error;
        } else {
            assertionErrors.add(error);
        }
    }
    
    /**
     * Reports all collected assertion errors if any.
     */
    public void reportCollectedErrors() {
        if (!assertionErrors.isEmpty()) {
            StringBuilder message = new StringBuilder("The following assertions failed:\n");
            
            for (int i = 0; i < assertionErrors.size(); i++) {
                message.append(i + 1).append(") ").append(assertionErrors.get(i).getMessage()).append("\n");
            }
            
            Assert.fail(message.toString());
        }
    }
}
