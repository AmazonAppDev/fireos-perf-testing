package commonUtils;


import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CommonTools extends DataUtils {

    private static final Logger log = Logger.getLogger(CommonTools.class.getSimpleName());
    private File file;
    private FileWriter fileWriter;

    public Boolean installAppAPK(String DSN, File apkPath) throws InterruptedException {
        Process process;
        process = cmd.adb(DSN, cmd.ADB_INSTALL + apkPath);
        log.info("Installing the app");
        process.waitFor();
        try (BufferedReader read =
                     new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = read.readLine()) != null) {
                if (line.contains("Performing Streamed Install")) continue;
                if (line.contains("Success")) {
                    log.info(DEVICE_NAME + ": App Installed by pushing APK");
                    return true;
                } else if (line.contains("Failure") || line.contains("failed") || line.contains("FAILED")) {
                    log.error(DEVICE_NAME + ": " + line);
                }
            }
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while sideloading the APK", e);
        }
        APP_FAILURE_REASON = "App Failed to SideLoad/Install";
        return false;
    }

    public void uninstallAll3PApps(String DSN) {
        try (BufferedReader read = new BufferedReader(new InputStreamReader(cmd.adb(DSN,
                cmd.ADB_LIST_PACKAGES_3P).getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = read.readLine()) != null) {
                if (line.equals("")) continue;
                line = line.split("package:")[1];
                cmd.adb(DSN, cmd.ADB_UNINSTALL + line);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.adb(DSN,
                    cmd.ADB_LIST_PACKAGES_3P).getInputStream(), StandardCharsets.UTF_8));) {
                int count = 0;
                while ((reader.readLine()) != null) {
                    count++;
                }

                if (count <= 5) log.info(getDeviceModelName(DSN) + ": All apps uninstalled");
                else log.warn(getDeviceModelName(DSN) + ": Failed to uninstall all apps");
            } catch (Exception e) {
                log.error(DEVICE_NAME + ": Exception occurred while fetching 3P app list: ", e);
            }
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while uninstalling all 3P apps", e);
        }
    }

    public void kpiTestSetup(String DSN) {
        try {
            showDeviceInfo(DSN);
            DEVICE_NAME = DEVICE_NAME;

            if (DEVICE_TYPE.equals(TABLET)) {
                //For Tablets Specific Pre Requisite Only
                deviceScreenState(DSN);
                disableGameMode(DSN);
                disableScreenLock(DSN);
                if (prop.getProperty("reboot_device").equals("true")) {
                    rebootDevice(DSN);
                    deviceScreenState(DSN);
                }
                uninstallAll3PApps(DSN);
            } else if (DEVICE_TYPE.equals(FTV)) {
                //For FTV Specific Pre Requisite Only
                FtvScenarios fs = new FtvScenarios();
                if (prop.getProperty("reboot_device").equals("true")) rebootDevice(DSN);
                uninstallAll3PApps(DSN);
                fs.enableScreenAwake(DSN);
            }
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while handling Device " +
                    "Specific Pre Requisite actions", e);
        }
    }

    public void clearLogcatVitals(String DSN) {
        try {
            String deviceName = DEVICE_NAME;
            cmd.adb(DSN, cmd.ADB_LOGCAT_CLEAR);
            cmd.adb(DSN, cmd.ADB_CLEAR_LOGCAT_VITALS);
            explicitWait(2);
            log.info(deviceName + ": Logcat Buffer cleared");
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while clearing Logcat : ", e);
        }
    }

    public void testInitiate() {
        try {
            file = new File(outputPath);
            if (!file.exists()) {
                FileUtils.forceMkdir(file);
            }
            FileUtils.cleanDirectory(file);
            log.info("Output Directory Cleaned");

            File file = new File(kpiLog);
            if (!file.exists()) {
                FileUtils.forceMkdir(file);
            }
            FileUtils.cleanDirectory(file);
            log.info("KPI-Logs Directory Cleaned");

            file = new File(adbLogs);
            if (!file.exists()) {
                FileUtils.forceMkdir(file);
            }
            FileUtils.cleanDirectory(file);
            log.info("ADB-Logs Directory Cleaned");

            file = new File(reportPath);
            if (!file.exists()) {
                FileUtils.forceMkdir(file);
            }
            FileUtils.cleanDirectory(file);
            log.info("Reports Directory Cleaned");
        } catch (Exception e) {
            log.error("Exception occurred while cleaning up directories: ", e);
        }
    }

    public void commonTestPreRequisites(String DSN) {
        try {
            File apkFile = new File(apkPath + prop.getProperty("apk_name"));
            if (apkFile.exists()) {
                installAppAPK(DSN, apkFile);
            } else {
                APP_FAILURE_REASON = "Error Occurred while locating APK in the specified location";
                log.error(DEVICE_NAME + ":" + APP_FAILURE_REASON + ": " + apkPath);
            }
        } catch (Exception e) {
            APP_FAILURE_REASON = "Exception occurred while performing Common Pre-request changes";
            log.error(DEVICE_NAME + ": " + APP_FAILURE_REASON + ": ", e);
        }
    }

    public boolean launchAppUsingMonkey(String DSN, String appPackage) {
        String deviceName = DEVICE_NAME;
        if (DEVICE_TYPE.equals(TABLET)) {
            try (BufferedReader launchRead = new BufferedReader(new InputStreamReader(cmd.adb(DSN,
                    cmd.ADB_MONKEY_TOOL + appPackage + cmd.ADB_MONKEY_LAUNCH_INTENT).getInputStream(),
                    StandardCharsets.UTF_8))) {
                String line;
                while ((line = launchRead.readLine()) != null) {
                    if (line.contains("Events injected")) {
                        log.info(deviceName + ": App Launch successful using Monkey Tool - " + appPackage);
                        break;
                    } else if (line.contains("Error") || line.contains("No activities found")) {
                        log.warn(deviceName + ": Error while launching app through Monkey Tool, using Intent to launch");
                        return false;
                    }
                }
            } catch (Exception e) {
                log.error("Exception occurred while launching app using monkey tool", e);
            }

        } else if (DEVICE_TYPE.equals(FTV)) {
            try (BufferedReader launchRead = new BufferedReader(new InputStreamReader(cmd.adb(DSN,
                    cmd.ADB_MONKEY_TOOL_FTV + appPackage + cmd.ADB_MONKEY_LAUNCH_INTENT).getInputStream(),
                    StandardCharsets.UTF_8))) {
                String line;
                while ((line = launchRead.readLine()) != null) {
                    if (line.contains("Events injected")) {
                        log.info(deviceName + ": App Launch successful using Monkey Tool - " + appPackage);
                        break;
                    } else if (line.contains("Error") || line.contains("No activities found")) {
                        log.warn(deviceName + ": Error while launching app through Monkey Tool, using " +
                                "Intent to launch");
                        return false;
                    }
                }
            } catch (Exception e) {
                log.error("Exception occurred while launching app using monkey tool", e);
            }

        }
        return true;
    }

    public Boolean launchAppUsingIntent(String DSN, String appIntent) {
        String deviceName = DEVICE_NAME;
        try (BufferedReader read = new BufferedReader(new InputStreamReader(cmd.adb(DSN, cmd.ADB_START_ACTIVITY
                + appIntent).getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = read.readLine()) != null) {
                if (line.startsWith("Starting: Intent")) {
                    log.info(deviceName + ": App Launched Successfully using App Intent - " + appIntent);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": There was an exception while launching app using App Intent: : ", e);
        }
        APP_FAILURE_REASON = "App Failed to launch using the App Intent";
        log.error(deviceName + ": " + APP_FAILURE_REASON + " - " + appIntent);
        return false;
    }

    public void disableGameMode(String DSN) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(cmd.adb(DSN, cmd.ADB_GET_GAME_MODE).
                getInputStream(), StandardCharsets.UTF_8))) {
            int deviceOs = getDeviceOS(DSN);
            if (deviceOs >= 9) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equalsIgnoreCase("1")) {
                        cmd.adb(DSN, cmd.ADB_DISABLE_GAME_MODE);
                        break;
                    }
                }
                log.info(getDeviceModelName(DSN) + ": Game mode disabled");
            }
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while fetching Disabling Game Mode: ", e);
        }
    }

    public void clearLogcatBuffer(String DSN) {
        try {
            cmd.adb(DSN, cmd.ADB_LOGCAT_CLEAR);
            log.info(DEVICE_NAME + " : Logcat Buffer Cleared");
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while clearing logcat buffer: ", e);
        }
    }

    /**
     * Reboots the device identified by the given Device Serial Number (DSN).
     * The method ensures that the device is properly rebooted by performing
     * checks specific to the device type (TABLET or FTV). If the device does not
     * reboot correctly after a specified number of retries, a warning is logged.
     *
     * @param DSN the Device Serial Number of the device to reboot
     */
    public void rebootDevice(String DSN) {
        final int MAX_RETRIES = 5;

        try {
            log.info("Rebooting Device...");
            cmd.adb(DSN, cmd.ADB_SHELL_REBOOT);
            explicitWait(60);

            if (DEVICE_TYPE.equalsIgnoreCase(TABLET)) {
                handleTabletReboot(DSN, MAX_RETRIES);
            } else if (DEVICE_TYPE.equalsIgnoreCase(FTV)) {
                handleFTVReboot(DSN, MAX_RETRIES);
            }
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception while rebooting Device and unlocking it: ", e);
        }
    }

    /**
     * Handles the reboot process for a tablet device. It checks the lock status of the device
     * and waits for the device to fully reboot. If the device does not reboot correctly after
     * a specified number of retries, a warning is logged.
     *
     * @param DSN the Device Serial Number of the tablet to reboot
     * @param maxRetries the maximum number of retry attempts
     */
    private void handleTabletReboot(String DSN, int maxRetries) {
        String lockDisplay;
        int rebootCheck = 0;

        while (rebootCheck < maxRetries) {
            rebootCheck++;
            try (BufferedReader read = new BufferedReader(new InputStreamReader(cmd.adb(DSN, cmd.ADB_CHECK_A11_LOCK_STATUS).getInputStream(), StandardCharsets.UTF_8))) {
                lockDisplay = read.readLine();
                if (lockDisplay == null) continue;
                if (lockDisplay.contains("WallpaperService")) {
                    log.info("Device Rebooted");
                    return;
                }
                waitInSec(10);
            } catch (Exception e) {
                log.error(DEVICE_NAME + ": Exception occurred while fetching current screen focus: ", e);
            }
        }
        log.warn(DEVICE_NAME + ": Device did not reboot properly after " + maxRetries + " attempts.");
    }

    /**
     * Handles the reboot process for an FTV device. It checks the current screen focus of the device
     * and waits for the device to fully reboot. If the device does not reboot correctly after a specified
     * number of retries, a warning is logged.
     *
     * @param DSN the Device Serial Number of the FTV to reboot
     * @param maxRetries the maximum number of retry attempts
     */
    private void handleFTVReboot(String DSN, int maxRetries) {
        String profilePage;
        int outerRebootCheck = 0;

        while (outerRebootCheck < maxRetries) {
            outerRebootCheck++;
            int rebootCheck = 0;
            while (rebootCheck < maxRetries) {
                rebootCheck++;
                try (BufferedReader read = new BufferedReader(new InputStreamReader(cmd.adb(DSN, cmd.ADB_CURRENT_SCREEN_FOCUS).getInputStream(), StandardCharsets.UTF_8))) {
                    profilePage = read.readLine();
                    if (profilePage != null && (profilePage.contains(prop.getProperty("profilePage")) || profilePage.contains(prop.getProperty("homePage")) || profilePage.contains(prop.getProperty("signPage")))) {
                        log.info("Device Rebooted");
                        return;
                    }
                    waitInSec(10);
                } catch (Exception e) {
                    log.error(DEVICE_NAME + ": Exception occurred while fetching current screen focus: ", e);
                }
            }
        }
        log.warn(DEVICE_NAME + ": Device did not reboot properly after " + maxRetries + " attempts.");
    }

    public void vitalsLogCapture(File logFile, String DSN) throws IOException {
        try (BufferedReader read = new BufferedReader(new InputStreamReader(cmd.adb(DSN, cmd.ADB_DUMP_LOGCAT_VITALS).
                getInputStream(), StandardCharsets.UTF_8))) {
            String line_LatencyApp;
            fileWriter = new FileWriter(logFile, true);
            while ((line_LatencyApp = read.readLine()) != null) {
                fileWriter.write(line_LatencyApp.trim() + "\n");
            }
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while capturing vitals/metrics from adb " +
                    "logs: ", e);
        } finally {
            fileWriter.flush();
        }
    }

    public boolean launchApp(String appPackage, String appIntent) {
        try {
            if (!launchAppUsingMonkey(DSN, appPackage)) {
                return launchAppUsingIntent(DSN, appIntent);
            }
            return true;
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Error occurred while launching the app: ", e);
        }
        return false;
    }

    public Boolean executeFirstColdLaunch(String DSN, String kpiType, String appPackage, String appIntent) {
        try {
            int launchCheck = 0;
            for (int i = 0; i < extraIterations; i++) {
                File adbLogFile = new File(adbLogs + appPackage + "_" + DEVICE_NAME + "_" + "Launch_Test_Loop_" +
                        i+1 + "_" + kpiType + "_ADB.txt");
                double timer = 0.0;
                double displayedTimer = 0.0;
                LaunchTest launchTest = new LaunchTest();
                if (launchApp(appPackage, appIntent)) {
                    explicitWait(30);
                    if (launchTest.checkAppForegroundUsingUi(DSN, appPackage)
                            || launchTest.checkAppForeground(DSN, appPackage)) {
                        if (DEVICE_TYPE.equals(FTV) && kpiType.equalsIgnoreCase(COOL_APP_FF)) {
                            timer = performanceCoolLaunchLogCapture(appPackage, DSN);
                        } else if (DEVICE_TYPE.equals(FTV) && kpiType.equalsIgnoreCase(WARM_APP_FF)) {
                            timer = performanceWarmLaunchLogCapture(appPackage, DSN);
                        } else if (DEVICE_TYPE.equals(TABLET) && kpiType.equalsIgnoreCase(COOL_APP_FF)) {
                            timer = getCoolLaunchTimer(DSN, appPackage);
                        } else if (DEVICE_TYPE.equals(TABLET) && kpiType.equalsIgnoreCase(WARM_APP_FF)) {
                            timer = getWarmLaunchTimer(DSN, appPackage);
                        }
                        getApp_ADBLogsFile(adbLogFile, DSN);
                        displayedTimer = getFFDisplayedValue(DSN, appPackage, adbLogFile);
                        if (!((displayedTimer == 0.0) && (timer == 0.0))) launchCheck++;
                    }
                }
                if (kpiType.equalsIgnoreCase(WARM_APP_FF)) goHome(DSN);
                else forceStopApp(DSN, appPackage);
                clearLogcatBuffer(DSN);
            }
            return launchCheck >= 2;
        } catch (Exception e) {
            log.error("Exception while executing initial cold launch: ", e);
        }
        return false;
    }

    public String getAppPackageFromAPK(String apkName) throws IOException {
        BufferedReader read = null;
        Pattern package_name;
        Matcher matcher;
        try {
            String aaptPAth = aaptPathFinder();
            String commandAapt = aaptPAth + cmd.AAPT_DUMP + apkPath + apkName + cmd.PACKAGE_NAME;
            Process process = Runtime.getRuntime().exec(commandAapt);
            InputStream is = process.getInputStream();
            read = new BufferedReader(new InputStreamReader(is));
            package_name = Pattern.compile("package\\W+name='([a-zA-Z0-9._]+)'.*");
            String line;
            while ((line = read.readLine()) != null) {
                matcher = package_name.matcher(line);
                if (matcher.find()) {
                    line = matcher.group(1);
                    break;
                }
            }
            if (line != null) {
                log.info("App Package From APK: " + line);
                return line;
            }
        } catch (Exception e) {
            log.error("Error occurred while fetching Package Name from APK: ", e);
        } finally {
            if (read != null) read.close();
        }
        APP_FAILURE_REASON = "Failed To fetch App Package Name from APK";
        log.error(APP_FAILURE_REASON);
        return null;
    }

    public String aaptPathFinder() {
        try {
            String sdkPath = System.getenv("ANDROID_HOME");
            log.info("SDK path: " + sdkPath);
            if (sdkPath.contains("platform-tools")) {
                sdkPath = sdkPath.split("platform-tools")[0];
            }
            if (sdkPath != null) {
                String aaptPath = sdkPath + "/build-tools/34.0.0/";
                log.info("Found aapt at: " + aaptPath);
                return aaptPath;
            }
        } catch (Exception e) {
            log.error("Exception occurred while fetching AAPT path: ", e);
        }
        log.error("ANDROID_HOME environment variable not set");
        return null;
    }

}
