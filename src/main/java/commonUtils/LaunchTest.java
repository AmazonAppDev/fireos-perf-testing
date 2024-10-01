package commonUtils;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LaunchTest extends CommonTools {

    private final Logger log = Logger.getLogger(LaunchTest.class.getSimpleName());

    /**
     * Checks if the specified app is currently in the foreground on the given device.
     *
     * @param DSN        The Device Serial Number (DSN) identifying the target device.
     * @param appPackage The package name of the app to check for in the foreground.
     * @return {@code true} if the app is in the foreground, {@code false} otherwise.
     */
    public Boolean checkAppForeground(String DSN, String appPackage) {
        try {
            if (getDeviceOS(DSN) <= 9) {
                String currentFocus;
                try (BufferedReader read = new BufferedReader(new InputStreamReader(cmd.adb(DSN, cmd.ADB_CURRENT_SCREEN_FOCUS).
                        getInputStream()))) {
                    while ((currentFocus = read.readLine()) != null) {
                        log.info(currentFocus);
                        if (currentFocus.contains(appPackage)) {
                            log.info("App Passed Launch Test");
                            return true;
                        }
                    }
                } catch (Exception e) {
                    log.error(DEVICE_NAME + ": Exception Occurred while performing launch test: ", e);
                }
            } else {
                String currentFocus;
                try (BufferedReader read = new BufferedReader(new InputStreamReader(cmd.adb(DSN, cmd.ADB_CURRENT_SCREEN_FOCUS_A11).
                        getInputStream()))) {
                    while ((currentFocus = read.readLine()) != null) {
                        log.info(currentFocus);
                        if (currentFocus.contains(appPackage)) {
                            log.info("App Passed Launch Test");
                            return true;
                        }
                    }
                } catch (Exception e) {
                    log.error(DEVICE_NAME + ": Exception Occurred while performing launch test: ", e);
                }
            }
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Error Occurred while performing App Launch Test: ", e);
            return false;
        }
        log.info("App Failed to launch");
        return false;
    }


    /**
     * Checks if the specified app is currently in the foreground on the given device
     * by examining the UI dump obtained using UI Automator.
     *
     * @param DSN        The Device Serial Number (DSN) identifying the target device.
     * @param appPackage The package name of the app to check for in the UI dump.
     * @return {@code true} if the app is in the foreground, {@code false} otherwise.
     */
    public Boolean checkAppForegroundUsingUi(String DSN, String appPackage) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.adb(DSN, cmd.UI_AUTOMATOR1_DUMP).
                getInputStream()))) {
            String appPackageCheck;
            while ((appPackageCheck = reader.readLine()) != null) {
                if (appPackageCheck.contains(appPackage)) {
                    log.info("App Launch Test Passed by Verifying App Package in UI Dump");
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while checking app package in Dump logs: ", e);
        }
        log.info("App Failed to Launch Verification using UI Dump Logs");
        return false;
    }

}
