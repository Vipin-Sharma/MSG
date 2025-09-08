package com.jfeatures.msg.codegen.filesystem;

import com.jfeatures.msg.codegen.constants.ProjectConstants;
import com.jfeatures.msg.codegen.domain.ProjectDirectoryStructure;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Builds standardized Maven project directory structure for generated microservices.
 * Creates all necessary directories following Maven conventions and ensures
 * they exist before file writing operations.
 */
public class ProjectDirectoryBuilder {
    
    /**
     * Builds the complete Maven project directory structure at the specified base path.
     * Creates all standard directories (src/main/java, src/test/java, src/main/resources)
     * and returns a structured representation of the paths.
     * 
     * @param baseProjectPath the root path where the project should be created
     * @return a ProjectDirectoryStructure containing all created directory paths
     * @throws IOException if directory creation fails
     * @throws IllegalArgumentException if baseProjectPath is null or empty
     */
    public ProjectDirectoryStructure buildDirectoryStructure(String baseProjectPath) throws IOException {
        if (baseProjectPath == null || baseProjectPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Base project path cannot be null or empty");
        }
        
        Path targetDirectory = Paths.get(baseProjectPath);
        
        // Build Maven standard directory paths
        Path srcMainJava = targetDirectory.resolve(ProjectConstants.SRC_MAIN_JAVA_PATH);
        Path srcTestJava = targetDirectory.resolve(ProjectConstants.SRC_TEST_JAVA_PATH)
                                         .resolve(ProjectConstants.JFEATURES_PACKAGE_PATH);
        Path srcMainResources = targetDirectory.resolve(ProjectConstants.SRC_MAIN_RESOURCES_PATH);
        
        // Create all directories
        createDirectorySafely(srcMainJava);
        createDirectorySafely(srcTestJava);
        createDirectorySafely(srcMainResources);
        
        return new ProjectDirectoryStructure(
            srcMainJava,
            srcTestJava,
            srcMainResources,
            targetDirectory
        );
    }
    
    private void createDirectorySafely(Path directoryPath) throws IOException {
        try {
            Files.createDirectories(directoryPath);
        } catch (IOException e) {
            throw new IOException("Failed to create directory: " + directoryPath + " - " + e.getMessage(), e);
        }
    }
}