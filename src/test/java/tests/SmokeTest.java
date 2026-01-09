package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class SmokeTest {

    @Test
    public void opensHomePage() throws Exception {
        String chromiumPath = "/snap/bin/chromium";

        WebDriverManager.chromedriver().setup();

        // Create a fresh, writable user-data-dir (fixes DevToolsActivePort often)
        Path profileDir = Files.createTempDirectory("chrome-profile-");

        ChromeOptions options = new ChromeOptions();
        options.setBinary(chromiumPath);

        // Snap/Linux stability flags
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-debugging-port=9222");
        options.addArguments("--user-data-dir=" + profileDir.toAbsolutePath());

        // If you want to actually SEE the browser, comment this out.
        // Headless is the most reliable in CI/Linux.
        options.addArguments("--headless=new");

        WebDriver driver = null;
        try {
            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.get("https://ecommerce.tealiumdemo.com/");
            System.out.println("Title: " + driver.getTitle());
        } finally {
            if (driver != null) driver.quit();
        }
    }
}
