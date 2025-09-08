package com.jfeatures.msg.codegen.filesystem;

import static org.junit.jupiter.api.Assertions.*;

import com.jfeatures.msg.codegen.domain.ProjectDirectoryStructure;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectDirectoryBuilderTest {

    private ProjectDirectoryBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new ProjectDirectoryBuilder();
    }

    @Test
    void testBuildDirectoryStructure_NullPath() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> builder.buildDirectoryStructure(null)
        );
        assertEquals("Base project path cannot be null or empty", exception.getMessage());
    }

    @Test
    void testBuildDirectoryStructure_EmptyPath() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> builder.buildDirectoryStructure("")
        );
        assertEquals("Base project path cannot be null or empty", exception.getMessage());
    }

    @Test
    void testBuildDirectoryStructure_WhitespacePath() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> builder.buildDirectoryStructure("   ")
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
}