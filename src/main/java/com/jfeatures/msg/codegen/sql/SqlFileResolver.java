package com.jfeatures.msg.codegen.sql;

import com.jfeatures.msg.codegen.constants.ProjectConstants;
import com.jfeatures.msg.sql.ReadFileFromResources;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolves and reads SQL files for microservice generation.
 * Handles the logic for determining which SQL file to use based on user input
 * or default fallback behavior (UPDATE first, then SELECT).
 */
@Slf4j
public class SqlFileResolver {
    
    /**
     * Locates and reads the appropriate SQL file for microservice generation.
     * If no specific file is provided, attempts files in order: UPDATE, INSERT, DELETE, then SELECT.
     * 
     * @param specifiedSqlFileName the SQL file name specified by user, or null for default behavior
     * @return the SQL content as a string
     * @throws RuntimeException if no SQL file can be located or read
     */
    public String locateAndReadSqlFile(String specifiedSqlFileName) {
        if (specifiedSqlFileName != null && !specifiedSqlFileName.trim().isEmpty()) {
            log.info("Using specified SQL file: {}", specifiedSqlFileName);
            return readSqlFromResources(specifiedSqlFileName);
        }
        
        // Default behavior: try files in priority order: UPDATE, INSERT, DELETE, then SELECT
        return tryDefaultSqlFiles();
    }
    
    private String tryDefaultSqlFiles() {
        // Try UPDATE first (most common)
        try {
            String sql = readSqlFromResources(ProjectConstants.DEFAULT_UPDATE_SQL_FILE);
            log.info("Using UPDATE SQL file: {}", ProjectConstants.DEFAULT_UPDATE_SQL_FILE);
            return sql;
        } catch (RuntimeException e) {
            log.debug("UPDATE SQL file not found, trying INSERT");
        }
        
        // Try INSERT second
        try {
            String sql = readSqlFromResources(ProjectConstants.DEFAULT_INSERT_SQL_FILE);
            log.info("Using INSERT SQL file: {}", ProjectConstants.DEFAULT_INSERT_SQL_FILE);
            return sql;
        } catch (RuntimeException e) {
            log.debug("INSERT SQL file not found, trying DELETE");
        }
        
        // Try DELETE third
        try {
            String sql = readSqlFromResources(ProjectConstants.DEFAULT_DELETE_SQL_FILE);
            log.info("Using DELETE SQL file: {}", ProjectConstants.DEFAULT_DELETE_SQL_FILE);
            return sql;
        } catch (RuntimeException e) {
            log.debug("DELETE SQL file not found, trying SELECT");
        }
        
        // Fall back to SELECT (last resort)
        try {
            String sql = readSqlFromResources(ProjectConstants.DEFAULT_SELECT_SQL_FILE);
            log.info("Using SELECT SQL file: {}", ProjectConstants.DEFAULT_SELECT_SQL_FILE);
            return sql;
        } catch (RuntimeException e) {
            throw new RuntimeException("No default SQL files found. Please provide a specific SQL file or ensure at least one of the following files exists: " +
                    ProjectConstants.DEFAULT_UPDATE_SQL_FILE + ", " + 
                    ProjectConstants.DEFAULT_INSERT_SQL_FILE + ", " + 
                    ProjectConstants.DEFAULT_DELETE_SQL_FILE + ", " + 
                    ProjectConstants.DEFAULT_SELECT_SQL_FILE);
        }
    }
    
    private String readSqlFromResources(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL file name cannot be null or empty");
        }
        
        return ReadFileFromResources.readFileFromResources(fileName);
    }
}