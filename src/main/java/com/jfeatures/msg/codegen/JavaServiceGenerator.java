package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.maven.PomGenerator;
import com.squareup.javapoet.JavaFile;
import net.sf.jsqlparser.JSQLParserException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class JavaServiceGenerator {
    public static void main(String[] args) throws IOException, JSQLParserException {
        CreateDirectoryStructure.createDirectoryStructure();
        Path pomPath = PomGenerator.generatePomFile();

        String sql = "Select tableC.a, tableC.b, tableD.c, tableD.d, e from tableC as tableC, tableD as tableD, tableE" +
                " where tableC.a = 123 and tableD.d = 'abc'";
        Map<String, String> ddlPerTableName = new HashMap<>();
        ddlPerTableName.put("tableC", "CREATE TABLE tableC (a INT, b NVARCHAR(50))");
        ddlPerTableName.put("tableD", "CREATE TABLE tableD (c INT, d NVARCHAR(50))");
        ddlPerTableName.put("tableE", "CREATE TABLE tableD (e INT)");

        String businessPurposeOfSQL = "BusinessData";

        JavaFile dtoForMultiTableSQL = GenerateDTO.getDTOForMultiTableSQL(sql, ddlPerTableName, businessPurposeOfSQL);
        JavaFile springBootMainClass = GenerateSpringBootApp.createSpringBootApp(businessPurposeOfSQL);
        JavaFile controllerClass = GenerateController.createController(sql, businessPurposeOfSQL, ddlPerTableName);
        JavaFile daoClass = GenerateDao.createDao(businessPurposeOfSQL);

        Path javaFilesSrcPath = Paths.get( System.getProperty("java.io.tmpdir")
                + File.separator + "generated"
                + File.separator + "src"
                + File.separator + "main"
                + File.separator + "java");

        dtoForMultiTableSQL.writeTo(javaFilesSrcPath);
        springBootMainClass.writeTo(javaFilesSrcPath);
        controllerClass.writeTo(javaFilesSrcPath);
        daoClass.writeTo(javaFilesSrcPath);

    }
}
