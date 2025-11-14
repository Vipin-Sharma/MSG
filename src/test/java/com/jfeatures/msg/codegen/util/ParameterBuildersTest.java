package com.jfeatures.msg.codegen.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

import com.jfeatures.msg.codegen.SQLServerDataTypeEnum;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.domain.DBColumn;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

/**
 * Comprehensive tests for ParameterBuilders utility class to achieve 90%+ coverage.
 * Tests parameter creation for various scenarios including request parameters, path variables, and database columns.
 */
class ParameterBuildersTest {

    private static final String REQUEST_PARAM = "org.springframework.web.bind.annotation.RequestParam";
    private static final String PATH_VARIABLE = "org.springframework.web.bind.annotation.PathVariable";
    private static final String REQUEST_BODY = "org.springframework.web.bind.annotation.RequestBody";
    private static final String VALID = "jakarta.validation.Valid";

    // ============================= REQUEST PARAM TESTS =========================

    @Test
    void shouldCreateBasicRequestParam() {
        // When
        ParameterSpec param = ParameterBuilders.requestParam(TypeName.get(String.class), "customerId");

        // Then
        assertParameter(param, "customerId", TypeName.get(String.class), REQUEST_PARAM);
    }

    @Test
    void shouldCreateRequestParamWithName() {
        // When
        ParameterSpec param = ParameterBuilders.requestParamWithName(
            TypeName.get(Integer.class), 
            "customerId", 
            "customer_id"
        );

        // Then
        assertParameter(param, "customerId", TypeName.get(Integer.class), REQUEST_PARAM);
    }

    @Test
    void shouldCreateOptionalRequestParamWithStringDefault() {
        // When
        ParameterSpec param = ParameterBuilders.optionalRequestParam(
            TypeName.get(String.class), 
            "status", 
            "active"
        );

        // Then
        assertParameter(param, "status", TypeName.get(String.class), REQUEST_PARAM);
    }

    @Test
    void shouldCreateOptionalRequestParamWithNonStringDefault() {
        // When
        ParameterSpec param = ParameterBuilders.optionalRequestParam(
            TypeName.get(Integer.class), 
            "limit", 
            100
        );

        // Then
        assertParameter(param, "limit", TypeName.get(Integer.class), REQUEST_PARAM);
    }

    @Test
    void shouldCreateOptionalRequestParamWithNullDefault() {
        // When
        ParameterSpec param = ParameterBuilders.optionalRequestParam(
            TypeName.get(String.class), 
            "filter", 
            null
        );

        // Then
        assertParameter(param, "filter", TypeName.get(String.class), REQUEST_PARAM);
    }

