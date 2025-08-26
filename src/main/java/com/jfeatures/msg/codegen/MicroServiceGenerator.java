package com.jfeatures.msg.codegen;

import com.jfeatures.msg.Application;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.SqlMetadata;
import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.controller.CodeGenController;
import com.jfeatures.msg.sql.ReadFileFromResources;
import com.squareup.javapoet.JavaFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.SQLException;
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
        String sql = getSql("sample_parameterized_sql.sql");
        String businessPurposeOfSQL = "BusinessData";

        Application application = new Application();
        DataSource dataSource = application.dataSource();
        JdbcTemplate jdbcTemplate = application.jdbcTemplate(dataSource);
        SqlMetadata sqlMetadata = new SqlMetadata(jdbcTemplate);

        CodeGenController codeGenController = new CodeGenController(sqlMetadata);
        List<ColumnMetadata> selectColumnMetadata = codeGenController.selectColumnMetadata();

        JavaFile dtoForMultiTableSQL = GenerateDTO.dtoFromColumnMetadata(selectColumnMetadata, businessPurposeOfSQL);
        JavaFile springBootApplication = GenerateSpringBootApp.createSpringBootApp(businessPurposeOfSQL);

        ArrayList<DBColumn> predicateHavingLiterals = getPredicateHavingLiterals(sql, dataSource);

        //Controller is using parameter metadata from PreparedStatement, no complex SQL parsing needed.
        // This is much simpler and more reliable than complex AST parsing.
        JavaFile controllerClass = GenerateController.createController(businessPurposeOfSQL, predicateHavingLiterals);

        //DAO generation using metadata approach - much simpler and more reliable than SQL parsing
        JavaFile daoClass = GenerateDAO.createDaoFromMetadata(businessPurposeOfSQL, selectColumnMetadata, predicateHavingLiterals, sql);

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

    private static ArrayList<DBColumn> getPredicateHavingLiterals(String sql, DataSource dataSource) throws SQLException
    {
        ParameterMetadataExtractor extractor = new ParameterMetadataExtractor(dataSource);
        List<DBColumn> parameters = extractor.extractParameters(sql);
        return new ArrayList<>(parameters);
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

    public static String getSql(String fileName) throws URISyntaxException
    {
        return ReadFileFromResources.readFileFromResources(fileName);
    }

}
