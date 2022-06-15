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

        String sql = getSql();
        Map<String, String> ddlPerTableName = getDdlPerTable();

        String businessPurposeOfSQL = "BusinessData";

        JavaFile dtoForMultiTableSQL = GenerateDTO.getDTOForMultiTableSQL(sql, ddlPerTableName, businessPurposeOfSQL);
        JavaFile springBootMainClass = GenerateSpringBootApp.createSpringBootApp(businessPurposeOfSQL);

        List<DBColumn> predicateHavingLiterals = MsgSqlParser.extractPredicateHavingLiterals(sql, ddlPerTableName);

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
                        SELECT

                        p.FirstName , p.MiddleName, p.LastName, cus.CustomerID, pmpd.ProductDescriptionID, pdes.Description, product.Name, sod.SalesOrderDetailID, sod.ProductID
                                FROM

                        AdventureWorks2019.Sales.SalesOrderDetail as sod, AdventureWorks2019.Sales.SalesOrderHeader as soh,
                        AdventureWorks2019.Production.Product as product, AdventureWorks2019.Sales.Customer as cus,
                        AdventureWorks2019.Production.ProductModel as pm, AdventureWorks2019.Production.ProductModelProductDescriptionCulture as pmpd,
                        AdventureWorks2019.Production.ProductDescription as pdes,
                                AdventureWorks2019.Sales.PersonCreditCard pcc, AdventureWorks2019.Person.Person p

                                where

                        sod.SalesOrderID = soh.SalesOrderID

                        and

                        sod.ProductID = product.ProductID

                        and soh.CustomerID = cus.CustomerID

                        and  sod.ProductID = product.ProductID

                        and product.ProductModelID = pm.ProductModelID

                        and pmpd.ProductModelID = product.ProductModelID

                        and pdes.ProductDescriptionID = pmpd.ProductDescriptionID

                        and soh.CreditCardID = pcc.CreditCardID

                        and pcc.BusinessEntityID = p.BusinessEntityID

                        and cus.CustomerID = 29825
                        and pmpd.CultureID = 'en'

                        ;
                        """
                ;
    }

    private static Map<String, String> getDdlPerTable() throws IOException, URISyntaxException {
        return ReadDDL.readDDLsFromFile("/Adwentureworks_ddls_for_test.txt");
    }
}
