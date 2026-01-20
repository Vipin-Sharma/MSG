package com.jfeatures.msg.codegen.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.domain.AuditConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AuditColumnPartitionerTest {

    @Test
    void partitionColumns_WithAuditEnabled_SeparatesAuditColumns() {
        // Given
        AuditConfiguration auditConfig = new AuditConfiguration(true, "X-User-ID",
            Set.of("created_by", "updated_by"));

        List<ColumnMetadata> allColumns = List.of(
            createColumnMetadata("customer_name"),
            createColumnMetadata("email"),
            createColumnMetadata("created_by"),
            createColumnMetadata("updated_by")
        );

        List<ColumnMetadata> regularColumns = new ArrayList<>();
        List<ColumnMetadata> auditColumns = new ArrayList<>();

        // When
        AuditColumnPartitioner.partitionColumns(allColumns, auditConfig, regularColumns, auditColumns);

        // Then
        assertThat(regularColumns).hasSize(2);
        assertThat(regularColumns).extracting(ColumnMetadata::getColumnName)
            .containsExactly("customer_name", "email");

        assertThat(auditColumns).hasSize(2);
        assertThat(auditColumns).extracting(ColumnMetadata::getColumnName)
            .containsExactly("created_by", "updated_by");
    }

    @Test
    void partitionColumns_WithAuditDisabled_AllColumnsAreRegular() {
        // Given
        AuditConfiguration auditConfig = AuditConfiguration.disabled();

        List<ColumnMetadata> allColumns = List.of(
            createColumnMetadata("customer_name"),
            createColumnMetadata("created_by"),
            createColumnMetadata("updated_by")
        );

        List<ColumnMetadata> regularColumns = new ArrayList<>();
        List<ColumnMetadata> auditColumns = new ArrayList<>();

        // When
        AuditColumnPartitioner.partitionColumns(allColumns, auditConfig, regularColumns, auditColumns);

        // Then
        assertThat(regularColumns).hasSize(3);
        assertThat(auditColumns).isEmpty();
    }

    @Test
    void partitionColumns_CaseInsensitiveMatching() {
        // Given
        AuditConfiguration auditConfig = new AuditConfiguration(true, "X-User-ID",
            Set.of("CREATED_BY"));

        List<ColumnMetadata> allColumns = List.of(
            createColumnMetadata("customer_name"),
            createColumnMetadata("created_by")  // lowercase in column, uppercase in config
        );

        List<ColumnMetadata> regularColumns = new ArrayList<>();
        List<ColumnMetadata> auditColumns = new ArrayList<>();

        // When
        AuditColumnPartitioner.partitionColumns(allColumns, auditConfig, regularColumns, auditColumns);

        // Then
        assertThat(regularColumns).hasSize(1);
        assertThat(auditColumns).hasSize(1);
        assertThat(auditColumns.get(0).getColumnName()).isEqualTo("created_by");
    }

    @Test
    void partitionColumns_EmptyColumnList() {
        // Given
        AuditConfiguration auditConfig = new AuditConfiguration(true, "X-User-ID",
            Set.of("created_by"));

        List<ColumnMetadata> allColumns = List.of();
        List<ColumnMetadata> regularColumns = new ArrayList<>();
        List<ColumnMetadata> auditColumns = new ArrayList<>();

        // When
        AuditColumnPartitioner.partitionColumns(allColumns, auditConfig, regularColumns, auditColumns);

        // Then
        assertThat(regularColumns).isEmpty();
        assertThat(auditColumns).isEmpty();
    }

    @Test
    void partitionColumns_NoAuditColumnsInList() {
        // Given
        AuditConfiguration auditConfig = new AuditConfiguration(true, "X-User-ID",
            Set.of("created_by", "updated_by"));

        List<ColumnMetadata> allColumns = List.of(
            createColumnMetadata("customer_name"),
            createColumnMetadata("email")
        );

        List<ColumnMetadata> regularColumns = new ArrayList<>();
        List<ColumnMetadata> auditColumns = new ArrayList<>();

        // When
        AuditColumnPartitioner.partitionColumns(allColumns, auditConfig, regularColumns, auditColumns);

        // Then
        assertThat(regularColumns).hasSize(2);
        assertThat(auditColumns).isEmpty();
    }

    @Test
    void partitionColumns_AllColumnsAreAuditColumns() {
        // Given
        AuditConfiguration auditConfig = new AuditConfiguration(true, "X-User-ID",
            Set.of("created_by", "updated_by"));

        List<ColumnMetadata> allColumns = List.of(
            createColumnMetadata("created_by"),
            createColumnMetadata("updated_by")
        );

        List<ColumnMetadata> regularColumns = new ArrayList<>();
        List<ColumnMetadata> auditColumns = new ArrayList<>();

        // When
        AuditColumnPartitioner.partitionColumns(allColumns, auditConfig, regularColumns, auditColumns);

        // Then
        assertThat(regularColumns).isEmpty();
        assertThat(auditColumns).hasSize(2);
    }

    private ColumnMetadata createColumnMetadata(String columnName) {
        ColumnMetadata metadata = new ColumnMetadata();
        metadata.setColumnName(columnName);
        metadata.setColumnTypeName("VARCHAR");
        return metadata;
    }
}
