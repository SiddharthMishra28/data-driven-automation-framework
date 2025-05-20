package rest;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import report.AllureLogger;
import utils.LoggerUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder class for constructing REST API requests using a fluent interface.
 */
public class RequestBuilder {
    private static final Logger logger = LogManager.getLogger(RequestBuilder.class);
    
    private final RestApiClient apiClient;
    private final String endpoint;
    private final RequestSpecification requestSpec;
    private String method;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, Object> queryParams = new HashMap<>();
    private Map<String, Object> pathParams = new HashMap<>();
    private Object body;
    private ContentType contentType = ContentType.JSON;
    private ContentType accept = ContentType.JSON;
    private List<Object> pathParamValues = new ArrayList<>();
    private AuthType authType;
    private String username;
    private String password;
    private String token;
    private String authHeader;

    /**
     * Creates a RequestBuilder with the specified API client, endpoint and request specification.
     *
     * @param apiClient RestApiClient instance
     * @param endpoint API endpoint path
     * @param requestSpec RequestSpecification to build upon
     */
    RequestBuilder(RestApiClient apiClient, String endpoint, RequestSpecification requestSpec) {
        this.apiClient = apiClient;
        this.endpoint = endpoint;
        this.requestSpec = requestSpec;
    }

    /**
     * Sets the HTTP method for the request.
     *
     * @param method HTTP method (GET, POST, PUT, DELETE, etc.)
     * @return This RequestBuilder instance
     */
    RequestBuilder method(String method) {
        this.method = method;
        return this;
    }

    /**
     * Sets a header for the request.
     *
     * @param name Header name
     * @param value Header value
     * @return This RequestBuilder instance
     */
    public RequestBuilder header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    /**
     * Sets multiple headers for the request.
     *
     * @param headers Map of header names to values
     * @return This RequestBuilder instance
     */
    public RequestBuilder headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    /**
     * Sets a query parameter for the request.
     *
     * @param name Parameter name
     * @param value Parameter value
     * @return This RequestBuilder instance
     */
    public RequestBuilder queryParam(String name, Object value) {
        this.queryParams.put(name, value);
        return this;
    }

    /**
     * Sets multiple query parameters for the request.
     *
     * @param params Map of parameter names to values
     * @return This RequestBuilder instance
     */
    public RequestBuilder queryParams(Map<String, Object> params) {
        this.queryParams.putAll(params);
        return this;
    }

    /**
     * Sets a path parameter for the request.
     *
     * @param name Parameter name
     * @param value Parameter value
     * @return This RequestBuilder instance
     */
    public RequestBuilder pathParam(String name, Object value) {
        this.pathParams.put(name, value);
        return this;
    }

    /**
     * Sets multiple path parameters for the request.
     *
     * @param params Map of parameter names to values
     * @return This RequestBuilder instance
     */
    public RequestBuilder pathParams(Map<String, Object> params) {
        this.pathParams.putAll(params);
        return this;
    }

    /**
     * Sets ordered path parameters for the request.
     *
     * @param pathParams Path parameter values in order
     * @return This RequestBuilder instance
     */
    public RequestBuilder pathParams(Object... pathParams) {
        this.pathParamValues.addAll(Arrays.asList(pathParams));
        return this;
    }

    /**
     * Sets the request body.
     *
     * @param body Request body object (will be serialized to JSON/XML)
     * @return This RequestBuilder instance
     */
    public RequestBuilder body(Object body) {
        this.body = body;
        return this;
    }

    /**
     * Sets the Content-Type header for the request.
     *
     * @param contentType ContentType enum value
     * @return This RequestBuilder instance
     */
    public RequestBuilder contentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Sets the Accept header for the request.
     *
     * @param accept ContentType enum value
     * @return This RequestBuilder instance
     */
    public RequestBuilder accept(ContentType accept) {
        this.accept = accept;
        return this;
    }

    /**
     * Configures Basic Authentication for the request.
     *
     * @param username Username
     * @param password Password
     * @return This RequestBuilder instance
     */
    public RequestBuilder basicAuth(String username, String password) {
        this.authType = AuthType.BASIC;
        this.username = username;
        this.password = password;
        return this;
    }

    /**
     * Configures Bearer Token Authentication for the request.
     *
     * @param token Bearer token
     * @return This RequestBuilder instance
     */
    public RequestBuilder bearerAuth(String token) {
        this.authType = AuthType.BEARER;
        this.token = token;
        return this;
    }

