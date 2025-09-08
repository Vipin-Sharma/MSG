package com.jfeatures.msg.codegen.dbmetadata;

import com.jfeatures.msg.codegen.ParameterMetadataExtractor;
import com.jfeatures.msg.codegen.domain.DBColumn;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Extracts metadata from DELETE statements using database metadata approach.
 * Following Vipin's Principle: Single responsibility - metadata extraction only.
 */
@Slf4j
public class DeleteMetadataExtractor {
    
    private final DataSource dataSource;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    
    public DeleteMetadataExtractor(DataSource dataSource, NamedParameterJdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * Extracts metadata from DELETE statement using database metadata.
     * Single responsibility: Extract DELETE operation metadata.
     */
    public DeleteMetadata extractDeleteMetadata(String sql) throws JSQLParserException, SQLException {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL is not a DELETE statement");
        }
        // Parse with JSQLParser 5.x
        Statement statement = CCJSqlParserUtil.parse(sql);
        if (!(statement instanceof Delete deleteStatement)) {
            throw new IllegalArgumentException("SQL is not a DELETE statement");
        }
        String tableName = deleteStatement.getTable().getName();
        log.info("Extracting DELETE metadata for table: {}", tableName);
        
        // Extract WHERE clause parameters using existing parameter metadata extractor
        ParameterMetadataExtractor parameterExtractor = new ParameterMetadataExtractor(dataSource);
        List<DBColumn> whereParameters = parameterExtractor.extractParameters(sql);
        
        // Convert DBColumn to ColumnMetadata for WHERE clause
        List<ColumnMetadata> whereColumnMetadata = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData dbMetadata = connection.getMetaData();
            
            for (DBColumn whereParam : whereParameters) {
                // Extract column name from the parameter (remove table prefix if present)
                String columnName = extractColumnNameFromParameter(whereParam.columnName());
                ColumnMetadata columnMetadata = extractColumnMetadata(dbMetadata, tableName, columnName);
                if (columnMetadata != null) {
                    whereColumnMetadata.add(columnMetadata);
                    log.info("Extracted DELETE WHERE column metadata: {} ({})", columnName, columnMetadata.getColumnTypeName());
                } else {
                    log.warn("Could not extract metadata for DELETE WHERE column: {}", columnName);
                }
            }
        }
        
        log.info("Successfully extracted DELETE metadata for {} WHERE columns in table: {}", whereColumnMetadata.size(), tableName);
        
        return new DeleteMetadata(tableName, whereColumnMetadata, sql);
    }
    
    private String extractColumnNameFromParameter(String parameterName) {
        // Handle cases like "cus.customer_id" -> "customer_id"
        if (parameterName.contains(".")) {
            String[] parts = parameterName.split("\\.");
            return parts[parts.length - 1]; // Get the last part (column name)
        }
        
        // Handle camelCase to snake_case conversion (e.g., "customerId" -> "customer_id")
        String columnName = parameterName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        return columnName;
    }
    
    private ColumnMetadata extractColumnMetadata(DatabaseMetaData dbMetadata, String tableName, String columnName) throws SQLException {
        // Try exact match first
        try (ResultSet columns = dbMetadata.getColumns(null, null, tableName, columnName)) {
            if (columns.next()) {
                return createColumnMetadataFromResultSet(columns);
            }
        } catch (SQLException e) {
            log.debug("Exact column name match failed for {}: {}", columnName, e.getMessage());
        }
        
        // Try case-insensitive match by getting all columns and comparing
        try (ResultSet allColumns = dbMetadata.getColumns(null, null, tableName, "%")) {
            while (allColumns.next()) {
                String actualColumnName = allColumns.getString("COLUMN_NAME");
                if (actualColumnName.equalsIgnoreCase(columnName)) {
                    return createColumnMetadataFromResultSet(allColumns);
                }
            }
        } catch (SQLException e) {
            log.error("Error extracting metadata for column {} in table {}: {}", columnName, tableName, e.getMessage());
            throw e;
        }
        
        return null;
    }
    
    // JSQLParser 5.x is used; no manual identifier parsing required.
    
    private ColumnMetadata createColumnMetadataFromResultSet(ResultSet columns) throws SQLException {
        ColumnMetadata columnMetadata = new ColumnMetadata();
        columnMetadata.setColumnName(columns.getString("COLUMN_NAME"));
        columnMetadata.setColumnTypeName(columns.getString("TYPE_NAME"));
        columnMetadata.setColumnType(columns.getInt("DATA_TYPE"));
        columnMetadata.setIsNullable(columns.getInt("NULLABLE"));
        return columnMetadata;
    }
}
