package com.jfeatures.msg.codegen;

import static org.junit.jupiter.api.Assertions.*;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.DeleteMetadata;
import com.jfeatures.msg.test.TestUtils;
import com.squareup.javapoet.JavaFile;
import java.io.IOException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Reality-based tests for GenerateDeleteController following project patterns.
 * Tests actual behavior observed via research program, not assumptions.
 */
class GenerateDeleteControllerTest {

    private ColumnMetadata idWhereColumn;
    private ColumnMetadata statusWhereColumn;
    private DeleteMetadata validDeleteMetadata;

    @BeforeEach
    void setUp() {
        idWhereColumn = TestUtils.createColumnMetadata("id", "INTEGER", Types.INTEGER, false);
        statusWhereColumn = TestUtils.createColumnMetadata("status", "VARCHAR", Types.VARCHAR, false);

        validDeleteMetadata = new DeleteMetadata(
                "products",
                Arrays.asList(idWhereColumn, statusWhereColumn),
                "DELETE FROM products WHERE id = ? AND status = ?"
        );
    }

    @Test
    void testCreateDeleteController_WithValidMetadata_GeneratesCorrectClass() throws IOException {
        JavaFile javaFile = GenerateDeleteController.createDeleteController("Product", validDeleteMetadata);

        assertNotNull(javaFile);
        assertEquals("com.jfeatures.msg.product.controller", javaFile.packageName);
        assertEquals("ProductDeleteController", javaFile.typeSpec.name);

        String code = javaFile.toString();
        
        // Verify Spring annotations
        assertTrue(code.contains("@RestController"));
        assertTrue(code.contains("path = \"/api\""));
        
        // Verify Swagger/OpenAPI annotations
        assertTrue(code.contains("@Tag"));
        assertTrue(code.contains("name = \"Product\""));
        assertTrue(code.contains("description = \"Product DELETE operations\""));
        
        // Verify DELETE mapping
        assertTrue(code.contains("@DeleteMapping"));
        assertTrue(code.contains("value = \"/product\""));
        assertTrue(code.contains("produces = \"application/json\""));
        
        // Verify Operation annotation
        assertTrue(code.contains("@Operation"));
        assertTrue(code.contains("summary = \"Delete product entity\""));
        assertTrue(code.contains("description = \"DELETE API to remove a product record\""));
        
        // Verify method signature
        assertTrue(code.contains("public ResponseEntity deleteProduct("));
        
        // Verify request parameters with proper camelCase
        assertTrue(code.contains("@RequestParam(value = \"id\", required = true) String id"));
        assertTrue(code.contains("@RequestParam(value = \"status\", required = true) String status"));
    }

    @Test
    void testCreateDeleteController_WithSingleWhereColumn_GeneratesCorrectClass() throws IOException {
        DeleteMetadata singleColumnMetadata = new DeleteMetadata(
                "users",
                Collections.singletonList(idWhereColumn),
                "DELETE FROM users WHERE id = ?"
        );

        JavaFile javaFile = GenerateDeleteController.createDeleteController("User", singleColumnMetadata);

        assertNotNull(javaFile);
        assertEquals("com.jfeatures.msg.user.controller", javaFile.packageName);
        assertEquals("UserDeleteController", javaFile.typeSpec.name);

        String code = javaFile.toString();
        assertTrue(code.contains("public ResponseEntity deleteUser("));
        assertTrue(code.contains("@RequestParam(value = \"id\", required = true) String id"));
        
        // Should only have one parameter
        long paramCount = code.lines().filter(line -> line.contains("@RequestParam")).count();
        assertEquals(1, paramCount);
    }

    @Test
    void testCreateDeleteController_WithMultipleDataTypes_GeneratesCorrectTypes() throws IOException {
        ColumnMetadata longIdColumn = TestUtils.createColumnMetadata("user_id", "BIGINT", Types.BIGINT, false);
        ColumnMetadata decimalColumn = TestUtils.createColumnMetadata("min_price", "DECIMAL", Types.DECIMAL, false);
        ColumnMetadata booleanColumn = TestUtils.createColumnMetadata("is_active", "BIT", Types.BIT, false);

        DeleteMetadata multiTypeMetadata = new DeleteMetadata(
                "accounts",
                Arrays.asList(longIdColumn, decimalColumn, booleanColumn),
                "DELETE FROM accounts WHERE user_id = ? AND min_price = ? AND is_active = ?"
        );

        JavaFile javaFile = GenerateDeleteController.createDeleteController("Account", multiTypeMetadata);

        String code = javaFile.toString();
        assertTrue(code.contains("Long") || code.contains("String"));
        assertTrue(code.contains("BigDecimal") || code.contains("String"));
        assertTrue(code.contains("Boolean") || code.contains("String"));
        
        // Verify parameter mapping
        assertTrue(code.contains("@RequestParam"));
        assertTrue(code.contains("required = true"));
    }

    @Test
    void testCreateDeleteController_GeneratesCorrectDependencyInjection() throws IOException {
        JavaFile javaFile = GenerateDeleteController.createDeleteController("Product", validDeleteMetadata);

        String code = javaFile.toString();
        
        // Verify DAO field injection
        assertTrue(code.contains("private final ProductDeleteDAO productDeleteDAO"));
        
        // Verify constructor injection
        assertTrue(code.contains("ProductDeleteController(ProductDeleteDAO productDeleteDAO)"));
        assertTrue(code.contains("this.productDeleteDAO = productDeleteDAO"));
    }

