package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.squareup.javapoet.JavaFile;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateUpdateDTOTest {

    @Test
    void shouldGenerateSimpleUpdateDTO() throws Exception {
        // Given
        List<ColumnMetadata> setColumns = List.of(
                new ColumnMetadata("first_name", "VARCHAR", Types.VARCHAR, true, false),
                new ColumnMetadata("last_name", "VARCHAR", Types.VARCHAR, true, false),
                new ColumnMetadata("email", "VARCHAR", Types.VARCHAR, false, false)
        );

        List<ColumnMetadata> whereColumns = List.of(
                new ColumnMetadata("customer_id", "INT", Types.INTEGER, false, true)
        );

        UpdateMetadata updateMetadata = new UpdateMetadata(
                "customer", 
                setColumns, 
                whereColumns, 
                "UPDATE customer SET first_name = ?, last_name = ?, email = ? WHERE customer_id = ?"
        );

        // When
        JavaFile result = GenerateUpdateDTO.createUpdateDTO("Customer", updateMetadata);

        // Then
        assertThat(result).isNotNull();
        String generatedCode = result.toString();
        
        // Check class name and annotations
        assertThat(generatedCode).contains("class CustomerUpdateDTO");
        assertThat(generatedCode).contains("@Data");
        
        // Check fields are generated
        assertThat(generatedCode).contains("private String firstName");
        assertThat(generatedCode).contains("private String lastName");
        assertThat(generatedCode).contains("private String email");
        
        // Check NotNull annotations for non-nullable fields
        assertThat(generatedCode).contains("@NotNull");
        
        // Check package
        assertThat(generatedCode).contains("package com.jfeatures.msg.Customer.dto");
    }

    @Test
    void shouldGenerateUpdateDTOWithDifferentDataTypes() throws Exception {
        // Given
        List<ColumnMetadata> setColumns = List.of(
                new ColumnMetadata("customer_id", "INT", Types.INTEGER, false, true),
                new ColumnMetadata("active", "BIT", Types.BIT, false, false),
                new ColumnMetadata("create_date", "DATETIME", Types.TIMESTAMP, true, false),
                new ColumnMetadata("store_id", "TINYINT", Types.TINYINT, false, false)
        );

        UpdateMetadata updateMetadata = new UpdateMetadata(
                "customer", 
                setColumns, 
                List.of(), 
                "UPDATE customer SET customer_id = ?, active = ?, create_date = ?, store_id = ?"
        );

        // When
        JavaFile result = GenerateUpdateDTO.createUpdateDTO("Customer", updateMetadata);

        // Then
        String generatedCode = result.toString();
        
        // Check different Java types are used
        assertThat(generatedCode).contains("private Integer customerId");
        assertThat(generatedCode).contains("private Boolean active");
        assertThat(generatedCode).contains("private java.sql.Timestamp createDate");
        assertThat(generatedCode).contains("private Byte storeId");
    }

    @Test
    void shouldGenerateWhereDTO() throws Exception {
        // Given
        List<ColumnMetadata> whereColumns = List.of(
                new ColumnMetadata("customer_id", "INT", Types.INTEGER, false, true),
                new ColumnMetadata("active", "BIT", Types.BIT, false, false)
        );

        UpdateMetadata updateMetadata = new UpdateMetadata(
                "customer", 
                List.of(), 
                whereColumns, 
                "UPDATE customer SET email = ? WHERE customer_id = ? AND active = ?"
        );

        // When
        JavaFile result = GenerateUpdateDTO.createUpdateWhereDTO("Customer", updateMetadata);

        // Then
        assertThat(result).isNotNull();
        String generatedCode = result.toString();
        
        // Check class name
        assertThat(generatedCode).contains("class CustomerUpdateWhereDTO");
        
        // Check WHERE parameters are included
        assertThat(generatedCode).contains("private Integer id"); // First param becomes "id"
        assertThat(generatedCode).contains("private Boolean status"); // Second param becomes "status"
        
        // All WHERE parameters should be @NotNull
        assertThat(generatedCode).contains("@NotNull");
    }

    @Test
    void shouldReturnNullWhereDTOWhenNoWhereParameters() throws Exception {
        // Given
        UpdateMetadata updateMetadata = new UpdateMetadata(
                "customer", 
                List.of(new ColumnMetadata("email", "VARCHAR", Types.VARCHAR, true, false)), 
                List.of(), // No WHERE parameters
                "UPDATE customer SET email = ?"
        );

        // When
        JavaFile result = GenerateUpdateDTO.createUpdateWhereDTO("Customer", updateMetadata);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldHandleComplexColumnNames() throws Exception {
        // Given
        List<ColumnMetadata> setColumns = List.of(
                new ColumnMetadata("first_name", "VARCHAR", Types.VARCHAR, true, false),
                new ColumnMetadata("last_update", "DATETIME", Types.TIMESTAMP, false, false),
                new ColumnMetadata("address_id", "INT", Types.INTEGER, true, false)
        );

        UpdateMetadata updateMetadata = new UpdateMetadata(
                "customer", 
                setColumns, 
                List.of(), 
                "UPDATE customer SET first_name = ?, last_update = ?, address_id = ?"
        );

        // When
        JavaFile result = GenerateUpdateDTO.createUpdateDTO("Customer", updateMetadata);

        // Then
        String generatedCode = result.toString();
        
        // Check camelCase conversion
        assertThat(generatedCode).contains("private String firstName");
        assertThat(generatedCode).contains("private java.sql.Timestamp lastUpdate");
        assertThat(generatedCode).contains("private Integer addressId");
    }

    @Test
    void shouldIncludeJavaDoc() throws Exception {
        // Given
        List<ColumnMetadata> setColumns = List.of(
                new ColumnMetadata("email", "VARCHAR", Types.VARCHAR, true, false)
        );

        UpdateMetadata updateMetadata = new UpdateMetadata(
                "customer", 
                setColumns, 
                List.of(), 
                "UPDATE customer SET email = ?"
        );

        // When
        JavaFile result = GenerateUpdateDTO.createUpdateDTO("Customer", updateMetadata);

        // Then
        String generatedCode = result.toString();
        
        // Check JavaDoc is present
        assertThat(generatedCode).contains("/**");
        assertThat(generatedCode).contains("DTO for updating customer entity");
        assertThat(generatedCode).contains("The email to update");
    }
}