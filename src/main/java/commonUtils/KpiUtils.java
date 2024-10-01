package commonUtils;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

public class KpiUtils extends DeviceTools {

    private final Logger log = Logger.getLogger(KpiUtils.class.getSimpleName());
    public DecimalFormat df2 = new DecimalFormat("#.###");

    /**
     * Retrieves the memory usage of a specified app package on the device identified by the provided DSN.
     * This method fetches memory usage information for the specified app package on the device using adb commands.
     * The memory usage is returned in megabytes (MB).
     *
     * @param DSN        The Device Serial Number (DSN) of the device.
     * @param appPackage The package name of the app for which memory usage needs to be retrieved.
     * @return The memory usage of the specified app package in megabytes (MB), or "0" if unable to retrieve.
     * @throws IOException If an I/O error occurs while reading the memory information.
     */
    public String getApp_Memory_Usage(String DSN, String appPackage) throws IOException {
        BufferedReader reader = null;
        try {
            int deviceOs = getDeviceOS(DSN);

            if (deviceOs > 9 && DEVICE_TYPE.equals(TABLET)) {
                reader = new BufferedReader(new InputStreamReader(
                        cmd.adb(DSN, cmd.ADB_MEMORY_INFO + cmd.ADB_GREP + appPackage).getInputStream(), StandardCharsets.UTF_8));
            } else if (deviceOs > 9 && DEVICE_TYPE.equals(FTV)) {
                reader = new BufferedReader(new InputStreamReader(
                        cmd.adb(DSN, cmd.ADB_MEMORY_INFO_FOS7 + cmd.ADB_GREP + appPackage).getInputStream(), StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(
                        cmd.adb(DSN, cmd.ADB_MEMORY_INFO_FOS5 + cmd.ADB_GREP + appPackage).getInputStream(), StandardCharsets.UTF_8));
            }
            String memoryDump;
            while ((memoryDump = reader.readLine()) != null) {
                if (memoryDump.contains(appPackage)) {
                    memoryDump = memoryDump.split(":")[0].trim();
                    memoryDump = memoryDump.replaceAll("[^0-9]", "");
                    double mem = Double.parseDouble(memoryDump);
                    return df2.format(mem / 1024);
                }
            }
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while getting memory dump: ", e);
        } finally {
            if (reader != null) reader.close();
        }
        return "0";
    }

    /**
     * Retrieves the CPU usage of a specified app package on the device identified by the provided DSN.
     * This method fetches CPU usage information for the specified app package on the device using adb commands.
     * The CPU usage is returned in percentage.
     *
     * @param DSN        The Device Serial Number (DSN) of the device.
     * @param appPackage The package name of the app for which CPU usage needs to be retrieved.
     * @return The CPU usage of the specified app package in percentage, or an empty string if unable to retrieve.
     * @throws IOException If an I/O error occurs while reading the CPU information.
     */
    public String getApp_CPU_Usage(String DSN, String appPackage) throws IOException {
        BufferedReader reader = null;
        try {
            int deviceOs = getDeviceOS(DSN);
            if (deviceOs >= 9) {
                reader = new BufferedReader(new InputStreamReader(
                        cmd.adb(DSN, cmd.ADB_CPU_INFO + cmd.ADB_GREP + appPackage).getInputStream(),
                        StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(
                        cmd.adb(DSN, cmd.ADB_CPU_INFO_FOS5 + cmd.ADB_GREP + appPackage).getInputStream(),
                        StandardCharsets.UTF_8));
            }
            String cpuDump, user, total;
            while ((cpuDump = reader.readLine()) != null) {
                if (cpuDump.contains(appPackage)) {
                    user = cpuDump.split(":")[1];
                    user = user.split("/")[0];
                    total = cpuDump.split("%")[0];
                    reader.close();
                    return total + "%" + " total  " + user;
                }
            }
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while getting CPU dump: ", e);
        } finally {
            if (reader != null) reader.close();
        }
        return "";
    }

    /**
     * Retrieves the launch timer for the "Cool" application on the device identified by the provided DSN.
     * This method searches the device logs for performance metrics related to the "Cool" application launch,
     * including both the "CoolApp" and "CoolActivity" events. It extracts the launch timer from the logs.
     * The launch timer is returned in milliseconds.
     *
     * @param DSN        The Device Serial Number (DSN) of the device.
     * @param appPackage The package name of the "Cool" application for which the launch timer needs to be retrieved.
     * @return The launch timer for the "Cool" application in milliseconds, or 0 if unable to retrieve.
     * @throws IOException If an I/O error occurs while reading the device logs.
     */
    public double getCoolLaunchTimer(String DSN, String appPackage) throws IOException {
        int timer = 0;
        BufferedReader reader_New = null, read = null;
        try {
            String line_CoolApp;
            String line_CoolActivity;
            boolean condition = false;
            int i = 0;

            read = new BufferedReader(new InputStreamReader(cmd.adb(DSN, cmd.ADB_DUMP_LOGCAT_VITALS).getInputStream(),
                    StandardCharsets.UTF_8));
            while ((line_CoolApp = read.readLine()) != null) {
                if (line_CoolApp.contains("performance:" + COOL_APP) && line_CoolApp.contains("key=" + appPackage)) {
                    timer = splitToTimer(line_CoolApp);
                    i++;
                    condition = true;
                    metricsName = COOL_APP_FF;

                }
            }

            reader_New = new BufferedReader(new InputStreamReader(cmd.adb(DSN,
                    cmd.ADB_DUMP_LOGCAT_VITALS).getInputStream(), StandardCharsets.UTF_8));
            while ((line_CoolActivity = reader_New.readLine()) != null && !condition) {
                if (line_CoolActivity.contains("performance:" + COOL_ACTIVITY) && line_CoolActivity.contains("key=" + appPackage)) {
                    timer = splitToTimer(line_CoolActivity);
                    i++;
                    condition = true;
                    metricsName = COOL_APP_FF;

                }
            }
            log.info(DEVICE_NAME + ": Cool : " + metricsName + " : " + timer + " ms");

            if (!condition) {
                log.error(DEVICE_NAME + ": No Cool Vital Latency value found in logs");
            }
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while capturing cool vitals/metrics in adb logs: ", e);
        } finally {
            if (read != null) read.close();
            if (reader_New != null) reader_New.close();
        }
        return (double) timer / 1000;
    }

    /**
     * Retrieves the launch timer for the "Warm" application on the device identified by the provided DSN.
     * This method searches the device logs for performance metrics related to the "Warm" application launch,
     * including both the "WarmApp" and "WarmActivity" events. It extracts the launch timer from the logs.
     * The launch timer is returned in milliseconds.
     *
     * @param DSN        The Device Serial Number (DSN) of the device.
     * @param appPackage The package name of the "Warm" application for which the launch timer needs to be retrieved.
     * @return The launch timer for the "Warm" application in milliseconds, or 0 if unable to retrieve.
     * @throws IOException If an I/O error occurs while reading the device logs.
     */
    public double getWarmLaunchTimer(String DSN, String appPackage) throws IOException {
        int timer = 0;
        metricsName = null;
        BufferedReader read = null, reader_New = null;
        try {
            String line_WarmApp;
            String line_WarmActivity;
            boolean condition = false;
            int i = 0;

            read = new BufferedReader(new InputStreamReader(cmd.adb(DSN, cmd.ADB_DUMP_LOGCAT_VITALS).getInputStream(),
                    StandardCharsets.UTF_8));
            while ((line_WarmApp = read.readLine()) != null) {
                if (line_WarmApp.contains("performance:" + WARM_APP_WARM) && line_WarmApp.contains("key=" + appPackage)) {
                    timer = splitToTimer(line_WarmApp);
                    i++;
                    condition = true;
                    metricsName = WARM_APP_FF;

                }
            }

            reader_New = new BufferedReader(new InputStreamReader(cmd.adb(DSN,
                    cmd.ADB_DUMP_LOGCAT_VITALS).getInputStream(), StandardCharsets.UTF_8));
            while ((line_WarmActivity = reader_New.readLine()) != null && !condition) {
                if (line_WarmActivity.contains("performance:" + WARM_APP_COOL) && line_WarmActivity.contains("key=" + appPackage)) {
                    timer = splitToTimer(line_WarmActivity);
                    i++;
                    condition = true;
                    metricsName = WARM_APP_FF;

                }
            }
            log.info(DEVICE_NAME + ": Warm : " + metricsName + " : " + timer + " ms");

            if (!condition) {
                log.error(DEVICE_NAME + ": No Warm Vital Latency value found in logs");
            }
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while capturing Warm vitals/metrics in adb logs: ", e);
        } finally {
            if (read != null) read.close();
            if (reader_New != null) reader_New.close();
        }
        return (double) timer / 1000;
    }

    /**
     * Captures the launch performance metrics for the "Warm" application by analyzing the device logs.
     * This method retrieves the performance metrics related to the launch of the "Warm" application
     * from the device logs and writes them to a text file for further analysis. It then parses the logs
     * to extract the launch timer and the associated metrics. The launch timer is returned in milliseconds.
     *
     * @param appPackage The package name of the "Warm" application for which performance metrics are captured.
     * @param DSN        The Device Serial Number (DSN) of the device.
     * @return The launch timer for the "Warm" application in milliseconds, or 0 if unable to retrieve.
     * @throws IOException If an I/O error occurs while reading or writing the device logs.
     */
    public double performanceWarmLaunchLogCapture(String appPackage, String DSN) throws IOException {
        DEVICE_NAME = getDeviceName(DSN);
        metricsName = "null";
        int timer = 0;
        int metricsChk = 0;
        boolean condition = false;
        BufferedReader read = null, reader = null, read_New = null;
        FileWriter fileWriter = null;
        File file;
        String pathName = kpiLog + "Warm_ADB_Log_" + appPackage + "_" + DEVICE_NAME + ".txt";
        Path path = Paths.get(pathName);
        try {
            read = new BufferedReader(new InputStreamReader(cmd.adb(DSN,
                    cmd.ADB_DUMP_LOGCAT_VITALS).getInputStream(), StandardCharsets.UTF_8));
            String line_WarmApp;
            String line_WarmActivity;

            file = new File(pathName);
            fileWriter = new FileWriter(file);
            while ((line_WarmApp = read.readLine()) != null) {
                fileWriter.write(line_WarmApp.trim() + "\n");
            }

            reader = new BufferedReader(new FileReader(file));
            while ((line_WarmApp = reader.readLine()) != null) {
                if (line_WarmApp.contains("performance:" + WARM_APP_WARM) && line_WarmApp.contains("key=" + appPackage)) {
                    timer = splitToTimer(line_WarmApp);
                    metricsName = WARM_APP_WARM;
                    condition = true;
                    metricsChk = 1;
                }
            }

            read_New = new BufferedReader(new FileReader(file));
            while ((line_WarmActivity = read_New.readLine()) != null && metricsChk < 1) {
                if (line_WarmActivity.contains("performance:" + WARM_APP_COOL) && line_WarmActivity.contains("key=" + appPackage)) {
                    timer = splitToTimer(line_WarmActivity);
                    condition = true;
                    metricsName = WARM_APP_COOL;
                }
            }

            if (!condition) {
                log.error(DEVICE_NAME + ": No Warm Vital Activity value found in logs");
            }

        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while capturing warm vitals/metrics in adb logs: ", e);
        } finally {
            if (fileWriter != null) fileWriter.flush();
            if (fileWriter != null) fileWriter.close();
            if (read != null) read.close();
            if (reader != null) reader.close();
            if (read_New != null) read_New.close();
            if (Files.exists(path)) Files.delete(path);
        }
        log.info(DEVICE_NAME + ": Warm : " + metricsName + " : " + timer + " ms");
        return (double) timer / 1000;
    }

    /**
     * Captures the launch performance metrics for the "Cool" application by analyzing the device logs.
     * This method retrieves the performance metrics related to the launch of the "Cool" application
     * from the device logs and writes them to a text file for further analysis. It then parses the logs
     * to extract the launch timer and the associated metrics. The launch timer is returned in milliseconds.
     *
     * @param appPackage The package name of the "Cool" application for which performance metrics are captured.
     * @param DSN        The Device Serial Number (DSN) of the device.
     * @return The launch timer for the "Cool" application in milliseconds, or 0 if unable to retrieve.
     */
    public double performanceCoolLaunchLogCapture(String appPackage, String DSN) {
        metricsName = "null";
        int timer = 0;
        int metricsChk = 0;
        boolean condition = false;
        BufferedReader read = null, reader = null, read_New = null;
        FileWriter fileWriter = null;
        File file;
        String pathName = kpiLog + "Cool_ADB_Log_" + appPackage + "_" + DEVICE_NAME + ".txt";
        Path path = Paths.get(pathName);
        try {
            read = new BufferedReader(new InputStreamReader(cmd.adb(DSN, cmd.ADB_DUMP_LOGCAT_VITALS).getInputStream(),
                    StandardCharsets.UTF_8));
            String line_CoolApp;
            String line_CoolActivity;

            file = new File(pathName);
            fileWriter = new FileWriter(file);
            while ((line_CoolApp = read.readLine()) != null) {
                fileWriter.write(line_CoolApp.trim() + "\n");
            }
            fileWriter.flush();

            reader = new BufferedReader(new FileReader(file));
            while ((line_CoolApp = reader.readLine()) != null) {
                if (line_CoolApp.contains("performance:" + COOL_APP) && line_CoolApp.contains("key=" + appPackage)) {
                    timer = splitToTimer(line_CoolApp);
                    metricsName = COOL_APP;
                    condition = true;
                    metricsChk = 1;
                }
            }

            read_New = new BufferedReader(new FileReader(file));
            while ((line_CoolActivity = read_New.readLine()) != null && metricsChk < 1) {
                if (line_CoolActivity.contains("performance:" + COOL_ACTIVITY) && line_CoolActivity.contains("key=" + appPackage)) {
                    timer = splitToTimer(line_CoolActivity);
                    condition = true;
                    metricsName = COOL_ACTIVITY;
                }
            }

            if (!condition) {
                log.error(DEVICE_NAME + ": No Cool Vital Latency value found in logs");
            }

        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception while capturing cool vitals/metrics in adb logs: ", e);
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.flush();
                    fileWriter.close();
                }
                if (read != null) read.close();
                if (reader != null) reader.close();
                if (read_New != null) read_New.close();
                if (Files.exists(path)) Files.delete(path);
            } catch (Exception e) {
                log.error(DEVICE_NAME + ": Exception Occurred while closing Buffer Reader: ", e);
            }
        }
        log.info(DEVICE_NAME + ": Cool : " + metricsName + " : " + timer + " ms");
        return (double) timer / 1000;
    }

