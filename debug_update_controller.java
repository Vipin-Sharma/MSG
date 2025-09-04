import com.jfeatures.msg.codegen.GenerateUpdateController;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.jfeatures.msg.test.TestUtils;
import java.sql.Types;
import java.util.Arrays;

public class debug_update_controller {
    public static void main(String[] args) {
        try {
            // Create test data
            ColumnMetadata nameSetColumn = TestUtils.createColumnMetadata("product_name", "VARCHAR", Types.VARCHAR, false);
            ColumnMetadata statusSetColumn = TestUtils.createColumnMetadata("status", "VARCHAR", Types.VARCHAR, true);
            
            ColumnMetadata idWhereColumn = TestUtils.createColumnMetadata("id", "INTEGER", Types.INTEGER, false);
            ColumnMetadata categoryWhereColumn = TestUtils.createColumnMetadata("category_id", "INTEGER", Types.INTEGER, false);

            UpdateMetadata validUpdateMetadata = new UpdateMetadata(
                "products",
                Arrays.asList(nameSetColumn, statusSetColumn), // SET columns
                Arrays.asList(idWhereColumn, categoryWhereColumn), // WHERE columns
                "UPDATE products SET product_name = ?, status = ? WHERE id = ? AND category_id = ?"
            );

            // Generate controller
            var javaFile = GenerateUpdateController.createUpdateController("Product", validUpdateMetadata);
            
            System.out.println("=== GENERATED UPDATE CONTROLLER ===");
            System.out.println(javaFile.toString());
            System.out.println("=== END ===");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}