# Java Test Automation Framework

A scalable, modular, and configurable Java-based test automation framework built with TestNG, Rest Assured, and database integrations. This framework supports comprehensive API testing with rich reporting capabilities.

## Features

- **REST API Testing**: Fluent interface for API requests using Rest Assured
- **Database Validation**: Integration with Azure Cosmos DB and MS SQL Server
- **Rich Assertions**: Custom assertion library for API responses and Spark datasets
- **Apache Spark Testing**: Comprehensive data validation and transformation testing
- **Data-Driven Testing**: CSV, JSON, and Database resultset support for parameterized tests
- **Reporting**: Detailed HTML reports using Allure Framework
- **Configuration Management**: Environment-specific configurations
- **Parallel Execution**: Multi-threaded test execution
- **Logging**: Comprehensive logging using Log4j2

## Project Structure

```
src/
├── main/
│   └── java/
│       ├── config/                 // Configuration management
│       ├── utils/                  // Utility classes
│       ├── rest/                   // RestAssured wrapper
│       ├── db/                     // Database clients
│       │   ├── cosmos/             // Azure Cosmos DB client
│       │   └── sql/                // SQL Server client
│       ├── assertions/             // Custom assertions
│       ├── report/                 // Allure reporting
│       └── base/                   // Base test classes
├── test/
│   └── java/
│       └── tests/                  // Test classes
└── resources/
    ├── config/                     // Configuration files
    │   └── application.properties  // Default config
    └── log4j2.xml                  // Logging config
```

## Setup

### Prerequisites

- Java 17 or higher
- Maven 3.8 or higher
- Azure Cosmos DB account (for Cosmos DB tests)
- MS SQL Server instance (for SQL tests)

### Installation

1. Clone the repository
2. Install dependencies using Maven:

```bash
mvn clean install -DskipTests
```

## Configuration

The framework uses a hierarchical configuration approach:

1. Default properties in `src/main/resources/config/application.properties`
2. Environment-specific properties in `src/main/resources/config/application-{env}.properties`
3. Command-line overrides using `-D` parameters

### Sample Properties File

```properties
# API Configuration
api.base.url=https://jsonplaceholder.typicode.com
api.request.timeout=30000
api.retry.count=3

# SQL Database Configuration
db.sql.url=jdbc:sqlserver://localhost:1433;databaseName=TestDB
db.sql.username=sa
db.sql.password=YourPassword
db.sql.connection.pool.size=10

# Cosmos DB Configuration
db.cosmos.connection.string=AccountEndpoint=https://your-cosmos-db.documents.azure.com:443/;AccountKey=your-key==
db.cosmos.database=TestDatabase
db.cosmos.container=TestContainer

# Reporting Configuration
report.allure.results.directory=target/allure-results
```

## Running Tests

### Basic Test Execution

```bash
mvn clean test
```

### Running with Specific Profiles

```bash
mvn clean test -Pqa
```

### Running Specific Test Classes

```bash
mvn clean test -Dtest=UserAPITest
```

### Running Tests with Custom Configuration

```bash
mvn clean test -Denv=staging -Dapi.base.url=https://staging-api.example.com
```

### Generating Allure Reports

```bash
mvn clean test allure:report
```

Then open `target/site/allure-maven-plugin/index.html` in your browser.

## API Testing

### Request Builder

The framework provides a fluent interface for building API requests:

```java
Response response = getApiClient().get("/users/{id}")
    .pathParam("id", "1")
    .header("Accept", "application/json")
    .queryParam("filter", "active")
    .send();
```

#### Available HTTP Methods

- `get(String endpoint)` - Send a GET request
- `post(String endpoint)` - Send a POST request
- `put(String endpoint)` - Send a PUT request
- `patch(String endpoint)` - Send a PATCH request
- `delete(String endpoint)` - Send a DELETE request

#### Request Customization

- `header(String name, String value)` - Add a header
- `headers(Map<String, String> headers)` - Add multiple headers

### Apache Spark Testing

#### Local File Operations
```java
// Initialize Spark client
SparkClient sparkClient = new SparkClient();

// Load and transform data
Dataset<Row> sourceData = sparkClient.readCsv("data.csv");
```

