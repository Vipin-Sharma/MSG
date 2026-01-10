package com.jfeatures.msg.codegen.dbmetadata;

import com.jfeatures.msg.codegen.audit.AuditFieldType;

import java.util.List;
import java.util.Map;

/**
 * Metadata extracted from INSERT statements using database metadata approach.
 * Following Vipin's Principle: Single responsibility - data container only.
 */
public record InsertMetadata(
    String tableName,
    List<ColumnMetadata> insertColumns,
    String originalSql,
    Map<ColumnMetadata, AuditFieldType> auditFields
) {}