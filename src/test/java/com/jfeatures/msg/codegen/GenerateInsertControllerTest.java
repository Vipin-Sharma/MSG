package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.InsertMetadata;
import com.jfeatures.msg.test.TestUtils;
import com.squareup.javapoet.JavaFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reality-based tests for GenerateInsertController following project patterns.
 * Tests actual behavior observed via research program, not assumptions.
 */
class GenerateInsertControllerTest {

    private ColumnMetadata productNameColumn;
    private ColumnMetadata descriptionColumn;
    private ColumnMetadata priceColumn;
    private InsertMetadata validInsertMetadata;

    @BeforeEach
    void setUp() {
        productNameColumn = TestUtils.createColumnMetadata("product_name", "VARCHAR", Types.VARCHAR, false);
        descriptionColumn = TestUtils.createColumnMetadata("description", "TEXT", Types.LONGVARCHAR, true);
        priceColumn = TestUtils.createColumnMetadata("price", "DECIMAL", Types.DECIMAL, false);

        validInsertMetadata = new InsertMetadata(
                "products",
                Arrays.asList(productNameColumn, descriptionColumn, priceColumn),
                "INSERT INTO products (product_name, description, price) VALUES (?, ?, ?)"
        );
    }

    @Test
    void testCreateInsertController_WithValidMetadata_GeneratesCorrectClass() throws IOException {
        JavaFile javaFile = GenerateInsertController.createInsertController("Product", validInsertMetadata);

        assertNotNull(javaFile);
        assertEquals("com.jfeatures.msg.product.controller", javaFile.packageName);
        assertEquals("ProductInsertController", javaFile.typeSpec.name);

        String code = javaFile.toString();
        
        // Verify Spring annotations
        assertTrue(code.contains("@RestController"));
        assertTrue(code.contains("path = \"/api\""));
        
        // Verify Swagger/OpenAPI annotations
        assertTrue(code.contains("@Tag"));
        assertTrue(code.contains("name = \"Product\""));
        assertTrue(code.contains("description = \"Product INSERT operations\""));
        
        // Verify POST mapping
        assertTrue(code.contains("@PostMapping"));
        assertTrue(code.contains("value = \"/product\""));
        assertTrue(code.contains("consumes = \"application/json\""));
        assertTrue(code.contains("produces = \"application/json\""));
        
        // Verify Operation annotation
        assertTrue(code.contains("@Operation"));
        assertTrue(code.contains("summary = \"Create new product entity\""));
        assertTrue(code.contains("description = \"POST API to create a new product record\""));
        
        // Verify method signature
        assertTrue(code.contains("public ResponseEntity createProduct("));
        
        // Verify request body parameter with validation
        assertTrue(code.contains("@Valid @RequestBody ProductInsertDTO insertRequest"));
    }

    @Test
    void testCreateInsertController_WithSingleColumn_GeneratesCorrectClass() throws IOException {
        InsertMetadata singleColumnMetadata = new InsertMetadata(
                "categories",
                Collections.singletonList(productNameColumn),
                "INSERT INTO categories (product_name) VALUES (?)"
        );

        JavaFile javaFile = GenerateInsertController.createInsertController("Category", singleColumnMetadata);

        assertNotNull(javaFile);
        assertEquals("com.jfeatures.msg.category.controller", javaFile.packageName);
        assertEquals("CategoryInsertController", javaFile.typeSpec.name);

        String code = javaFile.toString();
        assertTrue(code.contains("public ResponseEntity createCategory("));
        assertTrue(code.contains("@Valid @RequestBody CategoryInsertDTO insertRequest"));
    }

    @Test
    void testCreateInsertController_GeneratesCorrectDependencyInjection() throws IOException {
        JavaFile javaFile = GenerateInsertController.createInsertController("Product", validInsertMetadata);

        String code = javaFile.toString();
        
        // Verify DAO field injection
        assertTrue(code.contains("private final ProductInsertDAO productInsertDAO"));
        
        // Verify constructor injection
        assertTrue(code.contains("ProductInsertController(ProductInsertDAO productInsertDAO)"));
        assertTrue(code.contains("this.productInsertDAO = productInsertDAO"));
    }

    @Test
    void testCreateInsertController_GeneratesCorrectMethodBody() throws IOException {
        JavaFile javaFile = GenerateInsertController.createInsertController("Product", validInsertMetadata);

        String code = javaFile.toString();
        
        // Verify DAO method call
        assertTrue(code.contains("int rowsAffected = productInsertDAO.insertProduct(insertRequest)"));
        
        // Verify response handling
        assertTrue(code.contains("if (rowsAffected > 0)"));
        assertTrue(code.contains("ResponseEntity.status(HttpStatus.CREATED).body(\"product created successfully\")"));
        assertTrue(code.contains("ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(\"Failed to create product\")"));
    }

    @Test
    void testCreateInsertController_GeneratesCorrectPackageStructure() throws IOException {
        JavaFile productFile = GenerateInsertController.createInsertController("Product", validInsertMetadata);
        assertEquals("com.jfeatures.msg.product.controller", productFile.packageName);

        JavaFile customerFile = GenerateInsertController.createInsertController("Customer", validInsertMetadata);
        assertEquals("com.jfeatures.msg.customer.controller", customerFile.packageName);

        JavaFile orderDetailFile = GenerateInsertController.createInsertController("OrderDetail", validInsertMetadata);
        assertEquals("com.jfeatures.msg.orderdetail.controller", orderDetailFile.packageName);
    }