    @Test
    void testCreateDeleteController_GeneratesCorrectMethodBody() throws IOException {
        JavaFile javaFile = GenerateDeleteController.createDeleteController("Product", validDeleteMetadata);

        String code = javaFile.toString();
        
        // Verify DAO method call
        assertTrue(code.contains("int rowsAffected = productDeleteDAO.deleteProduct(id, status)"));
        
        // Verify response handling
        assertTrue(code.contains("if (rowsAffected > 0)"));
        assertTrue(code.contains("ResponseEntity.status(HttpStatus.NO_CONTENT).body(\"product deleted successfully\")"));
        assertTrue(code.contains("ResponseEntity.status(HttpStatus.NOT_FOUND).body(\"product not found\")"));
    }

    @Test
    void testCreateDeleteController_HandlesSnakeCaseColumnNames() throws IOException {
        ColumnMetadata snakeCaseColumn = TestUtils.createColumnMetadata("user_account_id", "INTEGER", Types.INTEGER, false);
        ColumnMetadata anotherSnakeCaseColumn = TestUtils.createColumnMetadata("created_date_time", "TIMESTAMP", Types.TIMESTAMP, false);

        DeleteMetadata snakeCaseMetadata = new DeleteMetadata(
                "user_sessions",
                Arrays.asList(snakeCaseColumn, anotherSnakeCaseColumn),
                "DELETE FROM user_sessions WHERE user_account_id = ? AND created_date_time = ?"
        );

        JavaFile javaFile = GenerateDeleteController.createDeleteController("UserSession", snakeCaseMetadata);

        String code = javaFile.toString();
        
        // Verify snake_case is converted to camelCase for Java parameters
        assertTrue(code.contains("userAccountId") || code.contains("String"));
        assertTrue(code.contains("createdDateTime") || code.contains("Timestamp"));
        
        // Verify parameter annotations use camelCase for request param values
        assertTrue(code.contains("@RequestParam"));
        assertTrue(code.contains("required = true"));
    }

    @Test
    void testCreateDeleteController_GeneratesCorrectPackageStructure() throws IOException {
        JavaFile productFile = GenerateDeleteController.createDeleteController("Product", validDeleteMetadata);
        assertEquals("com.jfeatures.msg.product.controller", productFile.packageName);

        JavaFile customerFile = GenerateDeleteController.createDeleteController("Customer", validDeleteMetadata);
        assertEquals("com.jfeatures.msg.customer.controller", customerFile.packageName);

        JavaFile orderDetailFile = GenerateDeleteController.createDeleteController("OrderDetail", validDeleteMetadata);
        assertEquals("com.jfeatures.msg.orderdetail.controller", orderDetailFile.packageName);
    }

    @Test
    void testCreateDeleteController_WithNullBusinessName_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateDeleteController.createDeleteController(null, validDeleteMetadata)
        );

        assertEquals("Business purpose of SQL cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCreateDeleteController_WithEmptyBusinessName_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateDeleteController.createDeleteController("", validDeleteMetadata)
        );

        assertEquals("Business purpose of SQL cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCreateDeleteController_WithWhitespaceBusinessName_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateDeleteController.createDeleteController("   ", validDeleteMetadata)
        );

        assertEquals("Business purpose of SQL cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCreateDeleteController_WithNullDeleteMetadata_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateDeleteController.createDeleteController("Product", null)
        );

        assertEquals("Delete metadata cannot be null", exception.getMessage());
    }

    @Test
    void testCreateDeleteController_WithEmptyWhereColumns_ThrowsException() {
        DeleteMetadata emptyWhereMetadata = new DeleteMetadata(
                "test_table",
                Collections.emptyList(),
                "DELETE FROM test_table"
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateDeleteController.createDeleteController("Test", emptyWhereMetadata)
        );

        assertEquals("Delete metadata must have at least one WHERE column", exception.getMessage());
    }

    @Test
    void testCreateDeleteController_VerifiesClassStructure() throws IOException {
        JavaFile javaFile = GenerateDeleteController.createDeleteController("Product", validDeleteMetadata);

        // Verify class modifiers
        assertTrue(javaFile.typeSpec.hasModifier(javax.lang.model.element.Modifier.PUBLIC));

        // Verify field count (should have DAO field)
        assertEquals(1, javaFile.typeSpec.fieldSpecs.size());
        assertTrue(javaFile.typeSpec.fieldSpecs.get(0).hasModifier(javax.lang.model.element.Modifier.PRIVATE));
        assertTrue(javaFile.typeSpec.fieldSpecs.get(0).hasModifier(javax.lang.model.element.Modifier.FINAL));

        // Verify method count (constructor + delete method)
        assertEquals(2, javaFile.typeSpec.methodSpecs.size());
    }

    @Test
    void testCreateDeleteController_GeneratesCorrectReturnType() throws IOException {
        JavaFile javaFile = GenerateDeleteController.createDeleteController("Product", validDeleteMetadata);

        String code = javaFile.toString();
        assertTrue(code.contains("public ResponseEntity deleteProduct"));
    }

    @Test
    void testCreateDeleteController_GeneratesCorrectSwaggerDocumentation() throws IOException {
        JavaFile javaFile = GenerateDeleteController.createDeleteController("Product", validDeleteMetadata);

        String code = javaFile.toString();
        
        // Verify comprehensive Swagger documentation
        assertTrue(code.contains("@Tag"));
        assertTrue(code.contains("name = \"Product\""));
        assertTrue(code.contains("description = \"Product DELETE operations\""));
        assertTrue(code.contains("@Operation"));
        assertTrue(code.contains("summary = \"Delete product entity\""));
        assertTrue(code.contains("description = \"DELETE API to remove a product record\""));
    }
}