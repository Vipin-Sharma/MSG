package com.jfeatures.msg.e2e;

import com.jfeatures.msg.codegen.util.SqlStatementType;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates the structure and quality of generated microservices.
 */
public class GeneratedMicroserviceValidator {
    
    private final Path baseOutputDir;
    
    public GeneratedMicroserviceValidator(Path baseOutputDir) {
        this.baseOutputDir = baseOutputDir;
    }
    
    /**
     * Validates the complete project structure for a business domain.
     */
    public void validateCompleteProjectStructure(String businessName) {
        Path projectRoot = getProjectRoot(businessName);
        
        // Validate Maven structure
        validateMavenProjectStructure(businessName);
        
        // Validate Java source structure
        validateJavaSourceStructure(projectRoot);
        
        // Validate configuration files
        validateConfigurationFiles(projectRoot);
        
        System.out.println("✅ Complete project structure validation passed for: " + businessName);
    }
    
    /**
     * Validates Maven project structure (pom.xml, directory layout).
     */
    public void validateMavenProjectStructure(String businessName) {
        Path projectRoot = getProjectRoot(businessName);
        
        // Check pom.xml exists
        assertThat(projectRoot.resolve("pom.xml"))
                .as("pom.xml should exist")
                .exists()
                .isRegularFile();
        
        // Check Maven directory structure
        assertThat(projectRoot.resolve("src/main/java"))
                .as("src/main/java directory should exist")
                .exists()
                .isDirectory();
        
        assertThat(projectRoot.resolve("src/main/resources"))
                .as("src/main/resources directory should exist")
                .exists()
                .isDirectory();
        
        System.out.println("✅ Maven project structure validation passed for: " + businessName);
    }
    
    /**
     * Validates generated Java classes exist and have correct structure.
     */
    public void validateJavaClasses(String businessName) {
        Path javaSourceRoot = getJavaSourceRoot(businessName);
        
        // Validate Application class
        validateApplicationClass(javaSourceRoot);
        
        // Validate Controller classes
        validateControllerClasses(javaSourceRoot, businessName);
        
        // Validate DAO classes
        validateDAOClasses(javaSourceRoot, businessName);
        
        // Validate DTO classes  
        validateDTOClasses(javaSourceRoot, businessName);
        
        // Validate Config classes
        validateConfigClasses(javaSourceRoot);
        
        System.out.println("✅ Java classes validation passed for: " + businessName);
    }
    
    /**
     * Validates Spring Boot configuration files.
     */
    public void validateSpringBootConfiguration(String businessName) {
        Path projectRoot = getProjectRoot(businessName);
        Path resourcesDir = projectRoot.resolve("src/main/resources");
        
        // Check application.properties exists
        assertThat(resourcesDir.resolve("application.properties"))
                .as("application.properties should exist")
                .exists()
                .isRegularFile();
        
        System.out.println("✅ Spring Boot configuration validation passed for: " + businessName);
    }
    
    /**
     * Validates API endpoint mappings in controller classes.
     */
    public void validateApiEndpointMappings(String businessName) {
        Path controllerDir = getJavaSourceRoot(businessName).resolve("controller");
        
        try (Stream<Path> controllerFiles = Files.list(controllerDir)) {
            List<Path> controllers = controllerFiles
                    .filter(path -> path.toString().endsWith("Controller.java"))
                    .toList();
            
            assertThat(controllers)
                    .as("At least one controller should be generated")
                    .isNotEmpty();
            
            // Validate each controller has proper REST annotations
            for (Path controller : controllers) {
                validateControllerContent(controller);
            }
            
        } catch (IOException e) {
            throw new AssertionError("Failed to read controller directory: " + e.getMessage());
        }
        
        System.out.println("✅ API endpoint mappings validation passed for: " + businessName);
    }
    
    /**
     * Validates basic structure for a specific SQL statement type.
     */
    public void validateBasicStructure(String businessName, SqlStatementType statementType) {
        Path projectRoot = getProjectRoot(businessName);
        
        // Basic existence check
        assertThat(projectRoot)
                .as("Project directory should exist for: " + businessName)
                .exists()
                .isDirectory();
        
        // Check basic Maven structure was created
        assertThat(projectRoot.resolve("pom.xml"))
                .as("pom.xml should be generated")
                .exists();
        
        System.out.println("✅ Basic structure validation passed for: " + businessName + " (" + statementType + ")");
    }
    
    /**
     * Validates that different business domains are properly separated.
     */
    public void validateBusinessDomainSeparation(String businessName) {
        Path projectRoot = getProjectRoot(businessName);
        
        assertThat(projectRoot)
                .as("Each business domain should have its own directory")
                .exists()
                .isDirectory();
        
        // Validate the domain-specific package structure
        Path javaRoot = getJavaSourceRoot(businessName);
        assertThat(javaRoot)
                .as("Each domain should have its own Java package structure")
                .exists()
                .isDirectory();
        
        System.out.println("✅ Business domain separation validation passed for: " + businessName);
    }
    
