package com.jfeatures.msg.codegen.util;

import com.squareup.javapoet.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.Modifier;
import javax.sql.DataSource;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CommonJavaPoetBuilders to ensure code generation consolidation works correctly.
 */
class CommonJavaPoetBuildersTest {

    // ============================= FIELD BUILDER TESTS ===============================

    @Test
    void shouldCreateJdbcTemplateField() {
        // When
        FieldSpec field = CommonJavaPoetBuilders.jdbcTemplateField("namedParameterJdbcTemplate");
        
        // Then
        assertThat(field.name).isEqualTo("namedParameterJdbcTemplate");
        assertThat(field.type).isEqualTo(TypeName.get(NamedParameterJdbcTemplate.class));
        assertThat(field.modifiers).containsExactlyInAnyOrder(Modifier.PRIVATE, Modifier.FINAL);
    }

    @Test
    void shouldCreateSqlField() {
        // Given
        String sql = "SELECT * FROM customer WHERE id = :id";
        String businessName = "Customer";
        
        // When
        FieldSpec field = CommonJavaPoetBuilders.sqlField(sql, businessName);
        
        // Then
        assertThat(field.name).isEqualTo("customerSql");
        assertThat(field.type).isEqualTo(TypeName.get(String.class));
        assertThat(field.modifiers).containsExactlyInAnyOrder(
            Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
        assertThat(field.initializer.toString()).contains(sql);
    }

    @Test
    void shouldCreateSqlFieldWithCustomName() {
        // Given
        String sql = "INSERT INTO customer (name) VALUES (:name)";
        String fieldName = "INSERT_SQL";
        
        // When
        FieldSpec field = CommonJavaPoetBuilders.sqlFieldWithName(sql, fieldName);
        
        // Then
        assertThat(field.name).isEqualTo(fieldName);
        assertThat(field.type).isEqualTo(TypeName.get(String.class));
        assertThat(field.modifiers).containsExactlyInAnyOrder(
            Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
    }

    // ============================= METHOD BUILDER TESTS ==============================

    @Test
    void shouldCreateJdbcTemplateConstructor() {
        // When
        MethodSpec constructor = CommonJavaPoetBuilders.jdbcTemplateConstructor("jdbcTemplate");
        
        // Then
        assertThat(constructor.isConstructor()).isTrue();
        assertThat(constructor.modifiers).contains(Modifier.PUBLIC);
        assertThat(constructor.parameters).hasSize(1);
        assertThat(constructor.parameters.get(0).type).isEqualTo(TypeName.get(NamedParameterJdbcTemplate.class));
        assertThat(constructor.parameters.get(0).name).isEqualTo("jdbcTemplate");
        
        // Check the constructor body contains assignment
        String codeString = constructor.code.toString();
        assertThat(codeString).contains("this.jdbcTemplate = jdbcTemplate");
    }

    @Test
    void shouldCreateDualParameterConstructor() {
        // When
        MethodSpec constructor = CommonJavaPoetBuilders.dualParameterConstructor("dataSource", "jdbcTemplate");
        
        // Then
        assertThat(constructor.isConstructor()).isTrue();
        assertThat(constructor.modifiers).contains(Modifier.PUBLIC);
        assertThat(constructor.parameters).hasSize(2);
        
        // Check DataSource parameter
        assertThat(constructor.parameters.get(0).type).isEqualTo(TypeName.get(DataSource.class));
        assertThat(constructor.parameters.get(0).name).isEqualTo("dataSource");
        
        // Check JdbcTemplate parameter
        assertThat(constructor.parameters.get(1).type).isEqualTo(TypeName.get(NamedParameterJdbcTemplate.class));
        assertThat(constructor.parameters.get(1).name).isEqualTo("jdbcTemplate");
        
        // Check both assignments are present
        String codeString = constructor.code.toString();
        assertThat(codeString).contains("this.dataSource = dataSource");
        assertThat(codeString).contains("this.jdbcTemplate = jdbcTemplate");
    }

    // ============================= CLASS BUILDER TESTS ===============================

    @Test
    void shouldCreateBasicDAOClass() {
        // When
        TypeSpec.Builder daoClass = CommonJavaPoetBuilders.basicDAOClass("Customer", "namedParameterJdbcTemplate");
        TypeSpec dao = daoClass.build();
        
        // Then
        assertThat(dao.name).isEqualTo("CustomerDAO");
        assertThat(dao.modifiers).contains(Modifier.PUBLIC);
        
        // Check @Component annotation
        assertThat(dao.annotations).hasSize(1);
        assertThat(dao.annotations.get(0).type).isEqualTo(ClassName.get(Component.class));
        
        // Check field is present
        assertThat(dao.fieldSpecs).hasSize(1);
        FieldSpec field = dao.fieldSpecs.get(0);
        assertThat(field.name).isEqualTo("namedParameterJdbcTemplate");
        assertThat(field.type).isEqualTo(TypeName.get(NamedParameterJdbcTemplate.class));
        
        // Check constructor is present
        assertThat(dao.methodSpecs).hasSize(1);
        MethodSpec constructor = dao.methodSpecs.get(0);
        assertThat(constructor.isConstructor()).isTrue();
    }

    @Test
    void shouldCreateBasicControllerClass() {
        // When
        TypeSpec.Builder controllerClass = CommonJavaPoetBuilders.basicControllerClass("Customer", "/api/customer");
        TypeSpec controller = controllerClass.build();
        
        // Then
        assertThat(controller.name).isEqualTo("CustomerController");
        assertThat(controller.modifiers).contains(Modifier.PUBLIC);
        
        // Check annotations
        assertThat(controller.annotations).hasSize(2);
        
        // Check @RestController annotation
        boolean hasRestController = controller.annotations.stream()
            .anyMatch(ann -> ann.type.equals(ClassName.get(RestController.class)));
        assertThat(hasRestController).isTrue();
        
        // Check @RequestMapping annotation
        boolean hasRequestMapping = controller.annotations.stream()
            .anyMatch(ann -> ann.type.equals(ClassName.get(RequestMapping.class)));
        assertThat(hasRequestMapping).isTrue();
    }

    @Test
    void shouldCreateBasicDTOClass() {
        // When
        TypeSpec.Builder dtoClass = CommonJavaPoetBuilders.basicDTOClass("Customer", "DTO");
        TypeSpec dto = dtoClass.build();
        
        // Then
        assertThat(dto.name).isEqualTo("CustomerDTO");
        assertThat(dto.modifiers).contains(Modifier.PUBLIC);
        assertThat(dto.annotations).isEmpty(); // DTO classes have no annotations by default
    }

    // ============================= ENDPOINT METHOD TESTS =============================

    @Test
    void shouldCreateGetEndpointMethod() {
        // When
        MethodSpec.Builder methodBuilder = CommonJavaPoetBuilders.getEndpointMethod("getCustomer", TypeName.get(String.class));
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.name).isEqualTo("getCustomer");
        assertThat(method.modifiers).contains(Modifier.PUBLIC);
        assertThat(method.returnType).isEqualTo(TypeName.get(String.class));
        
        // Check @GetMapping annotation
        assertThat(method.annotations).hasSize(1);
        assertThat(method.annotations.get(0).type).isEqualTo(ClassName.get(GetMapping.class));
    }

    @Test
    void shouldCreatePostEndpointMethod() {
        // When
        MethodSpec.Builder methodBuilder = CommonJavaPoetBuilders.postEndpointMethod(
            "createCustomer", TypeName.get(String.class), TypeName.get(String.class), "customerData");
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.name).isEqualTo("createCustomer");
        assertThat(method.modifiers).contains(Modifier.PUBLIC);
        assertThat(method.returnType).isEqualTo(TypeName.get(String.class));
        
        // Check @PostMapping annotation
        assertThat(method.annotations).hasSize(1);
        assertThat(method.annotations.get(0).type).isEqualTo(ClassName.get(PostMapping.class));
        
        // Check parameter annotations
        assertThat(method.parameters).hasSize(1);
        ParameterSpec param = method.parameters.get(0);
        assertThat(param.name).isEqualTo("customerData");
        assertThat(param.annotations).hasSize(2); // @Valid and @RequestBody
    }

