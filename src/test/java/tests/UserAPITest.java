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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import assertions.ApiAssertion;
import utils.LoggerUtils;

/**
 * Tests for the JSONPlaceholder User API endpoints.
 * Demonstrates the usage of the test automation framework.
 */
@Feature("User API")
public class UserAPITest extends BaseTest {
    private static final Logger logger = LogManager.getLogger(UserAPITest.class);
    private String userId = "1";
    
    @BeforeClass(alwaysRun = true)
    public void setupUserTest() {
        logger.info("Setting up User API tests for JSONPlaceholder API");
    }
    
    @Test(groups = {"smoke", "api"}, priority = 1)
    @Description("Verify a user can be fetched by ID")
    @Story("Get User by ID")
    @Severity(SeverityLevel.CRITICAL)
    public void testFetchUserById() {
        logger.info("Fetching user with ID: " + userId);
        
        // Given
        userId = "1";
        
        // When
        Response response = getApiClient().get("/users/{id}")
            .pathParam("id", userId)
            .header("Accept", "application/json")
            .send();
        
        // Then
        ApiAssertion.assertThat(response)
            .hasStatusCode(200)
            .hasContentType("application/json")
            .hasJsonValue("$.id", Integer.parseInt(userId))
            .hasJsonField("$.name")
            .hasJsonField("$.email");
        
        // Validate response time
        ApiAssertion.assertThat(response)
            .hasResponseTimeLessThan(5000, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        // Validate response data
        String username = response.jsonPath().getString("username");
        Assert.assertNotNull(username, "Username should not be null");
        logger.info("Fetched user: {}", username);
        
        // Example of database validation that's only performed if the database is available
        if (getSqlClient() != null) {
            logger.info("Validating user data against SQL database");
            // Perform SQL validation here - omitted since the database is not available in this demo
        } else {
            logger.info("Skipping SQL database validation as the database is not available");
        }
    }
    
    @Test(groups = {"smoke", "api"}, priority = 2)
    @Description("Verify error handling when fetching a non-existent user")
    @Story("Error Handling")
    @Severity(SeverityLevel.NORMAL)
    public void testFetchNonExistentUser() {
        logger.info("Fetching non-existent user");
        
        // Given
        String nonExistentId = "999999";
        
        // When
        Response response = getApiClient().get("/users/{id}")
            .pathParam("id", nonExistentId)
            .header("Accept", "application/json")
            .send();
        
        // Then
        ApiAssertion.assertThat(response)
            .hasStatusCode(404);
    }
    
    @Test(groups = {"regression", "api"}, priority = 3)
    @Description("Verify creating a new user")
    @Story("Create User")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreateUser() {
        logger.info("Creating a new user");
        
        // Given
        String newUsername = "testuser_" + System.currentTimeMillis();
        String newEmail = newUsername + "@example.com";
        String newName = "Test User";
        
        String requestBody = String.format(
            "{\"name\":\"%s\",\"username\":\"%s\",\"email\":\"%s\",\"phone\":\"555-1234\"}",
            newName, newUsername, newEmail);
        
        // When
        Response response = getApiClient().post("/users")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .body(requestBody)
            .send();
        
        // Then - using soft assertions to check multiple conditions
        // Note: JSONPlaceholder doesn't really create resources, but returns a mock response
        ApiAssertion.softly(response)
            .hasStatusCode(201)
            .hasJsonValue("$.username", newUsername)
            .hasJsonValue("$.email", newEmail)
            .hasJsonValue("$.name", newName)
            .assertAll();
        
        // Extract the new user ID (JSONPlaceholder usually returns id=11 for new posts)
        Integer newUserId = response.jsonPath().getInt("id");
        Assert.assertNotNull(newUserId, "New user ID should not be null");
        logger.info("Created new user with ID: {}", newUserId);
    }
    
    @Test(groups = {"regression", "api"}, priority = 4)
    @Description("Verify updating an existing user")
    @Story("Update User")
    @Severity(SeverityLevel.NORMAL)
    public void testUpdateUser() {
        logger.info("Updating user with ID: " + userId);
        
        // Given
        String updatedEmail = "updated_" + System.currentTimeMillis() + "@example.com";
        String requestBody = String.format("{\"email\":\"%s\"}", updatedEmail);
        
        // When
        Response response = getApiClient().patch("/users/{id}")
            .pathParam("id", userId)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .body(requestBody)
            .send();
        
        // Then
        // Note: JSONPlaceholder doesn't really update resources, but returns a mock response
        ApiAssertion.assertThat(response)
            .hasStatusCode(200)
            .hasJsonValue("$.id", Integer.parseInt(userId))
            .hasJsonValue("$.email", updatedEmail);
    }
    
    @Test(groups = {"regression", "api"}, priority = 5)
    @Description("Verify deleting a user")
    @Story("Delete User")
    @Severity(SeverityLevel.NORMAL)
    public void testDeleteUser() {
        logger.info("Deleting user");
        
        // Given an existing user ID (using the first user in JSONPlaceholder)
        String userIdToDelete = "1";
        logger.info("Deleting user with ID: " + userIdToDelete);
        
        // When
        Response response = getApiClient().delete("/users/{id}")
            .pathParam("id", userIdToDelete)
            .send();
        
        // Then
        // Note: JSONPlaceholder returns 200 instead of 204 for DELETE
        ApiAssertion.assertThat(response)
            .hasStatusCode(200);
        
        logger.info("Successfully tested DELETE operation for user ID: {}", userIdToDelete);
    }
}
