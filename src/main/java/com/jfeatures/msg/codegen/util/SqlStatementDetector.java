package com.jfeatures.msg.codegen.util;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

/**
 * Utility class to detect the type of SQL statement.
 */
public class SqlStatementDetector {
    
    /**
     * Determines the type of SQL statement from the given SQL string.
     * 
     * @param sql The SQL statement to analyze
     * @return The type of SQL statement
     * @throws JSQLParserException if the SQL cannot be parsed
     */
    public static SqlStatementType detectStatementType(String sql) throws JSQLParserException {
        if (sql == null || sql.trim().isEmpty()) {
            return SqlStatementType.UNKNOWN;
        }
        
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            
            if (statement instanceof Select) {
                return SqlStatementType.SELECT;
            } else if (statement instanceof Update) {
                return SqlStatementType.UPDATE;
            } else if (statement instanceof Insert) {
                return SqlStatementType.INSERT;
            } else if (statement instanceof Delete) {
                return SqlStatementType.DELETE;
            } else {
                return SqlStatementType.UNKNOWN;
            }
        } catch (JSQLParserException e) {
            // If parsing fails, try to detect based on first keyword
            String trimmedSql = sql.trim().toUpperCase();
            if (trimmedSql.startsWith("SELECT")) {
                return SqlStatementType.SELECT;
            } else if (trimmedSql.startsWith("UPDATE")) {
                return SqlStatementType.UPDATE;
            } else if (trimmedSql.startsWith("INSERT")) {
                return SqlStatementType.INSERT;
            } else if (trimmedSql.startsWith("DELETE")) {
                return SqlStatementType.DELETE;
            } else {
                return SqlStatementType.UNKNOWN;
            }
        }
    }
}