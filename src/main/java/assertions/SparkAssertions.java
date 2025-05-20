
package assertions;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.testng.Assert;
import java.util.List;

public class SparkAssertions {
    
    public static void assertDatasetEquals(Dataset<Row> expected, Dataset<Row> actual) {
        Assert.assertEquals(expected.count(), actual.count(), "Dataset row counts do not match");
        Assert.assertTrue(expected.except(actual).isEmpty(), "Datasets have different content");
    }
    
    public static void assertColumnExists(Dataset<Row> dataset, String columnName) {
        List<String> columns = java.util.Arrays.asList(dataset.columns());
        Assert.assertTrue(columns.contains(columnName), 
            String.format("Column '%s' not found in dataset", columnName));
    }
    
    public static void assertRowCount(Dataset<Row> dataset, long expectedCount) {
        Assert.assertEquals(dataset.count(), expectedCount, 
            String.format("Expected %d rows but found %d", expectedCount, dataset.count()));
    }
    
    public static void assertColumnValueEquals(Dataset<Row> dataset, String columnName, Object expectedValue) {
        assertColumnExists(dataset, columnName);
        Assert.assertTrue(dataset.filter(String.format("%s = '%s'", columnName, expectedValue)).count() > 0,
            String.format("Value '%s' not found in column '%s'", expectedValue, columnName));
    }
    
    public static void assertSchemaEquals(Dataset<Row> expected, Dataset<Row> actual) {
        Assert.assertEquals(expected.schema().toString(), actual.schema().toString(),
            "Dataset schemas do not match");
    }
}
