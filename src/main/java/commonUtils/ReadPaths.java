package commonUtils;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class ReadPaths {

    public String apkPath, kpiLog, reportPath, outputPath, testSuite, extentReportXml, adbLogs, logZipPath,
            appPackage = null, appIntent = null;
    public String kpi_values_csv;
    public static String FTV = "ftv", TABLET = "tablet";
    public Properties prop;
    public static String DEVICE_TYPE;
    private final Logger log = Logger.getLogger(ReadPaths.class.getSimpleName());
    public final String USER_DIR = System.getProperty("user.dir");
    private final String COMMON_CONFIG_PATH = "/Input/Resources/commonconfig.properties";
    private final String LOG_PATH_JAR = "/Input/Resources/log4j.properties";
    public static String CSV_HEADERS = "KPI_ID," + "APP_PACKAGE," + "APP_VERSION," + "KPI_TYPE," + "KPI_METRICS_NAME,"
            + "KPI_ITERATION_WISE_VALUES," + "KPI_AVERAGE_VALUE," + "KPI_TP50_VALUE," + "KPI_TP90_VALUE,"
            + "TEST_DEVICE_NAME," + "TEST_DEVICE_FOS_VERSION," + "TEST_DEVICE_RAM_USED," + "TEST_DEVICE_CPU_USED,"
            + "ITERATIONS_EXECUTED," + "TEST_EXECUTION_DATE," + "FAILURE_REASON," + "\n";
    public String COOL_APP = "cool_app_launch_time";
    public String COOL_ACTIVITY = "cool_activity_launch_time";
    public String WARM_APP_WARM = "warm_app_warm_transition_launch_time";
    public String WARM_APP_COOL = "warm_app_cool_transition_launch_time";
    public String AM_ACTIVITY_LAUNCH_TIME = "am_activity_launch_time:";
    public String WM_ACTIVITY_LAUNCH_TIME = "wm_activity_launch_time:";
    public String WARM_ACTIVITY_LAUNCH_TIME = "performance:warm_activity_launch_time:";
    public String AM_FULLY_DRAWN = "am_activity_fully_drawn_time:";
    public String WM_FULLY_DRAWN = "wm_activity_fully_drawn_time:";

    public String COOL_APP_FF = "Cool_FF";
    public String WARM_APP_FF = "Warm_FF";
    public static boolean fullyDrawnMarker = false;

    public static String DSN, KPI_TYPE, APP_VERSION, DEVICE_NAME, APP_PACKAGE_INPUT = null, APP_FAILURE_REASON, metricsName;

    /**
     * Reads the paths from the configuration file and initializes the paths used in the test suite.
     * Initializes various paths such as extent report XML, APK path, ADB logs path, KPI log path, log zip path,
     * report path, test suite path, output path, and KPI values CSV path.
     * If any exception occurs during the process, it logs the error.
     */
    public ReadPaths() {
        File commonFile = new File(USER_DIR + COMMON_CONFIG_PATH);
        try (FileInputStream inputStream = new FileInputStream(commonFile)) {
            prop = new Properties();
            prop.load(inputStream);

            try (var logInputStream = Files.newInputStream(Paths.get(USER_DIR + LOG_PATH_JAR))) {
                PropertyConfigurator.configure(logInputStream);
            }

            extentReportXml = USER_DIR + prop.getProperty("extentXMLConfigJar");
            apkPath = USER_DIR + prop.getProperty("apk_local");
            adbLogs = USER_DIR + prop.getProperty("adb_Log_Path_Local");
            kpiLog = USER_DIR + prop.getProperty("log_Path_Local");
            logZipPath = USER_DIR + prop.getProperty("log_zip_Local");
            reportPath = USER_DIR + prop.getProperty("report_Path_Local");
            testSuite = USER_DIR + prop.getProperty("perfXml_Local");
            outputPath = USER_DIR + prop.getProperty("output_local");
            kpi_values_csv = USER_DIR + prop.getProperty("kpi_values_Local");

        } catch (Exception e) {
            log.error("Exception occurred while reading properties/paths: ", e);
        }
    }
}
