package perfTVKpis.LatencyTestTV;

import commonUtils.CommonTools;
import org.apache.log4j.Logger;

import java.io.File;

public class LatencyRead extends CommonTools {
    Logger log = Logger.getLogger(LatencyRead.class.getSimpleName());

    /**
     * Captures latency for the specified type (cool or warm launch) on the given device.
     *
     * @param DSN         The Device Serial Number (DSN) identifying the target device.
     * @param latencyType The type of latency to capture ("Cool" or "Warm" launch).
     * @param appPackage  The package name of the app for which latency is being captured.
     * @param appIntent   The intent used to launch the app.
     * @return The tp50 latency value captured during the test.
     */
    public double captureLatency(String DSN, String latencyType, String appPackage, String appIntent) {
        try {
            if (latencyType.equalsIgnoreCase(COOL_APP_FF)) {
                executeCoolLaunch(DSN, appPackage, DEVICE_NAME, COOL_APP_FF, appIntent);
            } else if (latencyType.equalsIgnoreCase(WARM_APP_FF)) {
                executeWarmLaunch(DSN, appPackage, DEVICE_NAME, WARM_APP_FF, appIntent);
            }
            writeLatencyValues(APP_VERSION, DSN, latencyType);
            log.info(DEVICE_NAME + ": Latency Test Execution Completed");
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": There was an exception in Latency kpi", e);
        }
        return tp50Value;
    }

    /**
     * Executes the cool launch performance measurement for the specified app on the given device.
     *
     * @param DSN         The Device Serial Number (DSN) identifying the target device.
     * @param appPackage  The package name of the app for which the cool launch is being measured.
     * @param deviceName  The name of the device where the measurement is being executed.
     * @param metricsName The name of the performance metric being captured (e.g., "Cool App FF").
     * @param appIntent   The intent used to launch the app.
     */
    public void executeCoolLaunch(String DSN, String appPackage, String deviceName,
                                  String metricsName, String appIntent) {
        if (executeFirstColdLaunch(DSN, metricsName, appPackage, appIntent)) {
            log.info("------------------------ Cool KPI Measurement -----------------------");
            log.info(deviceName + ": Starting capture for " + iterations + " iterations and wait of " + waitTime
                    + "s/iteration");
            for (int i = 0; i < iterations; i++) {
                try {
                    File adbLogFile = new File(adbLogs + appPackage + "_" + DEVICE_NAME + "_" + "Loop_" + (i + 1)
                            + "_" + metricsName + "_ADB.txt");
                    launchApp(appPackage, appIntent);
                    waitInSec(waitTime);
                    timerArr[i] = performanceCoolLaunchLogCapture(appPackage, DSN);

                    cpuConsumption = getCpuConsumption(DSN).split("%")[0];
                    memConsumption = getApp_Memory_Usage(DSN, appPackage).trim();
                    memUsage[i] = Double.parseDouble(memConsumption);
                    cpuUsage[i] = Double.parseDouble(cpuConsumption);

                    forceStopApp(DSN, appPackage);
                    waitInSec(waitTime);

                    getApp_ADBLogsFile(adbLogFile, DSN);
                    displayedArr[i] = getFFDisplayedValue(DSN, appPackage, adbLogFile);

                    clearLogcatBuffer(DSN);
                    clearLogcatVitals(DSN);
                } catch (Exception e) {
                    APP_FAILURE_REASON = "Error Occurred while Executing Cool Launch";
                    log.error(APP_FAILURE_REASON + ": ", e);
                }
            }
        } else {
            APP_FAILURE_REASON = "Error Occurred while performing pre launch test";
            log.error(APP_FAILURE_REASON);
        }
    }

    /**
     * Executes the warm launch performance measurement for the specified app on the given device.
     *
     * @param DSN         The Device Serial Number (DSN) identifying the target device.
     * @param appPackage  The package name of the app for which the warm launch is being measured.
     * @param deviceName  The name of the device where the measurement is being executed.
     * @param metricsName The name of the performance metric being captured (e.g., "Warm App FF").
     * @param appIntent   The intent used to launch the app.
     */
    public void executeWarmLaunch(String DSN, String appPackage, String deviceName,
                                  String metricsName, String appIntent) {
        emptyExistingArray();
        if (executeFirstColdLaunch(DSN, metricsName, appPackage, appIntent)) {
            log.info("------------------------ Warm KPI Measurement -----------------------");
            log.info(deviceName + ": Starting capture for " + iterations + " iterations and wait of " +
                    waitTime + "s/iteration");
            for (int i = 0; i < iterations; i++) {
                try {
                    File adbLogFile = new File(adbLogs + appPackage + "_" + DEVICE_NAME + "_"
                            + "Loop_" + (i + 1) + "_" + metricsName + "_ADB.txt");
                    log.info("Loop: " + (i + 1));
                    launchApp(appPackage, appIntent);
                    waitInSec(waitTime);
                    timerArr[i] = performanceWarmLaunchLogCapture(appPackage, DSN);

                    cpuConsumption = getCpuConsumption(DSN).split("%")[0];
                    memConsumption = getApp_Memory_Usage(DSN, appPackage).trim();
                    memUsage[i] = Double.parseDouble(memConsumption);
                    cpuUsage[i] = Double.parseDouble(cpuConsumption);

                    getApp_ADBLogsFile(adbLogFile, DSN);
                    displayedArr[i] = getFFDisplayedValue(DSN, appPackage, adbLogFile);
                    goHome(DSN);

                    clearLogcatVitals(DSN);
                    clearLogcatBuffer(DSN);
                    waitInSec(waitTime);
                } catch (Exception e) {
                    APP_FAILURE_REASON = "Exception Occurred While Executing Warm Launch";
                    log.error(APP_FAILURE_REASON + ": " + e);
                }

            }
            forceStopApp(DSN, appPackage);
        } else {
            APP_FAILURE_REASON = "Error Occurred while performing pre launch test";
            log.error(APP_FAILURE_REASON);
        }
    }


}