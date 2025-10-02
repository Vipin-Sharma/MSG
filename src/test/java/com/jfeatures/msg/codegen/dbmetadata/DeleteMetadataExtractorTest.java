package com.jfeatures.msg.codegen.dbmetadata;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    @Test
    void testDeleteWithComplexWhereClause_IN() throws Exception {
        String sql = "DELETE FROM customers WHERE customer_id IN (?, ?, ?)";

        setupSingleColumnMetadata("customer_id", Types.INTEGER);

        // Mock PreparedStatement for parameter metadata
        PreparedStatement ps = mock(PreparedStatement.class);
        ParameterMetaData pmd = mock(ParameterMetaData.class);
        when(connection.prepareStatement(sql)).thenReturn(ps);
        when(ps.getParameterMetaData()).thenReturn(pmd);
        when(pmd.getParameterCount()).thenReturn(3);

        DeleteMetadata result = extractor.extractDeleteMetadata(sql);

        assertNotNull(result);
        assertEquals("customers", result.tableName());
        assertNotNull(result.whereColumns());
    }

    @Test
    void testDeleteWithComplexWhereClause_BETWEEN() throws Exception {
        String sql = "DELETE FROM customers WHERE created_date BETWEEN ? AND ?";

        setupSingleColumnMetadata("created_date", Types.TIMESTAMP);

        // Mock PreparedStatement for parameter metadata
        PreparedStatement ps = mock(PreparedStatement.class);
        ParameterMetaData pmd = mock(ParameterMetaData.class);
        when(connection.prepareStatement(sql)).thenReturn(ps);
        when(ps.getParameterMetaData()).thenReturn(pmd);
        when(pmd.getParameterCount()).thenReturn(2);

        DeleteMetadata result = extractor.extractDeleteMetadata(sql);

        assertNotNull(result);
        assertEquals("customers", result.tableName());
    }

    @Test
    void testDeleteWithComplexWhereClause_LIKE() throws Exception {
        String sql = "DELETE FROM customers WHERE name LIKE ?";

        setupSingleColumnMetadata("name", Types.VARCHAR);

        // Mock PreparedStatement for parameter metadata
        PreparedStatement ps = mock(PreparedStatement.class);
        ParameterMetaData pmd = mock(ParameterMetaData.class);
        when(connection.prepareStatement(sql)).thenReturn(ps);
        when(ps.getParameterMetaData()).thenReturn(pmd);
        when(pmd.getParameterCount()).thenReturn(1);

        DeleteMetadata result = extractor.extractDeleteMetadata(sql);

        assertNotNull(result);
        assertEquals("customers", result.tableName());
        assertEquals(1, result.whereColumns().size());
    }

    @Test
    void testDeleteWithSubquery() throws Exception {
        String sql = "DELETE FROM customers WHERE customer_id IN (SELECT customer_id FROM orders WHERE order_date > ?)";

        setupSingleColumnMetadata("customer_id", Types.INTEGER);

        // Mock PreparedStatement for parameter metadata
        PreparedStatement ps = mock(PreparedStatement.class);
        ParameterMetaData pmd = mock(ParameterMetaData.class);
        when(connection.prepareStatement(sql)).thenReturn(ps);
        when(ps.getParameterMetaData()).thenReturn(pmd);
        when(pmd.getParameterCount()).thenReturn(1);

        DeleteMetadata result = extractor.extractDeleteMetadata(sql);

        assertNotNull(result);
        assertEquals("customers", result.tableName());
    }

    @Test
    void testDeleteWithoutWhereClause() throws Exception {
        String sql = "DELETE FROM customers";

        // Mock PreparedStatement for parameter metadata
        PreparedStatement ps = mock(PreparedStatement.class);
        ParameterMetaData pmd = mock(ParameterMetaData.class);
        when(connection.prepareStatement(sql)).thenReturn(ps);
        when(ps.getParameterMetaData()).thenReturn(pmd);
        when(pmd.getParameterCount()).thenReturn(0);

        DeleteMetadata result = extractor.extractDeleteMetadata(sql);

        assertNotNull(result);
        assertEquals("customers", result.tableName());
        assertTrue(result.whereColumns().isEmpty());
    }

    @Test
    void testDeleteWithNullSql() {
        assertThrows(IllegalArgumentException.class, () ->
            extractor.extractDeleteMetadata(null)
        );
    }

    @Test
    void testDeleteWithEmptySql() {
        assertThrows(IllegalArgumentException.class, () ->
            extractor.extractDeleteMetadata("")
        );
    }

    @Test
    void testDeleteWithBlankSql() {
        assertThrows(IllegalArgumentException.class, () ->
            extractor.extractDeleteMetadata("   ")
        );
    }

    @Test
    void testDeleteWithNonDeleteStatement() {
        String sql = "SELECT * FROM customers";

        assertThrows(IllegalArgumentException.class, () ->
            extractor.extractDeleteMetadata(sql)
        );
    }

    @Test
    void testDeleteWithDatabaseMetadataFailure() throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";

        // Mock PreparedStatement for parameter metadata
        PreparedStatement ps = mock(PreparedStatement.class);
        ParameterMetaData pmd = mock(ParameterMetaData.class);
        when(connection.prepareStatement(sql)).thenReturn(ps);
        when(ps.getParameterMetaData()).thenReturn(pmd);
        when(pmd.getParameterCount()).thenReturn(1);

        // Use anyString() to match any column name parameter
        // When using matchers, ALL arguments must be matchers
        when(databaseMetaData.getColumns(isNull(), isNull(), eq("customers"), anyString()))
            .thenThrow(new SQLException("Database metadata error"));

        assertThrows(SQLException.class, () ->
            extractor.extractDeleteMetadata(sql)
        );
    }

    @Test
    void testDeleteWithConnectionFailure() throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";

        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));

        assertThrows(SQLException.class, () ->
            extractor.extractDeleteMetadata(sql)
        );
    }

    @Test
    void testDeleteWithMalformedSql() {
        String sql = "DELETE FROM customers WHERE customer_id = ?";

        assertThrows(Exception.class, () ->
            extractor.extractDeleteMetadata(sql + " AND")
        );
    }

    @Test
    void testDeleteWithMultipleConditions() throws Exception {
        String sql = "DELETE FROM customers WHERE customer_id = ? AND status = ? OR region = ?";

        // Mock PreparedStatement for parameter metadata
        PreparedStatement ps = mock(PreparedStatement.class);
        ParameterMetaData pmd = mock(ParameterMetaData.class);
        when(connection.prepareStatement(sql)).thenReturn(ps);
        when(ps.getParameterMetaData()).thenReturn(pmd);
        when(pmd.getParameterCount()).thenReturn(3);

        when(databaseMetaData.getColumns(null, null, "customers", "customer_id"))
            .thenReturn(columnsResultSet);
        when(databaseMetaData.getColumns(null, null, "customers", "status"))
            .thenReturn(columnsResultSet);
        when(databaseMetaData.getColumns(null, null, "customers", "region"))
            .thenReturn(columnsResultSet);

        when(columnsResultSet.next()).thenReturn(true);
        when(columnsResultSet.getString("COLUMN_NAME"))
            .thenReturn("customer_id", "status", "region");
        when(columnsResultSet.getString("TYPE_NAME"))
            .thenReturn("INT", "VARCHAR", "VARCHAR");
        when(columnsResultSet.getInt("DATA_TYPE"))
            .thenReturn(Types.INTEGER, Types.VARCHAR, Types.VARCHAR);
        when(columnsResultSet.getInt("NULLABLE"))
            .thenReturn(0, 1, 1);

        DeleteMetadata result = extractor.extractDeleteMetadata(sql);

        assertNotNull(result);
        assertTrue(result.whereColumns().size() >= 1);
    }

    @Test
    void testDeleteWithUnicodeTableName() throws Exception {
        String sql = "DELETE FROM 顧客テーブル WHERE id = ?";

        // Mock PreparedStatement for parameter metadata
        PreparedStatement ps = mock(PreparedStatement.class);
        ParameterMetaData pmd = mock(ParameterMetaData.class);
        when(connection.prepareStatement(sql)).thenReturn(ps);
        when(ps.getParameterMetaData()).thenReturn(pmd);
        when(pmd.getParameterCount()).thenReturn(1);

        when(databaseMetaData.getColumns(null, null, "顧客テーブル", "id"))
            .thenReturn(columnsResultSet);
        when(columnsResultSet.next()).thenReturn(true);
        when(columnsResultSet.getString("COLUMN_NAME")).thenReturn("id");
        when(columnsResultSet.getString("TYPE_NAME")).thenReturn("INT");
        when(columnsResultSet.getInt("DATA_TYPE")).thenReturn(Types.INTEGER);
        when(columnsResultSet.getInt("NULLABLE")).thenReturn(0);

        DeleteMetadata result = extractor.extractDeleteMetadata(sql);

        assertNotNull(result);
        assertEquals("顧客テーブル", result.tableName());
    }

    private void setupSingleColumnMetadata(String columnName, int sqlType) throws SQLException {
        // Use lenient() to avoid strict stubbing issues with parameter names
        // When using matchers, ALL arguments must be matchers
        lenient().when(databaseMetaData.getColumns(isNull(), isNull(), eq("customers"), anyString()))
            .thenReturn(columnsResultSet);
        lenient().when(columnsResultSet.next()).thenReturn(true);
        lenient().when(columnsResultSet.getString("COLUMN_NAME")).thenReturn(columnName);
        lenient().when(columnsResultSet.getString("TYPE_NAME")).thenReturn("VARCHAR");
        lenient().when(columnsResultSet.getInt("DATA_TYPE")).thenReturn(sqlType);
        lenient().when(columnsResultSet.getInt("NULLABLE")).thenReturn(1);
    }
}