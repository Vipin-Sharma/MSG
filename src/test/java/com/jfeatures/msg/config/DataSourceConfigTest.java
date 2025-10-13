package com.jfeatures.msg.config;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class DataSourceConfigTest {

    private DataSourceConfig config;

    @BeforeEach
    void setUp() {
        config = new DataSourceConfig();
    }

    @Test
    void testDataSourceConfiguration() {
        // Test that DataSource is configured with correct properties
        DataSource dataSource = config.dataSource();
        DriverManagerDataSource driverManagerDataSource = (DriverManagerDataSource) dataSource;

        // Verify URL contains expected components
        String url = driverManagerDataSource.getUrl();
        assertNotNull(url, "URL should not be null");
        assertTrue(url.contains("jdbc:sqlserver://"), "URL should contain SQL Server JDBC protocol");
        assertTrue(url.contains("localhost:1433"), "URL should contain localhost:1433");
        assertTrue(url.contains("databaseName=sakila"), "URL should contain sakila database");
        assertTrue(url.contains("encrypt=true"), "URL should have encryption enabled");
        assertTrue(url.contains("trustServerCertificate=true"), "URL should trust server certificate");

        // Verify credentials
        assertEquals("sa", driverManagerDataSource.getUsername(), "Username should be 'sa'");
        assertEquals("Password@1", driverManagerDataSource.getPassword(), "Password should be 'Password@1'");
    }

    @Test
    void testDataSourceConnectionAttempt() {
        // Test connection attempt (will fail without actual database, but should not throw during creation)
        DataSource dataSource = config.dataSource();

        assertNotNull(dataSource, "DataSource should be created");

        // Attempting to get connection will fail without actual database running
        // But we can verify the DataSource is properly configured
        try {
            Connection connection = dataSource.getConnection();
            // If we get here, database is running and connection worked
            assertNotNull(connection);
            connection.close();
        } catch (SQLException e) {
            // Expected when database is not running - this is normal
            assertTrue(e.getMessage().contains("TCP/IP connection") ||
                      e.getMessage().contains("Connection refused") ||
                      e.getMessage().contains("No suitable driver") ||
                      e.getMessage().contains("Login failed"),
                      "Should fail with connection-related error when DB is not available");
        }
    }
}
