package com.jfeatures.msg.integration;

import com.jfeatures.msg.codegen.MicroServiceGenerator;
import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.codegen.domain.GeneratedMicroservice;
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

@ExtendWith(MockitoExtension.class)
class ComprehensiveCRUDWorkflowTest {

    private MicroServiceGenerator microServiceGenerator;
    private DataSource mockDataSource;
    private JdbcTemplate mockJdbcTemplate;
    private NamedParameterJdbcTemplate mockNamedParameterJdbcTemplate;
    private DatabaseConnection mockDatabaseConnection;
    
    @BeforeEach
    void setUp() {
        microServiceGenerator = new MicroServiceGenerator();
        
        mockDataSource = mock(DataSource.class);
        mockJdbcTemplate = mock(JdbcTemplate.class);
        mockNamedParameterJdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        mockDatabaseConnection = mock(DatabaseConnection.class);
        
        when(mockDatabaseConnection.dataSource()).thenReturn(mockDataSource);
        when(mockDatabaseConnection.jdbcTemplate()).thenReturn(mockJdbcTemplate);
        when(mockDatabaseConnection.namedParameterJdbcTemplate()).thenReturn(mockNamedParameterJdbcTemplate);
    }
    
    @Test
    void testCompleteSelectWorkflow_ValidSqlFile_GeneratesCompleteService() throws Exception {
        // Given
        String businessDomainName = "Customer";
        String selectSql = """
            SELECT customer_id, customer_name, email, phone, address, city, country
            FROM customers 
            WHERE customer_id = ? AND status = ? AND created_date > ?
            """;
        
        // Mock SQL statement detection
        SqlStatementDetector detector = spy(new SqlStatementDetector());
        when(detector.detectSqlStatementType(selectSql)).thenReturn(SqlStatementType.SELECT);
        
        // Setup database mocks for SELECT workflow
        TestUtils.setupSelectWorkflowMocks(mockDatabaseConnection, mockJdbcTemplate);
        
        // When
        GeneratedMicroservice result = microServiceGenerator.generateMicroserviceFromSql(
            selectSql, businessDomainName, mockDatabaseConnection
        );
        
        // Then
        assertNotNull(result);
        assertEquals(businessDomainName, result.businessDomainName());
        assertEquals(SqlStatementType.SELECT, result.sqlStatementType());
        
        // Verify all components are generated
        assertNotNull(result.springBootApplication(), "Spring Boot application should be generated");
        assertNotNull(result.dtoFile(), "DTO file should be generated");
        assertNotNull(result.controllerFile(), "Controller file should be generated");
        assertNotNull(result.daoFile(), "DAO file should be generated");
        assertNotNull(result.databaseConfigContent(), "Database config should be generated");
        
        // Verify the generated files contain expected content
        String dtoContent = result.dtoFile().toString();
        assertTrue(dtoContent.contains("CustomerDTO"), "DTO should have correct class name");
        assertTrue(dtoContent.contains("customerId"), "DTO should contain customerId field");
        assertTrue(dtoContent.contains("customerName"), "DTO should contain customerName field");
        assertTrue(dtoContent.contains("email"), "DTO should contain email field");
        
        String controllerContent = result.controllerFile().toString();
        assertTrue(controllerContent.contains("CustomerController"), "Controller should have correct class name");
        assertTrue(controllerContent.contains("@RestController"), "Controller should have REST annotations");
        assertTrue(controllerContent.contains("@GetMapping"), "Controller should have GET mapping");
        
        String daoContent = result.daoFile().toString();
        assertTrue(daoContent.contains("CustomerDAO"), "DAO should have correct class name");
        assertTrue(daoContent.contains("@Repository"), "DAO should have repository annotation");
    }
    
