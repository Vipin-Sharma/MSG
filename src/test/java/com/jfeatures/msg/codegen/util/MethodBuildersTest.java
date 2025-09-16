package com.jfeatures.msg.codegen.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jfeatures.msg.codegen.constants.CodeGenerationConstants;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Comprehensive tests for MethodBuilders utility class to achieve 90%+ coverage.
 * Tests method creation for various scenarios including constructors, REST endpoints, and DAO methods.
 */
class MethodBuildersTest {

    // ============================= JDBC TEMPLATE CONSTRUCTOR TESTS =========================

    @Test
    void shouldCreateJdbcTemplateConstructor() {
        // When
        MethodSpec constructor = MethodBuilders.jdbcTemplateConstructor(CodeGenerationConstants.JDBC_TEMPLATE_FIELD_NAME);
        
        // Then
        assertThat(constructor.name).isEqualTo("<init>");
        assertThat(constructor.modifiers).containsExactlyInAnyOrder(Modifier.PUBLIC);
        assertThat(constructor.parameters)
            .hasSize(1)
            .first()
            .returns(TypeName.get(NamedParameterJdbcTemplate.class), parameter -> parameter.type)
            .returns(CodeGenerationConstants.JDBC_TEMPLATE_FIELD_NAME, parameter -> parameter.name);
        assertThat(constructor.javadoc.toString()).contains("Constructor with dependency injection for JDBC template");
        assertThat(constructor.code.toString())
                .contains("this." + CodeGenerationConstants.JDBC_TEMPLATE_FIELD_NAME + " = "
                        + CodeGenerationConstants.JDBC_TEMPLATE_FIELD_NAME);
    }

