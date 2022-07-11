package com.jfeatures.msg.sql;

import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

import java.util.Map;
import java.util.Optional;

//todo write tests for this class
public class TableUtils {
    static String getTableName(String tableAlias, String columnName, Map<String, String> tableAliasToTableName, Map<String, String> ddlPerTableName) {
        if (tableAlias != null) {
            return tableAliasToTableName.get(tableAlias);
        }
        return findTableNameByColumnName(columnName, ddlPerTableName);
    }

    static String findTableNameByColumnName(String columnName, Map<String, String> ddlPerTableName) {
        for (String tableName : ddlPerTableName.keySet()) {
            Optional<ColumnDefinition> columnDefinitionOptional = ColumnUtils.getColumnDefinition(columnName, ddlPerTableName.get(tableName));
            if (columnDefinitionOptional.isPresent()) {
                return tableName;
            }
        }
        return null;
    }

}
