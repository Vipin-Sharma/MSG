package com.jfeatures.msg.codegen.generator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import com.jfeatures.msg.codegen.ParameterMetadataExtractor;
import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.codegen.domain.GeneratedMicroservice;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import java.sql.SQLException;
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
class DeleteMicroserviceGeneratorTest {

    @Mock
    private DatabaseConnection databaseConnection;
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private ParameterMetadataExtractor parameterExtractor;
    
    private DeleteMicroserviceGenerator generator;
    private List<DBColumn> mockParameters;
    
    @BeforeEach
    void setUp() {
        generator = new DeleteMicroserviceGenerator();
        
        // Setup database connection mocks with lenient stubbing
        lenient().when(databaseConnection.dataSource()).thenReturn(dataSource);
        
        // Setup mock parameters
        mockParameters = Arrays.asList(
            new DBColumn("table", "customerId", "java.lang.String", "INTEGER"),
            new DBColumn("table", "status", "java.lang.String", "VARCHAR")
        );
    }
    
    @Test
    void testGenerateDeleteMicroservice_ValidInput_ReturnsGeneratedMicroservice() throws Exception {
        // Given
        String sql = "DELETE FROM customers WHERE customer_id = ? AND status = ?";
        String businessDomainName = "Customer";
        
        try (var mockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
            when(mock.extractParameters(sql)).thenReturn(mockParameters);
        })) {
            
            // When
            GeneratedMicroservice result = generator.generateDeleteMicroservice(sql, businessDomainName, databaseConnection);
            
            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.DELETE, result.statementType());
            assertNotNull(result.springBootApplication());
            assertNotNull(result.dtoFile());
            assertNotNull(result.controllerFile());
            assertNotNull(result.daoFile());
            assertNotNull(result.databaseConfigContent());
            
            // Verify interactions with constructed mock
            var constructedMocks = mockedConstruction.constructed();
            assertEquals(1, constructedMocks.size());
            verify(constructedMocks.get(0)).extractParameters(sql);
        }
    }
    
    @Test
    void testGenerateDeleteMicroservice_NullSql_ThrowsIllegalArgumentException() {
        // Given
        String sql = null;
        String businessDomainName = "Customer";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateDeleteMicroservice(sql, businessDomainName, databaseConnection)
        );
        
        assertEquals("SQL statement cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGenerateDeleteMicroservice_EmptySql_ThrowsIllegalArgumentException() {
        // Given
        String sql = "   ";
        String businessDomainName = "Customer";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateDeleteMicroservice(sql, businessDomainName, databaseConnection)
        );
        
        assertEquals("SQL statement cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGenerateDeleteMicroservice_NullBusinessDomainName_ThrowsIllegalArgumentException() {
        // Given
        String sql = "DELETE FROM customers WHERE id = ?";
        String businessDomainName = null;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateDeleteMicroservice(sql, businessDomainName, databaseConnection)
        );
        
        assertEquals("Business domain name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGenerateDeleteMicroservice_EmptyBusinessDomainName_ThrowsIllegalArgumentException() {
        // Given
        String sql = "DELETE FROM customers WHERE id = ?";
        String businessDomainName = "";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateDeleteMicroservice(sql, businessDomainName, databaseConnection)
        );
        
        assertEquals("Business domain name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGenerateDeleteMicroservice_NullDatabaseConnection_ThrowsIllegalArgumentException() {
        // Given
        String sql = "DELETE FROM customers WHERE id = ?";
        String businessDomainName = "Customer";
        DatabaseConnection nullConnection = null;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateDeleteMicroservice(sql, businessDomainName, nullConnection)
        );
        
        assertEquals("Database connection cannot be null", exception.getMessage());
    }
    
    @Test
    void testGenerateDeleteMicroservice_InvalidDeleteSqlMissingFrom_ThrowsIllegalArgumentException() {
        // Given
        String invalidSql = "DELETE customers WHERE id = ?";
        String businessDomainName = "Customer";

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateDeleteMicroservice(invalidSql, businessDomainName, databaseConnection)
        );
        
        assertEquals("Invalid DELETE SQL: missing DELETE FROM clause", exception.getMessage());
    }
    
    @Test
    void testGenerateDeleteMicroservice_DeleteWithoutWhereClause_HandlesCorrectly() throws Exception {
        // Given
        String sql = "DELETE FROM customers";
        String businessDomainName = "Customer";
        List<DBColumn> emptyParameters = Collections.emptyList();
        
        try (var mockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
            when(mock.extractParameters(sql)).thenReturn(emptyParameters);
        })) {
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> generator.generateDeleteMicroservice(sql, businessDomainName, databaseConnection)
            );
            
            assertEquals("Delete metadata must have at least one WHERE column", exception.getMessage());
            var constructedMocks = mockedConstruction.constructed();
            assertEquals(1, constructedMocks.size());
            verify(constructedMocks.get(0)).extractParameters(sql);
        }
    }
    
    @Test
    void testGenerateDeleteMicroservice_ComplexDeleteWithMultipleConditions_HandlesCorrectly() throws Exception {
        // Given
        String complexSql = """
            DELETE FROM customers 
            WHERE customer_id = ? 
            AND status = ? 
            AND created_date < ? 
            AND region = ?
            """;
        String businessDomainName = "Customer";
        
        List<DBColumn> complexParameters = Arrays.asList(
            new DBColumn("table", "customerId", "java.lang.String", "INTEGER"),
            new DBColumn("table", "status", "java.lang.String", "VARCHAR"),
            new DBColumn("table", "createdDate", "java.lang.String", "TIMESTAMP"),
            new DBColumn("table", "region", "java.lang.String", "VARCHAR")
        );
        
        try (var mockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
            
            when(mock.extractParameters(complexSql)).thenReturn(complexParameters);
        })) {
            
            // When
            GeneratedMicroservice result = generator.generateDeleteMicroservice(complexSql, businessDomainName, databaseConnection);
            
            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.DELETE, result.statementType());
            
            var constructedMocks = mockedConstruction.constructed();
            assertEquals(1, constructedMocks.size());
            verify(constructedMocks.get(0)).extractParameters(complexSql);
        }
    }
    
    @Test
    void testGenerateDeleteMicroservice_DeleteWithSubquery_HandlesCorrectly() throws Exception {
        // Given
        String subquerySql = """
            DELETE FROM customers 
            WHERE customer_id IN (
                SELECT customer_id FROM orders WHERE order_date < ?
            )
            """;
        String businessDomainName = "Customer";
        
        List<DBColumn> subqueryParameters = Arrays.asList(
            new DBColumn("table", "orderDate", "java.lang.String", "TIMESTAMP")
        );
        
        try (var mockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
            
            when(mock.extractParameters(subquerySql)).thenReturn(subqueryParameters);
        })) {
            
            // When
            GeneratedMicroservice result = generator.generateDeleteMicroservice(subquerySql, businessDomainName, databaseConnection);
            
            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.DELETE, result.statementType());
            
            var constructedMocks = mockedConstruction.constructed();
            assertEquals(1, constructedMocks.size());
            verify(constructedMocks.get(0)).extractParameters(subquerySql);
        }
    }
    
    @Test
    void testGenerateDeleteMicroservice_ParameterExtractionFailure_PropagatesException() throws Exception {
        // Given
        String sql = "DELETE FROM customers WHERE id = ?";
        String businessDomainName = "Customer";
        SQLException sqlException = new SQLException("Failed to extract parameters");
        
        try (var mockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
            
            when(mock.extractParameters(sql)).thenThrow(sqlException);
        })) {
            
            // When & Then
            SQLException exception = assertThrows(
                SQLException.class,
                () -> generator.generateDeleteMicroservice(sql, businessDomainName, databaseConnection)
            );
            
            assertEquals("Failed to extract parameters", exception.getMessage());
            var constructedMocks = mockedConstruction.constructed();
            assertEquals(1, constructedMocks.size());
            verify(constructedMocks.get(0)).extractParameters(sql);
        }
    }
    
    @Test
    void testGenerateDeleteMicroservice_DatabaseConnectionFailure_PropagatesException() {
        // Given
        String sql = "DELETE FROM customers WHERE id = ?";
        String businessDomainName = "Customer";
        RuntimeException dbException = new RuntimeException("Database connection failed");

        when(databaseConnection.dataSource()).thenThrow(dbException);

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> generator.generateDeleteMicroservice(sql, businessDomainName, databaseConnection)
        );

        assertEquals("Database connection failed", exception.getMessage());
    }

    @Test
    void testGenerateDeleteMicroservice_TableNameExtractionWithSemicolon_HandlesCorrectly() {
        // Given
        String sql = "DELETE FROM customers; ";
        String businessDomainName = "Customer";
        
        try (var mockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
            
            when(mock.extractParameters(sql)).thenReturn(Collections.emptyList());
        })) {
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> generator.generateDeleteMicroservice(sql, businessDomainName, databaseConnection)
            );
            
            assertEquals("Delete metadata must have at least one WHERE column", exception.getMessage());
        }
    }
    
    @Test
    void testGenerateDeleteMicroservice_CaseInsensitiveDeleteFrom_HandlesCorrectly() throws Exception {
        // Given
        String sql = "delete from customers WHERE id = ?";
        String businessDomainName = "Customer";
        
        List<DBColumn> singleParameter = Arrays.asList(
            new DBColumn("table", "id", "java.lang.String", "INTEGER")
        );
        
        try (var mockedConstruction = mockConstruction(ParameterMetadataExtractor.class, (mock, context) -> {
            
            when(mock.extractParameters(sql)).thenReturn(singleParameter);
        })) {
            
            // When
            GeneratedMicroservice result = generator.generateDeleteMicroservice(sql, businessDomainName, databaseConnection);
            
            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.DELETE, result.statementType());
            
            var constructedMocks = mockedConstruction.constructed();
            assertEquals(1, constructedMocks.size());
            verify(constructedMocks.get(0)).extractParameters(sql);
        }
    }
}