    @Test
    void shouldThrowExceptionForNullFieldNameInJdbcTemplateConstructor() {
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.jdbcTemplateConstructor(null));
    }

    @Test
    void shouldThrowExceptionForEmptyFieldNameInJdbcTemplateConstructor() {
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.jdbcTemplateConstructor(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\t", "\n", " \t\n "})
    void shouldThrowExceptionForWhitespaceFieldNameInJdbcTemplateConstructor(String fieldName) {
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.jdbcTemplateConstructor(fieldName));
    }

    // ============================= DUAL PARAMETER CONSTRUCTOR TESTS ===========================

    @Test
    void shouldCreateDualParameterConstructor() {
        // When
        MethodSpec constructor = MethodBuilders.dualParameterConstructor("dataSource", "jdbcTemplate");
        
        // Then
        assertThat(constructor.name).isEqualTo("<init>");
        assertThat(constructor.modifiers).containsExactlyInAnyOrder(Modifier.PUBLIC);
        assertThat(constructor.parameters)
            .extracting(parameter -> parameter.type, parameter -> parameter.name)
            .containsExactly(
                tuple(TypeName.get(DataSource.class), "dataSource"),
                tuple(TypeName.get(NamedParameterJdbcTemplate.class), "jdbcTemplate")
            );
        assertThat(constructor.javadoc.toString()).contains("Constructor with dependency injection");
        assertThat(constructor.code.toString())
            .contains("this.dataSource = dataSource")
            .contains("this.jdbcTemplate = jdbcTemplate");
    }

    @Test
    void shouldThrowExceptionForNullDataSourceFieldInDualConstructor() {
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.dualParameterConstructor(null, "jdbcTemplate"));
    }

    @Test
    void shouldThrowExceptionForNullJdbcTemplateFieldInDualConstructor() {
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.dualParameterConstructor("dataSource", null));
    }

    @Test
    void shouldThrowExceptionForEmptyFieldsInDualConstructor() {
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.dualParameterConstructor("", "jdbcTemplate"));
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.dualParameterConstructor("dataSource", ""));
    }

    // ============================= DEPENDENCY INJECTION CONSTRUCTOR TESTS ===========================

    @Test
    void shouldCreateDependencyInjectionConstructor() {
        // Given
        TypeName dependencyType = ClassName.get("com.example", "CustomerDAO");
        String fieldName = "customerDAO";
        
        // When
        MethodSpec constructor = MethodBuilders.dependencyInjectionConstructor(dependencyType, fieldName);
        
        // Then
        assertThat(constructor.name).isEqualTo("<init>");
        assertThat(constructor.modifiers).containsExactlyInAnyOrder(Modifier.PUBLIC);
        assertThat(constructor.parameters)
            .hasSize(1)
            .first()
            .returns(dependencyType, parameter -> parameter.type)
            .returns(fieldName, parameter -> parameter.name);
        assertThat(constructor.javadoc.toString()).contains("Constructor with dependency injection");
        assertThat(constructor.code.toString()).contains("this.customerDAO = customerDAO");
    }

    @Test
    void shouldThrowExceptionForNullDependencyType() {
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.dependencyInjectionConstructor(null, "fieldName"));
    }

    @Test
    void shouldThrowExceptionForNullFieldNameInDependencyConstructor() {
        TypeName dependencyType = ClassName.get("com.example", "CustomerDAO");
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.dependencyInjectionConstructor(dependencyType, null));
    }

    // ============================= GET ENDPOINT TESTS ===========================

    @Test
    void shouldCreateGetEndpointMethodWithAllParameters() {
        // When
        MethodSpec.Builder methodBuilder = MethodBuilders.getEndpointMethod(
            "getCustomers", 
            TypeName.get(String.class),
            "/api/customers",
            "Retrieve all customers"
        );
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.name).isEqualTo("getCustomers");
        assertThat(method.modifiers).containsExactlyInAnyOrder(Modifier.PUBLIC);
        assertThat(method.returnType).isEqualTo(TypeName.get(String.class));
        assertThat(method.annotations).hasSize(2); // GetMapping and Operation
        
        // Check GetMapping annotation
        assertThat(method.annotations.get(0).type.toString()).contains("GetMapping");
        
        // Check Operation annotation
        assertThat(method.annotations.get(1).type.toString()).contains("Operation");
    }

    @Test
    void shouldCreateGetEndpointMethodWithNullPath() {
        // When
        MethodSpec.Builder methodBuilder = MethodBuilders.getEndpointMethod(
            "getCustomers", 
            TypeName.get(String.class),
            null,
            "Retrieve all customers"
        );
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.annotations).hasSize(2);
        assertThat(method.annotations.get(0).type.toString()).contains("GetMapping");
    }

    @Test
    void shouldCreateGetEndpointMethodWithNullSummary() {
        // When
        MethodSpec.Builder methodBuilder = MethodBuilders.getEndpointMethod(
            "getCustomers", 
            TypeName.get(String.class),
            "/api/customers",
            null
        );
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.annotations).hasSize(1); // Only GetMapping, no Operation
        assertThat(method.annotations.get(0).type.toString()).contains("GetMapping");
    }

    @Test
    void shouldCreateGetEndpointMethodWithEmptySummary() {
        // When
        MethodSpec.Builder methodBuilder = MethodBuilders.getEndpointMethod(
            "getCustomers", 
            TypeName.get(String.class),
            "/api/customers",
            ""
        );
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.annotations).hasSize(1); // Only GetMapping, no Operation
        assertThat(method.annotations.get(0).type.toString()).contains("GetMapping");
    }

    @Test
    void shouldThrowExceptionForNullMethodNameInGetEndpoint() {
        TypeName returnType = TypeName.get(String.class);

        assertThrows(IllegalArgumentException.class,
            () -> MethodBuilders.getEndpointMethod(null, returnType, "/path", "summary"));
    }

    @Test
    void shouldThrowExceptionForNullReturnTypeInGetEndpoint() {
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.getEndpointMethod("methodName", null, "/path", "summary"));
    }

    // ============================= POST ENDPOINT TESTS ===========================

    @Test
    void shouldCreatePostEndpointMethod() {
        // When
        MethodSpec.Builder methodBuilder = MethodBuilders.postEndpointMethod(
            "createCustomer",
            TypeName.get(String.class),
            ClassName.get("com.example", "CustomerRequest"),
            "request",
            "Create a new customer"
        );
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.name).isEqualTo("createCustomer");
        assertThat(method.modifiers).containsExactlyInAnyOrder(Modifier.PUBLIC);
        assertThat(method.returnType).isEqualTo(TypeName.get(String.class));
        assertThat(method.parameters).hasSize(1);
        assertThat(method.parameters.get(0).name).isEqualTo("request");
        assertThat(method.annotations).hasSize(2); // PostMapping and Operation
        
        // Check parameter annotations
        assertThat(method.parameters.get(0).annotations).hasSize(2); // @Valid and @RequestBody
    }

    @Test
    void shouldCreatePostEndpointMethodWithNullSummary() {
        // When
        MethodSpec.Builder methodBuilder = MethodBuilders.postEndpointMethod(
            "createCustomer",
            TypeName.get(String.class),
            ClassName.get("com.example", "CustomerRequest"),
            "request",
            null
        );
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.annotations).hasSize(1); // Only PostMapping, no Operation
    }

    @Test
    void shouldThrowExceptionForNullParametersInPostEndpoint() {
        TypeName returnType = TypeName.get(String.class);
        TypeName requestType = ClassName.get("com.example", "Request");
        
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.postEndpointMethod(null, returnType, requestType, "param", "summary"));
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.postEndpointMethod("method", null, requestType, "param", "summary"));
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.postEndpointMethod("method", returnType, null, "param", "summary"));
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.postEndpointMethod("method", returnType, requestType, null, "summary"));
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.postEndpointMethod("method", returnType, requestType, "", "summary"));
    }

    // ============================= PUT ENDPOINT TESTS ===========================

    @Test
    void shouldCreatePutEndpointMethod() {
        // When
        MethodSpec.Builder methodBuilder = MethodBuilders.putEndpointMethod(
            "updateCustomer",
            TypeName.get(String.class),
            ClassName.get("com.example", "CustomerRequest"),
            "request",
            "Update a customer"
        );
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.name).isEqualTo("updateCustomer");
        assertThat(method.modifiers).containsExactlyInAnyOrder(Modifier.PUBLIC);
        assertThat(method.returnType).isEqualTo(TypeName.get(String.class));
        assertThat(method.parameters).hasSize(1);
        assertThat(method.parameters.get(0).name).isEqualTo("request");
        assertThat(method.annotations).hasSize(2); // PutMapping and Operation
    }

    @Test
    void shouldCreatePutEndpointMethodWithNullSummary() {
        // When
        MethodSpec.Builder methodBuilder = MethodBuilders.putEndpointMethod(
            "updateCustomer",
            TypeName.get(String.class),
            ClassName.get("com.example", "CustomerRequest"),
            "request",
            null
        );
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.annotations).hasSize(1); // Only PutMapping, no Operation
    }

    @Test
    void shouldThrowExceptionForNullParametersInPutEndpoint() {
        TypeName returnType = TypeName.get(String.class);
        TypeName requestType = ClassName.get("com.example", "Request");
        
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.putEndpointMethod(null, returnType, requestType, "param", "summary"));
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.putEndpointMethod("method", null, requestType, "param", "summary"));
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.putEndpointMethod("method", returnType, null, "param", "summary"));
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.putEndpointMethod("method", returnType, requestType, null, "summary"));
    }

    // ============================= DELETE ENDPOINT TESTS ===========================

    @Test
    void shouldCreateDeleteEndpointMethod() {
        // When
        MethodSpec.Builder methodBuilder = MethodBuilders.deleteEndpointMethod(
            "deleteCustomer",
            TypeName.get(String.class),
            "Delete a customer"
        );
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.name).isEqualTo("deleteCustomer");
        assertThat(method.modifiers).containsExactlyInAnyOrder(Modifier.PUBLIC);
        assertThat(method.returnType).isEqualTo(TypeName.get(String.class));
        assertThat(method.parameters).isEmpty();
        assertThat(method.annotations).hasSize(2); // DeleteMapping and Operation
    }

    @Test
    void shouldCreateDeleteEndpointMethodWithNullSummary() {
        // When
        MethodSpec.Builder methodBuilder = MethodBuilders.deleteEndpointMethod(
            "deleteCustomer",
            TypeName.get(String.class),
            null
        );
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.annotations).hasSize(1); // Only DeleteMapping, no Operation
    }

    @Test
    void shouldThrowExceptionForNullParametersInDeleteEndpoint() {
        TypeName returnType = TypeName.get(String.class);

        assertThrows(IllegalArgumentException.class,
            () -> MethodBuilders.deleteEndpointMethod(null, returnType, "summary"));
        assertThrows(IllegalArgumentException.class,
            () -> MethodBuilders.deleteEndpointMethod("method", null, "summary"));
    }

    // ============================= DAO METHOD TESTS ===========================

    @Test
    void shouldCreateDaoMethodWithParameters() {
        // Given
        List<ParameterSpec> parameters = List.of(
            ParameterSpec.builder(String.class, "param1").build(),
            ParameterSpec.builder(Integer.class, "param2").build()
        );
        
        // When
        MethodSpec.Builder methodBuilder = MethodBuilders.daoMethod(
            "findCustomers",
            TypeName.get(List.class),
            parameters
        );
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.name).isEqualTo("findCustomers");
        assertThat(method.modifiers).containsExactlyInAnyOrder(Modifier.PUBLIC);
        assertThat(method.returnType).isEqualTo(TypeName.get(List.class));
        assertThat(method.parameters).hasSize(2);
        assertThat(method.parameters.get(0).name).isEqualTo("param1");
        assertThat(method.parameters.get(1).name).isEqualTo("param2");
    }

    @Test
    void shouldCreateDaoMethodWithoutParameters() {
        // When
        MethodSpec.Builder methodBuilder = MethodBuilders.daoMethod(
            "findAll",
            TypeName.get(List.class),
            null
        );
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.name).isEqualTo("findAll");
        assertThat(method.parameters).isEmpty();
    }

    @Test
    void shouldCreateDaoMethodWithEmptyParameterList() {
        // When
        MethodSpec.Builder methodBuilder = MethodBuilders.daoMethod(
            "findAll",
            TypeName.get(List.class),
            Collections.emptyList()
        );
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.name).isEqualTo("findAll");
        assertThat(method.parameters).isEmpty();
    }

    @Test
    void shouldThrowExceptionForNullParametersInDaoMethod() {
        TypeName returnType = TypeName.get(String.class);

        assertThrows(IllegalArgumentException.class,
            () -> MethodBuilders.daoMethod(null, returnType, null));
        assertThrows(IllegalArgumentException.class,
            () -> MethodBuilders.daoMethod("method", null, null));
    }

    // ============================= PARAMETER MAP CREATION TESTS ===========================

    @Test
    void shouldCreateParameterMapCreation() {
        // When
        CodeBlock codeBlock = MethodBuilders.parameterMapCreation();
        
        // Then
        assertThat(codeBlock.toString()).contains("java.util.Map<java.lang.String, java.lang.Object> paramMap = new java.util.HashMap<>");
    }

    @Test
    void shouldCreateParameterMapping() {
        // When
        CodeBlock codeBlock = MethodBuilders.parameterMapping("customerId", "customer.getId()");
        
        // Then
        assertThat(codeBlock.toString()).contains("paramMap.put(\"customerId\", customer.getId())");
    }

    @Test
    void shouldThrowExceptionForNullParametersInParameterMapping() {
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.parameterMapping(null, "value"));
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.parameterMapping("param", null));
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.parameterMapping("", "value"));
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.parameterMapping("param", ""));
    }

    // ============================= CONSTRUCTOR TESTS =================================

    @Test
    void shouldNotAllowInstantiation() throws Exception {
        // Verify utility class cannot be instantiated
        // Use reflection to try to create instance
        java.lang.reflect.Constructor<MethodBuilders> constructor =
            MethodBuilders.class.getDeclaredConstructor();
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
    void shouldHandleSpecialCharactersInMethodNames() {
        // When
        MethodSpec constructor = MethodBuilders.jdbcTemplateConstructor("jdbcTemplate$WithSpecialChars");
        
        // Then
        ParameterSpec parameter = constructor.parameters.get(0);
        assertThat(parameter.name).isEqualTo("jdbcTemplate$WithSpecialChars");
    }

    @Test
    void shouldHandleLongMethodNames() {
        // Given
        String longMethodName = "veryLongMethodNameThatExceedsNormalLengthButShouldStillWork";
        
        // When
        MethodSpec.Builder methodBuilder = MethodBuilders.getEndpointMethod(
            longMethodName,
            TypeName.get(String.class),
            "/api/long-path",
            "Long summary"
        );
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.name).isEqualTo(longMethodName);
    }

    @Test
    void shouldValidateEmptyMethodNameInGetEndpoint() {
        TypeName returnType = TypeName.get(String.class);

        assertThrows(IllegalArgumentException.class,
            () -> MethodBuilders.getEndpointMethod("", returnType, "/path", "summary"));
    }

    @Test
    void shouldValidateWhitespaceMethodNameInPostEndpoint() {
        TypeName returnType = TypeName.get(String.class);
        ClassName requestType = ClassName.get("com.example", "Request");

        assertThrows(IllegalArgumentException.class,
            () -> MethodBuilders.postEndpointMethod("   ", returnType,
                requestType, "param", "summary"));
    }

    @Test
    void shouldValidateEmptyParameterNameInParameterMapping() {
        assertThrows(IllegalArgumentException.class, 
            () -> MethodBuilders.parameterMapping("   ", "value"));
    }

    @Test
    void shouldCreateMethodsWithComplexTypes() {
        // Given
        TypeName complexReturnType = ClassName.get("com.example.dto", "CustomerResponseDTO");
        TypeName complexRequestType = ClassName.get("com.example.request", "CreateCustomerRequest");
        
        // When
        MethodSpec.Builder methodBuilder = MethodBuilders.postEndpointMethod(
            "createCustomer",
            complexReturnType,
            complexRequestType,
            "createRequest",
            "Create a new customer with complex types"
        );
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.returnType).isEqualTo(complexReturnType);
        ParameterSpec parameter = method.parameters.get(0);
        assertThat(parameter.type).isEqualTo(complexRequestType);
        assertThat(parameter.name).isEqualTo("createRequest");
    }
}
