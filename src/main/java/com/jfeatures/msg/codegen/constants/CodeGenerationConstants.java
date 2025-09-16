package com.jfeatures.msg.codegen.constants;

public final class CodeGenerationConstants {
    
    // Private constructor to prevent instantiation of utility class
    private CodeGenerationConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // Field names used in generated code
    public static final String JDBC_TEMPLATE_FIELD_NAME = "namedParameterJdbcTemplate";
    public static final String SQL_FIELD_NAME = "SQL";
    
    // Builder pattern threshold - when to use builder vs setter pattern
    public static final int BUILDER_PATTERN_FIELD_THRESHOLD = 255;
    
    // Code generation patterns
    public static final String BUILDER_VARIABLE_NAME = "Builder";
    public static final String DTO_VARIABLE_NAME = "dto";
    public static final String RESULT_LIST_NAME = "result";

    // Method prefixes
    public static final String DAO_METHOD_PREFIX = "get";
    public static final String SETTER_METHOD_PREFIX = "set";

    // Commonly reused JavaPoet placeholders and annotation members
    public static final String STRING_PLACEHOLDER = "$S";
    public static final String LITERAL_PLACEHOLDER = "$L";
    public static final String ANNOTATION_MEMBER_VALUE = "value";
    public static final String ANNOTATION_MEMBER_PATH = "path";
    public static final String ANNOTATION_MEMBER_DESCRIPTION = "description";
    public static final String ANNOTATION_MEMBER_REQUIRED = "required";
    public static final String ANNOTATION_MEMBER_PRODUCES = "produces";
    public static final String ANNOTATION_MEMBER_CONSUMES = "consumes";
    public static final String ANNOTATION_MEMBER_SUMMARY = "summary";
}
