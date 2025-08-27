package com.jfeatures.msg.codegen.dbmetadata;

import java.util.List;

/**
 * Metadata extracted from UPDATE statements using database metadata approach.
 */
public record UpdateMetadata(
    String tableName,
    List<ColumnMetadata> setColumns,
    List<ColumnMetadata> whereColumns,
    String originalSql
) {}