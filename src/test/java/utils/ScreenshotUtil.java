package utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotUtil {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    public static String takeScreenshot(WebDriver driver, String testName) {
        if (driver == null) return null;

        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            String safeName = testName == null ? "test" : testName.replaceAll("[^a-zA-Z0-9._-]", "_");
            String fileName = safeName + "_" + LocalDateTime.now().format(FMT) + ".png";

            Path outDir = Path.of("screenshots");
            File dest = outDir.resolve(fileName).toFile();

            // ensure folder exists
            dest.getParentFile().mkdirs();

            FileUtils.copyFile(src, dest);
            return dest.getAbsolutePath();
        } catch (Exception e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
            return null;
        }
    }
}
