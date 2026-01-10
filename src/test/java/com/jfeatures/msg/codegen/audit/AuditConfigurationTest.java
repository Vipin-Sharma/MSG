package com.jfeatures.msg.codegen.audit;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuditConfiguration class.
 * Tests YAML loading, field detection, and configuration retrieval.
 */
class AuditConfigurationTest {

    @Test
    void testGetAuditFieldType_WithCreatedAtColumn_ReturnsCreatedAt() {
        AuditConfiguration config = AuditConfiguration.getInstance();

        Optional<AuditFieldType> result = config.getAuditFieldType("created_at");

        assertTrue(result.isPresent(), "Should detect created_at as audit field");
        assertEquals(AuditFieldType.CREATED_AT, result.get());
    }

    @Test
    void testGetAuditFieldType_WithCreatedByColumn_ReturnsCreatedBy() {
        AuditConfiguration config = AuditConfiguration.getInstance();

        Optional<AuditFieldType> result = config.getAuditFieldType("created_by");

        assertTrue(result.isPresent(), "Should detect created_by as audit field");
        assertEquals(AuditFieldType.CREATED_BY, result.get());
    }

    @Test
    void testGetAuditFieldType_WithUpdatedAtColumn_ReturnsUpdatedAt() {
        AuditConfiguration config = AuditConfiguration.getInstance();

        Optional<AuditFieldType> result = config.getAuditFieldType("updated_at");

        assertTrue(result.isPresent(), "Should detect updated_at as audit field");
        assertEquals(AuditFieldType.UPDATED_AT, result.get());
    }

    @Test
    void testGetAuditFieldType_WithUpdatedByColumn_ReturnsUpdatedBy() {
        AuditConfiguration config = AuditConfiguration.getInstance();

        Optional<AuditFieldType> result = config.getAuditFieldType("updated_by");

        assertTrue(result.isPresent(), "Should detect updated_by as audit field");
        assertEquals(AuditFieldType.UPDATED_BY, result.get());
    }

    @Test
    void testGetAuditFieldType_CaseInsensitive() {
        AuditConfiguration config = AuditConfiguration.getInstance();

        assertTrue(config.getAuditFieldType("CREATED_AT").isPresent(),
            "Should detect CREATED_AT (uppercase)");
        assertTrue(config.getAuditFieldType("Created_At").isPresent(),
            "Should detect Created_At (mixed case)");
        assertTrue(config.getAuditFieldType("created_AT").isPresent(),
            "Should detect created_AT (mixed case)");
    }

    @Test
    void testGetAuditFieldType_WithAlternateNames() {
        AuditConfiguration config = AuditConfiguration.getInstance();

        // Test alternate column names configured in audit-config.yaml
        assertTrue(config.getAuditFieldType("create_date").isPresent(),
            "Should detect create_date as CREATED_AT variant");
        assertTrue(config.getAuditFieldType("creator_id").isPresent(),
            "Should detect creator_id as CREATED_BY variant");
        assertTrue(config.getAuditFieldType("last_update").isPresent(),
            "Should detect last_update as UPDATED_AT variant");
        assertTrue(config.getAuditFieldType("modifier_id").isPresent(),
            "Should detect modifier_id as UPDATED_BY variant");
    }

    @Test
    void testGetAuditFieldType_WithNonAuditColumn_ReturnsEmpty() {
        AuditConfiguration config = AuditConfiguration.getInstance();

        Optional<AuditFieldType> result = config.getAuditFieldType("first_name");

        assertFalse(result.isPresent(), "Should NOT detect first_name as audit field");
    }

    @Test
    void testGetAuditFieldType_WithNullOrEmpty_ReturnsEmpty() {
        AuditConfiguration config = AuditConfiguration.getInstance();

        assertFalse(config.getAuditFieldType(null).isPresent(),
            "Should return empty for null column name");
        assertFalse(config.getAuditFieldType("").isPresent(),
            "Should return empty for empty column name");
        assertFalse(config.getAuditFieldType("   ").isPresent(),
            "Should return empty for blank column name");
    }

    @Test
    void testIsAuditField() {
        AuditConfiguration config = AuditConfiguration.getInstance();

        assertTrue(config.isAuditField("created_at"), "created_at is an audit field");
        assertTrue(config.isAuditField("created_by"), "created_by is an audit field");
        assertTrue(config.isAuditField("updated_at"), "updated_at is an audit field");
        assertTrue(config.isAuditField("updated_by"), "updated_by is an audit field");
        assertFalse(config.isAuditField("email"), "email is NOT an audit field");
        assertFalse(config.isAuditField("customer_id"), "customer_id is NOT an audit field");
    }

    @Test
    void testGetAllMappings_ContainsAllAuditTypes() {
        AuditConfiguration config = AuditConfiguration.getInstance();

        var mappings = config.getAllMappings();

        assertNotNull(mappings, "Mappings should not be null");
        assertTrue(mappings.containsKey(AuditFieldType.CREATED_AT),
            "Mappings should contain CREATED_AT");
        assertTrue(mappings.containsKey(AuditFieldType.CREATED_BY),
            "Mappings should contain CREATED_BY");
        assertTrue(mappings.containsKey(AuditFieldType.UPDATED_AT),
            "Mappings should contain UPDATED_AT");
        assertTrue(mappings.containsKey(AuditFieldType.UPDATED_BY),
            "Mappings should contain UPDATED_BY");
    }

    @Test
    void testGetColumnNamesForType_ReturnsConfiguredNames() {
        AuditConfiguration config = AuditConfiguration.getInstance();

        List<String> createdAtNames = config.getColumnNamesForType(AuditFieldType.CREATED_AT);

        assertNotNull(createdAtNames, "Column names list should not be null");
        assertFalse(createdAtNames.isEmpty(), "Column names list should not be empty");
        assertTrue(createdAtNames.contains("created_at"),
            "Should include created_at in normalized form");
        assertTrue(createdAtNames.contains("create_date"),
            "Should include create_date variant");
        assertTrue(createdAtNames.contains("created_timestamp"),
            "Should include created_timestamp variant");
    }

    @Test
    void testGetColumnNamesForType_AllTypes() {
        AuditConfiguration config = AuditConfiguration.getInstance();

        // Verify each audit type has configured column names
        List<String> createdAtNames = config.getColumnNamesForType(AuditFieldType.CREATED_AT);
        List<String> createdByNames = config.getColumnNamesForType(AuditFieldType.CREATED_BY);
        List<String> updatedAtNames = config.getColumnNamesForType(AuditFieldType.UPDATED_AT);
        List<String> updatedByNames = config.getColumnNamesForType(AuditFieldType.UPDATED_BY);

        assertTrue(createdAtNames.size() > 0, "CREATED_AT should have column patterns");
        assertTrue(createdByNames.size() > 0, "CREATED_BY should have column patterns");
        assertTrue(updatedAtNames.size() > 0, "UPDATED_AT should have column patterns");
        assertTrue(updatedByNames.size() > 0, "UPDATED_BY should have column patterns");
    }

    @Test
    void testSingletonPattern() {
        AuditConfiguration instance1 = AuditConfiguration.getInstance();
        AuditConfiguration instance2 = AuditConfiguration.getInstance();

        assertSame(instance1, instance2,
            "getInstance() should return the same singleton instance");
    }
}
