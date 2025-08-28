package com.jfeatures.msg.codegen.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DBColumnTest {

    @Test
    void shouldCreateDBColumnWithAllFields() {
        // When
        DBColumn column = new DBColumn("customer", "customer_id", "java.lang.Integer", "INTEGER");
        
        // Then
        assertThat(column.tableName()).isEqualTo("customer");
        assertThat(column.columnName()).isEqualTo("customer_id");
        assertThat(column.javaType()).isEqualTo("java.lang.Integer");
        assertThat(column.jdbcType()).isEqualTo("INTEGER");
    }

    @Test
    void shouldHandleStringColumns() {
        // When
        DBColumn column = new DBColumn("customer", "first_name", "java.lang.String", "VARCHAR");
        
        // Then
        assertThat(column.tableName()).isEqualTo("customer");
        assertThat(column.columnName()).isEqualTo("first_name");
        assertThat(column.javaType()).isEqualTo("java.lang.String");
        assertThat(column.jdbcType()).isEqualTo("VARCHAR");
    }

    @Test
    void shouldBeValueObject() {
        // Given
        DBColumn column1 = new DBColumn("customer", "id", "Integer", "INTEGER");
        DBColumn column2 = new DBColumn("customer", "id", "Integer", "INTEGER");
        
        // Then - should be equal (records implement equals/hashCode)
        assertThat(column1).isEqualTo(column2);
        assertThat(column1.hashCode()).isEqualTo(column2.hashCode());
    }
}