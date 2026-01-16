package pages;

import org.openqa.selenium.*;
import utils.WaitUtils;

public class LoginPage {
    private final WebDriver driver;
    private final WaitUtils wait;

    // Locators
    private final By emailInput = By.id("email");
    private final By passwordInput = By.id("pass");
    private final By loginBtn = By.cssSelector("button[title='Login']");

    public LoginPage(WebDriver driver, WaitUtils wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void login(String email, String password) {

        wait.visible(emailInput).sendKeys(email);
        driver.findElement(passwordInput).sendKeys(password);
        wait.safeClick(loginBtn);
        wait.jsReady();
    }
}