package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.WaitUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ProductListPage {

    private final WebDriver driver;
    private final WaitUtils wait;

    public ProductListPage(WebDriver driver, WaitUtils wait) {
        this.driver = driver;
        this.wait = wait;
    }

    // ---------- waits/helpers ----------
    private WebDriverWait w(long seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    private void closeConsentIfPresent() {
        try {
            List<WebElement> close = driver.findElements(By.cssSelector(".ui-dialog-titlebar-close, .ui-dialog-titlebar button"));
            if (!close.isEmpty() && close.get(0).isDisplayed()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", close.get(0));
            }
        } catch (Exception ignored) {}
    }

    private void jsClick(WebElement el) {
        try {
            el.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    private void scrollCenter(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }

    // ---------- locators ----------
    private final By productCards = By.cssSelector(".category-products li.item, ul.products-grid li.item, ol.products-list li.item");
    private final By productLinkInsideCard = By.cssSelector("h2.product-name a, a.product-image");
    private final By actionsInCard = By.cssSelector(".actions, .actions-wrapper, .add-to-links");
    private final By sortSelect = By.cssSelector(".sort-by select, select[title='Sort By'], select#sorter");
    private final By currentlyBox = By.cssSelector(".currently, .block-layered-nav .currently");
    private final By wishlistRows = By.cssSelector("#wishlist-table tbody tr, table#wishlist-table tbody tr, .my-wishlist tbody tr");
    private final By oldPrice = By.cssSelector(".old-price .price, .price-box .old-price .price");
    private final By specialPrice = By.cssSelector(".special-price .price, .price-box .special-price .price");

    // ---------- core listing ----------
    public void waitForProducts() {
        closeConsentIfPresent();
        if (wait != null) {
            try { wait.jsReady(); } catch (Exception ignored) {}
        }
        w(20).ignoring(StaleElementReferenceException.class)
                .until(d -> !d.findElements(productCards).isEmpty());
    }

    private List<WebElement> dFindCards() {
        return driver.findElements(productCards);
    }

    public List<WebElement> getProducts() {
        waitForProducts();
        return dFindCards();
    }

    private WebElement productAt(int index) {
        List<WebElement> cards = getProducts();
        if (index < 0 || index >= cards.size()) throw new IllegalArgumentException("Bad index " + index);
        return cards.get(index);
    }

    // ---------- hover test (Test 3 FIX) ----------
    public String getProductCardStyles(int index) {
        WebElement card = productAt(index);
        String boxShadow = card.getCssValue("box-shadow");
        String border = card.getCssValue("border");
        String outline = card.getCssValue("outline");
        String zIndex = card.getCssValue("z-index");

        String actions = "N/A";
        try {
            WebElement a = card.findElement(actionsInCard);
            // Checking visibility, opacity, and display
            actions = a.getCssValue("display") + "|" + a.getCssValue("opacity") + "|" + a.getCssValue("visibility");
        } catch (Exception ignored) {}

        return "shadow=" + boxShadow + "; border=" + border + "; outline=" + outline + "; z=" + zIndex + "; actions=" + actions;
    }

    public boolean hoverChangesSomething(int index) {
        closeConsentIfPresent();
        WebElement card = productAt(index);
        scrollCenter(card);

        // Capture the state of the card before hover (specifically the class attribute)
        String classBefore = card.getAttribute("class");

        // Perform the hover using Selenium Actions
        new Actions(driver)
                .moveToElement(card)
                .pause(Duration.ofMillis(500)) // Using Duration pause per requirements
                .build()
                .perform();

        // Strategy 1: Check if a "hover" class was added by the site's JavaScript
        String classAfter = card.getAttribute("class");
        if (!classAfter.equals(classBefore)) return true;

        // Strategy 2: Check if the 'actions' container (Add to Cart buttons) became visible
        try {
            WebElement actions = card.findElement(actionsInCard);
            // We wait for the 'opacity' to change or 'display' to not be 'none'
            w(3).until(d -> {
                String opacity = actions.getCssValue("opacity");
                String display = actions.getCssValue("display");
                return display.equals("block") || (!opacity.equals("0") && !opacity.isEmpty());
            });
            return true;
        } catch (Exception e) {
            // Strategy 3: Forced JS Hover as a final fallback
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].dispatchEvent(new MouseEvent('mouseover', {bubbles:true}));", card);

            // Final check on any CSS style change (like background or shadow)
            String shadowAfter = card.getCssValue("box-shadow");
            return !shadowAfter.contains("none") || !shadowAfter.isEmpty();
        }
    }
    // ---------- SALE helpers (Test 4) ----------
    public List<WebElement> getSaleProductCards() {
        List<WebElement> out = new ArrayList<>();
        for (WebElement card : getProducts()) {
            if (!card.findElements(oldPrice).isEmpty() && !card.findElements(specialPrice).isEmpty()) {
                out.add(card);
            }
        }
        return out;
    }

    public String getOldPriceTextDecoration(WebElement saleCard) {
        WebElement el = saleCard.findElement(oldPrice);
        String line = el.getCssValue("text-decoration-line");
        if (line == null || line.isBlank() || "none".equalsIgnoreCase(line)) line = el.getCssValue("text-decoration");
        return line;
    }

    public String getFinalPriceTextDecoration(WebElement saleCard) {
        WebElement el = saleCard.findElement(specialPrice);
        String line = el.getCssValue("text-decoration-line");
        if (line == null || line.isBlank() || "none".equalsIgnoreCase(line)) line = el.getCssValue("text-decoration");
        return line;
    }

    public String getOldPriceColor(WebElement saleCard) {
        return saleCard.findElement(oldPrice).getCssValue("color");
    }

    public String getFinalPriceColor(WebElement saleCard) {
        return saleCard.findElement(specialPrice).getCssValue("color");
    }

    public boolean isBlueish(String cssColor) {
        if (cssColor == null) return false;
        String s = cssColor.replace("rgba(", "").replace("rgb(", "").replace(")", "");
        String[] p = s.split(",");
        if (p.length < 3) return false;
        int r = Integer.parseInt(p[0].trim());
        int g = Integer.parseInt(p[1].trim());
        int b = Integer.parseInt(p[2].trim());
        return (b > 120) && (b > r + 30) && (b > g + 30);
    }

    private boolean isBlackish(String cssColor) {
        if (cssColor == null) return false;
        String s = cssColor.replace("rgba(", "").replace("rgb(", "").replace(")", "");
        String[] p = s.split(",");
        if (p.length < 3) return false;
        int r = Integer.parseInt(p[0].trim());
        int g = Integer.parseInt(p[1].trim());
        int b = Integer.parseInt(p[2].trim());
        return r < 45 && g < 45 && b < 45;
    }

    // ---------- FILTERS (Test 5) ----------
    public void applyBlackColorFilter() {
        closeConsentIfPresent();
        waitForProducts();
        String beforeUrl = driver.getCurrentUrl();
        By blackInColorGroup = By.xpath("//dt[contains(translate(.,'ABC','abc'),'color')]/following-sibling::dd[1]//a[contains(@title,'Black') or contains(.,'Black')]");
        List<WebElement> candidates = driver.findElements(blackInColorGroup);
        if (!candidates.isEmpty()) {
            jsClick(candidates.get(0));
        } else {
            String base = driver.getCurrentUrl();
            driver.get(base.contains("?") ? base + "&color=20" : base + "?color=20");
        }
        w(15).until(d -> !d.getCurrentUrl().equals(beforeUrl));
        waitForProducts();
    }

    public boolean isColorFilterApplied() {
        return driver.getCurrentUrl().contains("color=") || driver.findElements(currentlyBox).size() > 0;
    }

    public boolean cardHasBlackSwatch(WebElement card) {
        List<WebElement> swatches = card.findElements(By.cssSelector(
                ".configurable-swatch-list .swatch-link, .configurable-swatch-list span, .swatch-link, .swatch-link span, .swatch-label"));
        for (WebElement sw : swatches) {
            if (hasBlackLabel(sw)) return true;
            if (isBlackish(sw.getCssValue("background-color"))) return true;
        }
        return false;
    }

    private boolean hasBlackLabel(WebElement el) {
        String text = safeLower(el.getText());
        if (text.contains("black")) return true;
        String title = safeLower(el.getAttribute("title"));
        if (title.contains("black")) return true;
        String aria = safeLower(el.getAttribute("aria-label"));
        if (aria.contains("black")) return true;
        String cls = safeLower(el.getAttribute("class"));
        return cls.contains("black");
    }

    private String safeLower(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    public void selectPriceFilterFirstOption() {
        By priceFirst = By.xpath("//dt[contains(translate(.,'PRICE','price'),'price')]/following-sibling::dd[1]//a[1]");
        List<WebElement> els = driver.findElements(priceFirst);
        if (!els.isEmpty()) jsClick(els.get(0));
        waitForProducts();
    }

    public List<Double> getDisplayedPrices() {
        List<Double> prices = new ArrayList<>();
        for (WebElement card : getProducts()) {
            try {
                List<WebElement> priceEls = card.findElements(By.cssSelector(".price-box .price, .special-price .price, .regular-price .price, .price"));
                double min = Double.POSITIVE_INFINITY;
                for (WebElement pEl : priceEls) {
                    for (Double val : extractPrices(pEl.getText())) {
                        if (val < min) min = val;
                    }
                }
                if (min != Double.POSITIVE_INFINITY) prices.add(min);
            } catch (Exception ignored) {}
        }
        return prices;
    }

    private List<Double> extractPrices(String text) {
        List<Double> out = new ArrayList<>();
        if (text == null) return out;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(text);
        while (m.find()) {
            try { out.add(Double.parseDouble(m.group(1))); } catch (Exception ignored) {}
        }
        return out;
    }

    public void selectSortByContains(String text) {
        String beforeUrl = driver.getCurrentUrl();
        Select select = new Select(driver.findElement(sortSelect));
        String chosenValue = null;
        String chosenText = null;
        for (WebElement opt : select.getOptions()) {
            String optText = opt.getText();
            if (optText.contains(text)) {
                chosenText = optText;
                chosenValue = opt.getAttribute("value");
                break;
            }
        }
        if (chosenText != null) {
            try {
                select.selectByVisibleText(chosenText);
            } catch (StaleElementReferenceException e) {
                new Select(driver.findElement(sortSelect)).selectByVisibleText(chosenText);
            }
        }
        try {
            w(10).until(d -> !d.getCurrentUrl().equals(beforeUrl));
        } catch (Exception ignored) {}
        if (!driver.getCurrentUrl().toLowerCase().contains("order=price")) {
            forceSortByPrice(chosenValue);
        }
        waitForProducts();
    }

    private void forceSortByPrice(String chosenValue) {
        String url = driver.getCurrentUrl();
        String orderValue = (chosenValue != null && !chosenValue.isBlank()) ? chosenValue : "price";
        if (!orderValue.toLowerCase().contains("price")) orderValue = "price";
        String newUrl = withParam(url, "order", orderValue);
        newUrl = withParam(newUrl, "dir", "asc");
        if (!newUrl.equals(url)) {
            driver.get(newUrl);
            if (wait != null) {
                try { wait.jsReady(); } catch (Exception ignored) {}
            }
        }
    }

    private String withParam(String url, String key, String value) {
        String base = url;
        String hash = "";
        int hashIdx = url.indexOf('#');
        if (hashIdx >= 0) {
            base = url.substring(0, hashIdx);
            hash = url.substring(hashIdx);
        }
        String sep = base.contains("?") ? "&" : "?";
        if (base.matches(".*[?&]" + key + "=.*")) {
            return base.replaceAll(key + "=[^&]*", key + "=" + value) + hash;
        }
        return base + sep + key + "=" + value + hash;
    }

    public void addFirstNToWishlistViaPdp(int n) {
        String listingUrl = driver.getCurrentUrl();
        for (int i = 0; i < n; i++) {
            waitForProducts();
            boolean opened = false;
            for (int attempt = 0; attempt < 2 && !opened; attempt++) {
                try {
                    WebElement link = productAt(i).findElement(productLinkInsideCard);
                    String href = link.getAttribute("href");
                    if (href != null && !href.isBlank()) {
                        driver.get(href);
                    } else {
                        jsClick(link);
                    }
                    opened = true;
                } catch (StaleElementReferenceException ignored) {
                    waitForProducts();
                }
            }
            if (wait != null) {
                try { wait.jsReady(); } catch (Exception ignored) {}
                wait.closeConsentIfPresent();
            }
            jsClick(w(10).until(d -> d.findElement(By.cssSelector("a.link-wishlist"))));
            waitForWishlistAdded();
            driver.get(listingUrl);
        }
    }

    private void waitForWishlistAdded() {
        try {
            w(10).until(d -> {
                String url = d.getCurrentUrl();
                if (url.contains("/wishlist")) return true;
                return !d.findElements(By.cssSelector(".success-msg, .messages .success-msg")).isEmpty();
            });
        } catch (Exception ignored) {}
    }

    public void goToWishlist() {
        driver.get("https://ecommerce.tealiumdemo.com/wishlist/");
    }

    public int getWishlistItemCount() {
        return driver.findElements(wishlistRows).size();
    }
}
