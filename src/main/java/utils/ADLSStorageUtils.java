
package utils;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ADLSStorageUtils {
    private static final Logger logger = LogManager.getLogger(ADLSStorageUtils.class);
    private final SparkSession spark;
    private final String storageAccountName;
    private final String accessKey;

    public ADLSStorageUtils(SparkSession spark, String storageAccountName, String accessKey) {
        this.spark = spark;
        this.storageAccountName = storageAccountName;
        this.accessKey = accessKey;
        configureADLS();
    }

    private void configureADLS() {
        spark.conf().set(
            String.format("fs.azure.account.key.%s.dfs.core.windows.net", storageAccountName),
            accessKey
        );
    }

    public Dataset<Row> readFromADLS(String container, String path, String format) {
        String fullPath = String.format("abfss://%s@%s.dfs.core.windows.net/%s",
            container, storageAccountName, path);
        
        logger.info("Reading {} file from ADLS: {}", format, fullPath);
        
        switch (format.toLowerCase()) {
            case "csv":
                return readCsvFromADLS(fullPath);
            case "json":
                return readJsonFromADLS(fullPath);
            case "jsonld":
                return readJsonLDFromADLS(fullPath);
            case "parquet":
                return readParquetFromADLS(fullPath);
            case "text":
                return readTextFromADLS(fullPath);
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    private Dataset<Row> readCsvFromADLS(String path) {
        return spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .option("multiline", "true")
                .csv(path);
    }

    private Dataset<Row> readJsonFromADLS(String path) {
        return spark.read()
                .option("multiline", "true")
                .option("primitivesAsString", "true")
                .json(path);
    }

    private Dataset<Row> readJsonLDFromADLS(String path) {
        return spark.read()
                .option("multiline", "true")
                .option("primitivesAsString", "true")
                .json(path);
    }

    private Dataset<Row> readParquetFromADLS(String path) {
        return spark.read().parquet(path);
    }

    private Dataset<Row> readTextFromADLS(String path) {
        return spark.read().text(path);
    }

    public void writeToADLS(Dataset<Row> df, String container, String path, String format) {
        String fullPath = String.format("abfss://%s@%s.dfs.core.windows.net/%s",
            container, storageAccountName, path);
            
        logger.info("Writing {} file to ADLS: {}", format, fullPath);
        
        df.write()
            .mode("overwrite")
            .format(format)
            .save(fullPath);
    }
}
