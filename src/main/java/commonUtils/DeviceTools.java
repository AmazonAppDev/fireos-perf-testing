package commonUtils;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class DeviceTools extends ReadPaths {
    private final Logger log = Logger.getLogger(DeviceTools.class.getSimpleName());
    Commands cmd = new Commands();
    private FileWriter fileWriter;


    public AndroidDriver<MobileElement> device;

    /**
     * Retrieves the model name of the device specified by the DSN (Device Serial Number).
     * This method executes an ADB (Android Debug Bridge) command to obtain the device model
     * name, reads the output, and returns it in uppercase. If the output is null, it returns "NA".
     * If an exception occurs during the process, it logs the error and returns null.
     *
     * @param DSN the Device Serial Number of the device whose model name is to be fetched.
     * @return the device model name in uppercase, "NA" if the model name is not available,
     * or null if an exception occurs.
     */
    public String getDeviceModelName(String DSN) {
        try (BufferedReader read = new BufferedReader(new InputStreamReader
                (cmd.adb(DSN, cmd.ADB_DEVICE_MODEL).getInputStream(), StandardCharsets.UTF_8))) {
            String processReadLine = read.readLine();
            if (processReadLine != null) {
                return processReadLine.toUpperCase(Locale.ROOT);
            }
            return "NA";
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception while fetching Get Device Model Name: ", e);
            return null;
        }
    }

    /**
     * Retrieves the version of the specified app installed on the device with the given DSN (Device Serial Number).
     * This method executes an ADB (Android Debug Bridge) command to obtain the app version from the device.
     * It reads the output, extracts the version number, and returns it. If an exception occurs during the process,
     * it logs the error and returns null.
     *
     * @param DSN        the Device Serial Number of the device from which the app version is to be fetched.
     * @param appPackage the package name of the app whose version is to be fetched.
     * @return the version number of the specified app, or null if an exception occurs.
     */
    public String getAppVersionFromDevice(String DSN, String appPackage) {
        try (BufferedReader read = new BufferedReader(new InputStreamReader(cmd.adb(DSN, cmd.ADB_DUMPSYS_PACKAGE
                + appPackage + cmd.GET_VERSION_NO).getInputStream(), StandardCharsets.UTF_8))) {
            String line, versionName = null;
            while ((line = read.readLine()) != null) {
                if (line.equals("")) {
                    continue;
                }
                versionName = line.split("=")[1];
            }
            return versionName;
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while Getting app version: ", e);
            return null;
        }
    }

    /**
     * Retrieves the major version of the operating system running on the device with the given DSN (Device Serial Number).
     * This method executes an ADB (Android Debug Bridge) command to obtain the OS version from the device.
     * It reads the output, extracts the major version number, and returns it as an integer.
     * If an exception occurs during the process, it logs the error and returns 0.
     *
     * @param DSN the Device Serial Number of the device from which the OS version is to be fetched.
     * @return the major version number of the device's operating system, or 0 if an exception occurs.
     */
    public int getDeviceOS(String DSN) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(cmd.adb(DSN, cmd.ADB_DEVICE_OS).
                getInputStream()))) {
            String os = null;
            int OS = 0;
            while ((os = in.readLine()) != null) {
                if (os.contains(".")) {
                    os = os.substring(0, 1).trim();
                }
                OS = Integer.parseInt(os);
                break;
            }
            return OS;
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while getting OS version: ", e);
            return 0;
        }
    }

    /**
     * Retrieves the Fire OS build version of the device specified by the DSN (Device Serial Number).
     * This method executes an ADB (Android Debug Bridge) command to obtain the Fire OS build version
     * from the device. It reads the output and returns it as a string. If an exception occurs during
     * the process, it logs the error and returns null.
     *
     * @param DSN the Device Serial Number of the device from which the Fire OS build version is to be fetched.
     * @return the Fire OS build version of the device, or null if an exception occurs.
     * @throws IOException if an I/O error occurs during the reading process.
     */
    public String getFireOSBuild(String DSN) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(cmd.adb(DSN, cmd.ADB_DEVICE_FOS_VERSION).
                getInputStream(), StandardCharsets.UTF_8))) {
            return in.readLine();
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while getting Fire OS: ", e);
            return null;
        }
    }

    /**
     * Retrieves the battery status of the device specified by the DSN (Device Serial Number).
     * This method executes an ADB (Android Debug Bridge) command to obtain the battery status
     * from the device. It reads the output, extracts the battery percentage, logs the battery
     * status, and advises if the battery level is below 40% (recommended for performance testing).
     * If an exception occurs during the process, it logs the error.
     *
     * @param DSN the Device Serial Number of the device from which the battery status is to be fetched.
     */
    private void getDeviceBattery(String DSN) {
        try {
            String deviceName = DEVICE_NAME;
            BufferedReader batteryReader = new BufferedReader(new InputStreamReader(cmd.adb(DSN, cmd.ADB_DEVICE_BATTERY)
                    .getInputStream(), StandardCharsets.UTF_8));
            String battery = batteryReader.readLine();
            String batteryDetails = battery.substring(battery.indexOf(":") + 1, battery.length());
            log.info(deviceName + ": Battery status: " + batteryDetails + "%");

            if (Integer.parseInt(batteryDetails.trim()) < 40) {
                log.info("For Performance Testing, Recommended battery percentage is 40%");
            }
            batteryReader.close();
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while getting battery details: ", e);
        }
    }

    /**
     * Retrieves the name of the device specified by the DSN (Device Serial Number).
     * This method executes an ADB (Android Debug Bridge) command to obtain the device name.
     * It reads the output and returns the device name in uppercase. If no name is found,
     * it returns "device". If an exception occurs during the process, it logs the error
     * and returns null.
     *
     * @param DSN the Device Serial Number of the device whose name is to be fetched.
     * @return the device name in uppercase, "device" if the name is not found, or null if an exception occurs.
     */
    public String getDeviceName(String DSN) {
        try (BufferedReader read = new BufferedReader(new InputStreamReader(cmd.adb(DSN, cmd.ADB_DEVICE_NAME)
                .getInputStream(), StandardCharsets.UTF_8))) {
            String line = null;
            while ((line = read.readLine()) != null) {
                return line.toUpperCase();
            }
            return "device";
        } catch (Exception e) {
            log.error("Exception occurred while fetching Device Name: ", e);
            return null;
        }
    }

    /**
     * Displays information about the device specified by the DSN (Device Serial Number).
     * This method logs the connection status of the device, retrieves and logs the device OS version,
     * and fetches and logs the device battery status. If an exception occurs during the process, it logs the error.
     *
     * @param DSN the Device Serial Number of the device whose information is to be displayed.
     */
    public void showDeviceInfo(String DSN) {
        try {
            String deviceName = DEVICE_NAME;
            log.info("Device Connected: " + deviceName + ": " + DSN);
            String OS = getFireOSBuild(DSN);
            log.info(deviceName + ": Device OS Version: " + OS);
            getDeviceBattery(DSN);
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while capturing device details: ", e);
        }
    }

    /**
     * Pauses the execution for a specified number of seconds.
     * This method puts the current thread to sleep for the given number of seconds.
     * If an exception occurs during the sleep period, it logs the error.
     *
     * @param secs the number of seconds to wait.
     */
    public void explicitWait(int secs) {
        try {
            Thread.sleep(secs * 1000L);
        } catch (Exception e) {
            log.error("Exception occurred while waiting explicitly: ", e);
        }
    }

    /**
     * Checks the screen state of the device specified by the DSN (Device Serial Number) and unlocks it if necessary.
     * This method performs the following steps:
     * 1. Checks if the device screen is locked by querying the ADB lock status.
     * 2. If the screen is locked, it waits for 10 seconds and retries up to 5 times.
     * 3. If the screen is not locked, it logs that the device is not locked and exits.
     * 4. If the screen is still locked after 5 retries, it checks the screen state.
     * 5. If the screen state is "OFF", it attempts to unlock the device.
     * 6. If the screen state is "ON", it logs that unlocking is not required.
     * 7. Attempts to unlock the device if needed.
     * If an exception occurs during any step, it logs the error.
     *
     * @param DSN the Device Serial Number of the device whose screen state is to be checked and managed.
     */
    public void deviceScreenState(String DSN) {
        String device = getDeviceModelName(DSN);
        String lockDisplay;
        int loopBreak = 0;
        while (loopBreak < 5) {
            loopBreak++;
            try (BufferedReader read = new BufferedReader(new InputStreamReader(cmd.adb(DSN, cmd.ADB_CHECK_A11_LOCK_STATUS).
                    getInputStream()));) {
                lockDisplay = read.readLine();
                if (lockDisplay == null) continue;
                if (!lockDisplay.contains("WallpaperService")) {
                    log.info(device + " : Device is not locked...");
                    return;
                }
                explicitWait(10);
            } catch (Exception e) {
                log.error(DEVICE_NAME + ": Exception occurred while fetching current screen focus: ", e);
            }
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader
                (cmd.adb(DSN, cmd.ADB_SCREEN_STATE).getInputStream(), StandardCharsets.UTF_8));) {
            String processReadLine;
            while ((processReadLine = in.readLine()) != null) {
                if (processReadLine.equals("")) continue;

                if (processReadLine.contains("OFF")) {
                    log.info(DEVICE_NAME + " : Device lock state: TRUE, unlocking the device");
                    cmd.adb(DSN, cmd.ADB_DEVICE_LOCK_UNLOCK);
                    explicitWait(3);
                } else {
                    log.info(DEVICE_NAME + " : Device Display is ON, unlocking device is not required");
                }
                break;
            }
            unlockDevice(DSN);
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while detecting Lock Screen and Unlocking Device:", e);
        }
    }

    /**
     * Retrieves the screen width of the device specified by the DSN (Device Serial Number).
     * This method executes an ADB (Android Debug Bridge) command to obtain the physical screen size of the device.
     * It parses the output to extract and return the screen width in pixels. If an exception occurs during the process,
     * it logs the error and returns 0.
     *
     * @param DSN the Device Serial Number of the device whose screen width is to be fetched.
     * @return the screen width in pixels, or 0 if an exception occurs.
     */
    public int getScreenWidth(String DSN) {
        try (BufferedReader read = new BufferedReader(new InputStreamReader
                (cmd.adb(DSN, cmd.ADB_DEVICE_SCREEN_SIZE).getInputStream(), StandardCharsets.UTF_8))) {
            String line = read.readLine();
            line = line.split("Physical size:")[1];
            line = line.split("x")[0];
            return Integer.parseInt(line.trim());
        } catch (Exception e) {
            log.error(DEVICE_NAME + ":Exception occurred while fetching Screen Width: ", e);
            return 0;
        }
    }

    /**
     * Unlocks the device specified by the DSN (Device Serial Number).
     * This method attempts to unlock the device by performing the following steps:
     * 1. Waits for 5 seconds.
     * 2. Checks the screen width using {@link #getScreenWidth(String)} method.
     * 3. If the screen width is 600 pixels, performs a swipe gesture for small screens; otherwise, for large screens.
     * 4. Simulates a device key input to finalize the unlocking process.
     * If an exception occurs during any step, it logs the error.
     *
     * @param DSN the Device Serial Number of the device to be unlocked.
     */
    private void unlockDevice(String DSN) {
        try {
            explicitWait(5);
            if (getScreenWidth(DSN) == 600) {
                cmd.adb(DSN, cmd.ADB_SWIPE_SMALL_SCREEN);
            } else {
                cmd.adb(DSN, cmd.ADB_SWIPE_LARGE_SCREEN);
            }
            explicitWait(1);
            cmd.adb(DSN, cmd.ADB_INPUT_DEVICE_KEY);
            explicitWait(1);
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while unlocking the device: ", e);
        }
    }

    /**
     * Disables the screen lock of the device specified by the DSN (Device Serial Number).
     * This method disables the screen lock by executing an ADB (Android Debug Bridge) command
     * to keep the screen on. It logs the device model name and a message indicating that the
     * screen lock has been disabled. If an exception occurs during the process, it logs the error.
     *
     * @param DSN the Device Serial Number of the device whose screen lock is to be disabled.
     */
    public void disableScreenLock(String DSN) {
        try {
            cmd.adb(DSN, cmd.ADB_SCREEN_STAY_ON);
            log.info(getDeviceModelName(DSN) + ": Screen Lock Disabled");
            explicitWait(1);
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while Disabling Screen Lock: ", e);
        }
    }

    /**
     * Retrieves the minimum SDK version required by the specified app package on the device specified by the DSN (Device Serial Number).
     * This method checks if the device OS version is 9 or higher before proceeding to fetch the app's minimum SDK version.
     * It executes an ADB (Android Debug Bridge) command to obtain information about the specified app package,
     * including its minimum SDK version. If an exception occurs during the process, it logs the error.
     *
     * @param DSN        the Device Serial Number of the device where the app is installed.
     * @param appPackage the package name of the app whose minimum SDK version is to be fetched.
     * @return the minimum SDK version required by the app, or "NA" if an exception occurs or if the device OS version is less than 9.
     * if an exception occurs during any step, it logs the error..
     */
    public String getAppMinSDK(String DSN, String appPackage) throws IOException {
        BufferedReader reader = null;
        try {
            if (getDeviceOS(DSN) < 9) return "NA";
            reader = new BufferedReader(new InputStreamReader(cmd.adb(DSN,
                    cmd.ADB_DUMPSYS_PACKAGE + " " + appPackage + " " + cmd.GET_VERSION_CODE).getInputStream(),
                    StandardCharsets.UTF_8));
            String processReadLine = reader.readLine();
            processReadLine = processReadLine.replaceAll(" ", "");
            processReadLine = processReadLine.split("minSdk=")[1];
            processReadLine = processReadLine.split("targetSdk=")[0].trim();
            return processReadLine;
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while fetching Min SDK of App: ", e);
        } finally {
            if (reader != null) reader.close();
        }
        return "NA";
    }

    /**
     * Retrieves the target SDK version required by the specified app package on the device specified by the DSN (Device Serial Number).
     * This method checks if the device OS version is 9 or higher before proceeding to fetch the app's target SDK version.
     * It executes an ADB (Android Debug Bridge) command to obtain information about the specified app package,
     * including its target SDK version. If an exception occurs during the process, it logs the error.
     *
     * @param DSN        the Device Serial Number of the device where the app is installed.
     * @param appPackage the package name of the app whose target SDK version is to be fetched.
     * @return the target SDK version required by the app, or "NA" if an exception occurs.
     */
    public String getAppTargetSDK(String DSN, String appPackage) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.adb(DSN,
                cmd.ADB_DUMPSYS_PACKAGE + " " + appPackage + " " + cmd.GET_VERSION_CODE).getInputStream(),
                StandardCharsets.UTF_8))) {
            String processReadLine = reader.readLine();
            if (getDeviceOS(DSN) < 9) {
                processReadLine = processReadLine.replaceAll(" ", "");
                processReadLine = processReadLine.split("targetSdk=")[1].trim();
            } else {
                processReadLine = processReadLine.replaceAll(" ", "");
                processReadLine = processReadLine.split("minSdk=")[1];
                processReadLine = processReadLine.split("targetSdk=")[1].trim();
            }
            return processReadLine;
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while fetching Target SDK of App: ", e);
        }
        return "NA";
    }

    /**
     * Fetches the ADB logs for the specified device (identified by DSN) and appends them to the provided log file.
     * This method executes an ADB (Android Debug Bridge) command to fetch the logcat dump for the specified device.
     * It reads the output stream line by line and appends each line to the provided log file.
     * If an exception occurs during the process, it logs the error.
     *
     * @param logFile the File object representing the log file where the ADB logs will be appended.
     * @param DSN     the Device Serial Number of the device whose logs are to be fetched.
     */
    public void getApp_ADBLogsFile(File logFile, String DSN) {
        try (BufferedReader read = new BufferedReader(new InputStreamReader(cmd.adb(DSN,
                cmd.ADB_LOGCAT_DUMP).getInputStream(), StandardCharsets.UTF_8))) {
            fileWriter = new FileWriter(logFile, true);
            String processReadLine = null;
            while ((processReadLine = read.readLine()) != null) {
                fileWriter.write(processReadLine.trim() + "\n");
            }
            fileWriter.flush();
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while fetching logcat dump");
        }
    }

    /**
     * Retrieves the main activity intent of the specified app package installed on the device identified by the DSN (Device Serial Number).
     * This method executes an ADB (Android Debug Bridge) command to obtain information about the specified app package,
     * including its main activity intent. It parses the output to extract the main activity intent and returns it.
     * If the app is not installed on the device or if an exception occurs during the process, it logs the error.
     *
     * @param DSN        the Device Serial Number of the device where the app is installed.
     * @param appPackage the package name of the app whose main activity intent is to be retrieved.
     * @return the main activity intent of the app, or null if the app is not installed or if an exception occurs.
     */
    public String getAppIntent(String DSN, String appPackage) {
        try (BufferedReader read = new BufferedReader(new InputStreamReader(cmd.adb(DSN, cmd.ADB_PM_DUMP +
                appPackage + cmd.GET_ACTIVITY_MAIN).getInputStream(), StandardCharsets.UTF_8))) {
            String line = null;
            while ((line = read.readLine()) != null) {
                if (line.contains(appPackage + "/")) {
                    break;
                }
            }
            if (line == null) {
                log.error("App is not installed on device");
                return null;
            }
            line = line.split("/")[1];
            line = line.split(" ")[0];
            appIntent = appPackage + "/" + line;
            return appIntent;
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while fetching App Intent");
            return appIntent;
        }
    }

    /**
     * Retrieves the package name of the app installed on the device identified by the DSN (Device Serial Number).
     * This method executes an ADB (Android Debug Bridge) command to obtain a list of third-party packages installed on the device.
     * It filters out certain packages and returns the package name of the app.
     * If an exception occurs during the process, it logs the error.
     *
     * @param DSN the Device Serial Number of the device where the app is installed.
     * @return the package name of the installed app, or null if an exception occurs.
     * @throws IOException if an I/O error occurs while reading from the input stream.
     */
    public String getAppPackage(String DSN) throws IOException {
        BufferedReader read = null;
        try {
            if (DEVICE_TYPE.equals(TABLET)) {
                read = new BufferedReader(new InputStreamReader(cmd.adb(DSN,
                        cmd.ADB_LIST_PACKAGES_3P).getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = read.readLine()) != null) {
                    //For FOS 5 devices, there is a blank after each package line under package lists
                    if (line.equals("")) continue;

                    line = line.substring(8);
                    if (line.startsWith("io.appium") || line.contains("vysor") || line.contains("washingtonpost"))
                        continue;

                    else appPackage = line;
                    explicitWait(1);
                }
                return appPackage;
            } else if (DEVICE_TYPE.equals(FTV)) {
                read = new BufferedReader(new InputStreamReader(cmd.adb(DSN,
                        cmd.ADB_LIST_PACKAGES_3P).getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = read.readLine()) != null) {
                    //For FOS 5 devices, there is a blank after each package line under package lists
                    //log.info("Line Package: " , e + line);
                    if (line.isEmpty() || line.equals("")) {
                        continue;
                    }
                    line = line.substring(8);
                    if (line.contains("io.appium") || line.contains("com.nordvpn") || line.contains("vysor") ||
                            line.contains("com.expressvpn.vpn") || line.contains("com.surfshark.vpnclient.android") ||
                            line.contains("com.amazon.tablet.automaticoobe") ||
                            line.contains("com.amazon.kats.utils")) {
                        continue;
                    } else {
                        appPackage = line;
                    }
                    explicitWait(1);
                }
                return appPackage;
            } else {
                read = new BufferedReader(new InputStreamReader(cmd.adb(DSN,
                        cmd.ADB_LIST_PACKAGES_3P).getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = read.readLine()) != null) {
                    //For FOS 5 devices, there is a blank after each package line under package lists
                    if (line.equals("")) continue;

                    line = line.substring(8);
                    if (line.startsWith("io.appium") || line.contains("vysor") || line.contains("washingtonpost"))
                        continue;

                    else appPackage = line;
                    explicitWait(1);
                }
                return appPackage;
            }
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while fetching App Package: ", e);
            return null;
        } finally {
            read.close();
        }
    }

    /**
     * Sends the device identified by the DSN (Device Serial Number) to the home screen.
     * This method executes an ADB (Android Debug Bridge) command to simulate pressing the home button on the device.
     * It waits for a few seconds after sending the command to ensure that the action is completed.
     * If an exception occurs during the process, it logs the error.
     *
     * @param DSN the Device Serial Number of the device where the home action is to be performed.
     */
    public void goHome(String DSN) {
        try {
            cmd.adb(DSN, cmd.ADB_GO_HOME);
            explicitWait(5);
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while performing GoHome action: ", e);
        }
    }

    /**
     * Pauses the execution for the specified number of seconds.
     * This method pauses the execution of the current thread for the specified number of seconds.
     * It's primarily used to introduce delays in the code execution for synchronization purposes.
     * If an exception occurs during the waiting period, it logs the error.
     *
     * @param sec the number of seconds to wait.
     */
    public void waitInSec(int sec) {
        try {
            Thread.sleep(sec * 1000L);
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while performing wait: ", e);
        }
    }

    /**
     * Force stops the specified app on the device identified by the DSN (Device Serial Number).
     * This method executes an ADB (Android Debug Bridge) command to force stop the specified app.
     * It waits for a few seconds after sending the command to ensure that the action is completed.
     * If an exception occurs during the process, it logs the error.
     *
     * @param DSN        the Device Serial Number of the device where the app is installed.
     * @param appPackage the package name of the app to be force stopped.
     */
    public void forceStopApp(String DSN, String appPackage) {
        try {
            String deviceName = DEVICE_NAME;
            cmd.adb(DSN, cmd.ADB_FORCE_STOP + appPackage);
            log.info(deviceName + ": App force stopped - " + appPackage);
            explicitWait(5);
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception occurred while force stopping the app: ", e);
        }
    }

    /**
     * Enum representing available test types.
     * This enum defines three test constants: latency, cool, and warm.
     */
    enum availableTest {
        latency, cool, warm;
    }

    /**
     * Validates the provided test type against the available test types defined in the enum.
     * <p>
     * This method checks if the provided test type matches any of the available test types
     * defined in the enum `availableTest`. If a match is found, it returns true. Otherwise, it
     * sets the APP_FAILURE_REASON and returns false.
     *
     * @param testType the test type to be validated.
     * @return true if the test type is valid; false otherwise.
     */
    public boolean validateTestType(String testType) {
        try {
            for (availableTest test : availableTest.values()) {
                if (test.name().equals(testType)) return true;
            }

        } catch (Exception e) {
            log.error("Exception Occurred while sanitizing test type: ", e);
        }
        APP_FAILURE_REASON = "Identified Bad Input Test Config";
        return false;

    }

    /**
     * Validates the type of the device identified by the given Device Serial Number (DSN).
     * This method executes an ADB (Android Debug Bridge) command to determine the type of the device
     * identified by the provided DSN. It then sets the DEVICE_TYPE accordingly and logs the device type.
     * If the device type is unsupported or an exception occurs during the process, it sets the APP_FAILURE_REASON
     * and returns false.
     *
     * @param DSN the Device Serial Number of the device to be validated.
     * @return true if the device type is validated successfully; false otherwise.
     */
    public boolean validateDevice(String DSN) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader
                (cmd.adb(DSN, cmd.ADB_DEVICE_TYPE).getInputStream(), StandardCharsets.UTF_8))) {
            DeviceTools.DSN = DSN;
            String outputLines = br.readLine();
            switch (outputLines) {
                case "tv":
                    DeviceTools.DEVICE_TYPE = "ftv";
                    log.info("Device Type: " + DeviceTools.DEVICE_TYPE);
                    break;
                case "tablet":
                    DeviceTools.DEVICE_TYPE = "tablet";
                    log.info("Device Type: " + DeviceTools.DEVICE_TYPE);
                    break;
                default:
                    DeviceTools.DEVICE_TYPE = outputLines;
                    log.info("Unsupported Device type");
                    APP_FAILURE_REASON = "Unsupported Device type";
                    return false;
            }
        } catch (Exception e) {
            log.error("Exception occurred while Fetching Device Type: ", e);
        }
        return true;
    }

}


