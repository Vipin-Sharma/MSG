package com.jfeatures.msg.codegen.generator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadataExtractor;
import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.codegen.domain.GeneratedMicroservice;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateMicroserviceGeneratorTest {

    @Mock
    private DatabaseConnection databaseConnection;
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private UpdateMetadataExtractor updateMetadataExtractor;
    
    @Mock
    private UpdateMetadata updateMetadata;
    
    private UpdateMicroserviceGenerator generator;
    
    @BeforeEach
    void setUp() {
        generator = new UpdateMicroserviceGenerator();
        
        // Setup database connection mocks with lenient stubbing
        lenient().when(databaseConnection.dataSource()).thenReturn(dataSource);
        
        // Setup update metadata mock with lenient stubbing
        lenient().when(updateMetadata.tableName()).thenReturn("customers");
    }
    
    @Test
    void testGenerateUpdateMicroservice_ValidInput_ReturnsGeneratedMicroservice() throws Exception {
        // Given
        String sql = "UPDATE customers SET customer_name = ?, email = ? WHERE customer_id = ?";
        String businessDomainName = "Customer";
        
        try (var mockedConstruction = mockConstruction(UpdateMetadataExtractor.class, (mock, context) -> {
            when(mock.extractUpdateMetadata(sql)).thenReturn(updateMetadata);
        })) {
            
            // When
            GeneratedMicroservice result = generator.generateUpdateMicroservice(sql, businessDomainName, databaseConnection);
            
            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.UPDATE, result.statementType());
            assertNotNull(result.springBootApplication());
            assertNotNull(result.dtoFile());
            assertNotNull(result.controllerFile());
            assertNotNull(result.daoFile());
            assertNotNull(result.databaseConfigContent());
            
            // Verify interactions with the constructed mock
            var constructedMocks = mockedConstruction.constructed();
            assertEquals(1, constructedMocks.size());
            verify(constructedMocks.get(0)).extractUpdateMetadata(sql);
        }
    }
    
    @Test
    void testGenerateUpdateMicroservice_NullSql_ThrowsIllegalArgumentException() {
        // Given
        String sql = null;
        String businessDomainName = "Customer";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateUpdateMicroservice(sql, businessDomainName, databaseConnection)
        );
        
        assertEquals("SQL statement cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGenerateUpdateMicroservice_EmptySql_ThrowsIllegalArgumentException() {
        // Given
        String sql = "   ";
        String businessDomainName = "Customer";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateUpdateMicroservice(sql, businessDomainName, databaseConnection)
        );
        
        assertEquals("SQL statement cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGenerateUpdateMicroservice_NullBusinessDomainName_ThrowsIllegalArgumentException() {
        // Given
        String sql = "UPDATE customers SET name = ? WHERE id = ?";
        String businessDomainName = null;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateUpdateMicroservice(sql, businessDomainName, databaseConnection)
        );
        
        assertEquals("Business domain name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGenerateUpdateMicroservice_EmptyBusinessDomainName_ThrowsIllegalArgumentException() {
        // Given
        String sql = "UPDATE customers SET name = ? WHERE id = ?";
        String businessDomainName = "";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateUpdateMicroservice(sql, businessDomainName, databaseConnection)
        );
        
        assertEquals("Business domain name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGenerateUpdateMicroservice_NullDatabaseConnection_ThrowsIllegalArgumentException() {
        // Given
        String sql = "UPDATE customers SET name = ? WHERE id = ?";
        String businessDomainName = "Customer";
        DatabaseConnection nullConnection = null;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> generator.generateUpdateMicroservice(sql, businessDomainName, nullConnection)
        );
        
        assertEquals("Database connection cannot be null", exception.getMessage());
    }
    
    @Test
    void testGenerateUpdateMicroservice_MetadataExtractionFailure_PropagatesException() throws Exception {
        // Given
        String sql = "UPDATE customers SET name = ? WHERE id = ?";
        String businessDomainName = "Customer";
        RuntimeException metadataException = new RuntimeException("Failed to extract update metadata");
        
        try (var mockedConstruction = mockConstruction(UpdateMetadataExtractor.class, (mock, context) -> {
            when(mock.extractUpdateMetadata(sql)).thenThrow(metadataException);
        })) {
            
            // When & Then
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> generator.generateUpdateMicroservice(sql, businessDomainName, databaseConnection)
            );
            
            assertEquals("Failed to extract update metadata", exception.getMessage());
            var constructedMocks = mockedConstruction.constructed();
            assertEquals(1, constructedMocks.size());
            verify(constructedMocks.get(0)).extractUpdateMetadata(sql);
        }
    }
    
    @Test
    void testGenerateUpdateMicroservice_ComplexUpdateWithMultipleColumns_HandlesCorrectly() throws Exception {
        // Given
        String complexSql = """
            UPDATE customers 
            SET customer_name = ?, email = ?, phone = ?, address = ?, city = ?, country = ?, modified_date = ? 
            WHERE customer_id = ? AND status = ?
            """;
        String businessDomainName = "Customer";
        
        try (var mockedConstruction = mockConstruction(UpdateMetadataExtractor.class, (mock, context) -> {
            when(mock.extractUpdateMetadata(complexSql)).thenReturn(updateMetadata);
        })) {
            
            // When
            GeneratedMicroservice result = generator.generateUpdateMicroservice(complexSql, businessDomainName, databaseConnection);
            
            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.UPDATE, result.statementType());
            
            var constructedMocks = mockedConstruction.constructed();
            assertEquals(1, constructedMocks.size());
            verify(constructedMocks.get(0)).extractUpdateMetadata(complexSql);
        }
    }
    
    @Test
    void testGenerateUpdateMicroservice_NamedParametersUpdate_HandlesCorrectly() throws Exception {
        // Given
        String namedParamSql = """
            UPDATE customers 
            SET customer_name = :customerName, email = :email, phone = :phone 
            WHERE customer_id = :customerId
            """;
        String businessDomainName = "Customer";
        
        try (var mockedConstruction = mockConstruction(UpdateMetadataExtractor.class, (mock, context) -> {
            when(mock.extractUpdateMetadata(namedParamSql)).thenReturn(updateMetadata);
        })) {
            
            // When
            GeneratedMicroservice result = generator.generateUpdateMicroservice(namedParamSql, businessDomainName, databaseConnection);
            
            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.UPDATE, result.statementType());
            
            var constructedMocks = mockedConstruction.constructed();
            assertEquals(1, constructedMocks.size());
            verify(constructedMocks.get(0)).extractUpdateMetadata(namedParamSql);
        }
    }
    
    @Test
    void testGenerateUpdateMicroservice_UpdateWithJoin_HandlesCorrectly() throws Exception {
        // Given
        String joinUpdateSql = """
            UPDATE c 
            SET c.customer_name = ?, c.email = ? 
            FROM customers c 
            JOIN orders o ON c.customer_id = o.customer_id 
            WHERE o.order_date > ?
            """;
        String businessDomainName = "Customer";
        
        try (var mockedConstruction = mockConstruction(UpdateMetadataExtractor.class, (mock, context) -> {
            when(mock.extractUpdateMetadata(joinUpdateSql)).thenReturn(updateMetadata);
        })) {
            
            // When
            GeneratedMicroservice result = generator.generateUpdateMicroservice(joinUpdateSql, businessDomainName, databaseConnection);
            
            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.UPDATE, result.statementType());
            
            var constructedMocks = mockedConstruction.constructed();
            assertEquals(1, constructedMocks.size());
            verify(constructedMocks.get(0)).extractUpdateMetadata(joinUpdateSql);
        }
    }
    
    @Test
    void testGenerateUpdateMicroservice_UpdateWithSubquery_HandlesCorrectly() throws Exception {
        // Given
        String subquerySql = """
            UPDATE customers 
            SET customer_status = ? 
            WHERE customer_id IN (
                SELECT customer_id FROM orders WHERE order_date > ?
            )
            """;
        String businessDomainName = "Customer";
        
        try (var mockedConstruction = mockConstruction(UpdateMetadataExtractor.class, (mock, context) -> {
            when(mock.extractUpdateMetadata(subquerySql)).thenReturn(updateMetadata);
        })) {
            
            // When
            GeneratedMicroservice result = generator.generateUpdateMicroservice(subquerySql, businessDomainName, databaseConnection);
            
            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.UPDATE, result.statementType());
            
            var constructedMocks = mockedConstruction.constructed();
            assertEquals(1, constructedMocks.size());
            verify(constructedMocks.get(0)).extractUpdateMetadata(subquerySql);
        }
    }
    
    @Test
    void testGenerateUpdateMicroservice_DatabaseConnectionFailure_PropagatesException() {
        // Given
        String sql = "UPDATE customers SET name = ? WHERE id = ?";
        String businessDomainName = "Customer";
        RuntimeException dbException = new RuntimeException("Database connection failed");

        when(databaseConnection.dataSource()).thenThrow(dbException);

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> generator.generateUpdateMicroservice(sql, businessDomainName, databaseConnection)
        );
        
        assertEquals("Database connection failed", exception.getMessage());
    }
}