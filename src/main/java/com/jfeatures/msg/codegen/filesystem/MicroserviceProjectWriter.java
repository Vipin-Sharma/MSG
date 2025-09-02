package com.jfeatures.msg.codegen.filesystem;

import com.jfeatures.msg.codegen.constants.ProjectConstants;
import com.jfeatures.msg.codegen.domain.GeneratedMicroservice;
import com.jfeatures.msg.codegen.domain.ProjectDirectoryStructure;
import com.squareup.javapoet.JavaFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes generated microservice files to the filesystem.
 * Handles all file writing operations including Java source files,
 * configuration files, and template resource copying.
 */
@Slf4j
public class MicroserviceProjectWriter {
    
    private final ProjectDirectoryBuilder directoryBuilder;
    
    public MicroserviceProjectWriter() {
        this.directoryBuilder = new ProjectDirectoryBuilder();
    }
    
    /**
     * Writes a complete generated microservice to the filesystem.
     * Creates the project directory structure and writes all Java files,
     * configuration files, and template resources.
     * 
     * @param microservice the generated microservice to write
     * @param destinationPath the target directory path
     * @throws IOException if file writing operations fail
     * @throws IllegalArgumentException if input parameters are invalid
     */
    public void writeMicroserviceProject(GeneratedMicroservice microservice, String destinationPath) throws IOException {
        validateInputParameters(microservice, destinationPath);
        
        log.info("Writing {} microservice to: {}", microservice.statementType(), destinationPath);
        
        try {
            // Build project directory structure
            ProjectDirectoryStructure directories = directoryBuilder.buildDirectoryStructure(destinationPath);
            
            // Write all Java source files
            writeJavaFile(microservice.springBootApplication(), directories.srcMainJava());
            writeJavaFile(microservice.dtoFile(), directories.srcMainJava());
            writeJavaFile(microservice.controllerFile(), directories.srcMainJava());
            writeJavaFile(microservice.daoFile(), directories.srcMainJava());
            
            // Write database configuration file
            writeDatabaseConfigFile(microservice.databaseConfigContent(), 
                                   microservice.businessDomainName(), 
                                   directories.srcMainJava());
            
            // Copy template files (pom.xml, application.properties)
            copyTemplateFiles(directories);
            
        } catch (IOException e) {
            log.error("I/O error while writing microservice project: {}", e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            log.error("Runtime error while writing microservice project: {}", e.getMessage(), e);
            throw new IOException("Failed to write microservice project due to runtime error", e);
        }
        
        log.info("Successfully wrote {} microservice for '{}' to: {}", 
                microservice.statementType(), 
                microservice.businessDomainName(), 
                destinationPath);
    }
    
    private void validateInputParameters(GeneratedMicroservice microservice, String destinationPath) {
        if (microservice == null) {
            throw new IllegalArgumentException("Generated microservice cannot be null");
        }
        if (destinationPath == null || destinationPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination path cannot be null or empty");
        }
    }
    
    private void writeJavaFile(JavaFile javaFile, Path sourceDirectory) throws IOException {
        try {
            javaFile.writeTo(sourceDirectory);
            log.debug("Successfully wrote Java file: {}.{}", javaFile.packageName, javaFile.typeSpec.name);
        } catch (IOException e) {
            log.error("Failed to write Java file: {}.{} - {}", javaFile.packageName, javaFile.typeSpec.name, e.getMessage());
            throw new IOException("Failed to write Java file: " + javaFile.packageName + "." + 
                               javaFile.typeSpec.name + " - " + e.getMessage(), e);
        }
    }
    
    private void writeDatabaseConfigFile(String content, String businessDomainName, Path sourceDirectory) throws IOException {
        try {
            Path configPackagePath = sourceDirectory.resolve("com/jfeatures/" + 
                                                            businessDomainName.toLowerCase() + "/config");
            Files.createDirectories(configPackagePath);
            Path configFilePath = configPackagePath.resolve(ProjectConstants.DATABASE_CONFIG_FILE_NAME);
            Files.write(configFilePath, content.getBytes(StandardCharsets.UTF_8));
            log.debug("Successfully wrote database config file: {}", configFilePath);
        } catch (IOException e) {
            log.error("Failed to write database config file: {}", e.getMessage());
            throw new IOException("Failed to write database config file: " + e.getMessage(), e);
        }
    }
    
    private void copyTemplateFiles(ProjectDirectoryStructure directories) throws IOException {
        // Copy pom.xml template
        copyResourceFileToPath(
            ProjectConstants.POM_TEMPLATE_FILE,
            directories.targetDirectory().resolve(ProjectConstants.POM_FILE_NAME)
        );
        
        // Copy application.properties template
        copyResourceFileToPath(
            ProjectConstants.APPLICATION_PROPERTIES_TEMPLATE_FILE,
            directories.srcMainResources().resolve(ProjectConstants.APPLICATION_PROPERTIES_FILE_NAME)
        );
    }
    
    private void copyResourceFileToPath(String resourceFileName, Path targetFilePath) throws IOException {
        try (InputStream inputStream = MicroserviceProjectWriter.class.getClassLoader()
                                                                      .getResourceAsStream(resourceFileName)) {
            if (inputStream == null) {
                log.error("Template resource file not found: {}", resourceFileName);
                throw new IOException("Template resource file not found: " + resourceFileName);
            }
            Files.write(targetFilePath, inputStream.readAllBytes());
            log.debug("Successfully copied resource file {} to {}", resourceFileName, targetFilePath);
        } catch (IOException e) {
            log.error("Failed to copy resource file {} to {}: {}", resourceFileName, targetFilePath, e.getMessage());
            throw new IOException("Failed to copy resource file " + resourceFileName + 
                               " to " + targetFilePath + ": " + e.getMessage(), e);
        }
    }
}