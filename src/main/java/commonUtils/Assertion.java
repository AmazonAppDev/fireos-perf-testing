package commonUtils;

import org.testng.Assert;

public class Assertion {

    private String message = "";
    private static String resultMessage;

    /**
     * Returns an Assertion object representing a passing test result.
     *
     * @return An Assertion object indicating that the test has passed.
     */
    public static Assertion pass() {
        resultMessage = "Test Pass";
        Assert.assertTrue(true, resultMessage);
        Assertion result = new Assertion();
        result.message = resultMessage;
        return result;
    }

    /**
     * Returns an Assertion object representing a failing test result with the provided message.
     *
     * @param resultMessage The message indicating the reason for test failure.
     * @return An Assertion object indicating that the test has failed.
     */
    public static Assertion fail(String resultMessage) {
        Assertion result = new Assertion();
        result.message = resultMessage;
        Assert.fail(resultMessage);
        return result;
    }

}
