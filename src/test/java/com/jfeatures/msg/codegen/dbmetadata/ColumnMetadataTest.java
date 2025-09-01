package com.jfeatures.msg.codegen.dbmetadata;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ColumnMetadataTest {

    @Test
    void shouldCreateColumnMetadataWithSetters() {
        // Given
        ColumnMetadata metadata = new ColumnMetadata();
        
        // When
        metadata.setColumnName("customer_id");
        metadata.setColumnTypeName("INT");
        metadata.setIsNullable(0);
        
        // Then
        assertThat(metadata.getColumnName()).isEqualTo("customer_id");
        assertThat(metadata.getColumnTypeName()).isEqualTo("INT");
        assertThat(metadata.getIsNullable()).isEqualTo(0);
    }

    @Test
    void shouldHandleNullableFields() {
        // Given
        ColumnMetadata metadata = new ColumnMetadata();
        
        // When
        metadata.setColumnName("email");
        metadata.setIsNullable(1);
        
        // Then
        assertThat(metadata.getColumnName()).isEqualTo("email");
        assertThat(metadata.getIsNullable()).isEqualTo(1);
    }

    @Test
    void shouldSetAllProperties() {
        // Given
        ColumnMetadata metadata = new ColumnMetadata();
        
        // When
        metadata.setColumnName("first_name");
        metadata.setColumnTypeName("VARCHAR");
        metadata.setColumnType(12);
        metadata.setIsNullable(1);
        metadata.setAutoIncrement(false);
        metadata.setColumnClassName("java.lang.String");
        
        // Then
        assertThat(metadata.getColumnName()).isEqualTo("first_name");
        assertThat(metadata.getColumnTypeName()).isEqualTo("VARCHAR");
        assertThat(metadata.getColumnType()).isEqualTo(12);
        assertThat(metadata.getIsNullable()).isEqualTo(1);
        assertThat(metadata.isAutoIncrement()).isFalse();
        assertThat(metadata.getColumnClassName()).isEqualTo("java.lang.String");
    }
}