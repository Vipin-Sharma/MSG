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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reality-based tests for GenerateInsertDTO following project patterns.
 * Tests actual behavior observed via research program, not assumptions.
 */
class GenerateInsertDTOTest {

    private ColumnMetadata productNameColumn;
    private ColumnMetadata descriptionColumn;
    private ColumnMetadata priceColumn;
    private InsertMetadata validInsertMetadata;

    @BeforeEach
    void setUp() {
        // Set up test data based on research
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
    void testCreateInsertDTO_WithValidMetadata_GeneratesCorrectClass() throws IOException {
        // Test with valid INSERT metadata
        JavaFile javaFile = GenerateInsertDTO.createInsertDTO("Product", validInsertMetadata);

        assertNotNull(javaFile);
        assertEquals("com.jfeatures.msg.product.dto", javaFile.packageName);
        assertEquals("ProductInsertDTO", javaFile.typeSpec.name);

        String code = javaFile.toString();
        // Verify Lombok annotations are present
        assertTrue(code.contains("@Builder"));
        assertTrue(code.contains("@Value"));
        assertTrue(code.contains("@Jacksonized"));
        
        // Verify fields are generated with proper camelCase naming
        assertTrue(code.contains("private String productName"));
        assertTrue(code.contains("private String description"));
        assertTrue(code.contains("private BigDecimal price"));
        
        // Verify validation annotations for non-nullable fields
        assertTrue(code.contains("@NotNull"));
        assertTrue(code.contains("productName is required for product creation"));
        assertTrue(code.contains("price is required for product creation"));
        
        // Description is nullable, so no @NotNull annotation
        assertFalse(code.contains("description is required for product creation"));
        
        // Verify JavaDoc is present
        assertTrue(code.contains("DTO for creating new product entities"));
        assertTrue(code.contains("productName field for product insertion"));
    }

    @Test
    void testCreateInsertDTO_WithSingleColumn_GeneratesCorrectClass() throws IOException {
        InsertMetadata singleColumnMetadata = new InsertMetadata(
                "categories",
                Collections.singletonList(productNameColumn),
                "INSERT INTO categories (product_name) VALUES (?)"
        );

        JavaFile javaFile = GenerateInsertDTO.createInsertDTO("Category", singleColumnMetadata);

        assertNotNull(javaFile);
        assertEquals("com.jfeatures.msg.category.dto", javaFile.packageName);
        assertEquals("CategoryInsertDTO", javaFile.typeSpec.name);

        String code = javaFile.toString();
        assertTrue(code.contains("private String productName"));
        assertTrue(code.contains("@NotNull"));
        assertTrue(code.contains("productName is required for category creation"));
    }

    @Test
    void testCreateInsertDTO_WithMultipleDataTypes_GeneratesCorrectTypes() throws IOException {
        ColumnMetadata idColumn = TestUtils.createColumnMetadata("id", "BIGINT", Types.BIGINT, false);
        ColumnMetadata activeFlagColumn = TestUtils.createColumnMetadata("is_active", "BIT", Types.BIT, false);
        ColumnMetadata createdDateColumn = TestUtils.createColumnMetadata("created_date", "TIMESTAMP", Types.TIMESTAMP, true);

        InsertMetadata multiTypeMetadata = new InsertMetadata(
                "users",
                Arrays.asList(idColumn, activeFlagColumn, createdDateColumn),
                "INSERT INTO users (id, is_active, created_date) VALUES (?, ?, ?)"
        );

        JavaFile javaFile = GenerateInsertDTO.createInsertDTO("User", multiTypeMetadata);

        String code = javaFile.toString();
        assertTrue(code.contains("private Long id"));
        assertTrue(code.contains("private Boolean isActive"));
        assertTrue(code.contains("private Timestamp createdDate"));
        
        // Verify validation annotations for non-nullable fields
        assertTrue(code.contains("id is required for user creation"));
        assertTrue(code.contains("isActive is required for user creation"));
        
        // createdDate is nullable, so no @NotNull annotation
        assertFalse(code.contains("createdDate is required for user creation"));
    }

    @Test
    void testCreateInsertDTO_WithNullBusinessName_ThrowsException() {
        // Test error conditions based on research observations
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateInsertDTO.createInsertDTO(null, validInsertMetadata)
        );
        
        assertEquals("Business purpose of SQL cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCreateInsertDTO_WithEmptyBusinessName_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateInsertDTO.createInsertDTO("", validInsertMetadata)
        );
        
        assertEquals("Business purpose of SQL cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCreateInsertDTO_WithWhitespaceBusinessName_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateInsertDTO.createInsertDTO("   ", validInsertMetadata)
        );
        
        assertEquals("Business purpose of SQL cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCreateInsertDTO_WithNullMetadata_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateInsertDTO.createInsertDTO("Product", null)
        );
        
        assertEquals("Insert metadata cannot be null", exception.getMessage());
    }

