package com.jfeatures.msg.codegen.util;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.domain.DBColumn;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import jakarta.validation.Valid;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Comprehensive tests for ParameterBuilders utility class to achieve 90%+ coverage.
 * Tests parameter creation for various scenarios including request parameters, path variables, and database columns.
 */
class ParameterBuildersTest {

    // ============================= REQUEST PARAM TESTS =========================

    @Test
    void shouldCreateBasicRequestParam() {
        // When
        ParameterSpec param = ParameterBuilders.requestParam(TypeName.get(String.class), "customerId");
        
        // Then
        assertThat(param.name).isEqualTo("customerId");
        assertThat(param.type).isEqualTo(TypeName.get(String.class));
        assertThat(param.annotations).hasSize(1);
        assertThat(param.annotations.get(0).type.toString()).contains("RequestParam");
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
        assertThat(param.name).isEqualTo("customerId");
        assertThat(param.type).isEqualTo(TypeName.get(Integer.class));
        assertThat(param.annotations).hasSize(1);
        assertThat(param.annotations.get(0).type.toString()).contains("RequestParam");
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
        assertThat(param.name).isEqualTo("status");
        assertThat(param.type).isEqualTo(TypeName.get(String.class));
        assertThat(param.annotations).hasSize(1);
        assertThat(param.annotations.get(0).type.toString()).contains("RequestParam");
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
        assertThat(param.name).isEqualTo("limit");
        assertThat(param.type).isEqualTo(TypeName.get(Integer.class));
        assertThat(param.annotations).hasSize(1);
        assertThat(param.annotations.get(0).type.toString()).contains("RequestParam");
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
        assertThat(param.name).isEqualTo("filter");
        assertThat(param.type).isEqualTo(TypeName.get(String.class));
        assertThat(param.annotations).hasSize(1);
        assertThat(param.annotations.get(0).type.toString()).contains("RequestParam");
    }

