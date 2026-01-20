package com.jfeatures.msg.codegen.generator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadataExtractor;
import com.jfeatures.msg.codegen.domain.AuditConfiguration;
import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.codegen.domain.GeneratedMicroservice;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
    void testGenerateUpdateMicroservice_DatabaseConnectionFailure_PropagatesException() throws Exception {
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

    // ========== AUDIT CONFIGURATION TESTS ==========

    private UpdateMetadata createMockUpdateMetadataWithAudit() {
        ColumnMetadata nameCol = new ColumnMetadata();
        nameCol.setColumnName("customer_name");
        nameCol.setColumnTypeName("varchar");
        nameCol.setColumnType(12);
        nameCol.setColumnClassName("java.lang.String");

        ColumnMetadata auditCol = new ColumnMetadata();
        auditCol.setColumnName("updated_by");
        auditCol.setColumnTypeName("varchar");
        auditCol.setColumnType(12);
        auditCol.setColumnClassName("java.lang.String");

        ColumnMetadata idCol = new ColumnMetadata();
        idCol.setColumnName("customer_id");
        idCol.setColumnTypeName("int");
        idCol.setColumnType(4);
        idCol.setColumnClassName("java.lang.Integer");

        UpdateMetadata auditMetadata = mock(UpdateMetadata.class);
        when(auditMetadata.tableName()).thenReturn("customers");
        when(auditMetadata.setColumns()).thenReturn(List.of(nameCol, auditCol));
        when(auditMetadata.whereColumns()).thenReturn(List.of(idCol));
        when(auditMetadata.originalSql()).thenReturn(
                "UPDATE customers SET customer_name = ?, updated_by = ? WHERE customer_id = ?");
        return auditMetadata;
    }

    @Test
    void testGenerateUpdateMicroservice_WithAuditEnabled_GeneratesCorrectly() throws Exception {
        // Given
        String sql = "UPDATE customers SET customer_name = ?, updated_by = ? WHERE customer_id = ?";
        String businessDomainName = "Customer";
        AuditConfiguration auditConfig = new AuditConfiguration(
                true, "X-User-ID", Set.of("updated_by"));

        UpdateMetadata auditMetadata = createMockUpdateMetadataWithAudit();

        try (var mockedConstruction = mockConstruction(UpdateMetadataExtractor.class, (mock, context) -> {
            when(mock.extractUpdateMetadata(sql)).thenReturn(auditMetadata);
        })) {

            // When
            GeneratedMicroservice result = generator.generateUpdateMicroservice(
                    sql, businessDomainName, databaseConnection, auditConfig);

            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());
            assertEquals(SqlStatementType.UPDATE, result.statementType());

            // Verify generated code contains audit-related elements
            String controllerCode = result.controllerFile().toString();
            assertTrue(controllerCode.contains("@RequestHeader") || controllerCode.contains("userId"),
                    "Controller should contain audit header handling");

            String daoCode = result.daoFile().toString();
            assertTrue(daoCode.contains("userId"),
                    "DAO should contain userId parameter");
        }
    }

    @Test
    void testGenerateUpdateMicroservice_WithAuditDisabled_GeneratesWithoutAudit() throws Exception {
        // Given
        String sql = "UPDATE customers SET customer_name = ? WHERE customer_id = ?";
        String businessDomainName = "Customer";
        AuditConfiguration auditConfig = AuditConfiguration.disabled();

        try (var mockedConstruction = mockConstruction(UpdateMetadataExtractor.class, (mock, context) -> {
            when(mock.extractUpdateMetadata(sql)).thenReturn(updateMetadata);
        })) {

            // When
            GeneratedMicroservice result = generator.generateUpdateMicroservice(
                    sql, businessDomainName, databaseConnection, auditConfig);

            // Then
            assertNotNull(result);
            String controllerCode = result.controllerFile().toString();
            assertFalse(controllerCode.contains("@RequestHeader"),
                    "Controller should NOT contain @RequestHeader when audit disabled");
        }
    }

    @Test
    void testGenerateUpdateMicroservice_WithNullAuditConfig_DefaultsToDisabled() throws Exception {
        // Given
        String sql = "UPDATE customers SET customer_name = ? WHERE customer_id = ?";
        String businessDomainName = "Customer";

        try (var mockedConstruction = mockConstruction(UpdateMetadataExtractor.class, (mock, context) -> {
            when(mock.extractUpdateMetadata(sql)).thenReturn(updateMetadata);
        })) {

            // When - call with null audit config
            GeneratedMicroservice result = generator.generateUpdateMicroservice(
                    sql, businessDomainName, databaseConnection, null);

            // Then
            assertNotNull(result);
            String controllerCode = result.controllerFile().toString();
            assertFalse(controllerCode.contains("@RequestHeader"),
                    "Controller should NOT contain @RequestHeader when audit config is null");
        }
    }

    @Test
    void testGenerateUpdateMicroservice_WithMultipleAuditColumns_FiltersCorrectly() throws Exception {
        // Given
        String sql = "UPDATE customers SET customer_name = ?, modified_by = ?, updated_by = ? WHERE customer_id = ?";
        String businessDomainName = "Customer";
        AuditConfiguration auditConfig = new AuditConfiguration(
                true, "X-User-ID", Set.of("modified_by", "updated_by"));

        // Setup metadata with multiple audit columns
        ColumnMetadata nameCol = new ColumnMetadata();
        nameCol.setColumnName("customer_name");
        nameCol.setColumnTypeName("varchar");
        nameCol.setColumnType(12);
        nameCol.setColumnClassName("java.lang.String");

        ColumnMetadata auditCol1 = new ColumnMetadata();
        auditCol1.setColumnName("modified_by");
        auditCol1.setColumnTypeName("varchar");
        auditCol1.setColumnType(12);
        auditCol1.setColumnClassName("java.lang.String");

        ColumnMetadata auditCol2 = new ColumnMetadata();
        auditCol2.setColumnName("updated_by");
        auditCol2.setColumnTypeName("varchar");
        auditCol2.setColumnType(12);
        auditCol2.setColumnClassName("java.lang.String");

        ColumnMetadata idCol = new ColumnMetadata();
        idCol.setColumnName("customer_id");
        idCol.setColumnTypeName("int");
        idCol.setColumnType(4);
        idCol.setColumnClassName("java.lang.Integer");

        UpdateMetadata multiAuditMetadata = mock(UpdateMetadata.class);
        when(multiAuditMetadata.tableName()).thenReturn("customers");
        when(multiAuditMetadata.setColumns()).thenReturn(List.of(nameCol, auditCol1, auditCol2));
        when(multiAuditMetadata.whereColumns()).thenReturn(List.of(idCol));
        when(multiAuditMetadata.originalSql()).thenReturn(sql);

        try (var mockedConstruction = mockConstruction(UpdateMetadataExtractor.class, (mock, context) -> {
            when(mock.extractUpdateMetadata(sql)).thenReturn(multiAuditMetadata);
        })) {

            // When
            GeneratedMicroservice result = generator.generateUpdateMicroservice(
                    sql, businessDomainName, databaseConnection, auditConfig);

            // Then
            assertNotNull(result);

            // DAO should map audit columns from userId
            String daoCode = result.daoFile().toString();
            assertTrue(daoCode.contains("userId"),
                    "DAO should reference userId for audit columns");
        }
    }

    @Test
    void testGenerateUpdateMicroservice_WithCustomHeaderName_UsesCorrectHeader() throws Exception {
        // Given
        String sql = "UPDATE customers SET customer_name = ?, updated_by = ? WHERE customer_id = ?";
        String businessDomainName = "Customer";
        AuditConfiguration auditConfig = new AuditConfiguration(
                true, "Authorization", Set.of("updated_by"));

        UpdateMetadata auditMetadata = createMockUpdateMetadataWithAudit();

        try (var mockedConstruction = mockConstruction(UpdateMetadataExtractor.class, (mock, context) -> {
            when(mock.extractUpdateMetadata(sql)).thenReturn(auditMetadata);
        })) {

            // When
            GeneratedMicroservice result = generator.generateUpdateMicroservice(
                    sql, businessDomainName, databaseConnection, auditConfig);

            // Then
            assertNotNull(result);
            String controllerCode = result.controllerFile().toString();
            assertTrue(controllerCode.contains("Authorization"),
                    "Controller should use custom header name 'Authorization'");
        }
    }

    @Test
    void testGenerateUpdateMicroservice_BackwardCompatibility_WithoutAuditConfig() throws Exception {
        // Given - call the original method without AuditConfiguration
        String sql = "UPDATE customers SET customer_name = ? WHERE customer_id = ?";
        String businessDomainName = "Customer";

        try (var mockedConstruction = mockConstruction(UpdateMetadataExtractor.class, (mock, context) -> {
            when(mock.extractUpdateMetadata(sql)).thenReturn(updateMetadata);
        })) {

            // When - call original method (backward compatibility)
            GeneratedMicroservice result = generator.generateUpdateMicroservice(
                    sql, businessDomainName, databaseConnection);

            // Then
            assertNotNull(result);
            assertEquals(businessDomainName, result.businessDomainName());

            // Should behave like audit disabled
            String controllerCode = result.controllerFile().toString();
            assertFalse(controllerCode.contains("@RequestHeader"),
                    "Original method should behave like audit disabled");
        }
    }
}