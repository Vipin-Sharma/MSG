package com.jfeatures.msg.sql;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ModifySQLTest
{
  private static final String sql1 = """
                    Select tableC.a, tableC.b, tableD.c, tableD.d, e
                    from tableC as tableC
                        join tableD as tableD on tableC.a = tableD.c and tableC.a = 1 and tableC.b = 'b'
                        join tableE as tableE on tableC.a = tableE.e
            """;

  @Test
  void modifySQLToUseNamedParameter() throws Exception {
    String sql = ReadFileFromResources.readFileFromResources("sample_sql.sql");
    String modifiedSql = ModifySQL.modifySQLToUseNamedParameter(sql);
    System.out.println(modifiedSql);
  }

  @Test
  void testModifySQLToUseNamedParameter() throws JSQLParserException {
    String sql = """
                Select tableC.a, tableC.b, tableD.c, tableD.d, e ,  tableF.a, tableF.b, tableF.c, tableF.d
                from tableC as tableC, tableD as tableD, tableE, tableF as tableF
                where tableC.a = tableD.c and tableC.b = tableD.d and tableC.a = tableE.e and tableC.b = tableE.e
                and tableC.a = 1 and tableC.b = 'HARDCODE_AS_STRING{Vipin}' and e = 100
                """;
    String modifiedSQL = ModifySQL.modifySQLToUseNamedParameter(sql);
    Assertions.assertTrue(modifiedSQL.contains("tableC.a = :a"));
    Assertions.assertTrue(modifiedSQL.contains("tableC.b = 'Vipin'"));
    Assertions.assertTrue(modifiedSQL.contains("e = :e"));
  }

  @Test
  void testModifySQLToUseNamedParameterWhenPassedLiteralWithHardcodeTextShouldBeHardCodedInSQL() throws JSQLParserException {
    String sql = """
                Select tableC.a, tableC.b, tableD.c, tableD.d, e ,  tableF.a, tableF.b, tableF.c, tableF.d
                from tableC as tableC, tableD as tableD, tableE, tableF as tableF
                where tableC.a = tableD.c and tableC.b = tableD.d and tableC.a = tableE.e and tableC.b = tableE.e
                and tableC.a = 1 and tableC.b = 'Vipin' and e = 'HARDCODE_AS_{100}'
                """;
    String modifiedSQL = ModifySQL.modifySQLToUseNamedParameter(sql);
    Assertions.assertTrue(modifiedSQL.contains("tableC.a = :a"));
    Assertions.assertTrue(modifiedSQL.contains("tableC.b = :b"));
    Assertions.assertTrue(modifiedSQL.contains("e = 100"));
  }

  @ParameterizedTest(name = "testExtractPredicateHavingLiteralsFromWhereClause: {0}")
  @ValueSource(strings = sql1)
  void testModifySQLHavingJoinClauseWithLiteralsToUseNamedParameter(String sql) throws JSQLParserException {
    String modifiedSQL = ModifySQL.modifySQLToUseNamedParameter(sql);
    System.out.println(modifiedSQL);
    Assertions.assertTrue(modifiedSQL.contains("tableC.a = :a"));
    Assertions.assertTrue(modifiedSQL.contains("tableC.b = :b"));
  }
}
