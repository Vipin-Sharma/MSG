package com.jfeatures.msg.codegen.dbmetadata;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts metadata from INSERT statements using database metadata approach.
 * Following Vipin's Principle: Single responsibility - metadata extraction only.
 */
@Slf4j
public class InsertMetadataExtractor {
    
    private final DataSource dataSource;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    
    public InsertMetadataExtractor(DataSource dataSource, NamedParameterJdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * Extracts metadata from INSERT statement using database metadata.
     * Single responsibility: Extract INSERT operation metadata.
     */
    public InsertMetadata extractInsertMetadata(String sql) throws JSQLParserException, SQLException {
        // Parse INSERT statement to get basic structure
        Statement statement = CCJSqlParserUtil.parse(sql);
        if (!(statement instanceof Insert insertStatement)) {
            throw new IllegalArgumentException("SQL is not an INSERT statement");
        }
        
        String tableName = insertStatement.getTable().getName();
        log.info("Extracting INSERT metadata for table: {}", tableName);
        
        // Get columns specified in INSERT statement
        List<Column> insertColumns = insertStatement.getColumns();
        if (insertColumns == null || insertColumns.isEmpty()) {
            throw new IllegalArgumentException("INSERT statement must specify column names");
        }
        
        // Extract metadata for each INSERT column using database metadata
        List<ColumnMetadata> columnMetadataList = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData dbMetadata = connection.getMetaData();
            
            for (Column column : insertColumns) {
                String columnName = column.getColumnName();
                ColumnMetadata columnMetadata = extractColumnMetadata(dbMetadata, tableName, columnName);
                if (columnMetadata != null) {
                    columnMetadataList.add(columnMetadata);
                    log.info("Extracted INSERT column metadata: {} ({})", columnName, columnMetadata.getColumnTypeName());
                } else {
                    log.warn("Could not extract metadata for INSERT column: {}", columnName);
                }
            }
        }
        
        log.info("Successfully extracted INSERT metadata for {} columns in table: {}", columnMetadataList.size(), tableName);
        
        return new InsertMetadata(tableName, columnMetadataList, sql);
    }
    
    private ColumnMetadata extractColumnMetadata(DatabaseMetaData dbMetadata, String tableName, String columnName) throws SQLException {
        try (ResultSet columns = dbMetadata.getColumns(null, null, tableName, columnName)) {
            if (columns.next()) {
                ColumnMetadata columnMetadata = new ColumnMetadata();
                columnMetadata.setColumnName(columns.getString("COLUMN_NAME"));
                columnMetadata.setColumnTypeName(columns.getString("TYPE_NAME"));
                columnMetadata.setColumnType(columns.getInt("DATA_TYPE"));
                columnMetadata.setIsNullable(columns.getInt("NULLABLE"));
                
                return columnMetadata;
            }
        } catch (SQLException e) {
            log.error("Error extracting metadata for column {} in table {}: {}", columnName, tableName, e.getMessage());
            throw e;
        }
        
        return null;
    }
}