    @Test
    void testCompleteInsertWorkflow_ValidSqlFile_GeneratesCompleteService() throws Exception {
        // Given
        String businessDomainName = "Product";
        String insertSql = """
            INSERT INTO products (product_name, description, price, category_id, created_date)
            VALUES (?, ?, ?, ?, ?)
            """;
        
        // Setup database mocks for INSERT workflow
        TestUtils.setupInsertWorkflowMocks(mockDatabaseConnection, mockDataSource, mockNamedParameterJdbcTemplate);
        
        // When
        GeneratedMicroservice result = microServiceGenerator.generateMicroserviceFromSql(
            insertSql, businessDomainName, mockDatabaseConnection
        );
        
        // Then
        assertNotNull(result);
        assertEquals(businessDomainName, result.businessDomainName());
        assertEquals(SqlStatementType.INSERT, result.sqlStatementType());
        
        // Verify all components are generated
        assertNotNull(result.springBootApplication(), "Spring Boot application should be generated");
        assertNotNull(result.dtoFile(), "DTO file should be generated");
        assertNotNull(result.controllerFile(), "Controller file should be generated");
        assertNotNull(result.daoFile(), "DAO file should be generated");
        assertNotNull(result.databaseConfigContent(), "Database config should be generated");
        
        // Verify INSERT-specific content
        String dtoContent = result.dtoFile().toString();
        assertTrue(dtoContent.contains("ProductDTO"), "DTO should have correct class name");
        assertTrue(dtoContent.contains("productName"), "DTO should contain productName field");
        assertTrue(dtoContent.contains("description"), "DTO should contain description field");
        assertTrue(dtoContent.contains("price"), "DTO should contain price field");
        
        String controllerContent = result.controllerFile().toString();
        assertTrue(controllerContent.contains("ProductController"), "Controller should have correct class name");
        assertTrue(controllerContent.contains("@PostMapping"), "Controller should have POST mapping for INSERT");
        assertTrue(controllerContent.contains("@RequestBody"), "Controller should accept request body");
        
        String daoContent = result.daoFile().toString();
        assertTrue(daoContent.contains("ProductDAO"), "DAO should have correct class name");
        assertTrue(daoContent.contains("insert"), "DAO should have insert method");
    }
    
    @Test
    void testCompleteUpdateWorkflow_ValidSqlFile_GeneratesCompleteService() throws Exception {
        // Given
        String businessDomainName = "Order";
        String updateSql = """
            UPDATE orders 
            SET order_status = ?, updated_date = ?, notes = ?
            WHERE order_id = ? AND customer_id = ?
            """;
        
        // Setup database mocks for UPDATE workflow
        TestUtils.setupUpdateWorkflowMocks(mockDatabaseConnection, mockDataSource, mockNamedParameterJdbcTemplate);
        
        // When
        GeneratedMicroservice result = microServiceGenerator.generateMicroserviceFromSql(
            updateSql, businessDomainName, mockDatabaseConnection
        );
        
        // Then
        assertNotNull(result);
        assertEquals(businessDomainName, result.businessDomainName());
        assertEquals(SqlStatementType.UPDATE, result.sqlStatementType());
        
        // Verify all components are generated
        assertNotNull(result.springBootApplication(), "Spring Boot application should be generated");
        assertNotNull(result.dtoFile(), "DTO file should be generated");
        assertNotNull(result.controllerFile(), "Controller file should be generated");
        assertNotNull(result.daoFile(), "DAO file should be generated");
        assertNotNull(result.databaseConfigContent(), "Database config should be generated");
        
        // Verify UPDATE-specific content
        String dtoContent = result.dtoFile().toString();
        assertTrue(dtoContent.contains("OrderDTO"), "DTO should have correct class name");
        assertTrue(dtoContent.contains("orderStatus"), "DTO should contain orderStatus field");
        assertTrue(dtoContent.contains("updatedDate"), "DTO should contain updatedDate field");
        
        String controllerContent = result.controllerFile().toString();
        assertTrue(controllerContent.contains("OrderController"), "Controller should have correct class name");
        assertTrue(controllerContent.contains("@PutMapping"), "Controller should have PUT mapping for UPDATE");
        assertTrue(controllerContent.contains("@RequestBody"), "Controller should accept request body");
        
        String daoContent = result.daoFile().toString();
        assertTrue(daoContent.contains("OrderDAO"), "DAO should have correct class name");
        assertTrue(daoContent.contains("update"), "DAO should have update method");
    }
    
