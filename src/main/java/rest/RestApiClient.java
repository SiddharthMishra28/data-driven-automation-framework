package rest;

import config.TestConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import report.AllureLogger;
import utils.LoggerUtils;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Rest API client wrapper around Rest Assured for making HTTP requests.
 */
public class RestApiClient {
    private static final Logger logger = LogManager.getLogger(RestApiClient.class);
    private final String baseUrl;
    private final int requestTimeout;
    private final int connectionTimeout;
    private final int retryCount;
    private final boolean loggingEnabled;

    /**
     * Creates a RestApiClient with default configuration from TestConfig.
     */
    public RestApiClient() {
        this.baseUrl = TestConfig.getApiBaseUrl();
        this.requestTimeout = TestConfig.getApiRequestTimeout();
        this.connectionTimeout = TestConfig.getApiConnectionTimeout();
        this.retryCount = TestConfig.getApiRetryCount();
        this.loggingEnabled = TestConfig.isApiLoggingEnabled();
        
        configureRestAssured();
    }

    /**
     * Creates a RestApiClient with custom configuration.
     *
     * @param baseUrl Base URL for API requests
     * @param requestTimeout Request timeout in milliseconds
     * @param connectionTimeout Connection timeout in milliseconds
     * @param retryCount Number of retries for failed requests
     * @param loggingEnabled Whether to enable request/response logging
     */
    public RestApiClient(String baseUrl, int requestTimeout, int connectionTimeout, int retryCount, boolean loggingEnabled) {
        this.baseUrl = baseUrl;
        this.requestTimeout = requestTimeout;
        this.connectionTimeout = connectionTimeout;
        this.retryCount = retryCount;
        this.loggingEnabled = loggingEnabled;
        
        configureRestAssured();
    }

    /**
     * Creates a RequestBuilder for a GET request.
     *
     * @param endpoint API endpoint path
     * @return RequestBuilder for fluent request building
     */
    public RequestBuilder get(String endpoint) {
        return request(endpoint).method("GET");
    }

    /**
     * Creates a RequestBuilder for a GET request with path parameters.
     *
     * @param endpoint API endpoint path with placeholders
     * @param pathParams Path parameters to replace placeholders
     * @return RequestBuilder for fluent request building
     */
    public RequestBuilder get(String endpoint, Object... pathParams) {
        return request(endpoint).method("GET").pathParams(pathParams);
    }

    /**
     * Creates a RequestBuilder for a POST request.
     *
     * @param endpoint API endpoint path
     * @return RequestBuilder for fluent request building
     */
    public RequestBuilder post(String endpoint) {
        return request(endpoint).method("POST");
    }

    /**
     * Creates a RequestBuilder for a POST request with path parameters.
     *
     * @param endpoint API endpoint path with placeholders
     * @param pathParams Path parameters to replace placeholders
     * @return RequestBuilder for fluent request building
     */
    public RequestBuilder post(String endpoint, Object... pathParams) {
        return request(endpoint).method("POST").pathParams(pathParams);
    }

    /**
     * Creates a RequestBuilder for a PUT request.
     *
     * @param endpoint API endpoint path
     * @return RequestBuilder for fluent request building
     */
    public RequestBuilder put(String endpoint) {
        return request(endpoint).method("PUT");
    }

    /**
     * Creates a RequestBuilder for a PUT request with path parameters.
     *
     * @param endpoint API endpoint path with placeholders
     * @param pathParams Path parameters to replace placeholders
     * @return RequestBuilder for fluent request building
     */
    public RequestBuilder put(String endpoint, Object... pathParams) {
        return request(endpoint).method("PUT").pathParams(pathParams);
    }

    /**
     * Creates a RequestBuilder for a PATCH request.
     *
     * @param endpoint API endpoint path
     * @return RequestBuilder for fluent request building
     */
    public RequestBuilder patch(String endpoint) {
        return request(endpoint).method("PATCH");
    }

    /**
     * Creates a RequestBuilder for a PATCH request with path parameters.
     *
     * @param endpoint API endpoint path with placeholders
     * @param pathParams Path parameters to replace placeholders
     * @return RequestBuilder for fluent request building
     */
    public RequestBuilder patch(String endpoint, Object... pathParams) {
        return request(endpoint).method("PATCH").pathParams(pathParams);
    }

    /**
     * Creates a RequestBuilder for a DELETE request.
     *
     * @param endpoint API endpoint path
     * @return RequestBuilder for fluent request building
     */
    public RequestBuilder delete(String endpoint) {
        return request(endpoint).method("DELETE");
    }

