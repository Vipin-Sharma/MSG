package com.jfeatures.msg.sql;

import com.jfeatures.msg.codegen.SQLServerDataTypeEnum;
import com.jfeatures.msg.codegen.domain.DBColumn;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
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
        for (Map.Entry<String, String> entry : ddlPerTableName.entrySet()) {
            Optional<ColumnDefinition> columnDefinitionOptional = ColumnUtils.getColumnDefinition(columnName, entry.getValue());
            if (columnDefinitionOptional.isPresent()) {
                return entry.getKey();
            }
        }
        return null;
    }

    static Map<String, Map<String, DBColumn>> getColumnsPerTableName(String propertyFileName) throws SQLException, IOException, ClassNotFoundException {

        Map<String, Map<String, DBColumn>> result = new HashMap<>();

        Map<String, Map<String, String>> columnDetailsPerTableName = ReadFileFromResources.readTableDetailsFromDatabase(propertyFileName);

        for (Map.Entry<String, Map<String, String>> entry : columnDetailsPerTableName.entrySet()) {
            String tableName = entry.getKey();
            Map<String, String> columnDetails = entry.getValue();

            Map<String, DBColumn> columnsPerTableName = new HashMap<>();
            for (Map.Entry<String, String> column : columnDetails.entrySet()) {
                String columnName = column.getKey();
                // Used split to cover cases like "int identity"
                String columnType = column.getValue().split(" ")[0];
                String jdbcType = SQLServerDataTypeEnum.getJdbcTypeForDBType(columnType);
                DBColumn dbColumn = new DBColumn(tableName, columnName, columnType, jdbcType);
                columnsPerTableName.put(columnName, dbColumn);
            }

            result.put(tableName, columnsPerTableName);
        }

        return result;

    }

}
