package com.jfeatures.msg.codegen.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Comprehensive tests for FieldBuilders utility class to achieve 90%+ coverage.
 * Tests field creation for various scenarios including database fields, dependency injection, and SQL fields.
 */
class FieldBuildersTest {

    // ============================= JDBC TEMPLATE FIELD TESTS =========================

    @Test
    void shouldCreateJdbcTemplateField() {
        // When
        FieldSpec field = FieldBuilders.jdbcTemplateField("namedParameterJdbcTemplate");
        
        // Then
        assertThat(field.name).isEqualTo("namedParameterJdbcTemplate");
        assertThat(field.type).isEqualTo(TypeName.get(NamedParameterJdbcTemplate.class));
        assertThat(field.modifiers).containsExactlyInAnyOrder(Modifier.PRIVATE, Modifier.FINAL);
    }

    @Test
    void shouldThrowExceptionForNullJdbcTemplateFieldName() {
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.jdbcTemplateField(null));
    }

    @Test
    void shouldThrowExceptionForEmptyJdbcTemplateFieldName() {
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.jdbcTemplateField(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\t", "\n", " \t\n "})
    void shouldThrowExceptionForWhitespaceOnlyJdbcTemplateFieldName(String fieldName) {
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.jdbcTemplateField(fieldName));
    }

    // ============================= DATA SOURCE FIELD TESTS ===========================

    @Test
    void shouldCreateDataSourceField() {
        // When
        FieldSpec field = FieldBuilders.dataSourceField("customerDataSource");
        
        // Then
        assertThat(field.name).isEqualTo("customerDataSource");
        assertThat(field.type).isEqualTo(TypeName.get(DataSource.class));
        assertThat(field.modifiers).containsExactlyInAnyOrder(Modifier.PRIVATE, Modifier.FINAL);
    }

    @Test
    void shouldThrowExceptionForNullDataSourceFieldName() {
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.dataSourceField(null));
    }

    @Test
    void shouldThrowExceptionForEmptyDataSourceFieldName() {
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.dataSourceField(""));
    }

    // ============================= SQL FIELD TESTS ===================================

    @Test
    void shouldCreateSqlField() {
        // Given
        String sql = "SELECT * FROM customer WHERE id = :id";
        String fieldName = "customerSql";
        
        // When
        FieldSpec field = FieldBuilders.sqlField(sql, fieldName);
        
        // Then
        assertThat(field.name).isEqualTo("customerSql");
        assertThat(field.type).isEqualTo(TypeName.get(String.class));
        assertThat(field.modifiers).containsExactlyInAnyOrder(
            Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
        assertThat(field.initializer.toString()).contains(sql);
    }

    @Test
    void shouldCreateSqlFieldWithMultiLineSQL() {
        // Given
        String multiLineSql = """
            SELECT c.id, c.name, c.email
            FROM customer c
            WHERE c.id = :id
              AND c.active = 1
            ORDER BY c.name
            """;
        
        // When
        FieldSpec field = FieldBuilders.sqlField(multiLineSql, "SELECT_SQL");
        
        // Then
        assertThat(field.initializer.toString()).contains("SELECT c.id, c.name, c.email");
        assertThat(field.initializer.toString()).contains("FROM customer c");
        assertThat(field.initializer.toString()).contains("WHERE c.id = :id");
    }

    @Test
    void shouldCreateSqlFieldWithJavaDoc() {
        // Given
        String sql = "INSERT INTO customer (name) VALUES (:name)";
        String fieldName = "INSERT_SQL";
        String operation = "INSERT";
        String businessName = "Customer";
        
        // When
        FieldSpec field = FieldBuilders.sqlFieldWithJavaDoc(sql, fieldName, operation, businessName);
        
        // Then
        assertThat(field.name).isEqualTo(fieldName);
        assertThat(field.type).isEqualTo(TypeName.get(String.class));
        assertThat(field.modifiers).containsExactlyInAnyOrder(
            Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
        assertThat(field.initializer.toString()).contains(sql);
        assertThat(field.javadoc.toString()).contains("SQL statement for customer insert operations");
    }

    @Test
    void shouldThrowExceptionForNullSqlInField() {
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.sqlField(null, "SQL_FIELD"));
    }

    @Test
    void shouldThrowExceptionForNullFieldNameInSqlField() {
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.sqlField("SELECT * FROM customer", null));
    }

    @Test
    void shouldThrowExceptionForNullParametersInSqlFieldWithJavaDoc() {
        String sql = "SELECT * FROM customer";
        String fieldName = "SQL_FIELD";
        String operation = "SELECT";
        String businessName = "Customer";

        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.sqlFieldWithJavaDoc(null, fieldName, operation, businessName));
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.sqlFieldWithJavaDoc(sql, null, operation, businessName));
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.sqlFieldWithJavaDoc(sql, fieldName, null, businessName));
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.sqlFieldWithJavaDoc(sql, fieldName, operation, null));
    }

    // ============================= DTO FIELD TESTS ===================================

    @ParameterizedTest
    @MethodSource("provideDtoFieldArguments")
    void shouldCreateDtoFields(String columnName, String columnType, String expectedFieldName, 
                               Class<?> expectedType) {
        // Given
        ColumnMetadata metadata = createColumnMetadata(columnName, columnType, true);
        
        // When
        FieldSpec field = FieldBuilders.dtoField(metadata);
        
        // Then
        assertThat(field.name).isEqualTo(expectedFieldName);
        assertThat(field.type).isEqualTo(TypeName.get(expectedType));
        assertThat(field.modifiers).containsExactlyInAnyOrder(Modifier.PRIVATE);
        assertThat(field.javadoc.toString()).contains("Database column: " + columnName);
        assertThat(field.javadoc.toString()).contains("Type: " + columnType);
    }

    static Stream<Arguments> provideDtoFieldArguments() {
        return Stream.of(
            Arguments.of("customer_id", "bigint", "customerId", Long.class),
            Arguments.of("customer_name", "varchar", "customerName", String.class),
            Arguments.of("created_date", "datetime", "createdDate", Timestamp.class),
            Arguments.of("price", "decimal", "price", BigDecimal.class),
            Arguments.of("is_active", "bit", "isActive", Boolean.class)
        );
    }

    @Test
    void shouldCreateDtoFieldWithAlias() {
        // Given
        ColumnMetadata metadata = createColumnMetadata("customer_name", "varchar", false);
        metadata.setColumnAlias("name");
        
        // When
        FieldSpec field = FieldBuilders.dtoField(metadata);
        
        // Then
        assertThat(field.name).isEqualTo("name");
        assertThat(field.javadoc.toString()).contains("Database column: customer_name");
    }

    @Test
    void shouldCreatePublicDtoField() {
        // Given
        ColumnMetadata metadata = createColumnMetadata("customer_id", "bigint", false);
        
        // When
        FieldSpec field = FieldBuilders.publicDtoField(metadata);
        
        // Then
        assertThat(field.name).isEqualTo("customerId");
        assertThat(field.type).isEqualTo(TypeName.get(Long.class));
        assertThat(field.modifiers).containsExactlyInAnyOrder(Modifier.PUBLIC);
        assertThat(field.javadoc.toString()).isEmpty(); // Public DTO fields don't have JavaDoc
    }

    @Test
    void shouldThrowExceptionForNullColumnMetadataInDtoField() {
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.dtoField(null));
    }

    @Test
    void shouldThrowExceptionForNullColumnMetadataInPublicDtoField() {
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.publicDtoField(null));
    }

    // ============================= DAO FIELD TESTS ===================================

    @Test
    void shouldCreateDaoField() {
        // Given
        TypeName daoType = ClassName.get("com.example", "CustomerDAO");
        String businessName = "Customer";
        
        // When
        FieldSpec field = FieldBuilders.daoField(daoType, businessName);
        
        // Then
        assertThat(field.name).isEqualTo("customerDAO");
        assertThat(field.type).isEqualTo(daoType);
        assertThat(field.modifiers).containsExactlyInAnyOrder(Modifier.PRIVATE, Modifier.FINAL);
        assertThat(field.javadoc.toString()).contains("Data access object for customer operations");
    }

    @Test
    void shouldThrowExceptionForNullDaoType() {
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.daoField(null, "Customer"));
    }

    @Test
    void shouldThrowExceptionForNullBusinessNameInDaoField() {
        TypeName daoType = ClassName.get("com.example", "CustomerDAO");
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.daoField(daoType, null));
    }

    @Test
    void shouldThrowExceptionForEmptyBusinessNameInDaoField() {
        TypeName daoType = ClassName.get("com.example", "CustomerDAO");
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.daoField(daoType, ""));
    }

    // ============================= EDGE CASE TESTS ===================================

    @Test
    void shouldHandleSpecialCharactersInSQL() {
        // Given SQL with special formatting
        String specialSql = """
            SELECT /* comment */ 
                id, 
                'special"chars' as field,
                $value
            FROM table_name 
            WHERE field_name = :param
            """;
        
        // When
        FieldSpec field = FieldBuilders.sqlField(specialSql, "SPECIAL_SQL");
        
        // Then
        assertThat(field.name).isEqualTo("SPECIAL_SQL");
        assertThat(field.initializer.toString()).contains(specialSql);
    }

    @Test
    void shouldHandleLongFieldNames() {
        // Given
        String longFieldName = "veryLongFieldNameThatExceedsNormalLengthButShouldStillWork";
        
        // When
        FieldSpec field = FieldBuilders.jdbcTemplateField(longFieldName);
        
        // Then
        assertThat(field.name).isEqualTo(longFieldName);
    }

    @Test
    void shouldHandleComplexColumnTypes() {
        // Given
        ColumnMetadata metadata = createColumnMetadata("complex_field", "nvarchar", true);
        
        // When
        FieldSpec field = FieldBuilders.dtoField(metadata);
        
        // Then
        assertThat(field.name).isEqualTo("complexField");
        // Type mapping should handle nvarchar appropriately
        assertThat(field.type).isNotNull();
    }

    // ============================= CONSTRUCTOR TESTS =================================

    @Test
    void shouldNotAllowInstantiation() throws Exception {
        // Verify utility class cannot be instantiated
        // Use reflection to try to create instance
        java.lang.reflect.Constructor<FieldBuilders> constructor =
            FieldBuilders.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Exception exception = assertThrows(Exception.class, constructor::newInstance);
        
        // The actual exception will be InvocationTargetException wrapping UnsupportedOperationException
        assertThat(exception.getCause()).isInstanceOf(UnsupportedOperationException.class);
        assertThat(exception.getCause().getMessage()).contains("This is a utility class and cannot be instantiated");
    }

    // ============================= HELPER METHODS ====================================

    private static ColumnMetadata createColumnMetadata(String columnName, String dataType, boolean nullable) {
        ColumnMetadata metadata = new ColumnMetadata();
        metadata.setColumnName(columnName);
        metadata.setColumnTypeName(dataType);
        metadata.setIsNullable(nullable ? 1 : 0); // Use setIsNullable with int value
        return metadata;
    }

    // ============================= VALIDATION EDGE CASES =============================

    @Test
    void shouldHandleEmptyStringValidation() {
        // All these should throw IllegalArgumentException for empty strings
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.sqlField("", "fieldName"));
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.sqlField("SELECT * FROM test", ""));
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.sqlFieldWithJavaDoc("SELECT * FROM test", "", "SELECT", "Test"));
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.sqlFieldWithJavaDoc("SELECT * FROM test", "fieldName", "", "Test"));
        assertThrows(IllegalArgumentException.class, 
            () -> FieldBuilders.sqlFieldWithJavaDoc("SELECT * FROM test", "fieldName", "SELECT", ""));
    }

    @Test
    void shouldHandleWhitespaceStringValidation() {
        // All these should throw IllegalArgumentException for whitespace-only strings
        assertThrows(IllegalArgumentException.class,
            () -> FieldBuilders.sqlField("   ", "fieldName"));
        assertThrows(IllegalArgumentException.class,
            () -> FieldBuilders.sqlField("SELECT * FROM test", "   "));
        assertThrows(IllegalArgumentException.class,
            () -> FieldBuilders.dataSourceField("\t\n"));
        TypeName stringType = TypeName.get(String.class);
        assertThrows(IllegalArgumentException.class,
            () -> FieldBuilders.daoField(stringType, "   "));
    }

    @Test
    void shouldCreateFieldsWithDifferentCasing() {
        // Test that field names with different cases are preserved
        assertThat(FieldBuilders.jdbcTemplateField("CamelCaseField").name)
            .isEqualTo("CamelCaseField");
        assertThat(FieldBuilders.dataSourceField("snake_case_field").name)
            .isEqualTo("snake_case_field");
        assertThat(FieldBuilders.sqlField("SELECT 1", "UPPER_CASE_FIELD").name)
            .isEqualTo("UPPER_CASE_FIELD");
    }

    @Test
    void shouldPreserveJavaDocFormatting() {
        // Given
        String businessName = "OrderDetail";
        String operation = "UPDATE";
        
        // When
        FieldSpec field = FieldBuilders.sqlFieldWithJavaDoc(
            "UPDATE order_detail SET status = :status", 
            "UPDATE_SQL", 
            operation, 
            businessName);
        
        // Then
        assertThat(field.javadoc.toString()).contains("orderdetail update operations");
    }
}
