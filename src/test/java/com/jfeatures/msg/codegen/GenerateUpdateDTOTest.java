package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.jfeatures.msg.test.TestUtils;
import com.squareup.javapoet.JavaFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for GenerateUpdateDTO to improve coverage from 45% to 90%+.
 * Tests both SET clause DTOs and WHERE clause DTOs.
 */
class GenerateUpdateDTOTest {

    private ColumnMetadata nameSetColumn;
    private ColumnMetadata statusSetColumn;
    private ColumnMetadata idWhereColumn;
    private ColumnMetadata categoryWhereColumn;
    private UpdateMetadata validUpdateMetadata;

    @BeforeEach
    void setUp() {
        // Set up test data for SET columns (what gets updated)
        nameSetColumn = TestUtils.createColumnMetadata("product_name", "VARCHAR", Types.VARCHAR, false);
        statusSetColumn = TestUtils.createColumnMetadata("status", "VARCHAR", Types.VARCHAR, true);
        
        // Set up test data for WHERE columns (filter criteria)
        idWhereColumn = TestUtils.createColumnMetadata("id", "INTEGER", Types.INTEGER, false);
        categoryWhereColumn = TestUtils.createColumnMetadata("category_id", "INTEGER", Types.INTEGER, false);

        validUpdateMetadata = new UpdateMetadata(
                "products",
                Arrays.asList(nameSetColumn, statusSetColumn), // SET columns
                Arrays.asList(idWhereColumn, categoryWhereColumn), // WHERE columns
                "UPDATE products SET product_name = ?, status = ? WHERE id = ? AND category_id = ?"
        );
    }

    @Test
    void testCreateUpdateDTO_WithValidMetadata_GeneratesCorrectClass() throws IOException {
        // Test main UPDATE DTO generation
        JavaFile javaFile = GenerateUpdateDTO.createUpdateDTO("Product", validUpdateMetadata);

        assertNotNull(javaFile);
        assertEquals("com.jfeatures.msg.product.dto", javaFile.packageName);
        assertEquals("ProductUpdateDTO", javaFile.typeSpec.name);

        String code = javaFile.toString();
        // Verify Lombok @Data annotation is present
        assertTrue(code.contains("@Data"));
        
        // Verify SET columns are included as fields
        assertTrue(code.contains("private String productName"));
        assertTrue(code.contains("private String status"));
        
        // Verify validation annotations for non-nullable SET columns
        assertTrue(code.contains("@NotNull"));
        assertTrue(code.contains("productName cannot be null"));
        
        // Status is nullable, so no @NotNull annotation
        assertFalse(code.contains("status cannot be null"));
        
        // Verify JavaDoc is present
        assertTrue(code.contains("DTO for updating product entity"));
        assertTrue(code.contains("Contains fields that can be updated via PUT API"));
        assertTrue(code.contains("The product name to update"));
        assertTrue(code.contains("The status to update"));
    }

    @Test
    void testCreateUpdateWhereDTO_WithValidMetadata_GeneratesCorrectClass() throws IOException {
        // Test WHERE clause DTO generation (previously 0% coverage)
        JavaFile javaFile = GenerateUpdateDTO.createUpdateWhereDTO("Product", validUpdateMetadata);

        assertNotNull(javaFile);
        assertEquals("com.jfeatures.msg.product.dto", javaFile.packageName);
        assertEquals("ProductUpdateWhereDTO", javaFile.typeSpec.name);

        String code = javaFile.toString();
        // Verify Lombok @Data annotation is present
        assertTrue(code.contains("@Data"));
        
        // Verify WHERE columns are included as fields
        assertTrue(code.contains("private String id"));
        assertTrue(code.contains("private String categoryId"));
        
        // Verify all WHERE parameters have @NotNull (they're required for updates)
        assertTrue(code.contains("id cannot be null"));
        assertTrue(code.contains("categoryId cannot be null"));
        
        // Verify JavaDoc is present
        assertTrue(code.contains("DTO for WHERE clause parameters in product update operations"));
        assertTrue(code.contains("WHERE clause parameter: id"));
        assertTrue(code.contains("WHERE clause parameter: categoryId"));
    }

    @Test
    void testCreateUpdateWhereDTO_WithEmptyWhereColumns_ReturnsNull() throws IOException {
        // Test edge case with no WHERE columns (previously 0% coverage)
        UpdateMetadata noWhereMetadata = new UpdateMetadata(
                "simple_table",
                Arrays.asList(nameSetColumn),
                Collections.emptyList(), // No WHERE columns
                "UPDATE simple_table SET product_name = ?"
        );

        JavaFile javaFile = GenerateUpdateDTO.createUpdateWhereDTO("Simple", noWhereMetadata);

        // Should return null when there are no WHERE columns
        assertNull(javaFile);
    }