    /**
     * Creates a RequestBuilder for a DELETE request with path parameters.
     *
     * @param endpoint API endpoint path with placeholders
     * @param pathParams Path parameters to replace placeholders
     * @return RequestBuilder for fluent request building
     */
    public RequestBuilder delete(String endpoint, Object... pathParams) {
        return request(endpoint).method("DELETE").pathParams(pathParams);
    }

    /**
     * Creates a RequestBuilder for an OPTIONS request.
     *
     * @param endpoint API endpoint path
     * @return RequestBuilder for fluent request building
     */
    public RequestBuilder options(String endpoint) {
        return request(endpoint).method("OPTIONS");
    }

    /**
     * Creates a RequestBuilder for a HEAD request.
     *
     * @param endpoint API endpoint path
     * @return RequestBuilder for fluent request building
     */
    public RequestBuilder head(String endpoint) {
        return request(endpoint).method("HEAD");
    }

    /**
     * Creates a new RequestBuilder with the specified endpoint.
     *
     * @param endpoint API endpoint path
     * @return RequestBuilder for fluent request building
     */
    public RequestBuilder request(String endpoint) {
        RequestSpecification spec = createBaseRequestSpec();
        return new RequestBuilder(this, endpoint, spec);
    }

    /**
     * Executes the HTTP request with retry logic.
     *
     * @param method HTTP method
     * @param requestSpec Request specification
     * @return Response object
     */
    Response executeRequest(String method, RequestSpecification requestSpec) {
        Supplier<Response> requestSupplier = () -> {
            switch (method.toUpperCase()) {
                case "GET":
                    return requestSpec.get();
                case "POST":
                    return requestSpec.post();
                case "PUT":
                    return requestSpec.put();
                case "DELETE":
                    return requestSpec.delete();
                case "PATCH":
                    return requestSpec.patch();
                case "HEAD":
                    return requestSpec.head();
                case "OPTIONS":
                    return requestSpec.options();
                default:
                    throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            }
        };

        return executeWithRetry(requestSupplier);
    }

    /**
     * Executes a request with retry logic
     *
     * @param requestSupplier Supplier function that performs the request
     * @return Response object
     */
    private Response executeWithRetry(Supplier<Response> requestSupplier) {
        int attempts = 0;
        Response response = null;
        Exception lastException = null;

        while (attempts < retryCount + 1) {
            try {
                long startTime = System.currentTimeMillis();
                response = requestSupplier.get();
                long endTime = System.currentTimeMillis();
                
                logger.debug("Request completed in {}ms", (endTime - startTime));
                
                // If response is successful or if it's a client error (4xx), don't retry
                if (response.getStatusCode() < 500) {
                    return response;
                }
                
                logger.warn("Request failed with server error status code: {}, attempt {}/{}",
                        response.getStatusCode(), attempts + 1, retryCount + 1);
            } catch (Exception e) {
                lastException = e;
                logger.warn("Request failed with exception: {}, attempt {}/{}",
                        e.getMessage(), attempts + 1, retryCount + 1);
            }
            
            attempts++;
            
            if (attempts < retryCount + 1) {
                try {
                    // Exponential backoff
                    long backoffTime = (long) Math.pow(2, attempts) * 100;
                    logger.debug("Retrying after {}ms", backoffTime);
                    Thread.sleep(backoffTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }
        
        if (response != null) {
            return response;
        } else {
            throw new RuntimeException("Request failed after " + retryCount + " retries", lastException);
        }
    }

    /**
     * Creates a base RequestSpecification with common configuration.
     *
     * @return Base RequestSpecification
     */
    private RequestSpecification createBaseRequestSpec() {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setConfig(getRestAssuredConfig());

        if (loggingEnabled) {
            builder.log(LogDetail.ALL);
        }
        
        // Add Allure reporting filter
        builder.addFilter(new AllureRestAssured());
        
        return builder.build();
    }

    /**
     * Configures global Rest Assured settings.
     */
    private void configureRestAssured() {
        RestAssured.config = getRestAssuredConfig();
        
        if (loggingEnabled) {
            RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        }
    }

    /**
     * Creates Rest Assured configuration.
     *
     * @return RestAssuredConfig
     */
    private RestAssuredConfig getRestAssuredConfig() {
        HttpClientConfig httpClientConfig = HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", connectionTimeout)
                .setParam("http.socket.timeout", requestTimeout)
                .setParam("http.connection.manager.timeout", connectionTimeout);
        
        LogConfig logConfig = LogConfig.logConfig()
                .enableLoggingOfRequestAndResponseIfValidationFails();
        
        return RestAssuredConfig.config()
                .httpClient(httpClientConfig)
                .logConfig(logConfig);
    }
}
