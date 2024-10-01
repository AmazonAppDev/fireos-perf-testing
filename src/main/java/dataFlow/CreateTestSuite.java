package dataFlow;

import commonUtils.CommonTools;
import commonUtils.DeviceTools;
import commonUtils.ReadPaths;
import org.apache.log4j.Logger;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateTestSuite {
    CommonTools commonTools = new CommonTools();
    ReadPaths rp = new ReadPaths();
    private static final Logger log = Logger.getLogger(CreateTestSuite.class.getSimpleName());
    private final long timeOut = Long.parseLong(rp.prop.getProperty("timeout"));
    List<String> groups = new ArrayList<>();

    /**
     * Creates a TestNG XML file for executing performance tests based on the specified KPI type and device serial number (DSN).
     * Adds the specified KPI type as a test group.
     * Sets up the TestNG configuration for the performance test suite, including listeners, test names, thread count, parameters, classes, and included groups.
     * Determines the appropriate test class based on the device type (tablet or Fire TV).
     * Generates and executes the TestNG XML suite for the performance test.
     * Logs relevant information and any exceptions that occur during the process.
     *
     * @param kpiType The type of Key Performance Indicator (KPI) for the test.
     * @param DSN     The serial number of the device on which the test will be executed.
     */
    public void createXMLFile(String kpiType, String DSN) {
        try {
            DeviceTools.KPI_TYPE = kpiType;
            groups.add(kpiType);

            TestNG myTestNG = new TestNG();
            XmlSuite mySuite = new XmlSuite();

            mySuite.setName("Performance Test");
            mySuite.addListener("testReporter.ExtentReporter");

            XmlTest myTest;
            List<XmlTest> myTests = new ArrayList<XmlTest>();
            HashMap<String, String> testngParams;
            XmlClass myClass;

            String className = null;

            if (ReadPaths.DEVICE_TYPE.equals(ReadPaths.TABLET)) {
                className = "perfTabKpis.PerfTestKPIs.TestKpiExecutorTablets";
            } else if (ReadPaths.DEVICE_TYPE.equals(ReadPaths.FTV)) {
                className = "perfTVKpis.PerfTestKPIsFTV.TestKpiExecutorFtv";
            }

            myTest = new XmlTest(mySuite.shallowCopy());
            String deviceName = commonTools.getDeviceName(DSN);
            log.info("Adding Test Name in XML for device: " + deviceName + " : " + DSN);

            myTest.setName("Test " + deviceName + " : " + DSN);
            myTest.setThreadCount(100);
            myTest.setIncludedGroups(groups);
            testngParams = new HashMap<>();
            testngParams.put("DSN", DSN);
            myTest.setParameters(testngParams);
            myClass = new XmlClass(className);
            myTest.getClasses().add(myClass);
            myTests.add(myTest);

            mySuite.setTests(myTests);
            List<XmlSuite> mySuites = new ArrayList<XmlSuite>();
            mySuites.add(mySuite);

            myTestNG.setXmlSuites(mySuites);
            mySuite.setFileName("perfTestSuite.xml");
            mySuite.setParallel(XmlSuite.ParallelMode.TESTS);
            log.info("Parallel Test Mode Enabled");
            mySuite.setTimeOut(String.valueOf(timeOut));
            log.info("Test Time Out after: " + (timeOut / 3600000) + " hrs");
            for (XmlSuite suite : mySuites) createXmlFileAbs(suite);
            log.info("XML File generated successfully...");
            myTestNG.run();
        } catch (Exception e) {
            log.error("Exception occurred while executing XML Test Suite", e);
        }
    }

    /**
     * Creates an XML file from the given XmlSuite object and writes it to the specified file path.
     *
     * @param mSuite The XmlSuite object to be converted to XML.
     */
    private void createXmlFileAbs(XmlSuite mSuite) {

        try (FileWriter writer = new FileWriter(commonTools.testSuite)) {
            writer.write(mSuite.toXml());
            writer.flush();
            writer.close();
            log.info("XML File: " + new File("perfTestSuite.xml").getAbsolutePath());
        } catch (IOException e) {
            log.error("IO Exception occurred while creating xml", e);
        }
    }
}