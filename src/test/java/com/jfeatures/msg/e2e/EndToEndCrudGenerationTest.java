package com.jfeatures.msg.e2e;

import com.jfeatures.msg.codegen.MicroServiceGenerator;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive End-to-End test for MSG CRUD generation workflow.
 * Tests the complete flow from SQL files to generated Spring Boot microservices.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@DisplayName("End-to-End CRUD Generation Tests")
class EndToEndCrudGenerationTest {

    @Container
    static MSSQLServerContainer<?> sqlServer = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
            .withPassword("TestPassword@123")
            .withInitScript("e2e/sakila-test-schema.sql")
            .withReuse(true);

    private static Path testOutputDir;
    private static GeneratedMicroserviceValidator validator;
    private static ApiEndpointTester apiTester;

    @BeforeAll
    static void setupTestEnvironment() throws IOException {
        // Create unique test output directory
        testOutputDir = Files.createTempDirectory("msg-e2e-test");
        validator = new GeneratedMicroserviceValidator(testOutputDir);
        apiTester = new ApiEndpointTester();
        
        // Set database connection properties for tests
        System.setProperty("spring.datasource.url", sqlServer.getJdbcUrl());
        System.setProperty("spring.datasource.username", sqlServer.getUsername());
        System.setProperty("spring.datasource.password", sqlServer.getPassword());
        System.setProperty("spring.datasource.driver-class-name", sqlServer.getDriverClassName());
        
        System.out.println("Test database URL: " + sqlServer.getJdbcUrl());
        System.out.println("Test output directory: " + testOutputDir);
    }

    @AfterAll
    static void cleanupTestEnvironment() throws IOException {
        if (testOutputDir != null && Files.exists(testOutputDir)) {
            // Clean up test output directory
            Files.walk(testOutputDir)
                    .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete: " + path);
                        }
                    });
        }
    }

    @Test
    @Order(1)
    @DisplayName("Complete CRUD Generation Flow")
    void testCompleteCrudGeneration() {
        // Test SELECT API generation
        assertThat(generateAndValidateApi("Customer", "customer_select.sql", SqlStatementType.SELECT))
                .as("SELECT API generation should succeed")
                .isEqualTo(0);

        // Test INSERT API generation
        assertThat(generateAndValidateApi("Customer", "customer_insert.sql", SqlStatementType.INSERT))
                .as("INSERT API generation should succeed")
                .isEqualTo(0);

        // Test UPDATE API generation
        assertThat(generateAndValidateApi("Customer", "customer_update.sql", SqlStatementType.UPDATE))
                .as("UPDATE API generation should succeed")
                .isEqualTo(0);

        // Test DELETE API generation
        assertThat(generateAndValidateApi("Customer", "customer_delete.sql", SqlStatementType.DELETE))
                .as("DELETE API generation should succeed")
                .isEqualTo(0);

        // Validate complete project structure
        validator.validateCompleteProjectStructure("Customer");
    }

    @Test
    @Order(2)
    @DisplayName("Generated Code Structure Validation")
    void testGeneratedCodeStructure() {
        String businessName = "Customer";
        
        // Verify Maven project structure
        validator.validateMavenProjectStructure(businessName);
        
        // Verify Java classes are generated correctly
        validator.validateJavaClasses(businessName);
        
        // Verify Spring Boot configuration
        validator.validateSpringBootConfiguration(businessName);
        
        // Verify API endpoint mappings
        validator.validateApiEndpointMappings(businessName);
    }

    @Test
    @Order(3)
    @DisplayName("Generated Code Compilation")
    void testGeneratedCodeCompiles() {
        String businessName = "Customer";
        Path projectRoot = testOutputDir.resolve(businessName.toLowerCase());
        
        assertThat(validator.compileGeneratedProject(projectRoot))
                .as("Generated code should compile without errors")
                .isTrue();
    }

    @Test
    @Order(4)
    @DisplayName("Generated APIs Integration Test")
    void testGeneratedApisIntegration() throws Exception {
        String businessName = "Customer";
        Path projectRoot = testOutputDir.resolve(businessName.toLowerCase());
        
        // Start the generated microservice
        Process microserviceProcess = apiTester.startGeneratedMicroservice(projectRoot);
        
        try {
            // Wait for service to start
            Thread.sleep(10000);
            
            // Test all CRUD endpoints
            apiTester.testCreateCustomerEndpoint();
            apiTester.testGetCustomerEndpoint();
            apiTester.testUpdateCustomerEndpoint();
            apiTester.testDeleteCustomerEndpoint();
            
        } finally {
            // Cleanup: stop the microservice
            apiTester.stopMicroservice(microserviceProcess);
        }
    }

    @Test
    @Order(5)
    @DisplayName("Multiple Business Domains Generation")
    void testMultipleBusinessDomainsGeneration() {
        // Test generating multiple business domains
        String[] businessDomains = {"Customer", "Product", "Order"};
        
        for (String domain : businessDomains) {
            int exitCode = generateAndValidateApi(domain, "customer_select.sql", SqlStatementType.SELECT);
            assertThat(exitCode)
                    .as("Generation should succeed for domain: " + domain)
                    .isEqualTo(0);
                    
            // Verify each domain has its own directory and files
            validator.validateBusinessDomainSeparation(domain);
        }
    }

    @Test
    @Order(6)
    @DisplayName("Error Handling and Edge Cases")
    void testErrorHandlingAndEdgeCases() {
        // Test invalid SQL file
        assertThat(generateApi("InvalidDomain", "non_existent.sql"))
                .as("Should handle non-existent SQL files gracefully")
                .isNotEqualTo(0);

        // Test invalid business name
        assertThat(generateApi("", "customer_select.sql"))
                .as("Should handle empty business names gracefully")
                .isNotEqualTo(0);

        // Test invalid destination path
        assertThat(generateApiWithDestination("Customer", "customer_select.sql", "/invalid/path/that/does/not/exist"))
                .as("Should handle invalid paths gracefully")
                .isNotEqualTo(0);
    }

    /**
     * Generates API and validates the basic structure.
     */
    private int generateAndValidateApi(String businessName, String sqlFile, SqlStatementType expectedType) {
        int exitCode = generateApi(businessName, sqlFile);
        
        if (exitCode == 0) {
            // Validate basic structure was created
            validator.validateBasicStructure(businessName, expectedType);
        }
        
        return exitCode;
    }

    /**
     * Executes the MSG generator with specified parameters.
     */
    private int generateApi(String businessName, String sqlFile) {
        return generateApiWithDestination(businessName, sqlFile, testOutputDir.toString());
    }

    /**
     * Executes the MSG generator with custom destination.
     */
    private int generateApiWithDestination(String businessName, String sqlFile, String destination) {
        String[] args = {
                "--name", businessName,
                "--destination", destination,
                "--sql-file", sqlFile
        };

        try {
            return new CommandLine(new MicroServiceGenerator()).execute(args);
        } catch (Exception e) {
            System.err.println("Failed to execute generator: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
}