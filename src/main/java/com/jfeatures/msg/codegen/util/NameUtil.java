package com.jfeatures.msg.codegen.util;

import com.jfeatures.msg.codegen.domain.TableColumn;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.apache.commons.text.CaseUtils;

public class NameUtil {
    public static TypeName getJavaClassTypeName(String daoPackage, String businessPurposeOfSQL, String DAO) {
        return ClassName.get(daoPackage, businessPurposeOfSQL + DAO);
    }

    public static String getPackageName(String businessPurposeOfSQL, String packageType) {
        return "com.jfeatures.msg." + businessPurposeOfSQL + "." + packageType;
    }

    public static String getFieldNameForDTO(TableColumn tableColumn)
    {
        if (tableColumn.columnAliasIfAvailable() != null)
        {
            return tableColumn.columnAliasIfAvailable();
        }
        else
        {
            return CaseUtils.toCamelCase(tableColumn.columnName(), false, '_');
        }
    }

    public static String getFieldNameForDTO(String columnName)
    {
        return CaseUtils.toCamelCase(columnName, false, '_');
    }
}
