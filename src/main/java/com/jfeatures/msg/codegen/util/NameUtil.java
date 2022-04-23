package com.jfeatures.msg.codegen.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

public class NameUtil {
    public static TypeName getJavaClassTypeName(String daoPackage, String businessPurposeOfSQL, String DAO) {
        return ClassName.get(daoPackage, businessPurposeOfSQL + DAO);
    }

    public static String getPackageName(String businessPurposeOfSQL, String packageType) {
        return "com.jfeatures.msg." + businessPurposeOfSQL + "." + packageType;
    }

}
