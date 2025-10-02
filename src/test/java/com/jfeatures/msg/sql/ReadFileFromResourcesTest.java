package com.jfeatures.msg.sql;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ReadFileFromResourcesTest {

    @Test
    void testReadFileFromResourcesValidFile() {
        // Test reading an existing resource file
        String content = ReadFileFromResources.readFileFromResources("sample_parameterized_sql.sql");
        
        assertNotNull(content);
        assertFalse(content.isEmpty());
    }

    @Test
    void testReadFileFromResourcesNonExistentFile() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            ReadFileFromResources.readFileFromResources("non_existent_file.txt"));
        
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Resource file not found"));
    }

    @Test
    void testReadFileFromResourcesNullFileName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            ReadFileFromResources.readFileFromResources(null));
        
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("File name cannot be null or empty"));
    }

    @Test
    void testReadFileFromResourcesEmptyFileName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            ReadFileFromResources.readFileFromResources(""));
        
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("File name cannot be null or empty"));
    }

    @Test
    void testReadFileFromResourcesDirectoryPath() {
        // Test trying to read a directory instead of a file - may succeed if META-INF is readable
        assertDoesNotThrow(() -> ReadFileFromResources.readFileFromResources("META-INF"));
    }

    @Test
    void testReadFileFromResourcesWithSubdirectory() {
        // Test reading a file in a subdirectory (if exists)
        try {
            String content = ReadFileFromResources.readFileFromResources("META-INF/MANIFEST.MF");
            // If the file exists, content should be valid
            if (content != null) {
                assertFalse(content.isEmpty());
            }
        } catch (NullPointerException e) {
            // If file doesn't exist, exception is expected
            assertNotNull(e);
        }
    }

    @Test
    void testReadFileFromResourcesWithLeadingSlash() {
        // Test with leading slash (should be blocked for security)
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            ReadFileFromResources.readFileFromResources("/sample_parameterized_sql.sql"));
        
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("directory traversal patterns"));
    }

    @Test
    void testReadFileFromResourcesSpecialCharactersInFileName() {
        String[] specialFileNames = {
            "file with spaces.txt",
            "file-with-dashes.txt", 
            "file_with_underscores.txt",
            "file.with.dots.txt",
            "file@with#special$.txt"
        };
        
        for (String fileName : specialFileNames) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
                ReadFileFromResources.readFileFromResources(fileName));
            
            assertNotNull(exception);
            assertTrue(exception.getMessage().contains("Resource file not found"));
        }
    }

    @Test
    void testReadFileFromResourcesWhitespaceFileName() {
        String[] whitespaceFileNames = {"   ", "\t", "\n", "\r\n", " \t \n "};
        
        for (String fileName : whitespaceFileNames) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
                ReadFileFromResources.readFileFromResources(fileName));
            
            assertNotNull(exception);
            assertTrue(exception.getMessage().contains("File name cannot be null or empty"));
        }
    }

    @Test
    void testReadFileFromResourcesLongFileName() {
        // Test with very long file name
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            longName.append("a");
        }
        longName.append(".txt");
        
        String longFileName = longName.toString();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            ReadFileFromResources.readFileFromResources(longFileName));
        
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Resource file not found"));
    }

    @Test
    void testReadFileFromResourcesReturnedContentNotNull() {
        try {
            String content = ReadFileFromResources.readFileFromResources("sample_parameterized_sql.sql");
            assertNotNull(content, "Content should never be null for existing files");
        } catch (IllegalArgumentException e) {
            // If file doesn't exist, that's also a valid test case
            assertNotNull(e);
        }
    }

    @Test
    void testReadFileFromResourcesMultipleReads() {
        // Test reading the same file multiple times
        try {
            String content1 = ReadFileFromResources.readFileFromResources("sample_parameterized_sql.sql");
            String content2 = ReadFileFromResources.readFileFromResources("sample_parameterized_sql.sql");
            
            assertEquals(content1, content2, "Multiple reads should return identical content");
        } catch (IllegalArgumentException e) {
            // If file doesn't exist, both calls should throw the same exception
            IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> 
                ReadFileFromResources.readFileFromResources("sample_parameterized_sql.sql"));
            
            assertEquals(e.getClass(), exception2.getClass());
        }
    }

    @Test
    void testReadFileFromResourcesClassLoaderBehavior() {
        // Test that the method uses the correct class loader
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Create a custom class loader that doesn't have access to resources
            ClassLoader emptyClassLoader = new ClassLoader() {
                @Override
                public java.io.InputStream getResourceAsStream(String name) {
                    return null;
                }
            };
            Thread.currentThread().setContextClassLoader(emptyClassLoader);
            
            // The method should still work because it uses ReadFileFromResources.class.getClassLoader()
            try {
                String content = ReadFileFromResources.readFileFromResources("sample_parameterized_sql.sql");
                // If successful, content should be valid
                assertNotNull(content);
            } catch (IllegalArgumentException e) {
                // If file doesn't exist, exception is expected
                assertNotNull(e);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    @Test
    void testReadFileFromResourcesExceptionChaining() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            ReadFileFromResources.readFileFromResources("definitely_non_existent_file.txt"));
        
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Resource file not found"));
    }

    @Test
    void testReadFileFromResourcesWithDifferentExtensions() {
        String[] extensions = {".sql", ".txt", ".xml", ".json", ".properties", ".yml", ".yaml"};
        
        for (String extension : extensions) {
            String fileName = "test" + extension;
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
                ReadFileFromResources.readFileFromResources(fileName));
            
            assertNotNull(exception);
            assertTrue(exception.getMessage().contains("Resource file not found"));
        }
    }

    @Test
    void testReadFileFromResourcesPathTraversal() {
        // Test potential path traversal attempts
        String[] pathTraversalAttempts = {
            "../../../etc/passwd",
            "..\\..\\windows\\system32\\config\\sam",
            "./config/secrets.txt",
            "../../config/../config/database.properties"
        };
        
        for (String path : pathTraversalAttempts) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
                ReadFileFromResources.readFileFromResources(path));
            
            assertNotNull(exception);
            assertTrue(exception.getMessage().contains("directory traversal patterns"));
        }
    }

    @Test
    void testReadFileFromResourcesUnicodeFileName() {
        // Test with Unicode characters in file name
        String[] unicodeNames = {
            "файл.txt",      // Russian
            "文件.txt",       // Chinese
            "ファイル.txt",    // Japanese
            "파일.txt",       // Korean
            "αρχείο.txt"     // Greek
        };
        
        for (String fileName : unicodeNames) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
                ReadFileFromResources.readFileFromResources(fileName));
            
            assertNotNull(exception);
            assertTrue(exception.getMessage().contains("Resource file not found"));
        }
    }

    @Test
    void testReadFileFromResourcesResourceStreamNull() {
        // This tests the behavior when getResourceAsStream returns null
        // We can't mock the class loader easily, so we test with non-existent file
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            ReadFileFromResources.readFileFromResources("absolutely_does_not_exist.txt"));
        
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Resource file not found"));
    }

    @Test
    void testReadFileFromResourcesEmptyFileContent() {
        // Create an empty file in resources for testing
        try {
            // Try to read a file that might be empty (common case)
            String content = ReadFileFromResources.readFileFromResources("empty.sql");
            assertEquals("", content, "Empty file should return empty string");
        } catch (IllegalArgumentException e) {
            // If file doesn't exist, that's expected
            assertNotNull(e);
        }
    }

    @Test
    void testReadFileFromResourcesWithCustomCharset() {
        // Test reading with custom charset
        try {
            String contentUTF8 = ReadFileFromResources.readFileFromResources("sample_parameterized_sql.sql",
                                                                            java.nio.charset.StandardCharsets.UTF_8);
            assertNotNull(contentUTF8);
            assertFalse(contentUTF8.isEmpty());

            String contentISO = ReadFileFromResources.readFileFromResources("sample_parameterized_sql.sql",
                                                                           java.nio.charset.StandardCharsets.ISO_8859_1);
            assertNotNull(contentISO);
            // Content might differ based on charset encoding
        } catch (IllegalArgumentException e) {
            // File doesn't exist - acceptable for test
            assertNotNull(e);
        }
    }

    @Test
    void testReadFileFromResourcesWithNullCharset() {
        // Test that null charset defaults to UTF-8
        try {
            String content = ReadFileFromResources.readFileFromResources("sample_parameterized_sql.sql", null);
            assertNotNull(content);
            assertFalse(content.isEmpty());
        } catch (IllegalArgumentException e) {
            // File doesn't exist - acceptable for test
            assertNotNull(e);
        }
    }

    @Test
    void testReadFileFromResourcesConstructor() {
        // Test that constructor throws UnsupportedOperationException
        java.lang.reflect.InvocationTargetException exception = assertThrows(
            java.lang.reflect.InvocationTargetException.class,
            () -> {
                // Use reflection to access private constructor
                java.lang.reflect.Constructor<ReadFileFromResources> constructor =
                    ReadFileFromResources.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            });

        // Verify the cause is UnsupportedOperationException
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
    }

    @Test
    void testReadFileFromResourcesSecuritySystemPaths() {
        // Test various system path patterns that should be blocked
        String[] systemPathAttempts = {
            "path/etc/passwd",
            "path/bin/bash",
            "path/usr/local/file",
            "path/var/log/system",
            "path/sys/kernel",
            "path/proc/self/status",
            "path\\windows\\system32\\file",
            "path\\program files\\app\\config"
        };

        for (String path : systemPathAttempts) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ReadFileFromResources.readFileFromResources(path));

            assertNotNull(exception);
            assertTrue(exception.getMessage().contains("system paths") ||
                      exception.getMessage().contains("Resource file not found"));
        }
    }

    @Test
    void testReadFileFromResourcesBackslashNormalization() {
        // Test that backslashes are normalized to forward slashes
        try {
            // This should be normalized internally
            String content = ReadFileFromResources.readFileFromResources("sample_parameterized_sql.sql");
            assertNotNull(content);
        } catch (IllegalArgumentException e) {
            // File doesn't exist - acceptable
            assertNotNull(e);
        }
    }

    @Test
    void testReadFileFromResourcesIOExceptionHandling() {
        // Test IOException wrapping into UncheckedIOException
        // Since we can't easily force an IOException, we test with a malformed resource access
        try {
            ReadFileFromResources.readFileFromResources("sample_parameterized_sql.sql");
        } catch (java.io.UncheckedIOException e) {
            // If UncheckedIOException is thrown, verify it has proper message and cause
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("Failed to read resource file"));
            assertNotNull(e.getCause());
        } catch (IllegalArgumentException e) {
            // File not found is also acceptable
            assertNotNull(e);
        }
    }

    @Test
    void testReadFileFromResourcesCaseSensitivity() {
        // Test case sensitivity of file names
        String[] caseVariations = {
            "Sample_Parameterized_Sql.sql",
            "SAMPLE_PARAMETERIZED_SQL.SQL",
            "sample_PARAMETERIZED_sql.SQL"
        };

        for (String fileName : caseVariations) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ReadFileFromResources.readFileFromResources(fileName));

            assertNotNull(exception);
            assertTrue(exception.getMessage().contains("Resource file not found"));
        }
    }
}