    /**
     * Retrieves the CPU consumption information of the device.
     * This method retrieves the CPU consumption information of the device specified by its Device Serial Number (DSN).
     * It executes the appropriate adb command based on the Android OS version of the device to fetch CPU usage data.
     * The method then parses the output to extract the CPU consumption information and returns it as a string.
     *
     * @param DSN The Device Serial Number (DSN) of the device.
     * @return A string representing the CPU consumption information of the device, or "0" if unable to retrieve.
     * @throws IOException If an I/O error occurs while fetching the CPU consumption information.
     */
    public String getCpuConsumption(String DSN) throws IOException {
        String line = "0";
        BufferedReader read = null;
        try {
            if (getDeviceOS(DSN) < 9) {

                read = new BufferedReader(new InputStreamReader
                        (cmd.adb(DSN, cmd.ADB_CPU_INFO_FOS5).getInputStream(), StandardCharsets.UTF_8));
            } else {
                read = new BufferedReader(new InputStreamReader
                        (cmd.adb(DSN, cmd.ADB_CPU_INFO).getInputStream(), StandardCharsets.UTF_8));
            }
            while ((line = read.readLine()) != null) {
                if (line.contains("TOTAL")) {
                    line = line.split("TOTAL")[0].trim();
                    break;
                }
            }
            return line;
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while fetching Device CPU: ", e);
        } finally {
            if (read != null) read.close();
        }
        return line;
    }

