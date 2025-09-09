package com.jfeatures.msg.test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jfeatures.msg.codegen.ParameterMetadataExtractor;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.InsertMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.controller.CodeGenController;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Utility class for test data creation and mock setup
 */
public final class TestUtils {

    private TestUtils() {
        // prevents instantiation
    }

    /**
     * Creates a ColumnMetadata object with proper initialization for testing
     */
    public static ColumnMetadata createColumnMetadata(String columnName, String columnTypeName, int columnType, boolean isNullable) {
        ColumnMetadata metadata = new ColumnMetadata();
        metadata.setColumnName(columnName);
        metadata.setColumnTypeName(columnTypeName);
        metadata.setColumnType(columnType);
        metadata.setIsNullable(isNullable ? 1 : 0);
        metadata.setColumnClassName(getJavaTypeForSqlType(columnType));
        return metadata;
    }
    
    /**
     * Create ColumnMetadata with alias for testing
     */
    public static ColumnMetadata createColumnMetadataWithAlias(String columnName, String columnAlias, String columnTypeName, int columnType, boolean isNullable) {
        ColumnMetadata metadata = createColumnMetadata(columnName, columnTypeName, columnType, isNullable);
        metadata.setColumnAlias(columnAlias);
        return metadata;
    }
    
    /**
     * Maps SQL types to Java class names for test data
     */
    private static String getJavaTypeForSqlType(int sqlType) {
        return switch (sqlType) {
            case Types.INTEGER -> "java.lang.Integer";
            case Types.BIGINT -> "java.lang.Long";
            case Types.VARCHAR, Types.CHAR, Types.LONGVARCHAR -> "java.lang.String";
            case Types.DECIMAL, Types.NUMERIC -> "java.math.BigDecimal";
            case Types.DOUBLE, Types.FLOAT -> "java.lang.Double";
            case Types.BOOLEAN, Types.BIT -> "java.lang.Boolean";
            case Types.DATE -> "java.sql.Date";
            case Types.TIME -> "java.sql.Time";
            case Types.TIMESTAMP -> "java.sql.Timestamp";
            case Types.BINARY, Types.VARBINARY -> "byte[]";
            case Types.SMALLINT -> "java.lang.Short";
            case Types.TINYINT -> "java.lang.Byte";
            default -> "java.lang.Object";
        };
    }
    
    // Mock setup methods for integration tests
    
    public static void setupSelectWorkflowMocks(DatabaseConnection mockDatabaseConnection, JdbcTemplate mockJdbcTemplate) throws Exception {
        // Mock column metadata for SELECT
        List<ColumnMetadata> selectColumns = Arrays.asList(
            createColumnMetadata("customer_id", "INT", Types.INTEGER, false),
            createColumnMetadata("customer_name", "VARCHAR", Types.VARCHAR, true),
            createColumnMetadata("email", "VARCHAR", Types.VARCHAR, true),
            createColumnMetadata("phone", "VARCHAR", Types.VARCHAR, true),
            createColumnMetadata("address", "VARCHAR", Types.VARCHAR, true),
            createColumnMetadata("city", "VARCHAR", Types.VARCHAR, true),
            createColumnMetadata("country", "VARCHAR", Types.VARCHAR, true)
        );
        
        // Mock parameters for WHERE clause
        List<DBColumn> whereParameters = Arrays.asList(
            new DBColumn("table", "customerId", "INTEGER", "VARCHAR"),
            new DBColumn("table", "status", "VARCHAR", "VARCHAR"),
            new DBColumn("table", "createdDate", "TIMESTAMP", "VARCHAR")
        );
        
        // Setup mocks - These should be handled by the calling test methods
        // Static constructor mocking is complex and should be done in individual tests
    }
    
    public static void setupInsertWorkflowMocks(DatabaseConnection mockDatabaseConnection, DataSource mockDataSource, NamedParameterJdbcTemplate mockNamedParameterJdbcTemplate) throws Exception {
        // Mock insert metadata
        List<ColumnMetadata> insertColumns = Arrays.asList(
            createColumnMetadata("product_name", "VARCHAR", Types.VARCHAR, false),
            createColumnMetadata("description", "TEXT", Types.LONGVARCHAR, true),
            createColumnMetadata("price", "DECIMAL", Types.DECIMAL, false),
            createColumnMetadata("category_id", "INT", Types.INTEGER, false),
            createColumnMetadata("created_date", "TIMESTAMP", Types.TIMESTAMP, false)
        );
        
        InsertMetadata mockInsertMetadata = new InsertMetadata("products", insertColumns, "INSERT INTO products...");
        
        // Mock extractor should be setup by calling test method
    }
    
    public static void setupUpdateWorkflowMocks(DatabaseConnection mockDatabaseConnection, DataSource mockDataSource, NamedParameterJdbcTemplate mockNamedParameterJdbcTemplate) throws Exception {
        // Mock update metadata
        List<ColumnMetadata> setColumns = Arrays.asList(
            createColumnMetadata("order_status", "VARCHAR", Types.VARCHAR, false),
            createColumnMetadata("updated_date", "TIMESTAMP", Types.TIMESTAMP, false),
            createColumnMetadata("notes", "TEXT", Types.LONGVARCHAR, true)
        );
        
        List<ColumnMetadata> whereColumns = Arrays.asList(
            createColumnMetadata("order_id", "INT", Types.INTEGER, false),
            createColumnMetadata("customer_id", "INT", Types.INTEGER, false)
        );
        
        UpdateMetadata mockUpdateMetadata = new UpdateMetadata("orders", setColumns, whereColumns, "UPDATE orders...");
        
        // Mock extractor should be setup by calling test method
    }
    
