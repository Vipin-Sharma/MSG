package com.jfeatures.msg.codegen.audit;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuditFieldDetector utility class.
 * Tests audit field detection, filtering, and classification.
 */
class AuditFieldDetectorTest {

    @Test
    void testDetectAuditFields_WithAuditColumns_ReturnsCorrectMapping() {
        List<ColumnMetadata> columns = Arrays.asList(
            createColumn("first_name"),
            createColumn("created_at"),
            createColumn("created_by"),
            createColumn("updated_at")
        );

        Map<ColumnMetadata, AuditFieldType> result = AuditFieldDetector.detectAuditFields(columns);

        assertEquals(3, result.size(), "Should detect 3 audit fields");
        assertTrue(result.values().contains(AuditFieldType.CREATED_AT),
            "Should detect CREATED_AT");
        assertTrue(result.values().contains(AuditFieldType.CREATED_BY),
            "Should detect CREATED_BY");
        assertTrue(result.values().contains(AuditFieldType.UPDATED_AT),
            "Should detect UPDATED_AT");
    }

    @Test
    void testDetectAuditFields_WithAllAuditTypes_ReturnsAllFour() {
        List<ColumnMetadata> columns = Arrays.asList(
            createColumn("created_at"),
            createColumn("created_by"),
            createColumn("updated_at"),
            createColumn("updated_by")
        );

        Map<ColumnMetadata, AuditFieldType> result = AuditFieldDetector.detectAuditFields(columns);

        assertEquals(4, result.size(), "Should detect all 4 audit field types");
        assertTrue(result.values().contains(AuditFieldType.CREATED_AT));
        assertTrue(result.values().contains(AuditFieldType.CREATED_BY));
        assertTrue(result.values().contains(AuditFieldType.UPDATED_AT));
        assertTrue(result.values().contains(AuditFieldType.UPDATED_BY));
    }

    @Test
    void testDetectAuditFields_WithNoAuditColumns_ReturnsEmptyMap() {
        List<ColumnMetadata> columns = Arrays.asList(
            createColumn("first_name"),
            createColumn("last_name"),
            createColumn("email")
        );

        Map<ColumnMetadata, AuditFieldType> result = AuditFieldDetector.detectAuditFields(columns);

        assertTrue(result.isEmpty(), "Should return empty map when no audit fields present");
    }

    @Test
    void testDetectAuditFields_WithNullOrEmptyList_ReturnsEmptyMap() {
        Map<ColumnMetadata, AuditFieldType> resultNull = AuditFieldDetector.detectAuditFields(null);
        Map<ColumnMetadata, AuditFieldType> resultEmpty = AuditFieldDetector.detectAuditFields(Collections.emptyList());

        assertTrue(resultNull.isEmpty(), "Should return empty map for null input");
        assertTrue(resultEmpty.isEmpty(), "Should return empty map for empty list");
    }

    @Test
    void testFilterOutAuditFields_RemovesAuditColumns() {
        List<ColumnMetadata> columns = Arrays.asList(
            createColumn("first_name"),
            createColumn("created_at"),
            createColumn("email"),
            createColumn("created_by")
        );

        List<ColumnMetadata> filtered = AuditFieldDetector.filterOutAuditFields(columns);

        assertEquals(2, filtered.size(), "Should have 2 non-audit columns");
        assertTrue(filtered.stream().anyMatch(c -> c.getColumnName().equals("first_name")),
            "Should include first_name");
        assertTrue(filtered.stream().anyMatch(c -> c.getColumnName().equals("email")),
            "Should include email");
        assertFalse(filtered.stream().anyMatch(c -> c.getColumnName().equals("created_at")),
            "Should NOT include created_at");
        assertFalse(filtered.stream().anyMatch(c -> c.getColumnName().equals("created_by")),
            "Should NOT include created_by");
    }

    @Test
    void testFilterOutAuditFields_WithAllAuditFields_ReturnsEmptyList() {
        List<ColumnMetadata> columns = Arrays.asList(
            createColumn("created_at"),
            createColumn("created_by"),
            createColumn("updated_at"),
            createColumn("updated_by")
        );

        List<ColumnMetadata> filtered = AuditFieldDetector.filterOutAuditFields(columns);

        assertTrue(filtered.isEmpty(), "Should return empty list when all columns are audit fields");
    }

    @Test
    void testFilterOutAuditFields_WithNoAuditFields_ReturnsAllColumns() {
        List<ColumnMetadata> columns = Arrays.asList(
            createColumn("first_name"),
            createColumn("last_name"),
            createColumn("email")
        );

        List<ColumnMetadata> filtered = AuditFieldDetector.filterOutAuditFields(columns);

        assertEquals(3, filtered.size(), "Should return all columns when none are audit fields");
        assertEquals(columns, filtered, "Filtered list should match original when no audit fields");
    }

