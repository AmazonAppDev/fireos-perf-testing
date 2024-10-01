package commonUtils;


import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FtvScenarios extends CommonTools {
    private final Logger log = Logger.getLogger(FtvScenarios.class.getSimpleName());

    /**
     * Retrieves the account ID of the device using the provided DSN.
     * This method executes an account verification check on the device identified by the given DSN.
     * If the device is not logged in with any account, it returns "null".
     * If the device is logged in with an account, it retrieves and returns the account ID.
     *
     * @param DSN The Device Serial Number (DSN) of the device.
     * @return The account ID of the device, or "null" if the device is not logged in with any account.
     */
    public String getDeviceAccount(String DSN) {
        try {
            String accountID = null;
            log.info("Executing Account Verification Check");
            BufferedReader br = new BufferedReader(new InputStreamReader
                    (cmd.adb(DSN, cmd.GET_USERINFO).getInputStream()));
            String outputLines = br.readLine();
            if (outputLines == null) {
                log.info("Device is not logged in with any account");
                return "null";
            } else if (outputLines.contains("UserInfo")) {
                accountID = outputLines.split(":")[1].split(":")[0].trim();
            }
            log.info("Current logged in Account : " + accountID);
            return accountID;
        } catch (Exception e) {
            log.info(getDeviceAccount(DSN) + ":Exception occurred while fetching Device Account: ", e);
        }
        return null;
    }

    /**
     * Enables the screen awake feature for the device identified by the provided DSN.
     * This method uses adb to keep the screen always awake for the device identified by the given DSN.
     *
     * @param DSN The Device Serial Number (DSN) of the device.
     */
    public void enableScreenAwake(String DSN) {
        try {
            cmd.adb(DSN, cmd.ADB_SCREEN_STAY_ON);
            log.info("Screen sleep disabled");
            explicitWait(1);
        } catch (Exception e) {
            log.error(getDeviceAccount(DSN) + ": Exception occurred while turning always ON: ", e);
        }
    }

}
