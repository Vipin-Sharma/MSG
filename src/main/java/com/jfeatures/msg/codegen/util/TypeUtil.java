package com.jfeatures.msg.codegen.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

public class TypeUtil {
    public static ParameterizedTypeName getParameterizedTypeName(TypeName dtoTypeName, ClassName list) {
        return ParameterizedTypeName.get(list, dtoTypeName);
    }

    public static TypeName getJavaClassTypeName(String businessPurposeOfSQL, String packageName, String classType) {
        return NameUtil.getJavaClassTypeName(NameUtil.getPackageName(businessPurposeOfSQL, packageName), businessPurposeOfSQL, classType);
    }
}
