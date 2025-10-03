package com.jfeatures.msg.codegen;

import static org.junit.jupiter.api.Assertions.*;

import com.jfeatures.msg.codegen.util.JavaPackageNameBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class GenerateDatabaseConfigExceptionTest {

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

    static class GenerateDatabaseConfigWithIOException {

        static class FailingInputStream extends InputStream {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated IO failure");
            }

            @Override
            public byte[] readAllBytes() throws IOException {
                throw new IOException("Simulated readAllBytes failure");
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
        IllegalStateException missingFileException = assertThrows(
            IllegalStateException.class,
            () -> GenerateDatabaseConfigWithMissingTemplate.createDatabaseConfig("Product")
        );

        assertTrue(missingFileException.getMessage().contains("/templates/"));
        assertTrue(missingFileException.getMessage().contains(".template"));
    }

    @Test
    void testCreateDatabaseConfig_IOExceptionPreservesOriginalCause() {
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> GenerateDatabaseConfigWithIOException.createDatabaseConfigWithIOException("Order")
        );

        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IOException);
        assertTrue(exception.getCause().getMessage().contains("readAllBytes"));
    }

    @Test
    void testCreateDatabaseConfig_MultipleExceptionScenarios() {
        String[] businessNames = {"Customer", "Product", "Order", "Service"};

        for (String businessName : businessNames) {
            IllegalStateException missingException = assertThrows(
                IllegalStateException.class,
                () -> GenerateDatabaseConfigWithMissingTemplate.createDatabaseConfig(businessName)
            );
            assertTrue(missingException.getMessage().contains("Template file not found"));

            IllegalStateException ioException = assertThrows(
                IllegalStateException.class,
                () -> GenerateDatabaseConfigWithIOException.createDatabaseConfigWithIOException(businessName)
            );
            assertTrue(ioException.getMessage().contains("Failed to read template file"));
            assertNotNull(ioException.getCause());
        }
    }
}
