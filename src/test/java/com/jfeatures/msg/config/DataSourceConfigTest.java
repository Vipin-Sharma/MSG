package com.jfeatures.msg.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DataSourceConfigTest {

    private DataSourceConfig config;

    @BeforeEach
    void setUp() {
        config = new DataSourceConfig();
    }

    @Test
    void testClassHasConfigurationAnnotation() {
        // Verify that the config class has @Configuration annotation
        assertTrue(DataSourceConfig.class.isAnnotationPresent(Configuration.class),
                  "DataSourceConfig should have @Configuration annotation");
    }

    @Test
    void testDataSourceMethodHasBeanAnnotation() throws Exception {
        // Verify that the dataSource method has @Bean annotation
        Method dataSourceMethod = DataSourceConfig.class.getMethod("dataSource");
        assertTrue(dataSourceMethod.isAnnotationPresent(Bean.class),
                  "dataSource method should have @Bean annotation");
    }

    @Test
    void testDataSourceMethodReturnsDataSource() throws Exception {
        // Verify method signature
        Method dataSourceMethod = DataSourceConfig.class.getMethod("dataSource");
        assertEquals(DataSource.class, dataSourceMethod.getReturnType(),
                    "dataSource method should return DataSource");
        assertEquals(0, dataSourceMethod.getParameterCount(),
                    "dataSource method should have no parameters");
    }

    @Test
    void testDataSourceCreation() {
        // Test that dataSource method creates a valid DataSource
        DataSource dataSource = config.dataSource();
        
        assertNotNull(dataSource, "DataSource should not be null");
        assertInstanceOf(DriverManagerDataSource.class, dataSource,
                        "DataSource should be instance of DriverManagerDataSource");
    }

    @Test
    void testDataSourceConfiguration() {
        // Test that DataSource is configured with correct properties
        DataSource dataSource = config.dataSource();
        DriverManagerDataSource driverManagerDataSource = (DriverManagerDataSource) dataSource;
        
        // Verify driver class name (note: getDriverClassName() is not available, 
        // but we can verify the DataSource is properly configured by other means)
        
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
    void testDataSourceIsConfiguredForSqlServer() {
        DataSource dataSource = config.dataSource();
        DriverManagerDataSource driverManagerDataSource = (DriverManagerDataSource) dataSource;
        
        // Verify SQL Server specific configuration  
        assertTrue(driverManagerDataSource.getUrl().contains("sqlserver"),
                  "URL should be for SQL Server");
    }

    @Test
    void testDataSourceSecurityConfiguration() {
        DataSource dataSource = config.dataSource();
        DriverManagerDataSource driverManagerDataSource = (DriverManagerDataSource) dataSource;
        
        String url = driverManagerDataSource.getUrl();
        
        // Verify security settings
        assertTrue(url.contains("encrypt=true"), 
                  "Connection should be encrypted");
        assertTrue(url.contains("trustServerCertificate=true"), 
                  "Should trust server certificate for development");
    }

    @Test
    void testDataSourceForSakilaDatabase() {
        DataSource dataSource = config.dataSource();
        DriverManagerDataSource driverManagerDataSource = (DriverManagerDataSource) dataSource;
        
        // Verify it's configured for Sakila database
        assertTrue(driverManagerDataSource.getUrl().contains("databaseName=sakila"),
                  "Should connect to Sakila database");
    }

    @Test
    void testMultipleDataSourceCalls() {
        // Test that multiple calls return different instances (not singleton)
        DataSource dataSource1 = config.dataSource();
        DataSource dataSource2 = config.dataSource();
        
        assertNotNull(dataSource1);
        assertNotNull(dataSource2);
        // Note: Spring @Bean typically creates singletons, but this tests the method itself
        // Both should be properly configured regardless of instance equality
        
        DriverManagerDataSource ds1 = (DriverManagerDataSource) dataSource1;
        DriverManagerDataSource ds2 = (DriverManagerDataSource) dataSource2;
        
        assertEquals(ds1.getUrl(), ds2.getUrl(), "Both instances should have same URL");
        assertEquals(ds1.getUsername(), ds2.getUsername(), "Both instances should have same username");
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

    @Test
    void testConfigClassFollowsVipinsPrinciple() {
        // Test that the class follows "One public method per class" principle
        Method[] publicMethods = DataSourceConfig.class.getMethods();
        
        // Count non-inherited public methods (excluding Object methods)
        long nonInheritedPublicMethods = java.util.Arrays.stream(publicMethods)
            .filter(method -> method.getDeclaringClass() == DataSourceConfig.class)
            .count();
        
        assertEquals(1, nonInheritedPublicMethods, 
                    "Should have exactly one public method (following Vipin's Principle)");
    }

    @Test
    void testDataSourceMethodIsPublic() throws Exception {
        Method dataSourceMethod = DataSourceConfig.class.getMethod("dataSource");
        assertTrue(java.lang.reflect.Modifier.isPublic(dataSourceMethod.getModifiers()),
                  "dataSource method should be public");
    }
}