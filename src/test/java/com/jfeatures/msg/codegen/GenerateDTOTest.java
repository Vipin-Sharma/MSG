package com.jfeatures.msg.codegen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.test.TestUtils;
import com.squareup.javapoet.JavaFile;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GenerateDTOTest {

    @Test
    void shouldGenerateBasicDTO() throws Exception {
        // Given
        List<ColumnMetadata> columnMetadata = Arrays.asList(
            TestUtils.createColumnMetadata("id", "INT", java.sql.Types.INTEGER, false),
            TestUtils.createColumnMetadata("name", "VARCHAR", java.sql.Types.VARCHAR, true)
        );

        // When
        JavaFile result = GenerateDTO.dtoFromColumnMetadata(columnMetadata, "Customer");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.packageName).isEqualTo("com.jfeatures.msg.customer.dto");
        assertThat(result.typeSpec.name).isEqualTo("CustomerDTO");
        
        String generatedCode = result.toString();
        assertThat(generatedCode).contains("@Builder");
        assertThat(generatedCode).contains("@Value");
        assertThat(generatedCode).contains("@Jacksonized");
        assertThat(generatedCode).contains("public Integer id;");
        assertThat(generatedCode).contains("public String name;");
    }

    @Test
    void shouldHandleSnakeCaseFieldNames() throws Exception {
        // Given
        List<ColumnMetadata> columnMetadata = Arrays.asList(
            TestUtils.createColumnMetadata("user_id", "INT", java.sql.Types.INTEGER, false),
            TestUtils.createColumnMetadata("created_at", "TIMESTAMP", java.sql.Types.TIMESTAMP, true)
        );

        // When
        JavaFile result = GenerateDTO.dtoFromColumnMetadata(columnMetadata, "UserInfo");

        // Then
        String generatedCode = result.toString();
        assertThat(generatedCode).contains("public Integer userId;");
        assertThat(generatedCode).contains("public Timestamp createdAt;");
    }

    @Test
    void shouldHandleColumnAlias() throws Exception {
        // Given
        ColumnMetadata columnWithAlias = TestUtils.createColumnMetadata("id", "INT", java.sql.Types.INTEGER, false);
        columnWithAlias.setColumnAlias("customerId");
        
        List<ColumnMetadata> columnMetadata = Arrays.asList(columnWithAlias);

        // When
        JavaFile result = GenerateDTO.dtoFromColumnMetadata(columnMetadata, "Order");

        // Then
        String generatedCode = result.toString();
        assertThat(generatedCode).contains("public Integer customerId;");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Product", "OrderDetails", "CustomerInfo"})
    void shouldGenerateDTOForDifferentBusinessPurposes(String businessPurpose) throws Exception {
        // Given
        List<ColumnMetadata> columnMetadata = Arrays.asList(
            TestUtils.createColumnMetadata("id", "INT", java.sql.Types.INTEGER, false)
        );

        // When
        JavaFile result = GenerateDTO.dtoFromColumnMetadata(columnMetadata, businessPurpose);

        // Then
        assertThat(result.typeSpec.name).isEqualTo(businessPurpose + "DTO");
        assertThat(result.packageName).isEqualTo("com.jfeatures.msg." + businessPurpose.toLowerCase() + ".dto");
    }

    @Test
    void shouldHandleEmptyColumnList() throws Exception {
        // Given
        List<ColumnMetadata> emptyColumnMetadata = Arrays.asList();

        // When
        JavaFile result = GenerateDTO.dtoFromColumnMetadata(emptyColumnMetadata, "Empty");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.typeSpec.fieldSpecs).isEmpty();
    }

    @Test
    void shouldHandleMultipleDataTypes() throws Exception {
        // Given
        List<ColumnMetadata> columnMetadata = Arrays.asList(
            TestUtils.createColumnMetadata("id", "BIGINT", java.sql.Types.BIGINT, false),
            TestUtils.createColumnMetadata("price", "DECIMAL", java.sql.Types.DECIMAL, false),
            TestUtils.createColumnMetadata("active", "BIT", java.sql.Types.BIT, false),
            TestUtils.createColumnMetadata("created_date", "DATE", java.sql.Types.DATE, true)
        );

        // When
        JavaFile result = GenerateDTO.dtoFromColumnMetadata(columnMetadata, "Product");

        // Then
        String generatedCode = result.toString();
        assertThat(generatedCode).contains("public Long id;");
        assertThat(generatedCode).contains("public BigDecimal price;");
        assertThat(generatedCode).contains("public Boolean active;");
        assertThat(generatedCode).contains("public Date createdDate;");
    }

    @Test
    void shouldThrowExceptionForInvalidClassName() {
        // Given
        ColumnMetadata invalidColumn = TestUtils.createColumnMetadata("id", "INT", java.sql.Types.INTEGER, false);
        invalidColumn.setColumnClassName("InvalidClassName");
        List<ColumnMetadata> columnMetadata = Arrays.asList(invalidColumn);

        // When & Then
        assertThatThrownBy(() -> GenerateDTO.dtoFromColumnMetadata(columnMetadata, "Test"))
            .isInstanceOf(ClassNotFoundException.class);
    }
}