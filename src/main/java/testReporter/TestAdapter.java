package testReporter;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.util.Iterator;

public class TestAdapter extends TestListenerAdapter {
    Logger log = Logger.getLogger(TestAdapter.class.getSimpleName());

    /**
     * This method is invoked after all the tests in the specified context have run.
     * It iterates through the skipped test cases and removes duplicates, ensuring that
     * each test method only appears once in the skipped tests list.
     *
     * @param context the test context containing all the information for a test run.
     */
    @Override
    public void onFinish(ITestContext context) {
        Iterator<ITestResult> skippedTestCases = context.getSkippedTests().getAllResults().iterator();
        while (skippedTestCases.hasNext()) {
            ITestResult skippedTestCase = skippedTestCases.next();
            ITestNGMethod method = skippedTestCase.getMethod();
            if (context.getSkippedTests().getResults(method).size() > 0) {
                log.info("Removing:" + skippedTestCase.getTestClass().toString());
                skippedTestCases.remove();
            }
        }
    }
}
