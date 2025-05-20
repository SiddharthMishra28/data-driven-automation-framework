package base;

import config.ConfigurationManager;
import config.TestConfig;
import db.cosmos.CosmosDBClient;
import db.sql.SqlClient;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import report.AllureLogger;
import report.AllureReportManager;
import rest.RestApiClient;
import utils.LoggerUtils;
import utils.ThreadLocalContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all TestNG test classes.
 * Provides common setup and teardown methods, and initializes test resources.
 */
@Listeners({TestListener.class})
@Epic("Automation Framework Tests")
public class BaseTest {
    private static final Logger logger = LogManager.getLogger(BaseTest.class);
    
    /**
     * Setup method that runs before the test suite.
     * Initializes global resources and configurations.
     *
     * @param context TestNG test context
     */
    @BeforeSuite(alwaysRun = true)
    public void setupSuite(ITestContext context) {
        logger.info("Setting up test suite: {}", context.getSuite().getName());
        
        // Load system properties into configuration
        Map<String, String> systemProps = new HashMap<>();
        System.getProperties().forEach((key, value) -> 
            systemProps.put(key.toString(), value.toString()));
        ConfigurationManager.setCliProperties(systemProps);
        
        // Set up Allure environment info
        AllureReportManager.setupAllureEnvironmentInfo();
        AllureLogger.logEnvironmentInfo(
            TestConfig.getEnvironment(),
            TestConfig.getApiBaseUrl(),
            "Test Suite: " + context.getSuite().getName()
        );
        
        // Ensure Allure results directory exists
        AllureReportManager.ensureAllureResultsDirectory();
        
        logger.info("Test suite setup completed");
    }
    
    /**
     * Cleanup method that runs after the test suite.
     *
     * @param context TestNG test context
     */
    @AfterSuite(alwaysRun = true)
    public void teardownSuite(ITestContext context) {
        logger.info("Tearing down test suite: {}", context.getSuite().getName());
        // Perform any global cleanup if needed
    }
    
    /**
     * Setup method that runs before each test class.
     * Initializes class-level resources.
     */
    @BeforeClass(alwaysRun = true)
    public void setupClass() {
        logger.info("Setting up test class: {}", this.getClass().getSimpleName());
    }
    
    /**
     * Cleanup method that runs after each test class.
     * Releases class-level resources.
     */
    @AfterClass(alwaysRun = true)
    public void teardownClass() {
        logger.info("Tearing down test class: {}", this.getClass().getSimpleName());
    }
    
    /**
     * Setup method that runs before each test method.
     * Initializes thread-local resources for the test.
     *
     * @param method Test method
     * @param context TestNG test context
     */
    @BeforeMethod(alwaysRun = true)
    public void setupMethod(Method method, ITestContext context) {
        String testName = method.getName();
        logger.info("Starting test: {}", testName);
        
        // Set up thread-local context for this test
        ThreadLocalContext.setTestName(testName);
        LoggerUtils.setupThreadContext(testName);
        
        // Initialize API client
        RestApiClient apiClient = new RestApiClient();
        ThreadLocalContext.setRestApiClient(apiClient);
        
        // Initialize database clients if configuration is available
        initializeDatabaseClients();
        
        // Log test start
        Allure.step("Starting test: " + testName);
    }
    
    /**
     * Cleanup method that runs after each test method.
     * Releases thread-local resources.
     *
     * @param result TestNG test result
     */
    @AfterMethod(alwaysRun = true)
    public void teardownMethod(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String resultStatus = getTestResultStatus(result);
        logger.info("Finished test: {} - {}", testName, resultStatus);
        
        // Log test result
        Allure.step("Test result: " + resultStatus);
        
        // Clean up thread-local resources
        ThreadLocalContext.clear();
        LoggerUtils.clearThreadContext();
        
        // Remove ThreadLocal variables to prevent memory leaks
        ThreadLocalContext.remove();
    }
    
    /**
     * Gets the RestApiClient for the current thread.
     *
     * @return RestApiClient instance
     */
    protected RestApiClient getApiClient() {
        return ThreadLocalContext.getRestApiClient();
    }
    
    /**
     * Gets the SqlClient for the current thread.
     *
     * @return SqlClient instance
     */
    protected SqlClient getSqlClient() {
        return ThreadLocalContext.getSqlClient();
    }
    
    /**
     * Gets the CosmosDBClient for the current thread.
     *
     * @return CosmosDBClient instance
     */
    protected CosmosDBClient getCosmosClient() {
        return ThreadLocalContext.getCosmosDBClient();
    }
    
    /**
     * Initializes database clients if configuration is available.
     * Uses a try-catch block to prevent test failures if the database is not available.
     */
    private void initializeDatabaseClients() {
        // Initialize SQL client if configuration is available
        String sqlJdbcUrl = TestConfig.getSqlJdbcUrl();
        if (sqlJdbcUrl != null && !sqlJdbcUrl.isEmpty()) {
            try {
                logger.debug("Initializing SQL client");
                SqlClient sqlClient = new SqlClient(
                    sqlJdbcUrl,
                    TestConfig.getSqlUsername(),
                    TestConfig.getSqlPassword(),
                    TestConfig.getSqlConnectionPoolSize(),
                    TestConfig.getSqlConnectionTimeout(),
                    TestConfig.getSqlMaxLifetime()
                );
                ThreadLocalContext.setSqlClient(sqlClient);
                logger.info("SQL client initialized successfully");
            } catch (Exception e) {
                logger.warn("Failed to initialize SQL client: {}. Tests will continue without SQL database access.", e.getMessage());
                // Log error but continue test execution
                Allure.step("SQL Database is not available. Tests requiring SQL validation will be skipped.");
            }
        }
        
        // Initialize Cosmos DB client if configuration is available
        String cosmosConnectionString = TestConfig.getCosmosConnectionString();
        if (cosmosConnectionString != null && !cosmosConnectionString.isEmpty()) {
            try {
                logger.debug("Initializing Cosmos DB client");
                String databaseName = TestConfig.getCosmosDatabaseName();
                String containerName = TestConfig.getCosmosContainerName();
                
                if (databaseName != null && !databaseName.isEmpty() && 
                    containerName != null && !containerName.isEmpty()) {
                    CosmosDBClient cosmosClient = new CosmosDBClient(
                        cosmosConnectionString, 
                        databaseName, 
                        containerName
                    );
                    ThreadLocalContext.setCosmosDBClient(cosmosClient);
                    logger.info("Cosmos DB client initialized successfully");
                } else {
                    logger.warn("Cosmos DB configuration is incomplete. Missing database name or container name.");
                }
            } catch (Exception e) {
                logger.warn("Failed to initialize Cosmos DB client: {}. Tests will continue without Cosmos DB access.", e.getMessage());
                // Log error but continue test execution
                Allure.step("Cosmos DB is not available. Tests requiring Cosmos DB validation will be skipped.");
            }
        }
    }
    
    /**
     * Gets a human-readable status string for a test result.
     *
     * @param result TestNG test result
     * @return Status string (PASSED, FAILED, SKIPPED)
     */
    private String getTestResultStatus(ITestResult result) {
        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                return "PASSED";
            case ITestResult.FAILURE:
                return "FAILED";
            case ITestResult.SKIP:
                return "SKIPPED";
            default:
                return "UNKNOWN";
        }
    }
}