    /**
     * Checks for the presence of the RTU (Ready To Use) marker for the specified app package in the adb log file.
     * This method reads the contents of the adb log file line by line to search for the RTU marker related to the fully drawn event of the specified app.
     * If the marker is found, it indicates that the app has been fully drawn, and the method returns true.
     * Additionally, if the `type` parameter is set to "value", the method retrieves the RTU marker value and returns it as a double.
     *
     * @param DSN        The Device Serial Number (DSN) of the device.
     * @param appPackage The package name of the app for which the RTU marker is being checked.
     * @param type       The type of RTU marker value to retrieve ("value" to get the marker value, any other value to skip).
     * @param adbLog     The adb log file containing the log messages.
     * @return If `type` is "value", returns the RTU marker value as a double; otherwise, returns 0.00.
     * @throws IOException If an I/O error occurs while reading the adb log file.
     */
    public double checkForRTUMarker(String DSN, String appPackage, String type, File adbLog) throws IOException {
        fullyDrawnMarker = false;

        BufferedReader br = null;

        try {
            int rtuValue = 0;
            double rtuFinalValue = 0.00;
            String appIntent = getAppIntent(DSN, appPackage);
            String rtuMarker = null;
            br = new BufferedReader(new FileReader(adbLog));
            while ((rtuMarker = br.readLine()) != null) {
                if (rtuMarker.contains(AM_FULLY_DRAWN) || rtuMarker.contains(WM_FULLY_DRAWN)) {
                    if (rtuMarker.contains(appIntent) || rtuMarker.contains((appPackage))) {
                        log.info("RTU Marker present for: " + appPackage);
                        fullyDrawnMarker = true;
                        log.info(rtuMarker);
                        break;
                    } else {
                        continue;
                    }
                }
            }

            if (type.equalsIgnoreCase("value")) {
                if (!(rtuMarker == null)) {
                    rtuMarker = rtuMarker.split("]")[0];
                    rtuMarker = rtuMarker.split("\\[")[1];
                    String[] rtuMarkerArr = rtuMarker.split(",");
                    int arraySize;
                    arraySize = rtuMarkerArr.length;
                    rtuMarker = rtuMarkerArr[arraySize - 1];
                    rtuValue = Integer.parseInt(rtuMarker);
                    rtuFinalValue = rtuValue / 1000.00;
                    log.info("RTU Marker Value: " + rtuFinalValue);
                    return rtuFinalValue;
                } else {
                    log.error(DEVICE_NAME + ": Unable to Find RTU Marker for:" + appPackage);
                    return 0.00;
                }
            } else {
                return 0.00;
            }
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while fetching RTU Marker: ", e);
            return 0.00;
        } finally {
            if (br != null) br.close();
        }
    }

