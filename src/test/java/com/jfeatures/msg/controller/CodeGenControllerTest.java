package com.jfeatures.msg.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.SqlMetadata;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

/**
 * Tests for CodeGenController to ensure proper REST endpoint behavior.
 */
@ExtendWith(MockitoExtension.class)
class CodeGenControllerTest {

    @Mock
    private SqlMetadata sqlMetadata;

    private CodeGenController controller;

    @BeforeEach
    void setUp() {
        controller = new CodeGenController(sqlMetadata);
    }

    @Test
    void selectColumnMetadata_WithValidSql_ReturnsColumnMetadata() {
        // Given
        ColumnMetadata column1 = createColumnMetadata("customer_id", "INT", Types.INTEGER);
        ColumnMetadata column2 = createColumnMetadata("customer_name", "VARCHAR", Types.VARCHAR);
        List<ColumnMetadata> expectedMetadata = Arrays.asList(column1, column2);

        when(sqlMetadata.getColumnMetadata(anyString())).thenReturn(expectedMetadata);

        // When
        List<ColumnMetadata> result = controller.selectColumnMetadata();

        // Then
        assertThat(result)
            .isNotNull()
            .hasSize(2)
            .satisfies(list -> {
                assertThat(list.get(0).getColumnName()).isEqualTo("customer_id");
                assertThat(list.get(1).getColumnName()).isEqualTo("customer_name");
            });
    }

    @Test
    void selectColumnMetadata_WhenDataAccessExceptionThrown_ThrowsIllegalStateException() {
        // Given
        when(sqlMetadata.getColumnMetadata(anyString()))
            .thenThrow(new DataAccessException("Database error") {});

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> controller.selectColumnMetadata()
        );

        assertEquals("Failed to get column metadata", exception.getMessage());
    }

    @Test
    void selectColumnMetadata_WhenIllegalArgumentExceptionThrown_ThrowsIllegalStateException() {
        // Given
        when(sqlMetadata.getColumnMetadata(anyString()))
            .thenThrow(new IllegalArgumentException("Invalid SQL"));

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> controller.selectColumnMetadata()
        );

        assertEquals("Failed to get column metadata", exception.getMessage());
    }

    @Test
    void selectColumnMetadata_WithEmptyResult_ReturnsEmptyList() {
        // Given
        when(sqlMetadata.getColumnMetadata(anyString())).thenReturn(List.of());

        // When
        List<ColumnMetadata> result = controller.selectColumnMetadata();

        // Then
        assertThat(result)
            .isNotNull()
            .isEmpty();
    }

    private ColumnMetadata createColumnMetadata(String name, String typeName, int typeCode) {
        ColumnMetadata metadata = new ColumnMetadata();
        metadata.setColumnName(name);
        metadata.setColumnTypeName(typeName);
        metadata.setColumnType(typeCode);
        metadata.setIsNullable(1); // 1 = nullable
        return metadata;
    }
}