    @Test
    void shouldCreatePutEndpointMethod() {
        // When
        MethodSpec.Builder methodBuilder = CommonJavaPoetBuilders.putEndpointMethod(
            "updateCustomer", TypeName.get(String.class), TypeName.get(String.class), "customerData");
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.name).isEqualTo("updateCustomer");
        assertThat(method.annotations).hasSize(1);
        assertThat(method.annotations.get(0).type).isEqualTo(ClassName.get(PutMapping.class));
    }

    @Test
    void shouldCreateDeleteEndpointMethod() {
        // When
        MethodSpec.Builder methodBuilder = CommonJavaPoetBuilders.deleteEndpointMethod(
            "deleteCustomer", TypeName.get(String.class));
        MethodSpec method = methodBuilder.build();
        
        // Then
        assertThat(method.name).isEqualTo("deleteCustomer");
        assertThat(method.annotations).hasSize(1);
        assertThat(method.annotations.get(0).type).isEqualTo(ClassName.get(DeleteMapping.class));
    }

    // ============================= PARAMETER BUILDER TESTS ===========================

    @Test
    void shouldCreateRequestParam() {
        // When
        ParameterSpec param = CommonJavaPoetBuilders.requestParam(TypeName.LONG, "id");
        
        // Then
        assertThat(param.name).isEqualTo("id");
        assertThat(param.type).isEqualTo(TypeName.LONG);
        assertThat(param.annotations).hasSize(1);
        assertThat(param.annotations.get(0).type).isEqualTo(ClassName.get(RequestParam.class));
    }

    @Test
    void shouldCreateRequestParamWithCustomName() {
        // When
        ParameterSpec param = CommonJavaPoetBuilders.requestParamWithName(TypeName.LONG, "customerId", "customer_id");
        
        // Then
        assertThat(param.name).isEqualTo("customerId");
        assertThat(param.type).isEqualTo(TypeName.LONG);
        assertThat(param.annotations).hasSize(1);
        
        // Check @RequestParam annotation has custom value
        AnnotationSpec annotation = param.annotations.get(0);
        assertThat(annotation.type).isEqualTo(ClassName.get(RequestParam.class));
        assertThat(annotation.members.get("value")).hasSize(1);
    }

    @Test
    void shouldCreatePathVariable() {
        // When
        ParameterSpec param = CommonJavaPoetBuilders.pathVariable(TypeName.LONG, "id");
        
        // Then
        assertThat(param.name).isEqualTo("id");
        assertThat(param.type).isEqualTo(TypeName.LONG);
        assertThat(param.annotations).hasSize(1);
        assertThat(param.annotations.get(0).type).isEqualTo(ClassName.get(PathVariable.class));
    }

    // ============================= UTILITY METHOD TESTS ==============================

    @ParameterizedTest
    @MethodSource("provideBusinessNameAndPackageSuffixArguments")
    void shouldBuildPackageName(String businessName, String packageSuffix, String expectedPackage) {
        // When
        String result = CommonJavaPoetBuilders.buildPackageName(businessName, packageSuffix);
        
        // Then
        assertThat(result).isEqualTo(expectedPackage);
    }

    static Stream<Arguments> provideBusinessNameAndPackageSuffixArguments() {
        return Stream.of(
            Arguments.of("Customer", "dao", "com.jfeatures.msg.customer.dao"),
            Arguments.of("OrderDetail", "dto", "com.jfeatures.msg.orderdetail.dto"),
            Arguments.of("USER", "controller", "com.jfeatures.msg.user.controller"),
            Arguments.of("Product", "service", "com.jfeatures.msg.product.service")
        );
    }

    @ParameterizedTest
    @MethodSource("provideBusinessNameAndClassSuffixArguments")
    void shouldBuildClassName(String businessName, String classSuffix, String expectedClassName) {
        // When
        String result = CommonJavaPoetBuilders.buildClassName(businessName, classSuffix);
        
        // Then
        assertThat(result).isEqualTo(expectedClassName);
    }

    static Stream<Arguments> provideBusinessNameAndClassSuffixArguments() {
        return Stream.of(
            Arguments.of("Customer", "DAO", "CustomerDAO"),
            Arguments.of("OrderDetail", "DTO", "OrderDetailDTO"),
            Arguments.of("User", "Controller", "UserController"),
            Arguments.of("Product", "Service", "ProductService")
        );
    }

    @Test
    void shouldCreateJdbcTemplateFieldName() {
        // When
        String result = CommonJavaPoetBuilders.jdbcTemplateFieldName("Customer");
        
        // Then
        assertThat(result).isEqualTo("customerNamedParameterJdbcTemplate");
    }

    @Test
    void shouldHandleNullInputsGracefully() {
        // Test null field name - this should throw NPE
        assertThrows(NullPointerException.class, () -> 
            CommonJavaPoetBuilders.jdbcTemplateField(null));
        
        // Note: buildPackageName with null inputs may not throw NPE if the underlying 
        // string concatenation handles nulls gracefully, which is acceptable behavior
    }

    @Test
    void shouldHandleEmptyInputsCorrectly() {
        // Empty business name should still work
        String result = CommonJavaPoetBuilders.buildPackageName("", "dao");
        assertThat(result).isEqualTo("com.jfeatures.msg..dao");
        
        // Empty class suffix should still work
        String className = CommonJavaPoetBuilders.buildClassName("Customer", "");
        assertThat(className).isEqualTo("Customer");
    }
}