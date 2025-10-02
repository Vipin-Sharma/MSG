package com.jfeatures.msg.codegen.dbmetadata;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class SqlMetadataTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    
    @Mock
    private ResultSet resultSet;
    
    @Mock
    private ResultSetMetaData resultSetMetaData;
    
    private SqlMetadata sqlMetadata;
    
    @BeforeEach
    void setUp() {
        sqlMetadata = new SqlMetadata(jdbcTemplate);
    }
    
    @Test
    void testGetColumnMetadata_ValidQuery_ReturnsColumnMetadata() throws SQLException {
        // Given
        String query = "SELECT customer_id, customer_name, email FROM customers";
        
        setupResultSetMetadata(3); // 3 columns
        
        // Mock the query execution to call our RowMapper
        when(jdbcTemplate.query(eq(query), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper<ColumnMetadata> rowMapper = invocation.getArgument(1);
            
            // Simulate calling the RowMapper with our mocked ResultSet
            when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
            rowMapper.mapRow(resultSet, 0);
            
            return null; // The actual return is handled by the RowMapper logic
        });
        
        // When
        List<ColumnMetadata> result = sqlMetadata.getColumnMetadata(query);
        
        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Verify first column
        ColumnMetadata firstColumn = result.get(0);
        assertEquals("customer_id", firstColumn.getColumnName());
        assertEquals("customer_id", firstColumn.getColumnAlias());
        assertEquals("customers", firstColumn.getTableName());
        assertEquals(Types.INTEGER, firstColumn.getColumnType());
        assertEquals("INT", firstColumn.getColumnTypeName());
        assertEquals("java.lang.Integer", firstColumn.getColumnClassName());
        assertEquals(10, firstColumn.getColumnDisplaySize());
        assertEquals(10, firstColumn.getPrecision());
        assertEquals(0, firstColumn.getScale());
        assertEquals(0, firstColumn.getIsNullable()); // NOT NULL
        assertFalse(firstColumn.isAutoIncrement());
        assertFalse(firstColumn.isCaseSensitive());
        assertTrue(firstColumn.isReadOnly());
        assertFalse(firstColumn.isWritable());
        assertFalse(firstColumn.isDefinitelyWritable());
        assertFalse(firstColumn.isCurrency());
        assertTrue(firstColumn.isSigned());
        
        // Verify second column
        ColumnMetadata secondColumn = result.get(1);
        assertEquals("customer_name", secondColumn.getColumnName());
        assertEquals("VARCHAR", secondColumn.getColumnTypeName());
        assertEquals(1, secondColumn.getIsNullable()); // NULLABLE
        
        // Verify third column
        ColumnMetadata thirdColumn = result.get(2);
        assertEquals("email", thirdColumn.getColumnName());
        assertEquals("VARCHAR", thirdColumn.getColumnTypeName());
        
        verify(jdbcTemplate).query(eq(query), any(RowMapper.class));
    }
    
    @Test
    void testGetColumnMetadata_EmptyResult_ReturnsEmptyList() {
        // Given
        String query = "SELECT customer_id FROM customers WHERE 1 = 0"; // Query that returns no rows
        
        // Mock query to return empty result but still call RowMapper to process metadata
        when(jdbcTemplate.query(eq(query), any(RowMapper.class))).thenReturn(null);
        
        // When
        List<ColumnMetadata> result = sqlMetadata.getColumnMetadata(query);
        
        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        
        verify(jdbcTemplate).query(eq(query), any(RowMapper.class));
    }
    
    @Test
    void testGetColumnMetadata_SingleColumn_ReturnsOneColumnMetadata() throws SQLException {
        // Given
        String query = "SELECT customer_id FROM customers";
        
        setupSingleColumnResultSetMetadata();
        
        // Mock the query execution
        when(jdbcTemplate.query(eq(query), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper<ColumnMetadata> rowMapper = invocation.getArgument(1);
            
            when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
            rowMapper.mapRow(resultSet, 0);
            
            return null;
        });
        
        // When
        List<ColumnMetadata> result = sqlMetadata.getColumnMetadata(query);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        ColumnMetadata column = result.get(0);
        assertEquals("customer_id", column.getColumnName());
        assertEquals(Types.INTEGER, column.getColumnType());
        assertEquals("INT", column.getColumnTypeName());
    }
    
    @Test
    void testGetColumnMetadata_ComplexQueryWithJoin_ReturnsAllColumns() throws SQLException {
        // Given
        String query = """
            SELECT c.customer_id, c.customer_name, o.order_id, o.order_date 
            FROM customers c 
            JOIN orders o ON c.customer_id = o.customer_id
            """;
        
        setupComplexQueryMetadata();
        
        // Mock the query execution
        when(jdbcTemplate.query(eq(query), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper<ColumnMetadata> rowMapper = invocation.getArgument(1);
            
            when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
            rowMapper.mapRow(resultSet, 0);
            
            return null;
        });
        
        // When
        List<ColumnMetadata> result = sqlMetadata.getColumnMetadata(query);
        
        // Then
        assertNotNull(result);
        assertEquals(4, result.size());
        
        // Verify table names are correctly extracted
        assertEquals("customers", result.get(0).getTableName());
        assertEquals("customers", result.get(1).getTableName());
        assertEquals("orders", result.get(2).getTableName());
        assertEquals("orders", result.get(3).getTableName());
    }
    
    @Test
    void testGetColumnMetadata_QueryWithAliases_HandlesAliasesCorrectly() throws SQLException {
        // Given
        String query = "SELECT customer_id AS id, customer_name AS name FROM customers";
        
        setupAliasedColumnMetadata();
        
        // Mock the query execution
        when(jdbcTemplate.query(eq(query), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper<ColumnMetadata> rowMapper = invocation.getArgument(1);
            
            when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
            rowMapper.mapRow(resultSet, 0);
            
            return null;
        });
        
        // When
        List<ColumnMetadata> result = sqlMetadata.getColumnMetadata(query);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify column names vs aliases
        ColumnMetadata firstColumn = result.get(0);
        assertEquals("customer_id", firstColumn.getColumnName());
        assertEquals("id", firstColumn.getColumnAlias());
        
        ColumnMetadata secondColumn = result.get(1);
        assertEquals("customer_name", secondColumn.getColumnName());
        assertEquals("name", secondColumn.getColumnAlias());
    }

    @Test
    void testGetColumnMetadata_CteQuery_AcceptsWithClause() throws SQLException {
        String query = """
            WITH cte AS (SELECT 1 AS id)
            SELECT id FROM cte
            """;

        setupSingleColumnResultSetMetadata();

        when(jdbcTemplate.query(eq(query), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper<ColumnMetadata> rowMapper = invocation.getArgument(1);

            when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
            rowMapper.mapRow(resultSet, 0);

            return null;
        });

        List<ColumnMetadata> result = sqlMetadata.getColumnMetadata(query);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetColumnMetadata_QueryWithLeadingComment_Allowed() throws SQLException {
        String query = """
            -- fetch customer ids
            SELECT customer_id FROM customers
            """;

        setupSingleColumnResultSetMetadata();

        when(jdbcTemplate.query(eq(query), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper<ColumnMetadata> rowMapper = invocation.getArgument(1);

            when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
            rowMapper.mapRow(resultSet, 0);

            return null;
        });

        List<ColumnMetadata> result = sqlMetadata.getColumnMetadata(query);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
    
    @Test
    void testGetColumnMetadata_SQLException_PropagatesException() {
        // Given - use valid SQL structure that will pass validation but fail on execution
        String query = "SELECT column1 FROM non_existent_table";
        DataAccessException dataAccessException = new DataAccessException("Invalid SQL syntax") {};
        
        when(jdbcTemplate.query(eq(query), any(RowMapper.class)))
            .thenThrow(dataAccessException);
        
        // When & Then
        DataAccessException exception = assertThrows(
            DataAccessException.class,
            () -> sqlMetadata.getColumnMetadata(query)
        );
        
        assertEquals("Unable to retrieve column metadata from database for the provided SQL query", exception.getMessage());
        verify(jdbcTemplate).query(eq(query), any(RowMapper.class));
    }
    
    @Test
    void testGetColumnMetadata_NullableAndAutoIncrementColumns_HandlesCorrectly() throws SQLException {
        // Given
        String query = "SELECT id, name, created_date FROM test_table";
        
        setupNullableAutoIncrementMetadata();
        
        // Mock the query execution
        when(jdbcTemplate.query(eq(query), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper<ColumnMetadata> rowMapper = invocation.getArgument(1);
            
            when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
            rowMapper.mapRow(resultSet, 0);
            
            return null;
        });
        
        // When
        List<ColumnMetadata> result = sqlMetadata.getColumnMetadata(query);
        
        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Verify auto-increment column
        ColumnMetadata idColumn = result.get(0);
        assertTrue(idColumn.isAutoIncrement());
        assertEquals(0, idColumn.getIsNullable()); // NOT NULL
        
        // Verify nullable column
        ColumnMetadata nameColumn = result.get(1);
        assertFalse(nameColumn.isAutoIncrement());
        assertEquals(1, nameColumn.getIsNullable()); // NULLABLE
        
        // Verify timestamp column
        ColumnMetadata dateColumn = result.get(2);
        assertEquals(Types.TIMESTAMP, dateColumn.getColumnType());
        assertEquals("TIMESTAMP", dateColumn.getColumnTypeName());
    }
    
    private void setupResultSetMetadata(int columnCount) throws SQLException {
        when(resultSetMetaData.getColumnCount()).thenReturn(columnCount);
        
        // Setup first column (customer_id)
        when(resultSetMetaData.getColumnName(1)).thenReturn("customer_id");
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("customer_id");
        when(resultSetMetaData.getTableName(1)).thenReturn("customers");
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.INTEGER);
        when(resultSetMetaData.getColumnTypeName(1)).thenReturn("INT");
        when(resultSetMetaData.getColumnClassName(1)).thenReturn("java.lang.Integer");
        when(resultSetMetaData.getColumnDisplaySize(1)).thenReturn(10);
        when(resultSetMetaData.getPrecision(1)).thenReturn(10);
        when(resultSetMetaData.getScale(1)).thenReturn(0);
        when(resultSetMetaData.isNullable(1)).thenReturn(0); // NOT NULL
        when(resultSetMetaData.isAutoIncrement(1)).thenReturn(false);
        when(resultSetMetaData.isCaseSensitive(1)).thenReturn(false);
        when(resultSetMetaData.isReadOnly(1)).thenReturn(true);
        when(resultSetMetaData.isWritable(1)).thenReturn(false);
        when(resultSetMetaData.isDefinitelyWritable(1)).thenReturn(false);
        when(resultSetMetaData.isCurrency(1)).thenReturn(false);
        when(resultSetMetaData.isSigned(1)).thenReturn(true);
        
        if (columnCount > 1) {
            // Setup second column (customer_name)
            when(resultSetMetaData.getColumnName(2)).thenReturn("customer_name");
            when(resultSetMetaData.getColumnLabel(2)).thenReturn("customer_name");
            when(resultSetMetaData.getTableName(2)).thenReturn("customers");
            when(resultSetMetaData.getColumnType(2)).thenReturn(Types.VARCHAR);
            when(resultSetMetaData.getColumnTypeName(2)).thenReturn("VARCHAR");
            when(resultSetMetaData.getColumnClassName(2)).thenReturn("java.lang.String");
            when(resultSetMetaData.getColumnDisplaySize(2)).thenReturn(255);
            when(resultSetMetaData.getPrecision(2)).thenReturn(255);
            when(resultSetMetaData.getScale(2)).thenReturn(0);
            when(resultSetMetaData.isNullable(2)).thenReturn(1); // NULLABLE
            when(resultSetMetaData.isAutoIncrement(2)).thenReturn(false);
            when(resultSetMetaData.isCaseSensitive(2)).thenReturn(true);
            when(resultSetMetaData.isReadOnly(2)).thenReturn(true);
            when(resultSetMetaData.isWritable(2)).thenReturn(false);
            when(resultSetMetaData.isDefinitelyWritable(2)).thenReturn(false);
            when(resultSetMetaData.isCurrency(2)).thenReturn(false);
            when(resultSetMetaData.isSigned(2)).thenReturn(false);
        }
        
        if (columnCount > 2) {
            // Setup third column (email)
            when(resultSetMetaData.getColumnName(3)).thenReturn("email");
            when(resultSetMetaData.getColumnLabel(3)).thenReturn("email");
            when(resultSetMetaData.getTableName(3)).thenReturn("customers");
            when(resultSetMetaData.getColumnType(3)).thenReturn(Types.VARCHAR);
            when(resultSetMetaData.getColumnTypeName(3)).thenReturn("VARCHAR");
            when(resultSetMetaData.getColumnClassName(3)).thenReturn("java.lang.String");
            when(resultSetMetaData.getColumnDisplaySize(3)).thenReturn(100);
            when(resultSetMetaData.getPrecision(3)).thenReturn(100);
            when(resultSetMetaData.getScale(3)).thenReturn(0);
            when(resultSetMetaData.isNullable(3)).thenReturn(1); // NULLABLE
            when(resultSetMetaData.isAutoIncrement(3)).thenReturn(false);
            when(resultSetMetaData.isCaseSensitive(3)).thenReturn(true);
            when(resultSetMetaData.isReadOnly(3)).thenReturn(true);
            when(resultSetMetaData.isWritable(3)).thenReturn(false);
            when(resultSetMetaData.isDefinitelyWritable(3)).thenReturn(false);
            when(resultSetMetaData.isCurrency(3)).thenReturn(false);
            when(resultSetMetaData.isSigned(3)).thenReturn(false);
        }
    }
    
    private void setupSingleColumnResultSetMetadata() throws SQLException {
        setupResultSetMetadata(1);
    }
    
    private void setupComplexQueryMetadata() throws SQLException {
        when(resultSetMetaData.getColumnCount()).thenReturn(4);
        
        // customer_id from customers table
        when(resultSetMetaData.getColumnName(1)).thenReturn("customer_id");
        when(resultSetMetaData.getTableName(1)).thenReturn("customers");
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.INTEGER);
        when(resultSetMetaData.getColumnTypeName(1)).thenReturn("INT");
        
        // customer_name from customers table
        when(resultSetMetaData.getColumnName(2)).thenReturn("customer_name");
        when(resultSetMetaData.getTableName(2)).thenReturn("customers");
        when(resultSetMetaData.getColumnType(2)).thenReturn(Types.VARCHAR);
        when(resultSetMetaData.getColumnTypeName(2)).thenReturn("VARCHAR");
        
        // order_id from orders table
        when(resultSetMetaData.getColumnName(3)).thenReturn("order_id");
        when(resultSetMetaData.getTableName(3)).thenReturn("orders");
        when(resultSetMetaData.getColumnType(3)).thenReturn(Types.INTEGER);
        when(resultSetMetaData.getColumnTypeName(3)).thenReturn("INT");
        
        // order_date from orders table
        when(resultSetMetaData.getColumnName(4)).thenReturn("order_date");
        when(resultSetMetaData.getTableName(4)).thenReturn("orders");
        when(resultSetMetaData.getColumnType(4)).thenReturn(Types.TIMESTAMP);
        when(resultSetMetaData.getColumnTypeName(4)).thenReturn("TIMESTAMP");
        
        // Set up other required metadata for all columns
        String[] columnNames = {"customer_id", "customer_name", "order_id", "order_date"};
        for (int i = 1; i <= 4; i++) {
            when(resultSetMetaData.getColumnLabel(i)).thenReturn(columnNames[i - 1]);
            when(resultSetMetaData.getColumnClassName(i)).thenReturn("java.lang.Object");
            when(resultSetMetaData.getColumnDisplaySize(i)).thenReturn(20);
            when(resultSetMetaData.getPrecision(i)).thenReturn(20);
            when(resultSetMetaData.getScale(i)).thenReturn(0);
            when(resultSetMetaData.isNullable(i)).thenReturn(1);
            when(resultSetMetaData.isAutoIncrement(i)).thenReturn(false);
            when(resultSetMetaData.isCaseSensitive(i)).thenReturn(false);
            when(resultSetMetaData.isReadOnly(i)).thenReturn(true);
            when(resultSetMetaData.isWritable(i)).thenReturn(false);
            when(resultSetMetaData.isDefinitelyWritable(i)).thenReturn(false);
            when(resultSetMetaData.isCurrency(i)).thenReturn(false);
            when(resultSetMetaData.isSigned(i)).thenReturn(true);
        }
    }
    
    private void setupAliasedColumnMetadata() throws SQLException {
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        
        // customer_id AS id
        when(resultSetMetaData.getColumnName(1)).thenReturn("customer_id");
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("id"); // Alias
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.INTEGER);
        when(resultSetMetaData.getColumnTypeName(1)).thenReturn("INT");
        
        // customer_name AS name
        when(resultSetMetaData.getColumnName(2)).thenReturn("customer_name");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("name"); // Alias
        when(resultSetMetaData.getColumnType(2)).thenReturn(Types.VARCHAR);
        when(resultSetMetaData.getColumnTypeName(2)).thenReturn("VARCHAR");
        
        // Set up other required metadata
        for (int i = 1; i <= 2; i++) {
            when(resultSetMetaData.getTableName(i)).thenReturn("customers");
            when(resultSetMetaData.getColumnClassName(i)).thenReturn("java.lang.Object");
            when(resultSetMetaData.getColumnDisplaySize(i)).thenReturn(20);
            when(resultSetMetaData.getPrecision(i)).thenReturn(20);
            when(resultSetMetaData.getScale(i)).thenReturn(0);
            when(resultSetMetaData.isNullable(i)).thenReturn(1);
            when(resultSetMetaData.isAutoIncrement(i)).thenReturn(false);
            when(resultSetMetaData.isCaseSensitive(i)).thenReturn(false);
            when(resultSetMetaData.isReadOnly(i)).thenReturn(true);
            when(resultSetMetaData.isWritable(i)).thenReturn(false);
            when(resultSetMetaData.isDefinitelyWritable(i)).thenReturn(false);
            when(resultSetMetaData.isCurrency(i)).thenReturn(false);
            when(resultSetMetaData.isSigned(i)).thenReturn(true);
        }
    }
    
    private void setupNullableAutoIncrementMetadata() throws SQLException {
        when(resultSetMetaData.getColumnCount()).thenReturn(3);
        
        // id - auto increment, not null
        when(resultSetMetaData.getColumnName(1)).thenReturn("id");
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("id");
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.INTEGER);
        when(resultSetMetaData.getColumnTypeName(1)).thenReturn("INT");
        when(resultSetMetaData.isNullable(1)).thenReturn(0); // NOT NULL
        when(resultSetMetaData.isAutoIncrement(1)).thenReturn(true);
        
        // name - nullable, not auto increment
        when(resultSetMetaData.getColumnName(2)).thenReturn("name");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("name");
        when(resultSetMetaData.getColumnType(2)).thenReturn(Types.VARCHAR);
        when(resultSetMetaData.getColumnTypeName(2)).thenReturn("VARCHAR");
        when(resultSetMetaData.isNullable(2)).thenReturn(1); // NULLABLE
        when(resultSetMetaData.isAutoIncrement(2)).thenReturn(false);
        
        // created_date - timestamp
        when(resultSetMetaData.getColumnName(3)).thenReturn("created_date");
        when(resultSetMetaData.getColumnLabel(3)).thenReturn("created_date");
        when(resultSetMetaData.getColumnType(3)).thenReturn(Types.TIMESTAMP);
        when(resultSetMetaData.getColumnTypeName(3)).thenReturn("TIMESTAMP");
        when(resultSetMetaData.isNullable(3)).thenReturn(1); // NULLABLE
        when(resultSetMetaData.isAutoIncrement(3)).thenReturn(false);
        
        // Set up other required metadata
        for (int i = 1; i <= 3; i++) {
            when(resultSetMetaData.getTableName(i)).thenReturn("test_table");
            when(resultSetMetaData.getColumnClassName(i)).thenReturn("java.lang.Object");
            when(resultSetMetaData.getColumnDisplaySize(i)).thenReturn(20);
            when(resultSetMetaData.getPrecision(i)).thenReturn(20);
            when(resultSetMetaData.getScale(i)).thenReturn(0);
            when(resultSetMetaData.isCaseSensitive(i)).thenReturn(false);
            when(resultSetMetaData.isReadOnly(i)).thenReturn(true);
            when(resultSetMetaData.isWritable(i)).thenReturn(false);
            when(resultSetMetaData.isDefinitelyWritable(i)).thenReturn(false);
            when(resultSetMetaData.isCurrency(i)).thenReturn(false);
            when(resultSetMetaData.isSigned(i)).thenReturn(true);
        }
    }
}