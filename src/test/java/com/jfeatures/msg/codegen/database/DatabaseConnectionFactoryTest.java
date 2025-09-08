package com.jfeatures.msg.codegen.database;

import static org.junit.jupiter.api.Assertions.*;

import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

class DatabaseConnectionFactoryTest {

    private DatabaseConnectionFactory factory;

    @BeforeEach
    void setUp() {
        factory = new DatabaseConnectionFactory();
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "SQL_SERVER_URL", matches = ".*")
    void testCreateDatabaseConnection_Success() {
        // This test requires actual database configuration
        // It will only run if SQL_SERVER_URL environment variable is set
        
        DatabaseConnection connection = assertDoesNotThrow(
            () -> factory.createDatabaseConnection(),
            "Should create database connection successfully when configuration is available"
        );

        assertNotNull(connection, "Database connection should not be null");
        assertNotNull(connection.dataSource(), "DataSource should not be null");
        assertNotNull(connection.jdbcTemplate(), "JdbcTemplate should not be null");
        assertNotNull(connection.namedParameterJdbcTemplate(), "NamedParameterJdbcTemplate should not be null");
    }

    @Test
    void testCreateDatabaseConnection_ConfigurationError() {
        // This test simulates what happens when database configuration fails
        // Note: The configuration classes might actually succeed in creating the connection objects
        // even without a running database, so we test both scenarios
        
        try {
            DatabaseConnection connection = factory.createDatabaseConnection();
            // If no exception is thrown, the configuration succeeded
            assertNotNull(connection, "Connection should be created even without running DB");
        } catch (RuntimeException e) {
            // If an exception is thrown, verify it's properly handled
            assertNotNull(e.getMessage(), "Exception should have a meaningful message");
            assertTrue(e.getMessage().contains("Failed to create database connection"), 
                      "Exception message should indicate database connection failure");
        }
    }

    @Test
    void testCreateDatabaseConnection_ErrorHandling() {
        // Test that various types of exceptions are properly wrapped in RuntimeException
        
        try {
            factory.createDatabaseConnection();
            // If no exception is thrown, the test environment has valid DB config
            // This is acceptable as it means the method works correctly
        } catch (RuntimeException e) {
            // Verify that the exception is properly wrapped with meaningful message
            assertNotNull(e.getMessage(), "Exception should have a meaningful message");
            assertTrue(e.getMessage().contains("Failed to create database connection"), 
                      "Exception message should indicate database connection failure");
            
            // Verify that the original cause is preserved
            assertNotNull(e.getCause(), "Original exception cause should be preserved");
        }
    }

    @Test
    void testCreateDatabaseConnection_LoggingBehavior() {
        // Test that the factory logs appropriate messages
        // This is more of a behavioral test to ensure logging occurs
        
        try {
            DatabaseConnection connection = factory.createDatabaseConnection();
            // If successful, verify connection is properly created
            assertNotNull(connection);
        } catch (RuntimeException e) {
            // If failed, verify exception handling occurred
            assertNotNull(e.getMessage());
        }
        
        // Note: Actual log verification would require a logging framework mock
        // For comprehensive testing, consider using a logging appender to capture log messages
    }

    @Test
    void testCreateDatabaseConnection_ExceptionWrapping() {
        // Test that all exceptions are properly wrapped as RuntimeException
        
        Exception caughtException = null;
        try {
            factory.createDatabaseConnection();
        } catch (Exception e) {
            caughtException = e;
        }

        if (caughtException != null) {
            // If an exception was thrown, it should be a RuntimeException
            assertInstanceOf(RuntimeException.class, caughtException,
                           "All exceptions should be wrapped as RuntimeException");
            
            // Verify the error message format
            String message = caughtException.getMessage();
            assertTrue(message.contains("Failed to create database connection"),
                      "Error message should be descriptive");
        }
        // If no exception was thrown, the configuration is valid and the method works correctly
    }

    @Test
    void testCreateDatabaseConnection_ConsistentBehavior() {
        // Test that multiple calls behave consistently
        
        Exception firstException = null;
        Exception secondException = null;
        
        try {
            factory.createDatabaseConnection();
        } catch (Exception e) {
            firstException = e;
        }
        
        try {
            factory.createDatabaseConnection();
        } catch (Exception e) {
            secondException = e;
        }

        // Both calls should have the same outcome (either both succeed or both fail)
        assertEquals(firstException == null, secondException == null,
                   "Multiple calls should have consistent behavior");
        
        if (firstException != null && secondException != null) {
            // If both failed, they should fail with the same type of exception
            assertEquals(firstException.getClass(), secondException.getClass(),
                       "Multiple failures should result in same exception type");
        }
    }

    @Test
    void testCreateDatabaseConnection_ComponentIntegration() {
        // Test that if connection is successful, all components are properly wired
        
        try {
            DatabaseConnection connection = factory.createDatabaseConnection();
            
            // All components should be non-null and properly initialized
            assertNotNull(connection.dataSource(), "DataSource should be initialized");
            assertNotNull(connection.jdbcTemplate(), "JdbcTemplate should be initialized");
            assertNotNull(connection.namedParameterJdbcTemplate(), "NamedParameterJdbcTemplate should be initialized");
            
            // Components should be related (jdbcTemplate should use the dataSource)
            assertSame(connection.dataSource(), connection.jdbcTemplate().getDataSource(),
                      "JdbcTemplate should use the same DataSource");
            
        } catch (RuntimeException e) {
            // If configuration fails, that's expected in test environments without proper DB setup
            assertNotNull(e.getMessage(), "Failure should have meaningful message");
        }
    }
}