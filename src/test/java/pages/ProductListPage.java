package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ScreenshotUtil;
import utils.WaitUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ProductListPage {
    private WebDriver driver;
    private WaitUtils wait;

    private final By productItems = By.cssSelector(".products-grid li.item, .products-list li.item");
    private final By sortDropdown = By.cssSelector("select[title='Sort By'], #sorter");
    private final By sortDirectionToggle = By.cssSelector(
            "a[title*='Set Ascending'], a[title*='Set Descending'], a.sort-by-switcher, a.sorter-direction"
    );
    private final By loadingMask = By.cssSelector(".loading-mask, .please-wait");

    public ProductListPage(WebDriver driver, WaitUtils wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void addToWishlist(int index) {
        List<WebElement> wishLinks = driver.findElements(By.cssSelector("a.link-wishlist"));

        // Click the link (JS click is good here because it's an onclick POST)
        WebElement btn = wishLinks.get(index);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

        // Wait until we see a success message (most reliable)
        wait.visible(By.cssSelector(".success-msg, .messages .success-msg, .messages .success"));
    }


    public void goToWishlist() {
        driver.get("https://ecommerce.tealiumdemo.com/wishlist/");
        wait.jsReady();
    }
    public void filterByColor(String color) {
        String xpath = String.format("//dt[contains(text(),'Color')]/following-sibling::dd[1]//a[contains(text(),'%s') or .//img[contains(@alt,'%s')]]", color, color);

        try {
            WebElement firstItem = firstProductItemOrNull();
            String oldUrl = safeUrl();

            WebElement filter = wait.visible(By.xpath(xpath));
            wait.jsClick(filter);

            waitForListingRefresh(oldUrl, firstItem);
        } catch (TimeoutException e) {
            WebElement firstItem = firstProductItemOrNull();
            String oldUrl = safeUrl();
            wait.jsClick(By.partialLinkText(color));
            waitForListingRefresh(oldUrl, firstItem);

        } catch (Exception e) {
            System.err.println("Filter by color failed: " + e.getMessage());
        }
    }

    public void filterByPrice(int index) {
        WebElement firstItem = firstProductItemOrNull();
        String oldUrl = safeUrl();

        List<WebElement> filters = driver.findElements(By.xpath(
                "//dt[contains(normalize-space(.),'Price')]/following-sibling::dd[1]//a"
        ));
        if (filters.size() <= index) {
            filters = driver.findElements(By.cssSelector("#narrow-by-list dd:nth-of-type(2) ol li a"));
        }

        if (filters.size() > index) {
            wait.jsClick(filters.get(index));
            waitForListingRefresh(oldUrl, firstItem);
        }
    }
    public void sortBy(String value) {
        wait.jsReady();

        List<WebElement> items = driver.findElements(productItems);
        if (items.isEmpty()) return;

        String oldUrl = driver.getCurrentUrl();
        WebElement firstItem = items.get(0);

        Select select = new Select(wait.visible(sortDropdown));
        select.selectByVisibleText(value);

        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(8));
            shortWait.until(d -> !d.getCurrentUrl().equals(oldUrl));
        } catch (TimeoutException e) {
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(8));
                shortWait.until(ExpectedConditions.stalenessOf(firstItem));
            } catch (TimeoutException ignored) {
                try { Thread.sleep(1500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }

        wait.jsReady();
        ensureAscendingSortIfPossible();
        wait.jsReady();
    }
    public List<Double> getPrices() {
        wait.jsReady();

        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                wait.visible(productItems);
                List<WebElement> items = driver.findElements(productItems);
                List<Double> prices = new ArrayList<>();

                for (WebElement item : items) {
                    List<WebElement> priceEls = item.findElements(By.cssSelector(".price-box .price, .price"));
                    double best = Double.POSITIVE_INFINITY;

                    for (WebElement el : priceEls) {
                        double parsed = parsePrice(el.getText());
                        if (parsed > 0 && parsed < best) best = parsed;
                    }

                    if (best != Double.POSITIVE_INFINITY) prices.add(best);
                }

                return prices;
            } catch (StaleElementReferenceException e) {
                try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            } catch (TimeoutException e) {
                return new ArrayList<>();
            }
        }

        return new ArrayList<>();
    }

    public boolean isOldPriceStrikeThrough(int index) {
        List<WebElement> oldPrices = driver.findElements(By.cssSelector(".old-price .price"));
        if (oldPrices.isEmpty()) return false;
        return oldPrices.get(index).getCssValue("text-decoration").contains("line-through");
    }

    public boolean hoverChangesSomething(int index) {
        List<WebElement> products = driver.findElements(productItems);
        if (products.isEmpty() || index < 0 || index >= products.size()) return false;

        WebElement product = products.get(index);

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", product);
        wait.jsReady();

        String before = hoverSignature(product);

        new Actions(driver).moveToElement(product).perform();
        try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        // If Selenium actually put the element into :hover state, that's enough for this check.
        try {
            Object hovered = ((JavascriptExecutor) driver).executeScript("return arguments[0].matches(':hover');", product);
            if (hovered instanceof Boolean && (Boolean) hovered) return true;
        } catch (Exception ignored) {}

        String after = hoverSignature(product);
        return !before.equals(after);
    }



    public void editWishlistItemsSelectOptionsAndAddAllToCart(int count) throws InterruptedException {
        // Wait for wishlist page to load
        wait.jsReady();

        try {
            int itemsToAdd = Math.max(1, count);

            // Prefer the per-item "Add to Cart" on the wishlist table (more reliable than "Add All").
            List<WebElement> rows = driver.findElements(By.cssSelector(
                    "#wishlist-table tbody tr, #wishlist-view-form .data-table tbody tr, .my-wishlist .data-table tbody tr"
            ));

            if (!rows.isEmpty()) {
                int added = 0;
                for (WebElement row : rows) {
                    if (added >= itemsToAdd) break;

                    WebElement addBtn = findFirst(row, By.cssSelector(
                            "button.btn-cart, button[title*='Add to Cart'], a[title*='Add to Cart'], a.btn-cart"
                    ));

                    if (addBtn != null) {
                        wait.jsClick(addBtn);
                        added++;

                        // Give navigation/redirect time
                        Thread.sleep(1500);
                        if (!driver.getCurrentUrl().contains("checkout/cart")) {
                            selectProductOptions();
                        }


                        return;
                    }
                }
            }

            // Fallback: try any page-level "Add to Cart" / "Add All to Cart"
            WebElement btn = findFirst(driver, By.cssSelector(
                    "button.btn-add-to-cart, .btn-add-to-cart, button.btn-cart, button[title*='Add to Cart'], a[title*='Add to Cart']"
            ));
            if (btn == null) throw new TimeoutException("No Add to Cart button found on wishlist page");
            wait.jsClick(btn);

            // Wait for redirect or product options page
            Thread.sleep(2000);

            // Handle Product Options if redirected
            if (!driver.getCurrentUrl().contains("checkout/cart")) {
                selectProductOptions();
            }


        } catch (Exception e) {
            System.err.println("Failed to add wishlist items to cart: " + e.getMessage());
            throw e;
        }
    }

    // Helper method for product options
    private void selectProductOptions() throws InterruptedException {
        System.out.println("=== SELECT PRODUCT OPTIONS START ===");
        System.out.println("Current URL: " + driver.getCurrentUrl());

        // Select color
        WebElement color = findFirst(driver, By.cssSelector("#configurable_swatch_color li a"));
        if (color != null) {
            System.out.println("Clicking color option");
            wait.jsClick(color);
            Thread.sleep(500);
        } else {
            System.out.println("No color option found");
        }

        // Select size
        WebElement size = findFirst(driver, By.cssSelector("#configurable_swatch_size li a"));
        if (size != null) {
            System.out.println("Clicking size option");
            wait.jsClick(size);
            Thread.sleep(500);
        } else {
            System.out.println("No size option found");
        }

        WebElement addToCartBtn = driver.findElement(By.cssSelector("button.btn-cart"));
        ((JavascriptExecutor) driver).executeScript("productAddToCartForm.submit(arguments[0]);", addToCartBtn);
        Thread.sleep(5000);
    }

    private void waitForListingRefresh(String oldUrl, WebElement oldFirstItem) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> !safeUrl().equals(oldUrl));
        } catch (Exception ignored) {
            if (oldFirstItem != null) {
                try {
                    new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.stalenessOf(oldFirstItem));
                } catch (Exception ignored2) {}
            }
        }

        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.invisibilityOfElementLocated(loadingMask));
        } catch (Exception ignored) {}

        wait.jsReady();
        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private WebElement firstProductItemOrNull() {
        try {
            List<WebElement> items = driver.findElements(productItems);
            return items.isEmpty() ? null : items.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    private String safeUrl() {
        try {
            return driver.getCurrentUrl();
        } catch (Exception e) {
            return "";
        }
    }

    private void ensureAscendingSortIfPossible() {
        String url = "";
        try { url = driver.getCurrentUrl().toLowerCase(); } catch (Exception ignored) {}
        boolean looksDesc = url.contains("dir=desc");

        List<WebElement> toggles = driver.findElements(sortDirectionToggle);
        if (toggles.isEmpty()) return;

        WebElement toggle = toggles.get(0);
        String title = "";
        String href = "";
        try { title = String.valueOf(toggle.getAttribute("title")).toLowerCase(); } catch (Exception ignored) {}
        try { href = String.valueOf(toggle.getAttribute("href")).toLowerCase(); } catch (Exception ignored) {}

        boolean clickToAsc = looksDesc || title.contains("ascending") || href.contains("dir=asc");
        if (!clickToAsc) return;

        String oldUrl = driver.getCurrentUrl();
        wait.jsClick(toggle);

        try {
            new WebDriverWait(driver, Duration.ofSeconds(8)).until(d -> !d.getCurrentUrl().equals(oldUrl));
        } catch (Exception ignored) {}
    }

    private double parsePrice(String raw) {
        if (raw == null) return 0.0;
        String cleaned = raw.replaceAll("[^0-9.]", "");
        if (cleaned.isBlank()) return 0.0;
        try {
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String hoverSignature(WebElement product) {
        try {
            WebElement img = findFirst(product, By.cssSelector(".product-image img, img"));
            WebElement name = findFirst(product, By.cssSelector(".product-name a, a"));
            return styleSig(product) + "|" + styleSig(img) + "|" + styleSig(name);
        } catch (Exception e) {
            return "sigerr";
        }
    }

    private String styleSig(WebElement el) {
        if (el == null) return "null";
        try {
            Object out = ((JavascriptExecutor) driver).executeScript(
                    "const s=getComputedStyle(arguments[0]);" +
                            "return [s.opacity,s.transform,s.boxShadow,s.backgroundColor," +
                            "s.borderTopColor,s.borderRightColor,s.borderBottomColor,s.borderLeftColor].join('|');",
                    el
            );
            return String.valueOf(out);
        } catch (Exception e) {
            try { return el.getAttribute("class"); } catch (Exception ignored) {}
            return "err";
        }
    }

    private WebElement findFirst(SearchContext ctx, By locator) {
        try {
            List<WebElement> els = ctx.findElements(locator);
            return els.isEmpty() ? null : els.get(0);
        } catch (Exception e) {
            return null;
        }
    }
}
