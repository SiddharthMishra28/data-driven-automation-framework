
package tests;

import base.BaseTest;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.testng.annotations.Test;
import utils.SparkClient;
import utils.SparkTransformations;
import assertions.SparkAssertions;

public class SparkDataValidationTest extends BaseTest {
    private SparkClient sparkClient = new SparkClient();
    
    @Test(description = "Validate data transformation and comparison")
    public void testDataTransformation() {
        // Load source data
        Dataset<Row> sourceData = sparkClient.readCsv("src/test/resources/testdata/source.csv");
        Dataset<Row> targetData = sparkClient.readCsv("src/test/resources/testdata/target.csv");
        
        // Apply transformations
        Dataset<Row> transformedData = SparkTransformations.cleanseData(sourceData);
        Dataset<Row> aggregatedData = SparkTransformations.aggregateByColumn(
            transformedData, "category", "amount", "sum");
            
        // Assert results
        SparkAssertions.assertSchemaEquals(targetData, aggregatedData);
        SparkAssertions.assertRowCount(aggregatedData, 5);
        SparkAssertions.assertColumnExists(aggregatedData, "category");
        SparkAssertions.assertColumnValueEquals(aggregatedData, "total_amount", 1000.0);
    }
}
