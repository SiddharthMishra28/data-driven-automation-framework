package assertions;

import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Extension of ApiAssertion that collects all assertion failures instead of failing immediately.
 * This allows multiple assertions to be checked in a single test, even if some fail.
 */
public class SoftApiAssertion extends ApiAssertion {
    private static final Logger logger = LogManager.getLogger(SoftApiAssertion.class);
    
    /**
     * Creates a SoftApiAssertion for the given Response.
     *
     * @param response Response to make assertions on
     */
    protected SoftApiAssertion(Response response) {
        super(response);
        this.failFast = false;
    }
    
    /**
     * Reports all collected assertion errors and clears the collection.
     *
     * @return This SoftApiAssertion instance
     */
    public SoftApiAssertion assertAll() {
        reportCollectedErrors();
        assertionErrors.clear();
        return this;
    }
    
    /**
     * Gets the number of assertion errors collected.
     *
     * @return Number of assertion errors
     */
    public int getErrorCount() {
        return assertionErrors.size();
    }
    
    /**
     * Checks if any assertion errors have been collected.
     *
     * @return true if there are assertion errors, false otherwise
     */
    public boolean hasErrors() {
        return !assertionErrors.isEmpty();
    }
    
    /**
     * Gets the underlying Response object.
     *
     * @return Response object
     */
    public Response getResponse() {
        return response;
    }
    
    @Override
    public SoftApiAssertion hasStatusCode(int expectedStatusCode) {
        super.hasStatusCode(expectedStatusCode);
        return this;
    }
    
    @Override
    public SoftApiAssertion hasStatusCodeIn(int... expectedStatusCodes) {
        super.hasStatusCodeIn(expectedStatusCodes);
        return this;
    }
    
    @Override
    public SoftApiAssertion hasHeader(String headerName, String expectedValue) {
        super.hasHeader(headerName, expectedValue);
        return this;
    }
    
    @Override
    public SoftApiAssertion hasHeader(String headerName) {
        super.hasHeader(headerName);
        return this;
    }
    
    @Override
    public SoftApiAssertion hasContentType(String expectedContentType) {
        super.hasContentType(expectedContentType);
        return this;
    }
    
    @Override
    public SoftApiAssertion hasResponseTimeLessThan(long maxTime, java.util.concurrent.TimeUnit timeUnit) {
        super.hasResponseTimeLessThan(maxTime, timeUnit);
        return this;
    }
    
    @Override
    public SoftApiAssertion hasJsonValue(String jsonPath, Object expectedValue) {
        super.hasJsonValue(jsonPath, expectedValue);
        return this;
    }
    
    @Override
    public SoftApiAssertion hasJsonField(String jsonPath) {
        super.hasJsonField(jsonPath);
        return this;
    }
    
    @Override
    public SoftApiAssertion hasNoJsonField(String jsonPath) {
        super.hasNoJsonField(jsonPath);
        return this;
    }
    
    @Override
    public SoftApiAssertion bodyContains(String expectedText) {
        super.bodyContains(expectedText);
        return this;
    }
    
    @Override
    public SoftApiAssertion bodyDoesNotContain(String text) {
        super.bodyDoesNotContain(text);
        return this;
    }
    
    @Override
    public SoftApiAssertion hasCookie(String cookieName) {
        super.hasCookie(cookieName);
        return this;
    }
    
    @Override
    public SoftApiAssertion hasCookie(String cookieName, String expectedValue) {
        super.hasCookie(cookieName, expectedValue);
        return this;
    }
}
