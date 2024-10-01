package perfTVKpis.PerfTestKPIsFTV;

import commonUtils.CommonTools;
import commonUtils.DeviceTools;
import org.testng.annotations.Listeners;
import org.testng.annotations.Parameters;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import perfTVKpis.LatencyTestTV.LatencyKPI;

import java.io.IOException;

@Listeners(testReporter.TestAdapter.class)
public class TestKpiExecutorFtv extends CommonTools {
    LatencyKPI latencyKPI = new LatencyKPI();

    /**
     * Sets up the performance test suite before running tests.
     * This method is annotated with @BeforeClass, indicating that it should be executed before the test class is initialized.
     * It takes the Device Serial Number (DSN) as a parameter, retrieved from the test suite configuration.
     * The method initializes DeviceTools properties such as DSN, DEVICE_NAME, and APP_PACKAGE_INPUT.
     * It then performs setup tasks for KPI tests and common test prerequisites using the provided DSN.
     *
     * @param DSN the Device Serial Number (DSN) of the device to be used for the performance test suite.
     * @throws IOException if an I/O exception occurs while performing setup tasks.
     */
    @BeforeClass(alwaysRun = true)
    @Parameters({"DSN"})
    public void setup_PerfTestSuite(String DSN) throws IOException {
        DeviceTools.DSN = DSN;
        DeviceTools.DEVICE_NAME = getDeviceName(DSN);
        DeviceTools.APP_PACKAGE_INPUT = getAppPackageFromAPK(prop.getProperty("apk_name"));
        kpiTestSetup(DSN);
        commonTestPreRequisites(DSN);
    }

    /**
     * Tests the latency for the first frame after launching the "Cool" application.
     * This test method is annotated with @Test, indicating that it is a test method to be executed by the test framework.
     * It has a priority of 1 and belongs to the groups "all", "latency", and "cool".
     * The method executes the latency test for the first frame after launching the "Cool" application on the device identified by the provided DSN.
     * After the test is completed, it forcefully stops the application.
     *
     * @throws IOException if an I/O exception occurs while running the test.
     */
    @Test(priority = 1, groups = {"all", "latency", "cool"})
    public void Cool_Latency_FirstFrame() throws IOException {
        latencyKPI.runLatencyTest(DSN, "Cool_FF");
        forceStopApp(DSN, getAppPackage(DSN));
    }

    /**
     * Tests the latency for the first frame after launching the "Warm" application.
     * This test method is annotated with @Test, indicating that it is a test method to be executed by the test framework.
     * It has a priority of 2 and belongs to the groups "all", "latency", and "warm".
     * The method executes the latency test for the first frame after launching the "Warm" application on the device identified by the provided DSN.
     * After the test is completed, it forcefully stops the application.
     *
     * @throws IOException if an I/O exception occurs while running the test.
     */
    @Test(priority = 2, groups = {"all", "latency", "warm"})
    public void Warm_Latency_FirstFrame() throws IOException {
        latencyKPI.runLatencyTest(DSN, "Warm_FF");
        forceStopApp(DSN, getAppPackage(DSN));
    }

    /**
     * Uninstalls all third-party applications after the test suite execution.
     * This method is annotated with @AfterSuite, indicating that it should be executed after all tests in the suite have been run.
     * It takes the Device Serial Number (DSN) as a parameter, retrieved from the test suite configuration.
     * The method invokes the uninstallAll3PApps method to uninstall all third-party applications from the device identified by the provided DSN.
     *
     * @param DSN the Device Serial Number (DSN) of the device from which third-party applications will be uninstalled.
     */
    @AfterSuite(alwaysRun = true)
    @Parameters({"DSN"})
    public void uninstall(String DSN) {
        uninstallAll3PApps(DSN);
    }

}