    @Test
    void testGenerateWhereFieldName_WithMeaningfulColumnNames_UsesCamelCase() throws IOException {
        // Test the private generateWhereFieldName method indirectly (previously 0% coverage)
        ColumnMetadata meaningfulColumn = TestUtils.createColumnMetadata("user_account_id", "INTEGER", Types.INTEGER, false);
        UpdateMetadata meaningfulWhereMetadata = new UpdateMetadata(
                "accounts",
                Arrays.asList(nameSetColumn),
                Arrays.asList(meaningfulColumn),
                "UPDATE accounts SET product_name = ? WHERE user_account_id = ?"
        );

        JavaFile javaFile = GenerateUpdateDTO.createUpdateWhereDTO("Account", meaningfulWhereMetadata);

        String code = javaFile.toString();
        // Should convert snake_case to camelCase
        assertTrue(code.contains("private String userAccountId"));
    }

    @Test
    void testGenerateWhereFieldName_WithGenericColumnNames_GeneratesDefaultNames() throws IOException {
        // Test the generateWhereFieldName method with generic parameter names (previously 0% coverage)
        ColumnMetadata genericColumn1 = TestUtils.createColumnMetadata("whereParam1", "INTEGER", Types.INTEGER, false);
        ColumnMetadata genericColumn2 = TestUtils.createColumnMetadata("whereParam2", "VARCHAR", Types.VARCHAR, false);
        ColumnMetadata genericColumn3 = TestUtils.createColumnMetadata("whereParam3", "VARCHAR", Types.VARCHAR, false);
        ColumnMetadata genericColumn4 = TestUtils.createColumnMetadata("whereParam4", "VARCHAR", Types.VARCHAR, false);
        
        UpdateMetadata genericWhereMetadata = new UpdateMetadata(
                "test_table",
                Arrays.asList(nameSetColumn),
                Arrays.asList(genericColumn1, genericColumn2, genericColumn3, genericColumn4),
                "UPDATE test_table SET product_name = ? WHERE whereParam1 = ? AND whereParam2 = ? AND whereParam3 = ? AND whereParam4 = ?"
        );

        JavaFile javaFile = GenerateUpdateDTO.createUpdateWhereDTO("Test", genericWhereMetadata);

        String code = javaFile.toString();
        // Should generate default meaningful names based on index
        assertTrue(code.contains("private String id")); // index 0
        assertTrue(code.contains("private String status")); // index 1
        assertTrue(code.contains("private String category")); // index 2
        assertTrue(code.contains("private String param4")); // index 3
    }

    @Test
    void testCreateUpdateDTO_WithMultipleDataTypes_GeneratesCorrectTypes() throws IOException {
        ColumnMetadata bigintColumn = TestUtils.createColumnMetadata("big_id", "BIGINT", Types.BIGINT, false);
        ColumnMetadata decimalColumn = TestUtils.createColumnMetadata("price", "DECIMAL", Types.DECIMAL, true);
        ColumnMetadata booleanColumn = TestUtils.createColumnMetadata("is_active", "BIT", Types.BIT, false);
        ColumnMetadata timestampColumn = TestUtils.createColumnMetadata("updated_at", "TIMESTAMP", Types.TIMESTAMP, true);

        UpdateMetadata multiTypeMetadata = new UpdateMetadata(
                "complex_table",
                Arrays.asList(bigintColumn, decimalColumn, booleanColumn, timestampColumn),
                Arrays.asList(idWhereColumn),
                "UPDATE complex_table SET big_id = ?, price = ?, is_active = ?, updated_at = ? WHERE id = ?"
        );

        JavaFile javaFile = GenerateUpdateDTO.createUpdateDTO("Complex", multiTypeMetadata);

        String code = javaFile.toString();
        assertTrue(code.contains("private Long bigId"));
        assertTrue(code.contains("private BigDecimal price"));
        assertTrue(code.contains("private Boolean isActive"));
        assertTrue(code.contains("private Timestamp updatedAt"));
        
        // Verify validation annotations for non-nullable fields
        assertTrue(code.contains("bigId cannot be null"));
        assertTrue(code.contains("isActive cannot be null"));
        
        // Nullable fields should not have @NotNull
        assertFalse(code.contains("price cannot be null"));
        assertFalse(code.contains("updatedAt cannot be null"));
    }

    @Test
    void testCreateUpdateDTO_WithSingleSetColumn_GeneratesCorrectClass() throws IOException {
        UpdateMetadata singleColumnMetadata = new UpdateMetadata(
                "simple_table",
                Collections.singletonList(statusSetColumn),
                Arrays.asList(idWhereColumn),
                "UPDATE simple_table SET status = ? WHERE id = ?"
        );

        JavaFile javaFile = GenerateUpdateDTO.createUpdateDTO("Simple", singleColumnMetadata);

        assertNotNull(javaFile);
        assertEquals("com.jfeatures.msg.simple.dto", javaFile.packageName);
        assertEquals("SimpleUpdateDTO", javaFile.typeSpec.name);

        String code = javaFile.toString();
        assertTrue(code.contains("private String status"));
        assertEquals(1, javaFile.typeSpec.fieldSpecs.size());
    }

