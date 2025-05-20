package db.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages connection pooling for SQL database connections using HikariCP.
 */
public class HikariCPDataSource {
    private static final Logger logger = LogManager.getLogger(HikariCPDataSource.class);
    private final HikariDataSource dataSource;
    
    /**
     * Creates a HikariCPDataSource with the specified connection parameters.
     *
     * @param jdbcUrl JDBC URL for the SQL Server
     * @param username Database username
     * @param password Database password
     * @param poolSize Connection pool size
     * @param connectionTimeout Connection timeout in milliseconds
     * @param maxLifetime Maximum connection lifetime in milliseconds
     */
    public HikariCPDataSource(String jdbcUrl, String username, String password, int poolSize, int connectionTimeout, int maxLifetime) {
        logger.info("Initializing HikariCP connection pool for: {}", jdbcUrl);
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(poolSize);
        config.setConnectionTimeout(connectionTimeout);
        config.setMaxLifetime(maxLifetime);
        
        // Additional recommended settings for SQL Server
        config.setAutoCommit(true);
        config.addDataSourceProperty("applicationName", "TestAutomationFramework");
        config.addDataSourceProperty("sendStringParametersAsUnicode", "true");
        config.addDataSourceProperty("encrypt", "true");
        config.addDataSourceProperty("trustServerCertificate", "true");
        
        // Connection test query
        config.setConnectionTestQuery("SELECT 1");
        
        dataSource = new HikariDataSource(config);
        logger.info("HikariCP connection pool initialized successfully");
    }
    
    /**
     * Gets a connection from the connection pool.
     *
     * @return Database connection
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    /**
     * Closes the connection pool and releases resources.
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Closing HikariCP connection pool");
            dataSource.close();
        }
    }
}