#### Azure Data Lake Storage (ADLS) Operations
```java
// Initialize Spark client with ADLS configuration
SparkClient sparkClient = new SparkClient();
sparkClient.configureADLS("your-storage-account", "your-access-key");

// Read different file formats from ADLS
Dataset<Row> csvData = sparkClient.readFromADLS("container", "path/to/file.csv", "csv");
Dataset<Row> jsonData = sparkClient.readFromADLS("container", "path/to/file.json", "json");
Dataset<Row> parquetData = sparkClient.readFromADLS("container", "path/to/file.parquet", "parquet");
Dataset<Row> textData = sparkClient.readFromADLS("container", "path/to/file.txt", "text");

// Write data to ADLS
sparkClient.writeToADLS(transformedData, "container", "path/to/output.parquet", "parquet");
```

The framework supports reading and writing various file formats from Azure Data Lake Storage Gen2:
- CSV files with header inference
- JSON and JSON-LD files with multiline support
- Parquet files
- Plain text files

Files are accessed using the abfss:// protocol with storage account credentials.
Dataset<Row> transformedData = SparkTransformations.cleanseData(sourceData);

// Assert results
SparkAssertions.assertRowCount(transformedData, expectedCount);
SparkAssertions.assertColumnExists(transformedData, "category");
SparkAssertions.assertColumnValueEquals(transformedData, "amount", 1000.0);

// Compare datasets
Dataset<Row> expectedData = sparkClient.readParquet("expected.parquet");
SparkAssertions.assertDatasetEquals(expectedData, transformedData);

// Aggregate data
Dataset<Row> aggregated = SparkTransformations.aggregateByColumn(
    transformedData, "category", "amount", "sum"
);
```

- `queryParam(String name, String value)` - Add a query parameter
- `queryParams(Map<String, String> params)` - Add multiple query parameters
- `pathParam(String name, String value)` - Add a path parameter
- `body(Object body)` - Set request body
- `contentType(String contentType)` - Set content type
- `accept(String acceptType)` - Set accept header
- `timeout(int timeout)` - Set request timeout

## Assertions

The framework includes a custom assertion library for API responses:

```java
ApiAssertion.assertThat(response)
    .hasStatusCode(200)
    .hasContentType("application/json")
    .hasJsonValue("$.id", 1)
    .hasJsonField("$.name")
    .hasResponseTimeLessThan(5000, TimeUnit.MILLISECONDS);
```

### Available Assertions

#### Status and Headers

- `hasStatusCode(int expectedStatusCode)` - Assert status code
- `hasContentType(String expectedContentType)` - Assert content type
- `hasHeader(String headerName, String headerValue)` - Assert header value
- `hasHeader(String headerName)` - Assert header exists

#### JSON Response Body

- `hasJsonValue(String jsonPath, Object expectedValue)` - Assert JSON value
- `hasJsonField(String jsonPath)` - Assert JSON field exists
- `containsJsonPath(String jsonPath)` - Assert JSON path exists
- `jsonPathCount(String jsonPath, int expectedCount)` - Assert count of JSON elements

#### Response Time

- `hasResponseTimeLessThan(long duration, TimeUnit unit)` - Assert response time

#### Soft Assertions

```java
ApiAssertion.softly(response)
    .hasStatusCode(200)
    .hasJsonValue("$.name", "John Doe")
    .hasJsonValue("$.email", "john@example.com")
    .assertAll();  // Will collect all failures and report them together
```

## Database Integration

### SQL Server

```java
// Initialize SQL client
SqlClient sqlClient = new SqlClient(jdbcUrl, username, password);

// Execute query
List<Map<String, Object>> results = sqlClient.executeQuery(
    "SELECT * FROM users WHERE email = ?", 
    "john@example.com"
);

// Assert results
Assert.assertEquals(results.size(), 1);
Assert.assertEquals(results.get(0).get("name"), "John Doe");
```

### Azure Cosmos DB

```java
// Initialize Cosmos DB client
CosmosDBClient cosmosClient = new CosmosDBClient(
    connectionString, 
    databaseName,
    containerName
);

// Execute query
List<JsonObject> results = cosmosClient.executeQuery(
    "SELECT * FROM c WHERE c.email = 'john@example.com'"
);

// Assert results
Assert.assertEquals(results.size(), 1);
Assert.assertEquals(results.get(0).getString("name"), "John Doe");
```

## Logging

The framework uses Log4j2 for logging. Log configuration can be customized in `src/main/resources/log4j2.xml`.

Example of log output:

```
[INFO] Sending GET request to https://jsonplaceholder.typicode.com/users/1
[INFO] Response received with status code 200 in 345ms
[DEBUG] Response body: { "id": 1, "name": "Leanne Graham", ... }
```

## Data-Driven Testing

The framework provides support for data-driven testing from multiple sources:

### CSV Data Source

```java
@DataProvider(name = "csvUserData")
public Object[][] getCsvUserData() {
    String csvFilePath = "src/test/resources/testdata/csv/users.csv";
    return DataProviderUtils.loadCsvData(csvFilePath);
}

