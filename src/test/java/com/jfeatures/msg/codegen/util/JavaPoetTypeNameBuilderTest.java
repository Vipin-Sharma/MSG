package com.jfeatures.msg.codegen.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JavaPoetTypeNameBuilderTest {

    @Test
    void testBuildParameterizedTypeName_ValidInputs_ReturnsParameterizedType() {
        // Given
        TypeName stringTypeName = ClassName.get(String.class);
        ClassName listClassName = ClassName.get(List.class);
        
        // When
        ParameterizedTypeName result = JavaPoetTypeNameBuilder.buildParameterizedTypeName(stringTypeName, listClassName);
        
        // Then
        assertNotNull(result);
        assertEquals("java.util.List<java.lang.String>", result.toString());
        assertEquals(listClassName, result.rawType);
        assertEquals(1, result.typeArguments.size());
        assertEquals(stringTypeName, result.typeArguments.get(0));
    }
    
    @Test
    void testBuildParameterizedTypeName_ComplexTypeName_ReturnsParameterizedType() {
        // Given
        ClassName customerClassName = ClassName.get("com.example.dto", "CustomerDTO");
        ClassName optionalClassName = ClassName.get(Optional.class);
        
        // When
        ParameterizedTypeName result = JavaPoetTypeNameBuilder.buildParameterizedTypeName(customerClassName, optionalClassName);
        
        // Then
        assertNotNull(result);
        assertEquals("java.util.Optional<com.example.dto.CustomerDTO>", result.toString());
        assertEquals(optionalClassName, result.rawType);
        assertEquals(1, result.typeArguments.size());
        assertEquals(customerClassName, result.typeArguments.get(0));
    }
    
    @Test
    void testBuildParameterizedTypeName_NullDtoTypeName_ThrowsException() {
        // Given
        TypeName dtoTypeName = null;
        ClassName containerType = ClassName.get(List.class);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetTypeNameBuilder.buildParameterizedTypeName(dtoTypeName, containerType)
        );
        
        assertEquals("DTO type name cannot be null", exception.getMessage());
    }
    
    @Test
    void testBuildParameterizedTypeName_NullContainerType_ThrowsException() {
        // Given
        TypeName dtoTypeName = ClassName.get(String.class);
        ClassName containerType = null;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetTypeNameBuilder.buildParameterizedTypeName(dtoTypeName, containerType)
        );
        
        assertEquals("Container type cannot be null", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeNameForClass_ValidInputs_ReturnsTypeName() {
        // Given
        String businessDomainName = "Customer";
        String packageType = "dto";
        String classType = "DTO";
        
        // When
        TypeName result = JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessDomainName, packageType, classType);
        
        // Then
        assertNotNull(result);
        // The exact format depends on JavaPoetClassNameBuilder and JavaPackageNameBuilder implementations
        assertTrue(result instanceof ClassName);
        ClassName className = (ClassName) result;
        assertEquals("CustomerDTO", className.simpleName());
        assertTrue(className.packageName().contains("customer"));
        assertTrue(className.packageName().contains("dto"));
    }
    
    @Test
    void testBuildJavaPoetTypeNameForClass_DifferentPackageType_ReturnsTypeName() {
        // Given
        String businessDomainName = "Order";
        String packageType = "controller";
        String classType = "Controller";
        
        // When
        TypeName result = JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessDomainName, packageType, classType);
        
        // Then
        assertNotNull(result);
        assertTrue(result instanceof ClassName);
        ClassName className = (ClassName) result;
        assertEquals("OrderController", className.simpleName());
        assertTrue(className.packageName().contains("order"));
        assertTrue(className.packageName().contains("controller"));
    }
    
    @Test
    void testBuildJavaPoetTypeNameForClass_NullBusinessDomainName_ThrowsException() {
        // Given
        String businessDomainName = null;
        String packageType = "dto";
        String classType = "DTO";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessDomainName, packageType, classType)
        );
        
        assertEquals("SQL business domain name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeNameForClass_EmptyBusinessDomainName_ThrowsException() {
        // Given
        String businessDomainName = "";
        String packageType = "dto";
        String classType = "DTO";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessDomainName, packageType, classType)
        );
        
        assertEquals("SQL business domain name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeNameForClass_WhitespaceOnlyBusinessDomainName_ThrowsException() {
        // Given
        String businessDomainName = "   ";
        String packageType = "dto";
        String classType = "DTO";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessDomainName, packageType, classType)
        );
        
        assertEquals("SQL business domain name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeNameForClass_NullPackageType_ThrowsException() {
        // Given
        String businessDomainName = "Customer";
        String packageType = null;
        String classType = "DTO";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessDomainName, packageType, classType)
        );
        
        assertEquals("Package type cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeNameForClass_EmptyPackageType_ThrowsException() {
        // Given
        String businessDomainName = "Customer";
        String packageType = "";
        String classType = "DTO";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessDomainName, packageType, classType)
        );
        
        assertEquals("Package type cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeNameForClass_WhitespaceOnlyPackageType_ThrowsException() {
        // Given
        String businessDomainName = "Customer";
        String packageType = "   ";
        String classType = "DTO";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessDomainName, packageType, classType)
        );
        
        assertEquals("Package type cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeNameForClass_NullClassType_ThrowsException() {
        // Given
        String businessDomainName = "Customer";
        String packageType = "dto";
        String classType = null;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessDomainName, packageType, classType)
        );
        
        assertEquals("Class type cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeNameForClass_EmptyClassType_ThrowsException() {
        // Given
        String businessDomainName = "Customer";
        String packageType = "dto";
        String classType = "";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessDomainName, packageType, classType)
        );
        
        assertEquals("Class type cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeNameForClass_WhitespaceOnlyClassType_ThrowsException() {
        // Given
        String businessDomainName = "Customer";
        String packageType = "dto";
        String classType = "   ";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessDomainName, packageType, classType)
        );
        
        assertEquals("Class type cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeNameForClass_MultiWordBusinessDomainName_HandlesCorrectly() {
        // Given
        String businessDomainName = "CustomerOrder";
        String packageType = "dao";
        String classType = "DAO";
        
        // When
        TypeName result = JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessDomainName, packageType, classType);
        
        // Then
        assertNotNull(result);
        assertTrue(result instanceof ClassName);
        ClassName className = (ClassName) result;
        assertEquals("CustomerOrderDAO", className.simpleName());
    }
    
    @Test
    void testBuildJavaPoetTypeNameForClass_CamelCaseInputs_HandlesCorrectly() {
        // Given
        String businessDomainName = "productCategory";
        String packageType = "service";
        String classType = "Service";
        
        // When
        TypeName result = JavaPoetTypeNameBuilder.buildJavaPoetTypeNameForClass(businessDomainName, packageType, classType);
        
        // Then
        assertNotNull(result);
        assertTrue(result instanceof ClassName);
        ClassName className = (ClassName) result;
        assertEquals("productCategoryService", className.simpleName());
    }
}