    @Test
    void shouldThrowExceptionForNullTypeInRequestParam() {
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.requestParam(null, "param"));
    }

    @Test
    void shouldThrowExceptionForNullParamNameInRequestParam() {
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.requestParam(TypeName.get(String.class), null));
    }

    @Test
    void shouldThrowExceptionForEmptyParamNameInRequestParam() {
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.requestParam(TypeName.get(String.class), ""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\t", "\n", " \t\n "})
    void shouldThrowExceptionForWhitespaceParamNameInRequestParam(String paramName) {
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.requestParam(TypeName.get(String.class), paramName));
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
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.optionalRequestParam(null, "param", "default"));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.optionalRequestParam(TypeName.get(String.class), null, "default"));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.optionalRequestParam(TypeName.get(String.class), "", "default"));
    }

    // ============================= PATH VARIABLE TESTS ===========================

    @Test
    void shouldCreateBasicPathVariable() {
        // When
        ParameterSpec param = ParameterBuilders.pathVariable(TypeName.get(Long.class), "id");
        
        // Then
        assertThat(param.name).isEqualTo("id");
        assertThat(param.type).isEqualTo(TypeName.get(Long.class));
        assertThat(param.annotations).hasSize(1);
        assertThat(param.annotations.get(0).type.toString()).contains("PathVariable");
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
        assertThat(param.name).isEqualTo("customerId");
        assertThat(param.type).isEqualTo(TypeName.get(Long.class));
        assertThat(param.annotations).hasSize(1);
        assertThat(param.annotations.get(0).type.toString()).contains("PathVariable");
    }

    @Test
    void shouldThrowExceptionForNullParametersInPathVariable() {
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.pathVariable(null, "id"));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.pathVariable(TypeName.get(Long.class), null));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.pathVariable(TypeName.get(Long.class), ""));
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
        assertThat(param.name).isEqualTo("customerRequest");
        assertThat(param.type.toString()).contains("CustomerDTO");
        assertThat(param.annotations).hasSize(2); // @Valid and @RequestBody
        
        boolean hasValid = param.annotations.stream()
            .anyMatch(a -> a.type.toString().contains("Valid"));
        boolean hasRequestBody = param.annotations.stream()
            .anyMatch(a -> a.type.toString().contains("RequestBody"));
        
        assertThat(hasValid).isTrue();
        assertThat(hasRequestBody).isTrue();
    }

    @Test
    void shouldCreateSimpleRequestBody() {
        // When
        ParameterSpec param = ParameterBuilders.requestBody(
            ClassName.get("com.example", "CustomerDTO"), 
            "customerRequest"
        );
        
        // Then
        assertThat(param.name).isEqualTo("customerRequest");
        assertThat(param.type.toString()).contains("CustomerDTO");
        assertThat(param.annotations).hasSize(1); // Only @RequestBody
        
        assertThat(param.annotations.get(0).type.toString()).contains("RequestBody");
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
        
        assertThat(parameters.get(0).name).isEqualTo("customerId");
        assertThat(parameters.get(0).type).isEqualTo(ClassName.get(Long.class));
        assertThat(parameters.get(0).annotations).hasSize(1);
        
        assertThat(parameters.get(1).name).isEqualTo("customerName");
        assertThat(parameters.get(1).type).isEqualTo(ClassName.get(String.class));
        
        assertThat(parameters.get(2).name).isEqualTo("isActive");
        assertThat(parameters.get(2).type).isEqualTo(ClassName.get(Boolean.class));
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
        assertThat(parameters.get(0).annotations).isEmpty();
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
        
        assertThat(parameters.get(0).name).isEqualTo("customerId");
        assertThat(parameters.get(0).type.toString()).contains("Long");
        assertThat(parameters.get(0).annotations).hasSize(1);
        
        assertThat(parameters.get(1).name).isEqualTo("customerName");
        assertThat(parameters.get(1).type.toString()).contains("String");
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
        assertThat(parameters.get(0).annotations).isEmpty();
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
        
        assertThat(parameters.get(0).name).isEqualTo("page");
        assertThat(parameters.get(0).type).isEqualTo(ClassName.get(Integer.class));
        
        assertThat(parameters.get(1).name).isEqualTo("size");
        assertThat(parameters.get(1).type).isEqualTo(ClassName.get(Integer.class));
        
        assertThat(parameters.get(2).name).isEqualTo("sort");
        assertThat(parameters.get(2).type).isEqualTo(ClassName.get(String.class));
    }

    @Test
    void shouldCreateIdParameter() {
        // When
        ParameterSpec param = ParameterBuilders.idParameter(TypeName.get(Long.class), "customerId");
        
        // Then
        assertThat(param.name).isEqualTo("customerId");
        assertThat(param.type).isEqualTo(TypeName.get(Long.class));
        assertThat(param.annotations).hasSize(1);
        assertThat(param.annotations.get(0).type.toString()).contains("PathVariable");
    }

    @Test
    void shouldThrowExceptionForNullParametersInIdParameter() {
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.idParameter(null, "id"));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.idParameter(TypeName.get(Long.class), null));
        assertThrows(IllegalArgumentException.class, 
            () -> ParameterBuilders.idParameter(TypeName.get(Long.class), ""));
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
        
        assertThat(parameters.get(0).name).isEqualTo("customerId");
        assertThat(parameters.get(0).type.toString()).contains("Long");
        
        assertThat(parameters.get(1).name).isEqualTo("status");
        assertThat(parameters.get(1).type.toString()).contains("String");
        
        assertThat(parameters.get(2).name).isEqualTo("category");
        assertThat(parameters.get(2).type.toString()).contains("String");
        
        assertThat(parameters.get(3).name).isEqualTo("extraParam");
        assertThat(parameters.get(3).type.toString()).contains("Integer");
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
        assertThat(parameters).hasSize(5);
        
        assertThat(parameters.get(0).name).isEqualTo("id");
        assertThat(parameters.get(1).name).isEqualTo("status");
        assertThat(parameters.get(2).name).isEqualTo("category");
        assertThat(parameters.get(3).name).isEqualTo("param4");
        assertThat(parameters.get(4).name).isEqualTo("param5");
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
    void shouldNotAllowInstantiation() {
        // Verify utility class cannot be instantiated
        Exception exception = assertThrows(Exception.class, () -> {
            // Use reflection to try to create instance
            java.lang.reflect.Constructor<ParameterBuilders> constructor = 
                ParameterBuilders.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
        
        // The actual exception will be InvocationTargetException wrapping UnsupportedOperationException
        assertThat(exception.getCause()).isInstanceOf(UnsupportedOperationException.class);
        assertThat(exception.getCause().getMessage()).contains("This is a utility class and cannot be instantiated");
    }

    // ============================= EDGE CASE TESTS ===================================

    @Test
    void shouldHandleComplexTypeNames() {
        // When
        TypeName complexType = ClassName.get("com.example.dto.nested", "CustomerOrderDTO");
        ParameterSpec param = ParameterBuilders.validRequestBody(complexType, "orderRequest");
        
        // Then
        assertThat(param.type).isEqualTo(complexType);
        assertThat(param.name).isEqualTo("orderRequest");
        assertThat(param.annotations).hasSize(2);
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
        assertThat(params.get(0).type).isEqualTo(TypeName.get(Integer.class));
        assertThat(params.get(1).type).isEqualTo(TypeName.get(Boolean.class));
        assertThat(params.get(2).type).isEqualTo(TypeName.get(Double.class));
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
        assertThat(parameters.get(0).type).isEqualTo(ClassName.get(Integer.class));
        assertThat(parameters.get(0).name).isEqualTo("countValue");
    }

    // ============================= HELPER METHODS ====================================

    private static ColumnMetadata createColumnMetadata(String columnName, String dataType) {
        ColumnMetadata metadata = new ColumnMetadata();
        metadata.setColumnName(columnName);
        metadata.setColumnTypeName(dataType);
        return metadata;
    }
}