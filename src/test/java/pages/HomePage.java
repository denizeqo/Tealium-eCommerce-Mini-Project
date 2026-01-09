package pages;

import org.openqa.selenium.*;
import pages.components.TopNavComponent;
import utils.WaitUtils;
import pages.ProductListPage;

public class HomePage {

    private final WebDriver driver;
    private final WaitUtils wait;

    private final String baseUrl = "https://ecommerce.tealiumdemo.com/";

    // Top links
    private final By accountLink = By.cssSelector("a.skip-account");
    private final By accountDropdown = By.cssSelector("#header-account"); // dropdown container

    // Dropdown items (fallbacks included)
    private final By registerLink = By.cssSelector("#header-account a[title='Register']");
    private final By signInLink = By.cssSelector("#header-account a[title='Log In'], #header-account a[title='Login'], #header-account a[title='Sign In']");
    private final By logoutLink = By.cssSelector("#header-account a[title='Log Out'], #header-account a[title='Logout']");

    // Welcome username area
    private final By welcomeText = By.cssSelector("p.welcome-msg, .welcome-msg");

    public HomePage(WebDriver driver, WaitUtils wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public HomePage open() {
        driver.get(baseUrl);
        wait.jsReady();
        return this;
    }

    public HomePage clickAccount() {
        wait.clickable(accountLink).click();
        // wait dropdown to be present (not always visible instantly)
        wait.visible(accountDropdown);
        return this;
    }

    public RegisterPage goToRegister() {
        driver.get("https://ecommerce.tealiumdemo.com/customer/account/create/");
        wait.jsReady();
        return new RegisterPage(driver, wait);
    }



    public LoginPage goToSignIn() {
        driver.get("https://ecommerce.tealiumdemo.com/customer/account/login/");
        wait.jsReady();
        return new LoginPage(driver, wait);
    }

    public boolean isUsernameDisplayed(String username) {
        String text = wait.visible(welcomeText).getText();
        return text != null && text.toLowerCase().contains(username.toLowerCase());
    }

    public HomePage logout() {
        clickAccount();
        wait.safeClick(logoutLink);
        wait.jsReady();
        return this;
    }
    private final By wishlistMenuLink = By.cssSelector("#header-account a[href*='wishlist']");

    public int getWishlistCountFromAccountMenu() {
        clickAccount();
        String txt = wait.visible(wishlistMenuLink).getText(); // e.g. "My Wish List (2 items)"
        // extract first number inside parentheses
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\((\\d+)").matcher(txt);
        if (m.find()) return Integer.parseInt(m.group(1));
        return 0;
    }

    public HomePage hoverMenu(String menuName) {
        TopNavComponent nav = new TopNavComponent(driver, wait);
        switch (normalizeMenu(menuName)) {
            case "women":
                nav.hoverWomen();
                break;
            case "men":
                nav.hoverMen();
                break;
            case "sale":
                nav.hoverSale();
                break;
            default:
                throw new IllegalArgumentException("Unknown menu: " + menuName);
        }
        return this;
    }

    public ProductListPage clickViewAll(String menuName) {
        TopNavComponent nav = new TopNavComponent(driver, wait);
        switch (normalizeMenu(menuName)) {
            case "women":
                nav.clickViewAllWomen();
                break;
            case "men":
                nav.clickViewAllMen();
                break;
            case "sale":
                nav.clickViewAllSale();
                break;
            default:
                throw new IllegalArgumentException("Unknown menu: " + menuName);
        }
        return new ProductListPage(driver, wait);
    }

    private String normalizeMenu(String menuName) {
        return menuName == null ? "" : menuName.trim().toLowerCase();
    }
}
