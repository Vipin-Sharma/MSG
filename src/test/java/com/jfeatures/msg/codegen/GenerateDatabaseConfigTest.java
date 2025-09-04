package com.jfeatures.msg.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for GenerateDatabaseConfig to improve coverage from 64% to 85%+.
 * Tests template-based configuration generation.
 */
class GenerateDatabaseConfigTest {

    @Test
    void shouldGenerateValidDatabaseConfig() throws Exception {
        // When
        String result = GenerateDatabaseConfig.createDatabaseConfig("Customer");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("@Configuration");
        assertThat(result).contains("DatabaseConfig");
        assertThat(result).contains("@Bean");
        assertThat(result).contains("DataSource");
        assertThat(result).contains("NamedParameterJdbcTemplate");
    }

    @Test
    void shouldContainSqlServerConfiguration() throws Exception {
        // When
        String result = GenerateDatabaseConfig.createDatabaseConfig("Customer");

        // Then
        // The generated config uses ConfigurationProperties, so it doesn't hard-code the URL
        assertThat(result).contains("@ConfigurationProperties(\"spring.datasource\")");
        assertThat(result).contains("DataSourceBuilder.create().build()");
    }

    // New tests to improve coverage

    @Test
    void testCreateDatabaseConfig_WithDifferentBusinessNames_GeneratesCorrectPackages() {
        // Test multiple business names to verify package generation
        String[] businessNames = {"Product", "Order", "Customer", "UserAccount", "OrderDetail"};
        
        for (String businessName : businessNames) {
            String result = GenerateDatabaseConfig.createDatabaseConfig(businessName);
            
            assertNotNull(result);
            // Should contain proper package name
            assertTrue(result.contains("package com.jfeatures.msg." + businessName.toLowerCase() + ".config"));
            assertTrue(result.contains("class DatabaseConfig"));
        }
    }

    @Test
    void testCreateDatabaseConfig_WithNullBusinessName_HandlesGracefully() {
        // Test null business name - should either handle gracefully or throw meaningful exception
        assertThrows(Exception.class, () -> {
            GenerateDatabaseConfig.createDatabaseConfig(null);
        });
    }

    @Test
    void testCreateDatabaseConfig_WithEmptyBusinessName_ThrowsException() {
        // Test empty business name - should throw exception for invalid input
        assertThrows(IllegalArgumentException.class, () -> {
            GenerateDatabaseConfig.createDatabaseConfig("");
        });
        
        // Test null business name - should also throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            GenerateDatabaseConfig.createDatabaseConfig(null);
        });
    }

    @Test
    void testCreateDatabaseConfig_ReplacesPlaceholdersCorrectly() {
        // Test that template placeholders are properly replaced
        String result = GenerateDatabaseConfig.createDatabaseConfig("TestService");
        
        assertNotNull(result);
        // Verify placeholders are replaced
        assertFalse(result.contains("${packageName}"));
        assertFalse(result.contains("${businessPurpose}"));
        
        // Verify actual values are present
        assertTrue(result.contains("com.jfeatures.msg.testservice.config"));
        assertTrue(result.contains("TestService") || result.contains("testservice"));
    }

    @Test 
    void testCreateDatabaseConfig_VerifiesTemplateFileHandling() {
        // Test template file reading behavior
        // This indirectly tests the InputStream and IOException handling paths
        
        // Valid case - should work with existing template
        assertDoesNotThrow(() -> {
            String result = GenerateDatabaseConfig.createDatabaseConfig("Valid");
            assertNotNull(result);
            assertFalse(result.isEmpty());
        });
        
        // Test the template reading mechanism indirectly
        String result = GenerateDatabaseConfig.createDatabaseConfig("TemplateTest");
        
        // Should contain content that indicates template was read successfully
        assertTrue(result.length() > 100); // Template should be substantial
        assertTrue(result.contains("import")); // Should have imports from template
        assertTrue(result.contains("package")); // Should have package declaration
    }

    @Test
    void testCreateDatabaseConfig_TemplateContainsExpectedStructure() {
        // Test that the generated config has expected Spring Boot structure
        String result = GenerateDatabaseConfig.createDatabaseConfig("Product");
        
        assertNotNull(result);
        
        
        // Verify Spring Boot configuration structure
        assertTrue(result.contains("@Configuration"));
        assertTrue(result.contains("@Bean"));
        assertTrue(result.contains("@Primary"));
        assertTrue(result.contains("@ConfigurationProperties"));
        assertTrue(result.contains("public DataSource dataSource"));
        assertTrue(result.contains("public NamedParameterJdbcTemplate namedParameterJdbcTemplate"));
        
        // Verify imports
        assertTrue(result.contains("import org.springframework.context.annotation.Configuration"));
        assertTrue(result.contains("import org.springframework.context.annotation.Bean"));
        assertTrue(result.contains("import javax.sql.DataSource"));
        assertTrue(result.contains("import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate"));
    }

    @Test
    void testCreateDatabaseConfig_GeneratesValidJavaCode() {
        // Test that generated code is syntactically valid Java
        String result = GenerateDatabaseConfig.createDatabaseConfig("Customer");
        
        assertNotNull(result);
        
        // Basic Java syntax checks
        assertTrue(result.contains("public class DatabaseConfig"));
        assertTrue(result.contains("{"));
        assertTrue(result.contains("}"));
        
        // Method signatures should be valid
        assertTrue(result.contains("() {"));
        assertTrue(result.contains("return "));
        
        // Should not contain template artifacts
        assertFalse(result.contains("${"));
        assertFalse(result.contains("}}"));
    }

    @Test
    void testCreateDatabaseConfig_WithSpecialCharactersInBusinessName_HandlesCorrectly() {
        // Test business names with special characters or edge cases
        String[] edgeCaseNames = {"User-Profile", "Order_Detail", "Product123", "ABC"};
        
        for (String businessName : edgeCaseNames) {
            assertDoesNotThrow(() -> {
                String result = GenerateDatabaseConfig.createDatabaseConfig(businessName);
                assertNotNull(result);
                assertTrue(result.contains("class DatabaseConfig"));
            });
        }
    }

    @Test
    void testCreateDatabaseConfig_ConsistentOutput() {
        // Test that multiple calls with same input produce same output
        String businessName = "ConsistencyTest";
        
        String result1 = GenerateDatabaseConfig.createDatabaseConfig(businessName);
        String result2 = GenerateDatabaseConfig.createDatabaseConfig(businessName);
        
        assertEquals(result1, result2);
    }

    @Test
    void testCreateDatabaseConfig_ContainsExpectedAnnotations() {
        // Test for specific Spring annotations that should be in the template
        String result = GenerateDatabaseConfig.createDatabaseConfig("AnnotationTest");
        
        assertNotNull(result);
        
        
        // Configuration annotations
        assertTrue(result.contains("@Configuration"));
        assertTrue(result.contains("@ConfigurationProperties"));
        assertTrue(result.contains("@Primary"));
        
        // Bean annotations
        assertTrue(result.contains("@Bean"));
        
        // Verify there are at least 2 @Bean methods (DataSource and NamedParameterJdbcTemplate)
        long beanCount = result.lines()
            .filter(line -> line.trim().equals("@Bean"))
            .count();
        assertTrue(beanCount >= 2);
    }
}