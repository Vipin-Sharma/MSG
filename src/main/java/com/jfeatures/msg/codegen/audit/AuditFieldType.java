package com.jfeatures.msg.codegen.audit;

/**
 * Enum representing types of audit fields in database tables.
 * These fields are automatically populated by the system and excluded from request DTOs.
 */
public enum AuditFieldType {
    /**
     * Timestamp when the record was created.
     * Auto-populated with current timestamp on INSERT operations.
     */
    CREATED_AT,

    /**
     * User ID who created the record.
     * Auto-populated with authenticated user ID on INSERT operations.
     */
    CREATED_BY,

    /**
     * Timestamp when the record was last updated.
     * Auto-populated with current timestamp on UPDATE operations.
     */
    UPDATED_AT,

    /**
     * User ID who last updated the record.
     * Auto-populated with authenticated user ID on UPDATE operations.
     */
    UPDATED_BY;

    /**
     * Checks if this audit field type is related to creation operations.
     * @return true if this is CREATED_AT or CREATED_BY
     */
    public boolean isCreationAudit() {
        return this == CREATED_AT || this == CREATED_BY;
    }

    /**
     * Checks if this audit field type is related to update operations.
     * @return true if this is UPDATED_AT or UPDATED_BY
     */
    public boolean isUpdateAudit() {
        return this == UPDATED_AT || this == UPDATED_BY;
    }

    /**
     * Checks if this audit field type is a timestamp field.
     * @return true if this is CREATED_AT or UPDATED_AT
     */
    public boolean isTimestampField() {
        return this == CREATED_AT || this == UPDATED_AT;
    }

    /**
     * Checks if this audit field type is a user ID field.
     * @return true if this is CREATED_BY or UPDATED_BY
     */
    public boolean isUserIdField() {
        return this == CREATED_BY || this == UPDATED_BY;
    }
}
