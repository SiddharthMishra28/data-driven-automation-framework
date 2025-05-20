
package utils;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class SparkClient {
    private static final Logger logger = LogManager.getLogger(SparkClient.class);
    private final SparkSession spark;

    private ADLSStorageUtils adlsUtils;

    public SparkClient() {
        spark = SparkSession.builder()
                .appName("Data Validation Framework")
                .master("local[*]")
                .config("spark.sql.legacy.allowUntypedScalaUDF", "true")
                .getOrCreate();
        logger.info("Spark session initialized");
    }

    public void configureADLS(String storageAccountName, String accessKey) {
        this.adlsUtils = new ADLSStorageUtils(spark, storageAccountName, accessKey);
        logger.info("ADLS configuration initialized for storage account: {}", storageAccountName);
    }

    public Dataset<Row> readFromADLS(String container, String path, String format) {
        if (adlsUtils == null) {
            throw new IllegalStateException("ADLS not configured. Call configureADLS first.");
        }
        return adlsUtils.readFromADLS(container, path, format);
    }

    public void writeToADLS(Dataset<Row> df, String container, String path, String format) {
        if (adlsUtils == null) {
            throw new IllegalStateException("ADLS not configured. Call configureADLS first.");
        }
        adlsUtils.writeToADLS(df, container, path, format);
    }

    public Dataset<Row> readFile(String path) {
        logger.info("Reading file: {}", path);
        String fileExtension = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
        
        switch (fileExtension) {
            case "csv":
                return readCsv(path);
            case "json":
                return readJson(path);
            case "jsonld":
                return readJsonLD(path);
            case "parquet":
                return readParquet(path);
            case "avro":
                return readAvro(path);
            case "orc":
                return readOrc(path);
            case "txt":
                return readText(path);
            case "xml":
                return readXml(path);
            default:
                throw new IllegalArgumentException("Unsupported file format: " + fileExtension);
        }
    }

    public Dataset<Row> readCsv(String path) {
        return spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .option("multiline", "true")
                .option("escape", "\"")
                .csv(path);
    }

    public Dataset<Row> readJson(String path) {
        return spark.read()
                .option("multiline", "true")
                .option("primitivesAsString", "true")
                .json(path);
    }

    public Dataset<Row> readJsonLD(String path) {
        return spark.read()
                .option("multiline", "true")
                .option("primitivesAsString", "true")
                .json(path);
    }

    public Dataset<Row> readParquet(String path) {
        return spark.read().parquet(path);
    }

    public Dataset<Row> readAvro(String path) {
        return spark.read().format("avro").load(path);
    }

    public Dataset<Row> readOrc(String path) {
        return spark.read().orc(path);
    }

    public Dataset<Row> readText(String path) {
        return spark.read().text(path);
    }

    public Dataset<Row> readXml(String path) {
        return spark.read()
                .format("com.databricks.spark.xml")
                .option("rowTag", "root")
                .load(path);
    }

    public void writeFile(Dataset<Row> df, String path, String format) {
        logger.info("Writing data to file: {}", path);
        df.write()
                .mode("overwrite")
                .format(format)
                .save(path);
    }

    public boolean compareDataframes(Dataset<Row> df1, Dataset<Row> df2) {
        logger.info("Comparing dataframes");
        return df1.exceptAll(df2).count() == 0 && df2.exceptAll(df1).count() == 0;
    }

    public Dataset<Row> convertSqlResultToDataframe(List<Map<String, Object>> sqlResults) {
        logger.info("Converting SQL results to Spark dataframe");
        return spark.createDataFrame(sqlResults, Row.class);
    }

    public void close() {
        if (spark != null) {
            logger.info("Closing Spark session");
            spark.close();
        }
    }
}
