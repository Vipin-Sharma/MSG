package com.jfeatures.msg.codegen.filesystem;

import static org.junit.jupiter.api.Assertions.*;

import com.jfeatures.msg.codegen.domain.ProjectDirectoryStructure;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ProjectDirectoryBuilderTest {

    private ProjectDirectoryBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new ProjectDirectoryBuilder();
    }

    private static Stream<Arguments> invalidPathProvider() {
        return Stream.of(
            Arguments.of(null, "null path"),
            Arguments.of("", "empty path"),
            Arguments.of("   ", "whitespace path")
        );
    }

    @ParameterizedTest(name = "{1} should throw IllegalArgumentException")
    @MethodSource("invalidPathProvider")
    void testBuildDirectoryStructure_InvalidPath(String path, String description) {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> builder.buildDirectoryStructure(path)
        );
        assertEquals("Base project path cannot be null or empty", exception.getMessage());
    }

    @Test
    void testBuildDirectoryStructure_Success(@TempDir Path tempDir) throws IOException {
        String basePath = tempDir.toString();
        
        ProjectDirectoryStructure structure = builder.buildDirectoryStructure(basePath);

        // Verify structure object is created correctly
        assertNotNull(structure);
        assertNotNull(structure.srcMainJava());
        assertNotNull(structure.srcTestJava());
        assertNotNull(structure.srcMainResources());
        assertNotNull(structure.targetDirectory());

        // Verify all directories are created
        assertTrue(Files.exists(structure.srcMainJava()), "src/main/java should exist");
        assertTrue(Files.exists(structure.srcTestJava()), "src/test/java/com/jfeatures should exist");
        assertTrue(Files.exists(structure.srcMainResources()), "src/main/resources should exist");
        assertTrue(Files.exists(structure.targetDirectory()), "Target directory should exist");

        // Verify directories are actually directories (not files)
        assertTrue(Files.isDirectory(structure.srcMainJava()), "src/main/java should be a directory");
        assertTrue(Files.isDirectory(structure.srcTestJava()), "src/test/java/com/jfeatures should be a directory");
        assertTrue(Files.isDirectory(structure.srcMainResources()), "src/main/resources should be a directory");
        assertTrue(Files.isDirectory(structure.targetDirectory()), "Target directory should be a directory");

        // Verify correct path structure
        assertEquals(tempDir, structure.targetDirectory());
        assertEquals(tempDir.resolve("src/main/java"), structure.srcMainJava());
        assertEquals(tempDir.resolve("src/test/java/com/jfeatures"), structure.srcTestJava());
        assertEquals(tempDir.resolve("src/main/resources"), structure.srcMainResources());
    }

    @Test
    void testBuildDirectoryStructure_ExistingDirectories(@TempDir Path tempDir) throws IOException {
        // Pre-create some directories
        Path srcMainJava = tempDir.resolve("src/main/java");
        Files.createDirectories(srcMainJava);
        
        // Add some existing content
        Files.createFile(srcMainJava.resolve("ExistingFile.java"));
        
        // Building should succeed even with existing directories
        ProjectDirectoryStructure structure = builder.buildDirectoryStructure(tempDir.toString());

        assertNotNull(structure);
        assertTrue(Files.exists(structure.srcMainJava()));
        assertTrue(Files.exists(structure.srcTestJava()));
        assertTrue(Files.exists(structure.srcMainResources()));
        
        // Existing content should remain
        assertTrue(Files.exists(srcMainJava.resolve("ExistingFile.java")));
    }

    @Test
    void testBuildDirectoryStructure_DeepPath(@TempDir Path tempDir) throws IOException {
        // Test with a deeper nested path
        Path deepPath = tempDir.resolve("projects/microservices/generated/customer-service");
        
        ProjectDirectoryStructure structure = builder.buildDirectoryStructure(deepPath.toString());

        assertNotNull(structure);
        assertTrue(Files.exists(structure.srcMainJava()));
        assertTrue(Files.exists(structure.srcTestJava()));
        assertTrue(Files.exists(structure.srcMainResources()));
        
        // Verify the deep path structure
        assertEquals(deepPath, structure.targetDirectory());
        assertEquals(deepPath.resolve("src/main/java"), structure.srcMainJava());
    }

    @Test
    void testBuildDirectoryStructure_SpecialCharactersInPath(@TempDir Path tempDir) throws IOException {
        // Test with path containing spaces and special characters
        Path specialPath = tempDir.resolve("project with spaces/microservice-gen");
        
        ProjectDirectoryStructure structure = builder.buildDirectoryStructure(specialPath.toString());

        assertNotNull(structure);
        assertTrue(Files.exists(structure.srcMainJava()));
        assertTrue(Files.exists(structure.srcTestJava()));
        assertTrue(Files.exists(structure.srcMainResources()));
    }

    @Test 
    void testBuildDirectoryStructure_RelativePath() throws IOException {
        // Test with relative path
        String relativePath = "./test-output";
        
        ProjectDirectoryStructure structure = builder.buildDirectoryStructure(relativePath);

        assertNotNull(structure);
        assertTrue(Files.exists(structure.srcMainJava()));
        assertTrue(Files.exists(structure.srcTestJava()));
        assertTrue(Files.exists(structure.srcMainResources()));
        
        // Cleanup
        Files.walk(structure.targetDirectory())
             .sorted((a, b) -> b.compareTo(a))
             .forEach(path -> {
                 try {
                     Files.delete(path);
                 } catch (IOException e) {
                     // Ignore cleanup errors
                 }
             });
    }

    @Test
    void testBuildDirectoryStructure_MultipleBuildsOnSamePath(@TempDir Path tempDir) throws IOException {
        String basePath = tempDir.toString();

        // Build structure first time
        ProjectDirectoryStructure structure1 = builder.buildDirectoryStructure(basePath);

        // Build structure second time on same path - should not fail
        ProjectDirectoryStructure structure2 = builder.buildDirectoryStructure(basePath);

        // Both structures should be valid and equal
        assertNotNull(structure1);
        assertNotNull(structure2);
        assertEquals(structure1.targetDirectory(), structure2.targetDirectory());
        assertEquals(structure1.srcMainJava(), structure2.srcMainJava());
        assertEquals(structure1.srcTestJava(), structure2.srcTestJava());
        assertEquals(structure1.srcMainResources(), structure2.srcMainResources());
    }

    @Test
    void testBuildDirectoryStructure_DeeplyNestedPath(@TempDir Path tempDir) throws IOException {
        // Create a deeply nested path (10 levels)
        StringBuilder deepPath = new StringBuilder(tempDir.toString());
        for (int i = 0; i < 10; i++) {
            deepPath.append("/level").append(i);
        }

        ProjectDirectoryStructure structure = builder.buildDirectoryStructure(deepPath.toString());

        assertNotNull(structure);
        assertTrue(Files.exists(structure.srcMainJava()));
        assertTrue(Files.isDirectory(structure.srcMainJava()));
    }

    @Test
    void testBuildDirectoryStructure_WithSpecialCharacters(@TempDir Path tempDir) throws IOException {
        // Test with special characters that are valid in filenames
        String basePath = tempDir.resolve("project-name_v1.0").toString();

        ProjectDirectoryStructure structure = builder.buildDirectoryStructure(basePath);

        assertNotNull(structure);
        assertTrue(Files.exists(structure.targetDirectory()));
        assertTrue(Files.exists(structure.srcMainJava()));
    }

    @Test
    void testBuildDirectoryStructure_WithSpaces(@TempDir Path tempDir) throws IOException {
        String basePath = tempDir.resolve("my project folder").toString();

        ProjectDirectoryStructure structure = builder.buildDirectoryStructure(basePath);

        assertNotNull(structure);
        assertTrue(Files.exists(structure.targetDirectory()));
        assertTrue(structure.targetDirectory().toString().contains(" "));
    }

    @Test
    void testBuildDirectoryStructure_VerifyAllSubdirectories(@TempDir Path tempDir) throws IOException {
        String basePath = tempDir.toString();

        ProjectDirectoryStructure structure = builder.buildDirectoryStructure(basePath);

        // Verify all expected directories exist
        assertTrue(Files.exists(structure.srcMainJava()));
        assertTrue(Files.exists(structure.srcTestJava()));
        assertTrue(Files.exists(structure.srcMainResources()));

        // Verify they are directories
        assertTrue(Files.isDirectory(structure.srcMainJava()));
        assertTrue(Files.isDirectory(structure.srcTestJava()));
        assertTrue(Files.isDirectory(structure.srcMainResources()));
    }

    @Test
    void testBuildDirectoryStructure_WithUnicodeCharacters(@TempDir Path tempDir) throws IOException {
        String basePath = tempDir.resolve("プロジェクト").toString();

        ProjectDirectoryStructure structure = builder.buildDirectoryStructure(basePath);

        assertNotNull(structure);
        assertTrue(Files.exists(structure.targetDirectory()));
    }

    @Test
    void testBuildDirectoryStructure_LongPath(@TempDir Path tempDir) throws IOException {
        // Create a very long path (but within OS limits)
        String longDirName = "a".repeat(100);
        String basePath = tempDir.resolve(longDirName).toString();

        ProjectDirectoryStructure structure = builder.buildDirectoryStructure(basePath);

        assertNotNull(structure);
        assertTrue(Files.exists(structure.targetDirectory()));
    }

    @Test
    void testBuildDirectoryStructure_EmptyBasePath() {
        assertThrows(Exception.class, () -> {
            builder.buildDirectoryStructure("");
        });
    }

    @Test
    void testBuildDirectoryStructure_NullBasePath() {
        assertThrows(Exception.class, () -> {
            builder.buildDirectoryStructure(null);
        });
    }

    @Test
    void testBuildDirectoryStructure_WhitespaceOnlyPath() {
        assertThrows(Exception.class, () -> {
            builder.buildDirectoryStructure("   ");
        });
    }

    @Test
    void testBuildDirectoryStructure_RelativePath(@TempDir Path tempDir) throws IOException {
        // Use relative path
        String relativePath = "./temp/project";
        Path resolvedPath = tempDir.resolve(relativePath);

        ProjectDirectoryStructure structure = builder.buildDirectoryStructure(resolvedPath.toString());

        assertNotNull(structure);
        assertTrue(Files.exists(structure.targetDirectory()));
    }

    @Test
    void testBuildDirectoryStructure_SymbolicLink(@TempDir Path tempDir) throws IOException {
        // Create a target directory
        Path targetDir = tempDir.resolve("target");
        Files.createDirectory(targetDir);

        // Create a symbolic link (skip if not supported)
        Path linkPath = tempDir.resolve("link");
        try {
            Files.createSymbolicLink(linkPath, targetDir);

            ProjectDirectoryStructure structure = builder.buildDirectoryStructure(linkPath.toString());

            assertNotNull(structure);
            assertTrue(Files.exists(structure.srcMainJava()));
        } catch (UnsupportedOperationException e) {
            // Symbolic links not supported on this OS
            assertTrue(true);
        }
    }

    @Test
    void testBuildDirectoryStructure_ParentDoesNotExist(@TempDir Path tempDir) throws IOException {
        String basePath = tempDir.resolve("nonexistent/parent/child").toString();

        ProjectDirectoryStructure structure = builder.buildDirectoryStructure(basePath);

        assertNotNull(structure);
        assertTrue(Files.exists(structure.targetDirectory()));
        assertTrue(Files.exists(structure.srcMainJava()));
    }

    @Test
    void testBuildDirectoryStructure_WithDots(@TempDir Path tempDir) throws IOException {
        String basePath = tempDir.resolve("project.with.dots").toString();

        ProjectDirectoryStructure structure = builder.buildDirectoryStructure(basePath);

        assertNotNull(structure);
        assertTrue(Files.exists(structure.targetDirectory()));
    }
}