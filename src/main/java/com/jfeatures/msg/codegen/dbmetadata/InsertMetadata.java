package com.jfeatures.msg.codegen.dbmetadata;

import java.util.List;

/**
 * Metadata extracted from INSERT statements using database metadata approach.
 * Following Vipin's Principle: Single responsibility - data container only.
 */
public record InsertMetadata(
    String tableName,
    List<ColumnMetadata> insertColumns,
    String originalSql
) {}