package testReporter;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Protocol;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.opencsv.CSVReader;
import commonUtils.DeviceTools;
import commonUtils.ReadPaths;
import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExtentReporter extends TestListenerAdapter {

    private final Logger log = Logger.getLogger(ExtentReporter.class.getSimpleName());
    private static ExtentReports extent;
    private static ExtentTest logger;
    ReadPaths paths = new ReadPaths();
    DeviceTools setup = new DeviceTools();
    String DSN = DeviceTools.DSN;
    String kpiType = DeviceTools.KPI_TYPE;
    private final String timeStamp = new SimpleDateFormat("MMdd", Locale.ROOT).format(new Date());


    /**
     * Method invoked when a test suite starts execution.
     * Initializes and configures the ExtentReports instance to generate an HTML report.
     *
     * @param testContext The context for the test suite.
     */
    @Override
    public void onStart(ITestContext testContext) {

        String repName = "Performance_Test_Report_" + timeStamp + ".html";
        String fileName = setup.reportPath + repName;
        ExtentSparkReporter htmlReporter = new ExtentSparkReporter(fileName);
        try {

            try {
                htmlReporter.loadXMLConfig(setup.extentReportXml);
            } catch (IOException e) {
                log.error("Exception occurred while setting Extent XML Config", e);
            }

            htmlReporter.config().setEncoding("UTF-8");
            htmlReporter.config().setProtocol(Protocol.HTTPS);
            htmlReporter.config().setDocumentTitle("Performance Test Report");
            htmlReporter.config().setReportName("Performance Test: " + kpiType);
            htmlReporter.config().setTheme(Theme.DARK);
            htmlReporter.config().setTimelineEnabled(false);
            extent = new ExtentReports();
            extent.attachReporter(htmlReporter);
            extent.setSystemInfo("Test Executed", kpiType);
            extent.setSystemInfo("Device Name", setup.getDeviceName(DSN).toUpperCase());
            extent.setSystemInfo("Device DSN", DSN);
            extent.setSystemInfo("Device OS Version", setup.getFireOSBuild(DSN));
        } catch (IOException e) {
            log.error("Exception occurred while creating Extent XML", e);
        }

    }

    /**
     * Method invoked when a test suite finishes execution.
     * Flushes the ExtentReports instance to ensure all information is written to the report.
     *
     * @param testContext The context for the test suite.
     */
    @Override
    public void onFinish(ITestContext testContext) {
        try {
            extent.flush();
        } catch (Exception e) {
            log.error("Exception occurred while finishing Report", e);
        }
    }

    /**
     * Invoked when a test method succeeds.
     * Creates an extent test based on the test result, logs the test status as PASS in the report with a green label,
     * and records the execution time of the test.
     *
     * @param tr The result of the test method.
     */
    @Override
    public void onTestSuccess(ITestResult tr) {
        createExtentTest(tr);
        String status = Status.PASS.toString();
        logger.log(Status.PASS, MarkupHelper.createLabel(status, ExtentColor.GREEN));
        getExecutionTime(tr);
    }

    /**
     * Invoked when a test method fails.
     * Creates an extent test based on the test result, logs the test status as FAIL in the report with a red label,
     * records the execution time of the test, and captures the reporter message.
     *
     * @param tr The result of the test method.
     */
    @Override
    public void onTestFailure(ITestResult tr) {
        createExtentTest(tr);
        String status = Status.FAIL.toString();
        logger.log(Status.FAIL, MarkupHelper.createLabel(status, ExtentColor.RED));
        getExecutionTime(tr);
        getReporterMsg(tr);
    }


    /**
     * Invoked when a test method is skipped.
     * Creates an extent test based on the test result, logs the test status as SKIP in the report with an orange label,
     * records the execution time of the test, and captures the reporter message.
     *
     * @param tr The result of the test method.
     */
    @Override
    public void onTestSkipped(ITestResult tr) {
        createExtentTest(tr);
        String status = Status.SKIP.toString();
        logger.log(Status.SKIP, MarkupHelper.createLabel(status, ExtentColor.ORANGE));
        getExecutionTime(tr);
        getReporterMsg(tr);
    }

    /**
     * Invoked when a test method fails but is within the success percentage.
     * Creates an extent test based on the test result, logs the test status as FAIL_WITHIN_SUCCESS_PERCENTAGE in the report with a purple label,
     * and records the execution time of the test.
     *
     * @param tr The result of the test method.
     */
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult tr) {
        createExtentTest(tr);
        String status = Status.i("FAIL_WITHIN_SUCCESS_PERCENTAGE");
        logger.log(Status.valueOf(Status.i("FAIL_WITHIN_SUCCESS_PERCENTAGE")), MarkupHelper.createLabel(status, ExtentColor.PURPLE));
        getExecutionTime(tr);
    }


    /**
     * Retrieves the details of the application and the test from the test result.
     * Returns a formatted string containing the application package name, version, test name, and device name.
     *
     * @param tr The test result.
     * @return A string containing the application details.
     */
    private String getAppDetails(ITestResult tr) {
        String kpiDetails = "[" + tr.getName() + "]";
        return DeviceTools.APP_PACKAGE_INPUT + " [" + DeviceTools.APP_VERSION + "] " + kpiDetails
                + " [" + setup.getDeviceName(DeviceTools.DSN) + "]";
    }

    /**
     * Logs the key performance indicator (KPI) details to the logger.
     *
     * @param outputCSV An array containing the KPI details.
     */
    private void getKpiDetails(String[] outputCSV) {
        logger.info("App Package: <b>" + outputCSV[1] + "</b>");
        logger.info("Test Device: <b>" + outputCSV[10] + "</b>");
        logger.info("App Package: <b>" + outputCSV[1] + "</b>");
        logger.info("App Version: <b>" + outputCSV[2] + "</b>");
        logger.info("KPI Tested: <b>" + outputCSV[3] + "</b>");
        logger.info("Latency Metrics Name: <b>" + outputCSV[4] + "</b>");
        logger.info("Latency_Values: <b>" + outputCSV[5] + "</b>");
        logger.info("Average: <b>" + outputCSV[6] + " ms </b> | " + "TP 50 Value: <b>" + outputCSV[7] + " ms" + "</b>");
    }


    /**
     * Retrieves the reporter messages associated with a test result.
     *
     * @param tr The test result object.
     * @return The concatenated reporter messages as a string.
     */
    private String getReporterMsg(ITestResult tr) {
        String reporterMessage = "";
        List<String> reporterMessageList = Reporter.getOutput(tr);
        for (String tmpMsg : reporterMessageList) {
            reporterMessage += tmpMsg;
            reporterMessage += " ";
        }
        return reporterMessage;
    }


    /**
     * Calculates and logs the execution time of a test result.
     *
     * @param tr The test result object.
     */
    private void getExecutionTime(ITestResult tr) {
        long startTime = tr.getStartMillis();
        long endTime = tr.getEndMillis();
        long execTime = endTime - startTime;

        long secs = execTime / 1000;
        long minutes = secs / 60;

        secs = secs % 60;
        String minStr = minutes + "";
        String secStr = secs + "";
        if (minutes < 10)
            minStr = "0" + minutes;
        if (secs < 10)
            secStr = "0" + secs;
        logger.info("KPI Execution Time: <b>" + minStr + ":" + secStr + "</b>");
    }

    /**
     * Creates an Extent test based on the provided test result.
     *
     * @param tr The test result object.
     */
    private void createExtentTest(ITestResult tr) {
        try {
            String testName = tr.getName();

            switch (testName) {
                case "Cool_Latency_FirstFrame":
                    testName = setup.COOL_APP_FF;
                    break;
                case "Warm_Latency_FirstFrame":
                    testName = setup.WARM_APP_FF;
                    break;
            }

            File kpiFile = new File(paths.kpi_values_csv);
            CSVReader csvReader = new CSVReader(new FileReader(kpiFile));
            String[] outputCSV = csvReader.readNext();

            while ((outputCSV = csvReader.readNext()) != null) {
                if (outputCSV[4].contains(testName)) {
                    logger = extent.createTest(getAppDetails(tr));
                    getKpiDetails(outputCSV);
                    break;
                }
            }
            csvReader.close();
        } catch (Exception e) {
            log.error("Exception occurred while creating Extent Test", e);
        }
    }
}