package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.sql.MsgSqlParser;
import com.jfeatures.msg.sql.ReadFileFromResources;
import com.squareup.javapoet.JavaFile;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import picocli.CommandLine;
import picocli.CommandLine.Option;

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
import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;

@Slf4j
@Command(name = "MSG", mixinStandardHelpOptions = true, version = "MSG 1.0",
        description = "Creates a microservice application.")
public class MicroServiceGenerator implements Callable<Integer> {

    private static final String SRC = "src";
    private static final String MAIN = "main";
    private static final String JAVA = "java";
    private static final String TEST = "test";
    private static final String COM = "com";
    private static final String JFEATURES = "jfeatures";
    private static final String RESOURCES = "resources";

    @Option(names = {"-d", "--destination"}, description = "The destination directory of the generated application. Default value is \"user.home\" system property.")
    private String destinationDirectory = System.getProperty("user.home");

    public static void main(String... args) {
        int exitCode = new CommandLine(new MicroServiceGenerator()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        String sql = getSql();
        Map<String, String> ddlPerTableName = getDdlPerTable();
        String businessPurposeOfSQL = "BusinessData";

        JavaFile dtoForMultiTableSQL = GenerateDTO.dtoFromSqlAndDdl(sql, ddlPerTableName, businessPurposeOfSQL);
        JavaFile springBootApplication = GenerateSpringBootApp.createSpringBootApp(businessPurposeOfSQL);

        ArrayList<DBColumn> predicateHavingLiterals = getPredicateHavingLiterals(sql, ddlPerTableName);

        JavaFile controllerClass = GenerateController.createController(businessPurposeOfSQL, predicateHavingLiterals);
        JavaFile daoClass = GenerateDAO.createDao(businessPurposeOfSQL, predicateHavingLiterals, sql, ddlPerTableName);

        String directoryNameWhereCodeWillBeGenerated = destinationDirectory + File.separator + businessPurposeOfSQL;

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

        log.info("Generated spring boot application is available at: {}", directoryNameWhereCodeWillBeGenerated);

        return 0;
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
        try (InputStream inputStream = MicroServiceGenerator.class.getClassLoader().getResourceAsStream(nameOfFileFromResourcesDir)) {
            assert inputStream != null;
            Files.write(pomFilePath, inputStream.readAllBytes());
        }
    }

    private static void writeJavaFile(JavaFile javaFile, Path srcPath) throws IOException
    {
        javaFile.writeTo(srcPath);
    }

    private static void createDirectory(Path path) throws IOException
    {
        Files.createDirectories(path);
    }

    private static String getSql() throws URISyntaxException
    {
        return ReadFileFromResources.readFileFromResources("sample_sql.sql");
    }

    private static Map<String, String> getDdlPerTable() throws IOException, JSQLParserException {
        return ReadFileFromResources.readDDLsFromFile("sample_ddl.sql");
    }
}
