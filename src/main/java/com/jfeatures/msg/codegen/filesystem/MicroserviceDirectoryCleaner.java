package com.jfeatures.msg.codegen.filesystem;

import com.jfeatures.msg.codegen.constants.ProjectConstants;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles cleaning of destination directories before microservice generation.
 * Selectively removes only generated code directories and files while preserving
 * IDE configurations (.idea, .git, etc.) and other user-created content.
 */
@Slf4j
public class MicroserviceDirectoryCleaner {
    
    /**
     * Cleans generated code directories and files from the target destination.
     * Only removes specific directories and files that will be regenerated,
     * preserving IDE configurations and other user content.
     * 
     * @param destinationPath the root directory path to clean
     * @throws Exception if cleaning operations fail
     */
    public void cleanGeneratedCodeDirectories(String destinationPath) throws Exception {
        if (destinationPath == null || destinationPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination path cannot be null or empty");
        }
        
        Path destPath = Paths.get(destinationPath);
        if (!Files.exists(destPath)) {
            log.info("Destination directory does not exist, no cleaning needed: {}", destinationPath);
            return;
        }
        
        log.info("Cleaning generated source directories only, preserving IDE configurations: {}", destinationPath);
        
        // Clean specific directories that contain generated code
        cleanDirectoryIfExists(destPath.resolve("src/main/java/" + ProjectConstants.JFEATURES_PACKAGE_PATH));
        cleanDirectoryIfExists(destPath.resolve("src/test/java/" + ProjectConstants.JFEATURES_PACKAGE_PATH));
        
        // Clean specific files that get regenerated
        deleteFileIfExists(destPath.resolve(ProjectConstants.POM_FILE_NAME));
        deleteFileIfExists(destPath.resolve(ProjectConstants.SRC_MAIN_RESOURCES_PATH + 
            "/" + ProjectConstants.APPLICATION_PROPERTIES_FILE_NAME));
    }
    
    private void cleanDirectoryIfExists(Path directoryPath) throws Exception {
        if (!Files.exists(directoryPath)) {
            return;
        }
        
        log.info("Cleaning directory: {}", directoryPath);
        try {
            Files.walk(directoryPath)
                 .sorted((a, b) -> b.compareTo(a)) // Reverse order to delete children first
                 .forEach(path -> {
                     try {
                         Files.delete(path);
                     } catch (Exception e) {
                         log.warn("Could not delete path: {} - {}", path, e.getMessage());
                     }
                 });
        } catch (Exception e) {
            throw new Exception("Failed to clean directory: " + directoryPath + " - " + e.getMessage(), e);
        }
    }
    
    private void deleteFileIfExists(Path filePath) {
        if (!Files.exists(filePath)) {
            return;
        }
        
        try {
            Files.delete(filePath);
            log.info("Deleted file: {}", filePath);
        } catch (Exception e) {
            log.warn("Could not delete file: {} - {}", filePath, e.getMessage());
        }
    }
}