    @Test
    void shouldThrowExceptionForNullTypeInRequestParam() {
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.requestParam(null, "param"));
    }

    @Test
    void shouldThrowExceptionForNullParamNameInRequestParam() {
        TypeName stringType = TypeName.get(String.class);

        assertThrows(IllegalArgumentException.class,
            () -> ParameterBuilders.requestParam(stringType, null));
    }

    @Test
    void shouldThrowExceptionForEmptyParamNameInRequestParam() {
        TypeName stringType = TypeName.get(String.class);

        assertThrows(IllegalArgumentException.class,
            () -> ParameterBuilders.requestParam(stringType, ""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\t", "\n", " \t\n "})
    void shouldThrowExceptionForWhitespaceParamNameInRequestParam(String paramName) {
        TypeName stringType = TypeName.get(String.class);

        assertThrows(IllegalArgumentException.class,
            () -> ParameterBuilders.requestParam(stringType, paramName));
    }

    @Test
    void shouldThrowExceptionForNullParametersInRequestParamWithName() {
        TypeName type = TypeName.get(String.class);
        
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.requestParamWithName(null, "param", "request_param"));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.requestParamWithName(type, null, "request_param"));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.requestParamWithName(type, "param", null));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.requestParamWithName(type, "", "request_param"));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.requestParamWithName(type, "param", ""));
    }

    @Test
    void shouldThrowExceptionForNullParametersInOptionalRequestParam() {
        TypeName stringType = TypeName.get(String.class);

        assertThrows(IllegalArgumentException.class,
            () -> ParameterBuilders.optionalRequestParam(null, "param", "default"));
        assertThrows(IllegalArgumentException.class,
            () -> ParameterBuilders.optionalRequestParam(stringType, null, "default"));
        assertThrows(IllegalArgumentException.class,
            () -> ParameterBuilders.optionalRequestParam(stringType, "", "default"));
    }

    // ============================= PATH VARIABLE TESTS ===========================

    @Test
    void shouldCreateBasicPathVariable() {
        // When
        ParameterSpec param = ParameterBuilders.pathVariable(TypeName.get(Long.class), "id");

        // Then
        assertParameter(param, "id", TypeName.get(Long.class), PATH_VARIABLE);
    }

    @Test
    void shouldCreatePathVariableWithName() {
        // When
        ParameterSpec param = ParameterBuilders.pathVariableWithName(
            TypeName.get(Long.class), 
            "customerId", 
            "customer-id"
        );

        // Then
        assertParameter(param, "customerId", TypeName.get(Long.class), PATH_VARIABLE);
    }

    @Test
    void shouldThrowExceptionForNullParametersInPathVariable() {
        TypeName longType = TypeName.get(Long.class);

        assertThrows(IllegalArgumentException.class,
            () -> ParameterBuilders.pathVariable(null, "id"));
        assertThrows(IllegalArgumentException.class,
            () -> ParameterBuilders.pathVariable(longType, null));
        assertThrows(IllegalArgumentException.class,
            () -> ParameterBuilders.pathVariable(longType, ""));
    }

    @Test
    void shouldThrowExceptionForNullParametersInPathVariableWithName() {
        TypeName type = TypeName.get(Long.class);
        
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.pathVariableWithName(null, "param", "path-var"));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.pathVariableWithName(type, null, "path-var"));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.pathVariableWithName(type, "param", null));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.pathVariableWithName(type, "", "path-var"));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.pathVariableWithName(type, "param", ""));
    }

    // ============================= REQUEST BODY TESTS ===========================

    @Test
    void shouldCreateValidRequestBody() {
        // When
        ParameterSpec param = ParameterBuilders.validRequestBody(
            ClassName.get("com.example", "CustomerDTO"),
            "customerRequest"
        );

        // Then
        assertParameter(
            param,
            "customerRequest",
            ClassName.get("com.example", "CustomerDTO"),
            VALID,
            REQUEST_BODY
        );
    }

    @Test
    void shouldCreateSimpleRequestBody() {
        // When
        ParameterSpec param = ParameterBuilders.requestBody(
            ClassName.get("com.example", "CustomerDTO"),
            "customerRequest"
        );

        // Then
        assertParameter(
            param,
            "customerRequest",
            ClassName.get("com.example", "CustomerDTO"),
            REQUEST_BODY
        );
    }

    @Test
    void shouldThrowExceptionForNullParametersInValidRequestBody() {
        TypeName type = ClassName.get("com.example", "CustomerDTO");
        
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.validRequestBody(null, "param"));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.validRequestBody(type, null));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.validRequestBody(type, ""));
    }

    @Test
    void shouldThrowExceptionForNullParametersInRequestBody() {
        TypeName type = ClassName.get("com.example", "CustomerDTO");
        
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.requestBody(null, "param"));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.requestBody(type, null));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.requestBody(type, ""));
    }

    // ============================= COLUMN METADATA TESTS ===========================

    @Test
    void shouldCreateParametersFromColumnMetadata() {
        // Given
        List<ColumnMetadata> columns = Arrays.asList(
            createColumnMetadata("customer_id", "bigint"),
            createColumnMetadata("customer_name", "varchar"),
            createColumnMetadata("is_active", "bit")
        );
        
        // When
        List<ParameterSpec> parameters = ParameterBuilders.fromColumnMetadata(columns, true);

        // Then
        assertThat(parameters).hasSize(3);
        assertParameter(parameters.get(0), "customerId", ClassName.get(Long.class), REQUEST_PARAM);
        assertParameter(parameters.get(1), "customerName", ClassName.get(String.class), REQUEST_PARAM);
        assertParameter(parameters.get(2), "isActive", ClassName.get(Boolean.class), REQUEST_PARAM);
    }

    @Test
    void shouldCreateParametersFromColumnMetadataWithoutRequestParamAnnotations() {
        // Given
        List<ColumnMetadata> columns = Arrays.asList(
            createColumnMetadata("customer_id", "bigint")
        );
        
        // When
        List<ParameterSpec> parameters = ParameterBuilders.fromColumnMetadata(columns, false);

        // Then
        assertThat(parameters).hasSize(1);
        assertParameter(parameters.get(0), "customerId", ClassName.get(Long.class));
    }

    @Test
    void shouldReturnEmptyListForNullColumnMetadata() {
        // When
        List<ParameterSpec> parameters = ParameterBuilders.fromColumnMetadata(null, true);
        
        // Then
        assertThat(parameters).isEmpty();
    }

    @Test
    void shouldReturnEmptyListForEmptyColumnMetadataList() {
        // When
        List<ParameterSpec> parameters = ParameterBuilders.fromColumnMetadata(Collections.emptyList(), true);
        
        // Then
        assertThat(parameters).isEmpty();
    }

    // ============================= DB COLUMN TESTS ===========================

    @Test
    void shouldCreateParametersFromDBColumns() {
        // Given
        List<DBColumn> columns = Arrays.asList(
            new DBColumn("customer", "customer_id", "java.lang.Long", "BIGINT"),
            new DBColumn("customer", "customer_name", "java.lang.String", "VARCHAR")
        );
        
        // When
        List<ParameterSpec> parameters = ParameterBuilders.fromDBColumns(columns, true);

        // Then
        assertThat(parameters).hasSize(2);
        assertParameter(parameters.get(0), "customerId", ClassName.get(Long.class), REQUEST_PARAM);
        assertParameter(parameters.get(1), "customerName", ClassName.get(String.class), REQUEST_PARAM);
    }

    @Test
    void shouldCreateParametersFromDBColumnsWithoutRequestParamAnnotations() {
        // Given
        List<DBColumn> columns = Arrays.asList(
            new DBColumn("customer", "customer_id", "java.lang.Long", "BIGINT")
        );
        
        // When
        List<ParameterSpec> parameters = ParameterBuilders.fromDBColumns(columns, false);

        // Then
        assertThat(parameters).hasSize(1);
        assertParameter(parameters.get(0), "customerId", ClassName.get(Long.class));
    }

    @Test
    void shouldReturnEmptyListForNullDBColumns() {
        // When
        List<ParameterSpec> parameters = ParameterBuilders.fromDBColumns(null, true);
        
        // Then
        assertThat(parameters).isEmpty();
    }

    @Test
    void shouldReturnEmptyListForEmptyDBColumnsList() {
        // When
        List<ParameterSpec> parameters = ParameterBuilders.fromDBColumns(Collections.emptyList(), true);
        
        // Then
        assertThat(parameters).isEmpty();
    }

    // ============================= SPECIALIZED PARAMETER TESTS ===========================

    @Test
    void shouldCreatePaginationParameters() {
        // When
        List<ParameterSpec> parameters = ParameterBuilders.paginationParameters();

        // Then
        assertThat(parameters).hasSize(3);
        assertParameter(parameters.get(0), "page", ClassName.get(Integer.class), REQUEST_PARAM);
        assertParameter(parameters.get(1), "size", ClassName.get(Integer.class), REQUEST_PARAM);
        assertParameter(parameters.get(2), "sort", ClassName.get(String.class), REQUEST_PARAM);
    }

    @Test
    void shouldCreateIdParameter() {
        // When
        ParameterSpec param = ParameterBuilders.idParameter(TypeName.get(Long.class), "customerId");

        // Then
        assertParameter(param, "customerId", TypeName.get(Long.class), PATH_VARIABLE);
    }

    @Test
    void shouldThrowExceptionForNullParametersInIdParameter() {
        TypeName idType = TypeName.get(Long.class);

        assertThrows(IllegalArgumentException.class,
            () -> ParameterBuilders.idParameter(null, "id"));
        assertThrows(IllegalArgumentException.class,
            () -> ParameterBuilders.idParameter(idType, null));
        assertThrows(IllegalArgumentException.class,
            () -> ParameterBuilders.idParameter(idType, ""));
    }

    @Test
    void shouldCreateWhereClauseParameters() {
        // Given
        List<ColumnMetadata> whereColumns = Arrays.asList(
            createColumnMetadata("customer_id", "bigint"),
            createColumnMetadata("status", "varchar"),
            createColumnMetadata("category", "varchar"),
            createColumnMetadata("extra_param", "int")
        );
        
        // When
        List<ParameterSpec> parameters = ParameterBuilders.whereClauseParameters(whereColumns);

        // Then
        assertThat(parameters).hasSize(4);
        assertParameter(parameters.get(0), "customerId", ClassName.get(Long.class));
        assertParameter(parameters.get(1), "status", ClassName.get(String.class));
        assertParameter(parameters.get(2), "category", ClassName.get(String.class));
        assertParameter(parameters.get(3), "extraParam", ClassName.get(Integer.class));
    }

    @Test
    void shouldCreateWhereClauseParametersWithGeneratedNames() {
        // Given - columns with generic names that trigger the switch statement
        List<ColumnMetadata> whereColumns = Arrays.asList(
            createColumnMetadata("whereParam1", "bigint"), // index 0 -> "id"
            createColumnMetadata("whereParam2", "varchar"), // index 1 -> "status" 
            createColumnMetadata("whereParam3", "varchar"), // index 2 -> "category"
            createColumnMetadata("whereParam4", "int"), // index 3 -> "param4"
            createColumnMetadata("whereParam5", "int")  // index 4 -> "param5"
        );
        
        // When
        List<ParameterSpec> parameters = ParameterBuilders.whereClauseParameters(whereColumns);
        
        // Then
        assertThat(parameters)
            .hasSize(5)
            .extracting(parameter -> parameter.name)
            .containsExactly("id", "status", "category", "param4", "param5");
    }

    @Test
    void shouldReturnEmptyListForNullWhereClauseColumns() {
        // When
        List<ParameterSpec> parameters = ParameterBuilders.whereClauseParameters(null);
        
        // Then
        assertThat(parameters).isEmpty();
    }

    @Test
    void shouldReturnEmptyListForEmptyWhereClauseColumns() {
        // When
        List<ParameterSpec> parameters = ParameterBuilders.whereClauseParameters(Collections.emptyList());
        
        // Then
        assertThat(parameters).isEmpty();
    }

    // ============================= CONSTRUCTOR TESTS =================================

    @Test
    void shouldNotAllowInstantiation() throws Exception {
        // Verify utility class cannot be instantiated
        // Use reflection to try to create instance
        java.lang.reflect.Constructor<ParameterBuilders> constructor =
            ParameterBuilders.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Exception exception = assertThrows(Exception.class, constructor::newInstance);
        
        // The actual exception will be InvocationTargetException wrapping UnsupportedOperationException
        Throwable cause = exception.getCause();
        assertThat(cause)
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("This is a utility class and cannot be instantiated");
    }

    // ============================= EDGE CASE TESTS ===================================

    @Test
    void shouldHandleComplexTypeNames() {
        // When
        TypeName complexType = ClassName.get("com.example.dto.nested", "CustomerOrderDTO");
        ParameterSpec param = ParameterBuilders.validRequestBody(complexType, "orderRequest");

        // Then
        assertParameter(param, "orderRequest", complexType, VALID, REQUEST_BODY);
    }

    @Test
    void shouldHandleBoxedPrimitiveTypes() {
        // When
        List<ParameterSpec> params = Arrays.asList(
            ParameterBuilders.requestParam(TypeName.get(Integer.class), "intParam"),
            ParameterBuilders.pathVariable(TypeName.get(Boolean.class), "boolParam"),
            ParameterBuilders.optionalRequestParam(TypeName.get(Double.class), "doubleParam", 0.0)
        );

        // Then
        assertParameter(params.get(0), "intParam", TypeName.get(Integer.class), REQUEST_PARAM);
        assertParameter(params.get(1), "boolParam", TypeName.get(Boolean.class), PATH_VARIABLE);
        assertParameter(params.get(2), "doubleParam", TypeName.get(Double.class), REQUEST_PARAM);
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\t\n", " \t ", "    \n\n  "})
    void shouldValidateWhitespaceOnlyInputsInAllMethods(String whitespaceInput) {
        TypeName type = TypeName.get(String.class);
        
        // Test all methods reject whitespace-only inputs
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.requestParam(type, whitespaceInput));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.requestParamWithName(type, whitespaceInput, "valid"));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.requestParamWithName(type, "valid", whitespaceInput));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.optionalRequestParam(type, whitespaceInput, null));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.pathVariable(type, whitespaceInput));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.pathVariableWithName(type, whitespaceInput, "valid"));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.pathVariableWithName(type, "valid", whitespaceInput));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.validRequestBody(type, whitespaceInput));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.requestBody(type, whitespaceInput));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.idParameter(type, whitespaceInput));
    }

    @Test
    void shouldHandlePrimitiveTypeBoxingInColumnMetadata() {
        // Given - Column that maps to a primitive type
        ColumnMetadata intColumn = createColumnMetadata("count_value", "int");
        List<ColumnMetadata> columns = List.of(intColumn);
        
        // When
        List<ParameterSpec> parameters = ParameterBuilders.fromColumnMetadata(columns, false);

        // Then - Should be boxed to Integer, not primitive int
        assertThat(parameters).hasSize(1);
        assertParameter(parameters.get(0), "countValue", ClassName.get(Integer.class));
    }

    @Test
    void shouldBoxPrimitiveTypesReturnedFromTypeLookup() {
        ColumnMetadata column = createColumnMetadata("primitive_flag", "bit");

        try (MockedStatic<SQLServerDataTypeEnum> mocked = mockStatic(SQLServerDataTypeEnum.class)) {
            mocked.when(() -> SQLServerDataTypeEnum.getClassForType("bit")).thenReturn(boolean.class);

            List<ColumnMetadata> columns = List.of(column);
            List<ParameterSpec> parameters = ParameterBuilders.fromColumnMetadata(columns, false);

            assertThat(parameters).hasSize(1);
            assertParameter(parameters.get(0), "primitiveFlag", ClassName.get(Boolean.class));
        }
    }

    private static void assertParameter(
        ParameterSpec parameter,
        String expectedName,
        TypeName expectedType,
        String... expectedAnnotationClassNames
    ) {
        assertThat(parameter)
            .returns(expectedName, spec -> spec.name)
            .returns(expectedType, spec -> spec.type);

        if (expectedAnnotationClassNames.length == 0) {
            assertThat(parameter.annotations).isEmpty();
            return;
        }

        assertThat(parameter.annotations)
            .extracting(annotation -> annotation.type.toString())
            .containsExactly(expectedAnnotationClassNames);
    }

    // ============================= HELPER METHODS ====================================

    private static ColumnMetadata createColumnMetadata(String columnName, String dataType) {
        ColumnMetadata metadata = new ColumnMetadata();
        metadata.setColumnName(columnName);
        metadata.setColumnTypeName(dataType);
        return metadata;
    }
}
