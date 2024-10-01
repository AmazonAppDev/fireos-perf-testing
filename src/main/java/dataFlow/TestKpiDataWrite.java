package dataFlow;

import commonUtils.CommonTools;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestKpiDataWrite extends CommonTools {
    private final Logger log = Logger.getLogger(TestKpiDataWrite.class.getSimpleName());
    File file = new File(kpi_values_csv);
    FileWriter fileWriter;
    final String COMMA_DELIMITER = ",";
    final String NEW_LINE_SEPARATOR = "\n";
    public String date = new SimpleDateFormat("MM-dd-yyyy", Locale.ROOT).format(new Date());

    /**
     * Writes performance metrics data to a CSV file.
     *
     * @param DSN                The Device Serial Number (DSN) identifying the target device.
     * @param appVersion         The version of the application being tested.
     * @param metricsName        The name of the performance metric being recorded.
     * @param valuesPerIteration The values per iteration of the metric.
     * @param ramConsumed        The RAM consumed during the test.
     * @param cpuConsumed        The CPU consumed during the test.
     * @param iterations         The number of iterations of the test.
     * @param average            The average value of the metric.
     * @param tp50               The 50th percentile value of the metric.
     * @param tp90               The 90th percentile value of the metric.
     */
    public void writeToCSV(String DSN,
                           String appVersion,
                           String metricsName,
                           String valuesPerIteration,
                           String ramConsumed,
                           String cpuConsumed,
                           int iterations,
                           double average,
                           double tp50,
                           double tp90) {
        try {

            String TestType = "LATENCY";

            fileWriter = new FileWriter(file, true);
            fileWriter.append(String.valueOf(getKpiId(metricsName)));
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(APP_PACKAGE_INPUT);
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(appVersion);
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(TestType);
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(metricsName);
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(valuesPerIteration);
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(String.valueOf(average));
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(String.valueOf(tp50));
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(String.valueOf(tp90));
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(getDeviceName(DSN));
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(getFireOSBuild(DSN));
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(ramConsumed);
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(cpuConsumed);
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(String.valueOf(iterations));
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(date);
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(APP_FAILURE_REASON);
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(NEW_LINE_SEPARATOR);
        } catch (Exception e) {
            log.error("Exception occurred while writing kpi data csv: ", e);
        } finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
                log.error("Exception occurred while writing kpi data csv: ", e);
            }
        }
    }
}
