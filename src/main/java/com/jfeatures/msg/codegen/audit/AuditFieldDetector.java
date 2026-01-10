package com.jfeatures.msg.codegen.audit;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for detecting audit fields in column metadata.
 * Follows Vipin's Principle: Single responsibility - audit field detection only.
 */
@Slf4j
public class AuditFieldDetector {

    private AuditFieldDetector() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Detect audit fields in a list of columns.
     * @param columns List of column metadata
     * @return Map of column metadata to audit field type
     */
    public static Map<ColumnMetadata, AuditFieldType> detectAuditFields(List<ColumnMetadata> columns) {
        Map<ColumnMetadata, AuditFieldType> auditFields = new HashMap<>();

        if (columns == null || columns.isEmpty()) {
            return auditFields;
        }

        AuditConfiguration config = AuditConfiguration.getInstance();

        for (ColumnMetadata column : columns) {
            Optional<AuditFieldType> auditFieldType = config.getAuditFieldType(column.getColumnName());
            if (auditFieldType.isPresent()) {
                auditFields.put(column, auditFieldType.get());
                log.debug("Detected audit field: {} -> {}", column.getColumnName(), auditFieldType.get());
            }
        }

        if (!auditFields.isEmpty()) {
            log.info("Detected {} audit field(s)", auditFields.size());
        }

        return auditFields;
    }

    /**
     * Check if a specific column is an audit field.
     * @param column Column metadata to check
     * @return Optional containing AuditFieldType if column is an audit field
     */
    public static Optional<AuditFieldType> getAuditFieldType(ColumnMetadata column) {
        if (column == null) {
            return Optional.empty();
        }

        AuditConfiguration config = AuditConfiguration.getInstance();
        return config.getAuditFieldType(column.getColumnName());
    }

    /**
     * Check if a column is an audit field.
     * @param column Column metadata to check
     * @return true if column is an audit field
     */
    public static boolean isAuditField(ColumnMetadata column) {
        return getAuditFieldType(column).isPresent();
    }

    /**
     * Filter out audit fields from a list of columns.
     * @param columns List of column metadata
     * @return List of columns excluding audit fields
     */
    public static List<ColumnMetadata> filterOutAuditFields(List<ColumnMetadata> columns) {
        if (columns == null || columns.isEmpty()) {
            return List.of();
        }

        return columns.stream()
                .filter(column -> !isAuditField(column))
                .toList();
    }

    /**
     * Get only audit fields from a list of columns.
     * @param columns List of column metadata
     * @return Map of audit fields to their types
     */
    public static Map<ColumnMetadata, AuditFieldType> getOnlyAuditFields(List<ColumnMetadata> columns) {
        return detectAuditFields(columns);
    }
}