    /**
     * Compiles the generated project to ensure it's syntactically correct.
     */
    public boolean compileGeneratedProject(Path projectRoot) {
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                System.err.println("No Java compiler available");
                return false;
            }
            
            List<String> javaFiles = findAllJavaFiles(projectRoot);
            if (javaFiles.isEmpty()) {
                System.err.println("No Java files found to compile");
                return false;
            }
            
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
            
            // Compile with classpath including test dependencies
            List<String> compileOptions = Arrays.asList(
                    "-cp", System.getProperty("java.class.path"),
                    "-d", projectRoot.resolve("target/classes").toString()
            );
            
            // Create target directory
            Files.createDirectories(projectRoot.resolve("target/classes"));
            
            var compilationTask = compiler.getTask(
                    null, fileManager, null, compileOptions, null,
                    fileManager.getJavaFileObjectsFromStrings(javaFiles)
            );
            
            Boolean success = compilationTask.call();
            fileManager.close();
            
            System.out.println(success ? "✅ Compilation successful" : "❌ Compilation failed");
            return Boolean.TRUE.equals(success);
            
        } catch (Exception e) {
            System.err.println("Compilation error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Helper methods
    
    private Path getProjectRoot(String businessName) {
        // The generator creates a flat structure directly in the destination, not in subdirectories
        return baseOutputDir;
    }
    
    private Path getJavaSourceRoot(String businessName) {
        return getProjectRoot(businessName).resolve("src/main/java/com/jfeatures/msg/" + businessName.toLowerCase());
    }
    
    private void validateJavaSourceStructure(Path projectRoot) {
        Path javaRoot = projectRoot.resolve("src/main/java");
        
        assertThat(javaRoot)
                .as("Java source root should exist")
                .exists()
                .isDirectory();
        
        // Check package structure exists
        try (Stream<Path> paths = Files.walk(javaRoot, 4)) {
            boolean hasPackageStructure = paths.anyMatch(path -> 
                path.toString().contains("com/jfeatures/msg"));
                
            assertThat(hasPackageStructure)
                    .as("Should have proper package structure")
                    .isTrue();
        } catch (IOException e) {
            throw new AssertionError("Failed to validate package structure: " + e.getMessage());
        }
    }
    
    private void validateConfigurationFiles(Path projectRoot) {
        Path resourcesDir = projectRoot.resolve("src/main/resources");
        
        assertThat(resourcesDir)
                .as("Resources directory should exist")
                .exists()
                .isDirectory();
    }
    
    private void validateApplicationClass(Path javaSourceRoot) {
        try (Stream<Path> paths = Files.walk(javaSourceRoot)) {
            boolean hasApplicationClass = paths.anyMatch(path -> 
                path.getFileName().toString().equals("Application.java"));
            
            assertThat(hasApplicationClass)
                    .as("Should have Application.java class")
                    .isTrue();
        } catch (IOException e) {
            throw new AssertionError("Failed to find Application class: " + e.getMessage());
        }
    }
    
    private void validateControllerClasses(Path javaSourceRoot, String businessName) {
        Path controllerDir = javaSourceRoot.resolve("controller");
        
        assertThat(controllerDir)
                .as("Controller directory should exist")
                .exists()
                .isDirectory();
        
        try (Stream<Path> controllers = Files.list(controllerDir)) {
            long controllerCount = controllers
                    .filter(path -> path.toString().endsWith("Controller.java"))
                    .count();
            
            assertThat(controllerCount)
                    .as("Should have at least one controller")
                    .isGreaterThan(0);
        } catch (IOException e) {
            throw new AssertionError("Failed to validate controllers: " + e.getMessage());
        }
    }
    
    private void validateDAOClasses(Path javaSourceRoot, String businessName) {
        Path daoDir = javaSourceRoot.resolve("dao");
        
        assertThat(daoDir)
                .as("DAO directory should exist")
                .exists()
                .isDirectory();
    }
    
    private void validateDTOClasses(Path javaSourceRoot, String businessName) {
        Path dtoDir = javaSourceRoot.resolve("dto");
        
        assertThat(dtoDir)
                .as("DTO directory should exist")
                .exists()
                .isDirectory();
    }
    
    private void validateConfigClasses(Path javaSourceRoot) {
        Path configDir = javaSourceRoot.resolve("config");
        
        assertThat(configDir)
                .as("Config directory should exist")
                .exists()
                .isDirectory();
    }
    
    private void validateControllerContent(Path controllerFile) {
        try {
            String content = Files.readString(controllerFile);
            
            assertThat(content)
                    .as("Controller should have @RestController annotation")
                    .contains("@RestController");
                    
            assertThat(content)
                    .as("Controller should have @RequestMapping annotation")
                    .contains("@RequestMapping");
            
        } catch (IOException e) {
            throw new AssertionError("Failed to read controller file: " + e.getMessage());
        }
    }
    
    private List<String> findAllJavaFiles(Path projectRoot) throws IOException {
        List<String> javaFiles = new ArrayList<>();
        
        try (Stream<Path> paths = Files.walk(projectRoot)) {
            paths.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> javaFiles.add(path.toString()));
        }
        
        return javaFiles;
    }
}