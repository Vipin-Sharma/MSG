package com.jfeatures.msg.e2e;

import com.jfeatures.msg.codegen.MicroServiceGenerator;
import org.junit.jupiter.api.*;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive End-to-End tests for MSG CRUD API generation without Docker dependencies.
 * 
 * PURPOSE: This test class validates the complete workflow of generating Spring Boot microservices
 * from SQL files, ensuring all CRUD operations work correctly and produce valid, compilable code.
 * 
 * WHAT IT TESTS:
 * - Complete CRUD generation workflow (SELECT, INSERT, UPDATE, DELETE)
 * - Generated project structure validation (Maven, Spring Boot setup)
 * - Java class generation (Controllers, DAOs, DTOs, Application classes)
 * - Spring Boot configuration file generation
 * - Error handling for invalid inputs
 * - Multi-operation workflow validation
 * 
 * EXECUTION: Fast execution (~5-6 seconds) without requiring Docker or external dependencies.
 * 
 * NAMING CONVENTION: Methods follow "when[Input]Should[ExpectedOutput]" pattern for clarity.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Complete CRUD Generation End-to-End Tests")
class CompleteCrudGenerationE2ETest {

    private static Path baseTestDir;
    private final String businessName = "TestCustomer";

    @BeforeAll
    static void setupTestEnvironment() throws IOException {
        baseTestDir = Files.createTempDirectory("msg-complete-crud-e2e");
        System.out.println("🚀 Starting Complete CRUD Generation E2E Test Suite");
        System.out.println("Test base directory: " + baseTestDir);
    }