    @Test
    void testCompleteDeleteWorkflow_ValidSqlFile_GeneratesCompleteService() throws Exception {
        // Given
        String businessDomainName = "User";
        String deleteSql = """
            DELETE FROM users 
            WHERE user_id = ? AND status = 'INACTIVE' AND last_login < ?
            """;
        
        // Setup database mocks for DELETE workflow
        TestUtils.setupDeleteWorkflowMocks(mockDatabaseConnection, mockDataSource);
        
        // When
        GeneratedMicroservice result = microServiceGenerator.generateMicroserviceFromSql(
            deleteSql, businessDomainName, mockDatabaseConnection
        );
        
        // Then
        assertNotNull(result);
        assertEquals(businessDomainName, result.businessDomainName());
        assertEquals(SqlStatementType.DELETE, result.sqlStatementType());
        
        // Verify all components are generated
        assertNotNull(result.springBootApplication(), "Spring Boot application should be generated");
        assertNotNull(result.dtoFile(), "DTO file should be generated");
        assertNotNull(result.controllerFile(), "Controller file should be generated");
        assertNotNull(result.daoFile(), "DAO file should be generated");
        assertNotNull(result.databaseConfigContent(), "Database config should be generated");
        
        // Verify DELETE-specific content
        String dtoContent = result.dtoFile().toString();
        assertTrue(dtoContent.contains("UserDTO"), "DTO should have correct class name");
        assertTrue(dtoContent.contains("userId"), "DTO should contain userId field");
        
        String controllerContent = result.controllerFile().toString();
        assertTrue(controllerContent.contains("UserController"), "Controller should have correct class name");
        assertTrue(controllerContent.contains("@DeleteMapping"), "Controller should have DELETE mapping");
        
        String daoContent = result.daoFile().toString();
        assertTrue(daoContent.contains("UserDAO"), "DAO should have correct class name");
        assertTrue(daoContent.contains("delete"), "DAO should have delete method");
    }
    
    @Test
    void testComplexSelectWithJoinsWorkflow_GeneratesCompleteService() throws Exception {
        // Given
        String businessDomainName = "CustomerOrder";
        String complexSelectSql = """
            SELECT 
                c.customer_id, c.customer_name, c.email,
                o.order_id, o.order_date, o.total_amount,
                oi.item_id, oi.product_name, oi.quantity, oi.unit_price
            FROM customers c
            JOIN orders o ON c.customer_id = o.customer_id
            JOIN order_items oi ON o.order_id = oi.order_id
            WHERE c.customer_id = ? 
            AND o.order_date >= ? 
            AND o.order_status = ?
            AND oi.quantity > ?
            ORDER BY o.order_date DESC, oi.item_id ASC
            """;
        
        // Setup database mocks for complex SELECT workflow
        TestUtils.setupComplexSelectWorkflowMocks(mockDatabaseConnection, mockJdbcTemplate);
        
        // When
        GeneratedMicroservice result = microServiceGenerator.generateMicroserviceFromSql(
            complexSelectSql, businessDomainName, mockDatabaseConnection
        );
        
        // Then
        assertNotNull(result);
        assertEquals(businessDomainName, result.businessDomainName());
        assertEquals(SqlStatementType.SELECT, result.sqlStatementType());
        
        // Verify all components are generated for complex query
        assertNotNull(result.springBootApplication());
        assertNotNull(result.dtoFile());
        assertNotNull(result.controllerFile());
        assertNotNull(result.daoFile());
        assertNotNull(result.databaseConfigContent());
        
        // Verify complex DTO contains fields from multiple tables
        String dtoContent = result.dtoFile().toString();
        assertTrue(dtoContent.contains("CustomerOrderDTO"), "DTO should have correct class name");
        assertTrue(dtoContent.contains("customerId"), "DTO should contain customer fields");
        assertTrue(dtoContent.contains("orderId"), "DTO should contain order fields");
        assertTrue(dtoContent.contains("itemId"), "DTO should contain item fields");
        
        // Verify complex controller handles multiple parameters
        String controllerContent = result.controllerFile().toString();
        assertTrue(controllerContent.contains("CustomerOrderController"));
        assertTrue(controllerContent.contains("@GetMapping"));
        assertTrue(controllerContent.contains("customerId"));
        assertTrue(controllerContent.contains("orderDate"));
        assertTrue(controllerContent.contains("orderStatus"));
        assertTrue(controllerContent.contains("quantity"));
        
        // Verify DAO handles complex query
        String daoContent = result.daoFile().toString();
        assertTrue(daoContent.contains("CustomerOrderDAO"));
        assertTrue(daoContent.contains("JOIN"));
    }
    
