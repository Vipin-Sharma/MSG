package com.jfeatures.msg.e2e;

import com.jfeatures.msg.codegen.MicroServiceGenerator;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import org.junit.jupiter.api.*;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simplified End-to-End test for MSG CRUD generation workflow.
 * Tests code generation without requiring Docker/Testcontainers.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Simple E2E CRUD Generation Tests")
class SimpleE2EGenerationTest {

    private static Path testOutputDir;
    private static GeneratedMicroserviceValidator validator;
    private final String businessName = "TestCustomer";

    @BeforeAll
    static void setupTestEnvironment() throws IOException {
        // Create unique test output directory
        testOutputDir = Files.createTempDirectory("msg-simple-e2e");
        validator = new GeneratedMicroserviceValidator(testOutputDir);
        
        System.out.println("🚀 Starting Simple E2E Test Suite");
        System.out.println("Test output directory: " + testOutputDir);
    }

    @AfterAll
    static void cleanupTestEnvironment() throws IOException {
        // Clean up test output directory
        if (testOutputDir != null && Files.exists(testOutputDir)) {
            Files.walk(testOutputDir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete: " + path);
                        }
                    });
        }
        System.out.println("✅ E2E Test cleanup completed");
    }

    @Test
    @Order(1)
    @DisplayName("1. Generate SELECT CRUD API")
    void testGenerateSelectCrudApi() {
        System.out.println("📋 Testing SELECT CRUD API generation...");
        
        // Run generation command for SELECT
        String[] args = {
            "--name", businessName + "Select", 
            "--destination", testOutputDir.toString(),
            "--sql-file", "customer_select.sql"
        };
        
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine commandLine = new CommandLine(generator);
        
        int exitCode = commandLine.execute(args);
        
        assertThat(exitCode)
                .as("SELECT generation should complete successfully")
                .isEqualTo(0);
        
        // Validate basic structure
        validator.validateBasicStructure(businessName + "Select", SqlStatementType.SELECT);
        
        System.out.println("✅ SELECT CRUD API generation test passed");
    }

    @Test
    @Order(2)
    @DisplayName("2. Generate INSERT CRUD API")  
    void testGenerateInsertCrudApi() {
        System.out.println("📋 Testing INSERT CRUD API generation...");
        
        String[] args = {
            "--name", businessName + "Insert", 
            "--destination", testOutputDir.toString(),
            "--sql-file", "customer_insert.sql"
        };
        
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine commandLine = new CommandLine(generator);
        
        int exitCode = commandLine.execute(args);
        
        assertThat(exitCode)
                .as("INSERT generation should complete successfully")
                .isEqualTo(0);
        
        // Validate basic structure
        validator.validateBasicStructure(businessName + "Insert", SqlStatementType.INSERT);
        
        System.out.println("✅ INSERT CRUD API generation test passed");
    }

    @Test
    @Order(3)
    @DisplayName("3. Generate UPDATE CRUD API")
    void testGenerateUpdateCrudApi() {
        System.out.println("📋 Testing UPDATE CRUD API generation...");
        
        String[] args = {
            "--name", businessName + "Update", 
            "--destination", testOutputDir.toString(),
            "--sql-file", "customer_update.sql"
        };
        
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine commandLine = new CommandLine(generator);
        
        int exitCode = commandLine.execute(args);
        
        assertThat(exitCode)
                .as("UPDATE generation should complete successfully")
                .isEqualTo(0);
        
        // Validate basic structure
        validator.validateBasicStructure(businessName + "Update", SqlStatementType.UPDATE);
        
        System.out.println("✅ UPDATE CRUD API generation test passed");
    }

    @Test
    @Order(4)
    @DisplayName("4. Generate DELETE CRUD API")
    void testGenerateDeleteCrudApi() {
        System.out.println("📋 Testing DELETE CRUD API generation...");
        
        String[] args = {
            "--name", businessName + "Delete", 
            "--destination", testOutputDir.toString(),
            "--sql-file", "customer_delete.sql"
        };
        
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine commandLine = new CommandLine(generator);
        
        int exitCode = commandLine.execute(args);
        
        assertThat(exitCode)
                .as("DELETE generation should complete successfully")
                .isEqualTo(0);
        
        // Validate basic structure
        validator.validateBasicStructure(businessName + "Delete", SqlStatementType.DELETE);
        
        System.out.println("✅ DELETE CRUD API generation test passed");
    }

    @Test
    @Order(5)
    @DisplayName("5. Validate Complete Project Structure")
    void testCompleteProjectStructure() {
        System.out.println("📋 Testing complete project structure validation...");
        
        // Validate each generated project has proper structure
        validator.validateCompleteProjectStructure(businessName + "Select");
        validator.validateCompleteProjectStructure(businessName + "Insert");
        validator.validateCompleteProjectStructure(businessName + "Update");
        validator.validateCompleteProjectStructure(businessName + "Delete");
        
        System.out.println("✅ Complete project structure validation passed");
    }

    @Test
    @Order(6)
    @DisplayName("6. Validate Generated Java Classes")
    void testGeneratedJavaClasses() {
        System.out.println("📋 Testing generated Java classes...");
        
        // Validate Java class structure for each operation
        validator.validateJavaClasses(businessName + "Select");
        validator.validateJavaClasses(businessName + "Insert");
        validator.validateJavaClasses(businessName + "Update");
        validator.validateJavaClasses(businessName + "Delete");
        
        System.out.println("✅ Generated Java classes validation passed");
    }

    @Test
    @Order(7)
    @DisplayName("7. Validate Spring Boot Configuration")
    void testSpringBootConfiguration() {
        System.out.println("📋 Testing Spring Boot configuration...");
        
        // Validate Spring Boot config for each project
        validator.validateSpringBootConfiguration(businessName + "Select");
        validator.validateSpringBootConfiguration(businessName + "Insert");
        validator.validateSpringBootConfiguration(businessName + "Update");
        validator.validateSpringBootConfiguration(businessName + "Delete");
        
        System.out.println("✅ Spring Boot configuration validation passed");
    }

    @Test
    @Order(8)
    @DisplayName("8. Validate API Endpoint Mappings")
    void testApiEndpointMappings() {
        System.out.println("📋 Testing API endpoint mappings...");
        
        // Validate API endpoints for each operation
        validator.validateApiEndpointMappings(businessName + "Select");
        validator.validateApiEndpointMappings(businessName + "Insert");
        validator.validateApiEndpointMappings(businessName + "Update");
        validator.validateApiEndpointMappings(businessName + "Delete");
        
        System.out.println("✅ API endpoint mappings validation passed");
    }

    @Test
    @Order(9)
    @DisplayName("9. Validate Business Domain Separation")
    void testBusinessDomainSeparation() {
        System.out.println("📋 Testing business domain separation...");
        
        // Validate that each business domain is properly separated
        validator.validateBusinessDomainSeparation(businessName + "Select");
        validator.validateBusinessDomainSeparation(businessName + "Insert");
        validator.validateBusinessDomainSeparation(businessName + "Update");
        validator.validateBusinessDomainSeparation(businessName + "Delete");
        
        System.out.println("✅ Business domain separation validation passed");
    }

    @Test
    @Order(10)
    @DisplayName("10. Test Error Handling and Edge Cases")
    void testErrorHandlingAndEdgeCases() {
        System.out.println("📋 Testing error handling and edge cases...");
        
        // Test with invalid business name
        String[] invalidNameArgs = {
            "--name", "", 
            "--destination", testOutputDir.toString()
        };
        
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine commandLine = new CommandLine(generator);
        
        int exitCode = commandLine.execute(invalidNameArgs);
        
        assertThat(exitCode)
                .as("Invalid business name should fail gracefully")
                .isNotEqualTo(0);
        
        // Test with invalid destination path
        String[] invalidPathArgs = {
            "--name", "TestBusiness", 
            "--destination", "/invalid/path/that/does/not/exist/really/long/path"
        };
        
        generator = new MicroServiceGenerator();
        commandLine = new CommandLine(generator);
        
        exitCode = commandLine.execute(invalidPathArgs);
        
        assertThat(exitCode)
                .as("Invalid destination path should fail gracefully")
                .isNotEqualTo(0);
        
        System.out.println("✅ Error handling and edge cases validation passed");
    }
}