package com.jfeatures.msg.codegen.util;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.domain.AuditConfiguration;
import java.util.List;

/**
 * Utility for partitioning columns into regular and audit columns.
 * Eliminates code duplication between InsertMicroserviceGenerator and UpdateMicroserviceGenerator.
 */
public final class AuditColumnPartitioner {

    private AuditColumnPartitioner() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Partitions columns into regular and audit columns based on audit configuration.
     * Regular columns are populated from the DTO, audit columns from the user header.
     *
     * @param allColumns All columns from the SQL statement
     * @param auditConfig Audit configuration to check which columns are audit columns
     * @param regularColumns Output list for regular columns (will be modified)
     * @param auditColumns Output list for audit columns (will be modified)
     */
    public static void partitionColumns(List<ColumnMetadata> allColumns, AuditConfiguration auditConfig,
                                        List<ColumnMetadata> regularColumns, List<ColumnMetadata> auditColumns) {
        for (ColumnMetadata column : allColumns) {
            if (auditConfig.isAuditColumn(column.getColumnName())) {
                auditColumns.add(column);
            } else {
                regularColumns.add(column);
            }
        }
    }
}
