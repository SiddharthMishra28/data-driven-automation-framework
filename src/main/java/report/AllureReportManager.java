package report;

import config.TestConfig;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages Allure reporting configurations and operations.
 * Provides utility methods for working with Allure reports.
 */
public class AllureReportManager {
    private static final Logger logger = LogManager.getLogger(AllureReportManager.class);
    
    private AllureReportManager() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Sets up the environment information for Allure reports.
     * This method should be called at the beginning of the test execution.
     */
    public static void setupAllureEnvironmentInfo() {
        logger.info("Setting up Allure environment information");
        
        // Add environment information to Allure properties file
        try {
            // Create properties file for Allure environment
            java.util.Properties props = new java.util.Properties();
            props.setProperty("Environment", TestConfig.getEnvironment());
            props.setProperty("API Base URL", TestConfig.getApiBaseUrl());
            props.setProperty("Java Version", System.getProperty("java.version"));
            props.setProperty("OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
            props.setProperty("Thread Count", String.valueOf(Runtime.getRuntime().availableProcessors()));
            
            // Ensure the directory exists
            String allureResultsDir = TestConfig.getAllureResultsDir();
            java.io.File directory = new java.io.File(allureResultsDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Write to file
            java.io.FileOutputStream fos = new java.io.FileOutputStream(allureResultsDir + "/environment.properties");
            props.store(fos, "Allure Environment Information");
            fos.close();
        } catch (Exception e) {
            logger.error("Failed to create Allure environment properties", e);
        }
    }
    
    /**
     * Attaches a file to the current Allure test report.
     *
     * @param attachmentName Name of the attachment
     * @param filePath Path to the file to attach
     * @return true if attachment was successful, false otherwise
     */
    public static boolean attachFileToReport(String attachmentName, String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Allure.addAttachment(attachmentName, Files.newInputStream(path));
                logger.debug("Attached file to Allure report: {}", filePath);
                return true;
            } else {
                logger.error("File not found for Allure attachment: {}", filePath);
                return false;
            }
        } catch (IOException e) {
            logger.error("Error attaching file to Allure report: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * Creates the Allure results directory if it doesn't exist.
     * 
     * @return true if the directory exists or was created successfully, false otherwise
     */
    public static boolean ensureAllureResultsDirectory() {
        String allureResultsDir = TestConfig.getAllureResultsDir();
        File directory = new File(allureResultsDir);
        
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                logger.info("Created Allure results directory: {}", allureResultsDir);
                return true;
            } else {
                logger.error("Failed to create Allure results directory: {}", allureResultsDir);
                return false;
            }
        } else {
            logger.debug("Allure results directory already exists: {}", allureResultsDir);
            return true;
        }
    }
    
    /**
     * Adds an HTML attachment to the Allure report.
     *
     * @param name Attachment name
     * @param html HTML content
     */
    public static void attachHtml(String name, String html) {
        Allure.addAttachment(name, "text/html", html, ".html");
        logger.debug("Added HTML attachment to Allure report: {}", name);
    }
    
    /**
     * Adds a text attachment to the Allure report.
     *
     * @param name Attachment name
     * @param text Text content
     */
    public static void attachText(String name, String text) {
        Allure.addAttachment(name, "text/plain", text);
        logger.debug("Added text attachment to Allure report: {}", name);
    }
    
    /**
     * Adds a JSON attachment to the Allure report.
     *
     * @param name Attachment name
     * @param json JSON content
     */
    public static void attachJson(String name, String json) {
        Allure.addAttachment(name, "application/json", json, ".json");
        logger.debug("Added JSON attachment to Allure report: {}", name);
    }
    
    /**
     * Adds an XML attachment to the Allure report.
     *
     * @param name Attachment name
     * @param xml XML content
     */
    public static void attachXml(String name, String xml) {
        Allure.addAttachment(name, "application/xml", xml, ".xml");
        logger.debug("Added XML attachment to Allure report: {}", name);
    }
}
