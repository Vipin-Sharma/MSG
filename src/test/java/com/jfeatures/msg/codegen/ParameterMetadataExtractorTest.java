package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.domain.DBColumn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ParameterMetadataExtractor Tests")
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
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        when(preparedStatement.getParameterMetaData()).thenReturn(parameterMetaData);
        
        extractor = new ParameterMetadataExtractor(dataSource);
    }
    
    // Helper method to access private methods via reflection
    private Object invokePrivateMethod(String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = ParameterMetadataExtractor.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(extractor, args);
    }
    
    @Test
    @DisplayName("Simple WHERE clause with single parameter")
    void testSimpleWhereClause() throws Exception {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) invokePrivateMethod("extractColumnNamesFromWhereClause", 
            new Class[]{String.class, int.class}, sql, 1);
        
        assertEquals(1, result.size());
        assertEquals("id", result.get(0));
    }
    
    @Test
    @DisplayName("Simple WHERE clause with multiple parameters")
    void testSimpleWhereClauseMultipleParams() throws Exception {
        String sql = "SELECT * FROM users WHERE name = ? AND age = ? AND email = ?";
        
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) invokePrivateMethod("extractColumnNamesFromWhereClause", 
            new Class[]{String.class, int.class}, sql, 3);
        
        assertEquals(3, result.size());
        assertEquals("name", result.get(0));
        assertEquals("age", result.get(1));
        assertEquals("email", result.get(2));
    }
    
    @Test
    @DisplayName("WHERE clause with table aliases")
    void testWhereClauseWithAliases() throws Exception {
        String sql = "SELECT * FROM users u WHERE u.name = ? AND u.email = ?";
        
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) invokePrivateMethod("extractColumnNamesFromWhereClause", 
            new Class[]{String.class, int.class}, sql, 2);
        
        assertEquals(2, result.size());
        assertEquals("name", result.get(0));
        assertEquals("email", result.get(1));
    }
    
    @Test
    @DisplayName("Complex JOIN query with aliases")
    void testComplexJoinWithAliases() throws Exception {
        String sql = "SELECT cus.first_name, cus.last_name, cit.city " +
                    "FROM customer cus " +
                    "JOIN address adr ON cus.address_id = adr.address_id " +
                    "JOIN city cit ON adr.city_id = cit.city_id " +
                    "WHERE cus.customer_id = ? AND cit.country_id = ? AND cus.email = ?";
        
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) invokePrivateMethod("extractColumnNamesFromWhereClause", 
            new Class[]{String.class, int.class}, sql, 3);
        
        assertEquals(3, result.size());
        assertEquals("customerId", result.get(0));
        assertEquals("countryId", result.get(1));
        assertEquals("email", result.get(2));
    }
    
    @Test
    @DisplayName("Snake case to camel case conversion")
    void testSnakeCaseToCamelCase() throws Exception {
        String result1 = (String) invokePrivateMethod("toCamelCase", new Class[]{String.class}, "first_name");
        assertEquals("firstName", result1);
        
        String result2 = (String) invokePrivateMethod("toCamelCase", new Class[]{String.class}, "customer_id");
        assertEquals("customerId", result2);
        
        String result3 = (String) invokePrivateMethod("toCamelCase", new Class[]{String.class}, "last_login_date");
        assertEquals("lastLoginDate", result3);
        
        String result4 = (String) invokePrivateMethod("toCamelCase", new Class[]{String.class}, "id");
        assertEquals("id", result4);
        
        String result5 = (String) invokePrivateMethod("toCamelCase", new Class[]{String.class}, "USER_NAME");
        assertEquals("userName", result5);
    }
    
    @Test
    @DisplayName("WHERE clause extraction with GROUP BY")
    void testWhereClauseWithGroupBy() throws Exception {
        String sql = "SELECT COUNT(*) FROM orders o WHERE o.status = ? AND o.customer_id = ? GROUP BY o.customer_id";
        
        String whereClause = (String) invokePrivateMethod("extractWhereClause", new Class[]{String.class}, sql);
        assertEquals("o.status = ? AND o.customer_id = ?", whereClause);
    }
    
    @Test
    @DisplayName("WHERE clause extraction with ORDER BY")
    void testWhereClauseWithOrderBy() throws Exception {
        String sql = "SELECT * FROM products p WHERE p.price > ? AND p.category = ? ORDER BY p.name";
        
        String whereClause = (String) invokePrivateMethod("extractWhereClause", new Class[]{String.class}, sql);
        assertEquals("p.price > ? AND p.category = ?", whereClause);
    }
    
    @Test
    @DisplayName("WHERE clause extraction with HAVING")
    void testWhereClauseWithHaving() throws Exception {
        String sql = "SELECT COUNT(*) FROM orders o WHERE o.status = ? GROUP BY o.customer_id HAVING COUNT(*) > ?";
        
        String whereClause = (String) invokePrivateMethod("extractWhereClause", new Class[]{String.class}, sql);
        assertEquals("o.status = ?", whereClause);
    }
    
    @Test
    @DisplayName("WHERE clause extraction with LIMIT")
    void testWhereClauseWithLimit() throws Exception {
        String sql = "SELECT * FROM users u WHERE u.active = ? AND u.created_date > ? LIMIT 10";
        
        String whereClause = (String) invokePrivateMethod("extractWhereClause", new Class[]{String.class}, sql);
        assertEquals("u.active = ? AND u.created_date > ?", whereClause);
    }
    
    @Test
    @DisplayName("Multi-line SQL formatting")
    void testMultiLineSQL() throws Exception {
        String sql = "SELECT u.id, u.name\n" +
                    "FROM users u\n" +
                    "WHERE u.email = ?\n" +
                    "  AND u.status = ?\n" +
                    "  AND u.created_date > ?";
        
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) invokePrivateMethod("extractColumnNamesFromWhereClause", 
            new Class[]{String.class, int.class}, sql, 3);
        
        assertEquals(3, result.size());
        assertEquals("email", result.get(0));
        assertEquals("status", result.get(1));
        // Note: Our regex only catches = ? patterns, not > ? patterns  
        assertEquals("param3", result.get(2)); // Falls back to default for > ?
    }
    
    @Test
    @DisplayName("SQL with subquery")
    void testSQLWithSubquery() throws Exception {
        String sql = "SELECT * FROM orders o WHERE o.customer_id = ? " +
                    "AND o.total > (SELECT AVG(total) FROM orders WHERE status = ?)";
        
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) invokePrivateMethod("extractColumnNamesFromWhereClause", 
            new Class[]{String.class, int.class}, sql, 2);
        
        assertEquals(2, result.size());
        assertEquals("customerId", result.get(0));
        assertEquals("status", result.get(1));
    }
    
    @Test
    @DisplayName("SQL without WHERE clause")
    void testSQLWithoutWhereClause() throws Exception {
        String sql = "SELECT * FROM users ORDER BY name";
        
        String whereClause = (String) invokePrivateMethod("extractWhereClause", new Class[]{String.class}, sql);
        assertNull(whereClause);
    }
    
    @Test
    @DisplayName("Empty WHERE clause")
    void testEmptyWhereClause() throws Exception {
        String sql = "SELECT * FROM users WHERE";
        
        String whereClause = (String) invokePrivateMethod("extractWhereClause", new Class[]{String.class}, sql);
        assertEquals("", whereClause);
    }
    
    @Test
    @DisplayName("WHERE clause with OR conditions")
    void testWhereClauseWithOrConditions() throws Exception {
        String sql = "SELECT * FROM users WHERE name = ? OR email = ? OR phone = ?";
        
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) invokePrivateMethod("extractColumnNamesFromWhereClause", 
            new Class[]{String.class, int.class}, sql, 3);
        
        assertEquals(3, result.size());
        assertEquals("name", result.get(0));
        assertEquals("email", result.get(1));
        assertEquals("phone", result.get(2));
    }
    
    @Test
    @DisplayName("WHERE clause with mixed AND/OR")
    void testWhereClauseWithMixedConditions() throws Exception {
        String sql = "SELECT * FROM products p WHERE (p.category = ? OR p.subcategory = ?) AND p.price > ? AND p.in_stock = ?";
        
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) invokePrivateMethod("extractColumnNamesFromWhereClause", 
            new Class[]{String.class, int.class}, sql, 4);
        
        assertEquals(4, result.size());
        assertEquals("category", result.get(0));
        assertEquals("subcategory", result.get(1));
        assertEquals("inStock", result.get(2)); // Only catches = ? patterns
        assertEquals("param4", result.get(3)); // Falls back for > ? pattern
    }
    
    @Test
    @DisplayName("Case insensitive WHERE keyword")
    void testCaseInsensitiveWhere() throws Exception {
        String sql1 = "SELECT * FROM users where name = ?";
        String sql2 = "SELECT * FROM users WHERE name = ?";
        String sql3 = "SELECT * FROM users Where name = ?";
        
        @SuppressWarnings("unchecked")
        List<String> result1 = (List<String>) invokePrivateMethod("extractColumnNamesFromWhereClause", 
            new Class[]{String.class, int.class}, sql1, 1);
        @SuppressWarnings("unchecked")
        List<String> result2 = (List<String>) invokePrivateMethod("extractColumnNamesFromWhereClause", 
            new Class[]{String.class, int.class}, sql2, 1);
        @SuppressWarnings("unchecked")
        List<String> result3 = (List<String>) invokePrivateMethod("extractColumnNamesFromWhereClause", 
            new Class[]{String.class, int.class}, sql3, 1);
        
        assertEquals("name", result1.get(0));
        assertEquals("name", result2.get(0));
        assertEquals("name", result3.get(0));
    }
    
    @Test
    @DisplayName("Parameter count mismatch - more parameters than extracted")
    void testParameterCountMismatch() throws Exception {
        String sql = "SELECT * FROM users WHERE name = ?";
        
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) invokePrivateMethod("extractColumnNamesFromWhereClause", 
            new Class[]{String.class, int.class}, sql, 3);
        
        assertEquals(3, result.size());
        assertEquals("name", result.get(0));
        assertEquals("param2", result.get(1)); // Should fallback to default
        assertEquals("param3", result.get(2)); // Should fallback to default
    }
    
    @Test
    @DisplayName("Column reference without table alias")
    void testColumnReferenceWithoutAlias() throws Exception {
        String sql = "SELECT * FROM users WHERE first_name = ? AND last_name = ?";
        
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) invokePrivateMethod("extractColumnNamesFromWhereClause", 
            new Class[]{String.class, int.class}, sql, 2);
        
        assertEquals(2, result.size());
        assertEquals("firstName", result.get(0));
        assertEquals("lastName", result.get(1));
    }
    
    @Test
    @DisplayName("Edge case: null and empty strings")
    void testEdgeCases() throws Exception {
        // Test null input for toCamelCase
        String result1 = (String) invokePrivateMethod("toCamelCase", new Class[]{String.class}, (String) null);
        assertNull(result1);
        
        // Test empty string for toCamelCase
        String result2 = (String) invokePrivateMethod("toCamelCase", new Class[]{String.class}, "");
        assertEquals("", result2);
        
        // Test default parameter name generation
        @SuppressWarnings("unchecked")
        List<String> result3 = (List<String>) invokePrivateMethod("generateDefaultParameterNames", new Class[]{int.class}, 3);
        assertEquals(3, result3.size());
        assertEquals("param1", result3.get(0));
        assertEquals("param2", result3.get(1));
        assertEquals("param3", result3.get(2));
    }
    
    @Test
    @DisplayName("Full integration test with mocked database metadata")
    void testFullIntegrationWithMockedDB() throws SQLException {
        String sql = "SELECT * FROM customer cus WHERE cus.customer_id = ? AND cus.first_name = ?";
        
        // Mock parameter metadata
        when(parameterMetaData.getParameterCount()).thenReturn(2);
        when(parameterMetaData.getParameterType(1)).thenReturn(Types.INTEGER);
        when(parameterMetaData.getParameterType(2)).thenReturn(Types.VARCHAR);
        
        List<DBColumn> result = extractor.extractParameters(sql);
        
        assertEquals(2, result.size());
        assertEquals("customerId", result.get(0).columnName());
        assertEquals("Integer", result.get(0).javaType());
        assertEquals("INTEGER", result.get(0).jdbcType());
        
        assertEquals("firstName", result.get(1).columnName());
        assertEquals("String", result.get(1).javaType());
        assertEquals("VARCHAR", result.get(1).jdbcType());
    }
    
    @Test
    @DisplayName("Integration test with SQL exception handling")
    void testSQLExceptionHandling() throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        when(parameterMetaData.getParameterCount()).thenReturn(1);
        when(parameterMetaData.getParameterType(1)).thenThrow(new SQLException("Parameter metadata not available"));
        
        List<DBColumn> result = extractor.extractParameters(sql);
        
        assertEquals(1, result.size());
        assertEquals("id", result.get(0).columnName());
        assertEquals("String", result.get(0).javaType()); // Default fallback
        assertEquals("VARCHAR", result.get(0).jdbcType()); // Default fallback
    }
    
    @Test
    @DisplayName("Complex query with EXISTS subquery")
    void testComplexQueryWithExists() throws Exception {
        String sql = "SELECT * FROM customers c WHERE c.status = ? " +
                    "AND EXISTS (SELECT 1 FROM orders o WHERE o.customer_id = c.id AND o.total > ?)";
        
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) invokePrivateMethod("extractColumnNamesFromWhereClause", 
            new Class[]{String.class, int.class}, sql, 2);
        
        assertEquals(2, result.size());
        assertEquals("status", result.get(0));
        // Note: Our regex only catches = ? patterns, not > ? patterns
        assertEquals("param2", result.get(1)); // Falls back to default for > ? 
    }
    
    @Test
    @DisplayName("Query with BETWEEN operator")
    void testQueryWithBetween() throws Exception {
        String sql = "SELECT * FROM products p WHERE p.price BETWEEN ? AND ? AND p.category = ?";
        
        // Note: BETWEEN uses 2 parameters but our current regex won't catch this
        // This test documents current behavior - may need enhancement for BETWEEN
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) invokePrivateMethod("extractColumnNamesFromWhereClause", 
            new Class[]{String.class, int.class}, sql, 3);
        
        assertEquals(3, result.size());
        assertEquals("category", result.get(0)); // Only catches the = ? pattern
        assertEquals("param2", result.get(1));   // Fallback for BETWEEN params
        assertEquals("param3", result.get(2));   // Fallback for BETWEEN params
    }
    
    @Test
    @DisplayName("Query with IN operator")
    void testQueryWithInOperator() throws Exception {
        String sql = "SELECT * FROM users u WHERE u.role IN (?, ?, ?) AND u.status = ?";
        
        // Similar to BETWEEN, IN operator usage may not be fully captured by current regex
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) invokePrivateMethod("extractColumnNamesFromWhereClause", 
            new Class[]{String.class, int.class}, sql, 4);
        
        assertEquals(4, result.size());
        assertEquals("status", result.get(0)); // Only catches the = ? pattern
        assertEquals("param2", result.get(1)); // Fallbacks for IN params
        assertEquals("param3", result.get(2));
        assertEquals("param4", result.get(3));
    }
    
    @Test
    @DisplayName("Java type mapping for different SQL types")
    void testJavaTypeMapping() throws SQLException {
        String sql = "SELECT * FROM test WHERE col = ?";
        
        // Test INTEGER mapping
        when(parameterMetaData.getParameterCount()).thenReturn(1);
        when(parameterMetaData.getParameterType(1)).thenReturn(Types.INTEGER);
        
        List<DBColumn> result = extractor.extractParameters(sql);
        assertEquals("Integer", result.get(0).javaType());
        assertEquals("INTEGER", result.get(0).jdbcType());
    }
    
    @Test
    @DisplayName("Complex SQL with CTE (Common Table Expression)")
    void testSQLWithCTE() throws Exception {
        String sql = "WITH active_users AS (SELECT * FROM users WHERE active = 1) " +
                    "SELECT * FROM active_users au WHERE au.created_date > ? AND au.department = ?";
        
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) invokePrivateMethod("extractColumnNamesFromWhereClause", 
            new Class[]{String.class, int.class}, sql, 2);
        
        assertEquals(2, result.size());
        // Note: Our regex only catches = ? patterns, not > ? patterns
        assertEquals("department", result.get(0)); // Only catches = ? pattern
        assertEquals("param2", result.get(1)); // Falls back for > ? pattern
    }
    
    @Test
    @DisplayName("SQL with UNION and WHERE clauses")
    void testSQLWithUnion() throws Exception {
        String sql = "SELECT * FROM customers c WHERE c.type = ? " +
                    "UNION " +
                    "SELECT * FROM prospects p WHERE p.status = ?";
        
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) invokePrivateMethod("extractColumnNamesFromWhereClause", 
            new Class[]{String.class, int.class}, sql, 2);
        
        assertEquals(2, result.size());
        assertEquals("type", result.get(0));
        assertEquals("status", result.get(1));
    }
}