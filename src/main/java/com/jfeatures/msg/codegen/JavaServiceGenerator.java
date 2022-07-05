package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.codegen.maven.PomGenerator;
import com.jfeatures.msg.sql.MsgSqlParser;
import com.jfeatures.msg.sql.ReadFileFromResources;
import com.squareup.javapoet.JavaFile;
import net.sf.jsqlparser.JSQLParserException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JavaServiceGenerator {
    public static void main(String[] args) throws Exception
    {
        CreateDirectoryStructure.createDirectoryStructure();
        PomGenerator.generatePomFile();
        PropGenerator.generatePropertiesFile();

        String sql = getSql();
        Map<String, String> ddlPerTableName = getDdlPerTable();

        String businessPurposeOfSQL = "BusinessData";

        JavaFile dtoForMultiTableSQL = GenerateDTO.dtoFromSqlAndDdl(sql, ddlPerTableName, businessPurposeOfSQL);
        JavaFile springBootMainClass = GenerateSpringBootApp.createSpringBootApp(businessPurposeOfSQL);

        List<DBColumn> predicateHavingLiteralsInFromClause = MsgSqlParser.extractPredicateHavingLiteralsFromWhereClause(sql, ddlPerTableName);
        List<DBColumn> predicatesHavingLiteralsInJoinClause = MsgSqlParser.extractPredicateHavingLiteralsFromJoinsClause(sql, ddlPerTableName);

        ArrayList<DBColumn> predicateHavingLiterals = new ArrayList<>();
        predicateHavingLiterals.addAll(predicateHavingLiteralsInFromClause);
        predicateHavingLiterals.addAll(predicatesHavingLiteralsInJoinClause);

        JavaFile controllerClass = GenerateController.createController(businessPurposeOfSQL, predicateHavingLiterals);
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

    private static String getSql() throws URISyntaxException
    {
        return ReadFileFromResources.readFileFromResources("/sql_hardcoded_literal.sql");
    }

    private static Map<String, String> getDdlPerTable() throws IOException, URISyntaxException, JSQLParserException {
        return ReadFileFromResources.readDDLsFromFile("/sakila_ddls_for_test.txt");
    }
}
