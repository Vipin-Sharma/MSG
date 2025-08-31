package com.jfeatures.msg.codegen.generator;

import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.codegen.domain.GeneratedMicroservice;
import com.jfeatures.msg.codegen.dbmetadata.InsertMetadata;
import com.jfeatures.msg.codegen.dbmetadata.InsertMetadataExtractor;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class InsertMicroserviceGeneratorTest {

    @Mock
    private DatabaseConnection databaseConnection;
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    @Mock
    private InsertMetadataExtractor insertMetadataExtractor;
    
    @Mock
    private InsertMetadata insertMetadata;
    
    private InsertMicroserviceGenerator generator;
    
    @BeforeEach
    void setUp() {
        generator = new InsertMicroserviceGenerator();
        
        // Setup database connection mocks with lenient stubbing
        lenient().when(databaseConnection.dataSource()).thenReturn(dataSource);
        lenient().when(databaseConnection.namedParameterJdbcTemplate()).thenReturn(namedParameterJdbcTemplate);
        
        // Setup insert metadata mock with lenient stubbing
        lenient().when(insertMetadata.tableName()).thenReturn("customers");
    }
    
    @Test
    void testGenerateInsertMicroservice_ValidInput_ReturnsGeneratedMicroservice() throws Exception {
        // Given
        String sql = "INSERT INTO customers (customer_name, email, phone) VALUES (?, ?, ?)";
        String businessDomainName = "Customer";
        
        try (MockedStatic<InsertMetadataExtractor> extractorMock = mockStatic(InsertMetadataExtractor.class)) {
            
            // Setup static mock
            extractorMock.when(() -> new InsertMetadataExtractor(dataSource, namedParameterJdbcTemplate))
                        .thenReturn(insertMetadataExtractor);
            
            when(insertMetadataExtractor.extractInsertMetadata(sql)).thenReturn(insertMetadata);
            
            // When
            GeneratedMicroservice result = generator.generateInsertMicroservice(sql, businessDomainName, databaseConnection);
            
            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.INSERT, result.statementType());
            assertNotNull(result.springBootApplication());
            assertNotNull(result.dtoFile());
            assertNotNull(result.controllerFile());
            assertNotNull(result.daoFile());
            assertNotNull(result.databaseConfigContent());
            
            // Verify interactions
            verify(insertMetadataExtractor).extractInsertMetadata(sql);
            verify(insertMetadata).tableName();
        }
    }
    
    @Test
    void testGenerateInsertMicroservice_NullSql_ThrowsIllegalArgumentException() {
        // Given
        String sql = null;
        String businessDomainName = "Customer";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateInsertMicroservice(sql, businessDomainName, databaseConnection)
        );
        
        assertEquals("SQL statement cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGenerateInsertMicroservice_EmptySql_ThrowsIllegalArgumentException() {
        // Given
        String sql = "   ";
        String businessDomainName = "Customer";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateInsertMicroservice(sql, businessDomainName, databaseConnection)
        );
        
        assertEquals("SQL statement cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGenerateInsertMicroservice_NullBusinessDomainName_ThrowsIllegalArgumentException() {
        // Given
        String sql = "INSERT INTO customers (name) VALUES (?)";
        String businessDomainName = null;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateInsertMicroservice(sql, businessDomainName, databaseConnection)
        );
        
        assertEquals("Business domain name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGenerateInsertMicroservice_EmptyBusinessDomainName_ThrowsIllegalArgumentException() {
        // Given
        String sql = "INSERT INTO customers (name) VALUES (?)";
        String businessDomainName = "";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateInsertMicroservice(sql, businessDomainName, databaseConnection)
        );
        
        assertEquals("Business domain name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGenerateInsertMicroservice_NullDatabaseConnection_ThrowsIllegalArgumentException() {
        // Given
        String sql = "INSERT INTO customers (name) VALUES (?)";
        String businessDomainName = "Customer";
        DatabaseConnection nullConnection = null;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateInsertMicroservice(sql, businessDomainName, nullConnection)
        );
        
        assertEquals("Database connection cannot be null", exception.getMessage());
    }
    
    @Test
    void testGenerateInsertMicroservice_MetadataExtractionFailure_PropagatesException() throws Exception {
        // Given
        String sql = "INSERT INTO customers (name) VALUES (?)";
        String businessDomainName = "Customer";
        RuntimeException metadataException = new RuntimeException("Failed to extract insert metadata");
        
        try (MockedStatic<InsertMetadataExtractor> extractorMock = mockStatic(InsertMetadataExtractor.class)) {
            
            extractorMock.when(() -> new InsertMetadataExtractor(dataSource, namedParameterJdbcTemplate))
                        .thenReturn(insertMetadataExtractor);
            
            when(insertMetadataExtractor.extractInsertMetadata(sql)).thenThrow(metadataException);
            
            // When & Then
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> generator.generateInsertMicroservice(sql, businessDomainName, databaseConnection)
            );
            
            assertEquals("Failed to extract insert metadata", exception.getMessage());
            verify(insertMetadataExtractor).extractInsertMetadata(sql);
        }
    }
    
    @Test
    void testGenerateInsertMicroservice_ComplexInsertWithMultipleColumns_HandlesCorrectly() throws Exception {
        // Given
        String complexSql = """
            INSERT INTO customers (customer_id, customer_name, email, phone, address, city, country, created_date) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        String businessDomainName = "Customer";
        
        try (MockedStatic<InsertMetadataExtractor> extractorMock = mockStatic(InsertMetadataExtractor.class)) {
            
            extractorMock.when(() -> new InsertMetadataExtractor(dataSource, namedParameterJdbcTemplate))
                        .thenReturn(insertMetadataExtractor);
            
            when(insertMetadataExtractor.extractInsertMetadata(complexSql)).thenReturn(insertMetadata);
            
            // When
            GeneratedMicroservice result = generator.generateInsertMicroservice(complexSql, businessDomainName, databaseConnection);
            
            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.INSERT, result.statementType());
            
            verify(insertMetadataExtractor).extractInsertMetadata(complexSql);
        }
    }
    
    @Test
    void testGenerateInsertMicroservice_NamedParametersInsert_HandlesCorrectly() throws Exception {
        // Given
        String namedParamSql = """
            INSERT INTO customers (customer_name, email, phone) 
            VALUES (:customerName, :email, :phone)
            """;
        String businessDomainName = "Customer";
        
        try (MockedStatic<InsertMetadataExtractor> extractorMock = mockStatic(InsertMetadataExtractor.class)) {
            
            extractorMock.when(() -> new InsertMetadataExtractor(dataSource, namedParameterJdbcTemplate))
                        .thenReturn(insertMetadataExtractor);
            
            when(insertMetadataExtractor.extractInsertMetadata(namedParamSql)).thenReturn(insertMetadata);
            
            // When
            GeneratedMicroservice result = generator.generateInsertMicroservice(namedParamSql, businessDomainName, databaseConnection);
            
            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.INSERT, result.statementType());
            
            verify(insertMetadataExtractor).extractInsertMetadata(namedParamSql);
        }
    }
    
    @Test
    void testGenerateInsertMicroservice_MultiTableInsert_HandlesCorrectly() throws Exception {
        // Given - Testing with a more complex scenario
        String businessDomainName = "ComplexCustomer";
        String sql = "INSERT INTO customers (name, email) VALUES (?, ?)";
        
        // Setup mock to return different table name for verification
        InsertMetadata complexInsertMetadata = mock(InsertMetadata.class);
        when(complexInsertMetadata.tableName()).thenReturn("complex_customers");
        
        try (MockedStatic<InsertMetadataExtractor> extractorMock = mockStatic(InsertMetadataExtractor.class)) {
            
            extractorMock.when(() -> new InsertMetadataExtractor(dataSource, namedParameterJdbcTemplate))
                        .thenReturn(insertMetadataExtractor);
            
            when(insertMetadataExtractor.extractInsertMetadata(sql)).thenReturn(complexInsertMetadata);
            
            // When
            GeneratedMicroservice result = generator.generateInsertMicroservice(sql, businessDomainName, databaseConnection);
            
            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.INSERT, result.statementType());
            
            verify(complexInsertMetadata).tableName();
        }
    }
    
    @Test
    void testGenerateInsertMicroservice_DatabaseConnectionFailure_PropagatesException() throws Exception {
        // Given
        String sql = "INSERT INTO customers (name) VALUES (?)";
        String businessDomainName = "Customer";
        RuntimeException dbException = new RuntimeException("Database connection failed");
        
        when(databaseConnection.dataSource()).thenThrow(dbException);
        
        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> generator.generateInsertMicroservice(sql, businessDomainName, databaseConnection)
        );
        
        assertEquals("Database connection failed", exception.getMessage());
    }
}