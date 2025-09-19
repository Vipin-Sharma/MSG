package com.jfeatures.msg.codegen.util;

import static org.junit.jupiter.api.Assertions.*;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.squareup.javapoet.CodeBlock;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SqlBuildersTest {

    private List<ColumnMetadata> testColumns;
    private List<ColumnMetadata> whereColumns;

    @BeforeEach
    void setUp() {
        testColumns = new ArrayList<>();
        testColumns.add(createColumnMetadata("first_name", "VARCHAR"));
        testColumns.add(createColumnMetadata("last_name", "VARCHAR"));
        testColumns.add(createColumnMetadata("age", "INT"));

        whereColumns = new ArrayList<>();
        whereColumns.add(createColumnMetadata("id", "BIGINT"));
        whereColumns.add(createColumnMetadata("status", "VARCHAR"));
    }

    private static ColumnMetadata createColumnMetadata(String columnName, String dataType) {
        ColumnMetadata metadata = new ColumnMetadata();
        metadata.setColumnName(columnName);
        metadata.setColumnTypeName(dataType);
        return metadata;
    }

    @Nested
    @DisplayName("SQL Statement Builders")
    class SqlStatementBuilders {

        @Test
        @DisplayName("buildInsertSql should create properly formatted INSERT statement")
        void buildInsertSql_ShouldCreateFormattedInsertStatement() {
            String result = SqlBuilders.buildInsertSql("users", testColumns);
            
            assertNotNull(result);
            assertTrue(result.contains("INSERT INTO"));
            assertTrue(result.contains("users"));
            assertTrue(result.contains("first_name, last_name, age"));
            assertTrue(result.contains(":firstName,:lastName,:age"));
        }

        @Test
        @DisplayName("buildInsertSql should throw exception for null tableName")
        void buildInsertSql_ShouldThrowExceptionForNullTableName() {
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildInsertSql(null, testColumns)
            );
        }

        @Test
        @DisplayName("buildInsertSql should throw exception for empty tableName")
        void buildInsertSql_ShouldThrowExceptionForEmptyTableName() {
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildInsertSql("", testColumns)
            );
        }

        @Test
        @DisplayName("buildInsertSql should throw exception for null columns")
        void buildInsertSql_ShouldThrowExceptionForNullColumns() {
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildInsertSql("users", null)
            );
        }

        @Test
        @DisplayName("buildInsertSql should throw exception for empty columns")
        void buildInsertSql_ShouldThrowExceptionForEmptyColumns() {
            List<ColumnMetadata> emptyColumns = new ArrayList<>();
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildInsertSql("users", emptyColumns)
            );
        }

        @Test
        @DisplayName("buildUpdateSql should create properly formatted UPDATE statement")
        void buildUpdateSql_ShouldCreateFormattedUpdateStatement() {
            String result = SqlBuilders.buildUpdateSql("users", testColumns, whereColumns);
            
            assertNotNull(result);
            assertTrue(result.contains("UPDATE"));
            assertTrue(result.contains("users"));
            assertTrue(result.contains("SET"));
            assertTrue(result.contains("first_name =:firstName"));
            assertTrue(result.contains("last_name =:lastName"));
            assertTrue(result.contains("age =:age"));
            assertTrue(result.contains("WHERE"));
            assertTrue(result.contains("id =:id"));
            assertTrue(result.contains("status =:status"));
        }

        @Test
        @DisplayName("buildUpdateSql should create UPDATE without WHERE when whereColumns is null")
        void buildUpdateSql_ShouldCreateUpdateWithoutWhereWhenWhereColumnsNull() {
            String result = SqlBuilders.buildUpdateSql("users", testColumns, null);
            
            assertNotNull(result);
            assertTrue(result.contains("UPDATE"));
            assertTrue(result.contains("users"));
            assertTrue(result.contains("SET"));
            assertFalse(result.contains("WHERE"));
        }

        @Test
        @DisplayName("buildUpdateSql should create UPDATE without WHERE when whereColumns is empty")
        void buildUpdateSql_ShouldCreateUpdateWithoutWhereWhenWhereColumnsEmpty() {
            String result = SqlBuilders.buildUpdateSql("users", testColumns, new ArrayList<>());
            
            assertNotNull(result);
            assertTrue(result.contains("UPDATE"));
            assertTrue(result.contains("users"));
            assertTrue(result.contains("SET"));
            assertFalse(result.contains("WHERE"));
        }

        @Test
        @DisplayName("buildUpdateSql should throw exception for null tableName")
        void buildUpdateSql_ShouldThrowExceptionForNullTableName() {
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildUpdateSql(null, testColumns, whereColumns)
            );
        }

        @Test
        @DisplayName("buildUpdateSql should throw exception for null setColumns")
        void buildUpdateSql_ShouldThrowExceptionForNullSetColumns() {
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildUpdateSql("users", null, whereColumns)
            );
        }

        @Test
        @DisplayName("buildDeleteSql should create properly formatted DELETE statement")
        void buildDeleteSql_ShouldCreateFormattedDeleteStatement() {
            String result = SqlBuilders.buildDeleteSql("users", whereColumns);
            
            assertNotNull(result);
            assertTrue(result.contains("DELETE FROM"));
            assertTrue(result.contains("users"));
            assertTrue(result.contains("WHERE"));
            assertTrue(result.contains("id =:id"));
            assertTrue(result.contains("status =:status"));
            assertTrue(result.contains("AND"));
        }

        @Test
        @DisplayName("buildDeleteSql should throw exception for null tableName")
        void buildDeleteSql_ShouldThrowExceptionForNullTableName() {
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildDeleteSql(null, whereColumns)
            );
        }

        @Test
        @DisplayName("buildDeleteSql should throw exception for null whereColumns")
        void buildDeleteSql_ShouldThrowExceptionForNullWhereColumns() {
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildDeleteSql("users", null)
            );
        }

        @Test
        @DisplayName("buildDeleteSql should throw exception for empty whereColumns")
        void buildDeleteSql_ShouldThrowExceptionForEmptyWhereColumns() {
            List<ColumnMetadata> emptyWhereColumns = new ArrayList<>();
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildDeleteSql("users", emptyWhereColumns)
            );
        }
    }

    @Nested
    @DisplayName("Parameter Mapping Code Builders")
    class ParameterMappingCodeBuilders {

        @Test
        @DisplayName("buildDtoParameterMapping should create proper parameter mapping code")
        void buildDtoParameterMapping_ShouldCreateProperParameterMappingCode() {
            CodeBlock result = SqlBuilders.buildDtoParameterMapping(testColumns, "userDto");
            
            assertNotNull(result);
            String code = result.toString();
            assertTrue(code.contains("java.util.Map<java.lang.String, java.lang.Object> paramMap = new java.util.HashMap<>();"));
            assertTrue(code.contains("paramMap.put(\"firstName\", userDto.getFirstName());"));
            assertTrue(code.contains("paramMap.put(\"lastName\", userDto.getLastName());"));
            assertTrue(code.contains("paramMap.put(\"age\", userDto.getAge());"));
        }

        @Test
        @DisplayName("buildDtoParameterMapping should throw exception for null columns")
        void buildDtoParameterMapping_ShouldThrowExceptionForNullColumns() {
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildDtoParameterMapping(null, "userDto")
            );
        }

        @Test
        @DisplayName("buildDtoParameterMapping should throw exception for null dtoParameterName")
        void buildDtoParameterMapping_ShouldThrowExceptionForNullDtoParameterName() {
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildDtoParameterMapping(testColumns, null)
            );
        }

        @Test
        @DisplayName("buildDtoParameterMapping should throw exception for empty dtoParameterName")
        void buildDtoParameterMapping_ShouldThrowExceptionForEmptyDtoParameterName() {
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildDtoParameterMapping(testColumns, "")
            );
        }

        @Test
        @DisplayName("buildDirectParameterMapping should create proper parameter mapping code")
        void buildDirectParameterMapping_ShouldCreateProperParameterMappingCode() {
            List<String> paramNames = List.of("id", "name");
            List<String> paramValues = List.of("userId", "userName");
            
            CodeBlock result = SqlBuilders.buildDirectParameterMapping(paramNames, paramValues);
            
            assertNotNull(result);
            String code = result.toString();
            assertTrue(code.contains("java.util.Map<java.lang.String, java.lang.Object> paramMap = new java.util.HashMap<>();"));
            assertTrue(code.contains("paramMap.put(\"id\", userId);"));
            assertTrue(code.contains("paramMap.put(\"name\", userName);"));
        }

        @Test
        @DisplayName("buildDirectParameterMapping should throw exception for null paramNames")
        void buildDirectParameterMapping_ShouldThrowExceptionForNullParamNames() {
            List<String> paramValues = List.of("value1", "value2");
            
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildDirectParameterMapping(null, paramValues)
            );
        }

        @Test
        @DisplayName("buildDirectParameterMapping should throw exception for null paramValues")
        void buildDirectParameterMapping_ShouldThrowExceptionForNullParamValues() {
            List<String> paramNames = List.of("param1", "param2");
            
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildDirectParameterMapping(paramNames, null)
            );
        }

        @Test
        @DisplayName("buildDirectParameterMapping should throw exception for mismatched list sizes")
        void buildDirectParameterMapping_ShouldThrowExceptionForMismatchedListSizes() {
            List<String> paramNames = List.of("param1", "param2");
            List<String> paramValues = List.of("value1");
            
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildDirectParameterMapping(paramNames, paramValues)
            );
        }

        @Test
        @DisplayName("buildMixedParameterMapping should create proper mixed parameter mapping code")
        void buildMixedParameterMapping_ShouldCreateProperMixedParameterMappingCode() {
            List<String> additionalNames = List.of("status");
            List<String> additionalValues = List.of("activeStatus");
            
            CodeBlock result = SqlBuilders.buildMixedParameterMapping(testColumns, "userDto", additionalNames, additionalValues);
            
            assertNotNull(result);
            String code = result.toString();
            assertTrue(code.contains("java.util.Map<java.lang.String, java.lang.Object> paramMap = new java.util.HashMap<>();"));
            assertTrue(code.contains("paramMap.put(\"firstName\", userDto.getFirstName());"));
            assertTrue(code.contains("paramMap.put(\"status\", activeStatus);"));
        }

        @Test
        @DisplayName("buildMixedParameterMapping should work with null additional parameters")
        void buildMixedParameterMapping_ShouldWorkWithNullAdditionalParameters() {
            CodeBlock result = SqlBuilders.buildMixedParameterMapping(testColumns, "userDto", null, null);
            
            assertNotNull(result);
            String code = result.toString();
            assertTrue(code.contains("java.util.Map<java.lang.String, java.lang.Object> paramMap = new java.util.HashMap<>();"));
            assertTrue(code.contains("paramMap.put(\"firstName\", userDto.getFirstName());"));
        }
    }

    @Nested
    @DisplayName("JDBC Execution Code Builders")
    class JdbcExecutionCodeBuilders {

        @Test
        @DisplayName("buildQueryExecution should create proper query execution code")
        void buildQueryExecution_ShouldCreateProperQueryExecutionCode() {
            CodeBlock result = SqlBuilders.buildQueryExecution("jdbcTemplate", "SELECT_SQL", false);
            
            assertNotNull(result);
            String code = result.toString();
            assertTrue(code.contains("return jdbcTemplate.query(SELECT_SQL, paramMap, rowMapper)"));
            assertFalse(code.contains("log.info"));
        }

        @Test
        @DisplayName("buildQueryExecution should include logging when withLogging is true")
        void buildQueryExecution_ShouldIncludeLoggingWhenWithLoggingTrue() {
            CodeBlock result = SqlBuilders.buildQueryExecution("jdbcTemplate", "SELECT_SQL", true);
            
            assertNotNull(result);
            String code = result.toString();
            assertTrue(code.contains("log.info(\"Executing query: {}\", SELECT_SQL)"));
            assertTrue(code.contains("log.debug(\"Parameters: {}\", paramMap)"));
            assertTrue(code.contains("return jdbcTemplate.query(SELECT_SQL, paramMap, rowMapper)"));
        }

        @Test
        @DisplayName("buildQueryExecution should throw exception for null jdbcTemplateField")
        void buildQueryExecution_ShouldThrowExceptionForNullJdbcTemplateField() {
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildQueryExecution(null, "SELECT_SQL", false)
            );
        }

        @Test
        @DisplayName("buildQueryExecution should throw exception for null sqlConstant")
        void buildQueryExecution_ShouldThrowExceptionForNullSqlConstant() {
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildQueryExecution("jdbcTemplate", null, false)
            );
        }

        @Test
        @DisplayName("buildUpdateExecution should create proper update execution code")
        void buildUpdateExecution_ShouldCreateProperUpdateExecutionCode() {
            CodeBlock result = SqlBuilders.buildUpdateExecution("jdbcTemplate", "UPDATE_SQL", "update", false);
            
            assertNotNull(result);
            String code = result.toString();
            assertTrue(code.contains("int rowsAffected = jdbcTemplate.update(UPDATE_SQL, paramMap)"));
            assertTrue(code.contains("return rowsAffected"));
            assertFalse(code.contains("log.info"));
        }

        @Test
        @DisplayName("buildUpdateExecution should include logging when withLogging is true")
        void buildUpdateExecution_ShouldIncludeLoggingWhenWithLoggingTrue() {
            CodeBlock result = SqlBuilders.buildUpdateExecution("jdbcTemplate", "UPDATE_SQL", "update", true);
            
            assertNotNull(result);
            String code = result.toString();
            assertTrue(code.contains("log.info(\"Executing UPDATE: {}\", UPDATE_SQL)"));
            assertTrue(code.contains("log.debug(\"Parameters: {}\", paramMap)"));
            assertTrue(code.contains("int rowsAffected = jdbcTemplate.update(UPDATE_SQL, paramMap)"));
            assertTrue(code.contains("log.info(\"update affected {} rows\", rowsAffected)"));
            assertTrue(code.contains("return rowsAffected"));
        }

        @Test
        @DisplayName("buildUpdateExecution should throw exception for null jdbcTemplateField")
        void buildUpdateExecution_ShouldThrowExceptionForNullJdbcTemplateField() {
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildUpdateExecution(null, "UPDATE_SQL", "update", false)
            );
        }

        @Test
        @DisplayName("buildUpdateExecution should throw exception for null sqlConstant")
        void buildUpdateExecution_ShouldThrowExceptionForNullSqlConstant() {
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildUpdateExecution("jdbcTemplate", null, "update", false)
            );
        }

        @Test
        @DisplayName("buildUpdateExecution should throw exception for null operation")
        void buildUpdateExecution_ShouldThrowExceptionForNullOperation() {
            assertThrows(IllegalArgumentException.class, () ->
                SqlBuilders.buildUpdateExecution("jdbcTemplate", "UPDATE_SQL", null, false)
            );
        }
    }

    @Nested
    @DisplayName("Utility Methods")
    class UtilityMethods {

        @Test
        @DisplayName("formatSql should format SQL properly")
        void formatSql_ShouldFormatSqlProperly() {
            String sql = "SELECT * FROM users WHERE id = :id";
            String result = SqlBuilders.formatSql(sql);
            
            assertNotNull(result);
            assertFalse(result.contains(": "));
            assertTrue(result.contains(":"));
        }

        @Test
        @DisplayName("formatSql should handle null input")
        void formatSql_ShouldHandleNullInput() {
            String result = SqlBuilders.formatSql(null);
            assertNull(result);
        }

        @Test
        @DisplayName("formatSql should handle empty input")
        void formatSql_ShouldHandleEmptyInput() {
            String result = SqlBuilders.formatSql("");
            assertEquals("", result);
        }

        @Test
        @DisplayName("formatSql should handle whitespace-only input")
        void formatSql_ShouldHandleWhitespaceOnlyInput() {
            String result = SqlBuilders.formatSql("   ");
            assertEquals("   ", result);
        }
    }

    @Nested
    @DisplayName("Parameter Generation Edge Cases")
    class ParameterGenerationEdgeCases {

        @Test
        @DisplayName("buildUpdateSql should handle whereParam columns correctly")
        void buildUpdateSql_ShouldHandleWhereParamColumnsCorrectly() {
            List<ColumnMetadata> whereParamColumns = new ArrayList<>();
            whereParamColumns.add(createColumnMetadata("whereParam1", "BIGINT"));
            whereParamColumns.add(createColumnMetadata("whereParam2", "VARCHAR"));
            
            String result = SqlBuilders.buildUpdateSql("users", testColumns, whereParamColumns);
            
            assertNotNull(result);
            assertTrue(result.contains("WHERE"));
            assertTrue(result.contains("id =:id"));
            assertTrue(result.contains("status =:status"));
        }

        @Test
        @DisplayName("buildDeleteSql should handle whereParam columns correctly")
        void buildDeleteSql_ShouldHandleWhereParamColumnsCorrectly() {
            List<ColumnMetadata> whereParamColumns = new ArrayList<>();
            whereParamColumns.add(createColumnMetadata("whereParam1", "BIGINT"));
            whereParamColumns.add(createColumnMetadata("whereParam2", "VARCHAR"));
            whereParamColumns.add(createColumnMetadata("whereParam3", "INT"));
            whereParamColumns.add(createColumnMetadata("whereParam4", "DATETIME"));
            
            String result = SqlBuilders.buildDeleteSql("users", whereParamColumns);
            
            assertNotNull(result);
            assertTrue(result.contains("WHERE"));
            assertTrue(result.contains("id =:id"));
            assertTrue(result.contains("status =:status"));
            assertTrue(result.contains("category =:category"));
            assertTrue(result.contains("column4 =:param4"));
        }

        @Test
        @DisplayName("buildMixedParameterMapping should handle mismatched additional parameter sizes")
        void buildMixedParameterMapping_ShouldHandleMismatchedAdditionalParameterSizes() {
            List<String> additionalNames = List.of("param1", "param2");
            List<String> additionalValues = List.of("value1");
            
            CodeBlock result = SqlBuilders.buildMixedParameterMapping(testColumns, "userDto", additionalNames, additionalValues);
            
            assertNotNull(result);
            String code = result.toString();
            assertTrue(code.contains("paramMap.put(\"firstName\", userDto.getFirstName())"));
            assertFalse(code.contains("param1"));
        }
    }
}