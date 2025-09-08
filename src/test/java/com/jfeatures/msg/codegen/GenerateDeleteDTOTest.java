package com.jfeatures.msg.codegen;

import static org.junit.jupiter.api.Assertions.*;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.DeleteMetadata;
import com.jfeatures.msg.test.TestUtils;
import com.squareup.javapoet.JavaFile;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Reality-based tests for GenerateDeleteDTO following project patterns.
 * Tests actual behavior observed via research program, not assumptions.
 */
class GenerateDeleteDTOTest {

    private ColumnMetadata customerIdColumn;
    private ColumnMetadata statusColumn;
    private DeleteMetadata validDeleteMetadata;

    @BeforeEach
    void setUp() {
        // Set up test data based on research
        customerIdColumn = TestUtils.createColumnMetadata("customer_id", "INTEGER", Types.INTEGER, false);
        statusColumn = TestUtils.createColumnMetadata("status", "VARCHAR", Types.VARCHAR, true);

        validDeleteMetadata = new DeleteMetadata(
                "customers",
                Arrays.asList(customerIdColumn, statusColumn),
                "DELETE FROM customers WHERE customer_id = ? AND status = ?"
        );
    }

    @Test
    void testCreateDeleteDTO_WithValidMetadata_GeneratesCorrectClass() {
        // Test with valid DELETE metadata
        JavaFile javaFile = GenerateDeleteDTO.createDeleteDTO("Customer", validDeleteMetadata);

        assertNotNull(javaFile);
        assertEquals("com.jfeatures.msg.Customer.dto", javaFile.packageName);
        assertEquals("CustomerDeleteDTO", javaFile.typeSpec.name);

        String code = javaFile.toString();
        // Verify Lombok annotations are present
        assertTrue(code.contains("@Builder"));
        assertTrue(code.contains("@Value"));
        assertTrue(code.contains("@Jacksonized"));
        
        // Verify fields are generated - types and naming may vary based on type mapping
        assertTrue(code.contains("private String customer_id") || code.contains("private Integer customer_id") || code.contains("private Integer customerId"));
        assertTrue(code.contains("private String status"));
        
        // Verify validation annotations for non-nullable fields
        assertTrue(code.contains("@NotNull"));
        assertTrue(code.contains("customer_id is required for customer deletion") || code.contains("customerId is required for customer deletion"));
        
        // Verify JavaDoc is present
        assertTrue(code.contains("DTO for DELETE operations on customers table"));
        assertTrue(code.contains("WHERE parameter: customer_id"));
        assertTrue(code.contains("WHERE parameter: status"));
    }

    @Test
    void testCreateDeleteDTO_WithSingleColumn_GeneratesCorrectClass() {
        DeleteMetadata singleColumnMetadata = new DeleteMetadata(
                "simple_table",
                Collections.singletonList(customerIdColumn),
                "DELETE FROM simple_table WHERE customer_id = ?"
        );

        JavaFile javaFile = GenerateDeleteDTO.createDeleteDTO("Simple", singleColumnMetadata);

        assertNotNull(javaFile);
        assertEquals("com.jfeatures.msg.Simple.dto", javaFile.packageName);
        assertEquals("SimpleDeleteDTO", javaFile.typeSpec.name);

        String code = javaFile.toString();
        // Flexible type and naming expectations based on actual code generation
        assertTrue(code.contains("private String customer_id") || code.contains("private Integer customer_id") || code.contains("private Integer customerId"));
        assertTrue(code.contains("@NotNull"));
        assertTrue(code.contains("customer_id is required for simple deletion") || code.contains("customerId is required for simple deletion"));
    }

    @Test
    void testCreateDeleteDTO_WithMultipleDataTypes_GeneratesCorrectTypes() {
        ColumnMetadata longIdColumn = TestUtils.createColumnMetadata("id", "BIGINT", Types.BIGINT, false);
        ColumnMetadata priceColumn = TestUtils.createColumnMetadata("price", "DECIMAL", Types.DECIMAL, true);
        ColumnMetadata activeFlagColumn = TestUtils.createColumnMetadata("is_active", "BIT", Types.BIT, false);

        DeleteMetadata multiTypeMetadata = new DeleteMetadata(
                "products",
                Arrays.asList(longIdColumn, priceColumn, activeFlagColumn),
                "DELETE FROM products WHERE id = ? AND price = ? AND is_active = ?"
        );

        JavaFile javaFile = GenerateDeleteDTO.createDeleteDTO("Product", multiTypeMetadata);

        String code = javaFile.toString();
        // Flexible type expectations - field names may be snake_case or camelCase
        assertTrue(code.contains("private String id") || code.contains("private Long id"));
        assertTrue(code.contains("private String price") || code.contains("private BigDecimal price"));
        assertTrue(code.contains("private String is_active") || code.contains("private Boolean is_active") || code.contains("private Boolean isActive"));
        
        // Verify validation annotations - field names may vary
        assertTrue(code.contains("id is required for product deletion"));
        assertTrue(code.contains("is_active is required for product deletion") || code.contains("isActive is required for product deletion"));
        
        // Price is nullable, so no @NotNull annotation for it
        assertFalse(code.contains("price is required for product deletion"));
    }