    @Test
    void testCreateUpdateDTO_GeneratesCorrectPackageStructure() throws IOException {
        // Test various business names to verify package naming convention
        JavaFile productFile = GenerateUpdateDTO.createUpdateDTO("Product", validUpdateMetadata);
        assertEquals("com.jfeatures.msg.product.dto", productFile.packageName);
        
        JavaFile customerFile = GenerateUpdateDTO.createUpdateDTO("Customer", validUpdateMetadata);
        assertEquals("com.jfeatures.msg.customer.dto", customerFile.packageName);
        
        JavaFile orderDetailFile = GenerateUpdateDTO.createUpdateDTO("OrderDetail", validUpdateMetadata);
        assertEquals("com.jfeatures.msg.orderdetail.dto", orderDetailFile.packageName);
    }

    @Test
    void testCreateUpdateDTO_HandlesSnakeCaseFieldNames() throws IOException {
        ColumnMetadata snakeCaseSetColumn = TestUtils.createColumnMetadata("user_full_name", "VARCHAR", Types.VARCHAR, false);
        ColumnMetadata snakeCaseWhereColumn = TestUtils.createColumnMetadata("account_created_date", "TIMESTAMP", Types.TIMESTAMP, false);
        
        UpdateMetadata snakeCaseMetadata = new UpdateMetadata(
                "user_profiles",
                Arrays.asList(snakeCaseSetColumn),
                Arrays.asList(snakeCaseWhereColumn),
                "UPDATE user_profiles SET user_full_name = ? WHERE account_created_date = ?"
        );

        JavaFile setDto = GenerateUpdateDTO.createUpdateDTO("UserProfile", snakeCaseMetadata);
        JavaFile whereDto = GenerateUpdateDTO.createUpdateWhereDTO("UserProfile", snakeCaseMetadata);

        String setCode = setDto.toString();
        String whereCode = whereDto.toString();
        
        // Verify snake_case is converted to camelCase in both DTOs
        assertTrue(setCode.contains("private String userFullName"));
        assertTrue(whereCode.contains("private Timestamp accountCreatedDate"));
    }

    @Test
    void testCreateUpdateDTO_VerifiesClassStructure() throws IOException {
        JavaFile javaFile = GenerateUpdateDTO.createUpdateDTO("Product", validUpdateMetadata);
        
        // Verify class modifiers
        assertTrue(javaFile.typeSpec.hasModifier(javax.lang.model.element.Modifier.PUBLIC));
        
        // Verify field count matches SET columns
        assertEquals(2, javaFile.typeSpec.fieldSpecs.size());
        
        // Verify all fields are private
        javaFile.typeSpec.fieldSpecs.forEach(fieldSpec -> {
            assertTrue(fieldSpec.hasModifier(javax.lang.model.element.Modifier.PRIVATE));
        });
    }

    @Test
    void testCreateUpdateWhereDTO_VerifiesClassStructure() throws IOException {
        JavaFile javaFile = GenerateUpdateDTO.createUpdateWhereDTO("Product", validUpdateMetadata);
        
        // Verify class modifiers
        assertTrue(javaFile.typeSpec.hasModifier(javax.lang.model.element.Modifier.PUBLIC));
        
        // Verify field count matches WHERE columns
        assertEquals(2, javaFile.typeSpec.fieldSpecs.size());
        
        // Verify all fields are private
        javaFile.typeSpec.fieldSpecs.forEach(fieldSpec -> {
            assertTrue(fieldSpec.hasModifier(javax.lang.model.element.Modifier.PRIVATE));
        });
    }

    @Test
    void testGenerateFieldSpecsForSetColumns_GeneratesCorrectJavaDoc() throws IOException {
        JavaFile javaFile = GenerateUpdateDTO.createUpdateDTO("Product", validUpdateMetadata);

        String code = javaFile.toString();
        // Verify JavaDoc formatting for SET columns
        assertTrue(code.contains("The product name to update"));
        assertTrue(code.contains("The status to update"));
    }

    @Test
    void testGenerateFieldSpecsForWhereColumns_GeneratesCorrectJavaDoc() throws IOException {
        JavaFile javaFile = GenerateUpdateDTO.createUpdateWhereDTO("Product", validUpdateMetadata);

        String code = javaFile.toString();
        // Verify JavaDoc formatting for WHERE columns
        assertTrue(code.contains("WHERE clause parameter: id"));
        assertTrue(code.contains("WHERE clause parameter: categoryId"));
    }
}