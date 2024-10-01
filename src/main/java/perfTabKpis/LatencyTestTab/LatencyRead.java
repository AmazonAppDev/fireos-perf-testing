package perfTabKpis.LatencyTestTab;

import commonUtils.CommonTools;
import org.apache.log4j.Logger;

import java.io.File;

public class LatencyRead extends CommonTools {

    Logger log = Logger.getLogger(LatencyRead.class.getSimpleName());

    /**
     * Captures latency data for the specified type of launch.
     *
     * @param latencyType The type of latency to capture (e.g., "Cool" or "Warm" launch).
     * @param DSN         The Device Serial Number (DSN) identifying the target device.
     * @param appPackage  The package name of the application being launched.
     * @param appIntent   The intent of the application being launched.
     * @return The 50th percentile (tp50) value of the captured latency.
     */
    public double captureLatency(
            String latencyType,
            String DSN,
            String appPackage,
            String appIntent) {
        try {
            String deviceName = DEVICE_NAME;
            if (latencyType.equalsIgnoreCase(COOL_APP_FF)) {
                executeCoolLaunch(deviceName, DSN, appPackage, COOL_APP_FF, appIntent);
            } else if (latencyType.equalsIgnoreCase(WARM_APP_FF)) {
                executeWarmLaunch(deviceName, DSN, appPackage, WARM_APP_FF, appIntent);
            }
            writeLatencyValues(APP_VERSION, DSN, latencyType);
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": There was an exception in starting Latency measurement", e);
        }
        return tp50Value;
    }

    /**
     * Executes measurements for cool launch latency.
     *
     * @param deviceName   The name of the device where the app is launched.
     * @param DSN          The Device Serial Number (DSN) identifying the target device.
     * @param appPackage   The package name of the application being launched.
     * @param metricsName  The name of the metrics being measured (e.g., "Cool" or "Warm" launch).
     * @param appIntent    The intent of the application being launched.
     */
    private void executeCoolLaunch(String deviceName, String DSN,
                                        String appPackage, String metricsName, String appIntent) {
            if (executeFirstColdLaunch(DSN, metricsName, appPackage, appIntent)) {
                log.info("------------------------ Cool KPI Measurement -----------------------");
                log.info(deviceName + ": Starting capture for " + iterations + " iterations");

                for (int i = 0; i < iterations; i++) {
                    try {
                        File adbLogFile = new File(adbLogs + appPackage + "_" + DEVICE_NAME + "_" + "Loop_"
                                + (i + 1) + "_" + metricsName + "_ADB.txt");
                        launchApp(appPackage, appIntent);
                        explicitWait(waitTime);

                        timerArr[i] = getCoolLaunchTimer(DSN, appPackage);
                        cpuConsumption = getCpuConsumption(DSN).split("%")[0];
                        memConsumption = getApp_Memory_Usage(DSN, appPackage).trim();
                        memUsage[i] = Double.parseDouble(memConsumption);
                        cpuUsage[i] = Double.parseDouble(cpuConsumption);

                        getApp_ADBLogsFile(adbLogFile, DSN);
                        displayedArr[i] = getFFDisplayedValue(DSN, appPackage, adbLogFile);
                        checkForRTUMarker(DSN, appPackage, "status", adbLogFile);
                        log.info("Fully Drawn Marker : - " + fullyDrawnMarker);

                        forceStopApp(DSN, appPackage);
                        explicitWait(waitTime);
                        clearLogcatVitals(DSN);
                        clearLogcatBuffer(DSN);
                    } catch (Exception e) {
                        log.error("Exception in executing Cool Launch latency until first frame", e);
                    }
                }
            } else {
                APP_FAILURE_REASON = "Error Occurred while performing pre launch test";
                log.error(APP_FAILURE_REASON);
            }

    }

    /**
     * Executes measurements for warm launch latency.
     *
     * @param deviceName   The name of the device where the app is launched.
     * @param DSN          The Device Serial Number (DSN) identifying the target device.
     * @param appPackage   The package name of the application being launched.
     * @param metricsName  The name of the metrics being measured (e.g., "Cool" or "Warm" launch).
     * @param appIntent    The intent of the application being launched.
     */
    private void executeWarmLaunch(String deviceName, String DSN,
                                        String appPackage, String metricsName, String appIntent) {
            emptyExistingArray();
            if (executeFirstColdLaunch(DSN, metricsName, appPackage, appIntent)) {
                log.info("------------------------ Warm KPI Measurement -----------------------");
                log.info(deviceName + ": Starting capture for " + iterations + " iterations");
                for (int i = 0; i < iterations; i++) {
                    try {
                        File adbLogFile = new File(adbLogs + appPackage + "_" + DEVICE_NAME + "_" + "Loop_"
                                + (i + 1) + "_" + metricsName + "_ADB.txt");
                        launchApp(appPackage, appIntent);
                        explicitWait(waitTime);

                        timerArr[i] = getWarmLaunchTimer(DSN, appPackage);
                        cpuConsumption = getCpuConsumption(DSN).split("%")[0];
                        memConsumption = getApp_Memory_Usage(DSN, appPackage).trim();
                        memUsage[i] = Double.parseDouble(memConsumption);
                        cpuUsage[i] = Double.parseDouble(cpuConsumption);

                        getApp_ADBLogsFile(adbLogFile, DSN);
                        displayedArr[i] = getFFDisplayedValue(DSN, appPackage, adbLogFile);
                        checkForRTUMarker(DSN, appPackage, "status", adbLogFile);
                        log.info("Fully Drawn Marker : - " + fullyDrawnMarker);

                        goHome(DSN);
                        explicitWait(10);
                        clearLogcatVitals(DSN);
                        clearLogcatBuffer(DSN);
                    } catch (Exception e) {
                        log.error("Exception in executing Warm launch latency until first frame", e);
                    }
                }
            } else {
                APP_FAILURE_REASON = "Error Occurred while performing pre launch test";
                log.error(APP_FAILURE_REASON);
            }
    }

}