package utils;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;
import java.util.List;

public class WaitUtils {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public WaitUtils(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
    }

    public WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public boolean textToBe(By locator, String text) {
        return wait.until(ExpectedConditions.textToBe(locator, text));
    }

    public void jsReady() {
        wait.until(d -> ((JavascriptExecutor) d)
                .executeScript("return document.readyState").equals("complete"));
    }

    // optional “ajax-ish” wait: works for many sites using jQuery (safe if jQuery absent)
    public void jqueryIdle() {
        wait.until(d -> {
            try {
                Object active = ((JavascriptExecutor) d).executeScript(
                        "return (window.jQuery && window.jQuery.active) ? window.jQuery.active : 0;");
                return active instanceof Number && ((Number) active).intValue() == 0;
            } catch (Exception e) {
                return true; // don't block if site doesn't support it
            }
        });
    }
    public void scrollIntoView(By locator) {
        WebElement el = visible(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }

    public void safeClick(By locator) {
        WebElement el = clickable(locator);

        try {
            // move mouse to element (helps with dropdown overlays)
            new Actions(driver).moveToElement(el).perform();
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
            el.click();
        } catch (ElementClickInterceptedException e) {
            // fallback: JS click
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }
    public void closeConsentIfPresent() {
        try {
            List<WebElement> closeBtns = driver.findElements(By.cssSelector(".ui-dialog-titlebar-close"));
            if (!closeBtns.isEmpty() && closeBtns.get(0).isDisplayed()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", closeBtns.get(0));
            }
        } catch (Exception ignored) {}
    }


}
