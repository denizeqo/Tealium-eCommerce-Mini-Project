package pages;

import org.openqa.selenium.*;
import utils.WaitUtils;

public class LoginPage {

    private final WebDriver driver;
    private final WaitUtils wait;

    private final By emailField = By.id("email");
    private final By passwordField = By.id("pass");
    private final By loginBtn = By.cssSelector("button[title='Login'], button[title='Log In'], button[title='Sign In']");

    public LoginPage(WebDriver driver, WaitUtils wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public HomePage login(String email, String password) {
        wait.visible(emailField).sendKeys(email);
        driver.findElement(passwordField).sendKeys(password);
        wait.closeConsentIfPresent();
        wait.safeClick(loginBtn);
        wait.jsReady();
        return new HomePage(driver, wait);
    }
}
