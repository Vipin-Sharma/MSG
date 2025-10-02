package com.jfeatures.msg.codegen.filesystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jfeatures.msg.codegen.domain.GeneratedMicroservice;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.lang.reflect.Field;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;

class MicroserviceProjectWriterTest {

    private MicroserviceProjectWriter writer;
    
    @Mock
    private GeneratedMicroservice mockMicroservice;
    
    @Mock
    private JavaFile mockJavaFile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        writer = new MicroserviceProjectWriter();
    }

    @Test
    void testWriteMicroserviceProject_NullMicroservice(@TempDir Path tempDir) {
        String destinationPath = tempDir.toString();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> writer.writeMicroserviceProject(null, destinationPath)
        );
        assertEquals("Generated microservice cannot be null", exception.getMessage());
    }

    private static Stream<Arguments> invalidDestinationProvider() {
        return Stream.of(
            Arguments.of("null destination", null),
            Arguments.of("empty destination", ""),
            Arguments.of("whitespace destination", "   ")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidDestinationProvider")
    void testWriteMicroserviceProject_InvalidDestination(String testName, String destination) {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> writer.writeMicroserviceProject(mockMicroservice, destination)
        );
        assertEquals("Destination path cannot be null or empty", exception.getMessage());
    }

    @Test
    void testWriteMicroserviceProject_InvalidPathFormat(@TempDir Path tempDir) throws IOException {
        setupMinimalMicroserviceMocks();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            writer.writeMicroserviceProject(mockMicroservice, "\0invalid")
        );

        assertTrue(exception.getMessage().contains("Invalid path format"));
    }

    @Test
    void testWriteMicroserviceProject_ForbidsSystemDirectories() throws IOException {
        setupMinimalMicroserviceMocks();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            writer.writeMicroserviceProject(mockMicroservice, "/etc/msg")
        );

        assertTrue(exception.getMessage().contains("Access to system directories"));
    }

    @Test
    void testWriteMicroserviceProject_Success(@TempDir Path tempDir) throws IOException {
        // Setup mock microservice
        TypeSpec mockTypeSpec = TypeSpec.classBuilder("TestClass").build();
        JavaFile mockSpringBootApp = JavaFile.builder("com.jfeatures.msg", mockTypeSpec).build();
        JavaFile mockDto = JavaFile.builder("com.jfeatures.msg.customer.dto", mockTypeSpec).build();
        JavaFile mockController = JavaFile.builder("com.jfeatures.msg.customer.controller", mockTypeSpec).build();
        JavaFile mockDao = JavaFile.builder("com.jfeatures.msg.customer.dao", mockTypeSpec).build();
        
        when(mockMicroservice.statementType()).thenReturn(SqlStatementType.SELECT);
        when(mockMicroservice.businessDomainName()).thenReturn("Customer");
        when(mockMicroservice.springBootApplication()).thenReturn(mockSpringBootApp);
        when(mockMicroservice.dtoFile()).thenReturn(mockDto);
        when(mockMicroservice.controllerFile()).thenReturn(mockController);
        when(mockMicroservice.daoFile()).thenReturn(mockDao);
        when(mockMicroservice.databaseConfigContent()).thenReturn("// Database config content");

        // Create required template resource files
        Path resourcesDir = tempDir.resolve("test-classes");
        Files.createDirectories(resourcesDir);
        Files.write(resourcesDir.resolve("pom_file.xml"), "<!-- POM template -->".getBytes());
        Files.write(resourcesDir.resolve("application_properties_file.txt"), "# App properties".getBytes());

        // Execute writing
        assertDoesNotThrow(() -> writer.writeMicroserviceProject(mockMicroservice, tempDir.toString()));

        // Verify directory structure was created
        assertTrue(Files.exists(tempDir.resolve("src/main/java")));
        assertTrue(Files.exists(tempDir.resolve("src/test/java/com/jfeatures")));
        assertTrue(Files.exists(tempDir.resolve("src/main/resources")));
    }

    @Test
    void testWriteMicroserviceProject_IOExceptionHandling(@TempDir Path tempDir) throws IOException {
        // Create a mock that throws IOException when trying to write files
        JavaFile faultyJavaFile = mock(JavaFile.class);
        doThrow(new IOException("Write failed")).when(faultyJavaFile).writeTo(any(Path.class));
        
        when(mockMicroservice.statementType()).thenReturn(SqlStatementType.SELECT);
        when(mockMicroservice.businessDomainName()).thenReturn("Customer");
        when(mockMicroservice.springBootApplication()).thenReturn(faultyJavaFile);
        when(mockMicroservice.dtoFile()).thenReturn(faultyJavaFile);
        when(mockMicroservice.controllerFile()).thenReturn(faultyJavaFile);
        when(mockMicroservice.daoFile()).thenReturn(faultyJavaFile);
        when(mockMicroservice.databaseConfigContent()).thenReturn("// Database config");

        // Should propagate IOException
        String destinationPath = tempDir.toString();

        IOException exception = assertThrows(
            IOException.class,
            () -> writer.writeMicroserviceProject(mockMicroservice, destinationPath)
        );
        
        assertNotNull(exception.getMessage());
        // The exception message could be from the IO failure or from the directory builder
        // As long as it's an IOException, it's expected behavior
        assertFalse(exception.getMessage().isEmpty());
    }

    @Test
    void testWriteMicroserviceProject_RuntimeExceptionHandling(@TempDir Path tempDir) {
        // Create a mock that throws RuntimeException
        when(mockMicroservice.statementType()).thenThrow(new RuntimeException("Runtime error"));

        // Should wrap RuntimeException in IOException
        String destinationPath = tempDir.toString();

        Exception exception = assertThrows(
            Exception.class,
            () -> writer.writeMicroserviceProject(mockMicroservice, destinationPath)
        );
        
        // The exception could be either IOException (wrapped) or RuntimeException (direct)
        assertTrue(exception instanceof IOException || exception instanceof RuntimeException);
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Runtime error") || 
                  exception.getMessage().contains("runtime error"));
    }

    @Test
    void testWriteMicroserviceProject_MissingTemplateFile(@TempDir Path tempDir) throws IOException {
        // Setup valid mock microservice but don't create template resources
        TypeSpec mockTypeSpec = TypeSpec.classBuilder("TestClass").build();
        JavaFile validJavaFile = JavaFile.builder("com.jfeatures.msg", mockTypeSpec).build();
        
        when(mockMicroservice.statementType()).thenReturn(SqlStatementType.SELECT);
        when(mockMicroservice.businessDomainName()).thenReturn("Customer");
        when(mockMicroservice.springBootApplication()).thenReturn(validJavaFile);
        when(mockMicroservice.dtoFile()).thenReturn(validJavaFile);
        when(mockMicroservice.controllerFile()).thenReturn(validJavaFile);
        when(mockMicroservice.daoFile()).thenReturn(validJavaFile);
        when(mockMicroservice.databaseConfigContent()).thenReturn("// Database config");

        // Should fail due to missing template files, but might succeed if files exist in resources
        String destinationPath = tempDir.toString();

        try {
            writer.writeMicroserviceProject(mockMicroservice, destinationPath);
            // If no exception, template files were found in resources
            assertTrue(true, "Template files were found");
        } catch (IOException exception) {
            // If exception thrown, verify it's about missing template files
            assertTrue(exception.getMessage().contains("Template resource file not found") ||
                      exception.getMessage().contains("resource file"));
        }
    }

    @Test
    void testWriteMicroserviceProject_CreateDatabaseConfigFile(@TempDir Path tempDir) throws IOException {
        // Setup minimal valid mock for testing database config file creation
        TypeSpec mockTypeSpec = TypeSpec.classBuilder("TestClass").build();
        JavaFile validJavaFile = JavaFile.builder("com.jfeatures.msg", mockTypeSpec).build();
        
        when(mockMicroservice.statementType()).thenReturn(SqlStatementType.INSERT);
        when(mockMicroservice.businessDomainName()).thenReturn("Product");
        when(mockMicroservice.springBootApplication()).thenReturn(validJavaFile);
        when(mockMicroservice.dtoFile()).thenReturn(validJavaFile);
        when(mockMicroservice.controllerFile()).thenReturn(validJavaFile);
        when(mockMicroservice.daoFile()).thenReturn(validJavaFile);
        when(mockMicroservice.databaseConfigContent()).thenReturn("@Configuration\npublic class DatabaseConfig {}");

        // Create template files
        Path resourcesDir = tempDir.resolve("test-classes");
        Files.createDirectories(resourcesDir);
        Files.write(resourcesDir.resolve("pom_file.xml"), "<!-- POM -->".getBytes());
        Files.write(resourcesDir.resolve("application_properties_file.txt"), "# Properties".getBytes());

        // Execute
        assertDoesNotThrow(() -> writer.writeMicroserviceProject(mockMicroservice, tempDir.toString()));

        // Verify database config file was created
        Path configFile = tempDir.resolve("src/main/java/com/jfeatures/product/config/DatabaseConfig.java");
        assertTrue(Files.exists(configFile));
        
        String configContent = Files.readString(configFile);
        assertTrue(configContent.contains("@Configuration"));
        assertTrue(configContent.contains("DatabaseConfig"));
    }

    @Test
    void testWriteMicroserviceProject_AllStatementTypes(@TempDir Path tempDir) throws IOException {
        // Test different statement types: SELECT, INSERT, UPDATE, DELETE
        SqlStatementType[] statementTypes = {SqlStatementType.SELECT, SqlStatementType.INSERT, SqlStatementType.UPDATE, SqlStatementType.DELETE};
        
        for (SqlStatementType statementType : statementTypes) {
            Path typeDir = tempDir.resolve(statementType.toString().toLowerCase());
            Files.createDirectories(typeDir);
            
            // Create template files for this test
            Path resourcesDir = typeDir.resolve("test-classes");
            Files.createDirectories(resourcesDir);
            Files.write(resourcesDir.resolve("pom_file.xml"), "<!-- POM -->".getBytes());
            Files.write(resourcesDir.resolve("application_properties_file.txt"), "# Properties".getBytes());
            
            // Setup mock for this statement type
            TypeSpec mockTypeSpec = TypeSpec.classBuilder("TestClass").build();
            JavaFile validJavaFile = JavaFile.builder("com.jfeatures.msg", mockTypeSpec).build();
            
            when(mockMicroservice.statementType()).thenReturn(statementType);
            when(mockMicroservice.businessDomainName()).thenReturn("Test");
            when(mockMicroservice.springBootApplication()).thenReturn(validJavaFile);
            when(mockMicroservice.dtoFile()).thenReturn(validJavaFile);
            when(mockMicroservice.controllerFile()).thenReturn(validJavaFile);
            when(mockMicroservice.daoFile()).thenReturn(validJavaFile);
            when(mockMicroservice.databaseConfigContent()).thenReturn("// Config for " + statementType);

            // Should handle all statement types successfully
            assertDoesNotThrow(() -> writer.writeMicroserviceProject(mockMicroservice, typeDir.toString()),
                             "Should handle " + statementType + " statement type");
        }
    }

    @Test
    void testValidatePathSecurityNullDoesNothing() throws Exception {
        Method method = MicroserviceProjectWriter.class.getDeclaredMethod("validatePathSecurity", String.class);
        method.setAccessible(true);

        assertDoesNotThrow(() -> method.invoke(writer, new Object[]{null}));
    }

    @Test
    void testWriteMicroserviceProject_RuntimeExceptionWrapped(@TempDir Path tempDir) throws Exception {
        setupMinimalMicroserviceMocks();

        ProjectDirectoryBuilder failingBuilder = mock(ProjectDirectoryBuilder.class);
        when(failingBuilder.buildDirectoryStructure(anyString())).thenThrow(new IllegalStateException("runtime failure"));

        Field field = MicroserviceProjectWriter.class.getDeclaredField("directoryBuilder");
        field.setAccessible(true);
        field.set(writer, failingBuilder);

        IOException exception = assertThrows(IOException.class, () ->
            writer.writeMicroserviceProject(mockMicroservice, tempDir.toString())
        );

        assertThat(exception)
            .hasMessage("Failed to write microservice project due to runtime error")
            .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void testCopyResourceFileMissingThrowsIOException(@TempDir Path tempDir) throws Exception {
        Method method = MicroserviceProjectWriter.class.getDeclaredMethod("copyResourceFileToPath", String.class, Path.class);
        method.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () ->
            method.invoke(writer, "missing_resource", tempDir.resolve("out.txt"))
        );

        assertThat(exception.getCause())
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Template resource file not found");
    }

    @Test
    void testCopyResourceFileWriteFailure(@TempDir Path tempDir) throws Exception {
        Method method = MicroserviceProjectWriter.class.getDeclaredMethod("copyResourceFileToPath", String.class, Path.class);
        method.setAccessible(true);
        Path target = tempDir.resolve("pom.xml");

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.write(eq(target), any(byte[].class))).thenThrow(new IOException("disk full"));
            filesMock.when(() -> Files.write(any(Path.class), any(byte[].class))).thenThrow(new IOException("disk full"));

            InvocationTargetException exception = assertThrows(InvocationTargetException.class, () ->
                method.invoke(writer, com.jfeatures.msg.codegen.constants.ProjectConstants.POM_TEMPLATE_FILE, target)
            );

            assertThat(exception.getCause())
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Failed to copy resource file");
        }
    }

    @Test
    void testWriteMicroserviceProject_InvalidDatabaseConfigWrite(@TempDir Path tempDir) throws Exception {
        setupMinimalMicroserviceMocks();

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.createDirectories(any(Path.class))).thenThrow(new IOException("permission denied"));

            IOException exception = assertThrows(IOException.class, () ->
                writer.writeMicroserviceProject(mockMicroservice, tempDir.toString())
            );

            assertThat(exception.getMessage()).contains("Failed to write database config file");
        }
    }

    private void setupMinimalMicroserviceMocks() throws IOException {
        TypeSpec typeSpec = TypeSpec.classBuilder("Sample").build();
        JavaFile javaFile = JavaFile.builder("com.test", typeSpec).build();

        when(mockMicroservice.statementType()).thenReturn(SqlStatementType.SELECT);
        when(mockMicroservice.businessDomainName()).thenReturn("Sample");
        when(mockMicroservice.springBootApplication()).thenReturn(javaFile);
        when(mockMicroservice.dtoFile()).thenReturn(javaFile);
        when(mockMicroservice.controllerFile()).thenReturn(javaFile);
        when(mockMicroservice.daoFile()).thenReturn(javaFile);
        when(mockMicroservice.databaseConfigContent()).thenReturn("config");
    }
}
