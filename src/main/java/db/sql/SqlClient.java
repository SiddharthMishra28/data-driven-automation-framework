package db.sql;

import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.LoggerUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client for interacting with MS SQL Server database.
 * Provides methods for executing SQL queries and managing connections.
 */
public class SqlClient {
    private static final Logger logger = LogManager.getLogger(SqlClient.class);
    private final HikariCPDataSource dataSource;
    
    /**
     * Creates a SqlClient using the specified connection parameters.
     *
     * @param jdbcUrl JDBC URL for the SQL Server
     * @param username Database username
     * @param password Database password
     * @param poolSize Connection pool size
     * @param connectionTimeout Connection timeout in milliseconds
     * @param maxLifetime Maximum connection lifetime in milliseconds
     */
    public SqlClient(String jdbcUrl, String username, String password, int poolSize, int connectionTimeout, int maxLifetime) {
        logger.info("Initializing SQL client for: {}", jdbcUrl);
        this.dataSource = new HikariCPDataSource(jdbcUrl, username, password, poolSize, connectionTimeout, maxLifetime);
    }
    
    /**
     * Creates a SqlClient using the specified connection parameters with default pool settings.
     *
     * @param jdbcUrl JDBC URL for the SQL Server
     * @param username Database username
     * @param password Database password
     */
    public SqlClient(String jdbcUrl, String username, String password) {
        this(jdbcUrl, username, password, 10, 30000, 1800000);
    }
    