@Test(dataProvider = "csvUserData")
public void testWithCsvData(Map<String, String> userData) {
    String userId = userData.get("id");
    String username = userData.get("username");
    // Use the data in your test
}
```

### JSON Data Source

```java
@DataProvider(name = "jsonUserData")
public Object[][] getJsonUserData() {
    String jsonFilePath = "src/test/resources/testdata/json/users.json";
    return DataProviderUtils.loadJsonData(jsonFilePath);
}

@Test(dataProvider = "jsonUserData")
public void testWithJsonData(Map<String, Object> userData) {
    Integer userId = (Integer) userData.get("id");
    String username = (String) userData.get("username");
    
    // Access nested objects
    @SuppressWarnings("unchecked")
    Map<String, String> address = (Map<String, String>) userData.get("address");
    String city = address.get("city");
}
```

### Database Data Source

```java
@DataProvider(name = "dbUserData")
public Object[][] getDbUserData() {
    if (getSqlClient() == null) {
        return new Object[0][0]; // Handle missing DB connection
    }
    
    String query = "SELECT id, name, email FROM users WHERE status = 'active'";
    return DataProviderUtils.loadDatabaseData(getSqlClient(), query);
}

@Test(dataProvider = "dbUserData")
public void testWithDatabaseData(Map<String, Object> userData) {
    String userId = userData.get("id").toString();
    String name = (String) userData.get("name");
    // Use the database data in your test
}
```

## Sample Test Cases

### Basic API Test

```java
@Feature("User API")
public class UserAPITest extends BaseTest {
    private static final Logger logger = LogManager.getLogger(UserAPITest.class);
    private String userId = "1";
    
    @BeforeClass(alwaysRun = true)
    public void setupUserTest() {
        logger.info("Setting up User API tests");
    }
    
    @Test(groups = {"smoke", "api"})
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
            .hasResponseTimeLessThan(5000, TimeUnit.MILLISECONDS);
        
        // Validate response data
        String username = response.jsonPath().getString("username");
        Assert.assertNotNull(username, "Username should not be null");
        logger.info("Fetched user: {}", username);
    }
}
```

### Data-Driven API Test

```java
@Feature("Data-Driven User API Testing")
public class DataDrivenUserAPITest extends BaseTest {
    private static final Logger logger = LogManager.getLogger(DataDrivenUserAPITest.class);
    
    @DataProvider(name = "csvUserData")
    public Object[][] getCsvUserData() {
        String csvFilePath = "src/test/resources/testdata/csv/users.csv";
        return DataProviderUtils.loadCsvData(csvFilePath);
    }
    
    @Test(groups = {"data-driven", "api"}, dataProvider = "csvUserData")
    @Description("Verify fetching users by ID with data from CSV")
    @Story("Data-Driven User Fetch")
    @Severity(SeverityLevel.NORMAL)
    public void testFetchUserByIdFromCsv(Map<String, String> userData) {
        String userId = userData.get("id");
        String expectedName = userData.get("name");
        String expectedEmail = userData.get("email");
        
        logger.info("Testing user with ID: {}, Name: {}", userId, expectedName);
        
        // When
        Response response = getApiClient().get("/users/{id}")
            .pathParam("id", userId)
            .header("Accept", "application/json")
            .send();
        
        // Then
        ApiAssertion.assertThat(response)
            .hasStatusCode(200)
            .hasContentType("application/json");
            
        if (response.getStatusCode() == 200) {
            String actualName = response.jsonPath().getString("name");
            String actualEmail = response.jsonPath().getString("email");
            
            Assert.assertEquals(actualName, expectedName, "User name should match");
            Assert.assertEquals(actualEmail, expectedEmail, "User email should match");
        }
    }
}
```

## Extending the Framework

### Adding New API Methods

To add new API methods, extend the `RestApiClient` class:

```java
public class EnhancedApiClient extends RestApiClient {
    public RequestBuilder head(String endpoint) {
        return new RequestBuilder(RequestMethod.HEAD, endpoint);
    }
}
```

### Adding Custom Assertions

To add custom assertions, extend the `ApiAssertion` class:

```java
public class EnhancedApiAssertion extends ApiAssertion {
    public EnhancedApiAssertion hasSuccessStatus() {
        int statusCode = response.getStatusCode();
        if (statusCode < 200 || statusCode >= 300) {
            failWithMessage("Expected successful status code but got %s", statusCode);
        }
        return this;
    }
}
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.