    /**
     * Retrieves the First Frame (FF) displayed value for the specified app package from the log file.
     * This method reads the contents of the log file line by line to search for the First Frame (FF) displayed marker related to the app launch time.
     * If the marker is found, it returns the displayed value as a double.
     *
     * @param DSN        The Device Serial Number (DSN) of the device.
     * @param appPackage The package name of the app for which the Fully Drawn (FF) displayed value is being retrieved.
     * @param logFile    The log file containing the log messages.
     * @return The Fully Drawn (FF) displayed value for the specified app package as a double.
     */
    public double getFFDisplayedValue(String DSN, String appPackage, File logFile) {
        BufferedReader br = null, cr = null;
        try {
            int ffValue;
            double ffFinalValue;
            String appIntent = getAppIntent(DSN, appPackage), displayedMarker, launch_time = null;

            br = new BufferedReader(new FileReader(logFile));
            while ((displayedMarker = br.readLine()) != null) {
                if (displayedMarker.contains(AM_ACTIVITY_LAUNCH_TIME) || displayedMarker.contains(WM_ACTIVITY_LAUNCH_TIME)) {
                    if (displayedMarker.contains(appIntent) || displayedMarker.contains((appPackage))) {
                        log.info("Displayed Marker present for: " + appPackage);
                        break;
                    }
                }
            }

            cr = new BufferedReader(new FileReader(logFile));
            if (displayedMarker == null) {
                while ((launch_time = cr.readLine()) != null) {
                    if (launch_time.contains(WARM_ACTIVITY_LAUNCH_TIME)) {
                        if (launch_time.contains(appIntent) || launch_time.contains((appPackage))) {
                            log.info("Displayed Marker Present for: " + appPackage);
                            break;
                        }
                    }
                }
            }

            if (displayedMarker != null) {
                displayedMarker = displayedMarker.split("]")[0];
                displayedMarker = displayedMarker.split("\\[")[1];
                String[] displayedMarkerArr = displayedMarker.split(",");
                int arraySize;
                arraySize = displayedMarkerArr.length;
                displayedMarker = displayedMarkerArr[arraySize - 1];
                ffValue = Integer.parseInt(displayedMarker);
                ffFinalValue = ffValue / 1000.00;
                log.info("Displayed Marker Value: " + ffFinalValue);
                return ffFinalValue;
            } else if (launch_time != null) {
                launch_time = launch_time.split("Timer=")[1];
                launch_time = launch_time.split("TI")[0];
                launch_time = launch_time.split("\\.")[0];
                ffValue = Integer.parseInt(launch_time);
                ffFinalValue = ffValue / 1000.00;
                log.info("Displayed Marker Value: " + ffFinalValue);
                return ffFinalValue;
            }
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Error Occurred while getting Displayed Value ", e);
            return 0;
        } finally {
            try {
                br.close();
                cr.close();
            } catch (Exception e) {
                log.error(DEVICE_NAME + ":Exception Occurred while closing Buffer Reader: ", e);
            }
        }
        log.error(DEVICE_NAME + ": Unable to Find Displayed Marker for:" + appPackage);
        return 0.00;
    }

