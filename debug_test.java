import com.jfeatures.msg.codegen.GenerateUpdateController;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.jfeatures.msg.test.TestUtils;
import com.squareup.javapoet.JavaFile;
import java.sql.Types;
import java.util.Arrays;

public class debug_test {
    public static void main(String[] args) throws Exception {
        // Test the exact same scenario as the failing test
        ColumnMetadata nameSetColumn = TestUtils.createColumnMetadata("product_name", "VARCHAR", Types.VARCHAR, false);
        ColumnMetadata idWhereColumn = TestUtils.createColumnMetadata("id", "INTEGER", Types.INTEGER, false);
        
        UpdateMetadata singleWhereMetadata = new UpdateMetadata(
                "users",
                Arrays.asList(nameSetColumn),
                Arrays.asList(idWhereColumn), // Single WHERE column
                "UPDATE users SET product_name = ? WHERE id = ?"
        );

        JavaFile javaFile = GenerateUpdateController.createUpdateController("User", singleWhereMetadata);
        String code = javaFile.toString();
        
        System.out.println("=== EXACT TEST CASE DEBUG ===");
        System.out.println(code);
        System.out.println("=== ASSERTIONS CHECK ===");
        
        // Check the failing assertions
        boolean pathVariableStringId = code.contains("@PathVariable") && code.contains("String id");
        boolean userIdPath = code.contains("value = \"/user/{id}\"") || code.contains("/{id}");
        boolean requestBody = code.contains("@Valid @RequestBody UserUpdateDTO updateDto") || code.contains("@RequestBody");
        boolean parameter = code.contains("@Parameter") && code.contains("description");
        
        System.out.println("pathVariableStringId: " + pathVariableStringId);
        System.out.println("userIdPath: " + userIdPath);
        System.out.println("requestBody: " + requestBody);
        System.out.println("parameter: " + parameter);
    }
}