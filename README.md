# 📊🚀 FOS Performance Testing - First Frame KPI

A performance testing framework for measuring First Frame (Time to Initial Display) values of Fire OS apps on Amazon Fire tablets and Fire TVs.

## 🌟 Features

- 📊 Performance metrics: First Frame (TTID) values for both Cool and Warm Launch Scenario
- 📈 Calculation of Tp50, Tp90, and average values of the execution
- 📝 Log storage for further debugging

## 📋 Prerequisites

Before you begin, ensure you have the following installed:
- [Java 8 or above](https://www.java.com/)
- [Android SDK (API Level 34)](https://developer.android.com/studio)
- [Maven](https://maven.apache.org/)

And remember to disable the screensaver on the Test Device.

## 🚀 Quick Start

1. Clone the repository:
```
git clone https://github.com/amazonappdev/FOSTTIDPerfTesting.git
```

2. Navigate to the project directory:
```
cd FOSTTIDPerfTesting
```

3. Build the project and create the JAR:
```
mvn clean install
```
4. Locate the ZIP file FOSTTIDPerfTesting-distribution.zip in the 'jar' folder.


## 📱 Running the Tests

1. Copy and unzip `FOSTTIDPerfTesting-distribution.zip` to your workspace.
2. Place your apk to test in the folder ```./Input/APK/``` of your workspace where you have extracted `FOSTTIDPerfTesting-distribution.zip`
3. Rename the apk to test to ```Input.apk```
4. Execute the JAR:
    ```
    java -jar PerformanceKpi-jar.jar <KpiType> <DSN(DeviceSerialNumber)>
    ```
    Where <KpiType> can be ```latency```(For both cool and warm launch Execution), ```cool```, ```warm```
    and <DSN(DeviceSerialNumber)> is the name of your testing device listed executing ```adb devices```
5. Wait for the completion of the tests.

## 📊 Understanding Results

After the tests are complented you can analyze the results opening the logs generated:

- **Runtime Logs**: Detailed test execution information
- **CSV File**: Test result values and app details
- **HTML Report**: UI representation of test results
- **Log Folder**: Device logs for each test iteration


## 🛠️ Customization

You can customize the following parameters for the tests to be excuted modifying `commonconfig.properties`:
- Test iterations (`latencyIterations`)
- Wait time after app launch (`latencyWait`)
- Device reboot option (`reboot_device`)


## 📚 Understanding Tp50, Tp90, and Average

>**Tp50 (50th Percentile, Median):**
> Definition: The value below which 50% of the data points fall.
> Usage: Indicates the median performance, less affected by outliers.
> Example: If Tp50 of response times is 200 ms, 50% of the responses are 200 ms or faster.

>**Tp90 (90th Percentile):**
> Definition: The value below which 90% of the data points fall.
> Usage: Highlights the upper range of typical performance, showing that most responses are this fast or faster.
> Example: If Tp90 is 500 ms, 90% of the responses are 500 ms or faster.

>**Average (Mean):**
> Definition: The sum of all values divided by the number of values.
> Usage: Gives a general sense of overall performance but can be skewed by outliers.
> Example: If response times are 100 ms, 200 ms, and 1000 ms, the average is 433 ms.

**Comparison:**
Tp50 and Tp90: Less affected by extreme values, providing a clearer picture of typical and upper-range performance.
Average: Can be skewed by outliers, giving a less accurate picture of typical performance.

## Get support
If you found a bug or want to suggest a new [feature/use case/sample], please [file an issue](../../issues).

If you have questions, comments, or need help with code, we're here to help:
- on X at [@AmazonAppDev](https://twitter.com/AmazonAppDev)
- on Stack Overflow at the [amazon-appstore](https://stackoverflow.com/questions/tagged/amazon-appstore) tag

### Stay updated
Get the most up to date Amazon Appstore developer news, product releases, tutorials, and more:

* 📣 Follow [@AmazonAppDev](https://twitter.com/AmazonAppDev) and [our team](https://twitter.com/i/lists/1580293569897984000) on [Twitter](https://twitter.com/AmazonAppDev)

* 📺 Subscribe to our [Youtube channel](https://www.youtube.com/amazonappstoredevelopers)

* 📧 Sign up for the [Developer Newsletter](https://m.amazonappservices.com/devto-newsletter-subscribe)

### Authors

- [@CharanSingh](https://github.com/csinghjq)
- [@giolaq](https://github.com/giolaq)