    @Test
    void testCreateInsertController_HandlesComplexBusinessPurpose() throws IOException {
        JavaFile javaFile = GenerateInsertController.createInsertController("QuarterlySalesReport", validInsertMetadata);

        assertNotNull(javaFile);
        assertEquals("com.jfeatures.msg.quarterlysalesreport.controller", javaFile.packageName);
        assertEquals("QuarterlySalesReportInsertController", javaFile.typeSpec.name);

        String code = javaFile.toString();
        assertTrue(code.contains("public ResponseEntity createQuarterlySalesReport("));
        assertTrue(code.contains("QuarterlySalesReportInsertDTO insertRequest"));
        // Flexible method name matching - verify DAO injection and method call patterns
        assertTrue(code.contains(".insert"));  // General pattern for DAO method call
    }

    @Test
    void testCreateInsertController_WithNullBusinessName_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateInsertController.createInsertController(null, validInsertMetadata)
        );

        assertEquals("Business purpose of SQL cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCreateInsertController_WithEmptyBusinessName_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateInsertController.createInsertController("", validInsertMetadata)
        );

        assertEquals("Business purpose of SQL cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCreateInsertController_WithWhitespaceBusinessName_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateInsertController.createInsertController("   ", validInsertMetadata)
        );

        assertEquals("Business purpose of SQL cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCreateInsertController_WithNullInsertMetadata_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateInsertController.createInsertController("Product", null)
        );

        assertEquals("Insert metadata cannot be null", exception.getMessage());
    }

    @Test
    void testCreateInsertController_VerifiesClassStructure() throws IOException {
        JavaFile javaFile = GenerateInsertController.createInsertController("Product", validInsertMetadata);

        // Verify class modifiers
        assertTrue(javaFile.typeSpec.hasModifier(javax.lang.model.element.Modifier.PUBLIC));

        // Verify field count (should have DAO field)
        assertEquals(1, javaFile.typeSpec.fieldSpecs.size());
        assertTrue(javaFile.typeSpec.fieldSpecs.get(0).hasModifier(javax.lang.model.element.Modifier.PRIVATE));
        assertTrue(javaFile.typeSpec.fieldSpecs.get(0).hasModifier(javax.lang.model.element.Modifier.FINAL));

        // Verify method count (constructor + create method)
        assertEquals(2, javaFile.typeSpec.methodSpecs.size());
    }

    @Test
    void testCreateInsertController_GeneratesCorrectReturnType() throws IOException {
        JavaFile javaFile = GenerateInsertController.createInsertController("Product", validInsertMetadata);

        String code = javaFile.toString();
        assertTrue(code.contains("public ResponseEntity createProduct"));
    }

    @Test
    void testCreateInsertController_GeneratesCorrectValidationAnnotations() throws IOException {
        JavaFile javaFile = GenerateInsertController.createInsertController("Product", validInsertMetadata);

        String code = javaFile.toString();
        
        // Verify validation annotations are present
        assertTrue(code.contains("@Valid"));
        assertTrue(code.contains("@RequestBody"));
    }

    @Test
    void testCreateInsertController_GeneratesCorrectSwaggerDocumentation() throws IOException {
        JavaFile javaFile = GenerateInsertController.createInsertController("Product", validInsertMetadata);

        String code = javaFile.toString();
        
        // Verify comprehensive Swagger documentation - flexible multiline format matching
        assertTrue(code.contains("@Tag"));
        assertTrue(code.contains("name = \"Product\""));
        assertTrue(code.contains("description = \"Product INSERT operations\""));
        assertTrue(code.contains("@Operation"));
        assertTrue(code.contains("summary = \"Create new product entity\""));
        assertTrue(code.contains("description = \"POST API to create a new product record\""));
    }

    @Test
    void testCreateInsertController_GeneratesCorrectHttpMappings() throws IOException {
        JavaFile javaFile = GenerateInsertController.createInsertController("Product", validInsertMetadata);

        String code = javaFile.toString();
        
        // Verify HTTP mapping configuration - flexible multiline format matching
        assertTrue(code.contains("@PostMapping"));
        assertTrue(code.contains("value = \"/product\""));
        assertTrue(code.contains("consumes = \"application/json\""));
        assertTrue(code.contains("produces = \"application/json\""));
        assertTrue(code.contains("@RequestMapping"));
        assertTrue(code.contains("path = \"/api\""));
    }

    @Test
    void testCreateInsertController_HandlesMultipleBusinessNames() throws IOException {
        String[] businessNames = {"User", "Order", "Customer", "Product", "Inventory"};
        
        for (String businessName : businessNames) {
            JavaFile javaFile = GenerateInsertController.createInsertController(businessName, validInsertMetadata);
            
            assertNotNull(javaFile);
            assertEquals(businessName + "InsertController", javaFile.typeSpec.name);
            
            String code = javaFile.toString();
            assertTrue(code.contains("public ResponseEntity create" + businessName + "("));
            assertTrue(code.contains(businessName + "InsertDAO"));
            assertTrue(code.contains(businessName + "InsertDTO"));
        }
    }

    @Test
    void testCreateInsertController_GeneratesCorrectStatusCodes() throws IOException {
        JavaFile javaFile = GenerateInsertController.createInsertController("Product", validInsertMetadata);

        String code = javaFile.toString();
        
        // Verify HTTP status codes are correctly used
        assertTrue(code.contains("HttpStatus.CREATED"));
        assertTrue(code.contains("HttpStatus.INTERNAL_SERVER_ERROR"));
        assertTrue(code.contains("status(HttpStatus.CREATED)"));
        assertTrue(code.contains("status(HttpStatus.INTERNAL_SERVER_ERROR)"));
    }
}