package com.jfeatures.msg.codegen.dbmetadata;

import java.util.List;

/**
 * Metadata extracted from DELETE statements using database metadata approach.
 * Following Vipin's Principle: Single responsibility - data container only.
 */
public record DeleteMetadata(
    String tableName,
    List<ColumnMetadata> whereColumns,
    String originalSql
) {}