package com.jfeatures.msg.codegen;

import com.jfeatures.msg.Application;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.ParameterMetadataExtractor;
import com.jfeatures.msg.codegen.dbmetadata.SqlMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadataExtractor;
import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.codegen.util.SqlStatementDetector;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import com.jfeatures.msg.controller.CodeGenController;
import com.jfeatures.msg.sql.ReadFileFromResources;
import com.squareup.javapoet.JavaFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
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

    @Option(names = {"-d", "--destination"}, description = "The destination directory of the generated application. Default value is \"/home/vipin/BusinessData\".")
    private String destinationDirectory = "/home/vipin/BusinessData";

    @Option(names = {"-n", "--name"}, description = "Business purpose name for the generated microservice (e.g., 'Customer', 'Product', 'Order'). Default is 'Customer'.")
    private String businessPurposeName = "Customer";

    @Option(names = {"-f", "--sql-file"}, description = "SQL file to use for generation. Default tries UPDATE first, then SELECT.")
    private String sqlFileName;

    public static void main(String... args) {
        int exitCode = new CommandLine(new MicroServiceGenerator()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        // Clean destination directory
        cleanDestinationDirectory();

        // Determine SQL file to use
        String sql = determineSqlFile();

        log.info("Using business purpose name: {}", businessPurposeName);
        log.info("Generating code in directory: {}", destinationDirectory);

        Application application = new Application();
        DataSource dataSource = application.dataSource();
        JdbcTemplate jdbcTemplate = application.jdbcTemplate(dataSource);
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = application.namedParameterJdbcTemplate(dataSource);
        
        // Detect SQL statement type
        SqlStatementType statementType = SqlStatementDetector.detectStatementType(sql);
        log.info("Detected SQL statement type: {}", statementType);

        JavaFile springBootApplication = GenerateSpringBootApp.createSpringBootApp(businessPurposeName);
        String databaseConfigContent = GenerateDatabaseConfig.createDatabaseConfig(businessPurposeName);

        switch (statementType) {
            case SELECT:
                return generateSelectMicroservice(sql, businessPurposeName, dataSource, jdbcTemplate, springBootApplication, databaseConfigContent);
            case UPDATE:
                return generateUpdateMicroservice(sql, businessPurposeName, dataSource, namedParameterJdbcTemplate, springBootApplication, databaseConfigContent);
            case INSERT:
            case DELETE:
                throw new UnsupportedOperationException("SQL statement type '" + statementType + "' is not yet supported. Currently supported: SELECT, UPDATE");
            default:
                throw new IllegalArgumentException("Unknown or unsupported SQL statement type: '" + statementType + "'. Please provide a valid SELECT or UPDATE statement.");
        }
    }

    private Integer generateUpdateMicroservice(String sql, String businessPurposeOfSQL, DataSource dataSource, 
                                             NamedParameterJdbcTemplate namedParameterJdbcTemplate, 
                                             JavaFile springBootApplication, String databaseConfigContent) throws Exception {
        
        // Extract UPDATE metadata
        UpdateMetadataExtractor updateExtractor = new UpdateMetadataExtractor(dataSource, namedParameterJdbcTemplate);
        UpdateMetadata updateMetadata = updateExtractor.extractUpdateMetadata(sql);
        log.info("Extracted UPDATE metadata for table: {}", updateMetadata.tableName());

        // Generate UPDATE components
        JavaFile updateDTO = GenerateUpdateDTO.createUpdateDTO(businessPurposeOfSQL, updateMetadata);
        JavaFile updateDAO = GenerateUpdateDAO.createUpdateDAO(businessPurposeOfSQL, updateMetadata);
        JavaFile updateController = GenerateUpdateController.createUpdateController(businessPurposeOfSQL, updateMetadata);

        return writeUpdateMicroservice(businessPurposeOfSQL, springBootApplication, databaseConfigContent, 
                                     updateDTO, updateDAO, updateController);
    }

    private Integer generateSelectMicroservice(String sql, String businessPurposeOfSQL, DataSource dataSource, 
                                             JdbcTemplate jdbcTemplate, JavaFile springBootApplication, 
                                             String databaseConfigContent) throws Exception {
        
        SqlMetadata sqlMetadata = new SqlMetadata(jdbcTemplate);
        CodeGenController codeGenController = new CodeGenController(sqlMetadata);
        List<ColumnMetadata> selectColumnMetadata = codeGenController.selectColumnMetadata();

        JavaFile dtoForMultiTableSQL = GenerateDTO.dtoFromColumnMetadata(selectColumnMetadata, businessPurposeOfSQL);
        ArrayList<DBColumn> predicateHavingLiterals = getPredicateHavingLiterals(sql, dataSource);

        //Controller is using parameter metadata from PreparedStatement, no complex SQL parsing needed.
        JavaFile controllerClass = GenerateController.createController(businessPurposeOfSQL, predicateHavingLiterals);

        //DAO generation using metadata approach - much simpler and more reliable than SQL parsing
        JavaFile daoClass = GenerateDAO.createDaoFromMetadata(businessPurposeOfSQL, selectColumnMetadata, predicateHavingLiterals, sql);

        return writeSelectMicroservice(businessPurposeOfSQL, springBootApplication, databaseConfigContent, 
                                     dtoForMultiTableSQL, controllerClass, daoClass);
    }

    private Integer writeUpdateMicroservice(String businessPurposeOfSQL, JavaFile springBootApplication, 
                                          String databaseConfigContent, JavaFile updateDTO, 
                                          JavaFile updateDAO, JavaFile updateController) throws Exception {

        String directoryNameWhereCodeWillBeGenerated = destinationDirectory;

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

        writeJavaFile(updateDTO, srcPath);
        writeJavaFile(springBootApplication, srcPath);
        writeJavaFile(updateController, srcPath);
        writeJavaFile(updateDAO, srcPath);
        writeDatabaseConfigFile(databaseConfigContent, businessPurposeOfSQL, srcPath);

        writeFileFromResources(directoryNameWhereCodeWillBeGenerated + File.separator + "pom.xml", "pom_file.xml");

        writeFileFromResources(directoryNameWhereCodeWillBeGenerated
                + File.separator + SRC
                + File.separator + MAIN
                + File.separator + RESOURCES
                + File.separator + "application.properties", "application_properties_file.txt");

        log.info("Generated UPDATE spring boot application is available at: {}", directoryNameWhereCodeWillBeGenerated);

        return 0;
    }

    private Integer writeSelectMicroservice(String businessPurposeOfSQL, JavaFile springBootApplication, 
                                          String databaseConfigContent, JavaFile dtoForMultiTableSQL, 
                                          JavaFile controllerClass, JavaFile daoClass) throws Exception {

        String directoryNameWhereCodeWillBeGenerated = destinationDirectory;

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
        writeDatabaseConfigFile(databaseConfigContent, businessPurposeOfSQL, srcPath);

        writeFileFromResources(directoryNameWhereCodeWillBeGenerated + File.separator + "pom.xml", "pom_file.xml");

        writeFileFromResources(directoryNameWhereCodeWillBeGenerated
                + File.separator + SRC
                + File.separator + MAIN
                + File.separator + RESOURCES
                + File.separator + "application.properties", "application_properties_file.txt");

        log.info("Generated SELECT spring boot application is available at: {}", directoryNameWhereCodeWillBeGenerated);

        return 0;
    }

    private void cleanDestinationDirectory() throws Exception {
        Path destPath = Paths.get(destinationDirectory);
        if (!Files.exists(destPath)) {
            return;
        }
        
        log.info("Cleaning generated source directories only, preserving IDE configurations: {}", destinationDirectory);
        
        // Clean specific directories that contain generated code
        cleanDirectoryIfExists(destPath.resolve("src/main/java/com/jfeatures"));
        cleanDirectoryIfExists(destPath.resolve("src/test/java/com/jfeatures"));
        
        // Clean specific files that get regenerated
        deleteFileIfExists(destPath.resolve("pom.xml"));
        deleteFileIfExists(destPath.resolve("src/main/resources/application.properties"));
    }
    
    private void cleanDirectoryIfExists(Path dirPath) throws Exception {
        if (Files.exists(dirPath)) {
            log.info("Cleaning directory: {}", dirPath);
            Files.walk(dirPath)
                 .sorted((a, b) -> b.compareTo(a)) // Reverse order to delete children first
                 .forEach(path -> {
                     try {
                         Files.delete(path);
                     } catch (Exception e) {
                         log.warn("Could not delete path: {} - {}", path, e.getMessage());
                     }
                 });
        }
    }
    
    private void deleteFileIfExists(Path filePath) {
        if (Files.exists(filePath)) {
            try {
                Files.delete(filePath);
                log.info("Deleted file: {}", filePath);
            } catch (Exception e) {
                log.warn("Could not delete file: {} - {}", filePath, e.getMessage());
            }
        }
    }

    private String determineSqlFile() throws Exception {
        if (sqlFileName != null) {
            log.info("Using specified SQL file: {}", sqlFileName);
            return getSql(sqlFileName);
        }

        // Default behavior: try UPDATE first, then fall back to SELECT
        try {
            String sql = getSql("sample_update_parameterized.sql");
            log.info("Using UPDATE SQL file: sample_update_parameterized.sql");
            return sql;
        } catch (Exception e) {
            String sql = getSql("sample_parameterized_sql.sql");
            log.info("Using SELECT SQL file: sample_parameterized_sql.sql");
            return sql;
        }
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

    private static void writeDatabaseConfigFile(String content, String businessPurpose, Path srcPath) throws IOException
    {
        Path configPackagePath = srcPath.resolve("com/jfeatures/" + businessPurpose.toLowerCase() + "/config");
        Files.createDirectories(configPackagePath);
        Path configFilePath = configPackagePath.resolve("DatabaseConfig.java");
        Files.write(configFilePath, content.getBytes(StandardCharsets.UTF_8));
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
