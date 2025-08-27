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
     * If no specific file is provided, attempts UPDATE file first, then falls back to SELECT.
     * 
     * @param specifiedSqlFileName the SQL file name specified by user, or null for default behavior
     * @return the SQL content as a string
     * @throws Exception if no SQL file can be located or read
     */
    public String locateAndReadSqlFile(String specifiedSqlFileName) throws Exception {
        if (specifiedSqlFileName != null && !specifiedSqlFileName.trim().isEmpty()) {
            log.info("Using specified SQL file: {}", specifiedSqlFileName);
            return readSqlFromResources(specifiedSqlFileName);
        }
        
        // Default behavior: try UPDATE first, then fall back to SELECT
        return tryUpdateThenSelectSqlFiles();
    }
    
    private String tryUpdateThenSelectSqlFiles() throws Exception {
        try {
            String sql = readSqlFromResources(ProjectConstants.DEFAULT_UPDATE_SQL_FILE);
            log.info("Using UPDATE SQL file: {}", ProjectConstants.DEFAULT_UPDATE_SQL_FILE);
            return sql;
        } catch (Exception e) {
            log.info("UPDATE SQL file not found, falling back to SELECT SQL file");
            String sql = readSqlFromResources(ProjectConstants.DEFAULT_SELECT_SQL_FILE);
            log.info("Using SELECT SQL file: {}", ProjectConstants.DEFAULT_SELECT_SQL_FILE);
            return sql;
        }
    }
    
    private String readSqlFromResources(String fileName) throws Exception {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL file name cannot be null or empty");
        }
        
        return ReadFileFromResources.readFileFromResources(fileName);
    }
}