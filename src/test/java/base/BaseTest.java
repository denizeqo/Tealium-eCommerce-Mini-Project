package base;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import utils.WaitUtils;

public class BaseTest {
    protected WebDriver driver;
    protected WaitUtils wait;

    @BeforeClass
    public void setup() {
        driver = DriverFactory.createDriver();
        wait = new WaitUtils(driver);
    }

    @AfterClass
    public void tearDown() {
        // This line caused the error before because the method didn't exist
        DriverFactory.quitDriver(driver);
    }
}