    @Test
    void testCreateInsertDTO_WithEmptyColumns_ThrowsException() {
        // Test edge cases based on research
        InsertMetadata emptyMetadata = new InsertMetadata(
                "test_table", 
                Collections.emptyList(), 
                "INSERT INTO test_table () VALUES ()"
        );
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateInsertDTO.createInsertDTO("Test", emptyMetadata)
        );
        
        assertEquals("Insert metadata must have at least one column", exception.getMessage());
    }

    @Test
    void testCreateInsertDTO_GeneratesCorrectPackageStructure() throws IOException {
        // Test various business names to verify package naming convention
        JavaFile productFile = GenerateInsertDTO.createInsertDTO("Product", validInsertMetadata);
        assertEquals("com.jfeatures.msg.product.dto", productFile.packageName);
        
        JavaFile customerFile = GenerateInsertDTO.createInsertDTO("Customer", validInsertMetadata);
        assertEquals("com.jfeatures.msg.customer.dto", customerFile.packageName);
        
        JavaFile orderDetailFile = GenerateInsertDTO.createInsertDTO("OrderDetail", validInsertMetadata);
        assertEquals("com.jfeatures.msg.orderdetail.dto", orderDetailFile.packageName);
    }

    @Test
    void testCreateInsertDTO_HandlesSnakeCaseColumnNames() throws IOException {
        ColumnMetadata snakeCaseColumn = TestUtils.createColumnMetadata("user_account_id", "INTEGER", Types.INTEGER, false);
        ColumnMetadata anotherSnakeCaseColumn = TestUtils.createColumnMetadata("first_login_date", "TIMESTAMP", Types.TIMESTAMP, true);

        InsertMetadata snakeCaseMetadata = new InsertMetadata(
                "user_sessions",
                Arrays.asList(snakeCaseColumn, anotherSnakeCaseColumn),
                "INSERT INTO user_sessions (user_account_id, first_login_date) VALUES (?, ?)"
        );

        JavaFile javaFile = GenerateInsertDTO.createInsertDTO("UserSession", snakeCaseMetadata);

        String code = javaFile.toString();
        // Verify snake_case is converted to camelCase - types may vary based on type mapping
        assertTrue(code.contains("private String userAccountId") || code.contains("private Integer userAccountId"));
        assertTrue(code.contains("private Timestamp firstLoginDate"));
        
        // Verify validation messages use camelCase field names
        assertTrue(code.contains("userAccountId is required for usersession creation"));
    }

    @Test
    void testCreateInsertDTO_VerifiesClassStructure() throws IOException {
        JavaFile javaFile = GenerateInsertDTO.createInsertDTO("Product", validInsertMetadata);
        
        // Verify class modifiers
        assertTrue(javaFile.typeSpec.hasModifier(javax.lang.model.element.Modifier.PUBLIC));
        
        // Verify field count matches INSERT columns
        assertEquals(3, javaFile.typeSpec.fieldSpecs.size());
        
        // Verify all fields are private
        javaFile.typeSpec.fieldSpecs.forEach(fieldSpec -> {
            assertTrue(fieldSpec.hasModifier(javax.lang.model.element.Modifier.PRIVATE));
        });
    }

    @Test
    void testCreateInsertDTO_GeneratesBuilderConfiguration() throws IOException {
        JavaFile javaFile = GenerateInsertDTO.createInsertDTO("Product", validInsertMetadata);

        String code = javaFile.toString();
        // Verify Builder annotation has proper configuration
        assertTrue(code.contains("@Builder"));
        assertTrue(code.contains("builderClassName = \"Builder\""));
    }

    @Test
    void testCreateInsertDTO_GeneratesCorrectJavaDoc() throws IOException {
        JavaFile javaFile = GenerateInsertDTO.createInsertDTO("Product", validInsertMetadata);

        String code = javaFile.toString();
        // Verify comprehensive JavaDoc generation
        assertTrue(code.contains("DTO for creating new product entities via POST API"));
        assertTrue(code.contains("Contains all fields required for insertion"));
        assertTrue(code.contains("productName field for product insertion"));
        assertTrue(code.contains("description field for product insertion"));
        assertTrue(code.contains("price field for product insertion"));
    }
}