    /**
     * Configures OAuth2 Authentication for the request.
     *
     * @param token OAuth2 access token
     * @return This RequestBuilder instance
     */
    public RequestBuilder oauth2(String token) {
        this.authType = AuthType.OAUTH2;
        this.token = token;
        return this;
    }

    /**
     * Configures Digest Authentication for the request.
     *
     * @param username Username
     * @param password Password
     * @return This RequestBuilder instance
     */
    public RequestBuilder digestAuth(String username, String password) {
        this.authType = AuthType.DIGEST;
        this.username = username;
        this.password = password;
        return this;
    }

    /**
     * Configures Preemptive Authentication for the request.
     *
     * @param username Username
     * @param password Password
     * @return This RequestBuilder instance
     */
    public RequestBuilder preemptiveAuth(String username, String password) {
        this.authType = AuthType.PREEMPTIVE;
        this.username = username;
        this.password = password;
        return this;
    }

    /**
     * Sets a custom Authorization header.
     *
     * @param authHeaderValue Authorization header value
     * @return This RequestBuilder instance
     */
    public RequestBuilder customAuth(String authHeaderValue) {
        this.authType = AuthType.CUSTOM;
        this.authHeader = authHeaderValue;
        return this;
    }

    /**
     * Executes the request and returns the response.
     *
     * @return Response object
     */
    @Step("Sending {method} request to {endpoint}")
    public Response send() {
        RequestSpecification spec = buildRequestSpec();
        
        // Log the request
        String fullUrl = buildFullUrl();
        String headersString = headers.toString();
        String bodyString = body != null ? body.toString() : "";
        LoggerUtils.logRequest(method, fullUrl, headersString, bodyString);
        
        // Add request details to Allure report
        AllureLogger.logRequest(method, fullUrl, headersString, bodyString);
        
        // Execute the request with retry logic
        long startTime = System.currentTimeMillis();
        Response response = apiClient.executeRequest(method, spec);
        long endTime = System.currentTimeMillis();
        
        // Log the response
        String responseHeaders = response.getHeaders().toString();
        String responseBody = response.getBody().asString();
        LoggerUtils.logResponse(response.getStatusCode(), responseHeaders, responseBody, endTime - startTime);
        
        // Add response details to Allure report
        AllureLogger.logResponse(response.getStatusCode(), responseHeaders, responseBody, endTime - startTime);
        
        return response;
    }

    /**
     * Builds the full URL for logging purposes.
     *
     * @return Full request URL
     */
    private String buildFullUrl() {
        StringBuilder url = new StringBuilder(endpoint);
        
        if (!queryParams.isEmpty()) {
            url.append("?");
            queryParams.forEach((key, value) -> url.append(key).append("=").append(value).append("&"));
            // Remove trailing '&'
            url.deleteCharAt(url.length() - 1);
        }
        
        return url.toString();
    }

    /**
     * Builds the request specification based on the builder parameters.
     *
     * @return RequestSpecification
     */
    private RequestSpecification buildRequestSpec() {
        RequestSpecification spec = requestSpec;
        
        // Add headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            spec = spec.header(header.getKey(), header.getValue());
        }
        
        // Add content type
        if (contentType != null) {
            spec = spec.contentType(contentType);
        }
        
        // Add accept type
        if (accept != null) {
            spec = spec.accept(accept);
        }
        
        // Add query parameters
        for (Map.Entry<String, Object> param : queryParams.entrySet()) {
            spec = spec.queryParam(param.getKey(), param.getValue());
        }
        
        // Add path parameters (named)
        for (Map.Entry<String, Object> param : pathParams.entrySet()) {
            spec = spec.pathParam(param.getKey(), param.getValue());
        }
        
        // Add path parameters (ordered)
        if (!pathParamValues.isEmpty()) {
            for (int i = 0; i < pathParamValues.size(); i++) {
                spec = spec.pathParam(String.valueOf(i), pathParamValues.get(i));
            }
        }
        
        // Add body
        if (body != null) {
            spec = spec.body(body);
        }
        
        // Configure authentication
        if (authType != null) {
            switch (authType) {
                case BASIC:
                    spec = spec.auth().basic(username, password);
                    break;
                case BEARER:
                    spec = spec.header("Authorization", "Bearer " + token);
                    break;
                case OAUTH2:
                    spec = spec.auth().oauth2(token);
                    break;
                case DIGEST:
                    spec = spec.auth().digest(username, password);
                    break;
                case PREEMPTIVE:
                    spec = spec.auth().preemptive().basic(username, password);
                    break;
                case CUSTOM:
                    spec = spec.header("Authorization", authHeader);
                    break;
            }
        }
        
        return spec;
    }
}
