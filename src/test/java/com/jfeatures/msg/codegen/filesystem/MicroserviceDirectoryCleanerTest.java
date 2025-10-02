package com.jfeatures.msg.codegen.filesystem;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

class MicroserviceDirectoryCleanerTest {

    private MicroserviceDirectoryCleaner cleaner;

    @BeforeEach
    void setUp() {
        cleaner = new MicroserviceDirectoryCleaner();
    }

    private static Stream<Arguments> invalidDestinationProvider() {
        return Stream.of(
            Arguments.of(null, "null destination"),
            Arguments.of("", "empty destination"),
            Arguments.of("   ", "whitespace destination")
        );
    }

    @ParameterizedTest(name = "{1} should throw IllegalArgumentException")
    @MethodSource("invalidDestinationProvider")
    void testCleanGeneratedCodeDirectories_InvalidDestination(String destination, String description) {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cleaner.cleanGeneratedCodeDirectories(destination)
        );
        assertEquals("Destination path cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCleanGeneratedCodeDirectories_NonExistentDirectory(@TempDir Path tempDir) {
        Path nonExistentPath = tempDir.resolve("non-existent");
        
        // Should not throw exception for non-existent directory
        assertDoesNotThrow(() -> cleaner.cleanGeneratedCodeDirectories(nonExistentPath.toString()));
    }

    @Test
    void testCleanGeneratedCodeDirectories_EmptyDirectory(@TempDir Path tempDir) {
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

    @Test
    void testCleanDirectoryIfExistsDeletionFailure() throws Exception {
        Path tempDir = Files.createTempDirectory("cleaner-test");
        Path file = tempDir.resolve("file.txt");
        Files.writeString(file, "data");

        Method method = MicroserviceDirectoryCleaner.class.getDeclaredMethod("cleanDirectoryIfExists", Path.class);
        method.setAccessible(true);

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(tempDir)).thenReturn(true);
            filesMock.when(() -> Files.walk(tempDir)).thenReturn(Stream.of(file, tempDir));
            filesMock.when(() -> Files.delete(file)).thenThrow(new RuntimeException("failure"));
            filesMock.when(() -> Files.delete(tempDir)).thenReturn(null);

            assertDoesNotThrow(() -> method.invoke(cleaner, tempDir));
        }
    }

    @Test
    void testCleanDirectoryIfExistsThrowsDirectoryCleanupException() throws Exception {
        Path tempDir = Files.createTempDirectory("cleaner-throws");

        Method method = MicroserviceDirectoryCleaner.class.getDeclaredMethod("cleanDirectoryIfExists", Path.class);
        method.setAccessible(true);

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(tempDir)).thenReturn(true);
            filesMock.when(() -> Files.walk(tempDir)).thenThrow(new RuntimeException("walk failed"));

            InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> method.invoke(cleaner, tempDir));
            assertTrue(exception.getCause() instanceof DirectoryCleanupException);
        }
    }

    @Test
    void testDeleteFileIfExistsFailureIsIgnored() throws Exception {
        Path tempFile = Files.createTempFile("cleaner-file", ".txt");

        Method method = MicroserviceDirectoryCleaner.class.getDeclaredMethod("deleteFileIfExists", Path.class);
        method.setAccessible(true);

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(tempFile)).thenReturn(true);
            filesMock.when(() -> Files.delete(tempFile)).thenThrow(new RuntimeException("delete failed"));

            assertDoesNotThrow(() -> method.invoke(cleaner, tempFile));
        }
    }
}
