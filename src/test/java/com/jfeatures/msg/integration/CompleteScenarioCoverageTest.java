package com.jfeatures.msg.integration;

import com.jfeatures.msg.codegen.MicroServiceGenerator;
import com.jfeatures.msg.codegen.constants.ProjectConstants;
import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.codegen.domain.GeneratedMicroservice;
import com.jfeatures.msg.codegen.sql.SqlFileResolver;
import com.jfeatures.msg.codegen.util.SqlStatementDetector;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import com.jfeatures.msg.test.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive end-to-end test covering all major scenarios and edge cases
 * for the MSG (Microservice Generator) system.
 */
@ExtendWith(MockitoExtension.class)
class CompleteScenarioCoverageTest {

    private MicroServiceGenerator microServiceGenerator;
    private SqlFileResolver sqlFileResolver;
    private SqlStatementDetector sqlStatementDetector;
    
    private DataSource mockDataSource;
    private JdbcTemplate mockJdbcTemplate;
    private NamedParameterJdbcTemplate mockNamedParameterJdbcTemplate;
    private DatabaseConnection mockDatabaseConnection;
    
    @BeforeEach
    void setUp() {
        microServiceGenerator = new MicroServiceGenerator();
        sqlFileResolver = new SqlFileResolver();
        sqlStatementDetector = new SqlStatementDetector();
        
        mockDataSource = mock(DataSource.class);
        mockJdbcTemplate = mock(JdbcTemplate.class);
        mockNamedParameterJdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        mockDatabaseConnection = mock(DatabaseConnection.class);
        
        when(mockDatabaseConnection.dataSource()).thenReturn(mockDataSource);
        when(mockDatabaseConnection.jdbcTemplate()).thenReturn(mockJdbcTemplate);
        when(mockDatabaseConnection.namedParameterJdbcTemplate()).thenReturn(mockNamedParameterJdbcTemplate);
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
        
        // Setup comprehensive mocks for SELECT scenario
        TestUtils.setupComplexSelectWorkflowMocks(mockDatabaseConnection, mockJdbcTemplate);
        
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
        assertTrue(controllerContent.contains("@PathVariable"), "Should have path variables for WHERE conditions");
        
        String daoContent = result.daoFile().toString();
        assertTrue(daoContent.contains("UPDATE"), "Should contain UPDATE statement");
        assertTrue(daoContent.contains("SET"), "Should contain SET clause");
        assertTrue(daoContent.contains("WHERE"), "Should contain WHERE clause");
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
        assertThrows(Exception.class, () -> {
            microServiceGenerator.generateMicroserviceFromSql(
                "INVALID SQL STATEMENT", "Test", mockDatabaseConnection
            );
        }, "Should handle invalid SQL gracefully");
        
        // Test null inputs
        assertThrows(IllegalArgumentException.class, () -> {
            microServiceGenerator.generateMicroserviceFromSql(
                null, "Test", mockDatabaseConnection
            );
        }, "Should handle null SQL");
        
        assertThrows(IllegalArgumentException.class, () -> {
            microServiceGenerator.generateMicroserviceFromSql(
                "SELECT * FROM test", null, mockDatabaseConnection
            );
        }, "Should handle null business domain name");
        
        assertThrows(IllegalArgumentException.class, () -> {
            microServiceGenerator.generateMicroserviceFromSql(
                "SELECT * FROM test", "Test", null
            );
        }, "Should handle null database connection");
        
        // Test empty inputs
        assertThrows(IllegalArgumentException.class, () -> {
            microServiceGenerator.generateMicroserviceFromSql(
                "", "Test", mockDatabaseConnection
            );
        }, "Should handle empty SQL");
        
        assertThrows(IllegalArgumentException.class, () -> {
            microServiceGenerator.generateMicroserviceFromSql(
                "SELECT * FROM test", "", mockDatabaseConnection
            );
        }, "Should handle empty business domain name");
    }
    
    /**
     * Tests edge cases for SQL file resolution
     */
    @Test 
    void testSqlFileResolution_DefaultFallbackBehavior_WorksCorrectly() throws Exception {
        // This test would require actual file system setup or mocking
        // Test priority order: UPDATE -> INSERT -> DELETE -> SELECT
        
        // Test specific file resolution
        assertThrows(Exception.class, () -> {
            sqlFileResolver.locateAndReadSqlFile("non_existent_file.sql");
        }, "Should handle non-existent file");
        
        // Test null file name (should try defaults)
        assertThrows(Exception.class, () -> {
            sqlFileResolver.locateAndReadSqlFile(null);
        }, "Should try default files when no specific file provided");
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
        
        // Setup mocks for various data types
        TestUtils.setupMultipleDataTypesWorkflowMocks(mockDatabaseConnection, mockJdbcTemplate);
        
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
    
    private void validateGeneratedComponents(GeneratedMicroservice result, String expectedDomainName, String expectedHttpMethod) {
        // Validate Spring Boot Application
        assertNotNull(result.springBootApplication(), "Spring Boot application should be generated");
        String springBootContent = result.springBootApplication().toString();
        assertTrue(springBootContent.contains("@SpringBootApplication"), "Should have SpringBootApplication annotation");
        assertTrue(springBootContent.contains(expectedDomainName + "Application"), "Should have correct application name");
        
        // Validate DTO
        assertNotNull(result.dtoFile(), "DTO should be generated");
        String dtoContent = result.dtoFile().toString();
        assertTrue(dtoContent.contains(expectedDomainName + "DTO"), "Should have correct DTO name");
        assertTrue(dtoContent.contains("@Data"), "Should have Lombok annotations");
        assertTrue(dtoContent.contains("public class"), "Should be public class");
        
        // Validate Controller
        assertNotNull(result.controllerFile(), "Controller should be generated");
        String controllerContent = result.controllerFile().toString();
        assertTrue(controllerContent.contains(expectedDomainName + "Controller"), "Should have correct controller name");
        assertTrue(controllerContent.contains("@RestController"), "Should have RestController annotation");
        assertTrue(controllerContent.contains("@RequestMapping"), "Should have request mapping");
        
        // Validate DAO
        assertNotNull(result.daoFile(), "DAO should be generated");
        String daoContent = result.daoFile().toString();
        assertTrue(daoContent.contains(expectedDomainName + "DAO"), "Should have correct DAO name");
        assertTrue(daoContent.contains("@Repository"), "Should have Repository annotation");
        assertTrue(daoContent.contains("JdbcTemplate"), "Should use JdbcTemplate");
        
        // Validate Database Configuration
        assertNotNull(result.databaseConfigContent(), "Database configuration should be generated");
        String dbConfigContent = result.databaseConfigContent();
        assertTrue(dbConfigContent.contains("@Configuration"), "Should have Configuration annotation");
        assertTrue(dbConfigContent.contains("DataSource"), "Should configure DataSource");
        assertTrue(dbConfigContent.contains("JdbcTemplate"), "Should configure JdbcTemplate");
    }
}