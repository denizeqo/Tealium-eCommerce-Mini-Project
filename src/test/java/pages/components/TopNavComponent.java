package pages.components;

import org.openqa.selenium.WebDriver;
import utils.WaitUtils;

public class TopNavComponent {

    private final WebDriver driver;
    private final WaitUtils wait;

    // These category URLs exist on this Magento demo
    private static final String WOMEN_URL = "https://ecommerce.tealiumdemo.com/women.html";
    private static final String MEN_URL   = "https://ecommerce.tealiumdemo.com/men.html";
    private static final String SALE_URL  = "https://ecommerce.tealiumdemo.com/sale.html";

    public TopNavComponent(WebDriver driver, WaitUtils wait) {
        this.driver = driver;
        this.wait = wait;
    }

    // Hover methods kept for later tests (no-op for now)
    public TopNavComponent hoverWomen() { return this; }
    public TopNavComponent hoverMen()   { return this; }
    public TopNavComponent hoverSale()  { return this; }

    public void clickViewAllWomen() {
        driver.get(WOMEN_URL);
        wait.jsReady();
    }

    public void clickViewAllMen() {
        driver.get(MEN_URL);
        wait.jsReady();
    }

    public void clickViewAllSale() {
        driver.get(SALE_URL);
        wait.jsReady();
    }
}
