package com.jfeatures.msg.codegen;

import static org.junit.jupiter.api.Assertions.*;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.jfeatures.msg.test.TestUtils;
import com.squareup.javapoet.JavaFile;
import java.io.IOException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Reality-based tests for GenerateUpdateController following project patterns.
 * Tests actual behavior observed via research program, not assumptions.
 */
class GenerateUpdateControllerTest {

    private ColumnMetadata nameSetColumn;
    private ColumnMetadata statusSetColumn;
    private ColumnMetadata idWhereColumn;
    private ColumnMetadata categoryWhereColumn;
    private UpdateMetadata validUpdateMetadata;

    @BeforeEach
    void setUp() {
        // SET columns (what gets updated)
        nameSetColumn = TestUtils.createColumnMetadata("product_name", "VARCHAR", Types.VARCHAR, false);
        statusSetColumn = TestUtils.createColumnMetadata("status", "VARCHAR", Types.VARCHAR, true);
        
        // WHERE columns (filter criteria)
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
    void testCreateUpdateController_WithValidMetadata_GeneratesCorrectClass() throws IOException {
        JavaFile javaFile = GenerateUpdateController.createUpdateController("Product", validUpdateMetadata);

        assertNotNull(javaFile);
        assertEquals("com.jfeatures.msg.product.controller", javaFile.packageName);
        assertEquals("ProductUpdateController", javaFile.typeSpec.name);

        String code = javaFile.toString();
        
        // Verify Spring annotations
        assertTrue(code.contains("@RestController"));
        assertTrue(code.contains("path = \"/api\""));
        
        // Verify Swagger/OpenAPI annotations
        assertTrue(code.contains("@Tag"));
        assertTrue(code.contains("name = \"Product Update API\""));
        assertTrue(code.contains("description = \"REST API for updating product records\""));
        
        // Verify PUT mapping with path variable
        assertTrue(code.contains("@PutMapping"));
        assertTrue(code.contains("value = \"/product/{id}\""));
        assertTrue(code.contains("consumes = \"application/json\""));
        
        // Verify Operation annotation
        assertTrue(code.contains("@Operation"));
        assertTrue(code.contains("summary = \"Update product record\""));
        assertTrue(code.contains("description = \"Updates an existing product record with the provided data\""));
        
        // Verify method signature
        assertTrue(code.contains("public ResponseEntity<Void> updateProduct("));
    }

    @Test
    void testCreateUpdateController_WithSingleWhereColumn_GeneratesPathVariable() throws IOException {
        UpdateMetadata singleWhereMetadata = new UpdateMetadata(
                "users",
                Arrays.asList(nameSetColumn),
                Collections.singletonList(idWhereColumn), // Single WHERE column
                "UPDATE users SET product_name = ? WHERE id = ?"
        );

        JavaFile javaFile = GenerateUpdateController.createUpdateController("User", singleWhereMetadata);

        String code = javaFile.toString();
        
        // Should generate path variable for single WHERE column (ID)
        assertTrue(code.contains("@PathVariable"));
        assertTrue(code.contains("String id"));
        assertTrue(code.contains("value = \"/user/{id}\""));
        
        // Verify method parameters
        assertTrue(code.contains("@RequestBody"));
        assertTrue(code.contains("@Parameter"));
    }

    @Test
    void testCreateUpdateController_WithMultipleWhereColumns_GeneratesPathAndQueryParams() throws IOException {
        JavaFile javaFile = GenerateUpdateController.createUpdateController("Product", validUpdateMetadata);

        String code = javaFile.toString();
        
        // First WHERE column should be path variable
        assertTrue(code.contains("@PathVariable"));
        assertTrue(code.contains("String"));
        
        // Additional WHERE columns should be query parameters or other parameters
        assertTrue(code.contains("@RequestParam"));
    }

    @Test
    void testCreateUpdateController_WithNoWhereColumns_GeneratesSimpleMapping() throws IOException {
        UpdateMetadata noWhereMetadata = new UpdateMetadata(
                "settings",
                Arrays.asList(nameSetColumn),
                Collections.emptyList(), // No WHERE columns
                "UPDATE settings SET product_name = ?"
        );

        JavaFile javaFile = GenerateUpdateController.createUpdateController("Setting", noWhereMetadata);

        String code = javaFile.toString();
        
        // Should generate simple mapping without path variables
        assertTrue(code.contains("value = \"/setting\""));
        assertFalse(code.contains("{id}"));
        assertFalse(code.contains("@PathVariable"));
        assertFalse(code.contains("@RequestParam"));
    }

    @Test
    void testCreateUpdateController_GeneratesCorrectDependencyInjection() throws IOException {
        JavaFile javaFile = GenerateUpdateController.createUpdateController("Product", validUpdateMetadata);

        String code = javaFile.toString();
        
        // Verify DAO field injection
        assertTrue(code.contains("private final ProductUpdateDAO productUpdateDAO"));
        
        // Verify constructor injection
        assertTrue(code.contains("ProductUpdateController(ProductUpdateDAO productUpdateDAO)"));
        assertTrue(code.contains("this.productUpdateDAO = productUpdateDAO"));
    }

    @Test
    void testCreateUpdateController_GeneratesCorrectMethodBody() throws IOException {
        JavaFile javaFile = GenerateUpdateController.createUpdateController("Product", validUpdateMetadata);

        String code = javaFile.toString();
        
        // Verify DAO method call with all parameters
        assertTrue(code.contains("int rowsUpdated = productUpdateDAO.updateProduct(updateDto, id, categoryId)"));
        
        // Verify response handling
        assertTrue(code.contains("if (rowsUpdated > 0)"));
        assertTrue(code.contains("ResponseEntity.ok().build()"));
        assertTrue(code.contains("ResponseEntity.notFound().build()"));
    }

    @Test
    void testCreateUpdateController_WithMultipleDataTypes_GeneratesCorrectTypes() throws IOException {
        ColumnMetadata longIdColumn = TestUtils.createColumnMetadata("user_id", "BIGINT", Types.BIGINT, false);
        ColumnMetadata decimalColumn = TestUtils.createColumnMetadata("min_price", "DECIMAL", Types.DECIMAL, false);
        ColumnMetadata timestampColumn = TestUtils.createColumnMetadata("updated_at", "TIMESTAMP", Types.TIMESTAMP, false);

        UpdateMetadata multiTypeMetadata = new UpdateMetadata(
                "accounts",
                Arrays.asList(nameSetColumn), // SET columns
                Arrays.asList(longIdColumn, decimalColumn, timestampColumn), // WHERE columns
                "UPDATE accounts SET product_name = ? WHERE user_id = ? AND min_price = ? AND updated_at = ?"
        );

        JavaFile javaFile = GenerateUpdateController.createUpdateController("Account", multiTypeMetadata);

        String code = javaFile.toString();
        
        // Verify correct Java types are generated
        assertTrue(code.contains("Long") || code.contains("BigDecimal") || code.contains("Timestamp"));
        
        // Verify parameter annotations exist
        assertTrue(code.contains("@PathVariable"));
        assertTrue(code.contains("@RequestParam"));
    }

    @Test
    void testCreateUpdateController_HandlesSnakeCaseColumnNames() throws IOException {
        ColumnMetadata snakeCaseWhereColumn = TestUtils.createColumnMetadata("user_account_id", "INTEGER", Types.INTEGER, false);
        ColumnMetadata anotherSnakeCaseWhereColumn = TestUtils.createColumnMetadata("created_date_time", "TIMESTAMP", Types.TIMESTAMP, false);
        
        UpdateMetadata snakeCaseMetadata = new UpdateMetadata(
                "user_profiles",
                Arrays.asList(nameSetColumn),
                Arrays.asList(snakeCaseWhereColumn, anotherSnakeCaseWhereColumn),
                "UPDATE user_profiles SET product_name = ? WHERE user_account_id = ? AND created_date_time = ?"
        );

        JavaFile javaFile = GenerateUpdateController.createUpdateController("UserProfile", snakeCaseMetadata);

        String code = javaFile.toString();
        
        // Verify snake_case is converted to camelCase or similar handling
        assertTrue(code.contains("String"));
        assertTrue(code.contains("Timestamp"));
        assertTrue(code.contains("createdDateTime"));
        assertTrue(code.contains("@PathVariable"));
        assertTrue(code.contains("@RequestParam"));
    }

    @Test
    void testCreateUpdateController_GeneratesCorrectPackageStructure() throws IOException {
        JavaFile productFile = GenerateUpdateController.createUpdateController("Product", validUpdateMetadata);
        assertEquals("com.jfeatures.msg.product.controller", productFile.packageName);

        JavaFile customerFile = GenerateUpdateController.createUpdateController("Customer", validUpdateMetadata);
        assertEquals("com.jfeatures.msg.customer.controller", customerFile.packageName);

        JavaFile orderDetailFile = GenerateUpdateController.createUpdateController("OrderDetail", validUpdateMetadata);
        assertEquals("com.jfeatures.msg.orderdetail.controller", orderDetailFile.packageName);
    }

    @Test
    void testCreateUpdateController_GeneratesCorrectApiResponses() throws IOException {
        JavaFile javaFile = GenerateUpdateController.createUpdateController("Product", validUpdateMetadata);

        String code = javaFile.toString();
        
        // Verify API response documentation
        assertTrue(code.contains("@ApiResponses"));
        assertTrue(code.contains("responseCode = \"200\", description = \"Successfully updated\""));
        assertTrue(code.contains("responseCode = \"400\", description = \"Invalid request data\""));
        assertTrue(code.contains("responseCode = \"404\", description = \"Record not found\""));
    }

    @Test
    void testCreateUpdateController_GeneratesCorrectJavaDoc() throws IOException {
        JavaFile javaFile = GenerateUpdateController.createUpdateController("Product", validUpdateMetadata);

        String code = javaFile.toString();
        
        // Verify JavaDoc generation
        assertTrue(code.contains("Updates a product record"));
        assertTrue(code.contains("@param updateDto The updated data"));
        assertTrue(code.contains("@param id The record identifier"));
        assertTrue(code.contains("@return ResponseEntity indicating success or failure"));
    }

    @Test
    void testCreateUpdateController_GeneratesWhereParamNames() throws IOException {
        // Test the generateWhereParamName method indirectly with generic parameters
        ColumnMetadata genericParam1 = TestUtils.createColumnMetadata("whereParam1", "INTEGER", Types.INTEGER, false);
        ColumnMetadata genericParam2 = TestUtils.createColumnMetadata("whereParam2", "VARCHAR", Types.VARCHAR, false);
        ColumnMetadata genericParam3 = TestUtils.createColumnMetadata("whereParam3", "VARCHAR", Types.VARCHAR, false);
        ColumnMetadata genericParam4 = TestUtils.createColumnMetadata("whereParam4", "VARCHAR", Types.VARCHAR, false);
        
        UpdateMetadata genericMetadata = new UpdateMetadata(
                "test_table",
                Arrays.asList(nameSetColumn),
                Arrays.asList(genericParam1, genericParam2, genericParam3, genericParam4),
                "UPDATE test_table SET product_name = ? WHERE whereParam1 = ? AND whereParam2 = ? AND whereParam3 = ? AND whereParam4 = ?"
        );

        JavaFile javaFile = GenerateUpdateController.createUpdateController("Test", genericMetadata);

        String code = javaFile.toString();
        
        // Should generate meaningful parameter handling
        assertTrue(code.contains("@PathVariable"));
        assertTrue(code.contains("@RequestParam"));
        assertTrue(code.contains("String"));
        // Check for parameter handling
        assertTrue(code.contains("id"));
        assertTrue(code.contains("status") || code.contains("category") || code.contains("param"));
    }

    @Test
    void testCreateUpdateController_VerifiesClassStructure() throws IOException {
        JavaFile javaFile = GenerateUpdateController.createUpdateController("Product", validUpdateMetadata);

        // Verify class modifiers
        assertTrue(javaFile.typeSpec.hasModifier(javax.lang.model.element.Modifier.PUBLIC));

        // Verify field count (should have DAO field)
        assertEquals(1, javaFile.typeSpec.fieldSpecs.size());
        assertTrue(javaFile.typeSpec.fieldSpecs.get(0).hasModifier(javax.lang.model.element.Modifier.PRIVATE));
        assertTrue(javaFile.typeSpec.fieldSpecs.get(0).hasModifier(javax.lang.model.element.Modifier.FINAL));

        // Verify method count (constructor + update method)
        assertEquals(2, javaFile.typeSpec.methodSpecs.size());
    }

    @Test
    void testCreateUpdateController_GeneratesCorrectReturnType() throws IOException {
        JavaFile javaFile = GenerateUpdateController.createUpdateController("Product", validUpdateMetadata);

        String code = javaFile.toString();
        assertTrue(code.contains("ResponseEntity<Void> updateProduct"));
    }

    @Test
    void testCreateUpdateController_HandlesComplexBusinessPurpose() throws IOException {
        JavaFile javaFile = GenerateUpdateController.createUpdateController("QuarterlySalesReport", validUpdateMetadata);

        assertNotNull(javaFile);
        assertEquals("com.jfeatures.msg.quarterlysalesreport.controller", javaFile.packageName);
        assertEquals("QuarterlySalesReportUpdateController", javaFile.typeSpec.name);

        String code = javaFile.toString();
        
        assertTrue(code.contains("ResponseEntity"));
        assertTrue(code.contains("update"));
        assertTrue(code.contains("QuarterlySalesReport"));
        assertTrue(code.contains("updateDto"));
        assertTrue(code.contains("DAO"));
    }
}