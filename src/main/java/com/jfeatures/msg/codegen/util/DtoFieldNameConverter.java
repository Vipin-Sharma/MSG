package com.jfeatures.msg.codegen.util;

import com.jfeatures.msg.codegen.domain.TableColumn;
import org.apache.commons.text.CaseUtils;

public final class DtoFieldNameConverter {

    private DtoFieldNameConverter() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    public static String convertToJavaCamelCase(TableColumn databaseTableColumn) {
        if (databaseTableColumn == null) {
            throw new IllegalArgumentException("Database table column cannot be null");
        }
        
        if (databaseTableColumn.columnAliasIfAvailable() != null) {
            return databaseTableColumn.columnAliasIfAvailable();
        } else {
            return CaseUtils.toCamelCase(databaseTableColumn.columnName(), false, '_');
        }
    }
    
    public static String convertToJavaCamelCase(String databaseColumnName) {
        if (databaseColumnName == null || databaseColumnName.trim().isEmpty()) {
            throw new IllegalArgumentException("Database column name cannot be null or empty");
        }
        
        return CaseUtils.toCamelCase(databaseColumnName, false, '_');
    }
}