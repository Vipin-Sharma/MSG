package com.jfeatures.msg.codegen.filesystem;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
    void testCleanGeneratedCodeDirectories_WithHiddenFiles(@TempDir Path tempDir) throws Exception {
        // Create structure with hidden files
        Path srcMainJava = tempDir.resolve("src/main/java/com/jfeatures");
        Files.createDirectories(srcMainJava);

        Path hiddenFile = srcMainJava.resolve(".hidden");
        Files.createFile(hiddenFile);

        Path normalFile = srcMainJava.resolve("Normal.java");
        Files.createFile(normalFile);

        cleaner.cleanGeneratedCodeDirectories(tempDir.toString());

        // Verify directory is cleaned
        assertFalse(Files.exists(srcMainJava));
    }

    @Test
    void testCleanGeneratedCodeDirectories_WithSymbolicLinks(@TempDir Path tempDir) throws Exception {
        // Create structure
        Path srcMainJava = tempDir.resolve("src/main/java/com/jfeatures");
        Files.createDirectories(srcMainJava);

        // Create a target file outside the cleanup area
        Path externalTarget = tempDir.resolve("external.txt");
        Files.createFile(externalTarget);

        // Create a symbolic link inside the cleanup area
        Path symlink = srcMainJava.resolve("link.txt");
        try {
            Files.createSymbolicLink(symlink, externalTarget);

            cleaner.cleanGeneratedCodeDirectories(tempDir.toString());

            // Verify cleanup happened
            assertFalse(Files.exists(symlink));
            // External target should still exist
            assertTrue(Files.exists(externalTarget));
        } catch (UnsupportedOperationException e) {
            // Symlinks not supported on this OS
            assertTrue(true);
        }
    }

    @Test
    void testCleanGeneratedCodeDirectories_EmptyDirectories(@TempDir Path tempDir) throws Exception {
        // Create empty directory structure
        Path srcMainJava = tempDir.resolve("src/main/java/com/jfeatures");
        Files.createDirectories(srcMainJava);

        Path srcTestJava = tempDir.resolve("src/test/java");
        Files.createDirectories(srcTestJava);

        cleaner.cleanGeneratedCodeDirectories(tempDir.toString());

        // Empty directories should be cleaned
        assertFalse(Files.exists(srcMainJava));
    }

    @Test
    void testCleanGeneratedCodeDirectories_MixedContent(@TempDir Path tempDir) throws Exception {
        // Create structure with various file types
        Path srcMainJava = tempDir.resolve("src/main/java/com/jfeatures");
        Files.createDirectories(srcMainJava);

        Files.createFile(srcMainJava.resolve("File.java"));
        Files.createFile(srcMainJava.resolve("data.json"));
        Files.createFile(srcMainJava.resolve("config.xml"));
        Files.createFile(srcMainJava.resolve("README.md"));

        cleaner.cleanGeneratedCodeDirectories(tempDir.toString());

        assertFalse(Files.exists(srcMainJava));
    }

    @Test
    void testCleanGeneratedCodeDirectories_VeryDeepNesting(@TempDir Path tempDir) throws Exception {
        // Create very deep nesting (15 levels)
        Path deepPath = tempDir.resolve("src/main/java/com/jfeatures");
        for (int i = 0; i < 10; i++) {
            deepPath = deepPath.resolve("level" + i);
        }
        Files.createDirectories(deepPath);
        Files.createFile(deepPath.resolve("DeepFile.java"));

        cleaner.cleanGeneratedCodeDirectories(tempDir.toString());

        assertFalse(Files.exists(tempDir.resolve("src/main/java/com/jfeatures")));
    }

    @Test
    void testCleanGeneratedCodeDirectories_WithSpecialCharactersInFilenames(@TempDir Path tempDir) throws Exception {
        Path srcMainJava = tempDir.resolve("src/main/java/com/jfeatures");
        Files.createDirectories(srcMainJava);

        Files.createFile(srcMainJava.resolve("File-with-dash.java"));
        Files.createFile(srcMainJava.resolve("File_with_underscore.java"));
        Files.createFile(srcMainJava.resolve("File.with.dots.java"));

        cleaner.cleanGeneratedCodeDirectories(tempDir.toString());

        assertFalse(Files.exists(srcMainJava));
    }

    @Test
    void testCleanGeneratedCodeDirectories_WithUnicodeFilenames(@TempDir Path tempDir) throws Exception {
        Path srcMainJava = tempDir.resolve("src/main/java/com/jfeatures");
        Files.createDirectories(srcMainJava);

        Files.createFile(srcMainJava.resolve("顧客.java"));
        Files.createFile(srcMainJava.resolve("プロジェクト.java"));

        cleaner.cleanGeneratedCodeDirectories(tempDir.toString());

        assertFalse(Files.exists(srcMainJava));
    }

    @Test
    void testCleanGeneratedCodeDirectories_LargeNumberOfFiles(@TempDir Path tempDir) throws Exception {
        Path srcMainJava = tempDir.resolve("src/main/java/com/jfeatures");
        Files.createDirectories(srcMainJava);

        // Create 100 files
        for (int i = 0; i < 100; i++) {
            Files.createFile(srcMainJava.resolve("File" + i + ".java"));
        }

        cleaner.cleanGeneratedCodeDirectories(tempDir.toString());

        assertFalse(Files.exists(srcMainJava));
    }

    @Test
    void testCleanGeneratedCodeDirectories_NullPath() {
        assertThrows(Exception.class, () ->
            cleaner.cleanGeneratedCodeDirectories(null)
        );
    }

    @Test
    void testCleanGeneratedCodeDirectories_EmptyPath() {
        assertThrows(Exception.class, () ->
            cleaner.cleanGeneratedCodeDirectories("")
        );
    }

    @Test
    void testCleanGeneratedCodeDirectories_NonExistentPath() {
        // Should not throw exception for non-existent path
        assertDoesNotThrow(() ->
            cleaner.cleanGeneratedCodeDirectories("/path/that/does/not/exist")
        );
    }

    @Test
    void testCleanGeneratedCodeDirectories_WithSpacesInPath(@TempDir Path tempDir) throws Exception {
        Path srcMainJava = tempDir.resolve("my project/src/main/java/com/jfeatures");
        Files.createDirectories(srcMainJava);
        Files.createFile(srcMainJava.resolve("File.java"));

        cleaner.cleanGeneratedCodeDirectories(tempDir.resolve("my project").toString());

        assertFalse(Files.exists(srcMainJava));
    }
}