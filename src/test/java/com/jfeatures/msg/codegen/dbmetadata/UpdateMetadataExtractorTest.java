package com.jfeatures.msg.codegen.dbmetadata;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateMetadataExtractorTest {

    @Mock
    private DataSource dataSource;
    
    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;
    
    @Mock
    private Connection connection;
    
    @Mock
    private DatabaseMetaData databaseMetaData;
    
    @Mock
    private ResultSet columnsResultSet;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ParameterMetaData parameterMetaData;
    
    private UpdateMetadataExtractor extractor;
    
    @BeforeEach
    void setUp() throws SQLException {
        extractor = new UpdateMetadataExtractor(dataSource, jdbcTemplate);
        
        // Setup basic mocks
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.getParameterMetaData()).thenReturn(parameterMetaData);
    }
    
    @Test
    void testExtractUpdateMetadata_ValidUpdateStatement_ReturnsMetadata() throws Exception {
        // Given
        String sql = "UPDATE customers SET customer_name = ?, email = ? WHERE customer_id = ?";
        
        setupColumnMetadata();
        setupParameterMetadata(3, 2); // 3 total params, 2 SET params
        
        // When
        UpdateMetadata result = extractor.extractUpdateMetadata(sql);
        
        // Then
        assertNotNull(result);
        assertEquals("customers", result.tableName());
        assertEquals(sql, result.originalSql());
        
        assertNotNull(result.setColumns());
        assertEquals(2, result.setColumns().size());
        assertEquals("customer_name", result.setColumns().get(0).getColumnName());
        assertEquals("email", result.setColumns().get(1).getColumnName());
        
        assertNotNull(result.whereColumns());
        assertEquals(1, result.whereColumns().size());
        assertEquals("whereParam1", result.whereColumns().get(0).getColumnName());
        
        verify(databaseMetaData).getColumns(null, null, "customers", null);
        verify(parameterMetaData).getParameterCount();
    }
    
    @Test
    void testExtractUpdateMetadata_InvalidSqlStatement_ThrowsException() {
        // Given
        String invalidSql = "SELECT * FROM customers";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> extractor.extractUpdateMetadata(invalidSql)
        );
        
        assertEquals("SQL is not an UPDATE statement", exception.getMessage());
    }
    
    @Test
    void testExtractUpdateMetadata_UnparsableSql_ThrowsJSQLParserException() {
        // Given
        String malformedSql = "UPDATE customers SET name = WHERE id = ?";
        
        // When & Then
        assertThrows(
            JSQLParserException.class,
            () -> extractor.extractUpdateMetadata(malformedSql)
        );
    }
    
    @Test
    void testExtractUpdateMetadata_MultipleSetColumns_HandlesCorrectly() throws Exception {
        // Given
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ?, address = ? WHERE id = ? AND status = ?";
        
        setupColumnMetadata();
        setupParameterMetadata(6, 4); // 6 total params, 4 SET params
        
        // When
        UpdateMetadata result = extractor.extractUpdateMetadata(sql);
        
        // Then
        assertNotNull(result);
        assertEquals("customers", result.tableName());
        assertEquals(4, result.setColumns().size());
        assertEquals(2, result.whereColumns().size());
    }
    
    @Test
    void testExtractUpdateMetadata_NoWhereClause_HandlesCorrectly() throws Exception {
        // Given
        String sql = "UPDATE customers SET customer_name = ?, email = ?";
        
        setupColumnMetadata();
        setupParameterMetadata(2, 2); // 2 total params, 2 SET params
        
        // When
        UpdateMetadata result = extractor.extractUpdateMetadata(sql);
        
        // Then
        assertNotNull(result);
        assertEquals("customers", result.tableName());
        assertEquals(2, result.setColumns().size());
        assertEquals(0, result.whereColumns().size());
    }
    
    @Test
    void testExtractUpdateMetadata_DatabaseMetadataFails_UsesFallback() throws Exception {
        // Given
        String sql = "UPDATE customers SET name = ? WHERE id = ?";
        
        // Setup metadata to fail
        when(databaseMetaData.getColumns(null, null, "customers", null))
            .thenThrow(new SQLException("Database metadata failed"));
        
        setupParameterMetadata(2, 1);
        
        // When
        UpdateMetadata result = extractor.extractUpdateMetadata(sql);
        
        // Then
        assertNotNull(result);
        assertEquals("customers", result.tableName());
        assertEquals(1, result.setColumns().size());
        assertEquals("VARCHAR", result.setColumns().get(0).getColumnTypeName()); // Fallback type
        assertEquals(1, result.whereColumns().size());
    }
    
    @Test
    void testExtractUpdateMetadata_ParameterMetadataFails_UsesParsing() throws Exception {
        // Given
        String sql = "UPDATE customers SET name = ? WHERE id = ?";
        
        setupColumnMetadata();
        
        // Setup parameter metadata to fail
        when(preparedStatement.getParameterMetaData())
            .thenThrow(new SQLException("Parameter metadata not supported"));
        
        // When
        UpdateMetadata result = extractor.extractUpdateMetadata(sql);
        
        // Then
        assertNotNull(result);
        assertEquals("customers", result.tableName());
        assertEquals(1, result.setColumns().size());
        assertEquals(1, result.whereColumns().size());
        assertEquals("VARCHAR", result.whereColumns().get(0).getColumnTypeName()); // Fallback parsing
    }
    
    @Test
    void testExtractUpdateMetadata_ComplexUpdateWithJoin_HandlesCorrectly() throws Exception {
        // Given
        String sql = """
            UPDATE c 
            SET c.customer_name = ?, c.email = ? 
            FROM customers c 
            JOIN orders o ON c.customer_id = o.customer_id 
            WHERE o.order_date > ?
            """;
        
        setupColumnMetadata();
        setupParameterMetadata(3, 2);
        
        // When & Then
        // This should handle the complex SQL structure
        UpdateMetadata result = extractor.extractUpdateMetadata(sql);
        assertNotNull(result);
        assertEquals("c", result.tableName()); // Table alias
    }
    
    @Test
    void testExtractUpdateMetadata_UpdateWithSubquery_HandlesCorrectly() throws Exception {
        // Given
        String sql = """
            UPDATE customers 
            SET status = ? 
            WHERE customer_id IN (SELECT customer_id FROM orders WHERE order_date > ?)
            """;
        
        setupColumnMetadata();
        setupParameterMetadata(2, 1);
        
        // When
        UpdateMetadata result = extractor.extractUpdateMetadata(sql);
        
        // Then
        assertNotNull(result);
        assertEquals("customers", result.tableName());
        assertEquals(1, result.setColumns().size());
        assertEquals(1, result.whereColumns().size());
    }
    
    @Test
    void testExtractUpdateMetadata_CaseInsensitiveSql_HandlesCorrectly() throws Exception {
        // Given
        String sql = "update customers set customer_name = ?, email = ? where customer_id = ?";
        
        setupColumnMetadata();
        setupParameterMetadata(3, 2);
        
        // When
        UpdateMetadata result = extractor.extractUpdateMetadata(sql);
        
        // Then
        assertNotNull(result);
        assertEquals("customers", result.tableName());
        assertEquals(2, result.setColumns().size());
        assertEquals(1, result.whereColumns().size());
    }
    
    @Test
    void testExtractUpdateMetadata_NullableColumns_HandlesCorrectly() throws Exception {
        // Given
        String sql = "UPDATE customers SET name = ?, email = ? WHERE id = ?";
        
        // Setup nullable and non-nullable columns
        when(databaseMetaData.getColumns(null, null, "customers", null))
            .thenReturn(columnsResultSet);
        
        when(columnsResultSet.next())
            .thenReturn(true, true, true, false); // 3 columns, then end
        
        when(columnsResultSet.getString("COLUMN_NAME"))
            .thenReturn("name", "email", "id");
        when(columnsResultSet.getString("TYPE_NAME"))
            .thenReturn("VARCHAR", "VARCHAR", "INT");
        when(columnsResultSet.getInt("DATA_TYPE"))
            .thenReturn(Types.VARCHAR, Types.VARCHAR, Types.INTEGER);
        when(columnsResultSet.getInt("NULLABLE"))
            .thenReturn(1, 0, 0); // name nullable, email/id not nullable
        
        setupParameterMetadata(3, 2);
        
        // When
        UpdateMetadata result = extractor.extractUpdateMetadata(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.setColumns().size());
        assertEquals(1, result.setColumns().get(0).getIsNullable()); // name is nullable
        assertEquals(0, result.setColumns().get(1).getIsNullable()); // email is not nullable
    }
    
    private void setupColumnMetadata() throws SQLException {
        when(databaseMetaData.getColumns(null, null, "customers", null))
            .thenReturn(columnsResultSet);
        
        when(columnsResultSet.next())
            .thenReturn(true, true, true, true, false); // 4 columns, then end
        
        when(columnsResultSet.getString("COLUMN_NAME"))
            .thenReturn("customer_name", "email", "phone", "customer_id");
        when(columnsResultSet.getString("TYPE_NAME"))
            .thenReturn("VARCHAR", "VARCHAR", "VARCHAR", "INT");
        when(columnsResultSet.getInt("DATA_TYPE"))
            .thenReturn(Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER);
        when(columnsResultSet.getInt("NULLABLE"))
            .thenReturn(1, 1, 1, 0); // All nullable except ID
    }
    
    private void setupParameterMetadata(int totalParams, int setParams) throws SQLException {
        when(parameterMetaData.getParameterCount()).thenReturn(totalParams);
        
        for (int i = 1; i <= totalParams; i++) {
            if (i <= setParams) {
                when(parameterMetaData.getParameterTypeName(i)).thenReturn("VARCHAR");
                when(parameterMetaData.getParameterType(i)).thenReturn(Types.VARCHAR);
            } else {
                when(parameterMetaData.getParameterTypeName(i)).thenReturn("INT");
                when(parameterMetaData.getParameterType(i)).thenReturn(Types.INTEGER);
            }
            when(parameterMetaData.isNullable(i)).thenReturn(ParameterMetaData.parameterNullable);
        }
    }
}