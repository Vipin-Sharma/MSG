package com.jfeatures.msg.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MsgDdlParserTest {

    public static final String DDL = "CREATE TABLE Inventory (id INT, name NVARCHAR(50), quantity INT)";

    @Test
    public void testParseDDL() throws JSQLParserException {
        Map<String, ColDataType> columnNameTypeMap = MsgDdlParser.parseDdl(DDL);

        //todo print should be removed once we have all types
        columnNameTypeMap.forEach( (column, colDataType) -> System.out.println(colDataType.getDataType()));

        assertEquals(3, columnNameTypeMap.size());
    }

    @Test
    public void testGetColumnDefinition() throws JSQLParserException {
        ColumnDefinition columnDefinition = MsgDdlParser.getColumnDefinition("id", DDL);
        assertEquals("id", columnDefinition.getColumnName());
    }
}
