package com.jfeatures.msg.codegen.filesystem;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MicroserviceDirectoryCleanerTest {

    private MicroserviceDirectoryCleaner cleaner;

    @BeforeEach
    void setUp() {
        cleaner = new MicroserviceDirectoryCleaner();
    }

    @Test
    void testCleanGeneratedCodeDirectories_NullDestination() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cleaner.cleanGeneratedCodeDirectories(null)
        );
        assertEquals("Destination path cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCleanGeneratedCodeDirectories_EmptyDestination() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cleaner.cleanGeneratedCodeDirectories("")
        );
        assertEquals("Destination path cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCleanGeneratedCodeDirectories_WhitespaceDestination() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cleaner.cleanGeneratedCodeDirectories("   ")
        );
        assertEquals("Destination path cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCleanGeneratedCodeDirectories_NonExistentDirectory(@TempDir Path tempDir) throws Exception {
        Path nonExistentPath = tempDir.resolve("non-existent");
        
        // Should not throw exception for non-existent directory
        assertDoesNotThrow(() -> cleaner.cleanGeneratedCodeDirectories(nonExistentPath.toString()));
    }

    @Test
    void testCleanGeneratedCodeDirectories_EmptyDirectory(@TempDir Path tempDir) throws Exception {
        // Should handle empty directory without issues
        assertDoesNotThrow(() -> cleaner.cleanGeneratedCodeDirectories(tempDir.toString()));
    }

    @Test
    void testCleanGeneratedCodeDirectories_WithGeneratedStructure(@TempDir Path tempDir) throws Exception {
        // Create Maven-style project structure with generated files
        Path srcMainJava = tempDir.resolve("src/main/java/com/jfeatures");
        Path srcTestJava = tempDir.resolve("src/test/java/com/jfeatures");
        Path srcMainResources = tempDir.resolve("src/main/resources");
        
        Files.createDirectories(srcMainJava);
        Files.createDirectories(srcTestJava);
        Files.createDirectories(srcMainResources);
        
        // Create files that should be cleaned
        Path pomFile = tempDir.resolve("pom.xml");
        Path applicationProps = srcMainResources.resolve("application.properties");
        Path javaFile = srcMainJava.resolve("TestService.java");
        Path testFile = srcTestJava.resolve("TestServiceTest.java");
        
        Files.createFile(pomFile);
        Files.createFile(applicationProps);
        Files.createFile(javaFile);
        Files.createFile(testFile);
        
        // Create files that should NOT be cleaned (IDE files, user files)
        Path ideaDir = tempDir.resolve(".idea");
        Path gitDir = tempDir.resolve(".git");
        Path userFile = tempDir.resolve("README.md");
        
        Files.createDirectories(ideaDir);
        Files.createDirectories(gitDir);
        Files.createFile(userFile);
        Files.createFile(ideaDir.resolve("workspace.xml"));
        Files.createFile(gitDir.resolve("config"));

        // Execute cleaning
        cleaner.cleanGeneratedCodeDirectories(tempDir.toString());

        // Verify generated directories and files are cleaned
        assertFalse(Files.exists(srcMainJava), "Generated Java source directory should be cleaned");
        assertFalse(Files.exists(srcTestJava), "Generated test directory should be cleaned");
        assertFalse(Files.exists(pomFile), "pom.xml should be cleaned");
        assertFalse(Files.exists(applicationProps), "application.properties should be cleaned");

        // Verify IDE and user files are preserved
        assertTrue(Files.exists(ideaDir), "IDE directory should be preserved");
        assertTrue(Files.exists(gitDir), "Git directory should be preserved");
        assertTrue(Files.exists(userFile), "User files should be preserved");
        assertTrue(Files.exists(ideaDir.resolve("workspace.xml")), "IDE files should be preserved");
        assertTrue(Files.exists(gitDir.resolve("config")), "Git files should be preserved");
    }

    @Test
    void testCleanGeneratedCodeDirectories_PartialStructure(@TempDir Path tempDir) throws Exception {
        // Create only some of the directories and files
        Path srcMainJava = tempDir.resolve("src/main/java/com/jfeatures/test");
        Files.createDirectories(srcMainJava);
        Files.createFile(srcMainJava.resolve("TestClass.java"));
        
        Path pomFile = tempDir.resolve("pom.xml");
        Files.createFile(pomFile);

        // Execute cleaning
        cleaner.cleanGeneratedCodeDirectories(tempDir.toString());

        // Verify cleanup occurred
        assertFalse(Files.exists(srcMainJava), "Generated Java directory should be cleaned");
        assertFalse(Files.exists(pomFile), "pom.xml should be cleaned");
    }

    @Test
    void testCleanGeneratedCodeDirectories_NestedStructure(@TempDir Path tempDir) throws Exception {
        // Create deeply nested structure
        Path deepPath = tempDir.resolve("src/main/java/com/jfeatures/domain/customer/dto");
        Files.createDirectories(deepPath);
        Files.createFile(deepPath.resolve("CustomerDTO.java"));
        Files.createFile(deepPath.resolve("CustomerRequestDTO.java"));
        
        Path parentPath = deepPath.getParent();
        Files.createFile(parentPath.resolve("Customer.java"));

        // Execute cleaning
        cleaner.cleanGeneratedCodeDirectories(tempDir.toString());

        // Verify entire nested structure is cleaned
        assertFalse(Files.exists(tempDir.resolve("src/main/java/com/jfeatures")), 
                   "Entire generated structure should be cleaned");
    }

    @Test
    void testCleanGeneratedCodeDirectories_WithReadOnlyFiles(@TempDir Path tempDir) throws Exception {
        // Create structure with files
        Path srcMainJava = tempDir.resolve("src/main/java/com/jfeatures");
        Files.createDirectories(srcMainJava);
        Path readOnlyFile = srcMainJava.resolve("ReadOnlyFile.java");
        Files.createFile(readOnlyFile);
        
        // Make file read-only (this tests the error handling in the cleaner)
        readOnlyFile.toFile().setReadOnly();

        // Should handle read-only files gracefully (may log warnings but not throw exceptions)
        assertDoesNotThrow(() -> cleaner.cleanGeneratedCodeDirectories(tempDir.toString()));
    }
}