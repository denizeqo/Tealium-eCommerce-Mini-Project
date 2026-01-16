package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class CartPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // --- Locators ---
    private By cartTableRows = By.cssSelector("#shopping-cart-table tbody tr");
    private By grandTotal = By.cssSelector(
            "#shopping-cart-totals-table tfoot .price, " +
                    "#shopping-cart-totals-table tfoot strong .price, " +
                    ".cart-totals tfoot .price, " +
                    ".cart-totals tfoot strong .price"
    );
    private By loadingMask = By.cssSelector(".loading-mask, .please-wait");
    private By emptyCartMsg = By.cssSelector(".cart-empty > p, .page-title h1, .cart-empty");

    // --- Constructor ---
    public CartPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // --- Methods used in Test 7 ---

    public void setQuantity(int rowIndex, int qty) {
        List<WebElement> rows = driver.findElements(cartTableRows);
        if (rows.size() > rowIndex) {
            WebElement qtyInput = rows.get(rowIndex).findElement(By.cssSelector("input.qty"));
            qtyInput.clear();
            qtyInput.sendKeys(String.valueOf(qty));
        }
    }

    public void clickUpdateCart() {
        try {
            WebElement btn = driver.findElement(By.cssSelector("button[name='update_cart_action'], button[title*='Update'], .btn-update"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        } catch (Exception e) {
            throw new NoSuchElementException("Could not find Update Cart button on " + driver.getCurrentUrl());
        }
    }

    public double getGrandTotal() {
        try {
            String text = driver.findElement(grandTotal).getText().replaceAll("[^0-9.]", "");
            return Double.parseDouble(text);
        } catch (Exception e) {
            System.out.println("Could not parse Grand Total: " + e.getMessage());
            return 0.0;
        }
    }

    public double calculateSumOfSubtotals() {
        List<WebElement> rows = driver.findElements(cartTableRows);
        double sum = 0.0;
        for (WebElement row : rows) {
            WebElement subCell = row.findElement(By.cssSelector("td.product-cart-total .price"));
            String text = subCell.getText().replaceAll("[^0-9.]", "");
            sum += Double.parseDouble(text);
            System.out.println(sum);
        }
        return sum;
    }


    public int getItemCount() {
        return driver.findElements(cartTableRows).size();
    }

    public boolean isCartEmptyMessageDisplayed() {
        // Check if the empty message exists and is visible
        List<WebElement> msgs = driver.findElements(emptyCartMsg);
        if (msgs.isEmpty()) return false;

        for (WebElement msg : msgs) {
            try {
                if (!msg.isDisplayed()) continue;
                String text = msg.getText().toLowerCase();
                if (text.contains("no items") || text.contains("shopping cart is empty") || text.contains("empty")) {
                    return true;
                }
            } catch (StaleElementReferenceException ignored) {}
        }

        return false;
    }
}
