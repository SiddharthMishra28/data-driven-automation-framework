package rest;

/**
 * Enum representing different authentication types for REST API requests.
 */
public enum AuthType {
    /**
     * Basic Authentication (username and password)
     */
    BASIC,
    
    /**
     * Bearer Token Authentication
     */
    BEARER,
    
    /**
     * OAuth2 Authentication
     */
    OAUTH2,
    
    /**
     * Digest Authentication
     */
    DIGEST,
    
    /**
     * Preemptive Authentication
     */
    PREEMPTIVE,
    
    /**
     * Custom Authentication
     */
    CUSTOM
}
