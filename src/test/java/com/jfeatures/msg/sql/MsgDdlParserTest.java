package com.jfeatures.msg.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MsgDdlParserTest {

    public static final String DDL = "CREATE TABLE Inventory (id INT, name NVARCHAR(50), quantity INT)";

    @Test
    public void testParseDDL() throws JSQLParserException {
        List<ColumnDefinition> columnDefinitions = MsgDdlParser.parseDdl(DDL);

        assertEquals(3, columnDefinitions.size());
    }

    @Test
    public void testGetColumnDefinition() throws JSQLParserException {
        ColumnDefinition columnDefinition = MsgDdlParser.getColumnDefinition("id", DDL);
        assertEquals("id", columnDefinition.getColumnName());
    }
}
