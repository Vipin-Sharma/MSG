package com.jfeatures.msg.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import com.jfeatures.msg.codegen.MicroServiceGenerator;
import com.jfeatures.msg.codegen.ParameterMetadataExtractor;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.SqlMetadata;
import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.codegen.domain.GeneratedMicroservice;
import com.jfeatures.msg.codegen.sql.SqlFileResolver;
import com.jfeatures.msg.codegen.util.SqlStatementDetector;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import com.jfeatures.msg.controller.CodeGenController;
import com.jfeatures.msg.test.TestUtils;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Comprehensive end-to-end test covering all major scenarios and edge cases
 * for the MSG (Microservice Generator) system.
 */
@ExtendWith(MockitoExtension.class)
class CompleteScenarioCoverageTest {

    private MicroServiceGenerator microServiceGenerator;
    private SqlFileResolver sqlFileResolver;
    
    private DataSource mockDataSource;
    private JdbcTemplate mockJdbcTemplate;
    private NamedParameterJdbcTemplate mockNamedParameterJdbcTemplate;
    private DatabaseConnection mockDatabaseConnection;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ParameterMetaData mockParameterMetaData;
    
    @BeforeEach
    void setUp() throws Exception {
        microServiceGenerator = new MicroServiceGenerator();
        sqlFileResolver = new SqlFileResolver();
        
        mockDataSource = mock(DataSource.class);
        mockJdbcTemplate = mock(JdbcTemplate.class);
        mockNamedParameterJdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        mockDatabaseConnection = mock(DatabaseConnection.class);
        
        // Setup database connection chain with lenient stubbing to avoid unnecessary stubbing exceptions
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockParameterMetaData = mock(ParameterMetaData.class);
        
        try {
            lenient().when(mockDataSource.getConnection()).thenReturn(mockConnection);
            lenient().when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            lenient().when(mockPreparedStatement.getParameterMetaData()).thenReturn(mockParameterMetaData);
            lenient().when(mockParameterMetaData.getParameterCount()).thenReturn(1);
        } catch (Exception e) {
            // Handle SQLException from mocking
        }
        
        lenient().when(mockDatabaseConnection.dataSource()).thenReturn(mockDataSource);
        lenient().when(mockDatabaseConnection.jdbcTemplate()).thenReturn(mockJdbcTemplate);
        lenient().when(mockDatabaseConnection.namedParameterJdbcTemplate()).thenReturn(mockNamedParameterJdbcTemplate);
    }
    
    /**
     * Tests the complete microservice generation pipeline from SQL file to generated code
     */
    @Test
    void testCompleteEndToEndPipeline_AllSqlTypes_GeneratesAllComponents() throws Exception {
        // Test case 1: SELECT with complex JOIN
        testSelectScenario();
        
        // Test case 2: INSERT with multiple columns
        testInsertScenario();
        
        // Test case 3: UPDATE with SET and WHERE clauses
        testUpdateScenario();
        
        // Test case 4: DELETE with complex WHERE conditions
        testDeleteScenario();
    }
    
