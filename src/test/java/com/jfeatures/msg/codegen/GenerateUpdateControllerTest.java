package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.squareup.javapoet.JavaFile;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateUpdateControllerTest {

    @Test
    void shouldGenerateSimpleUpdateController() throws Exception {
        // Given
        List<ColumnMetadata> setColumns = List.of(
                new ColumnMetadata("first_name", "VARCHAR", Types.VARCHAR, true, false),
                new ColumnMetadata("email", "VARCHAR", Types.VARCHAR, false, false)
        );

        List<ColumnMetadata> whereColumns = List.of(
                new ColumnMetadata("customer_id", "INT", Types.INTEGER, false, true)
        );

        UpdateMetadata updateMetadata = new UpdateMetadata(
                "customer", 
                setColumns, 
                whereColumns, 
                "UPDATE customer SET first_name = ?, email = ? WHERE customer_id = ?"
        );

        // When
        JavaFile result = GenerateUpdateController.createUpdateController("Customer", updateMetadata);

        // Then
        assertThat(result).isNotNull();
        String generatedCode = result.toString();
        
        // Check class name and annotations
        assertThat(generatedCode).contains("class CustomerUpdateController");
        assertThat(generatedCode).contains("@RestController");
        assertThat(generatedCode).contains("@RequestMapping(path = \"/api\")");
        assertThat(generatedCode).contains("@Tag(name = \"Customer Update API\")");
        
        // Check constructor
        assertThat(generatedCode).contains("public CustomerUpdateController(CustomerUpdateDAO customerUpdateDAO)");
        
        // Check PUT method
        assertThat(generatedCode).contains("@PutMapping(value = \"/customer/{id}\")");
        assertThat(generatedCode).contains("public ResponseEntity<Void> updateCustomer(");
        
        // Check method parameters
        assertThat(generatedCode).contains("@Valid @RequestBody CustomerUpdateDTO updateDto");
        assertThat(generatedCode).contains("@PathVariable(value = \"id\") Integer id");
        
        // Check method body
        assertThat(generatedCode).contains("customerUpdateDAO.updateCustomer(updateDto, id)");
        assertThat(generatedCode).contains("ResponseEntity.ok().build()");
        assertThat(generatedCode).contains("ResponseEntity.notFound().build()");
        
        // Check package
        assertThat(generatedCode).contains("package com.jfeatures.msg.Customer.controller");
    }

    @Test
    void shouldGenerateControllerWithMultipleWhereParameters() throws Exception {
        // Given
        List<ColumnMetadata> setColumns = List.of(
                new ColumnMetadata("active", "BIT", Types.BIT, false, false)
        );

        List<ColumnMetadata> whereColumns = List.of(
                new ColumnMetadata("customer_id", "INT", Types.INTEGER, false, true),
                new ColumnMetadata("store_id", "TINYINT", Types.TINYINT, false, false),
                new ColumnMetadata("active", "BIT", Types.BIT, false, false)
        );

        UpdateMetadata updateMetadata = new UpdateMetadata(
                "customer", 
                setColumns, 
                whereColumns, 
                "UPDATE customer SET active = ? WHERE customer_id = ? AND store_id = ? AND active = ?"
        );

        // When
        JavaFile result = GenerateUpdateController.createUpdateController("Customer", updateMetadata);

        // Then
        String generatedCode = result.toString();
        
        // Check method has path variable for first WHERE parameter
        assertThat(generatedCode).contains("@PathVariable(value = \"id\") Integer id");
        
        // Check remaining WHERE parameters are query parameters
        assertThat(generatedCode).contains("@RequestParam(value = \"status\", required = true) Byte status");
        assertThat(generatedCode).contains("@RequestParam(value = \"category\", required = true) Boolean category");
        
        // Check method call includes all parameters
        assertThat(generatedCode).contains("customerUpdateDAO.updateCustomer(updateDto, id, status, category)");
    }

    @Test
    void shouldGenerateControllerWithoutPathVariable() throws Exception {
        // Given
        List<ColumnMetadata> setColumns = List.of(
                new ColumnMetadata("active", "BIT", Types.BIT, false, false)
        );

        UpdateMetadata updateMetadata = new UpdateMetadata(
                "customer", 
                setColumns, 
                List.of(), // No WHERE parameters
                "UPDATE customer SET active = ?"
        );

        // When
        JavaFile result = GenerateUpdateController.createUpdateController("Customer", updateMetadata);

        // Then
        String generatedCode = result.toString();
        
        // Check PUT mapping without path variable
        assertThat(generatedCode).contains("@PutMapping(value = \"/customer\")");
        
        // Check method signature only has DTO parameter
        assertThat(generatedCode).contains("public ResponseEntity<Void> updateCustomer(@Valid @RequestBody CustomerUpdateDTO updateDto)");
        
        // Should not contain path variable or query parameters
        assertThat(generatedCode).doesNotContain("@PathVariable");
        assertThat(generatedCode).doesNotContain("@RequestParam");
        
        // Check method call only passes DTO
        assertThat(generatedCode).contains("customerUpdateDAO.updateCustomer(updateDto)");
    }

    @Test
    void shouldIncludeSwaggerAnnotations() throws Exception {
        // Given
        List<ColumnMetadata> setColumns = List.of(
                new ColumnMetadata("email", "VARCHAR", Types.VARCHAR, false, false)
        );

        List<ColumnMetadata> whereColumns = List.of(
                new ColumnMetadata("customer_id", "INT", Types.INTEGER, false, true)
        );

        UpdateMetadata updateMetadata = new UpdateMetadata(
                "customer", 
                setColumns, 
                whereColumns, 
                "UPDATE customer SET email = ? WHERE customer_id = ?"
        );

        // When
        JavaFile result = GenerateUpdateController.createUpdateController("Customer", updateMetadata);

        // Then
        String generatedCode = result.toString();
        
        // Check Swagger/OpenAPI annotations
        assertThat(generatedCode).contains("@Operation");
        assertThat(generatedCode).contains("@ApiResponses");
        assertThat(generatedCode).contains("@Parameter");
        
        // Check specific Swagger documentation
        assertThat(generatedCode).contains("summary = \"Update customer record\"");
        assertThat(generatedCode).contains("description = \"Updated customer data\"");
        assertThat(generatedCode).contains("responseCode = \"200\", description = \"Successfully updated\"");
        assertThat(generatedCode).contains("responseCode = \"404\", description = \"Record not found\"");
    }

    @Test
    void shouldIncludeProperImports() throws Exception {
        // Given
        List<ColumnMetadata> setColumns = List.of(
                new ColumnMetadata("email", "VARCHAR", Types.VARCHAR, false, false)
        );

        UpdateMetadata updateMetadata = new UpdateMetadata(
                "customer", 
                setColumns, 
                List.of(), 
                "UPDATE customer SET email = ?"
        );

        // When
        JavaFile result = GenerateUpdateController.createUpdateController("Customer", updateMetadata);

        // Then
        String generatedCode = result.toString();
        
        // Check Spring Web annotations
        assertThat(generatedCode).contains("import org.springframework.web.bind.annotation.RestController");
        assertThat(generatedCode).contains("import org.springframework.web.bind.annotation.PutMapping");
        assertThat(generatedCode).contains("import org.springframework.web.bind.annotation.RequestBody");
        assertThat(generatedCode).contains("import org.springframework.http.ResponseEntity");
        
        // Check validation annotations
        assertThat(generatedCode).contains("import jakarta.validation.Valid");
        
        // Check Swagger imports
        assertThat(generatedCode).contains("import io.swagger.v3.oas.annotations.Operation");
        assertThat(generatedCode).contains("import io.swagger.v3.oas.annotations.responses.ApiResponses");
    }

    @Test
    void shouldIncludeJavaDocumentation() throws Exception {
        // Given
        List<ColumnMetadata> setColumns = List.of(
                new ColumnMetadata("email", "VARCHAR", Types.VARCHAR, false, false)
        );

        List<ColumnMetadata> whereColumns = List.of(
                new ColumnMetadata("customer_id", "INT", Types.INTEGER, false, true)
        );

        UpdateMetadata updateMetadata = new UpdateMetadata(
                "customer", 
                setColumns, 
                whereColumns, 
                "UPDATE customer SET email = ? WHERE customer_id = ?"
        );

        // When
        JavaFile result = GenerateUpdateController.createUpdateController("Customer", updateMetadata);

        // Then
        String generatedCode = result.toString();
        
        // Check JavaDoc is present
        assertThat(generatedCode).contains("/**");
        assertThat(generatedCode).contains("Updates a customer record");
        assertThat(generatedCode).contains("@param updateDto");
        assertThat(generatedCode).contains("@param id");
        assertThat(generatedCode).contains("@return ResponseEntity");
    }

    @Test
    void shouldHandleComplexDataTypes() throws Exception {
        // Given
        List<ColumnMetadata> setColumns = List.of(
                new ColumnMetadata("create_date", "DATETIME", Types.TIMESTAMP, true, false),
                new ColumnMetadata("active", "BIT", Types.BIT, false, false),
                new ColumnMetadata("store_id", "TINYINT", Types.TINYINT, false, false)
        );

        List<ColumnMetadata> whereColumns = List.of(
                new ColumnMetadata("customer_id", "BIGINT", Types.BIGINT, false, true)
        );

        UpdateMetadata updateMetadata = new UpdateMetadata(
                "customer", 
                setColumns, 
                whereColumns, 
                "UPDATE customer SET create_date = ?, active = ?, store_id = ? WHERE customer_id = ?"
        );

        // When
        JavaFile result = GenerateUpdateController.createUpdateController("Customer", updateMetadata);

        // Then
        String generatedCode = result.toString();
        
        // Check correct Java types are used for path variable
        assertThat(generatedCode).contains("@PathVariable(value = \"id\") Long id");
        
        // Check DAO method call
        assertThat(generatedCode).contains("customerUpdateDAO.updateCustomer(updateDto, id)");
    }

    @Test
    void shouldGenerateCorrectMappingPaths() throws Exception {
        // Given
        List<ColumnMetadata> setColumns = List.of(
                new ColumnMetadata("name", "VARCHAR", Types.VARCHAR, true, false)
        );

        List<ColumnMetadata> whereColumns = List.of(
                new ColumnMetadata("id", "INT", Types.INTEGER, false, true)
        );

        UpdateMetadata updateMetadata = new UpdateMetadata(
                "product", 
                setColumns, 
                whereColumns, 
                "UPDATE product SET name = ? WHERE id = ?"
        );

        // When
        JavaFile result = GenerateUpdateController.createUpdateController("Product", updateMetadata);

        // Then
        String generatedCode = result.toString();
        
        // Check correct URL mapping
        assertThat(generatedCode).contains("@PutMapping(value = \"/product/{id}\")");
        assertThat(generatedCode).contains("class ProductUpdateController");
        
        // Check package reflects business purpose
        assertThat(generatedCode).contains("package com.jfeatures.msg.Product.controller");
    }
}