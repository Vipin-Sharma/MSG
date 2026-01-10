package com.jfeatures.msg.codegen.dbmetadata;

import com.jfeatures.msg.codegen.audit.AuditFieldType;

import java.util.List;
import java.util.Map;

/**
 * Metadata extracted from UPDATE statements using database metadata approach.
 */
public record UpdateMetadata(
    String tableName,
    List<ColumnMetadata> setColumns,
    List<ColumnMetadata> whereColumns,
    String originalSql,
    Map<ColumnMetadata, AuditFieldType> auditFields
) {}