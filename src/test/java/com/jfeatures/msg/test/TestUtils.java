package com.jfeatures.msg.test;

import com.jfeatures.msg.codegen.ParameterMetadataExtractor;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.InsertMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.controller.CodeGenController;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Utility class for test data creation and mock setup
 */
public class TestUtils {
    
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
            new DBColumn("customerId", "INTEGER", 1),
            new DBColumn("status", "VARCHAR", 2),
            new DBColumn("createdDate", "TIMESTAMP", 3)
        );
        
        // Setup mocks
        try (var codeGenMock = mockStatic(CodeGenController.class);
             var paramExtractorMock = mockStatic(ParameterMetadataExtractor.class)) {
            
            CodeGenController mockController = mock(CodeGenController.class);
            when(mockController.selectColumnMetadata()).thenReturn(selectColumns);
            codeGenMock.when(() -> new CodeGenController(any())).thenReturn(mockController);
            
            ParameterMetadataExtractor mockExtractor = mock(ParameterMetadataExtractor.class);
            when(mockExtractor.extractParameters(anyString())).thenReturn(whereParameters);
            paramExtractorMock.when(() -> new ParameterMetadataExtractor(any())).thenReturn(mockExtractor);
        }
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
        
        // Mock extractor would be setup here with static mocking
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
        
        // Mock extractor would be setup here with static mocking
    }
    
    public static void setupDeleteWorkflowMocks(DatabaseConnection mockDatabaseConnection, DataSource mockDataSource) throws Exception {
        // Mock delete parameters
        List<DBColumn> deleteParameters = Arrays.asList(
            new DBColumn("userId", "INTEGER", 1),
            new DBColumn("lastLogin", "TIMESTAMP", 2)
        );
        
        // Mock extractor would be setup here with static mocking
        try (var paramExtractorMock = mockStatic(ParameterMetadataExtractor.class)) {
            ParameterMetadataExtractor mockExtractor = mock(ParameterMetadataExtractor.class);
            when(mockExtractor.extractParameters(anyString())).thenReturn(deleteParameters);
            paramExtractorMock.when(() -> new ParameterMetadataExtractor(any())).thenReturn(mockExtractor);
        }
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
            new DBColumn("customerId", "INTEGER", 1),
            new DBColumn("orderDate", "DATE", 2),
            new DBColumn("orderStatus", "VARCHAR", 3),
            new DBColumn("quantity", "INTEGER", 4)
        );
        
        // Setup complex mocks
        try (var codeGenMock = mockStatic(CodeGenController.class);
             var paramExtractorMock = mockStatic(ParameterMetadataExtractor.class)) {
            
            CodeGenController mockController = mock(CodeGenController.class);
            when(mockController.selectColumnMetadata()).thenReturn(complexColumns);
            codeGenMock.when(() -> new CodeGenController(any())).thenReturn(mockController);
            
            ParameterMetadataExtractor mockExtractor = mock(ParameterMetadataExtractor.class);
            when(mockExtractor.extractParameters(anyString())).thenReturn(complexParameters);
            paramExtractorMock.when(() -> new ParameterMetadataExtractor(any())).thenReturn(mockExtractor);
        }
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
            new DBColumn("minPrice", "DECIMAL", 1),
            new DBColumn("maxPrice", "DECIMAL", 2),
            new DBColumn("createdDate", "DATE", 3),
            new DBColumn("isActive", "BIT", 4)
        );
        
        // Setup data type mocks
        try (var codeGenMock = mockStatic(CodeGenController.class);
             var paramExtractorMock = mockStatic(ParameterMetadataExtractor.class)) {
            
            CodeGenController mockController = mock(CodeGenController.class);
            when(mockController.selectColumnMetadata()).thenReturn(dataTypeColumns);
            codeGenMock.when(() -> new CodeGenController(any())).thenReturn(mockController);
            
            ParameterMetadataExtractor mockExtractor = mock(ParameterMetadataExtractor.class);
            when(mockExtractor.extractParameters(anyString())).thenReturn(dataTypeParameters);
            paramExtractorMock.when(() -> new ParameterMetadataExtractor(any())).thenReturn(mockExtractor);
        }
    }
    
    public static void setupCompleteWorkflowMocks(DatabaseConnection mockDatabaseConnection, JdbcTemplate mockJdbcTemplate, DataSource mockDataSource) throws Exception {
        // Simple integration test mocks
        List<ColumnMetadata> simpleColumns = Arrays.asList(
            createColumnMetadata("id", "INT", Types.INTEGER, false),
            createColumnMetadata("name", "VARCHAR", Types.VARCHAR, false)
        );
        
        List<DBColumn> simpleParameters = Arrays.asList(
            new DBColumn("id", "INTEGER", 1)
        );
        
        // Setup simple workflow mocks
        try (var codeGenMock = mockStatic(CodeGenController.class);
             var paramExtractorMock = mockStatic(ParameterMetadataExtractor.class)) {
            
            CodeGenController mockController = mock(CodeGenController.class);
            when(mockController.selectColumnMetadata()).thenReturn(simpleColumns);
            codeGenMock.when(() -> new CodeGenController(any())).thenReturn(mockController);
            
            ParameterMetadataExtractor mockExtractor = mock(ParameterMetadataExtractor.class);
            when(mockExtractor.extractParameters(anyString())).thenReturn(simpleParameters);
            paramExtractorMock.when(() -> new ParameterMetadataExtractor(any())).thenReturn(mockExtractor);
        }
    }
}