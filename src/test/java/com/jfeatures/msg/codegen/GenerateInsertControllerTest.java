package com.jfeatures.msg.codegen;

import static org.junit.jupiter.api.Assertions.*;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.InsertMetadata;
import com.jfeatures.msg.codegen.domain.AuditConfiguration;
import com.jfeatures.msg.test.TestUtils;
import com.squareup.javapoet.JavaFile;
import java.io.IOException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

    private static Stream<Arguments> invalidBusinessNameProvider() {
        return Stream.of(
            Arguments.of("null business name", null),
            Arguments.of("empty business name", ""),
            Arguments.of("whitespace business name", "   ")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidBusinessNameProvider")
    void testCreateInsertController_WithInvalidBusinessName_ThrowsException(String testName, String businessName) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateInsertController.createInsertController(businessName, validInsertMetadata)
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

    // ========== AUDIT CONFIGURATION TESTS ==========

    @Test
    void testCreateInsertController_WithAuditEnabled_AddsRequestHeaderParameter() throws IOException {
        AuditConfiguration auditConfig = new AuditConfiguration(
                true, "X-User-ID", Set.of("created_by", "updated_by"));

        JavaFile javaFile = GenerateInsertController.createInsertController("Product", validInsertMetadata, auditConfig);

        String code = javaFile.toString();

        // Verify @RequestHeader annotation is present
        assertTrue(code.contains("@RequestHeader"), "Should contain @RequestHeader annotation");
        assertTrue(code.contains("value = \"X-User-ID\""), "Should specify header name");
        assertTrue(code.contains("required = true"), "Header should be required");

        // Verify userId parameter is present
        assertTrue(code.contains("String userId"), "Should have userId parameter");

        // Verify @Parameter annotation for Swagger documentation
        assertTrue(code.contains("@Parameter"), "Should have Swagger @Parameter annotation");
        assertTrue(code.contains("description = \"User ID for audit purposes\""), "Should have description");

        // Verify DAO call includes userId
        assertTrue(code.contains("insertProduct(insertRequest, userId)"), "DAO call should include userId");
    }

    @Test
    void testCreateInsertController_WithAuditDisabled_NoRequestHeaderParameter() throws IOException {
        AuditConfiguration auditConfig = AuditConfiguration.disabled();

        JavaFile javaFile = GenerateInsertController.createInsertController("Product", validInsertMetadata, auditConfig);

        String code = javaFile.toString();

        // Verify @RequestHeader is NOT present
        assertFalse(code.contains("@RequestHeader"), "Should NOT contain @RequestHeader when audit disabled");
        assertFalse(code.contains("String userId"), "Should NOT have userId parameter when audit disabled");

        // Verify standard DAO call without userId
        assertTrue(code.contains("insertProduct(insertRequest)"), "DAO call should NOT include userId");
        assertFalse(code.contains("insertProduct(insertRequest, userId)"), "DAO call should NOT have userId");
    }

    @Test
    void testCreateInsertController_WithCustomHeaderName_UsesCorrectHeader() throws IOException {
        AuditConfiguration auditConfig = new AuditConfiguration(
                true, "Authorization", Set.of("created_by"));

        JavaFile javaFile = GenerateInsertController.createInsertController("Product", validInsertMetadata, auditConfig);

        String code = javaFile.toString();

        // Verify custom header name is used
        assertTrue(code.contains("value = \"Authorization\""), "Should use custom header name");
        assertFalse(code.contains("value = \"X-User-ID\""), "Should NOT use default header");
    }

    @Test
    void testCreateInsertController_WithNullAuditConfig_DefaultsToDisabled() throws IOException {
        JavaFile javaFile = GenerateInsertController.createInsertController("Product", validInsertMetadata, null);

        String code = javaFile.toString();

        // Should behave like audit disabled
        assertFalse(code.contains("@RequestHeader"), "Should NOT contain @RequestHeader when config is null");
        assertTrue(code.contains("insertProduct(insertRequest)"), "Should use standard DAO call");
    }

    @Test
    void testCreateInsertController_WithAuditEnabled_GeneratesCorrectImports() throws IOException {
        AuditConfiguration auditConfig = new AuditConfiguration(
                true, "X-User-ID", Set.of("created_by"));

        JavaFile javaFile = GenerateInsertController.createInsertController("Product", validInsertMetadata, auditConfig);

        String code = javaFile.toString();

        // Verify required imports are present
        assertTrue(code.contains("import org.springframework.web.bind.annotation.RequestHeader"),
                "Should import RequestHeader");
        assertTrue(code.contains("import io.swagger.v3.oas.annotations.Parameter"),
                "Should import Parameter annotation");
    }

    @Test
    void testCreateInsertController_BackwardCompatibility_NoAuditConfig() throws IOException {
        // Test that the original method without AuditConfiguration still works
        JavaFile javaFile = GenerateInsertController.createInsertController("Product", validInsertMetadata);

        String code = javaFile.toString();

        // Should behave like audit disabled
        assertFalse(code.contains("@RequestHeader"), "Default should NOT contain @RequestHeader");
        assertTrue(code.contains("insertProduct(insertRequest)"), "Default should use standard DAO call");
    }
}