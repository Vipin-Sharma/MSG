package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.codegen.maven.PomGenerator;
import com.jfeatures.msg.sql.MsgSqlParser;
import com.jfeatures.msg.sql.ReadDDL;
import com.squareup.javapoet.JavaFile;
import net.sf.jsqlparser.JSQLParserException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class JavaServiceGenerator {
    public static void main(String[] args) throws IOException, JSQLParserException, URISyntaxException {
        CreateDirectoryStructure.createDirectoryStructure();
        Path pomPath = PomGenerator.generatePomFile();
        Path propertiesFiles = PropGenerator.generatePropertiesFile();

        String sql = getSql();
        Map<String, String> ddlPerTableName = getDdlPerTable();

        String businessPurposeOfSQL = "BusinessData";

        JavaFile dtoForMultiTableSQL = GenerateDTO.getDTOForMultiTableSQL(sql, ddlPerTableName, businessPurposeOfSQL);
        JavaFile springBootMainClass = GenerateSpringBootApp.createSpringBootApp(businessPurposeOfSQL);

        List<DBColumn> predicateHavingLiterals = MsgSqlParser.extractPredicateHavingLiteralsFromWhereClause(sql, ddlPerTableName);

        JavaFile controllerClass = GenerateController.createController(sql, businessPurposeOfSQL, ddlPerTableName, predicateHavingLiterals);
        JavaFile daoClass = GenerateDAO.createDao(businessPurposeOfSQL, predicateHavingLiterals, sql, ddlPerTableName);

        Path javaFilesSrcPath = Paths.get(System.getProperty("java.io.tmpdir")
                + File.separator + "generated"
                + File.separator + "src"
                + File.separator + "main"
                + File.separator + "java");

        dtoForMultiTableSQL.writeTo(javaFilesSrcPath);
        springBootMainClass.writeTo(javaFilesSrcPath);
        controllerClass.writeTo(javaFilesSrcPath);
        daoClass.writeTo(javaFilesSrcPath);

    }

    private static String getSql() {
        return
                """
                        select cus.first_name, cus.last_name, cus.email, cit.city as mycity
                         from customer cus
                         join address adr
                         on cus.address_id = adr.address_id
                         join city cit
                         on adr.city_id = cit.city_id
                         join country cou
                         on cit.country_id = cou.country_id
                         where cou.country = 'Canada'
                        """
                ;
    }

    private static Map<String, String> getDdlPerTable() throws IOException, URISyntaxException {
        //return ReadDDL.readDDLsFromFile("/Adwentureworks_ddls_for_test.txt");
        return ReadDDL.readDDLsFromFile("/sakila_ddls_for_test.txt");
    }
}
