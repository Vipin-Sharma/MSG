package com.jfeatures.msg.codegen.mapping;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.constants.CodeGenerationConstants;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultSetMappingGeneratorTest {

    private List<ColumnMetadata> columnMetadataList;
    private TypeName targetDtoType;
    private ColumnMetadata customerIdColumn;
    private ColumnMetadata customerNameColumn;
    private ColumnMetadata orderAmountColumn;

    @BeforeEach
    void setUp() {
        columnMetadataList = new ArrayList<>();
        targetDtoType = ClassName.get("com.example.dto", "CustomerDTO");
        
        // Create sample column metadata
        customerIdColumn = new ColumnMetadata();
        customerIdColumn.setColumnName("customer_id");
        customerIdColumn.setColumnTypeName("INT");
        customerIdColumn.setColumnType(Types.INTEGER);
        
        customerNameColumn = new ColumnMetadata();
        customerNameColumn.setColumnName("customer_name");
        customerNameColumn.setColumnTypeName("VARCHAR");
        customerNameColumn.setColumnType(Types.VARCHAR);
        
        orderAmountColumn = new ColumnMetadata();
        orderAmountColumn.setColumnName("order_amount");
        orderAmountColumn.setColumnTypeName("DECIMAL");
        orderAmountColumn.setColumnType(Types.DECIMAL);
    }

    @Test
    void testBuildResultSetToObjectMappingCodeWithBuilderPattern() {
        // Test with small number of columns (should use builder pattern)
        columnMetadataList.add(customerIdColumn);
        columnMetadataList.add(customerNameColumn);
        
        String result = ResultSetMappingGenerator.buildResultSetToObjectMappingCode(columnMetadataList, targetDtoType);
        
        assertNotNull(result);
        assertTrue(result.contains("com.example.dto.CustomerDTO.Builder"));
        assertTrue(result.contains("CustomerDTO.builder()"));
        assertTrue(result.contains(".customerId(rs.getInt(\"customer_id\"))"));
        assertTrue(result.contains(".customerName(rs.getString(\"customer_name\"))"));
        assertTrue(result.contains(".build();"));
        assertTrue(result.contains(CodeGenerationConstants.RESULT_LIST_NAME + ".add(" + CodeGenerationConstants.DTO_VARIABLE_NAME + ")"));
    }

    @Test
    void testBuildResultSetToObjectMappingCodeWithSetterPattern() {
        // Test with many columns (should use setter pattern)
        for (int i = 0; i < CodeGenerationConstants.BUILDER_PATTERN_FIELD_THRESHOLD + 1; i++) {
            ColumnMetadata column = new ColumnMetadata();
            column.setColumnName("column_" + i);
            column.setColumnTypeName("VARCHAR");
            column.setColumnType(Types.VARCHAR);
            columnMetadataList.add(column);
        }
        
        String result = ResultSetMappingGenerator.buildResultSetToObjectMappingCode(columnMetadataList, targetDtoType);
        
        assertNotNull(result);
        assertTrue(result.contains("new com.example.dto.CustomerDTO()"));
        assertTrue(result.contains(CodeGenerationConstants.DTO_VARIABLE_NAME + "." + CodeGenerationConstants.SETTER_METHOD_PREFIX));
        assertTrue(result.contains("setColumn0(rs.getString(\"column_0\"))"));
        assertFalse(result.contains(".builder()"));
        assertFalse(result.contains(".build()"));
    }

    @Test
    void testBuildResultSetToObjectMappingCodeWithColumnAlias() {
        customerIdColumn.setColumnAlias("id");
        customerNameColumn.setColumnAlias("name");
        columnMetadataList.add(customerIdColumn);
        columnMetadataList.add(customerNameColumn);
        
        String result = ResultSetMappingGenerator.buildResultSetToObjectMappingCode(columnMetadataList, targetDtoType);
        
        assertNotNull(result);
        assertTrue(result.contains("rs.getInt(\"id\")"));
        assertTrue(result.contains("rs.getString(\"name\")"));
        assertTrue(result.contains(".id("));
        assertTrue(result.contains(".name("));
    }

    @Test
    void testBuildResultSetToObjectMappingCodeWithComplexColumnNames() {
        ColumnMetadata complexColumn = new ColumnMetadata();
        complexColumn.setColumnName("customer_contact_email_address");
        complexColumn.setColumnTypeName("VARCHAR");
        complexColumn.setColumnType(Types.VARCHAR);
        columnMetadataList.add(complexColumn);
        
        String result = ResultSetMappingGenerator.buildResultSetToObjectMappingCode(columnMetadataList, targetDtoType);
        
        assertNotNull(result);
        assertTrue(result.contains(".customerContactEmailAddress("));
        assertTrue(result.contains("rs.getString(\"customer_contact_email_address\")"));
    }

    @Test
    void testBuildResultSetToObjectMappingCodeWithVariousDataTypes() {
        ColumnMetadata bigintColumn = new ColumnMetadata();
        bigintColumn.setColumnName("big_number");
        bigintColumn.setColumnTypeName("BIGINT");
        bigintColumn.setColumnType(Types.BIGINT);
        
        ColumnMetadata dateColumn = new ColumnMetadata();
        dateColumn.setColumnName("created_date");
        dateColumn.setColumnTypeName("DATE");
        dateColumn.setColumnType(Types.DATE);
        
        ColumnMetadata booleanColumn = new ColumnMetadata();
        booleanColumn.setColumnName("is_active");
        booleanColumn.setColumnTypeName("BIT");
        booleanColumn.setColumnType(Types.BIT);
        
        columnMetadataList.addAll(Arrays.asList(bigintColumn, dateColumn, booleanColumn));
        
        String result = ResultSetMappingGenerator.buildResultSetToObjectMappingCode(columnMetadataList, targetDtoType);
        
        assertNotNull(result);
        assertTrue(result.contains("rs.getLong(\"big_number\")"));
        assertTrue(result.contains("rs.getDate(\"created_date\")"));
        assertTrue(result.contains("rs.getBoolean(\"is_active\")"));
        assertTrue(result.contains(".bigNumber("));
        assertTrue(result.contains(".createdDate("));
        assertTrue(result.contains(".isActive("));
    }

    @Test
    void testBuildResultSetToObjectMappingCodeAtThreshold() {
        // Test exactly at the threshold (should use builder pattern)
        for (int i = 0; i < CodeGenerationConstants.BUILDER_PATTERN_FIELD_THRESHOLD; i++) {
            ColumnMetadata column = new ColumnMetadata();
            column.setColumnName("field_" + i);
            column.setColumnTypeName("VARCHAR");
            column.setColumnType(Types.VARCHAR);
            columnMetadataList.add(column);
        }
        
        String result = ResultSetMappingGenerator.buildResultSetToObjectMappingCode(columnMetadataList, targetDtoType);
        
        assertNotNull(result);
        assertTrue(result.contains(".builder()"));
        assertTrue(result.contains(".build()"));
        assertFalse(result.contains("new com.example.dto.CustomerDTO()"));
    }

    @Test
    void testBuildResultSetToObjectMappingCodeSingleColumn() {
        columnMetadataList.add(customerIdColumn);
        
        String result = ResultSetMappingGenerator.buildResultSetToObjectMappingCode(columnMetadataList, targetDtoType);
        
        assertNotNull(result);
        assertTrue(result.contains("CustomerDTO.builder()"));
        assertTrue(result.contains(".customerId(rs.getInt(\"customer_id\"))"));
        assertTrue(result.contains(".build();"));
    }

    @Test
    void testBuildResultSetToObjectMappingCodeWithSetterPatternComplexNames() {
        // Create many columns to trigger setter pattern
        for (int i = 0; i < CodeGenerationConstants.BUILDER_PATTERN_FIELD_THRESHOLD + 5; i++) {
            ColumnMetadata column = new ColumnMetadata();
            column.setColumnName("complex_field_name_" + i);
            column.setColumnTypeName("VARCHAR");
            column.setColumnType(Types.VARCHAR);
            columnMetadataList.add(column);
        }
        
        String result = ResultSetMappingGenerator.buildResultSetToObjectMappingCode(columnMetadataList, targetDtoType);
        
        assertNotNull(result);
        assertTrue(result.contains("setComplexFieldName0(rs.getString(\"complex_field_name_0\"))"));
        assertTrue(result.contains("setComplexFieldName1(rs.getString(\"complex_field_name_1\"))"));
    }

    @Test
    void testBuildResultSetToObjectMappingCodeWithMixedAliasesAndNames() {
        customerIdColumn.setColumnAlias("id");
        // customerNameColumn has no alias, should use column name
        
        columnMetadataList.add(customerIdColumn);
        columnMetadataList.add(customerNameColumn);
        
        String result = ResultSetMappingGenerator.buildResultSetToObjectMappingCode(columnMetadataList, targetDtoType);
        
        assertNotNull(result);
        assertTrue(result.contains("rs.getInt(\"id\")"));  // Uses alias
        assertTrue(result.contains("rs.getString(\"customer_name\")"));  // Uses column name
        assertTrue(result.contains(".id("));  // Field name from alias
        assertTrue(result.contains(".customerName("));  // Field name from column name
    }

    @Test
    void testBuildResultSetToObjectMappingCodeNullColumnDefinitions() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            ResultSetMappingGenerator.buildResultSetToObjectMappingCode(null, targetDtoType));
        
        assertEquals("ResultSet column definitions list cannot be null", exception.getMessage());
    }

    @Test
    void testBuildResultSetToObjectMappingCodeEmptyColumnDefinitions() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            ResultSetMappingGenerator.buildResultSetToObjectMappingCode(Collections.emptyList(), targetDtoType));
        
        assertEquals("ResultSet column definitions list cannot be empty", exception.getMessage());
    }

    @Test
    void testBuildResultSetToObjectMappingCodeNullTargetDtoType() {
        columnMetadataList.add(customerIdColumn);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            ResultSetMappingGenerator.buildResultSetToObjectMappingCode(columnMetadataList, null));
        
        assertEquals("Target DTO type name cannot be null", exception.getMessage());
    }

    @Test
    void testColumnNameConversions() {
        // Test various column name formats
        ColumnMetadata[] testColumns = {
            createColumn("simple", "simple"),
            createColumn("snake_case", "snakeCase"),
            createColumn("UPPER_CASE", "UPPERCASE"),
            createColumn("mixed_Case_Name", "mixedCaseName"),
            createColumn("with_numbers_123", "withNumbers123"),
            createColumn("single", "single")
        };
        
        for (ColumnMetadata column : testColumns) {
            columnMetadataList.clear();
            columnMetadataList.add(column);
            
            String result = ResultSetMappingGenerator.buildResultSetToObjectMappingCode(columnMetadataList, targetDtoType);
            assertNotNull(result);
        }
    }

    @Test
    void testEmptyColumnName() {
        ColumnMetadata emptyColumn = new ColumnMetadata();
        emptyColumn.setColumnName("");
        emptyColumn.setColumnTypeName("VARCHAR");
        emptyColumn.setColumnType(Types.VARCHAR);
        columnMetadataList.add(emptyColumn);
        
        String result = ResultSetMappingGenerator.buildResultSetToObjectMappingCode(columnMetadataList, targetDtoType);
        
        assertNotNull(result);
        assertTrue(result.contains("rs.getString(\"\")"));
    }

    @Test
    void testEmptyColumnAlias() {
        customerIdColumn.setColumnAlias("");
        columnMetadataList.add(customerIdColumn);
        
        String result = ResultSetMappingGenerator.buildResultSetToObjectMappingCode(columnMetadataList, targetDtoType);
        
        assertNotNull(result);
        assertTrue(result.contains("rs.getInt(\"\")"));  // Uses empty alias, not column name
    }

    @Test
    void testNullColumnAlias() {
        customerIdColumn.setColumnAlias(null);
        columnMetadataList.add(customerIdColumn);
        
        String result = ResultSetMappingGenerator.buildResultSetToObjectMappingCode(columnMetadataList, targetDtoType);
        
        assertNotNull(result);
        assertTrue(result.contains("rs.getInt(\"customer_id\")"));  // Should fallback to column name
    }

    @Test
    void testWhitespaceInColumnNames() {
        ColumnMetadata whitespaceColumn = new ColumnMetadata();
        whitespaceColumn.setColumnName("   trimmed_name   ");
        whitespaceColumn.setColumnTypeName("VARCHAR");
        whitespaceColumn.setColumnType(Types.VARCHAR);
        columnMetadataList.add(whitespaceColumn);
        
        String result = ResultSetMappingGenerator.buildResultSetToObjectMappingCode(columnMetadataList, targetDtoType);
        
        assertNotNull(result);
        assertTrue(result.contains("rs.getString(\"   trimmed_name   \")"));
    }

    @Test
    void testDifferentPackageStructures() {
        TypeName nestedType = ClassName.get("com.example.deeply.nested.package", "DeepDTO");
        columnMetadataList.add(customerIdColumn);
        
        String result = ResultSetMappingGenerator.buildResultSetToObjectMappingCode(columnMetadataList, nestedType);
        
        assertNotNull(result);
        assertTrue(result.contains("com.example.deeply.nested.package.DeepDTO"));
        assertTrue(result.contains("com.example.deeply.nested.package.DeepDTO.Builder"));
    }

    @Test
    void testSimpleClassNames() {
        TypeName simpleType = ClassName.get("", "SimpleDTO");
        columnMetadataList.add(customerIdColumn);
        
        String result = ResultSetMappingGenerator.buildResultSetToObjectMappingCode(columnMetadataList, simpleType);
        
        assertNotNull(result);
        assertTrue(result.contains("SimpleDTO"));
    }

    private ColumnMetadata createColumn(String columnName, String expectedFieldName) {
        ColumnMetadata column = new ColumnMetadata();
        column.setColumnName(columnName);
        column.setColumnTypeName("VARCHAR");
        column.setColumnType(Types.VARCHAR);
        return column;
    }
}