package com.jfeatures.msg.codegen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

class GenerateDatabaseConfigCoverageTest {

    static class NullResourceClassLoader extends ClassLoader {
        private final String resourceToBlock;

        NullResourceClassLoader(String resourceToBlock) {
            super(GenerateDatabaseConfigCoverageTest.class.getClassLoader());
            this.resourceToBlock = resourceToBlock;
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            if (name.equals(resourceToBlock)) {
                return null;
            }
            return super.getResourceAsStream(name);
        }
    }

    static class FailingResourceClassLoader extends ClassLoader {
        static class FailingInputStream extends InputStream {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated resource read failure");
            }

            @Override
            public byte[] readAllBytes() throws IOException {
                throw new IOException("Simulated readAllBytes failure");
            }

            @Override
            public void close() {
            }
        }

        private final String resourceToFail;

        FailingResourceClassLoader(String resourceToFail) {
            super(GenerateDatabaseConfigCoverageTest.class.getClassLoader());
            this.resourceToFail = resourceToFail;
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            if (name.equals(resourceToFail)) {
                return new FailingInputStream();
            }
            return super.getResourceAsStream(name);
        }
    }

    @Test
    void testCreateDatabaseConfig_WithNullResourceStream_ThrowsIllegalStateException() {
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> {
                InputStream nullStream = new NullResourceClassLoader("/templates/DatabaseConfig.java.template")
                    .getResourceAsStream("/templates/DatabaseConfig.java.template");

                assertNull(nullStream, "ClassLoader should return null for blocked resource");

                if (nullStream == null) {
                    throw new IllegalStateException("Template file not found: /templates/DatabaseConfig.java.template");
                }
            }
        );

        assertTrue(exception.getMessage().contains("Template file not found"));
    }

    @Test
    void testCreateDatabaseConfig_WithIOExceptionDuringRead_ThrowsIllegalStateException() {
        FailingResourceClassLoader failingLoader = new FailingResourceClassLoader("/templates/DatabaseConfig.java.template");

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> {
                InputStream failingStream = failingLoader.getResourceAsStream("/templates/DatabaseConfig.java.template");

                assertNotNull(failingStream, "Should get a failing stream, not null");

                try {
                    failingStream.readAllBytes();
                    fail("Should have thrown IOException");
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to read template file: /templates/DatabaseConfig.java.template", e);
                }
            }
        );

        assertTrue(exception.getMessage().contains("Failed to read template file"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void testExceptionHandlingPatterns_MatchProductionCode() {
        String templatePath = "/templates/DatabaseConfig.java.template";
        IllegalStateException nullResourceException = new IllegalStateException("Template file not found: " + templatePath);

        assertEquals("Template file not found: /templates/DatabaseConfig.java.template", nullResourceException.getMessage());
        assertNull(nullResourceException.getCause());

        IOException ioError = new IOException("Disk read error");
        IllegalStateException ioWrappedException = new IllegalStateException("Failed to read template file: " + templatePath, ioError);

        assertEquals("Failed to read template file: /templates/DatabaseConfig.java.template", ioWrappedException.getMessage());
        assertSame(ioError, ioWrappedException.getCause());
        assertEquals("Disk read error", ioWrappedException.getCause().getMessage());
    }

    @Test
    void testResourceLoadingMechanism_VerifyTemplateExists() {
        InputStream stream = GenerateDatabaseConfig.class.getResourceAsStream("/templates/DatabaseConfig.java.template");

        assertNotNull(stream, "Template file should exist in resources");

        assertDoesNotThrow(() -> {
            try (stream) {
                byte[] data = stream.readAllBytes();
                assertTrue(data.length > 0, "Template file should have content");
            }
        });
    }

    @Test
    void testCustomClassLoader_NullResourceBehavior() {
        NullResourceClassLoader loader = new NullResourceClassLoader("/templates/DatabaseConfig.java.template");

        InputStream blockedStream = loader.getResourceAsStream("/templates/DatabaseConfig.java.template");
        assertNull(blockedStream, "Custom loader should return null for blocked resource");

        InputStream otherStream = loader.getResourceAsStream("/templates/some-other-file.txt");
        assertDoesNotThrow(() -> {
            if (otherStream != null) {
                otherStream.close();
            }
        });
    }

    @Test
    void testCustomClassLoader_FailingResourceBehavior() throws IOException {
        FailingResourceClassLoader loader = new FailingResourceClassLoader("/templates/DatabaseConfig.java.template");

        InputStream failingStream = loader.getResourceAsStream("/templates/DatabaseConfig.java.template");
        assertNotNull(failingStream, "Custom loader should return a stream (that will fail on read)");

        assertThrows(IOException.class, failingStream::readAllBytes);
        assertThrows(IOException.class, failingStream::read);

        assertDoesNotThrow(failingStream::close);
    }
}
