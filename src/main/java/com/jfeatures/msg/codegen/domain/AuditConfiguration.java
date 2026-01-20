package com.jfeatures.msg.codegen.domain;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration for audit/user context support in generated microservices.
 * Immutable record following project patterns.
 *
 * When enabled, audit columns (like created_by, updated_by) are populated
 * from HTTP headers instead of request body, providing automatic user tracking.
 */
public record AuditConfiguration(
    boolean enabled,
    String userHeaderName,
    Set<String> auditColumnNames
) {

    /**
     * Default HTTP header name for user identification.
     */
    public static final String DEFAULT_USER_HEADER = "X-User-ID";

    /**
     * Compact constructor with validation and defensive copying.
     */
    public AuditConfiguration {
        if (enabled && (auditColumnNames == null || auditColumnNames.isEmpty())) {
            throw new IllegalArgumentException(
                "Audit columns must be specified when audit is enabled. " +
                "Use --audit-columns=\"created_by,updated_by\" to specify column names."
            );
        }
        if (userHeaderName == null || userHeaderName.trim().isEmpty()) {
            userHeaderName = DEFAULT_USER_HEADER;
        }
        // Defensive copy to immutable set - prevents external modification
        auditColumnNames = (auditColumnNames == null)
            ? Set.of()
            : Set.copyOf(auditColumnNames);
    }

    /**
     * Factory method for disabled audit configuration.
     * Use this when --enable-audit is not provided.
     *
     * @return AuditConfiguration with audit disabled
     */
    public static AuditConfiguration disabled() {
        return new AuditConfiguration(false, DEFAULT_USER_HEADER, Set.of());
    }

    /**
     * Checks if a column name is an audit column.
     * Comparison is case-insensitive to handle different database naming conventions.
     * Uses optimized O(n) lookup where n is the number of audit columns (typically 1-3).
     *
     * @param columnName the column name to check
     * @return true if this column should be populated from user context header
     */
    public boolean isAuditColumn(String columnName) {
        if (!enabled || columnName == null || columnName.trim().isEmpty()) {
            return false;
        }
        String lowerColumnName = columnName.toLowerCase();
        return auditColumnNames.stream()
            .anyMatch(auditCol -> auditCol.toLowerCase().equals(lowerColumnName));
    }

    /**
     * Returns the number of configured audit columns.
     *
     * @return count of audit columns
     */
    public int auditColumnCount() {
        return auditColumnNames.size();
    }
}
