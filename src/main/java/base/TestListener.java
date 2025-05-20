package base;

import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import report.AllureLogger;

/**
 * TestNG listener for test execution events.
 * Provides additional logging and reporting functionality.
 */
public class TestListener implements ITestListener {
    private static final Logger logger = LogManager.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String testClass = result.getMethod().getTestClass().getName();
        logger.info("Test Started: {} ({})", testName, testClass);

        // Add test details to Allure report
        Allure.feature(result.getMethod().getTestClass().getName());
        if (result.getMethod().getDescription() != null) {
            Allure.description(result.getMethod().getDescription());
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        logger.info("Test Passed: {}", testName);
        
        // Calculate and log test execution time
        long executionTimeMs = result.getEndMillis() - result.getStartMillis();
        logger.info("Test execution time: {} ms", executionTimeMs);
        
        // Add success information to Allure report
        AllureLogger.logStep("Test completed successfully", io.qameta.allure.model.Status.PASSED);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        logger.error("Test Failed: {}", testName);
        
        // Log the exception if available
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            logger.error("Test failure reason: ", throwable);
            
            // Add error information to Allure report
            AllureLogger.logError("Test failed: " + testName, throwable);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        logger.info("Test Skipped: {}", testName);
        
        // Log the skip reason if available
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            logger.info("Test skipped reason: {}", throwable.getMessage());
            
            // Add skip information to Allure report
            AllureLogger.logStep("Test skipped: " + testName, io.qameta.allure.model.Status.SKIPPED);
        }
    }

    @Override
    public void onStart(ITestContext context) {
        logger.info("Test Suite Started: {}", context.getName());
        logger.info("Included groups: {}", String.join(", ", context.getIncludedGroups()));
        logger.info("Excluded groups: {}", String.join(", ", context.getExcludedGroups()));
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("Test Suite Finished: {}", context.getName());
        logger.info("Passed tests: {}", context.getPassedTests().size());
        logger.info("Failed tests: {}", context.getFailedTests().size());
        logger.info("Skipped tests: {}", context.getSkippedTests().size());
        
        // Calculate and log total execution time
        long totalTimeMs = context.getEndDate().getTime() - context.getStartDate().getTime();
        logger.info("Total execution time: {} ms", totalTimeMs);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        logger.info("Test Failed Within Success Percentage: {}", testName);
    }
}
