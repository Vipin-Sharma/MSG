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
        // Setup mock column metadata with complete data
        ColumnMetadata col1 = new ColumnMetadata();
        col1.setColumnName("customer_id");
        col1.setColumnTypeName("int");
        col1.setColumnType(1);
        col1.setColumnClassName("java.lang.Integer");
        
        ColumnMetadata col2 = new ColumnMetadata();
        col2.setColumnName("customer_name");
        col2.setColumnTypeName("varchar");
        col2.setColumnType(2);
        col2.setColumnClassName("java.lang.String");
        
        ColumnMetadata col3 = new ColumnMetadata();
        col3.setColumnName("email");
        col3.setColumnTypeName("varchar");
        col3.setColumnType(3);
        col3.setColumnClassName("java.lang.String");
        
        mockColumnMetadata = List.of(col1, col2, col3);
        
        // Setup mock parameters
        mockParameters = new ArrayList<>();
        mockParameters.add(new DBColumn("table", "customerId", "java.lang.String", "int"));
        mockParameters.add(new DBColumn("table", "status", "java.lang.String", "varchar"));
        
        // Setup database connection mocks with lenient stubbing
        lenient().when(databaseConnection.jdbcTemplate()).thenReturn(jdbcTemplate);
        lenient().when(databaseConnection.dataSource()).thenReturn(dataSource);
    }
    
    @Test
    void testGenerateSelectMicroservice_ValidInput_ReturnsGeneratedMicroservice() throws Exception {
        // Given
        String sql = "SELECT customer_id, customer_name, email FROM customers WHERE customer_id = ? AND status = ?";
        String businessDomainName = "Customer";
        
        try (var sqlMetadataMockedConstruction = mockConstruction(SqlMetadata.class, (mock, context) -> {
                 try {
                     when(mock.getColumnMetadata(sql)).thenReturn(mockColumnMetadata);
                 } catch (Exception e) {
                     // Handle SQLException from mocking
                 }
             });
             var controllerMockedConstruction = mockConstruction(CodeGenController.class, (mock, context) -> {
                 when(mock.selectColumnMetadata()).thenReturn(mockColumnMetadata);
             });
             var extractorMockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
                 when(mock.extractParameters(sql)).thenReturn(mockParameters);
             })) {
            
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
            
            // Verify interactions with constructed mocks
            var sqlMetadataConstructedMocks = sqlMetadataMockedConstruction.constructed();
            var controllerConstructedMocks = controllerMockedConstruction.constructed();
            var extractorConstructedMocks = extractorMockedConstruction.constructed();
            
            assertEquals(1, sqlMetadataConstructedMocks.size());
            assertEquals(1, controllerConstructedMocks.size());
            assertEquals(1, extractorConstructedMocks.size());
            
            verify(controllerConstructedMocks.get(0)).selectColumnMetadata();
            verify(extractorConstructedMocks.get(0)).extractParameters(sql);
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
        
        try (var sqlMetadataMockedConstruction = mockConstruction(SqlMetadata.class, (mock, context) -> {
                 try {
                     when(mock.getColumnMetadata(sql)).thenReturn(mockColumnMetadata);
                 } catch (Exception e) {
                     // Handle SQLException from mocking
                 }
             });
             var controllerMockedConstruction = mockConstruction(CodeGenController.class, (mock, context) -> {
                 when(mock.selectColumnMetadata()).thenReturn(mockColumnMetadata);
             });
             var extractorMockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
                 when(mock.extractParameters(sql)).thenThrow(sqlException);
             })) {
            
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
        
        try (var sqlMetadataMockedConstruction = mockConstruction(SqlMetadata.class, (mock, context) -> {
                 try {
                     when(mock.getColumnMetadata(sql)).thenReturn(mockColumnMetadata);
                 } catch (Exception e) {
                     // Handle SQLException from mocking
                 }
             });
             var controllerMockedConstruction = mockConstruction(CodeGenController.class, (mock, context) -> {
                 when(mock.selectColumnMetadata()).thenThrow(metadataException);
             })) {
            
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
        
        // Setup more complex mock data with complete ColumnMetadata
        ColumnMetadata col1 = new ColumnMetadata();
        col1.setColumnName("customer_id");
        col1.setColumnTypeName("int");
        col1.setColumnType(1);
        col1.setColumnClassName("java.lang.Integer");
        
        ColumnMetadata col2 = new ColumnMetadata();
        col2.setColumnName("customer_name");
        col2.setColumnTypeName("varchar");
        col2.setColumnType(2);
        col2.setColumnClassName("java.lang.String");
        
        ColumnMetadata col3 = new ColumnMetadata();
        col3.setColumnName("email");
        col3.setColumnTypeName("varchar");
        col3.setColumnType(3);
        col3.setColumnClassName("java.lang.String");
        
        ColumnMetadata col4 = new ColumnMetadata();
        col4.setColumnName("order_date");
        col4.setColumnTypeName("datetime");
        col4.setColumnType(4);
        col4.setColumnClassName("java.sql.Timestamp");
        
        ColumnMetadata col5 = new ColumnMetadata();
        col5.setColumnName("product_name");
        col5.setColumnTypeName("varchar");
        col5.setColumnType(5);
        col5.setColumnClassName("java.lang.String");
        
        List<ColumnMetadata> complexColumnMetadata = List.of(col1, col2, col3, col4, col5);
        
        ArrayList<DBColumn> complexParameters = new ArrayList<>();
        complexParameters.add(new DBColumn("table", "customerId", "java.lang.String", "int"));
        complexParameters.add(new DBColumn("table", "orderDate", "java.lang.String", "datetime"));
        complexParameters.add(new DBColumn("table", "category", "java.lang.String", "varchar"));
        
        try (var sqlMetadataMockedConstruction = mockConstruction(SqlMetadata.class, (mock, context) -> {
                 try {
                     when(mock.getColumnMetadata(complexSql)).thenReturn(complexColumnMetadata);
                 } catch (Exception e) {
                     // Handle SQLException from mocking
                 }
             });
             var controllerMockedConstruction = mockConstruction(CodeGenController.class, (mock, context) -> {
                 when(mock.selectColumnMetadata()).thenReturn(complexColumnMetadata);
             });
             var extractorMockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
                 when(mock.extractParameters(complexSql)).thenReturn(complexParameters);
             })) {
            
            // When
            GeneratedMicroservice result = generator.generateSelectMicroservice(complexSql, businessDomainName, databaseConnection);
            
            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.SELECT, result.statementType());
            
            // Verify interactions with constructed mocks
            var controllerConstructedMocks = controllerMockedConstruction.constructed();
            var extractorConstructedMocks = extractorMockedConstruction.constructed();
            
            assertEquals(1, controllerConstructedMocks.size());
            assertEquals(1, extractorConstructedMocks.size());
            
            verify(controllerConstructedMocks.get(0)).selectColumnMetadata();
            verify(extractorConstructedMocks.get(0)).extractParameters(complexSql);
        }
    }
    
    @Test
    void testGenerateSelectMicroservice_NoParameters_HandlesCorrectly() throws Exception {
        // Given
        String sqlWithoutParams = "SELECT customer_id, customer_name, email FROM customers";
        String businessDomainName = "Customer";
        ArrayList<DBColumn> emptyParameters = new ArrayList<>();
        
        try (var sqlMetadataMockedConstruction = mockConstruction(SqlMetadata.class, (mock, context) -> {
                 try {
                     when(mock.getColumnMetadata(sqlWithoutParams)).thenReturn(mockColumnMetadata);
                 } catch (Exception e) {
                     // Handle SQLException from mocking
                 }
             });
             var controllerMockedConstruction = mockConstruction(CodeGenController.class, (mock, context) -> {
                 when(mock.selectColumnMetadata()).thenReturn(mockColumnMetadata);
             });
             var extractorMockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
                 when(mock.extractParameters(sqlWithoutParams)).thenReturn(emptyParameters);
             })) {
            
            // When
            GeneratedMicroservice result = generator.generateSelectMicroservice(sqlWithoutParams, businessDomainName, databaseConnection);
            
            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.SELECT, result.statementType());
            
            // Verify interactions with constructed mocks
            var extractorConstructedMocks = extractorMockedConstruction.constructed();
            assertEquals(1, extractorConstructedMocks.size());
            verify(extractorConstructedMocks.get(0)).extractParameters(sqlWithoutParams);
        }
    }
}