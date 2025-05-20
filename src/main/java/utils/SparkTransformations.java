
package utils;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import static org.apache.spark.sql.functions.*;

public class SparkTransformations {
    
    public static Dataset<Row> pivotData(Dataset<Row> dataset, String pivotColumn, String[] values) {
        return dataset.groupBy("id")
                     .pivot(pivotColumn, java.util.Arrays.asList(values))
                     .agg(functions.first("value"));
    }
    
    public static Dataset<Row> aggregateByColumn(Dataset<Row> dataset, String groupByColumn, 
                                               String aggregateColumn, String operation) {
        switch (operation.toLowerCase()) {
            case "sum":
                return dataset.groupBy(groupByColumn).agg(sum(aggregateColumn));
            case "avg":
                return dataset.groupBy(groupByColumn).agg(avg(aggregateColumn));
            case "count":
                return dataset.groupBy(groupByColumn).agg(count(aggregateColumn));
            default:
                throw new IllegalArgumentException("Unsupported aggregation operation: " + operation);
        }
    }
    
    public static Dataset<Row> cleanseData(Dataset<Row> dataset) {
        return dataset.na().drop()
                     .dropDuplicates();
    }
}