    public static void setupDeleteWorkflowMocks(DatabaseConnection mockDatabaseConnection, DataSource mockDataSource) throws Exception {
        // Mock delete parameters to match the DELETE SQL with 3 parameters
        List<DBColumn> deleteParameters = Arrays.asList(
            new DBColumn("table", "orderId", "INTEGER", "VARCHAR"),
            new DBColumn("table", "productId", "INTEGER", "VARCHAR"),
            new DBColumn("table", "createdDate", "TIMESTAMP", "VARCHAR")
        );
        
        // Mock extractor should be setup by calling test method
    }
    
    public static void setupComplexSelectWorkflowMocks(DatabaseConnection mockDatabaseConnection, JdbcTemplate mockJdbcTemplate) throws Exception {
        // Mock complex query columns from multiple tables
        List<ColumnMetadata> complexColumns = Arrays.asList(
            createColumnMetadata("customer_id", "INT", Types.INTEGER, false),
            createColumnMetadata("customer_name", "VARCHAR", Types.VARCHAR, false),
            createColumnMetadata("email", "VARCHAR", Types.VARCHAR, true),
            createColumnMetadata("order_id", "INT", Types.INTEGER, false),
            createColumnMetadata("order_date", "DATE", Types.DATE, false),
            createColumnMetadata("total_amount", "DECIMAL", Types.DECIMAL, false),
            createColumnMetadata("item_id", "INT", Types.INTEGER, false),
            createColumnMetadata("product_name", "VARCHAR", Types.VARCHAR, false),
            createColumnMetadata("quantity", "INT", Types.INTEGER, false),
            createColumnMetadata("unit_price", "DECIMAL", Types.DECIMAL, false)
        );
        
        List<DBColumn> complexParameters = Arrays.asList(
            new DBColumn("table", "customerId", "INTEGER", "VARCHAR"),
            new DBColumn("table", "orderDate", "DATE", "VARCHAR"),
            new DBColumn("table", "orderStatus", "VARCHAR", "VARCHAR"),
            new DBColumn("table", "quantity", "INTEGER", "VARCHAR")
        );
        
        // Setup complex mocks - create mock objects directly instead of static constructor mocking
        CodeGenController mockController = mock(CodeGenController.class);
        when(mockController.selectColumnMetadata()).thenReturn(complexColumns);
        
        ParameterMetadataExtractor mockExtractor = mock(ParameterMetadataExtractor.class);
        when(mockExtractor.extractParameters(anyString())).thenReturn(complexParameters);
    }
    
    public static void setupMultipleDataTypesWorkflowMocks(DatabaseConnection mockDatabaseConnection, JdbcTemplate mockJdbcTemplate) throws Exception {
        // Mock various data types
        List<ColumnMetadata> dataTypeColumns = Arrays.asList(
            createColumnMetadata("id", "INT", Types.INTEGER, false),
            createColumnMetadata("name", "VARCHAR", Types.VARCHAR, false),
            createColumnMetadata("description", "TEXT", Types.LONGVARCHAR, true),
            createColumnMetadata("price", "DECIMAL", Types.DECIMAL, false),
            createColumnMetadata("quantity", "INT", Types.INTEGER, false),
            createColumnMetadata("is_active", "BIT", Types.BIT, false),
            createColumnMetadata("created_date", "DATE", Types.DATE, false),
            createColumnMetadata("updated_timestamp", "TIMESTAMP", Types.TIMESTAMP, true),
            createColumnMetadata("category_code", "CHAR", Types.CHAR, true),
            createColumnMetadata("discount_rate", "FLOAT", Types.FLOAT, true),
            createColumnMetadata("image_data", "BLOB", Types.BLOB, true)
        );
        
        List<DBColumn> dataTypeParameters = Arrays.asList(
            new DBColumn("table", "minPrice", "DECIMAL", "VARCHAR"),
            new DBColumn("table", "maxPrice", "DECIMAL", "VARCHAR"),
            new DBColumn("table", "createdDate", "DATE", "VARCHAR"),
            new DBColumn("table", "isActive", "BIT", "VARCHAR")
        );
        
        // Setup data type mocks should be done by calling test method
    }
    
    public static void setupCompleteWorkflowMocks(DatabaseConnection mockDatabaseConnection, JdbcTemplate mockJdbcTemplate, DataSource mockDataSource) throws Exception {
        // Simple integration test mocks
        List<ColumnMetadata> simpleColumns = Arrays.asList(
            createColumnMetadata("id", "INT", Types.INTEGER, false),
            createColumnMetadata("name", "VARCHAR", Types.VARCHAR, false)
        );
        
        List<DBColumn> simpleParameters = Arrays.asList(
            new DBColumn("table", "id", "INTEGER", "VARCHAR")
        );
        
        // Setup simple workflow mocks should be done by calling test method
    }
}