package com.jfeatures.msg.sql;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for reading files from the resources directory.
 * Provides robust file reading with proper error handling and resource management.
 */
public class ReadFileFromResources {
    
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
        
        // Normalize path separators for cross-platform compatibility
        String normalizedFileName = fileName.replace('\\', '/');
        
        try (var inputStream = ReadFileFromResources.class.getClassLoader().getResourceAsStream(normalizedFileName)) {
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
}
