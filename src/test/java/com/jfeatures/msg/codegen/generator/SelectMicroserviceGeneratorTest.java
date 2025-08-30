package com.jfeatures.msg.codegen.generator;

import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.codegen.domain.GeneratedMicroservice;
import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.SqlMetadata;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import com.jfeatures.msg.codegen.ParameterMetadataExtractor;
import com.jfeatures.msg.controller.CodeGenController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SelectMicroserviceGeneratorTest {

    @Mock
    private DatabaseConnection databaseConnection;
    
    @Mock
    private JdbcTemplate jdbcTemplate;
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private SqlMetadata sqlMetadata;
    
    @Mock
    private CodeGenController codeGenController;
    
    @Mock
    private ParameterMetadataExtractor parameterExtractor;
    
    private SelectMicroserviceGenerator generator;
    private List<ColumnMetadata> mockColumnMetadata;
    private ArrayList<DBColumn> mockParameters;
    
    @BeforeEach
    void setUp() {
        generator = new SelectMicroserviceGenerator();
        setupMockData();
    }
    
    private void setupMockData() {
        // Setup mock column metadata
        mockColumnMetadata = List.of(
            new ColumnMetadata("customer_id", "int", 1),
            new ColumnMetadata("customer_name", "varchar", 2),
            new ColumnMetadata("email", "varchar", 3)
        );
        
        // Setup mock parameters
        mockParameters = new ArrayList<>();
        mockParameters.add(new DBColumn("table", "customerId", "java.lang.String", "int"));
        mockParameters.add(new DBColumn("table", "status", "java.lang.String", "varchar"));
        
        // Setup database connection mocks
        when(databaseConnection.jdbcTemplate()).thenReturn(jdbcTemplate);
        when(databaseConnection.dataSource()).thenReturn(dataSource);
    }
    
    @Test
    void testGenerateSelectMicroservice_ValidInput_ReturnsGeneratedMicroservice() throws Exception {
        // Given
        String sql = "SELECT customer_id, customer_name, email FROM customers WHERE customer_id = ? AND status = ?";
        String businessDomainName = "Customer";
        
        try (MockedStatic<SqlMetadata> sqlMetadataMock = mockStatic(SqlMetadata.class);
             MockedStatic<CodeGenController> controllerMock = mockStatic(CodeGenController.class);
             MockedStatic<ParameterMetadataExtractor> extractorMock = mockStatic(ParameterMetadataExtractor.class)) {
            
            // Setup static mocks
            sqlMetadataMock.when(() -> new SqlMetadata(jdbcTemplate)).thenReturn(sqlMetadata);
            controllerMock.when(() -> new CodeGenController(sqlMetadata)).thenReturn(codeGenController);
            extractorMock.when(() -> new ParameterMetadataExtractor(dataSource)).thenReturn(parameterExtractor);
            
            // Setup method returns
            when(codeGenController.selectColumnMetadata()).thenReturn(mockColumnMetadata);
            when(parameterExtractor.extractParameters(sql)).thenReturn(mockParameters);
            
            // When
            GeneratedMicroservice result = generator.generateSelectMicroservice(sql, businessDomainName, databaseConnection);
            
            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.SELECT, result.statementType());
            assertNotNull(result.springBootApplication());
            assertNotNull(result.dtoFile());
            assertNotNull(result.controllerFile());
            assertNotNull(result.daoFile());
            assertNotNull(result.databaseConfigContent());
            
            // Verify interactions
            verify(codeGenController).selectColumnMetadata();
            verify(parameterExtractor).extractParameters(sql);
        }
    }
    
    @Test
    void testGenerateSelectMicroservice_NullSql_ThrowsIllegalArgumentException() {
        // Given
        String sql = null;
        String businessDomainName = "Customer";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateSelectMicroservice(sql, businessDomainName, databaseConnection)
        );
        
        assertEquals("SQL statement cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGenerateSelectMicroservice_EmptySql_ThrowsIllegalArgumentException() {
        // Given
        String sql = "   ";
        String businessDomainName = "Customer";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateSelectMicroservice(sql, businessDomainName, databaseConnection)
        );
        
        assertEquals("SQL statement cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGenerateSelectMicroservice_NullBusinessDomainName_ThrowsIllegalArgumentException() {
        // Given
        String sql = "SELECT * FROM customers";
        String businessDomainName = null;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateSelectMicroservice(sql, businessDomainName, databaseConnection)
        );
        
        assertEquals("Business domain name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGenerateSelectMicroservice_EmptyBusinessDomainName_ThrowsIllegalArgumentException() {
        // Given
        String sql = "SELECT * FROM customers";
        String businessDomainName = "";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateSelectMicroservice(sql, businessDomainName, databaseConnection)
        );
        
        assertEquals("Business domain name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGenerateSelectMicroservice_NullDatabaseConnection_ThrowsIllegalArgumentException() {
        // Given
        String sql = "SELECT * FROM customers";
        String businessDomainName = "Customer";
        DatabaseConnection nullConnection = null;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateSelectMicroservice(sql, businessDomainName, nullConnection)
        );
        
        assertEquals("Database connection cannot be null", exception.getMessage());
    }
    
    @Test
    void testGenerateSelectMicroservice_SqlException_PropagatesException() throws Exception {
        // Given
        String sql = "SELECT * FROM customers WHERE id = ?";
        String businessDomainName = "Customer";
        SQLException sqlException = new SQLException("Database connection failed");
        
        try (MockedStatic<SqlMetadata> sqlMetadataMock = mockStatic(SqlMetadata.class);
             MockedStatic<CodeGenController> controllerMock = mockStatic(CodeGenController.class);
             MockedStatic<ParameterMetadataExtractor> extractorMock = mockStatic(ParameterMetadataExtractor.class)) {
            
            sqlMetadataMock.when(() -> new SqlMetadata(jdbcTemplate)).thenReturn(sqlMetadata);
            controllerMock.when(() -> new CodeGenController(sqlMetadata)).thenReturn(codeGenController);
            extractorMock.when(() -> new ParameterMetadataExtractor(dataSource)).thenReturn(parameterExtractor);
            
            when(codeGenController.selectColumnMetadata()).thenReturn(mockColumnMetadata);
            when(parameterExtractor.extractParameters(sql)).thenThrow(sqlException);
            
            // When & Then
            SQLException exception = assertThrows(
                SQLException.class,
                () -> generator.generateSelectMicroservice(sql, businessDomainName, databaseConnection)
            );
            
            assertEquals("Database connection failed", exception.getMessage());
        }
    }
    
    @Test
    void testGenerateSelectMicroservice_MetadataExtractionFailure_PropagatesException() throws Exception {
        // Given
        String sql = "SELECT * FROM customers";
        String businessDomainName = "Customer";
        RuntimeException metadataException = new RuntimeException("Metadata extraction failed");
        
        try (MockedStatic<SqlMetadata> sqlMetadataMock = mockStatic(SqlMetadata.class);
             MockedStatic<CodeGenController> controllerMock = mockStatic(CodeGenController.class)) {
            
            sqlMetadataMock.when(() -> new SqlMetadata(jdbcTemplate)).thenReturn(sqlMetadata);
            controllerMock.when(() -> new CodeGenController(sqlMetadata)).thenReturn(codeGenController);
            
            when(codeGenController.selectColumnMetadata()).thenThrow(metadataException);
            
            // When & Then
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> generator.generateSelectMicroservice(sql, businessDomainName, databaseConnection)
            );
            
            assertEquals("Metadata extraction failed", exception.getMessage());
        }
    }
    
    @Test
    void testGenerateSelectMicroservice_ComplexSqlWithMultipleParameters_HandlesCorrectly() throws Exception {
        // Given
        String complexSql = """
            SELECT c.customer_id, c.customer_name, c.email, o.order_date, p.product_name 
            FROM customers c 
            JOIN orders o ON c.customer_id = o.customer_id 
            JOIN products p ON o.product_id = p.product_id 
            WHERE c.customer_id = ? AND o.order_date >= ? AND p.category = ?
            """;
        String businessDomainName = "CustomerOrder";
        
        // Setup more complex mock data
        List<ColumnMetadata> complexColumnMetadata = List.of(
            new ColumnMetadata("customer_id", "int", 1),
            new ColumnMetadata("customer_name", "varchar", 2),
            new ColumnMetadata("email", "varchar", 3),
            new ColumnMetadata("order_date", "datetime", 4),
            new ColumnMetadata("product_name", "varchar", 5)
        );
        
        ArrayList<DBColumn> complexParameters = new ArrayList<>();
        complexParameters.add(new DBColumn("table", "customerId", "java.lang.String", "int"));
        complexParameters.add(new DBColumn("table", "orderDate", "java.lang.String", "datetime"));
        complexParameters.add(new DBColumn("table", "category", "java.lang.String", "varchar"));
        
        try (MockedStatic<SqlMetadata> sqlMetadataMock = mockStatic(SqlMetadata.class);
             MockedStatic<CodeGenController> controllerMock = mockStatic(CodeGenController.class);
             MockedStatic<ParameterMetadataExtractor> extractorMock = mockStatic(ParameterMetadataExtractor.class)) {
            
            sqlMetadataMock.when(() -> new SqlMetadata(jdbcTemplate)).thenReturn(sqlMetadata);
            controllerMock.when(() -> new CodeGenController(sqlMetadata)).thenReturn(codeGenController);
            extractorMock.when(() -> new ParameterMetadataExtractor(dataSource)).thenReturn(parameterExtractor);
            
            when(codeGenController.selectColumnMetadata()).thenReturn(complexColumnMetadata);
            when(parameterExtractor.extractParameters(complexSql)).thenReturn(complexParameters);
            
            // When
            GeneratedMicroservice result = generator.generateSelectMicroservice(complexSql, businessDomainName, databaseConnection);
            
            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.SELECT, result.statementType());
            
            verify(codeGenController).selectColumnMetadata();
            verify(parameterExtractor).extractParameters(complexSql);
        }
    }
    
    @Test
    void testGenerateSelectMicroservice_NoParameters_HandlesCorrectly() throws Exception {
        // Given
        String sqlWithoutParams = "SELECT customer_id, customer_name, email FROM customers";
        String businessDomainName = "Customer";
        ArrayList<DBColumn> emptyParameters = new ArrayList<>();
        
        try (MockedStatic<SqlMetadata> sqlMetadataMock = mockStatic(SqlMetadata.class);
             MockedStatic<CodeGenController> controllerMock = mockStatic(CodeGenController.class);
             MockedStatic<ParameterMetadataExtractor> extractorMock = mockStatic(ParameterMetadataExtractor.class)) {
            
            sqlMetadataMock.when(() -> new SqlMetadata(jdbcTemplate)).thenReturn(sqlMetadata);
            controllerMock.when(() -> new CodeGenController(sqlMetadata)).thenReturn(codeGenController);
            extractorMock.when(() -> new ParameterMetadataExtractor(dataSource)).thenReturn(parameterExtractor);
            
            when(codeGenController.selectColumnMetadata()).thenReturn(mockColumnMetadata);
            when(parameterExtractor.extractParameters(sqlWithoutParams)).thenReturn(emptyParameters);
            
            // When
            GeneratedMicroservice result = generator.generateSelectMicroservice(sqlWithoutParams, businessDomainName, databaseConnection);
            
            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.SELECT, result.statementType());
            
            verify(parameterExtractor).extractParameters(sqlWithoutParams);
        }
    }
}