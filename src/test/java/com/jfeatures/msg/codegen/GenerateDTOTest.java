package com.jfeatures.msg.codegen;

import com.squareup.javapoet.JavaFile;
import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenerateDTOTest {


    private static final String ddl = "CREATE TABLE Inventory (id INT, name NVARCHAR(50), quantity INT)";
    private static final String sql = "Select id, name, quantity from Inventory";


    @Test
    public void testGenerateDTO() throws JSQLParserException, IOException {
        String businessPurposeOfSQL = "InventoryData";
        JavaFile inventoryData = GenerateDTO.getDTO(sql, ddl, businessPurposeOfSQL);

        assertEquals(businessPurposeOfSQL+"DTO", inventoryData.typeSpec.name);
    }

}
