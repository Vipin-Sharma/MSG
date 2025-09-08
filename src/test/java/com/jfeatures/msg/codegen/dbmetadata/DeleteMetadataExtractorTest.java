package com.jfeatures.msg.codegen.dbmetadata;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jfeatures.msg.codegen.ParameterMetadataExtractor;
import com.jfeatures.msg.codegen.domain.DBColumn;
import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteMetadataExtractorTest {

    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private DatabaseMetaData databaseMetaData;
    
    @Mock
    private ResultSet columnsResultSet;
    
    @Mock
    private ResultSet allColumnsResultSet;
    
    private DeleteMetadataExtractor extractor;
    
    @BeforeEach
    void setUp() throws SQLException {
        extractor = new DeleteMetadataExtractor(dataSource);
        
        // Setup basic mocks with lenient stubbing
        lenient().when(dataSource.getConnection()).thenReturn(connection);
        lenient().when(connection.getMetaData()).thenReturn(databaseMetaData);
    }
    
    @Test
    void testExtractDeleteMetadata_ValidDeleteStatement_ReturnsMetadata() throws Exception {
        // Given
        String sql = "DELETE FROM customers WHERE customer_id = ? AND status = ?";
        List<DBColumn> mockParameters = Arrays.asList(
            new DBColumn("table", "customer_id", "java.lang.String", "INTEGER"),
            new DBColumn("table", "status", "java.lang.String", "VARCHAR")
        );
        
        try (var mockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
            when(mock.extractParameters(sql)).thenReturn(mockParameters);
        })) {
            
            setupColumnMetadata();
            
            // When
            DeleteMetadata result = extractor.extractDeleteMetadata(sql);
            
            // Then
            assertNotNull(result);
            assertEquals("customers", result.tableName());
            assertEquals(sql, result.originalSql());
            
            assertNotNull(result.whereColumns());
            assertEquals(2, result.whereColumns().size());
            assertEquals("customer_id", result.whereColumns().get(0).getColumnName());
            assertEquals("status", result.whereColumns().get(1).getColumnName());
            
            var constructedMocks = mockedConstruction.constructed();
            assertEquals(1, constructedMocks.size());
            verify(constructedMocks.get(0)).extractParameters(sql);
            verify(databaseMetaData, times(2)).getColumns(isNull(), isNull(), eq("customers"), anyString());
        }
    }
    
    @Test
    void testExtractDeleteMetadata_InvalidSqlStatement_ThrowsException() {
        // Given
        String invalidSql = "SELECT * FROM customers";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> extractor.extractDeleteMetadata(invalidSql)
        );
        
        assertEquals("SQL is not a DELETE statement", exception.getMessage());
    }
    
    @Test
    void testExtractDeleteMetadata_UnparsableSql_ThrowsJSQLParserException() {
        // Given
        String malformedSql = "DELETE FROM WHERE id = ?";
        
        // When & Then
        assertThrows(
            NullPointerException.class,
            () -> extractor.extractDeleteMetadata(malformedSql)
        );
    }
    
    @Test
    void testExtractDeleteMetadata_NoWhereClause_HandlesCorrectly() throws Exception {
        // Given
        String sql = "DELETE FROM customers";
        List<DBColumn> emptyParameters = Collections.emptyList();
        
        try (var mockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
            when(mock.extractParameters(sql)).thenReturn(emptyParameters);
        })) {
            
            // When
            DeleteMetadata result = extractor.extractDeleteMetadata(sql);
            
            // Then
            assertNotNull(result);
            assertEquals("customers", result.tableName());
            assertEquals(0, result.whereColumns().size());
            
            var constructedMocks = mockedConstruction.constructed();
            assertEquals(1, constructedMocks.size());
            verify(constructedMocks.get(0)).extractParameters(sql);
        }
    }
    
    @Test
    void testExtractDeleteMetadata_CamelCaseParameterNames_ConvertsToSnakeCase() throws Exception {
        // Given
        String sql = "DELETE FROM customers WHERE customerId = ? AND customerStatus = ?";
        List<DBColumn> mockParameters = Arrays.asList(
            new DBColumn("table", "customerId", "java.lang.String", "INTEGER"),
            new DBColumn("table", "customerStatus", "java.lang.String", "VARCHAR")
        );
        
        try (var mockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
            when(mock.extractParameters(sql)).thenReturn(mockParameters);
        })) {
            
            // Setup column metadata for snake_case names
            when(databaseMetaData.getColumns(null, null, "customers", "customer_id"))
                .thenReturn(columnsResultSet);
            when(databaseMetaData.getColumns(null, null, "customers", "customer_status"))
                .thenReturn(columnsResultSet);
            
            when(columnsResultSet.next()).thenReturn(true);
            when(columnsResultSet.getString("COLUMN_NAME"))
                .thenReturn("customer_id", "customer_status");
            when(columnsResultSet.getString("TYPE_NAME"))
                .thenReturn("INT", "VARCHAR");
            when(columnsResultSet.getInt("DATA_TYPE"))
                .thenReturn(Types.INTEGER, Types.VARCHAR);
            when(columnsResultSet.getInt("NULLABLE"))
                .thenReturn(0, 1);
            
            // When
            DeleteMetadata result = extractor.extractDeleteMetadata(sql);
            
            // Then
            assertNotNull(result);
            assertEquals(2, result.whereColumns().size());
            assertEquals("customer_id", result.whereColumns().get(0).getColumnName());
            assertEquals("customer_status", result.whereColumns().get(1).getColumnName());
        }
    }
    
    @Test
    void testExtractDeleteMetadata_TablePrefixedColumns_ExtractsColumnName() throws Exception {
        // Given
        String sql = "DELETE FROM customers WHERE c.customer_id = ? AND c.status = ?";
        List<DBColumn> mockParameters = Arrays.asList(
            new DBColumn("table", "c.customer_id", "java.lang.String", "INTEGER"),
            new DBColumn("table", "c.status", "java.lang.String", "VARCHAR")
        );
        
        try (var mockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
            when(mock.extractParameters(sql)).thenReturn(mockParameters);
        })) {
            
            setupColumnMetadata();
            
            // When
            DeleteMetadata result = extractor.extractDeleteMetadata(sql);
            
            // Then
            assertNotNull(result);
            assertEquals(2, result.whereColumns().size());
            assertEquals("customer_id", result.whereColumns().get(0).getColumnName());
            assertEquals("status", result.whereColumns().get(1).getColumnName());
            
            verify(databaseMetaData).getColumns(null, null, "customers", "customer_id");
            verify(databaseMetaData).getColumns(null, null, "customers", "status");
        }
    }
    
    @Test
    void testExtractDeleteMetadata_CaseInsensitiveColumnMatch_HandlesCorrectly() throws Exception {
        // Given
        String sql = "DELETE FROM customers WHERE CUSTOMER_ID = ?";
        List<DBColumn> mockParameters = Arrays.asList(
            new DBColumn("table", "CUSTOMER_ID", "java.lang.String", "INTEGER")
        );
        
        try (var mockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
            when(mock.extractParameters(sql)).thenReturn(mockParameters);
        })) {
            
            // Setup exact match to fail
            when(databaseMetaData.getColumns(null, null, "customers", "customer_id"))
                .thenReturn(columnsResultSet);
            when(columnsResultSet.next()).thenReturn(false); // Exact match fails
            
            // Setup case-insensitive match to succeed
            when(databaseMetaData.getColumns(null, null, "customers", "%"))
                .thenReturn(allColumnsResultSet);
            when(allColumnsResultSet.next()).thenReturn(true, false);
            when(allColumnsResultSet.getString("COLUMN_NAME")).thenReturn("customer_id");
            when(allColumnsResultSet.getString("TYPE_NAME")).thenReturn("INT");
            when(allColumnsResultSet.getInt("DATA_TYPE")).thenReturn(Types.INTEGER);
            when(allColumnsResultSet.getInt("NULLABLE")).thenReturn(0);
            
            // When
            DeleteMetadata result = extractor.extractDeleteMetadata(sql);
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.whereColumns().size());
            assertEquals("customer_id", result.whereColumns().get(0).getColumnName());
            
            verify(databaseMetaData).getColumns(null, null, "customers", "%");
        }
    }
    
    @Test
    void testExtractDeleteMetadata_ColumnNotFound_SkipsColumn() throws Exception {
        // Given
        String sql = "DELETE FROM customers WHERE nonexistent_column = ?";
        List<DBColumn> mockParameters = Arrays.asList(
            new DBColumn("table", "nonexistent_column", "java.lang.String", "VARCHAR")
        );
        
        try (var mockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
            when(mock.extractParameters(sql)).thenReturn(mockParameters);
        })) {
            
            // Setup both exact and wildcard matches to fail
            when(databaseMetaData.getColumns(null, null, "customers", "nonexistent_column"))
                .thenReturn(columnsResultSet);
            when(databaseMetaData.getColumns(null, null, "customers", "%"))
                .thenReturn(allColumnsResultSet);
            when(columnsResultSet.next()).thenReturn(false);
            when(allColumnsResultSet.next()).thenReturn(false);
            
            // When
            DeleteMetadata result = extractor.extractDeleteMetadata(sql);
            
            // Then
            assertNotNull(result);
            assertEquals("customers", result.tableName());
            assertEquals(0, result.whereColumns().size()); // Column not found, so skipped
        }
    }
    
    @Test
    void testExtractDeleteMetadata_DatabaseException_PropagatesException() throws Exception {
        // Given
        String sql = "DELETE FROM customers WHERE id = ?";
        List<DBColumn> mockParameters = Arrays.asList(
            new DBColumn("table", "id", "java.lang.String", "INTEGER")
        );
        
        try (var mockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
            when(mock.extractParameters(sql)).thenReturn(mockParameters);
        })) {
            when(databaseMetaData.getColumns(null, null, "customers", "id"))
                .thenThrow(new SQLException("Database connection failed"));
            when(databaseMetaData.getColumns(null, null, "customers", "%"))
                .thenThrow(new SQLException("Database connection failed"));
            
            // When & Then
            SQLException exception = assertThrows(
                SQLException.class,
                () -> extractor.extractDeleteMetadata(sql)
            );
            
            assertEquals("Database connection failed", exception.getMessage());
        }
    }
    
    @Test
    void testExtractDeleteMetadata_ComplexDeleteWithSubquery_HandlesCorrectly() throws Exception {
        // Given
        String sql = """
            DELETE FROM customers 
            WHERE customer_id IN (
                SELECT customer_id FROM orders WHERE order_date < ?
            )
            """;
        List<DBColumn> mockParameters = Arrays.asList(
            new DBColumn("table", "order_date", "java.lang.String", "TIMESTAMP")
        );
        
        try (var mockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
            when(mock.extractParameters(sql)).thenReturn(mockParameters);
        })) {
            
            when(databaseMetaData.getColumns(null, null, "customers", "order_date"))
                .thenReturn(columnsResultSet);
            when(columnsResultSet.next()).thenReturn(true);
            when(columnsResultSet.getString("COLUMN_NAME")).thenReturn("order_date");
            when(columnsResultSet.getString("TYPE_NAME")).thenReturn("TIMESTAMP");
            when(columnsResultSet.getInt("DATA_TYPE")).thenReturn(Types.TIMESTAMP);
            when(columnsResultSet.getInt("NULLABLE")).thenReturn(1);
            
            // When
            DeleteMetadata result = extractor.extractDeleteMetadata(sql);
            
            // Then
            assertNotNull(result);
            assertEquals("customers", result.tableName());
            assertEquals(1, result.whereColumns().size());
            assertEquals("order_date", result.whereColumns().get(0).getColumnName());
            assertEquals("TIMESTAMP", result.whereColumns().get(0).getColumnTypeName());
        }
    }
    
    private void setupColumnMetadata() throws SQLException {
        when(databaseMetaData.getColumns(null, null, "customers", "customer_id"))
            .thenReturn(columnsResultSet);
        when(databaseMetaData.getColumns(null, null, "customers", "status"))
            .thenReturn(columnsResultSet);
        
        when(columnsResultSet.next()).thenReturn(true);
        when(columnsResultSet.getString("COLUMN_NAME"))
            .thenReturn("customer_id", "status");
        when(columnsResultSet.getString("TYPE_NAME"))
            .thenReturn("INT", "VARCHAR");
        when(columnsResultSet.getInt("DATA_TYPE"))
            .thenReturn(Types.INTEGER, Types.VARCHAR);
        when(columnsResultSet.getInt("NULLABLE"))
            .thenReturn(0, 1);
    }
}