    /**
     * Launches the specified app using the Monkey tool.
     * This method executes an adb command to launch the app using the Monkey tool on the device specified by its serial number (DSN).
     * It reads the output of the adb command to check if the app launch was successful.
     *
     * @param DSN        The Device Serial Number (DSN) of the device.
     * @param appPackage The package name of the app to be launched.
     * @return True if the app was successfully launched using the Monkey tool, otherwise false.
     * @throws IOException If an I/O error occurs.
     */
    public boolean launchAppUsingMonkey(String DSN, String appPackage) throws IOException {
        BufferedReader launchRead = null;
        try {
            if (DEVICE_TYPE.equals(TABLET)) {
                launchRead = new BufferedReader(new InputStreamReader
                        (cmd.adb(DSN, cmd.ADB_MONKEY_TOOL + appPackage + cmd.ADB_MONKEY_LAUNCH_INTENT)
                                .getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = launchRead.readLine()) != null) {
                    if (line.contains("Events injected")) {
                        log.info(DEVICE_NAME + ": App Launch successful using Monkey Tool - " + appPackage);
                        break;
                    } else if (line.contains("Error") || line.contains("No activities found")) {
                        log.warn(DEVICE_NAME + ": Error while launching app through Monkey Tool, using Intent to launch");
                        return false;
                    }
                }
            } else if (DEVICE_TYPE.equals(FTV)) {
                launchRead = new BufferedReader(new InputStreamReader
                        (cmd.adb(DSN, cmd.ADB_MONKEY_TOOL_FTV + appPackage + cmd.ADB_MONKEY_LAUNCH_INTENT)
                                .getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = launchRead.readLine()) != null) {
                    if (line.contains("Events injected")) {
                        log.info(DEVICE_NAME + ": App Launch successful using Monkey Tool - " + appPackage);
                        break;
                    } else if (line.contains("Error") || line.contains("No activities found")) {
                        log.warn(DEVICE_NAME + ": Error while launching app through Monkey Tool, using Intent to launch");
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while launching the app using Monkey tool: ", e);
            return false;
        } finally {
            if (launchRead != null) launchRead.close();
        }
    }

    /**
     * Extracts the timer value from the given line of text.
     * This method parses the input line to extract the timer value, which typically represents a time duration.
     *
     * @param line The input line of text containing the timer value.
     * @return The extracted timer value as an integer.
     * @throws NumberFormatException If the timer value cannot be parsed as an integer.
     */
    public int splitToTimer(String line) {
        try {
            line = line.split("Timer=")[1];
            line = line.split(";")[0];
            line = line.replace(".0", "");
            return Integer.parseInt(line);
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while formatting the timer value from logs: ", e);
            return 0;
        }
    }

}
