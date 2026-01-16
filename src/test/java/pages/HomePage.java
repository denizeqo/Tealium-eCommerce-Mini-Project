package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import utils.WaitUtils;

public class HomePage {
    private final WebDriver driver;
    private final WaitUtils wait;
    private final Actions actions;

    // Locators
    private final By accountLink = By.cssSelector("a.skip-account");
    private final By accountDropdown = By.cssSelector("#header-account");
    private final By welcomeText = By.cssSelector("p.welcome-msg, .welcome-msg");

    // Nav Locators
    private final By navWomen = By.linkText("Women");
    private final By navMen = By.linkText("Men");
    private final By viewAllWomen = By.linkText("View All Women");
    private final By viewAllMen = By.linkText("View All Men");

    public HomePage(WebDriver driver, WaitUtils wait) {
        this.driver = driver;
        this.wait = wait;
        this.actions = new Actions(driver);
    }

    public HomePage open() {
        System.out.println("Navigating to URL..."); // Debug log
        driver.get("https://ecommerce.tealiumdemo.com/");

        // REMOVE wait.jsReady();
        // Just return this. It's safer to wait for specific elements later.
        return this;
    }
    // *** THIS IS THE METHOD YOU ARE LOOKING FOR ***
    public RegisterPage goToRegister() {
        // We use direct URL for stability, but you can also click the menu
        driver.get("https://ecommerce.tealiumdemo.com/customer/account/create/");
        wait.jsReady();
        // This returns the RegisterPage object. If RegisterPage.java is missing, this line causes the error.
        return new RegisterPage(driver, wait);
    }

    public LoginPage goToSignIn() {
        driver.get("https://ecommerce.tealiumdemo.com/customer/account/login/");
        wait.jsReady();
        return new LoginPage(driver, wait);
    }

    public void logout() {
        driver.get("https://ecommerce.tealiumdemo.com/customer/account/logout/");
        wait.jsReady();
    }

    public boolean isUsernameDisplayed(String username) {
        try {
            return wait.visible(welcomeText).getText().toLowerCase().contains(username.toLowerCase());
        } catch (Exception e) { return false; }
    }

    // Menu Logic
    public HomePage hoverMenu(String menuName) {
        By locator = menuName.equalsIgnoreCase("women") ? navWomen : navMen;
        actions.moveToElement(wait.visible(locator)).perform();
        return this;
    }

}