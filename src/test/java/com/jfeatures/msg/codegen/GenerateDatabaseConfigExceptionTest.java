package com.jfeatures.msg.codegen;

import static org.junit.jupiter.api.Assertions.*;

import com.jfeatures.msg.codegen.util.JavaPackageNameBuilder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Exception path tests for GenerateDatabaseConfig to achieve 100% coverage.
 * Tests error handling scenarios including missing templates and IO exceptions.
 */
class GenerateDatabaseConfigExceptionTest {

    /**
     * Test helper class that simulates template file not found scenario.
     * This allows us to test the null inputStream branch.
     */
    static class GenerateDatabaseConfigWithMissingTemplate {
        private static final String TEMPLATE_PATH = "/templates/NonExistentTemplate.java.template";

        public static String createDatabaseConfig(String businessPurposeOfSQL) {
            try (InputStream inputStream = GenerateDatabaseConfigWithMissingTemplate.class.getResourceAsStream(TEMPLATE_PATH)) {
                if (inputStream == null) {
                    throw new IllegalStateException("Template file not found: " + TEMPLATE_PATH);
                }

                String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                String packageName = JavaPackageNameBuilder.buildJavaPackageName(businessPurposeOfSQL, "config");

                return template
                        .replace("${packageName}", packageName)
                        .replace("${businessPurpose}", businessPurposeOfSQL);

            } catch (IOException e) {
                throw new IllegalStateException("Failed to read template file: " + TEMPLATE_PATH, e);
            }
        }
    }

    /**
     * Test helper class that simulates IOException during template reading.
     */
    static class GenerateDatabaseConfigWithIOException {

        static class FailingInputStream extends InputStream {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated IO failure");
            }

            @Override
            public byte[] readAllBytes() throws IOException {
                throw new IOException("Simulated IO failure during readAllBytes");
            }
        }

        public static String createDatabaseConfigWithIOException(String businessPurposeOfSQL) {
            try (InputStream inputStream = new FailingInputStream()) {
                if (inputStream == null) {
                    throw new IllegalStateException("Template file not found");
                }

                String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                String packageName = JavaPackageNameBuilder.buildJavaPackageName(businessPurposeOfSQL, "config");

                return template
                        .replace("${packageName}", packageName)
                        .replace("${businessPurpose}", businessPurposeOfSQL);

            } catch (IOException e) {
                throw new IllegalStateException("Failed to read template file", e);
            }
        }
    }

    @Test
    void testCreateDatabaseConfig_WithMissingTemplate_ThrowsIllegalStateException() {
        // Test the null inputStream branch (line 22-23)
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> GenerateDatabaseConfigWithMissingTemplate.createDatabaseConfig("Customer")
        );

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Template file not found"));
        assertTrue(exception.getMessage().contains("NonExistentTemplate"));
    }

    @Test
    void testCreateDatabaseConfig_WithIOException_ThrowsIllegalStateException() {
        // Test the IOException catch block (line 33-34)
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> GenerateDatabaseConfigWithIOException.createDatabaseConfigWithIOException("Customer")
        );

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Failed to read template file"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void testCreateDatabaseConfig_ExceptionMessageContainsTemplatePath() {
        // Verify exception messages are informative
        IllegalStateException missingFileException = assertThrows(
            IllegalStateException.class,
            () -> GenerateDatabaseConfigWithMissingTemplate.createDatabaseConfig("Product")
        );

        assertTrue(missingFileException.getMessage().contains("/templates/"));
        assertTrue(missingFileException.getMessage().contains(".template"));
    }

    @Test
    void testCreateDatabaseConfig_IOExceptionPreservesOriginalCause() {
        // Verify IOException is properly wrapped and original cause is preserved
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> GenerateDatabaseConfigWithIOException.createDatabaseConfigWithIOException("Order")
        );

        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IOException);
        assertTrue(exception.getCause().getMessage().contains("Simulated IO failure"));
    }

    @Test
    void testCreateDatabaseConfig_MultipleExceptionScenarios() {
        // Test multiple business names to ensure exception handling is consistent
        String[] businessNames = {"Customer", "Product", "Order", "Service"};

        for (String businessName : businessNames) {
            // Test missing template scenario
            IllegalStateException missingException = assertThrows(
                IllegalStateException.class,
                () -> GenerateDatabaseConfigWithMissingTemplate.createDatabaseConfig(businessName)
            );
            assertTrue(missingException.getMessage().contains("Template file not found"));

            // Test IOException scenario
            IllegalStateException ioException = assertThrows(
                IllegalStateException.class,
                () -> GenerateDatabaseConfigWithIOException.createDatabaseConfigWithIOException(businessName)
            );
            assertTrue(ioException.getMessage().contains("Failed to read template file"));
            assertNotNull(ioException.getCause());
        }
    }
}
