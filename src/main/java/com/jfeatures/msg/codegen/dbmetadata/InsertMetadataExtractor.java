package com.jfeatures.msg.codegen.dbmetadata;

import com.jfeatures.msg.codegen.audit.AuditFieldDetector;
import com.jfeatures.msg.codegen.audit.AuditFieldType;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;

/**
 * Extracts metadata from INSERT statements using database metadata approach.
 * Following Vipin's Principle: Single responsibility - metadata extraction only.
 */
@Slf4j
public class InsertMetadataExtractor {
    
    private final DataSource dataSource;

    public InsertMetadataExtractor(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * Extracts metadata from INSERT statement using database metadata.
     * Single responsibility: Extract INSERT operation metadata.
     */
    public InsertMetadata extractInsertMetadata(String sql) throws JSQLParserException, SQLException {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL is not an INSERT statement");
        }
        // Parse with JSQLParser (5.x)
        Statement statement = CCJSqlParserUtil.parse(sql);
        if (!(statement instanceof Insert insertStatement)) {
            throw new IllegalArgumentException("SQL is not an INSERT statement");
        }

        String tableName = insertStatement.getTable().getName();
        log.info("Extracting INSERT metadata for table: {}", tableName);
        
        // Columns are now wrapped in ExpressionList<Column> in 5.x
        ExpressionList<Column> columnExprList = insertStatement.getColumns();
        List<Column> insertColumns = (columnExprList != null) ? columnExprList.stream().toList() : null;
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
                    // FAIL FAST: Column in SQL doesn't exist in database
                    String errorMsg = String.format(
                        "Column '%s' specified in INSERT statement does not exist in table '%s'. " +
                        "Please ensure the database schema matches the SQL file. " +
                        "If this is an audit field (created_at, created_by, etc.), verify the table has been created with audit columns. " +
                        "Check your DDL scripts in src/main/resources/",
                        columnName, tableName
                    );
                    log.error(errorMsg);
                    throw new IllegalStateException(errorMsg);
                }
            }
        }
        
        log.info("Successfully extracted INSERT metadata for {} columns in table: {}", columnMetadataList.size(), tableName);

        // Detect audit fields
        Map<ColumnMetadata, AuditFieldType> auditFields = AuditFieldDetector.detectAuditFields(columnMetadataList);
        if (!auditFields.isEmpty()) {
            log.info("Detected {} audit field(s) in INSERT statement", auditFields.size());
        }

        return new InsertMetadata(tableName, columnMetadataList, sql, auditFields);
    }
    
    // JSQLParser 5.x is used; no custom string parsing helpers needed here.

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
