package com.jfeatures.msg.e2e;

import com.jfeatures.msg.codegen.MicroServiceGenerator;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import org.junit.jupiter.api.*;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Working End-to-End test for MSG CRUD generation workflow.
 * Tests code generation with proper directory handling.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Working E2E CRUD Generation Tests")
class WorkingE2EGenerationTest {

    private static Path baseTestDir;
    private final String businessName = "TestCustomer";

    @BeforeAll
    static void setupTestEnvironment() throws IOException {
        // Create base test directory
        baseTestDir = Files.createTempDirectory("msg-working-e2e");
        System.out.println("🚀 Starting Working E2E Test Suite");
        System.out.println("Base test directory: " + baseTestDir);
    }

    @AfterAll
    static void cleanupTestEnvironment() throws IOException {
        // Clean up base test directory
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
        System.out.println("✅ E2E Test cleanup completed");
    }

    @Test
    @Order(1)
    @DisplayName("1. Generate and Validate SELECT CRUD API")
    void testGenerateSelectCrudApi() throws IOException {
        System.out.println("📋 Testing SELECT CRUD API generation...");
        
        // Create dedicated directory for SELECT generation
        Path selectDir = Files.createTempDirectory(baseTestDir, "select-");
        
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
                .isEqualTo(0);
        
        // Validate generated structure
        GeneratedMicroserviceValidator validator = new GeneratedMicroserviceValidator(selectDir);
        
        // Check basic Maven structure
        assertThat(selectDir.resolve("pom.xml"))
                .as("pom.xml should exist")
                .exists()
                .isRegularFile();
                
        assertThat(selectDir.resolve("src/main/java"))
                .as("Java source directory should exist")
                .exists()
                .isDirectory();
                
        assertThat(selectDir.resolve("src/main/resources"))
                .as("Resources directory should exist")
                .exists()
                .isDirectory();
        
        System.out.println("✅ SELECT CRUD API generation and validation passed");
    }

    @Test
    @Order(2)
    @DisplayName("2. Generate and Validate INSERT CRUD API")  
    void testGenerateInsertCrudApi() throws IOException {
        System.out.println("📋 Testing INSERT CRUD API generation...");
        
        // Create dedicated directory for INSERT generation
        Path insertDir = Files.createTempDirectory(baseTestDir, "insert-");
        
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
                .isEqualTo(0);
        
        // Validate generated structure
        assertThat(insertDir.resolve("pom.xml"))
                .as("pom.xml should exist")
                .exists();
        
        System.out.println("✅ INSERT CRUD API generation and validation passed");
    }

    @Test
    @Order(3)
    @DisplayName("3. Generate and Validate UPDATE CRUD API")
    void testGenerateUpdateCrudApi() throws IOException {
        System.out.println("📋 Testing UPDATE CRUD API generation...");
        
        // Create dedicated directory for UPDATE generation
        Path updateDir = Files.createTempDirectory(baseTestDir, "update-");
        
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
                .isEqualTo(0);
        
        // Validate generated structure
        assertThat(updateDir.resolve("pom.xml"))
                .as("pom.xml should exist")
                .exists();
        
        System.out.println("✅ UPDATE CRUD API generation and validation passed");
    }

    @Test
    @Order(4)
    @DisplayName("4. Generate and Validate DELETE CRUD API")
    void testGenerateDeleteCrudApi() throws IOException {
        System.out.println("📋 Testing DELETE CRUD API generation...");
        
        // Create dedicated directory for DELETE generation
        Path deleteDir = Files.createTempDirectory(baseTestDir, "delete-");
        
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
                .isEqualTo(0);
        
        // Validate generated structure
        assertThat(deleteDir.resolve("pom.xml"))
                .as("pom.xml should exist")
                .exists();
        
        System.out.println("✅ DELETE CRUD API generation and validation passed");
    }

