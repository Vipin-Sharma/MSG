package com.jfeatures.msg.sql;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Utility class for reading files from the resources directory.
 * Simplified to only include methods actually used in the application.
 */
public class ReadFileFromResources {
    
    /**
     * Reads a file from the resources directory and returns its content as a String.
     * Used by the SQL file reading functionality in the generator.
     */
    public static String readFileFromResources(String fileName) {
        try (var inputStream = ReadFileFromResources.class.getClassLoader().getResourceAsStream(fileName)) {
            return new String(inputStream.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading file from resources: " + e.getMessage(), e);
        }
    }
}
