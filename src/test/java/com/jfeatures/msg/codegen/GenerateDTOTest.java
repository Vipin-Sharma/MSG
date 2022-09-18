package com.jfeatures.msg.codegen;

import com.squareup.javapoet.JavaFile;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenerateDTOTest {

    @Test
    public void testGenerateDTOForMultipleTables() throws Exception {
        String sql = "Select tableC.a, tableC.b, tableD.c, tableD.d, e from tableC as tableC, tableD as tableD, tableE";
        Map<String, String> ddlPerTableName = new HashMap<>();
        ddlPerTableName.put("tableC", "CREATE TABLE tableC (a INT, b NVARCHAR(50))");
        ddlPerTableName.put("tableD", "CREATE TABLE tableD (c INT, d NVARCHAR(50))");
        ddlPerTableName.put("tableE", "CREATE TABLE tableD (e INT)");

        String businessPurposeOfSQL = "BusinessData";

        JavaFile dtoForMultiTableSQL = GenerateDTO.dtoFromSqlAndDdl(sql, ddlPerTableName, businessPurposeOfSQL);

        assertEquals(businessPurposeOfSQL+"DTO", dtoForMultiTableSQL.typeSpec.name);

    }


    @Test
    public void testGenerateDTOForMultipleTablesComplexSQL1() throws Exception {
        String sql = "Select tableC.a, tableC.b, tableD.c, tableD.d, e from tableC as tableC, tableD as tableD, tableE";
        Map<String, String> ddlPerTableName = new HashMap<>();
        ddlPerTableName.put("tableC", "CREATE TABLE tableC (a INT, b NVARCHAR(50))");
        ddlPerTableName.put("tableD", "CREATE TABLE tableD (c INT, d NVARCHAR(50))");
        ddlPerTableName.put("tableE", "CREATE TABLE tableD (e INT)");

        String businessPurposeOfSQL = "BusinessData";

        JavaFile dtoForMultiTableSQL = GenerateDTO.dtoFromSqlAndDdl(sql, ddlPerTableName, businessPurposeOfSQL);

        assertEquals(businessPurposeOfSQL+"DTO", dtoForMultiTableSQL.typeSpec.name);

    }

}