    @Test
    void testWorkflowWithDifferentDataTypes_HandlesAllTypes() throws Exception {
        // Given
        String businessDomainName = "DataType";
        String selectSql = """
            SELECT 
                id, name, description, price, quantity, 
                is_active, created_date, updated_timestamp, 
                category_code, discount_rate, image_data
            FROM products 
            WHERE price BETWEEN ? AND ? 
            AND created_date >= ? 
            AND is_active = ?
            """;
        
        // Setup database mocks with various data types
        TestUtils.setupMultipleDataTypesWorkflowMocks(mockDatabaseConnection, mockJdbcTemplate);
        
        // When
        GeneratedMicroservice result = microServiceGenerator.generateMicroserviceFromSql(
            selectSql, businessDomainName, mockDatabaseConnection
        );
        
        // Then
        assertNotNull(result);
        assertEquals(SqlStatementType.SELECT, result.sqlStatementType());
        
        // Verify DTO handles various data types correctly
        String dtoContent = result.dtoFile().toString();
        assertTrue(dtoContent.contains("DataTypeDTO"));
        assertTrue(dtoContent.contains("Integer id"), "Should handle integer type");
        assertTrue(dtoContent.contains("String name"), "Should handle string type");
        assertTrue(dtoContent.contains("BigDecimal price"), "Should handle decimal type");
        assertTrue(dtoContent.contains("Boolean isActive"), "Should handle boolean type");
        assertTrue(dtoContent.contains("Date createdDate"), "Should handle date type");
        assertTrue(dtoContent.contains("Timestamp updatedTimestamp"), "Should handle timestamp type");
        
        // Verify controller parameter handling for different types
        String controllerContent = result.controllerFile().toString();
        assertTrue(controllerContent.contains("@RequestParam"));
        assertTrue(controllerContent.contains("BigDecimal"));
        assertTrue(controllerContent.contains("Date"));
        assertTrue(controllerContent.contains("Boolean"));
    }
    
    @Test
    void testEndToEndWorkflow_FollowsAllSteps() throws Exception {
        // Given
        String businessDomainName = "Integration";
        String selectSql = "SELECT id, name FROM test_table WHERE id = ?";
        
        // Setup complete workflow mocks
        TestUtils.setupCompleteWorkflowMocks(mockDatabaseConnection, mockJdbcTemplate, mockDataSource);
        
        // When - Test the complete end-to-end workflow
        GeneratedMicroservice result = microServiceGenerator.generateMicroserviceFromSql(
            selectSql, businessDomainName, mockDatabaseConnection
        );
        
        // Then - Verify each step of the workflow was executed
        assertNotNull(result);
        
        // 1. SQL Statement Detection
        assertEquals(SqlStatementType.SELECT, result.sqlStatementType());
        
        // 2. Metadata Extraction
        assertNotNull(result.dtoFile());
        String dtoContent = result.dtoFile().toString();
        assertTrue(dtoContent.contains("IntegrationDTO"));
        
        // 3. Code Generation
        assertNotNull(result.springBootApplication());
        assertNotNull(result.controllerFile());
        assertNotNull(result.daoFile());
        assertNotNull(result.databaseConfigContent());
        
        // 4. Verify generated content quality
        String springBootContent = result.springBootApplication().toString();
        assertTrue(springBootContent.contains("@SpringBootApplication"));
        assertTrue(springBootContent.contains("IntegrationApplication"));
        
        String controllerContent = result.controllerFile().toString();
        assertTrue(controllerContent.contains("@RestController"));
        assertTrue(controllerContent.contains("@RequestMapping"));
        
        String daoContent = result.daoFile().toString();
        assertTrue(daoContent.contains("@Repository"));
        assertTrue(daoContent.contains("JdbcTemplate"));
        
        // 5. Verify database configuration
        assertTrue(result.databaseConfigContent().contains("DataSource"));
        assertTrue(result.databaseConfigContent().contains("JdbcTemplate"));
    }
    
    @Test
    void testWorkflowErrorHandling_InvalidSql_HandlesGracefully() {
        // Given
        String businessDomainName = "ErrorTest";
        String invalidSql = "INVALID SQL STATEMENT";
        
        // When & Then - Should handle invalid SQL gracefully
        assertThrows(Exception.class, () -> {
            microServiceGenerator.generateMicroserviceFromSql(
                invalidSql, businessDomainName, mockDatabaseConnection
            );
        });
    }
    
    @Test
    void testWorkflowErrorHandling_NullInputs_HandlesGracefully() {
        // Given
        String businessDomainName = "ErrorTest";
        String validSql = "SELECT * FROM test";
        
        // When & Then - Should handle null inputs gracefully
        assertThrows(IllegalArgumentException.class, () -> {
            microServiceGenerator.generateMicroserviceFromSql(
                null, businessDomainName, mockDatabaseConnection
            );
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            microServiceGenerator.generateMicroserviceFromSql(
                validSql, null, mockDatabaseConnection
            );
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            microServiceGenerator.generateMicroserviceFromSql(
                validSql, businessDomainName, null
            );
        });
    }
}