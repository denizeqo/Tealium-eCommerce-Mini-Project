package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class DriverFactory {

    private static final String CHROMIUM_BIN = "/snap/bin/chromium";

    public static WebDriver createDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.setBinary(CHROMIUM_BIN);

        // stability flags (Linux/snap)
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-debugging-port=9222");

        // fresh profile (fixes DevToolsActivePort)
        try {
            Path profile = Files.createTempDirectory("selenium-profile-");
            options.addArguments("--user-data-dir=" + profile.toAbsolutePath());
        } catch (Exception ignored) {}

        // Run headless for faster, non-UI test runs
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1280,900");

        WebDriver driver = new ChromeDriver(options);

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
        driver.manage().window().maximize(); // harmless even in headless

        return driver;
    }

    public static void quitDriver(WebDriver driver) {
        if (driver != null) {
            try { driver.quit(); } catch (Exception ignored) {}
        }
    }
}
