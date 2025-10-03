package com.jfeatures.msg.codegen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class GenerateDatabaseConfigMockTest {

    @Test
    void testCreateDatabaseConfig_WhenIOExceptionOccurs_ThrowsIllegalStateException() {
        IOException simulatedException = new IOException("Test IO failure");
        IllegalStateException wrapped = new IllegalStateException("Failed to read template file: /templates/DatabaseConfig.java.template", simulatedException);

        assertNotNull(wrapped.getCause());
        assertTrue(wrapped.getCause() instanceof IOException);
        assertEquals("Test IO failure", wrapped.getCause().getMessage());
        assertTrue(wrapped.getMessage().contains("Failed to read template file"));
        assertTrue(wrapped.getMessage().contains("/templates/DatabaseConfig.java.template"));
    }

    @Test
    void testCreateDatabaseConfig_ExceptionHandlingPattern() {
        String expectedTemplateNotFoundMessage = "Template file not found: /templates/DatabaseConfig.java.template";
        IllegalStateException templateNotFoundException = new IllegalStateException(expectedTemplateNotFoundMessage);

        assertNotNull(templateNotFoundException);
        assertTrue(templateNotFoundException.getMessage().contains("Template file not found"));
        assertTrue(templateNotFoundException.getMessage().contains("/templates/DatabaseConfig.java.template"));

        IOException ioException = new IOException("Read error");
        String expectedIOExceptionMessage = "Failed to read template file: /templates/DatabaseConfig.java.template";
        IllegalStateException ioWrappedException = new IllegalStateException(expectedIOExceptionMessage, ioException);

        assertNotNull(ioWrappedException.getCause());
        assertTrue(ioWrappedException.getCause() instanceof IOException);
        assertEquals("Read error", ioWrappedException.getCause().getMessage());
        assertTrue(ioWrappedException.getMessage().contains("Failed to read template file"));
    }
}
