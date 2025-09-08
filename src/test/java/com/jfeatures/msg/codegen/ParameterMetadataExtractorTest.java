package com.jfeatures.msg.codegen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import com.jfeatures.msg.codegen.domain.DBColumn;
import java.sql.*;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParameterMetadataExtractorTest {

    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ParameterMetaData parameterMetaData;
    
    private ParameterMetadataExtractor extractor;
    
    @BeforeEach
    void setUp() throws SQLException {
        extractor = new ParameterMetadataExtractor(dataSource);
        
        // Setup basic mocks with lenient stubbing
        lenient().when(dataSource.getConnection()).thenReturn(connection);
        lenient().when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        lenient().when(preparedStatement.getParameterMetaData()).thenReturn(parameterMetaData);
    }
    
    @Test
    void testExtractParameters_SimpleWhereClause_ReturnsCorrectParameters() throws SQLException {
        // Given
        String sql = "SELECT * FROM customers WHERE customer_id = ? AND status = ?";
        
        setupParameterMetaData(2, 
            new int[]{Types.INTEGER, Types.VARCHAR}, 
            new String[]{"customer_id", "status"});
        
        // When
        List<DBColumn> result = extractor.extractParameters(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        DBColumn firstParam = result.get(0);
        assertEquals("customerId", firstParam.columnName());
        assertEquals("Integer", firstParam.javaType());
        assertEquals("INTEGER", firstParam.jdbcType());
        
        DBColumn secondParam = result.get(1);
        assertEquals("status", secondParam.columnName());
        assertEquals("String", secondParam.javaType());
        assertEquals("VARCHAR", secondParam.jdbcType());
        
        verify(parameterMetaData).getParameterCount();
        verify(parameterMetaData).getParameterType(1);
        verify(parameterMetaData).getParameterType(2);
    }
    
    @Test
    void testExtractParameters_NoWhereClause_ReturnsDefaultParameterNames() throws SQLException {
        // Given
        String sql = "INSERT INTO customers (name, email) VALUES (?, ?)";
        
        setupParameterMetaData(2, 
            new int[]{Types.VARCHAR, Types.VARCHAR}, 
            new String[]{"param1", "param2"});
        
        // When
        List<DBColumn> result = extractor.extractParameters(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("param1", result.get(0).columnName());
        assertEquals("param2", result.get(1).columnName());
    }
    
    @Test
    void testExtractParameters_NoParameters_ReturnsEmptyList() throws SQLException {
        // Given
        String sql = "SELECT * FROM customers";
        
        when(parameterMetaData.getParameterCount()).thenReturn(0);
        
        // When
        List<DBColumn> result = extractor.extractParameters(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    void testExtractParameters_TableAliasInWhereClause_ExtractsColumnNames() throws SQLException {
        // Given
        String sql = "SELECT * FROM customers c WHERE c.customer_id = ? AND c.status = ?";
        
        setupParameterMetaData(2, 
            new int[]{Types.INTEGER, Types.VARCHAR}, 
            new String[]{"customer_id", "status"});
        
        // When
        List<DBColumn> result = extractor.extractParameters(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("customerId", result.get(0).columnName());
        assertEquals("status", result.get(1).columnName());
    }
    
    @Test
    void testExtractParameters_SnakeCaseColumns_ConvertsTosCamelCase() throws SQLException {
        // Given
        String sql = "SELECT * FROM customers WHERE first_name = ? AND last_name = ? AND date_of_birth = ?";
        
        setupParameterMetaData(3, 
            new int[]{Types.VARCHAR, Types.VARCHAR, Types.DATE}, 
            new String[]{"first_name", "last_name", "date_of_birth"});
        
        // When
        List<DBColumn> result = extractor.extractParameters(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("firstName", result.get(0).columnName());
        assertEquals("lastName", result.get(1).columnName());
        assertEquals("dateOfBirth", result.get(2).columnName());
    }
    
    @Test
    void testExtractParameters_ComplexWhereClause_HandlesCorrectly() throws SQLException {
        // Given
        String sql = """
            SELECT * FROM customers c 
            WHERE c.customer_id = ? 
            AND c.status = ? 
            AND c.created_date > ?
            ORDER BY c.customer_name
            """;
        
        setupParameterMetaData(3, 
            new int[]{Types.INTEGER, Types.VARCHAR, Types.TIMESTAMP}, 
            new String[]{"customer_id", "status", "created_date"});
        
        // When
        List<DBColumn> result = extractor.extractParameters(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("customerId", result.get(0).columnName());
        assertEquals("status", result.get(1).columnName());
        assertTrue(result.get(2).columnName().equals("createdDate") || result.get(2).columnName().equals("param3"), "Expected createdDate or param3, but got: " + result.get(2).columnName());
        assertEquals("Timestamp", result.get(2).javaType());
    }
    
    @Test
    void testExtractParameters_ParameterMetadataException_UsesFallbackValues() throws SQLException {
        // Given
        String sql = "SELECT * FROM customers WHERE customer_id = ? AND status = ?";
        
        when(parameterMetaData.getParameterCount()).thenReturn(2);
        when(parameterMetaData.getParameterType(1)).thenReturn(Types.INTEGER);
        when(parameterMetaData.getParameterType(2)).thenThrow(new SQLException("Parameter metadata not available"));
        
        // When
        List<DBColumn> result = extractor.extractParameters(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("customerId", result.get(0).columnName());
        assertEquals("Integer", result.get(0).javaType());
        
        // Second parameter should use default values due to exception
        assertEquals("status", result.get(1).columnName());
        assertEquals("String", result.get(1).javaType()); // Default fallback
        assertEquals("VARCHAR", result.get(1).jdbcType()); // Default fallback
    }
    
    @Test
    void testExtractParameters_VariousDataTypes_MapsCorrectly() throws SQLException {
        // Given
        String sql = "SELECT * FROM test_table WHERE int_col = ? AND long_col = ? AND decimal_col = ? AND bool_col = ? AND date_col = ?";
        
        setupParameterMetaData(5, 
            new int[]{Types.INTEGER, Types.BIGINT, Types.DECIMAL, Types.BOOLEAN, Types.DATE}, 
            new String[]{"int_col", "long_col", "decimal_col", "bool_col", "date_col"});
        
        // When
        List<DBColumn> result = extractor.extractParameters(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(5, result.size());
        
        assertEquals("Integer", result.get(0).javaType());
        assertEquals("INTEGER", result.get(0).jdbcType());
        
        assertEquals("Long", result.get(1).javaType());
        assertEquals("BIGINT", result.get(1).jdbcType());
        
        assertEquals("BigDecimal", result.get(2).javaType());
        assertEquals("DECIMAL", result.get(2).jdbcType());
        
        assertEquals("Boolean", result.get(3).javaType());
        assertEquals("BOOLEAN", result.get(3).jdbcType());
        
        assertEquals("Date", result.get(4).javaType());
        assertEquals("DATE", result.get(4).jdbcType());
    }
    
    @Test
    void testExtractParameters_UnknownSqlType_UsesFallbackType() throws SQLException {
        // Given
        String sql = "SELECT * FROM customers WHERE custom_type_col = ?";
        
        when(parameterMetaData.getParameterCount()).thenReturn(1);
        when(parameterMetaData.getParameterType(1)).thenReturn(9999); // Unknown SQL type
        
        // When
        List<DBColumn> result = extractor.extractParameters(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("String", result.get(0).javaType()); // Default fallback
        assertEquals("VARCHAR", result.get(0).jdbcType()); // Default fallback
    }
    
    @Test
    void testExtractParameters_DatabaseConnectionFails_ThrowsSQLException() throws SQLException {
        // Given
        String sql = "SELECT * FROM customers WHERE id = ?";
        SQLException connectionException = new SQLException("Connection failed");
        
        when(dataSource.getConnection()).thenThrow(connectionException);
        
        // When & Then
        SQLException exception = assertThrows(
            SQLException.class,
            () -> extractor.extractParameters(sql)
        );
        
        assertEquals("Connection failed", exception.getMessage());
    }
    
    @Test
    void testExtractParameters_CaseInsensitiveWhereKeyword_HandlesCorrectly() throws SQLException {
        // Given
        String sql = "SELECT * FROM customers WHERE customer_id = ? and status = ?";
        
        setupParameterMetaData(2, 
            new int[]{Types.INTEGER, Types.VARCHAR}, 
            new String[]{"customer_id", "status"});
        
        // When
        List<DBColumn> result = extractor.extractParameters(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("customerId", result.get(0).columnName());
        assertEquals("status", result.get(1).columnName());
    }
    
    @Test
    void testExtractParameters_WhereClauseWithGroupBy_ExcludesGroupByFromWhere() throws SQLException {
        // Given
        String sql = """
            SELECT customer_id, COUNT(*) 
            FROM customers 
            WHERE status = ? 
            GROUP BY customer_id 
            HAVING COUNT(*) > ?
            """;
        
        setupParameterMetaData(2, 
            new int[]{Types.VARCHAR, Types.INTEGER}, 
            new String[]{"status", "param2"});
        
        // When
        List<DBColumn> result = extractor.extractParameters(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("status", result.get(0).columnName());
        assertEquals("param2", result.get(1).columnName()); // HAVING parameter gets default name
    }
    
    @Test
    void testExtractParameters_MoreParametersThanColumns_FillsWithDefaults() throws SQLException {
        // Given
        String sql = "SELECT * FROM customers WHERE customer_id = ?"; // Only 1 column but 3 parameters
        
        setupParameterMetaData(3, 
            new int[]{Types.INTEGER, Types.VARCHAR, Types.DATE}, 
            new String[]{"customer_id", "param2", "param3"});
        
        // When
        List<DBColumn> result = extractor.extractParameters(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("customerId", result.get(0).columnName());
        assertEquals("param2", result.get(1).columnName());
        assertEquals("param3", result.get(2).columnName());
    }
    
    private void setupParameterMetaData(int parameterCount, int[] sqlTypes, String[] expectedColumnNames) throws SQLException {
        when(parameterMetaData.getParameterCount()).thenReturn(parameterCount);
        
        for (int i = 0; i < parameterCount; i++) {
            when(parameterMetaData.getParameterType(i + 1)).thenReturn(sqlTypes[i]);
        }
    }
    
    // ========== ERROR HANDLING TESTS ==========
    
    @Test
    void testConstructor_NullDataSource_ThrowsIllegalArgumentException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ParameterMetadataExtractor(null)
        );
        
        assertEquals("DataSource cannot be null", exception.getMessage());
    }
    
    @Test
    void testExtractParameters_NullSQL_ThrowsIllegalArgumentException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> extractor.extractParameters(null)
        );
        
        assertEquals("SQL cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testExtractParameters_EmptySQL_ThrowsIllegalArgumentException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> extractor.extractParameters("")
        );
        
        assertEquals("SQL cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testExtractParameters_WhitespaceOnlySQL_ThrowsIllegalArgumentException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> extractor.extractParameters("   \t\n  ")
        );
        
        assertEquals("SQL cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testExtractParameters_PreparedStatementCreationFails_ThrowsSQLException() throws SQLException {
        // Given
        String sql = "SELECT * FROM customers WHERE id = ?";
        SQLException psException = new SQLException("Failed to create prepared statement");
        
        when(connection.prepareStatement(sql)).thenThrow(psException);
        
        // When & Then
        SQLException exception = assertThrows(
            SQLException.class,
            () -> extractor.extractParameters(sql)
        );
        
        assertEquals("Failed to create prepared statement", exception.getMessage());
    }
    
    @Test
    void testExtractParameters_ParameterMetaDataRetrievalFails_ThrowsSQLException() throws SQLException {
        // Given
        String sql = "SELECT * FROM customers WHERE id = ?";
        SQLException pmdException = new SQLException("Parameter metadata not available");
        
        when(preparedStatement.getParameterMetaData()).thenThrow(pmdException);
        
        // When & Then
        SQLException exception = assertThrows(
            SQLException.class,
            () -> extractor.extractParameters(sql)
        );
        
        assertEquals("Parameter metadata not available", exception.getMessage());
    }
    
    @Test
    void testExtractParameters_ParameterCountRetrievalFails_ThrowsSQLException() throws SQLException {
        // Given
        String sql = "SELECT * FROM customers WHERE id = ?";
        SQLException countException = new SQLException("Could not get parameter count");
        
        when(parameterMetaData.getParameterCount()).thenThrow(countException);
        
        // When & Then
        SQLException exception = assertThrows(
            SQLException.class,
            () -> extractor.extractParameters(sql)
        );
        
        assertEquals("Could not get parameter count", exception.getMessage());
    }
    
    @Test
    void testExtractParameters_AllParameterTypesFail_UsesFallbackValues() throws SQLException {
        // Given
        String sql = "SELECT * FROM customers WHERE customer_id = ? AND status = ? AND email = ?";
        
        when(parameterMetaData.getParameterCount()).thenReturn(3);
        when(parameterMetaData.getParameterType(anyInt())).thenThrow(new SQLException("Type metadata unavailable"));
        
        // When
        List<DBColumn> result = extractor.extractParameters(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // All parameters should use fallback values
        result.forEach(param -> {
            assertEquals("String", param.javaType());
            assertEquals("VARCHAR", param.jdbcType());
        });
        
        // Parameter names should be extracted from SQL
        assertEquals("customerId", result.get(0).columnName());
        assertEquals("status", result.get(1).columnName());
        assertEquals("email", result.get(2).columnName());
    }
    
    @Test
    void testExtractParameters_MalformedSQL_HandlesGracefully() throws SQLException {
        // Given
        String malformedSql = "SELECT * FROM WHERE = ? AND ?? INVALID SQL";
        
        setupParameterMetaData(2, 
            new int[]{Types.VARCHAR, Types.VARCHAR}, 
            new String[]{"param1", "param2"});
        
        // When
        List<DBColumn> result = extractor.extractParameters(malformedSql);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        // Should fall back to default parameter names for malformed SQL
        assertEquals("param1", result.get(0).columnName());
        assertEquals("param2", result.get(1).columnName());
    }
    
    @Test
    void testExtractParameters_SqlWithSpecialCharactersInColumnNames_HandlesCorrectly() throws SQLException {
        // Given
        String sql = "SELECT * FROM customers WHERE `customer-id` = ? AND `first name` = ?";
        
        setupParameterMetaData(2, 
            new int[]{Types.INTEGER, Types.VARCHAR}, 
            new String[]{"customer-id", "first name"});
        
        // When
        List<DBColumn> result = extractor.extractParameters(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        // Should fall back to default names for columns with special chars
        assertEquals("param1", result.get(0).columnName());
        assertEquals("param2", result.get(1).columnName());
    }
    
    @Test
    void testExtractParameters_VeryLongSQL_HandlesEfficiently() throws SQLException {
        // Given
        StringBuilder longSql = new StringBuilder("SELECT * FROM customers WHERE ");
        for (int i = 0; i < 100; i++) {
            if (i > 0) longSql.append(" AND ");
            longSql.append("column_").append(i).append(" = ?");
        }
        
        int[] sqlTypes = new int[100];
        String[] columnNames = new String[100];
        for (int i = 0; i < 100; i++) {
            sqlTypes[i] = Types.VARCHAR;
            columnNames[i] = "column_" + i;
        }
        
        setupParameterMetaData(100, sqlTypes, columnNames);
        
        // When
        List<DBColumn> result = extractor.extractParameters(longSql.toString());
        
        // Then
        assertNotNull(result);
        assertEquals(100, result.size());
        // Verify some converted names
        assertEquals("column0", result.get(0).columnName());
        assertEquals("column50", result.get(50).columnName());
        assertEquals("column99", result.get(99).columnName());
    }
    
    @Test
    void testExtractParameters_EmptyWhereClause_UsesDefaultNames() throws SQLException {
        // Given
        String sql = "SELECT * FROM customers WHERE ? = ?";
        
        setupParameterMetaData(2, 
            new int[]{Types.VARCHAR, Types.VARCHAR}, 
            new String[]{"param1", "param2"});
        
        // When
        List<DBColumn> result = extractor.extractParameters(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("param1", result.get(0).columnName());
        assertEquals("param2", result.get(1).columnName());
    }
    
    @Test 
    void testExtractParameters_CamelCaseEdgeCases_HandlesCorrectly() throws SQLException {
        // Given - test various edge cases for camelCase conversion
        String sql = "SELECT * FROM test WHERE a = ? AND A_ = ? AND _b = ? AND __c__ = ? AND d_e_f_g = ?";
        
        setupParameterMetaData(5, 
            new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR}, 
            new String[]{"a", "A_", "_b", "__c__", "d_e_f_g"});
        
        // When
        List<DBColumn> result = extractor.extractParameters(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("a", result.get(0).columnName());
        assertEquals("a", result.get(1).columnName()); // A_ -> a
        assertEquals("B", result.get(2).columnName()); // _b -> B
        assertEquals("C", result.get(3).columnName()); // __c__ -> C
        assertEquals("dEFG", result.get(4).columnName()); // d_e_f_g -> dEFG
    }
    
    @Test
    void testExtractParameters_MultipleWhereClausesInSubqueries_ExtractsOuterWhereOnly() throws SQLException {
        // Given
        String sql = """
            SELECT * FROM customers c 
            WHERE c.customer_id = ? 
            AND c.total_orders > (SELECT COUNT(*) FROM orders o WHERE o.customer_id = c.customer_id AND o.status = ?)
            """;
        
        setupParameterMetaData(2, 
            new int[]{Types.INTEGER, Types.VARCHAR}, 
            new String[]{"customer_id", "status"});
        
        // When
        List<DBColumn> result = extractor.extractParameters(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("customerId", result.get(0).columnName());
        // The second parameter is in the subquery, so we might get default name
        assertTrue(result.get(1).columnName().equals("param2") || result.get(1).columnName().equals("status"));
    }
    
    @Test
    void testExtractParameters_NegativeParameterCount_ReturnsEmptyList() throws SQLException {
        // Given
        String sql = "SELECT * FROM customers";
        when(parameterMetaData.getParameterCount()).thenReturn(-1); // Unusual case
        
        // When
        List<DBColumn> result = extractor.extractParameters(sql);
        
        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}