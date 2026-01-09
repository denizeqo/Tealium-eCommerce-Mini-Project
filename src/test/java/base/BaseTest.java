package base;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import utils.ScreenshotUtil;
import utils.WaitUtils;

import java.time.Duration;

public class BaseTest {

    protected WebDriver driver;
    protected WaitUtils wait;

    @BeforeMethod
    public void setUp() {
        driver = base.DriverFactory.createDriver();
        wait = new WaitUtils(driver, Duration.ofSeconds(12));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        // if failed -> screenshot
        if (result.getStatus() == ITestResult.FAILURE) {
            String path = ScreenshotUtil.takeScreenshot(driver, result.getName());
            System.out.println("Screenshot saved: " + path);
        }
        base.DriverFactory.quitDriver(driver);
    }
}