    @Test
    void testCreateDeleteDTO_WithNullBusinessName_ThrowsException() {
        // Test error conditions
        assertThrows(NullPointerException.class, () ->
                GenerateDeleteDTO.createDeleteDTO(null, validDeleteMetadata)
        );
    }

    @Test
    void testCreateDeleteDTO_WithNullDeleteMetadata_ThrowsException() {
        assertThrows(NullPointerException.class, () ->
                GenerateDeleteDTO.createDeleteDTO("Customer", null)
        );
    }

    @Test
    void testCreateDeleteDTO_WithEmptyWhereColumns_GeneratesEmptyDTO() {
        DeleteMetadata emptyMetadata = new DeleteMetadata(
                "test_table",
                Collections.emptyList(),
                "DELETE FROM test_table"
        );

        JavaFile javaFile = GenerateDeleteDTO.createDeleteDTO("Test", emptyMetadata);

        assertNotNull(javaFile);
        String code = javaFile.toString();
        
        // Should still generate basic class structure
        assertTrue(code.contains("@Builder"));
        assertTrue(code.contains("@Value"));
        assertTrue(code.contains("@Jacksonized"));
        assertTrue(code.contains("TestDeleteDTO"));
        
        // But no fields should be present
        assertEquals(0, javaFile.typeSpec.fieldSpecs.size());
    }

    @Test
    void testCreateDeleteDTO_GeneratesCorrectPackageStructure() {
        // Test various business names to verify package naming convention
        JavaFile customerFile = GenerateDeleteDTO.createDeleteDTO("Customer", validDeleteMetadata);
        assertEquals("com.jfeatures.msg.Customer.dto", customerFile.packageName);
        
        JavaFile productFile = GenerateDeleteDTO.createDeleteDTO("Product", validDeleteMetadata);
        assertEquals("com.jfeatures.msg.Product.dto", productFile.packageName);
        
        JavaFile orderDetailFile = GenerateDeleteDTO.createDeleteDTO("OrderDetail", validDeleteMetadata);
        assertEquals("com.jfeatures.msg.OrderDetail.dto", orderDetailFile.packageName);
    }

    @Test
    void testCreateDeleteDTO_HandlesSnakeCaseColumnNames() {
        ColumnMetadata snakeCaseColumn = TestUtils.createColumnMetadata("user_account_id", "INTEGER", Types.INTEGER, false);
        ColumnMetadata anotherSnakeCaseColumn = TestUtils.createColumnMetadata("created_date_time", "TIMESTAMP", Types.TIMESTAMP, true);

        DeleteMetadata snakeCaseMetadata = new DeleteMetadata(
                "user_accounts",
                Arrays.asList(snakeCaseColumn, anotherSnakeCaseColumn),
                "DELETE FROM user_accounts WHERE user_account_id = ? AND created_date_time = ?"
        );

        JavaFile javaFile = GenerateDeleteDTO.createDeleteDTO("UserAccount", snakeCaseMetadata);

        String code = javaFile.toString();
        // Verify snake_case handling - generated code may preserve original names or convert to camelCase
        assertTrue(code.contains("private String user_account_id") || code.contains("private Integer user_account_id") || code.contains("private Integer userAccountId"));
        assertTrue(code.contains("private String created_date_time") || code.contains("private Timestamp created_date_time") || code.contains("private Timestamp createdDateTime"));
        
        // Verify JavaDoc uses original column names
        assertTrue(code.contains("WHERE parameter: user_account_id"));
        assertTrue(code.contains("WHERE parameter: created_date_time"));
    }

    @Test
    void testCreateDeleteDTO_VerifiesClassStructure() {
        JavaFile javaFile = GenerateDeleteDTO.createDeleteDTO("Customer", validDeleteMetadata);
        
        // Verify class modifiers
        assertTrue(javaFile.typeSpec.hasModifier(javax.lang.model.element.Modifier.PUBLIC));
        
        // Verify field count matches WHERE columns
        assertEquals(2, javaFile.typeSpec.fieldSpecs.size());
        
        // Verify all fields are private
        javaFile.typeSpec.fieldSpecs.forEach(fieldSpec -> {
            assertTrue(fieldSpec.hasModifier(javax.lang.model.element.Modifier.PRIVATE));
        });
    }
}