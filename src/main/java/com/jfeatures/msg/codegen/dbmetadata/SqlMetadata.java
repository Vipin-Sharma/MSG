package com.jfeatures.msg.codegen.dbmetadata;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class SqlMetadata {

    private static final Logger logger = LoggerFactory.getLogger(SqlMetadata.class);

    private final JdbcTemplate jdbcTemplate;

    public SqlMetadata(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ColumnMetadata> getColumnMetadata(String query) {
        // Input validation to prevent SQL injection
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL query cannot be null or empty");
        }
        
        // Remove SQL comments before validation
        String sanitizedQuery = query
            .replaceAll("(?s)/\\*.*?\\*/", " ")
            .replaceAll("(?m)--.*$", " ");

        // Basic SQL injection prevention - validate query structure
        String normalizedQuery = sanitizedQuery.trim().replaceAll("\\s+", " ").toUpperCase();
        if (!isValidQueryStructure(normalizedQuery)) {
            throw new IllegalArgumentException("Invalid SQL query structure detected");
        }

        List<ColumnMetadata> columnMetadataList = new ArrayList<>();
        
        try {
            jdbcTemplate.query(query, new RowMapper<ColumnMetadata>() {
            @Override
            public ColumnMetadata mapRow(ResultSet rs, int rowNum) throws SQLException {
                ResultSetMetaData metadata = rs.getMetaData();

                int columnCount = metadata.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    ColumnMetadata columnMetadata = new ColumnMetadata();
                    columnMetadata.setColumnName(metadata.getColumnName(i));
                    columnMetadata.setColumnAlias(metadata.getColumnLabel(i));
                    columnMetadata.setTableName(metadata.getTableName(i));
                    columnMetadata.setColumnType(metadata.getColumnType(i));
                    columnMetadata.setColumnTypeName(metadata.getColumnTypeName(i));
                    columnMetadata.setColumnClassName(metadata.getColumnClassName(i));
                    columnMetadata.setColumnDisplaySize(metadata.getColumnDisplaySize(i));
                    columnMetadata.setPrecision(metadata.getPrecision(i));
                    columnMetadata.setScale(metadata.getScale(i));
                    columnMetadata.setIsNullable(metadata.isNullable(i));
                    columnMetadata.setAutoIncrement(metadata.isAutoIncrement(i));
                    columnMetadata.setCaseSensitive(metadata.isCaseSensitive(i));
                    columnMetadata.setReadOnly(metadata.isReadOnly(i));
                    columnMetadata.setWritable(metadata.isWritable(i));
                    columnMetadata.setDefinitelyWritable(metadata.isDefinitelyWritable(i));
                    columnMetadata.setCurrency(metadata.isCurrency(i));
                    columnMetadata.setSigned(metadata.isSigned(i));

                    columnMetadataList.add(columnMetadata);
                }

                return null;
            }
            });
        } catch (org.springframework.dao.DataAccessException e) {
            // Log error with contextual information for debugging
            logger.error("Failed to fetch column metadata for query: {}. Error: {}",
                         query.substring(0, Math.min(100, query.length())), e.getMessage(), e);
            // Re-throw with additional context to maintain test compatibility and provide debugging info
            throw new org.springframework.dao.DataAccessException(
                "Unable to retrieve column metadata from database for the provided SQL query", e) {};
        }
        
        return columnMetadataList;
    }
    
    /**
     * Validates SQL query structure to prevent basic SQL injection attacks.
     * Ensures the query follows expected patterns for SELECT, INSERT, UPDATE, DELETE operations.
     * 
     * @param normalizedQuery the SQL query in uppercase and trimmed
     * @return true if the query structure is valid, false otherwise
     */
    private boolean isValidQueryStructure(String normalizedQuery) {
        // Allow standard CRUD operations and CTEs
        if (!normalizedQuery.matches("^(WITH|SELECT|INSERT|UPDATE|DELETE)\\s+.*")) {
            return false;
        }

        // Prevent dangerous SQL keywords and patterns (only multiple statements)
        String[] dangerousPatterns = {
            ";\\s*(DROP|CREATE|ALTER|EXEC|EXECUTE)\\s+"
        };

        for (String pattern : dangerousPatterns) {
            if (normalizedQuery.matches(".*" + pattern + ".*")) {
                return false;
            }
        }

        return true;
    }
}
