package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.WaitUtils;

public class RegisterPage {
    private final WebDriver driver;
    private final WaitUtils wait;

    private final By firstName = By.id("firstname");
    private final By lastName = By.id("lastname");
    private final By email = By.id("email_address");
    private final By password = By.id("password");
    private final By confirmPassword = By.id("confirmation");
    private final By registerBtn = By.cssSelector("button[title='Register']");
    private final By successMsg = By.cssSelector("li.success-msg span, .success-msg");

    public RegisterPage(WebDriver driver, WaitUtils wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void register(String fn, String ln, String mail, String pass, String confirm) {
        wait.visible(firstName).sendKeys(fn);
        driver.findElement(lastName).sendKeys(ln);
        driver.findElement(email).sendKeys(mail);
        driver.findElement(password).sendKeys(pass);
        driver.findElement(confirmPassword).sendKeys(confirm);

        wait.safeClick(registerBtn);
        wait.jsReady();
    }

    public String getSuccessMessage() {
        return wait.visible(successMsg).getText();
    }
}