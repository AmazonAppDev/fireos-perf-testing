package commonUtils;

import dataFlow.TestKpiDataWrite;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DataUtils extends KpiUtils {

    Logger log = Logger.getLogger(DataUtils.class.getSimpleName());
    public double average, tp50Value, tp90Value, displayedAverage, averageFinal;
    public int iterations = Integer.parseInt(prop.getProperty("latencyIterations"));
    public int extraIterations = (Integer.parseInt(prop.getProperty("extraIterations")));
    public double[] timerArr = new double[iterations];
    public double[] displayedArr = new double[iterations];
    public double[] memUsage = new double[iterations];
    public double[] cpuUsage = new double[iterations];
    public String cpuConsumption, memConsumption;

    StringBuilder valuesPerIterateFinal = new StringBuilder("[");

    public int waitTime = (Integer.parseInt(prop.getProperty("latencyWait")));

    /**
     * Calculates the median of an array of double values.
     *
     * @param arr The array of double values.
     * @return The median value of the array.
     */
    public double calculateMedian(double[] arr) {
        try {
            Arrays.sort(arr);
            double mid = 0;
            if (arr.length % 2 == 0) {
                int temp = (arr.length / 2) - 1;
                for (int i = 0; i < arr.length; i++) {
                    if (temp == i || (temp + 1) == i) {
                        mid = mid + arr[i];
                    }
                }
                mid = mid / 2;
            } else {
                int temp = (arr.length / 2);
                for (int i = 0; i < arr.length; i++) {
                    if (temp == i) {
                        mid = arr[i];

                    }
                }
            }
            return mid;
        } catch (Exception e) {
            log.error("Exception occurred while calculating Median: ", e);
        }
        return 0;
    }

    public double calculateMode(double[] arr) {
        try {
            Map<Double, Integer> valueMap = new HashMap<>();
            for (double element : arr) {
                valueMap.merge(element, 1, Integer::sum);
            }

            // Find the element with the highest frequency
            int maxCount = 0;
            double mode = 0.0;
            for (Map.Entry<Double, Integer> entry : valueMap.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    mode = entry.getKey();
                }
            }
            return mode;
        } catch (Exception e) {
            log.error("Exception occurred while calculating Mode: ", e);
        }
        return 0;
    }

    /**
     * Calculates the mode of an array of double values.
     *
     * @param arr The array of double values.
     * @return The mode value of the array.
     */
    public double calculatePercentile(double[] arr, double percentile) {
        try {
            Arrays.sort(arr);
            int index = (int) Math.ceil(percentile / 100.0 * arr.length);
            return arr[index - 1];
        } catch (Exception e) {
            log.error("Exception occurred while calculating percentile: ", e);
        }
        return 0;
    }

    /**
     * Writes latency values to a CSV file and logs relevant metrics.
     *
     * @param appVersion   The version of the application.
     * @param DSN          The Device Serial Number.
     * @param metricsName  The name of the metrics.
     */
    public void writeLatencyValues(
            String appVersion,
            String DSN,
            String metricsName) {

        try {

            TestKpiDataWrite csvWrite = new TestKpiDataWrite();
            double dtp50 = 0, dtp90 = 0, tp50 = 0, tp90 = 0, sum = 0, displayedSum = 0;

            StringBuilder valuesPerIterate = new StringBuilder("[");
            StringBuilder displayedValuesPerIterate = new StringBuilder("[");

            int lossCount = 0, displayedLossCount = 0;
            if(timerArr.length !=0) {
                for (int i = 0; i < timerArr.length; i++) {

                    if (timerArr[i] != 0) {
                        if (i == (iterations - 1)) {
                            valuesPerIterate.append(timerArr[i]);
                            sum = sum + timerArr[i];
                        } else if (i < (iterations - 1)) {
                            valuesPerIterate.append(timerArr[i]).append(";");
                            sum = sum + timerArr[i];
                        } else log.info(DEVICE_NAME + ": Extra Timer Values: " + timerArr[i]);

                    } else {
                        lossCount++;
                    }
                }
            }

            if (displayedArr.length != 0) {
                for (int j = 0; j < displayedArr.length; j++) {
                    if (displayedArr[j] != 0) {
                        if (j == (iterations - 1)) {
                            displayedValuesPerIterate.append(displayedArr[j]);
                            displayedSum = displayedSum + displayedArr[j];
                        } else if (j < (iterations - 1)) {
                            displayedValuesPerIterate.append(displayedArr[j]).append(";");
                            displayedSum = displayedSum + displayedArr[j];
                        } else log.info(DEVICE_NAME + ": Extra Timer Values: " + displayedArr[j]);
                    } else {
                        displayedLossCount++;
                    }
                }
            }

            if (lossCount > 0)
                log.warn(DEVICE_NAME + ": Calculating Average/Mean for only " + (iterations - lossCount) + " values");
            valuesPerIterate.append("]");
            displayedValuesPerIterate.append("]");


            double[] timerNewArr = Arrays.copyOf(Arrays.stream(timerArr)
                            .filter(a -> a != 0)
                            .toArray(),
                    iterations - lossCount);

            if (timerNewArr.length > 0) {
                tp50 = calculatePercentile(timerNewArr, 50);
                tp90 = calculatePercentile(timerNewArr, 90);
            }

            double[] displayedNewArr = Arrays.copyOf(Arrays.stream(displayedArr)
                            .filter(a -> a != 0)
                            .toArray(),
                    iterations - displayedLossCount);

            if (displayedNewArr.length > 0) {
                dtp50 = calculatePercentile(displayedNewArr, 50);
                dtp90 = calculatePercentile(displayedNewArr, 90);
            }

            double median = 0.0, mode = 0.0;
            if (dtp50 != 0 && tp50 != 0) {
                displayedAverage = Double.parseDouble(df2.format(displayedSum / (iterations -
                        displayedLossCount)));
                median = calculateMedian(displayedNewArr);
                mode = calculateMode(displayedNewArr);
                tp50Value = dtp50;
                tp90Value = dtp90;
                valuesPerIterateFinal = displayedValuesPerIterate;
                averageFinal = displayedAverage;
            } else if (tp50 != 0 & dtp50 == 0) {
                average = Double.parseDouble(df2.format(sum / (iterations - lossCount)));
                median = calculateMedian(timerNewArr);
                mode = calculateMode(timerNewArr);
                tp50Value = tp50;
                tp90Value = tp90;
                valuesPerIterateFinal = valuesPerIterate;
                averageFinal = average;
            } else if (dtp50 != 0) {
                displayedAverage = Double.parseDouble(df2.format(displayedSum / (iterations -
                        displayedLossCount)));
                median = calculateMedian(displayedNewArr);
                mode = calculateMode(displayedNewArr);
                tp50Value = dtp50;
                tp90Value = dtp90;
                valuesPerIterateFinal = displayedValuesPerIterate;
                averageFinal = displayedAverage;
            }

            if (tp50Value != 0) {
                String tp50_Memory = calculatePercentile(memUsage, 50) + " MB";
                String tp50_CpuUsage = calculatePercentile(cpuUsage, 90) + "%";

                log.info(DEVICE_NAME + ": Metrics Name in Logs: " + metricsName);
                if (displayedSum != 0) {
                    log.info(DEVICE_NAME + ": Displayed Values Per Iteration: " + displayedValuesPerIterate);
                    log.info(DEVICE_NAME + ": Displayed Average Time: " + displayedAverage);
                    log.info(DEVICE_NAME + ": Displayed TP50: " + dtp50);
                    log.info(DEVICE_NAME + ": Displayed TP90: " + dtp90);
                    log.info(DEVICE_NAME + ": Displayed Median Time: " + median);
                    log.info(DEVICE_NAME + ": Displayed Mode Time: " + mode);
                } else {
                    log.info("Displayed Marker/ RTU Marker not present");
                    log.info(DEVICE_NAME + ": Values Per Iteration Vitals: " + valuesPerIterate);
                    log.info(DEVICE_NAME + ": Average Time: " + average);
                    log.info(DEVICE_NAME + ": Median Time: " + median);
                    log.info(DEVICE_NAME + ": Mode Time: " + mode);
                    log.info(DEVICE_NAME + ": TP50 Value Vitals: " + tp50);
                    log.info(DEVICE_NAME + ": TP90 Value Vitals: " + tp90);
                }
                log.info(DEVICE_NAME + ": Total Device Ram Consumption: " + tp50_Memory);
                log.info(DEVICE_NAME + ": Total Device CPU Consumption: " + tp50_CpuUsage);
                csvWrite.writeToCSV(DSN, appVersion, metricsName, valuesPerIterateFinal.toString(), tp50_Memory,
                        tp50_CpuUsage, iterations, averageFinal, tp50Value, tp90Value);
            } else {
                csvWrite.writeToCSV(DSN, appVersion, metricsName, "NA", "NA",
                        "NA", 0, 0.0, 0, 0);
            }

            log.info(DEVICE_NAME + ": Execution Completed");

            timerArr = new double[iterations];
            memUsage = new double[iterations];
            cpuUsage = new double[iterations];
        } catch (Exception e) {
            log.error(DEVICE_NAME + ": Exception while calculating average timer values", e);
        }
    }

    /**
     * Retrieves the KPI ID based on the provided KPI metrics name.
     *
     * @param KPI_METRICS_NAME The name of the KPI metrics.
     * @return The ID corresponding to the provided KPI metrics name. Returns 0 if no matching KPI metrics name is found.
     */
    public int getKpiId(String KPI_METRICS_NAME) {
        int kpiId = 0;
        switch (KPI_METRICS_NAME) {
            case "Cool_FF":
                kpiId = 1;
                break;
            case "Warm_FF":
                kpiId = 2;
                break;
            default:
                break;
        }
        return kpiId;
    }

    /**
     * Empties the existing arrays and resets the TP50 value.
     * This method sets all elements of the displayedArr and timerArr arrays to 0.0 and resets the tp50Value to 0.0.
     */
    public void emptyExistingArray() {
        if (displayedArr != null) Arrays.fill(displayedArr, 0.0);
        if (timerArr != null) Arrays.fill(timerArr, 0.0);
        if (tp50Value != 0.0) tp50Value = 0.0;
    }
}
