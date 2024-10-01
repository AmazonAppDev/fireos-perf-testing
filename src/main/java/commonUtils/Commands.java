package commonUtils;

import org.apache.log4j.Logger;

public class Commands {

    private final Logger log = Logger.getLogger(Commands.class.getSimpleName());

    public final String ADB_SERIAL = "adb -s ";

    // ADB commands to fetch app level details
    public final String ADB_LIST_PACKAGES_3P = " shell pm list packages -3 ";
    public final String GET_ACTIVITY_MAIN = " | grep -A 1 MAIN";
    public final String AAPT_DUMP = "aapt dump badging ";
    public final String PACKAGE_NAME = " | grep package:\\ name";
    public final String UI_AUTOMATOR1_DUMP = " exec-out uiautomator dump /dev/tty";
    public final String ADB_DUMPSYS_PACKAGE = " shell dumpsys package ";
    public final String GET_VERSION_CODE = " | grep versionCode ";
    public final String GET_VERSION_NO = " | grep versionName ";
    public final String ADB_CURRENT_SCREEN_FOCUS = " shell dumpsys window windows | grep -E mCurrentFocus";

    // ADB command to perform app level interactions
    public final String ADB_START_ACTIVITY = " shell am start -n ";
    public final String ADB_START_ACTIVITY_COLD = " shell am start-activity -W -n ";
    public final String ADB_START_ACTIVITY_HOT = " shell am start-activity -W -R ";
    public final String ADB_MONKEY_TOOL = " shell monkey -p ";
    public final String ADB_MONKEY_TOOL_FTV = " shell monkey --pct-syskeys 0 -p ";
    public final String ADB_MONKEY_LAUNCH_INTENT = " -c android.intent.category.LAUNCHER 1";
    public final String ADB_FORCE_STOP = " shell am force-stop ";
    public final String ADB_UNINSTALL = " uninstall ";
    public final String ADB_INSTALL = " install -r ";

    // ADB command to fetch performance details
    public final String ADB_MEMORY_INFO = " shell dumpsys -t 60 meminfo ";
    public final String ADB_MEMORY_INFO_FOS7 = " shell dumpsys -t 900 meminfo";
    public final String ADB_CPU_INFO = " shell dumpsys -t 60 cpuinfo ";
    public final String ADB_MEMORY_INFO_FOS5 = " shell dumpsys meminfo ";
    public final String ADB_CPU_INFO_FOS5 = " shell dumpsys cpuinfo ";

    //ADB command to capture logs
    public final String ADB_LOGCAT_CLEAR = " logcat -v threadtime -b all -c";
    public final String ADB_LOGCAT_DUMP = " logcat -v threadtime -b all -d";
    public final String ADB_CLEAR_LOGCAT_VITALS = " logcat -b vitals -c";
    public final String ADB_DUMP_LOGCAT_VITALS = " logcat -b vitals -d ";

    //ADB command to interact with device
    public final String ADB_GO_HOME = " shell input keyevent KEYCODE_HOME";
    public final String ADB_GO_BACK = " shell input keyevent 4";
    public final String ADB_DEVICE_LOCK_UNLOCK = " shell input keyevent 26";
    public final String ADB_DEVICE_SCREEN_SIZE = " shell wm size";
    public final String ADB_SWIPE_SMALL_SCREEN = " shell input touchscreen swipe 400 500 600 500";
    public final String ADB_SWIPE_LARGE_SCREEN = " shell input touchscreen swipe 630 580 630 180";
    public final String ADB_INPUT_DEVICE_KEY = " shell input text 1111";
    public final String ADB_GET_GAME_MODE = " shell settings get global game_mode";
    public final String ADB_DISABLE_GAME_MODE = " shell settings put global game_mode 0";
    public final String ADB_SHELL_REBOOT = " shell reboot";
    public final String ADB_SCREEN_STAY_ON = " shell svc power stayon true";
    public final String ADB_CONNECT = "adb connect ";

    //ADB command to fetch device level information
    public final String ADB_DEVICE_OS = " shell getprop ro.build.version.release";
    public final String ADB_DEVICE_TYPE = " shell getprop ro.build.configuration";
    public final String ADB_DEVICE_TYPE_WSA = " shell getprop ro.build.product";
    public final String ADB_DEVICE_NAME = " shell getprop ro.product.name";
    public final String ADB_DEVICE_MODEL = " shell getprop ro.product.model";
    public final String ADB_DEVICE_FOS_VERSION = " shell getprop ro.build.version.name";
    public final String ADB_DEVICE_BATTERY = " shell dumpsys battery | grep level";
    public final String ADB_CURRENT_SCREEN_FOCUS_A11 = " shell dumpsys window windows | grep -E mHoldScreenWindow";
    public final String ADB_CHECK_A11_LOCK_STATUS = " shell dumpsys window windows | grep -E mObscuringWindow";
    public final String ADB_SCREEN_STATE = " shell dumpsys display | grep mScreenState";
    public final String ADB_PM_DUMP = " shell pm dump ";
    public final String ADB_GREP = " | grep ";
    public final String GET_USERINFO = " shell pm list users | grep UserInfo";
    public final String GET_PID = " shell pidof ";


    public Process process;

    /**
     * Executes an adb command with the provided device serial number and message.
     *
     * @param DSN     The device serial number.
     * @param message The adb command message to execute.
     * @return A Process object representing the adb process.
     */
    public Process adb(String DSN, String message) {
        try {
            process = Runtime.getRuntime().exec(ADB_SERIAL + DSN + message);
        } catch (Exception e) {
            log.error("Exception occurred while executing adb commands", e);
        }
        return process;
    }

    /**
     * Connects a device to adb using the provided IP address.
     *
     * @param ip The IP address of the device.
     * @return A Process object representing the adb connect process.
     */
    public Process adbConnect(String ip) {
        try {
            process = Runtime.getRuntime().exec(ADB_CONNECT + ip);
        } catch (Exception e) {
            log.info("Exception occurred while connecting device through IP Address", e);
        }
        return process;
    }

}
