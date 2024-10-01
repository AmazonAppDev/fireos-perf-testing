package perfTVKpis.LatencyTestTV;

import commonUtils.CommonTools;
import commonUtils.Assertion;
import dataFlow.TestKpiDataWrite;
import org.apache.log4j.Logger;

import java.util.Objects;

public class LatencyKPI extends CommonTools {

    Logger log = Logger.getLogger(LatencyKPI.class.getSimpleName());
    LatencyRead latencyRead = new LatencyRead();
    TestKpiDataWrite csvWrite = new TestKpiDataWrite();

    /**
     * Runs a latency test for the specified device and latency type.
     *
     * @param DSN         The Device Serial Number (DSN) identifying the target device.
     * @param latencyType The type of latency test to run (e.g., "Cool" or "Warm" launch).
     */
    public void runLatencyTest(String DSN, String latencyType) {
        log.info("-----------Starting Test------------");
        try {
            appPackage = getAppPackage(DSN);
            APP_FAILURE_REASON = "NA";
            if (!Objects.equals(appPackage, APP_PACKAGE_INPUT) || APP_PACKAGE_INPUT == null) {
                log.error(DEVICE_NAME + " : App Package not found in device");
                if (Objects.equals(APP_FAILURE_REASON, "NA")) APP_FAILURE_REASON = "APP Failed to Install";
                csvWrite.writeToCSV(DSN, "NA", latencyType,
                        "Skip",
                        "Skip",
                        "Skip", 0,
                        0, 0, 0);
                Assertion.fail(APP_FAILURE_REASON);
            }

            APP_VERSION = getAppVersionFromDevice(DSN, appPackage);
            appIntent = getAppIntent(DSN, appPackage);
            String minSDK = getAppMinSDK(DSN, appPackage);
            String targetSDK = getAppTargetSDK(DSN, appPackage);

            log.info(DEVICE_NAME + ": App Version: " + APP_VERSION);
            log.info(DEVICE_NAME + ": Latency Type: " + latencyType.toUpperCase());
            log.info(DEVICE_NAME + ": App Package Name: " + appPackage);
            log.info(DEVICE_NAME + ": App Intent: " + appIntent);
            log.info(DEVICE_NAME + ": App Min SDK: " + minSDK);
            log.info(DEVICE_NAME + ": App Target SDK: " + targetSDK);
            log.info("Running Test on: " + DEVICE_NAME);

            double tp50Latency = latencyRead.captureLatency(DSN, latencyType, appPackage, appIntent);
            log.info("TP50 Value: " + tp50Latency);

            if (tp50Latency > 0) Assertion.pass();
            else Assertion.fail(APP_FAILURE_REASON);

        } catch (Exception e) {
            if (Objects.equals(APP_FAILURE_REASON, "NA"))
                APP_FAILURE_REASON = "There is an exception in latency KPI Method";
            log.error(DEVICE_NAME + ": " + APP_FAILURE_REASON + ": ", e);
            csvWrite.writeToCSV(DSN, "NA", latencyType,
                    "Skip",
                    "Skip",
                    "Skip", 0,
                    0, 0, 0);
            Assertion.fail(APP_FAILURE_REASON);
        }
    }


}