    @Test
    void testFilterOutAuditFields_WithNullOrEmpty_ReturnsEmptyList() {
        List<ColumnMetadata> resultNull = AuditFieldDetector.filterOutAuditFields(null);
        List<ColumnMetadata> resultEmpty = AuditFieldDetector.filterOutAuditFields(Collections.emptyList());

        assertTrue(resultNull.isEmpty(), "Should return empty list for null input");
        assertTrue(resultEmpty.isEmpty(), "Should return empty list for empty input");
    }

    @Test
    void testIsAuditField_ReturnsTrueForAuditColumns() {
        ColumnMetadata createdAt = createColumn("created_at");
        ColumnMetadata createdBy = createColumn("created_by");
        ColumnMetadata updatedAt = createColumn("updated_at");
        ColumnMetadata updatedBy = createColumn("updated_by");

        assertTrue(AuditFieldDetector.isAuditField(createdAt), "created_at is an audit field");
        assertTrue(AuditFieldDetector.isAuditField(createdBy), "created_by is an audit field");
        assertTrue(AuditFieldDetector.isAuditField(updatedAt), "updated_at is an audit field");
        assertTrue(AuditFieldDetector.isAuditField(updatedBy), "updated_by is an audit field");
    }

    @Test
    void testIsAuditField_ReturnsFalseForNonAuditColumns() {
        ColumnMetadata businessColumn = createColumn("first_name");

        assertFalse(AuditFieldDetector.isAuditField(businessColumn),
            "Business columns should not be detected as audit fields");
    }

    @Test
    void testIsAuditField_WithNull_ReturnsFalse() {
        assertFalse(AuditFieldDetector.isAuditField(null),
            "Should return false for null column");
    }

    @Test
    void testGetAuditFieldType_WithAuditColumn_ReturnsCorrectType() {
        ColumnMetadata createdAt = createColumn("created_at");

        Optional<AuditFieldType> result = AuditFieldDetector.getAuditFieldType(createdAt);

        assertTrue(result.isPresent(), "Should detect created_at");
        assertEquals(AuditFieldType.CREATED_AT, result.get(), "Should return CREATED_AT type");
    }

    @Test
    void testGetAuditFieldType_WithNonAuditColumn_ReturnsEmpty() {
        ColumnMetadata businessColumn = createColumn("email");

        Optional<AuditFieldType> result = AuditFieldDetector.getAuditFieldType(businessColumn);

        assertFalse(result.isPresent(), "Should return empty for non-audit column");
    }

    @Test
    void testGetOnlyAuditFields_ReturnsOnlyAuditColumns() {
        List<ColumnMetadata> columns = Arrays.asList(
            createColumn("first_name"),
            createColumn("created_at"),
            createColumn("email"),
            createColumn("updated_by")
        );

        Map<ColumnMetadata, AuditFieldType> result = AuditFieldDetector.getOnlyAuditFields(columns);

        assertEquals(2, result.size(), "Should return only audit fields");
        assertTrue(result.values().contains(AuditFieldType.CREATED_AT),
            "Should include CREATED_AT");
        assertTrue(result.values().contains(AuditFieldType.UPDATED_BY),
            "Should include UPDATED_BY");
    }

    @Test
    void testDetectAuditFields_WithAlternateColumnNames() {
        List<ColumnMetadata> columns = Arrays.asList(
            createColumn("create_date"),      // Alternate for created_at
            createColumn("creator_id"),       // Alternate for created_by
            createColumn("last_update"),      // Alternate for updated_at
            createColumn("modifier_id")       // Alternate for updated_by
        );

        Map<ColumnMetadata, AuditFieldType> result = AuditFieldDetector.detectAuditFields(columns);

        assertEquals(4, result.size(), "Should detect all alternate audit field names");
        assertTrue(result.values().contains(AuditFieldType.CREATED_AT),
            "Should detect create_date as CREATED_AT");
        assertTrue(result.values().contains(AuditFieldType.CREATED_BY),
            "Should detect creator_id as CREATED_BY");
        assertTrue(result.values().contains(AuditFieldType.UPDATED_AT),
            "Should detect last_update as UPDATED_AT");
        assertTrue(result.values().contains(AuditFieldType.UPDATED_BY),
            "Should detect modifier_id as UPDATED_BY");
    }

    private ColumnMetadata createColumn(String name) {
        ColumnMetadata column = new ColumnMetadata();
        column.setColumnName(name);
        column.setColumnTypeName("VARCHAR");
        column.setColumnType(Types.VARCHAR);
        column.setIsNullable(1);
        return column;
    }
}
