package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.io.File;
import java.time.Duration;

public class DriverFactory {

    public static WebDriver createDriver() {
        // FIX: Remove hardcoded version, let it auto-detect
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        // --- CRITICAL: Add this to prevent detection ---
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        // --- LINUX SNAP FIXES ---
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-search-engine-choice-screen");

        // Snap browser fix: use a fresh profile per run (avoids state leaking across mvn test runs)
        String profilePath = System.getProperty("user.dir") + File.separator + "target" + File.separator
                + "chrome_profile_" + System.currentTimeMillis();
        new File(profilePath).mkdirs();
        options.addArguments("--user-data-dir=" + profilePath);

        WebDriver driver = new ChromeDriver(options);

        // REMOVE implicit wait - it conflicts with explicit waits!
        // driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));

        return driver;
    }

    public static void quitDriver(WebDriver driver) {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.err.println("Error quitting driver: " + e.getMessage());
            }
        }
    }
}
