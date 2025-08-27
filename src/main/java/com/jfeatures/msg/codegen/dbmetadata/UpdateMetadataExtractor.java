package com.jfeatures.msg.codegen.dbmetadata;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts metadata from UPDATE statements using database metadata approach.
 * Avoids complex SQL parsing by using database PreparedStatement metadata.
 */
@Slf4j
public class UpdateMetadataExtractor {
    
    private final DataSource dataSource;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    
    public UpdateMetadataExtractor(DataSource dataSource, NamedParameterJdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * Extracts metadata from UPDATE statement using database metadata.
     */
    public UpdateMetadata extractUpdateMetadata(String sql) throws JSQLParserException, SQLException {
        // Parse UPDATE statement to get basic structure
        Statement statement = CCJSqlParserUtil.parse(sql);
        if (!(statement instanceof Update updateStatement)) {
            throw new IllegalArgumentException("SQL is not an UPDATE statement");
        }
        
        String tableName = updateStatement.getTable().getName();
        
        // Extract SET columns from parsed statement
        List<ColumnMetadata> setColumns = extractSetColumns(updateStatement);
        
        // Extract WHERE columns using parameter metadata
        List<ColumnMetadata> whereColumns = extractWhereColumns(sql);
        
        return new UpdateMetadata(tableName, setColumns, whereColumns, sql);
    }
    
    /**
     * Extracts SET columns from the parsed UPDATE statement.
     */
    private List<ColumnMetadata> extractSetColumns(Update updateStatement) throws SQLException {
        List<ColumnMetadata> setColumns = new ArrayList<>();
        String tableName = updateStatement.getTable().getName();
        
        // Get table metadata to determine column types
        try (Connection connection = dataSource.getConnection()) {
            var dbMetaData = connection.getMetaData();
            var resultSet = dbMetaData.getColumns(null, null, tableName, null);
            
            // Build a map of column names to their metadata
            var columnTypeMap = new java.util.HashMap<String, ColumnMetadata>();
            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                String dataType = resultSet.getString("TYPE_NAME");
                int sqlType = resultSet.getInt("DATA_TYPE");
                boolean nullable = resultSet.getInt("NULLABLE") == 1;
                
                ColumnMetadata metadata = new ColumnMetadata();
                metadata.setColumnName(columnName);
                metadata.setColumnTypeName(dataType);
                metadata.setColumnType(sqlType);
                metadata.setIsNullable(nullable ? 1 : 0);
                columnTypeMap.put(columnName.toLowerCase(), metadata);
            }
            
            // Extract column names from SET expressions
            for (UpdateSet updateSet : updateStatement.getUpdateSets()) {
                for (Column column : updateSet.getColumns()) {
                    String columnName = column.getColumnName();
                    ColumnMetadata metadata = columnTypeMap.get(columnName.toLowerCase());
                    if (metadata != null) {
                        setColumns.add(metadata);
                    } else {
                        // Fallback: create basic metadata
                        ColumnMetadata fallback = new ColumnMetadata();
                        fallback.setColumnName(columnName);
                        fallback.setColumnTypeName("VARCHAR");
                        fallback.setColumnType(java.sql.Types.VARCHAR);
                        fallback.setIsNullable(1);
                        setColumns.add(fallback);
                        log.warn("Could not find metadata for SET column: {}", columnName);
                    }
                }
            }
        }
        
        return setColumns;
    }
    
    /**
     * Extracts WHERE columns using PreparedStatement parameter metadata.
     */
    private List<ColumnMetadata> extractWhereColumns(String sql) throws SQLException {
        List<ColumnMetadata> whereColumns = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // Count parameters in SET clause to skip them
            int setParameterCount = countSetParameters(sql);
            
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                var paramMetaData = ps.getParameterMetaData();
                int totalParams = paramMetaData.getParameterCount();
                
                // WHERE parameters come after SET parameters
                for (int i = setParameterCount + 1; i <= totalParams; i++) {
                    String paramTypeName = paramMetaData.getParameterTypeName(i);
                    int paramType = paramMetaData.getParameterType(i);
                    int nullable = paramMetaData.isNullable(i);
                    
                    // Generate parameter name based on position
                    String paramName = "whereParam" + (i - setParameterCount);
                    
                    ColumnMetadata paramMetadata = new ColumnMetadata();
                    paramMetadata.setColumnName(paramName);
                    paramMetadata.setColumnTypeName(paramTypeName);
                    paramMetadata.setColumnType(paramType);
                    paramMetadata.setIsNullable(nullable);
                    whereColumns.add(paramMetadata);
                }
            }
        } catch (SQLException e) {
            log.warn("Could not extract WHERE parameter metadata: {}", e.getMessage());
            // Fallback: try to extract WHERE columns by parsing
            whereColumns.addAll(extractWhereColumnsByParsing(sql));
        }
        
        return whereColumns;
    }
    
    /**
     * Counts the number of parameters in the SET clause.
     */
    private int countSetParameters(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Update updateStatement) {
                int count = 0;
                for (UpdateSet updateSet : updateStatement.getUpdateSets()) {
                    for (Expression expression : updateSet.getExpressions()) {
                        if (expression instanceof JdbcParameter) {
                            count++;
                        }
                    }
                }
                return count;
            }
        } catch (JSQLParserException e) {
            log.warn("Could not parse UPDATE statement to count SET parameters: {}", e.getMessage());
        }
        
        // Fallback: count '?' in SET clause
        String setClause = sql.substring(sql.toUpperCase().indexOf("SET"), 
                                       sql.toUpperCase().indexOf("WHERE"));
        return (int) setClause.chars().filter(ch -> ch == '?').count();
    }
    
    /**
     * Fallback method to extract WHERE columns by parsing the SQL.
     */
    private List<ColumnMetadata> extractWhereColumnsByParsing(String sql) {
        List<ColumnMetadata> whereColumns = new ArrayList<>();
        
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Update updateStatement && updateStatement.getWhere() != null) {
                updateStatement.getWhere().accept(new ExpressionVisitorAdapter() {
                    @Override
                    protected void visitBinaryExpression(BinaryExpression expr) {
                        if (expr instanceof ComparisonOperator) {
                            if (expr.getLeftExpression() instanceof Column column) {
                                // Create basic metadata for WHERE column
                                ColumnMetadata columnMetadata = new ColumnMetadata();
                                columnMetadata.setColumnName(column.getColumnName());
                                columnMetadata.setColumnTypeName("VARCHAR");
                                columnMetadata.setColumnType(java.sql.Types.VARCHAR);
                                columnMetadata.setIsNullable(1);
                                whereColumns.add(columnMetadata);
                            }
                        }
                        super.visitBinaryExpression(expr);
                    }
                });
            }
        } catch (JSQLParserException e) {
            log.warn("Could not parse WHERE clause: {}", e.getMessage());
        }
        
        return whereColumns;
    }
}