    /**
     * Executes a SQL query and returns the results.
     *
     * @param query SQL query string
     * @return List of maps where each map represents a row with column name to value mapping
     */
    @Step("Execute SQL query")
    public List<Map<String, Object>> executeQuery(String query) {
        logger.debug("Executing SQL query: {}", query);
        LoggerUtils.logDatabaseQuery("SQL", query);
        
        List<Map<String, Object>> resultList = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    
                    // Handle specific data types
                    if (value instanceof Timestamp) {
                        value = ((Timestamp) value).toLocalDateTime();
                    }
                    
                    row.put(columnName, value);
                }
                resultList.add(row);
            }
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            logger.debug("SQL query returned {} rows in {}ms", resultList.size(), executionTime);
            LoggerUtils.logDatabaseResult("SQL", resultList.size(), executionTime);
            
            return resultList;
        } catch (SQLException e) {
            logger.error("Error executing SQL query: {}", e.getMessage(), e);
            throw new RuntimeException("Error executing SQL query", e);
        }
    }
    
    /**
     * Executes a parameterized SQL query and returns the results.
     *
     * @param query SQL query string with parameter placeholders
     * @param params Parameter values in order of placeholders
     * @return List of maps where each map represents a row with column name to value mapping
     */
    @Step("Execute parameterized SQL query")
    public List<Map<String, Object>> executeQuery(String query, Object... params) {
        logger.debug("Executing parameterized SQL query: {}", query);
        LoggerUtils.logDatabaseQuery("SQL", query, params);
        
        List<Map<String, Object>> resultList = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                setParameter(statement, i + 1, params[i]);
            }
            
            try (ResultSet resultSet = statement.executeQuery()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = resultSet.getObject(i);
                        
                        // Handle specific data types
                        if (value instanceof Timestamp) {
                            value = ((Timestamp) value).toLocalDateTime();
                        }
                        
                        row.put(columnName, value);
                    }
                    resultList.add(row);
                }
            }
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            logger.debug("Parameterized SQL query returned {} rows in {}ms", resultList.size(), executionTime);
            LoggerUtils.logDatabaseResult("SQL", resultList.size(), executionTime);
            
            return resultList;
        } catch (SQLException e) {
            logger.error("Error executing parameterized SQL query: {}", e.getMessage(), e);
            throw new RuntimeException("Error executing parameterized SQL query", e);
        }
    }
    
    /**
     * Executes a SQL update (INSERT, UPDATE, DELETE) and returns the number of affected rows.
     *
     * @param query SQL update statement
     * @return Number of rows affected
     */
    @Step("Execute SQL update")
    public int executeUpdate(String query) {
        logger.debug("Executing SQL update: {}", query);
        LoggerUtils.logDatabaseQuery("SQL_UPDATE", query);
        
        long startTime = System.currentTimeMillis();
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            int rowsAffected = statement.executeUpdate(query);
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            logger.debug("SQL update affected {} rows in {}ms", rowsAffected, executionTime);
            LoggerUtils.logDatabaseResult("SQL_UPDATE", rowsAffected, executionTime);
            
            return rowsAffected;
        } catch (SQLException e) {
            logger.error("Error executing SQL update: {}", e.getMessage(), e);
            throw new RuntimeException("Error executing SQL update", e);
        }
    }
    
    /**
     * Executes a parameterized SQL update (INSERT, UPDATE, DELETE) and returns the number of affected rows.
     *
     * @param query SQL update statement with parameter placeholders
     * @param params Parameter values in order of placeholders
     * @return Number of rows affected
     */
    @Step("Execute parameterized SQL update")
    public int executeUpdate(String query, Object... params) {
        logger.debug("Executing parameterized SQL update: {}", query);
        LoggerUtils.logDatabaseQuery("SQL_UPDATE", query, params);
        
        long startTime = System.currentTimeMillis();
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                setParameter(statement, i + 1, params[i]);
            }
            
            int rowsAffected = statement.executeUpdate();
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            logger.debug("Parameterized SQL update affected {} rows in {}ms", rowsAffected, executionTime);
            LoggerUtils.logDatabaseResult("SQL_UPDATE", rowsAffected, executionTime);
            
            return rowsAffected;
        } catch (SQLException e) {
            logger.error("Error executing parameterized SQL update: {}", e.getMessage(), e);
            throw new RuntimeException("Error executing parameterized SQL update", e);
        }
    }
    
    /**
     * Executes a SQL query and returns a single value from the first row and column.
     *
     * @param query SQL query string
     * @return Object representing the single value, or null if no rows returned
     */
    public Object executeScalar(String query) {
        List<Map<String, Object>> results = executeQuery(query);
        if (results.isEmpty()) {
            return null;
        }
        
        Map<String, Object> row = results.get(0);
        if (row.isEmpty()) {
            return null;
        }
        
        return row.values().iterator().next();
    }
    
    /**
     * Executes a parameterized SQL query and returns a single value from the first row and column.
     *
     * @param query SQL query string with parameter placeholders
     * @param params Parameter values in order of placeholders
     * @return Object representing the single value, or null if no rows returned
     */
    public Object executeScalar(String query, Object... params) {
        List<Map<String, Object>> results = executeQuery(query, params);
        if (results.isEmpty()) {
            return null;
        }
        
        Map<String, Object> row = results.get(0);
        if (row.isEmpty()) {
            return null;
        }
        
        return row.values().iterator().next();
    }
    
    /**
     * Checks if a table exists in the database.
     *
     * @param tableName Table name to check
     * @return true if the table exists, false otherwise
     */
    public boolean tableExists(String tableName) {
        try (Connection connection = dataSource.getConnection()) {
            java.sql.DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet resultSet = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking if table exists: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Closes the SQL client and releases database connections.
     */
    public void close() {
        if (dataSource != null) {
            logger.info("Closing SQL client");
            dataSource.close();
        }
    }
    
    /**
     * Sets a parameter on a PreparedStatement with appropriate type handling.
     *
     * @param statement PreparedStatement to set parameter on
     * @param parameterIndex 1-based index of the parameter
     * @param value Value to set
     * @throws SQLException if a database access error occurs
     */
    private void setParameter(PreparedStatement statement, int parameterIndex, Object value) throws SQLException {
        if (value == null) {
            statement.setNull(parameterIndex, java.sql.Types.NULL);
        } else if (value instanceof String) {
            statement.setString(parameterIndex, (String) value);
        } else if (value instanceof Integer) {
            statement.setInt(parameterIndex, (Integer) value);
        } else if (value instanceof Long) {
            statement.setLong(parameterIndex, (Long) value);
        } else if (value instanceof Double) {
            statement.setDouble(parameterIndex, (Double) value);
        } else if (value instanceof Boolean) {
            statement.setBoolean(parameterIndex, (Boolean) value);
        } else if (value instanceof LocalDateTime) {
            statement.setTimestamp(parameterIndex, Timestamp.valueOf((LocalDateTime) value));
        } else if (value instanceof LocalDate) {
            statement.setDate(parameterIndex, java.sql.Date.valueOf((LocalDate) value));
        } else {
            statement.setObject(parameterIndex, value);
        }
    }
}