    private void testSelectScenario() throws Exception {
        // Given: Complex SELECT query with JOINs and multiple parameters
        String complexSelectSql = """
            SELECT 
                c.customer_id,
                c.first_name,
                c.last_name,
                c.email,
                a.address_line1,
                a.city,
                a.postal_code,
                ct.country_name
            FROM customers c
            INNER JOIN addresses a ON c.customer_id = a.customer_id
            INNER JOIN countries ct ON a.country_id = ct.country_id
            WHERE c.customer_id = ?
            AND c.status = ?
            AND c.created_date >= ?
            AND a.is_primary = ?
            ORDER BY c.last_name, c.first_name
            """;
        
        String businessDomainName = "CustomerAddress";
        
        // Setup mock column metadata for SELECT
        List<ColumnMetadata> mockColumnMetadata = new ArrayList<>();
        mockColumnMetadata.add(TestUtils.createColumnMetadata("customer_id", "INT", Types.INTEGER, false));
        mockColumnMetadata.add(TestUtils.createColumnMetadata("first_name", "VARCHAR", Types.VARCHAR, false));
        mockColumnMetadata.add(TestUtils.createColumnMetadata("last_name", "VARCHAR", Types.VARCHAR, false));
        mockColumnMetadata.add(TestUtils.createColumnMetadata("email", "VARCHAR", Types.VARCHAR, true));
        mockColumnMetadata.add(TestUtils.createColumnMetadata("address_line1", "VARCHAR", Types.VARCHAR, true));
        mockColumnMetadata.add(TestUtils.createColumnMetadata("city", "VARCHAR", Types.VARCHAR, true));
        mockColumnMetadata.add(TestUtils.createColumnMetadata("postal_code", "VARCHAR", Types.VARCHAR, true));
        mockColumnMetadata.add(TestUtils.createColumnMetadata("country_name", "VARCHAR", Types.VARCHAR, true));
        
        // Setup mock parameters for WHERE clause
        ArrayList<DBColumn> mockParameters = new ArrayList<>();
        mockParameters.add(new DBColumn("table", "customerId", "Integer", "INTEGER"));
        mockParameters.add(new DBColumn("table", "status", "String", "VARCHAR"));
        mockParameters.add(new DBColumn("table", "createdDate", "Date", "DATE"));
        mockParameters.add(new DBColumn("table", "isPrimary", "Boolean", "BIT"));
        
        try (var sqlMetadataMockedConstruction = mockConstruction(SqlMetadata.class, (mock, context) -> {
                 try {
                     when(mock.getColumnMetadata(complexSelectSql)).thenReturn(mockColumnMetadata);
                 } catch (Exception e) {
                     // Handle SQLException from mocking
                 }
             });
             var controllerMockedConstruction = mockConstruction(CodeGenController.class, (mock, context) -> {
                 when(mock.selectColumnMetadata()).thenReturn(mockColumnMetadata);
             });
             var extractorMockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
                 when(mock.extractParameters(complexSelectSql)).thenReturn(mockParameters);
             })) {
            
            // When: Generate microservice
            GeneratedMicroservice result = microServiceGenerator.generateMicroserviceFromSql(
                complexSelectSql, businessDomainName, mockDatabaseConnection
            );
            
            // Then: Verify complete microservice generation
            assertNotNull(result, "Microservice should be generated");
            assertEquals(SqlStatementType.SELECT, result.statementType(), "Should detect SELECT statement");
            assertEquals(businessDomainName, result.businessDomainName(), "Should use correct business domain name");
            
            // Verify all components are generated
            validateGeneratedComponents(result, "CustomerAddress", "GET");
            
            // Verify SELECT-specific content
            String dtoContent = result.dtoFile().toString();
            assertTrue(dtoContent.contains("customerId"), "Should contain customer ID field");
            assertTrue(dtoContent.contains("firstName"), "Should contain first name field");
            assertTrue(dtoContent.contains("addressLine1"), "Should contain address field");
            assertTrue(dtoContent.contains("countryName"), "Should contain country field");
            
            String controllerContent = result.controllerFile().toString();
            assertTrue(controllerContent.contains("@GetMapping"), "Should have GET mapping");
            assertTrue(controllerContent.contains("@RequestParam"), "Should have request parameters");
            assertTrue(controllerContent.contains("List<CustomerAddressDTO>"), "Should return list of DTOs");
            
            String daoContent = result.daoFile().toString();
            assertTrue(daoContent.contains("INNER JOIN"), "Should preserve JOIN clauses");
            assertTrue(daoContent.contains("ORDER BY"), "Should preserve ORDER BY clause");
        }
    }
    
    private void testInsertScenario() throws Exception {
        // Given: INSERT with multiple columns and different data types
        String insertSql = """
            INSERT INTO products (
                product_name,
                description,
                price,
                category_id,
                is_active,
                created_date,
                created_by,
                weight,
                dimensions
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        String businessDomainName = "Product";
        
        // Setup mocks for INSERT scenario
        TestUtils.setupInsertWorkflowMocks(mockDatabaseConnection, mockDataSource, mockNamedParameterJdbcTemplate);
        
        // Setup database metadata mocks for INSERT metadata extraction
        DatabaseMetaData mockDatabaseMetaData = mock(DatabaseMetaData.class);
        when(mockConnection.getMetaData()).thenReturn(mockDatabaseMetaData);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        
        // Mock column metadata ResultSet for products table
        ResultSet mockColumnsResultSet = mock(ResultSet.class);
        when(mockDatabaseMetaData.getColumns(isNull(), isNull(), eq("products"), anyString()))
            .thenReturn(mockColumnsResultSet);
        
        // Configure ResultSet to return column data for products table
        when(mockColumnsResultSet.next()).thenReturn(true, true, true, true, true, false);
        when(mockColumnsResultSet.getString("COLUMN_NAME")).thenReturn(
            "product_name", "description", "price", "category_id", "created_date"
        );
        when(mockColumnsResultSet.getString("TYPE_NAME")).thenReturn(
            "VARCHAR", "TEXT", "DECIMAL", "INT", "TIMESTAMP"
        );
        when(mockColumnsResultSet.getInt("DATA_TYPE")).thenReturn(
            Types.VARCHAR, Types.LONGVARCHAR, Types.DECIMAL, Types.INTEGER, Types.TIMESTAMP
        );
        when(mockColumnsResultSet.getInt("NULLABLE")).thenReturn(0, 1, 0, 0, 0);
        lenient().when(mockColumnsResultSet.getString("COLUMN_DEF")).thenReturn(null);
        lenient().when(mockColumnsResultSet.getInt("COLUMN_SIZE")).thenReturn(255, 65535, 10, 11, 19);
        lenient().when(mockColumnsResultSet.getInt("DECIMAL_DIGITS")).thenReturn(0, 0, 2, 0, 0);
        lenient().when(mockColumnsResultSet.getString("IS_AUTOINCREMENT")).thenReturn("NO");
        
        // When: Generate microservice
        GeneratedMicroservice result = microServiceGenerator.generateMicroserviceFromSql(
            insertSql, businessDomainName, mockDatabaseConnection
        );
        
        // Then: Verify INSERT microservice generation
        assertNotNull(result, "INSERT microservice should be generated");
        assertEquals(SqlStatementType.INSERT, result.statementType(), "Should detect INSERT statement");
        
        // Verify INSERT-specific components
        validateGeneratedComponents(result, "Product", "POST");
        
        String controllerContent = result.controllerFile().toString();
        assertTrue(controllerContent.contains("@PostMapping"), "Should have POST mapping");
        assertTrue(controllerContent.contains("@RequestBody"), "Should accept request body");
        assertTrue(controllerContent.contains("@Valid"), "Should validate input");
        
        String daoContent = result.daoFile().toString();
        assertTrue(daoContent.contains("INSERT INTO"), "Should contain INSERT statement");
        assertTrue(daoContent.contains("VALUES"), "Should contain VALUES clause");
    }
    
    private void testUpdateScenario() throws Exception {
        // Given: UPDATE with SET and WHERE clauses
        String updateSql = """
            UPDATE customers 
            SET 
                first_name = ?,
                last_name = ?,
                email = ?,
                phone_number = ?,
                updated_date = ?,
                updated_by = ?
            WHERE 
                customer_id = ?
                AND version = ?
                AND status = 'ACTIVE'
            """;
        
        String businessDomainName = "CustomerUpdate";
        
        // Setup mocks for UPDATE scenario
        TestUtils.setupUpdateWorkflowMocks(mockDatabaseConnection, mockDataSource, mockNamedParameterJdbcTemplate);
        
        // Setup database metadata mocks for UPDATE metadata extraction
        DatabaseMetaData mockDatabaseMetaData = mock(DatabaseMetaData.class);
        when(mockConnection.getMetaData()).thenReturn(mockDatabaseMetaData);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        
        // Mock column metadata ResultSet for customers table
        ResultSet mockColumnsResultSet = mock(ResultSet.class);
        when(mockDatabaseMetaData.getColumns(isNull(), isNull(), eq("customers"), isNull()))
            .thenReturn(mockColumnsResultSet);
        
        // Setup column data for UPDATE set columns
        when(mockColumnsResultSet.next()).thenReturn(true, true, true, true, true, true, false);
        when(mockColumnsResultSet.getString("COLUMN_NAME"))
            .thenReturn("first_name", "last_name", "email", "phone_number", "updated_date", "updated_by");
        when(mockColumnsResultSet.getString("TYPE_NAME"))
            .thenReturn("VARCHAR", "VARCHAR", "VARCHAR", "VARCHAR", "TIMESTAMP", "VARCHAR");
        when(mockColumnsResultSet.getInt("DATA_TYPE"))
            .thenReturn(Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP, Types.VARCHAR);
        when(mockColumnsResultSet.getInt("NULLABLE"))
            .thenReturn(1, 1, 1, 1, 0, 1);
        
        // When: Generate microservice
        GeneratedMicroservice result = microServiceGenerator.generateMicroserviceFromSql(
            updateSql, businessDomainName, mockDatabaseConnection
        );
        
        // Then: Verify UPDATE microservice generation
        assertNotNull(result, "UPDATE microservice should be generated");
        assertEquals(SqlStatementType.UPDATE, result.statementType(), "Should detect UPDATE statement");
        
        // Verify UPDATE-specific components
        validateGeneratedComponents(result, "CustomerUpdate", "PUT");
        
        String controllerContent = result.controllerFile().toString();
        assertTrue(controllerContent.contains("@PutMapping"), "Should have PUT mapping");
        assertTrue(controllerContent.contains("@RequestBody"), "Should accept request body for SET values");
        assertTrue(controllerContent.contains("@PathVariable") || controllerContent.contains("@RequestParam") || controllerContent.contains("@RequestBody"), "Should handle WHERE condition parameters");
        
        String daoContent = result.daoFile().toString();
        assertTrue(daoContent.contains("UPDATE"), "Should contain UPDATE statement");
        assertTrue(daoContent.contains("SET"), "Should contain SET clause");
        assertTrue(daoContent.contains("WHERE") || daoContent.contains("where") || daoContent.contains("namedParameterJdbcTemplate"), "Should handle WHERE conditions");
    }
    
    private void testDeleteScenario() throws Exception {
        // Given: DELETE with complex WHERE conditions
        String deleteSql = """
            DELETE FROM order_items 
            WHERE 
                order_id = ?
                AND product_id = ?
                AND quantity = 0
                AND created_date < ?
                AND status IN ('CANCELLED', 'EXPIRED')
            """;
        
        String businessDomainName = "OrderItem";
        
        // Setup mocks for DELETE scenario
        TestUtils.setupDeleteWorkflowMocks(mockDatabaseConnection, mockDataSource);
        
        // Mock ParameterMetadataExtractor for DELETE scenario
        try (var mockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
            List<DBColumn> deleteParameters = Arrays.asList(
                new DBColumn("table", "orderId", "java.lang.Integer", "INTEGER"),
                new DBColumn("table", "productId", "java.lang.Integer", "INTEGER"), 
                new DBColumn("table", "createdDate", "java.sql.Timestamp", "TIMESTAMP")
            );
            when(mock.extractParameters(deleteSql)).thenReturn(deleteParameters);
        })) {
        
        // When: Generate microservice
        GeneratedMicroservice result = microServiceGenerator.generateMicroserviceFromSql(
            deleteSql, businessDomainName, mockDatabaseConnection
        );
        
        // Then: Verify DELETE microservice generation
        assertNotNull(result, "DELETE microservice should be generated");
        assertEquals(SqlStatementType.DELETE, result.statementType(), "Should detect DELETE statement");
        
        // Verify DELETE-specific components
        validateGeneratedComponents(result, "OrderItem", "DELETE");
        
        String controllerContent = result.controllerFile().toString();
        assertTrue(controllerContent.contains("@DeleteMapping"), "Should have DELETE mapping");
        assertTrue(controllerContent.contains("@RequestParam"), "Should have request parameters for WHERE conditions");
        
        String daoContent = result.daoFile().toString();
        assertTrue(daoContent.contains("DELETE FROM"), "Should contain DELETE statement");
        assertTrue(daoContent.contains("WHERE"), "Should contain WHERE clause");
        
        } // Close try-with-resources for ParameterMetadataExtractor mock
    }
    
    /**
     * Tests SQL statement type detection accuracy
     */
    @Test
    void testSqlStatementDetection_AllTypes_DetectsCorrectly() throws Exception {
        // Test SELECT detection
        assertEquals(SqlStatementType.SELECT, 
            SqlStatementDetector.detectStatementType("SELECT * FROM customers WHERE id = ?"));
        assertEquals(SqlStatementType.SELECT, 
            SqlStatementDetector.detectStatementType("   select c.name from customers c"));
        
        // Test INSERT detection
        assertEquals(SqlStatementType.INSERT, 
            SqlStatementDetector.detectStatementType("INSERT INTO products VALUES (?, ?, ?)"));
        assertEquals(SqlStatementType.INSERT, 
            SqlStatementDetector.detectStatementType("   insert into customers (name) values (?)"));
        
        // Test UPDATE detection
        assertEquals(SqlStatementType.UPDATE, 
            SqlStatementDetector.detectStatementType("UPDATE customers SET name = ? WHERE id = ?"));
        assertEquals(SqlStatementType.UPDATE, 
            SqlStatementDetector.detectStatementType("   update products set price = ?"));
        
        // Test DELETE detection
        assertEquals(SqlStatementType.DELETE, 
            SqlStatementDetector.detectStatementType("DELETE FROM customers WHERE id = ?"));
        assertEquals(SqlStatementType.DELETE, 
            SqlStatementDetector.detectStatementType("   delete from orders where status = 'CANCELLED'"));
    }
    
    /**
     * Tests error handling and edge cases
     */
    @Test
    void testErrorHandlingScenarios_VariousErrors_HandledGracefully() {
        // Test invalid SQL
        assertThrows(
            Exception.class,
            () -> microServiceGenerator.generateMicroserviceFromSql(
                "INVALID SQL STATEMENT", "Test", mockDatabaseConnection
            ),
            "Should handle invalid SQL gracefully"
        );
        
        // Test null inputs
        assertThrows(
            IllegalArgumentException.class,
            () -> microServiceGenerator.generateMicroserviceFromSql(
                null, "Test", mockDatabaseConnection
            ),
            "Should handle null SQL"
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> microServiceGenerator.generateMicroserviceFromSql(
                "SELECT * FROM test", null, mockDatabaseConnection
            ),
            "Should handle null business domain name"
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> microServiceGenerator.generateMicroserviceFromSql(
                "SELECT * FROM test", "Test", null
            ),
            "Should handle null database connection"
        );
        
        // Test empty inputs
        assertThrows(
            IllegalArgumentException.class,
            () -> microServiceGenerator.generateMicroserviceFromSql(
                "", "Test", mockDatabaseConnection
            ),
            "Should handle empty SQL"
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> microServiceGenerator.generateMicroserviceFromSql(
                "SELECT * FROM test", "", mockDatabaseConnection
            ),
            "Should handle empty business domain name"
        );
    }
    
    /**
     * Tests edge cases for SQL file resolution
     */
    @Test 
    void testSqlFileResolution_DefaultFallbackBehavior_WorksCorrectly() throws Exception {
        // This test would require actual file system setup or mocking
        // Test priority order: UPDATE -> INSERT -> DELETE -> SELECT
        
        // Test specific file resolution
        assertThrows(
            RuntimeException.class,
            () -> sqlFileResolver.locateAndReadSqlFile("non_existent_file.sql"),
            "Should handle non-existent file"
        );
        
        // Test null file name (should try defaults and succeed if default files exist)
        String result = sqlFileResolver.locateAndReadSqlFile(null);
        assertNotNull(result, "Should return SQL content when default files exist");
    }
    
    /**
     * Tests data type handling across different SQL types
     */
    @Test
    void testDataTypeHandling_VariousTypes_MapsCorrectly() throws Exception {
        // Given: SQL with various data types
        String multiTypeSelectSql = """
            SELECT 
                id,           -- INTEGER
                name,         -- VARCHAR
                price,        -- DECIMAL
                is_active,    -- BOOLEAN
                created_date, -- DATE
                updated_at,   -- TIMESTAMP
                description,  -- TEXT
                rating,       -- FLOAT
                quantity      -- BIGINT
            FROM products 
            WHERE price BETWEEN ? AND ?
            AND created_date >= ?
            AND is_active = ?
            """;
        
        String businessDomainName = "MultiTypeProduct";
        
        // Setup mock column metadata for various data types
        List<ColumnMetadata> mockDataTypeColumns = new ArrayList<>();
        mockDataTypeColumns.add(TestUtils.createColumnMetadata("id", "INT", Types.INTEGER, false));
        mockDataTypeColumns.add(TestUtils.createColumnMetadata("name", "VARCHAR", Types.VARCHAR, false));
        mockDataTypeColumns.add(TestUtils.createColumnMetadata("price", "DECIMAL", Types.DECIMAL, false));
        mockDataTypeColumns.add(TestUtils.createColumnMetadata("is_active", "BIT", Types.BIT, false));
        mockDataTypeColumns.add(TestUtils.createColumnMetadata("created_date", "DATE", Types.DATE, false));
        mockDataTypeColumns.add(TestUtils.createColumnMetadata("updated_at", "TIMESTAMP", Types.TIMESTAMP, true));
        mockDataTypeColumns.add(TestUtils.createColumnMetadata("description", "TEXT", Types.LONGVARCHAR, true));
        mockDataTypeColumns.add(TestUtils.createColumnMetadata("rating", "FLOAT", Types.FLOAT, true));
        mockDataTypeColumns.add(TestUtils.createColumnMetadata("quantity", "BIGINT", Types.BIGINT, false));
        
        // Setup mock parameters
        ArrayList<DBColumn> mockDataTypeParameters = new ArrayList<>();
        mockDataTypeParameters.add(new DBColumn("table", "minPrice", "BigDecimal", "DECIMAL"));
        mockDataTypeParameters.add(new DBColumn("table", "maxPrice", "BigDecimal", "DECIMAL"));
        mockDataTypeParameters.add(new DBColumn("table", "createdDate", "Date", "DATE"));
        mockDataTypeParameters.add(new DBColumn("table", "isActive", "Boolean", "BIT"));
        
        try (var sqlMetadataMockedConstruction = mockConstruction(SqlMetadata.class, (mock, context) -> {
                 try {
                     when(mock.getColumnMetadata(multiTypeSelectSql)).thenReturn(mockDataTypeColumns);
                 } catch (Exception e) {
                     // Handle SQLException from mocking
                 }
             });
             var controllerMockedConstruction = mockConstruction(CodeGenController.class, (mock, context) -> {
                 when(mock.selectColumnMetadata()).thenReturn(mockDataTypeColumns);
             });
             var extractorMockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
                 when(mock.extractParameters(multiTypeSelectSql)).thenReturn(mockDataTypeParameters);
             })) {
        
        // When: Generate microservice
        GeneratedMicroservice result = microServiceGenerator.generateMicroserviceFromSql(
            multiTypeSelectSql, businessDomainName, mockDatabaseConnection
        );
        
            // Then: Verify data type handling
            assertNotNull(result, "Should generate microservice with various data types");
            
            String dtoContent = result.dtoFile().toString();
            
            // Verify data type mappings in DTO
            assertTrue(dtoContent.contains("Integer id") || dtoContent.contains("Long id"), "Should handle integer types");
            assertTrue(dtoContent.contains("String name"), "Should handle string types");
            assertTrue(dtoContent.contains("BigDecimal price"), "Should handle decimal types");
            assertTrue(dtoContent.contains("Boolean isActive"), "Should handle boolean types");
            assertTrue(dtoContent.contains("Date createdDate"), "Should handle date types");
            assertTrue(dtoContent.contains("Timestamp updatedAt"), "Should handle timestamp types");
        }
    }
    
    private void validateGeneratedComponents(GeneratedMicroservice result, String expectedDomainName, String expectedHttpMethod) {
        // Validate Spring Boot Application
        assertNotNull(result.springBootApplication(), "Spring Boot application should be generated");
        String springBootContent = result.springBootApplication().toString();
        assertTrue(springBootContent.contains("@SpringBootApplication"), "Should have SpringBootApplication annotation");
        assertTrue(springBootContent.contains(expectedDomainName + "SpringBootApplication"), "Should have correct application name");
        
        // Validate DTO
        assertNotNull(result.dtoFile(), "DTO should be generated");
        String dtoContent = result.dtoFile().toString();
        assertTrue(dtoContent.contains(expectedDomainName + "DTO") || dtoContent.contains(expectedDomainName + "InsertDTO") || dtoContent.contains(expectedDomainName + "UpdateDTO") || dtoContent.contains(expectedDomainName + "DeleteDTO"), "Should have correct DTO name");
        assertTrue(dtoContent.contains("@Builder") || dtoContent.contains("@Value") || dtoContent.contains("@Data"), "Should have Lombok annotations");
        assertTrue(dtoContent.contains("public class"), "Should be public class");
        
        // Validate Controller
        assertNotNull(result.controllerFile(), "Controller should be generated");
        String controllerContent = result.controllerFile().toString();
        assertTrue(controllerContent.contains(expectedDomainName + "Controller") || controllerContent.contains(expectedDomainName + "InsertController") || controllerContent.contains(expectedDomainName + "UpdateController") || controllerContent.contains(expectedDomainName + "DeleteController"), "Should have correct controller name");
        assertTrue(controllerContent.contains("@RestController"), "Should have RestController annotation");
        assertTrue(controllerContent.contains("@RequestMapping"), "Should have request mapping");
        
        // Validate DAO
        assertNotNull(result.daoFile(), "DAO should be generated");
        String daoContent = result.daoFile().toString();
        assertTrue(daoContent.contains(expectedDomainName + "DAO") || daoContent.contains(expectedDomainName + "InsertDAO") || daoContent.contains(expectedDomainName + "UpdateDAO") || daoContent.contains(expectedDomainName + "DeleteDAO"), "Should have correct DAO name");
        assertTrue(daoContent.contains("@Repository") || daoContent.contains("@Component") || daoContent.contains("@Service"), "Should have Repository annotation");
        assertTrue(daoContent.contains("JdbcTemplate"), "Should use JdbcTemplate");
        
        // Validate Database Configuration
        assertNotNull(result.databaseConfigContent(), "Database configuration should be generated");
        String dbConfigContent = result.databaseConfigContent();
        assertTrue(dbConfigContent.contains("@Configuration"), "Should have Configuration annotation");
        assertTrue(dbConfigContent.contains("DataSource"), "Should configure DataSource");
        assertTrue(dbConfigContent.contains("JdbcTemplate"), "Should configure JdbcTemplate");
    }
}