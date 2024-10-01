package perfExecution;

import commonUtils.DeviceTools;
import commonUtils.CommonTools;
import commonUtils.CreateDataFiles;

import commonUtils.ReadPaths;

import commonUtils.Assertion;
import dataFlow.CreateTestSuite;
import org.apache.log4j.Logger;

public class TestExecutor {
    static long startTime, endTime;

    public static final Logger log = Logger.getLogger(TestExecutor.class.getSimpleName());
    public static String KpiType = null, DSN = null;

    /**
     * The main entry point of the PerfTesting application.
     *
     * @param args The command-line arguments passed to the application. The first argument should be the KPI type
     *             and the second argument should be the Device Serial Number (DSN).
     */
    public static void main(String[] args) {
        try {
            startTime = System.currentTimeMillis();

            KpiType = args[0].trim();
            DSN = args[1].trim();

            DeviceTools deviceTools = new DeviceTools();

            log.info("PerfTesting Version - 1.0");
            log.info("Test Parameters - ");
            log.info("KPI TYPE: " + KpiType);
            log.info("DSN: " + DSN);


            if (deviceTools.validateDevice(DSN) && deviceTools.validateTestType(KpiType)) {
                CommonTools commonTools = new CommonTools();
                CreateDataFiles dataFiles = new CreateDataFiles();
                CreateTestSuite testSuite = new CreateTestSuite();


                commonTools.testInitiate();
                dataFiles.createCsvFile(deviceTools.kpi_values_csv);

                String kpi = KpiType;
                log.info("Found KPI Type as: " + kpi + " in config");

                if (ReadPaths.DEVICE_TYPE.equals(ReadPaths.TABLET)) {
                    log.info("Creating & Executing XML Suite for Tablet");
                    testSuite.createXMLFile(KpiType, DSN);
                } else if (ReadPaths.DEVICE_TYPE.equals(ReadPaths.FTV)) {
                    log.info("Creating & Executing XML Suite for FTV");
                    testSuite.createXMLFile(KpiType, DSN);
                }

                endTime = System.currentTimeMillis();

                int totalTime = (int) ((endTime - startTime) / 1000);
                log.info("Total Test Execution Time: " + totalTime + " seconds");
            } else {
                log.error(DeviceTools.APP_FAILURE_REASON);
                Assertion.fail(DeviceTools.APP_FAILURE_REASON);
            }
        } catch (Exception e) {
            log.error("Exception Occurred while executing Test Executor class: ", e);
        }
    }

}