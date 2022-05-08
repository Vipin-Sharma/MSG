package com.jfeatures.msg.codegen;

import com.squareup.javapoet.JavaFile;
import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenerateDTOTest {


    private static final String ddl = "CREATE TABLE Inventory (id INT, name NVARCHAR(50), quantity INT)";
    private static final String sql = "Select id, name, quantity from Inventory";


    /*@Test
    public void testGenerateDTO() throws JSQLParserException, IOException {
        String businessPurposeOfSQL = "InventoryData";
        JavaFile inventoryData = GenerateDTO.getDTO(sql, ddl, businessPurposeOfSQL);

        assertEquals(businessPurposeOfSQL+"DTO", inventoryData.typeSpec.name);
    }*/

    @Test
    public void testGenerateDTOForMultipleTables() throws JSQLParserException, IOException {
        String sql = "Select tableC.a, tableC.b, tableD.c, tableD.d, e from tableC as tableC, tableD as tableD, tableE";
        Map<String, String> ddlPerTableName = new HashMap<>();
        ddlPerTableName.put("tableC", "CREATE TABLE tableC (a INT, b NVARCHAR(50))");
        ddlPerTableName.put("tableD", "CREATE TABLE tableD (c INT, d NVARCHAR(50))");
        ddlPerTableName.put("tableE", "CREATE TABLE tableD (e INT)");

        String businessPurposeOfSQL = "BusinessData";

        JavaFile dtoForMultiTableSQL = GenerateDTO.getDTOForMultiTableSQL(sql, ddlPerTableName, businessPurposeOfSQL);

        assertEquals(businessPurposeOfSQL+"DTO", dtoForMultiTableSQL.typeSpec.name);

    }


    @Test
    public void testGenerateDTOForMultipleTablesComplexSQL1() throws JSQLParserException, IOException {
        String sql = "Select tableC.a, tableC.b, tableD.c, tableD.d, e from tableC as tableC, tableD as tableD, tableE";
        Map<String, String> ddlPerTableName = new HashMap<>();
        ddlPerTableName.put("tableC", "CREATE TABLE tableC (a INT, b NVARCHAR(50))");
        ddlPerTableName.put("tableD", "CREATE TABLE tableD (c INT, d NVARCHAR(50))");
        ddlPerTableName.put("tableE", "CREATE TABLE tableD (e INT)");

        String businessPurposeOfSQL = "BusinessData";

        JavaFile dtoForMultiTableSQL = GenerateDTO.getDTOForMultiTableSQL(sql, ddlPerTableName, businessPurposeOfSQL);

        assertEquals(businessPurposeOfSQL+"DTO", dtoForMultiTableSQL.typeSpec.name);

    }

}