    @Test
    @Order(5)
    @DisplayName("5. Validate Generated Java Classes Structure")
    void testGeneratedJavaClasses() throws IOException {
        System.out.println("📋 Testing generated Java classes structure...");
        
        // Generate a fresh project for detailed validation
        Path testDir = Files.createTempDirectory(baseTestDir, "validation-");
        
        String[] args = {
            "--name", "ValidationTest", 
            "--destination", testDir.toString(),
            "--sql-file", "customer_select.sql"
        };
        
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine commandLine = new CommandLine(generator);
        int exitCode = commandLine.execute(args);
        
        assertThat(exitCode).isEqualTo(0);
        
        // Check for expected Java files
        Path javaRoot = testDir.resolve("src/main/java");
        
        // Check Application class
        assertThat(Files.walk(javaRoot)
                .anyMatch(path -> path.getFileName().toString().contains("Application.java")))
                .as("Should have Application class")
                .isTrue();
        
        // Check Controller class
        assertThat(Files.walk(javaRoot)
                .anyMatch(path -> path.getFileName().toString().contains("Controller.java")))
                .as("Should have Controller class")
                .isTrue();
        
        // Check DAO class
        assertThat(Files.walk(javaRoot)
                .anyMatch(path -> path.getFileName().toString().contains("DAO.java")))
                .as("Should have DAO class")
                .isTrue();
        
        // Check DTO class
        assertThat(Files.walk(javaRoot)
                .anyMatch(path -> path.getFileName().toString().contains("DTO.java")))
                .as("Should have DTO class")
                .isTrue();
        
        System.out.println("✅ Generated Java classes structure validation passed");
    }

    @Test
    @Order(6)
    @DisplayName("6. Validate Spring Boot Configuration Files")
    void testSpringBootConfiguration() throws IOException {
        System.out.println("📋 Testing Spring Boot configuration...");
        
        // Generate a fresh project for config validation
        Path testDir = Files.createTempDirectory(baseTestDir, "config-");
        
        String[] args = {
            "--name", "ConfigTest", 
            "--destination", testDir.toString(),
            "--sql-file", "customer_select.sql"
        };
        
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine commandLine = new CommandLine(generator);
        int exitCode = commandLine.execute(args);
        
        assertThat(exitCode).isEqualTo(0);
        
        // Check application.properties
        assertThat(testDir.resolve("src/main/resources/application.properties"))
                .as("application.properties should exist")
                .exists()
                .isRegularFile();
        
        System.out.println("✅ Spring Boot configuration validation passed");
    }

    @Test
    @Order(7)
    @DisplayName("7. Test Error Handling and Edge Cases")
    void testErrorHandlingAndEdgeCases() {
        System.out.println("📋 Testing error handling and edge cases...");
        
        // Test with invalid business name
        String[] invalidNameArgs = {
            "--name", "", 
            "--destination", baseTestDir.toString()
        };
        
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine commandLine = new CommandLine(generator);
        
        int exitCode = commandLine.execute(invalidNameArgs);
        
        assertThat(exitCode)
                .as("Invalid business name should fail gracefully")
                .isNotEqualTo(0);
        
        // Test with non-existent SQL file
        String[] invalidSqlArgs = {
            "--name", "TestBusiness", 
            "--destination", baseTestDir.toString(),
            "--sql-file", "non_existent_file.sql"
        };
        
        generator = new MicroServiceGenerator();
        commandLine = new CommandLine(generator);
        
        exitCode = commandLine.execute(invalidSqlArgs);
        
        assertThat(exitCode)
                .as("Non-existent SQL file should fail gracefully")
                .isNotEqualTo(0);
        
        System.out.println("✅ Error handling and edge cases validation passed");
    }

    @Test
    @Order(8)
    @DisplayName("8. Comprehensive Generation Workflow Test")
    void testComprehensiveGenerationWorkflow() throws IOException {
        System.out.println("📋 Testing comprehensive generation workflow...");
        
        // Test all 4 CRUD operations in sequence
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
                    .as(operations[i] + " workflow generation should succeed")
                    .isEqualTo(0);
            
            assertThat(operationDir.resolve("pom.xml"))
                    .as(operations[i] + " should generate pom.xml")
                    .exists();
        }
        
        System.out.println("✅ Comprehensive generation workflow test passed");
    }
}