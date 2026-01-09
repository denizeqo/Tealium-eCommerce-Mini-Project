package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ScreenshotFailTest extends BaseTest {

    @Test
    public void shouldTakeScreenshotOnFailure() {
        driver.get("https://ecommerce.tealiumdemo.com/");
        Assert.assertTrue(false, "Forcing failure to verify screenshot capture.");
    }
}
