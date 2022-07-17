package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.sql.MsgSqlParser;
import com.jfeatures.msg.sql.ReadFileFromResources;
import com.squareup.javapoet.JavaFile;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class JavaServiceGenerator {

    private static final String SRC = "src";
    private static final String MAIN = "main";
    private static final String JAVA = "java";
    private static final String TEST = "test";
    private static final String COM = "com";
    private static final String JFEATURES = "jfeatures";
    private static final String RESOURCES = "resources";

    public static void main(String[] args) throws Exception
    {
        String sql = getSql();
        Map<String, String> ddlPerTableName = getDdlPerTable();
        String businessPurposeOfSQL = "BusinessData";

        JavaFile dtoForMultiTableSQL = GenerateDTO.dtoFromSqlAndDdl(sql, ddlPerTableName, businessPurposeOfSQL);
        JavaFile springBootApplication = GenerateSpringBootApp.createSpringBootApp(businessPurposeOfSQL);

        ArrayList<DBColumn> predicateHavingLiterals = getPredicateHavingLiterals(sql, ddlPerTableName);

        JavaFile controllerClass = GenerateController.createController(businessPurposeOfSQL, predicateHavingLiterals);
        JavaFile daoClass = GenerateDAO.createDao(businessPurposeOfSQL, predicateHavingLiterals, sql, ddlPerTableName);

        String directoryNameWhereCodeWillBeGenerated = getCodeGenerationDirectory(businessPurposeOfSQL);
        
        Path srcPath = Paths.get( directoryNameWhereCodeWillBeGenerated
                + File.separator + SRC
                + File.separator + MAIN
                + File.separator + JAVA);

        Path testPath = Paths.get(directoryNameWhereCodeWillBeGenerated
                + File.separator + SRC
                + File.separator + TEST
                + File.separator + JAVA
                + File.separator + COM
                + File.separator + JFEATURES);

        Path resourcesPath = Paths.get(directoryNameWhereCodeWillBeGenerated
                + File.separator + SRC
                + File.separator + MAIN
                + File.separator + RESOURCES);

        createDirectory(srcPath);
        createDirectory(testPath);
        createDirectory(resourcesPath);

        writeJavaFile(dtoForMultiTableSQL, srcPath);
        writeJavaFile(springBootApplication, srcPath);
        writeJavaFile(controllerClass, srcPath);
        writeJavaFile(daoClass, srcPath);

        writeFileFromResources(directoryNameWhereCodeWillBeGenerated + File.separator + "pom.xml", "pom_file.xml");

        writeFileFromResources(directoryNameWhereCodeWillBeGenerated
                + File.separator + SRC
                + File.separator + MAIN
                + File.separator + RESOURCES
                + File.separator + "application.properties", "application_properties_file.txt");

        log.info("Generated Java files in {}", directoryNameWhereCodeWillBeGenerated);
    }

    private static ArrayList<DBColumn> getPredicateHavingLiterals(String sql, Map<String, String> ddlPerTableName) throws JSQLParserException
    {
        List<DBColumn> predicateHavingLiteralsInFromClause = MsgSqlParser.extractPredicateHavingLiteralsFromWhereClause(sql, ddlPerTableName);
        List<DBColumn> predicatesHavingLiteralsInJoinClause = MsgSqlParser.extractPredicateHavingLiteralsFromJoinsClause(sql, ddlPerTableName);

        ArrayList<DBColumn> predicateHavingLiterals = new ArrayList<>();
        predicateHavingLiterals.addAll(predicateHavingLiteralsInFromClause);
        predicateHavingLiterals.addAll(predicatesHavingLiteralsInJoinClause);
        return predicateHavingLiterals;
    }

    private static void writeFileFromResources(String pathToGenerateFile, String nameOfFileFromResourcesDir) throws IOException
    {
        Path pomFilePath = Paths.get(pathToGenerateFile);
        InputStream inputStream = JavaServiceGenerator.class.getClassLoader().getResourceAsStream(nameOfFileFromResourcesDir);
        assert inputStream != null;
        Files.write(pomFilePath, inputStream.readAllBytes());
    }

    private static void writeJavaFile(JavaFile javaFile, Path srcPath) throws IOException
    {
        javaFile.writeTo(srcPath);
    }

    private static void createDirectory(Path path) throws IOException
    {
        Files.createDirectories(path);
    }

    private static String getCodeGenerationDirectory(String businessPurposeOfSQL)
    {
        return System.getProperty("user.home")
                + File.separator + businessPurposeOfSQL;
    }

    private static String getSql() throws URISyntaxException
    {
        return ReadFileFromResources.readFileFromResources("/sql_hardcoded_literal.sql");
    }

    private static Map<String, String> getDdlPerTable() throws IOException, URISyntaxException, JSQLParserException {
        return ReadFileFromResources.readDDLsFromFile("/sakila_ddls_for_test.txt");
    }
}
