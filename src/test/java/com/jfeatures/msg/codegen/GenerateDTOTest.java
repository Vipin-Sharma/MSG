package com.jfeatures.msg.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.squareup.javapoet.JavaFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.jupiter.api.Test;

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

    @Test
    public void testGenerateDTOWithJoin() throws JSQLParserException, IOException {
        String sql = "Select name, brand as companyName from emp e inner join company c on e.company_id = c.id";
        Map<String, String> ddlPerTableName = new HashMap<>();
        ddlPerTableName.put("emp", "CREATE TABLE emp (id INT, name NVARCHAR(50), company_id INT)");
        ddlPerTableName.put("company", "CREATE TABLE company (id INT, brand NVARCHAR(50))");

        String businessPurposeOfSQL = "BusinessData";

        JavaFile dtoForMultiTableSQL = GenerateDTO.dtoFromSqlAndDdl(sql, ddlPerTableName, businessPurposeOfSQL);

        assertEquals(businessPurposeOfSQL+"DTO", dtoForMultiTableSQL.typeSpec.name);

    }

    @Test
    public void testGenerateDTOWith300fields() throws JSQLParserException, IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Select name ");
        for(int i = 0; i < 300; i++){
            sb.append(", name").append(" AS name").append(i).append(" ");
        }
//        String sql = "Select name, brand as companyName from emp e inner join company c on e.company_id = c.id";
        sb.append("from emp");

        Map<String, String> ddlPerTableName = new HashMap<>();
        ddlPerTableName.put("emp", "CREATE TABLE emp (id INT, name NVARCHAR(50), company_id INT)");
        ddlPerTableName.put("company", "CREATE TABLE company (id INT, brand NVARCHAR(50))");

        String businessPurposeOfSQL = "BusinessData";

        JavaFile dtoForMultiTableSQL = GenerateDTO.dtoFromSqlAndDdl(sb.toString(), ddlPerTableName, businessPurposeOfSQL);

        assertEquals(businessPurposeOfSQL+"DTO", dtoForMultiTableSQL.typeSpec.name);
        assertEquals(301, dtoForMultiTableSQL.typeSpec.fieldSpecs.size());

    }


}
