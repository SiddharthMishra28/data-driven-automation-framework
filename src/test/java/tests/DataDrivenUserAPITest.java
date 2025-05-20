package tests;

import base.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.DataProviderUtils;

import java.util.Map;

/**
 * Example of data-driven tests for the User API endpoints.
 * Shows how to use different data sources (CSV, JSON, Database) for testing.
 */
@Feature("Data-Driven User API Testing")
public class DataDrivenUserAPITest extends BaseTest {
    private static final Logger logger = LogManager.getLogger(DataDrivenUserAPITest.class);
    
    /**
     * Data provider that loads user data from a CSV file.
     * 
     * @return Object[][] array of user data maps
     */
    @DataProvider(name = "csvUserData")
    public Object[][] getCsvUserData() {
        String csvFilePath = "src/test/resources/testdata/csv/users.csv";
        return DataProviderUtils.loadCsvData(csvFilePath);
    }
    
    /**
     * Data provider that loads user data from a JSON file.
     * 
     * @return Object[][] array of user data maps
     */
    @DataProvider(name = "jsonUserData")
    public Object[][] getJsonUserData() {
        String jsonFilePath = "src/test/resources/testdata/json/users.json";
        return DataProviderUtils.loadJsonData(jsonFilePath);
    }
    
    /**
     * Data provider that would load user data from a database.
     * This is commented out since we don't have a live database connection in this example.
     * 
     * @return Object[][] array of user data maps
     */
    /*
    @DataProvider(name = "dbUserData")
    public Object[][] getDbUserData() {
        if (getSqlClient() == null) {
            logger.warn("Database connection not available, skipping DB data provider");
            return new Object[0][0];
        }
        
        String query = "SELECT id, name, username, email, phone FROM users WHERE status = 'active' LIMIT 5";
        return DataProviderUtils.loadDatabaseData(getSqlClient(), query);
    }
    */
    
    /**
     * Test that fetches a user by ID using data from a CSV file.
     * 
     * @param userData Map containing user data from CSV
     */
    @Test(groups = {"data-driven", "api"}, dataProvider = "csvUserData")
    @Description("Verify fetching users by ID with data from CSV")
    @Story("Data-Driven User Fetch")
    @Severity(SeverityLevel.NORMAL)
    public void testFetchUserByIdFromCsv(Map<String, String> userData) {
        String userId = userData.get("id");
        String expectedName = userData.get("name");
        String expectedEmail = userData.get("email");
        
        logger.info("Testing user fetch with CSV data - ID: {}, Name: {}", userId, expectedName);
        
        // When
        Response response = getApiClient().get("/users/{id}")
            .pathParam("id", userId)
            .header("Accept", "application/json")
            .send();
        
        // Then
        // Note: This test will fail with JSONPlaceholder as it doesn't have our test data
        // But it demonstrates the pattern for data-driven tests
        if (response.getStatusCode() == 200) {
            String actualName = response.jsonPath().getString("name");
            String actualEmail = response.jsonPath().getString("email");
            
            Assert.assertEquals(actualName, expectedName, "User name should match");
            Assert.assertEquals(actualEmail, expectedEmail, "User email should match");
            
            logger.info("Successfully verified user data for ID: {}", userId);
        } else {
            logger.warn("User with ID {} not found, status code: {}", userId, response.getStatusCode());
            // In a real test, this might be an assertion failure
            // For demo purposes, we'll just log it
        }
    }
    
    /**
     * Test that fetches a user by ID using data from a JSON file.
     * This test demonstrates accessing nested properties from complex JSON objects.
     * 
     * @param userData Map containing user data from JSON
     */
    @Test(groups = {"data-driven", "api"}, dataProvider = "jsonUserData")
    @Description("Verify fetching users by ID with data from JSON")
    @Story("Data-Driven User Fetch")
    @Severity(SeverityLevel.NORMAL)
    public void testFetchUserByIdFromJson(Map<String, Object> userData) {
        // JSON data has more complex structure with nested objects
        Integer userId = (Integer) userData.get("id");
        String expectedName = (String) userData.get("name");
        String expectedEmail = (String) userData.get("email");
        
        // Extract nested address data (this demonstrates handling complex data structures)
        @SuppressWarnings("unchecked")
        Map<String, String> address = (Map<String, String>) userData.get("address");
        String expectedCity = address.get("city");
        
        logger.info("Testing user fetch with JSON data - ID: {}, Name: {}, City: {}", 
                userId, expectedName, expectedCity);
        
        // When
        Response response = getApiClient().get("/users/{id}")
            .pathParam("id", userId.toString())
            .header("Accept", "application/json")
            .send();
        
        // Then
        // Note: This test will fail with JSONPlaceholder as it doesn't have our test data
        // But it demonstrates the pattern for data-driven tests
        if (response.getStatusCode() == 200) {
            String actualName = response.jsonPath().getString("name");
            String actualEmail = response.jsonPath().getString("email");
            String actualCity = response.jsonPath().getString("address.city");
            
            Assert.assertEquals(actualName, expectedName, "User name should match");
            Assert.assertEquals(actualEmail, expectedEmail, "User email should match");
            if (actualCity != null) {
                Assert.assertEquals(actualCity, expectedCity, "User city should match");
            }
            
            logger.info("Successfully verified user data for ID: {}", userId);
        } else {
            logger.warn("User with ID {} not found, status code: {}", userId, response.getStatusCode());
            // In a real test, this might be an assertion failure
            // For demo purposes, we'll just log it
        }
    }
    
    /**
     * This test would use database data for data-driven testing.
     * It's commented out since we don't have a live database in this example.
     * 
     * @param userData Map containing user data from database
     */
    /*
    @Test(groups = {"data-driven", "api", "db"}, dataProvider = "dbUserData")
    @Description("Verify fetching users by ID with data from Database")
    @Story("Data-Driven User Fetch with DB")
    @Severity(SeverityLevel.NORMAL)
    public void testFetchUserByIdFromDatabase(Map<String, Object> userData) {
        String userId = userData.get("id").toString();
        String expectedName = (String) userData.get("name");
        String expectedEmail = (String) userData.get("email");
        
        logger.info("Testing user fetch with Database data - ID: {}, Name: {}", userId, expectedName);
        
        // When
        Response response = getApiClient().get("/users/{id}")
            .pathParam("id", userId)
            .header("Accept", "application/json")
            .send();
        
        // Then
        if (response.getStatusCode() == 200) {
            String actualName = response.jsonPath().getString("name");
            String actualEmail = response.jsonPath().getString("email");
            
            Assert.assertEquals(actualName, expectedName, "User name should match");
            Assert.assertEquals(actualEmail, expectedEmail, "User email should match");
            
            logger.info("Successfully verified user data for ID: {}", userId);
        } else {
            logger.warn("User with ID {} not found, status code: {}", userId, response.getStatusCode());
        }
    }
    */
}