package com.jfeatures.msg.sql;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for reading files from the resources directory.
 * Provides robust file reading with proper error handling and resource management.
 */
public final class ReadFileFromResources {

    private static java.util.function.Supplier<ClassLoader> classLoaderSupplier = ReadFileFromResources.class::getClassLoader;

    private ReadFileFromResources() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    /**
     * Reads a file from the resources directory and returns its content as a String using UTF-8 encoding.
     * Used by the SQL file reading functionality in the generator.
     * 
     * @param fileName the name of the file to read from resources
     * @return the file content as a String
     * @throws IllegalArgumentException if fileName is null, empty, or file doesn't exist
     * @throws UncheckedIOException if there's an I/O error reading the file
     */
    public static String readFileFromResources(String fileName) {
        return readFileFromResources(fileName, StandardCharsets.UTF_8);
    }
    
    /**
     * Reads a file from the resources directory and returns its content as a String with specified encoding.
     * 
     * @param fileName the name of the file to read from resources
     * @param charset the character encoding to use
     * @return the file content as a String
     * @throws IllegalArgumentException if fileName is null, empty, or file doesn't exist
     * @throws UncheckedIOException if there's an I/O error reading the file
     */
    public static String readFileFromResources(String fileName, Charset charset) {
        // Comprehensive input validation
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }
        
        // Validate file name to prevent directory traversal
        validateFileNameSecurity(fileName);
        
        // Normalize path separators for cross-platform compatibility
        String normalizedFileName = fileName.replace('\\', '/');
        
        try (var inputStream = classLoaderSupplier.get().getResourceAsStream(normalizedFileName)) {
            // Critical fix: Check for null before calling readAllBytes()
            if (inputStream == null) {
                throw new IllegalArgumentException(
                    String.format("Resource file not found: '%s'. " +
                        "Ensure the file exists in src/main/resources/ and the path is correct. " +
                        "Expected SQL files: sample_parameterized_sql.sql, sample_insert_parameterized.sql, " +
                        "sample_update_parameterized.sql, sample_delete_parameterized.sql", 
                        normalizedFileName)
                );
            }
            
            return new String(inputStream.readAllBytes(), charset);
        } catch (IOException e) {
            throw new UncheckedIOException(
                String.format("Failed to read resource file '%s': %s", normalizedFileName, e.getMessage()), 
                e
            );
        }
    }
    
    /**
     * Validates file name to prevent directory traversal attacks when accessing resources.
     *
     * @param fileName the file name to validate
     * @throws IllegalArgumentException if the file name contains directory traversal patterns
     */
    private static void validateFileNameSecurity(String fileName) {
        // Check for directory traversal patterns and absolute paths
        if (fileName.contains("../") || fileName.contains("..\\") ||
            fileName.startsWith("./") || fileName.contains("/../") ||
            fileName.startsWith("/") || fileName.startsWith("\\")) {
            throw new IllegalArgumentException("File name contains invalid directory traversal patterns: " + fileName);
        }

        // Ensure filename doesn't contain system paths using string operations instead of regex
        String lowerFileName = fileName.toLowerCase();
        String[] systemPaths = {"etc/", "bin/", "usr/", "var/", "sys/", "proc/",
                                "windows/", "program files/",
                                "etc\\", "bin\\", "usr\\", "var\\", "sys\\", "proc\\",
                                "windows\\", "program files\\"};

        for (String systemPath : systemPaths) {
            if (lowerFileName.contains("/" + systemPath) || lowerFileName.contains("\\" + systemPath)) {
                throw new IllegalArgumentException("Access to system paths is not allowed: " + fileName);
            }
        }
    }

    static void setClassLoaderSupplier(java.util.function.Supplier<ClassLoader> supplier) {
        classLoaderSupplier = supplier != null ? supplier : ReadFileFromResources.class::getClassLoader;
    }
}
