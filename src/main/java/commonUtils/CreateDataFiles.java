package commonUtils;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateDataFiles extends ReadPaths {

    private final Logger log = Logger.getLogger(CreateDataFiles.class.getSimpleName());

    /**
     * Creates a new CSV file at the specified file path.
     *
     * @param filePath The path to the CSV file to be created.
     */
    public void createCsvFile(String filePath) {
        try {
            Path fileCsvPath = Paths.get(filePath);
            Files.deleteIfExists(fileCsvPath);
            Files.createFile(fileCsvPath);

            try (FileWriter dataWriter = new FileWriter(fileCsvPath.toFile())) {
                if (filePath.equals(kpi_values_csv)) {
                    dataWriter.append(CSV_HEADERS);
                }
            } catch (Exception e) {
                log.error("Exception occurred while appending Data-Keys: ", e);
            }

            log.info("Test Data CSV File Created: " + filePath);
        } catch (Exception e) {
            log.error("Exception occurred while creating " + filePath + " csv file: ", e);
        }
    }
}
