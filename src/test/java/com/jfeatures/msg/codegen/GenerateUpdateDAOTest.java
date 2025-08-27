package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.squareup.javapoet.JavaFile;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateUpdateDAOTest {

    @Test
    void shouldGenerateSimpleUpdateDAO() throws Exception {
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
        JavaFile result = GenerateUpdateDAO.createUpdateDAO("Customer", updateMetadata);

        // Then
        assertThat(result).isNotNull();
        String generatedCode = result.toString();
        
        // Check class name and annotations
        assertThat(generatedCode).contains("class CustomerUpdateDAO");
        assertThat(generatedCode).contains("@Component");
        assertThat(generatedCode).contains("@Slf4j");
        
        // Check constructor
        assertThat(generatedCode).contains("public CustomerUpdateDAO(NamedParameterJdbcTemplate namedParameterJdbcTemplate)");
        
        // Check update method
        assertThat(generatedCode).contains("public int updateCustomer(");
        assertThat(generatedCode).contains("CustomerUpdateDTO updateDto");
        assertThat(generatedCode).contains("Integer id"); // First WHERE parameter becomes "id"
        
        // Check SQL execution
        assertThat(generatedCode).contains("namedParameterJdbcTemplate.update(sql, paramMap)");
        assertThat(generatedCode).contains("log.info");
        
        // Check parameter mapping
        assertThat(generatedCode).contains("paramMap.put(\"firstName\", updateDto.getFirstName())");
        assertThat(generatedCode).contains("paramMap.put(\"email\", updateDto.getEmail())");
        assertThat(generatedCode).contains("paramMap.put(\"id\", id)");
        
        // Check package
        assertThat(generatedCode).contains("package com.jfeatures.msg.Customer.dao");
        
        // Verify clean code practices: SQL as private constant and single public method
        assertThat(generatedCode).contains("private static final String SQL");
        assertThat(generatedCode).contains("UPDATE customer SET first_name = :firstName, email = :email WHERE id = :id");
        assertThat(generatedCode).contains("namedParameterJdbcTemplate.update(SQL, paramMap)");
        assertThat(generatedCode).contains("\"\"\""); // Check for text block usage
        
        // Count public methods to ensure single responsibility (only one public method)
        long publicMethodCount = generatedCode.lines()
            .filter(line -> line.trim().startsWith("public") && line.contains("("))
            .count();
        assertThat(publicMethodCount).isEqualTo(1);
    }

    @Test
    void shouldGenerateUpdateDAOWithMultipleWhereParameters() throws Exception {
        // Given
        List<ColumnMetadata> setColumns = List.of(
                new ColumnMetadata("active", "BIT", Types.BIT, false, false)
        );

        List<ColumnMetadata> whereColumns = List.of(
                new ColumnMetadata("customer_id", "INT", Types.INTEGER, false, true),
                new ColumnMetadata("store_id", "TINYINT", Types.TINYINT, false, false),
                new ColumnMetadata("last_update", "DATETIME", Types.TIMESTAMP, true, false)
        );

        UpdateMetadata updateMetadata = new UpdateMetadata(
                "customer", 
                setColumns, 
                whereColumns, 
                "UPDATE customer SET active = ? WHERE customer_id = ? AND store_id = ? AND last_update = ?"
        );

        // When
        JavaFile result = GenerateUpdateDAO.createUpdateDAO("Customer", updateMetadata);

        // Then
        String generatedCode = result.toString();
        
        // Check method signature with multiple WHERE parameters
        assertThat(generatedCode).contains("Integer id");
        assertThat(generatedCode).contains("Byte status");
        assertThat(generatedCode).contains("java.sql.Timestamp category");
        
        // Check parameter mapping for all WHERE parameters
        assertThat(generatedCode).contains("paramMap.put(\"id\", id)");
        assertThat(generatedCode).contains("paramMap.put(\"status\", status)");
        assertThat(generatedCode).contains("paramMap.put(\"category\", category)");
    }

    @Test
    void shouldGenerateUpdateDAOWithoutWhereClause() throws Exception {
        // Given
        List<ColumnMetadata> setColumns = List.of(
                new ColumnMetadata("active", "BIT", Types.BIT, false, false),
                new ColumnMetadata("last_update", "DATETIME", Types.TIMESTAMP, false, false)
        );

        UpdateMetadata updateMetadata = new UpdateMetadata(
                "customer", 
                setColumns, 
                List.of(), // No WHERE parameters
                "UPDATE customer SET active = ?, last_update = ?"
        );

        // When
        JavaFile result = GenerateUpdateDAO.createUpdateDAO("Customer", updateMetadata);

        // Then
        String generatedCode = result.toString();
        
        // Check method signature only has DTO parameter
        assertThat(generatedCode).contains("public int updateCustomer(CustomerUpdateDTO updateDto)");
        
        // Should not contain WHERE parameter handling
        assertThat(generatedCode).doesNotContain("Integer id");
        
        // Check parameter mapping only for SET columns
        assertThat(generatedCode).contains("paramMap.put(\"active\", updateDto.getActive())");
        assertThat(generatedCode).contains("paramMap.put(\"lastUpdate\", updateDto.getLastUpdate())");
        
        // Check SQL constant is used instead of inline SQL
        assertThat(generatedCode).contains("private static final String SQL");
        assertThat(generatedCode).contains("namedParameterJdbcTemplate.update(SQL, paramMap)");
    }

    @Test
    void shouldGenerateCorrectSQLWithNamedParameters() throws Exception {
        // Given
        List<ColumnMetadata> setColumns = List.of(
                new ColumnMetadata("first_name", "VARCHAR", Types.VARCHAR, true, false),
                new ColumnMetadata("last_name", "VARCHAR", Types.VARCHAR, true, false)
        );

        List<ColumnMetadata> whereColumns = List.of(
                new ColumnMetadata("customer_id", "INT", Types.INTEGER, false, true)
        );

        UpdateMetadata updateMetadata = new UpdateMetadata(
                "customer", 
                setColumns, 
                whereColumns, 
                "UPDATE customer SET first_name = ?, last_name = ? WHERE customer_id = ?"
        );

        // When
        JavaFile result = GenerateUpdateDAO.createUpdateDAO("Customer", updateMetadata);

        // Then
        String generatedCode = result.toString();
        
        // Check SQL format with named parameters
        // Note: The exact SQL format depends on implementation, but should contain named parameters
        assertThat(generatedCode).contains("UPDATE customer SET");
        assertThat(generatedCode).contains("first_name = :firstName");
        assertThat(generatedCode).contains("last_name = :lastName");
        assertThat(generatedCode).contains("WHERE");
        assertThat(generatedCode).contains("id = :id");
    }

    @Test
    void shouldIncludeValidationAnnotations() throws Exception {
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
        JavaFile result = GenerateUpdateDAO.createUpdateDAO("Customer", updateMetadata);

        // Then
        String generatedCode = result.toString();
        
        // Check @Valid annotation on DTO parameter
        assertThat(generatedCode).contains("@Valid");
        assertThat(generatedCode).contains("import jakarta.validation.Valid");
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
        JavaFile result = GenerateUpdateDAO.createUpdateDAO("Customer", updateMetadata);

        // Then
        String generatedCode = result.toString();
        
        // Check required imports
        assertThat(generatedCode).contains("import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate");
        assertThat(generatedCode).contains("import org.springframework.stereotype.Component");
        assertThat(generatedCode).contains("import lombok.extern.slf4j.Slf4j");
        assertThat(generatedCode).contains("import java.util.Map");
        assertThat(generatedCode).contains("import java.util.HashMap");
    }

    @Test
    void shouldIncludeJavaDocumentation() throws Exception {
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
        JavaFile result = GenerateUpdateDAO.createUpdateDAO("Customer", updateMetadata);

        // Then
        String generatedCode = result.toString();
        
        // Check JavaDoc is present
        assertThat(generatedCode).contains("/**");
        assertThat(generatedCode).contains("Updates customer record(s) in the database");
        assertThat(generatedCode).contains("@param updateDto");
        assertThat(generatedCode).contains("@return Number of rows updated");
    }
}