package testUtils;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.Reporter;

import commonUtils.ReadPaths;

import java.util.List;

public class RetryTest implements IRetryAnalyzer {

    ReadPaths rp = new ReadPaths();

    public int retryCount = Integer.parseInt(rp.prop.getProperty("retryCount"));
    private final int maxRetryCount = Integer.parseInt(rp.prop.getProperty("maxRetryCount"));

    /**
     * This method determines whether a test should be retried based on specific conditions.
     * It checks if the test name contains "Memory" and if the reporter message contains
     * "Memory Test failed". If both conditions are met, the test is not retried.
     * Otherwise, it retries the test up to a maximum retry count.
     *
     * @param result the result of the test that is being considered for retry.
     * @return true if the test should be retried, false otherwise.
     */
    public boolean retry(ITestResult result) {
        if (result.getName().contains("Memory") &&
                getReporterMsg(result).contains("Memory Test failed")) {
            return false;
        }
        if (retryCount < maxRetryCount) {
            System.out.println("Retrying performance test " +
                    result.getName() + " with status " +
                    getResultStatusName(result.getStatus()) + " for " +
                    (retryCount + 1) + " out of " + maxRetryCount + " times");
            retryCount++;
            return true;
        }
        return false;
    }

    /**
     * This method returns the status name corresponding to the given status code.
     * The status codes and their corresponding names are:
     * 1 - "SUCCESS"
     * 2 - "FAILURE"
     * 3 - "SKIP"
     *
     * @param status the status code of the test result.
     * @return the name of the status corresponding to the given status code.
     */
    public String getResultStatusName(int status) {
        String resultName = null;
        if (status == 1)
            resultName = "SUCCESS";
        if (status == 2)
            resultName = "FAILURE";
        if (status == 3)
            resultName = "SKIP";
        return resultName;
    }

    /**
     * This method retrieves the reporter messages associated with the given test result.
     * It concatenates all messages from the reporter's output into a single string.
     *
     * @param tr the test result from which to retrieve the reporter messages.
     * @return a concatenated string of all reporter messages for the given test result.
     */
    private String getReporterMsg(ITestResult tr) {
        String reporterMessage = "";
        List<String> reporterMessageList = Reporter.getOutput(tr);
        for (String tmpMsg : reporterMessageList) {
            reporterMessage += tmpMsg;
            reporterMessage += " ";
        }
        return reporterMessage;
    }

}
	