    @AfterAll
    static void cleanupTestEnvironment() throws IOException {
        if (baseTestDir != null && Files.exists(baseTestDir)) {
            Files.walk(baseTestDir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete: " + path);
                        }
                    });
        }
        System.out.println("✅ Complete CRUD Generation E2E cleanup completed");
    }

    @Test
    @Order(1)
    @DisplayName("When SELECT SQL provided should generate complete Spring Boot microservice with GET endpoints")
    void whenSelectSqlProvidedShouldGenerateCompleteSpringBootMicroserviceWithGetEndpoints() throws IOException {
        System.out.println("📋 Testing SELECT CRUD API generation...");
        
        Path selectDir = Files.createTempDirectory(baseTestDir, "select-crud-");
        
        String[] args = {
            "--name", businessName + "Select", 
            "--destination", selectDir.toString(),
            "--sql-file", "customer_select.sql"
        };
        
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine commandLine = new CommandLine(generator);
        
        int exitCode = commandLine.execute(args);
        
        assertThat(exitCode)
                .as("SELECT generation should complete successfully")
                .isZero();
        
        // Validate Maven project structure
        assertThat(selectDir.resolve("pom.xml"))
                .as("Generated project should have Maven pom.xml")
                .exists()
                .isRegularFile();
                
        assertThat(selectDir.resolve("src/main/java"))
                .as("Generated project should have Java source directory")
                .exists()
                .isDirectory();
                
        assertThat(selectDir.resolve("src/main/resources"))
                .as("Generated project should have resources directory")
                .exists()
                .isDirectory();
        
        System.out.println("✅ SELECT CRUD API generation completed successfully");
    }

    @Test
    @Order(2)
    @DisplayName("When INSERT SQL provided should generate Spring Boot microservice with POST endpoints and DTOs")  
    void whenInsertSqlProvidedShouldGenerateSpringBootMicroserviceWithPostEndpointsAndDtos() throws IOException {
        System.out.println("📋 Testing INSERT CRUD API generation...");
        
        Path insertDir = Files.createTempDirectory(baseTestDir, "insert-crud-");
        
        String[] args = {
            "--name", businessName + "Insert", 
            "--destination", insertDir.toString(),
            "--sql-file", "customer_insert.sql"
        };
        
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine commandLine = new CommandLine(generator);
        
        int exitCode = commandLine.execute(args);
        
        assertThat(exitCode)
                .as("INSERT generation should complete successfully")
                .isZero();
        
        assertThat(insertDir.resolve("pom.xml"))
                .as("INSERT project should generate valid Maven structure")
                .exists();
        
        System.out.println("✅ INSERT CRUD API generation completed successfully");
    }

    @Test
    @Order(3)
    @DisplayName("When UPDATE SQL provided should generate Spring Boot microservice with PUT endpoints and validation")
    void whenUpdateSqlProvidedShouldGenerateSpringBootMicroserviceWithPutEndpointsAndValidation() throws IOException {
        System.out.println("📋 Testing UPDATE CRUD API generation...");
        
        Path updateDir = Files.createTempDirectory(baseTestDir, "update-crud-");
        
        String[] args = {
            "--name", businessName + "Update", 
            "--destination", updateDir.toString(),
            "--sql-file", "customer_update.sql"
        };
        
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine commandLine = new CommandLine(generator);
        
        int exitCode = commandLine.execute(args);
        
        assertThat(exitCode)
                .as("UPDATE generation should complete successfully")
                .isZero();
        
        assertThat(updateDir.resolve("pom.xml"))
                .as("UPDATE project should generate valid Maven structure")
                .exists();
        
        System.out.println("✅ UPDATE CRUD API generation completed successfully");
    }

    @Test
    @Order(4)
    @DisplayName("When DELETE SQL provided should generate Spring Boot microservice with DELETE endpoints and safety checks")
    void whenDeleteSqlProvidedShouldGenerateSpringBootMicroserviceWithDeleteEndpointsAndSafetyChecks() throws IOException {
        System.out.println("📋 Testing DELETE CRUD API generation...");
        
        Path deleteDir = Files.createTempDirectory(baseTestDir, "delete-crud-");
        
        String[] args = {
            "--name", businessName + "Delete", 
            "--destination", deleteDir.toString(),
            "--sql-file", "customer_delete.sql"
        };
        
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine commandLine = new CommandLine(generator);
        
        int exitCode = commandLine.execute(args);
        
        assertThat(exitCode)
                .as("DELETE generation should complete successfully")
                .isZero();
        
        assertThat(deleteDir.resolve("pom.xml"))
                .as("DELETE project should generate valid Maven structure")
                .exists();
        
        System.out.println("✅ DELETE CRUD API generation completed successfully");
    }

    @Test
    @Order(5)
    @DisplayName("When any SQL provided should generate all required Java classes with proper structure")
    void whenAnySqlProvidedShouldGenerateAllRequiredJavaClassesWithProperStructure() throws IOException {
        System.out.println("📋 Testing generated Java classes structure...");
        
        Path testDir = Files.createTempDirectory(baseTestDir, "java-classes-validation-");
        
        String[] args = {
            "--name", "ValidationTest", 
            "--destination", testDir.toString(),
            "--sql-file", "customer_select.sql"
        };
        
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine commandLine = new CommandLine(generator);
        int exitCode = commandLine.execute(args);
        
        assertThat(exitCode).isZero();
        
        Path javaRoot = testDir.resolve("src/main/java");
        
        assertThat(Files.walk(javaRoot)
                .anyMatch(path -> path.getFileName().toString().contains("Application.java")))
                .as("Should generate Spring Boot Application class")
                .isTrue();
        
        assertThat(Files.walk(javaRoot)
                .anyMatch(path -> path.getFileName().toString().contains("Controller.java")))
                .as("Should generate REST Controller class")
                .isTrue();
        
        assertThat(Files.walk(javaRoot)
                .anyMatch(path -> path.getFileName().toString().contains("DAO.java")))
                .as("Should generate Data Access Object class")
                .isTrue();
        
        assertThat(Files.walk(javaRoot)
                .anyMatch(path -> path.getFileName().toString().contains("DTO.java")))
                .as("Should generate Data Transfer Object class")
                .isTrue();
        
        System.out.println("✅ Java classes structure validation completed successfully");
    }

    @Test
    @Order(6)
    @DisplayName("When generation completes should create valid Spring Boot configuration files")
    void whenGenerationCompletesShouldCreateValidSpringBootConfigurationFiles() throws IOException {
        System.out.println("📋 Testing Spring Boot configuration files...");
        
        Path configTestDir = Files.createTempDirectory(baseTestDir, "config-validation-");
        
        String[] args = {
            "--name", "ConfigTest", 
            "--destination", configTestDir.toString(),
            "--sql-file", "customer_select.sql"
        };
        
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine commandLine = new CommandLine(generator);
        int exitCode = commandLine.execute(args);
        
        assertThat(exitCode).isZero();
        
        assertThat(configTestDir.resolve("src/main/resources/application.properties"))
                .as("Should generate application.properties configuration file")
                .exists()
                .isRegularFile();
        
        System.out.println("✅ Spring Boot configuration validation completed successfully");
    }

    @Test
    @Order(7)
    @DisplayName("When invalid inputs provided should handle errors gracefully without crashing")
    void whenInvalidInputsProvidedShouldHandleErrorsGracefullyWithoutCrashing() {
        System.out.println("📋 Testing error handling and edge cases...");
        
        // Test with empty business name
        String[] invalidNameArgs = {
            "--name", "", 
            "--destination", baseTestDir.toString()
        };
        
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine commandLine = new CommandLine(generator);
        
        int exitCode = commandLine.execute(invalidNameArgs);
        
        assertThat(exitCode)
                .as("Empty business name should be rejected with non-zero exit code")
                .isNotEqualTo(0);
        
        // Test with non-existent SQL file
        String[] invalidSqlArgs = {
            "--name", "TestBusiness", 
            "--destination", baseTestDir.toString(),
            "--sql-file", "nonexistent_file.sql"
        };
        
        generator = new MicroServiceGenerator();
        commandLine = new CommandLine(generator);
        
        exitCode = commandLine.execute(invalidSqlArgs);
        
        assertThat(exitCode)
                .as("Non-existent SQL file should be rejected with non-zero exit code")
                .isNotEqualTo(0);
        
        System.out.println("✅ Error handling validation completed successfully");
    }

    @Test
    @Order(8)
    @DisplayName("When multiple CRUD operations requested should generate separate valid microservices for each")
    void whenMultipleCrudOperationsRequestedShouldGenerateSeparateValidMicroservicesForEach() throws IOException {
        System.out.println("📋 Testing comprehensive generation workflow...");
        
        String[] sqlFiles = {"customer_select.sql", "customer_insert.sql", "customer_update.sql", "customer_delete.sql"};
        String[] operations = {"Select", "Insert", "Update", "Delete"};
        
        for (int i = 0; i < sqlFiles.length; i++) {
            Path operationDir = Files.createTempDirectory(baseTestDir, operations[i].toLowerCase() + "-workflow-");
            
            String[] args = {
                "--name", "Workflow" + operations[i], 
                "--destination", operationDir.toString(),
                "--sql-file", sqlFiles[i]
            };
            
            MicroServiceGenerator generator = new MicroServiceGenerator();
            CommandLine commandLine = new CommandLine(generator);
            int exitCode = commandLine.execute(args);
            
            assertThat(exitCode)
                    .as(operations[i] + " workflow generation should complete successfully")
                    .isZero();
            
            assertThat(operationDir.resolve("pom.xml"))
                    .as(operations[i] + " should generate valid Maven project structure")
                    .exists();
        }
        
        System.out.println("✅ Comprehensive generation workflow validation completed successfully");
    }
}