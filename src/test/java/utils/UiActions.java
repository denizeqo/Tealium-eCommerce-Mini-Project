package utils;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

public class UiActions {

    private final WebDriver driver;
    private final WaitUtils wait;

    public UiActions(WebDriver driver, WaitUtils wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void hover(By locator) {
        WebElement el = wait.visible(locator);
        new Actions(driver).moveToElement(el).perform();
    }

    public void safeClick(By locator) {
        wait.scrollIntoView(locator);
        try {
            wait.clickable(locator).click();
        } catch (ElementClickInterceptedException e) {
            WebElement el = wait.visible(locator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }
}
