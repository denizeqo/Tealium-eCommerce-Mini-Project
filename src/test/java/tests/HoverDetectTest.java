package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.ProductListPage;

public class HoverDetectTest extends BaseTest {

    @Test
    public void hoverChangesStyles() {
        driver.get("https://ecommerce.tealiumdemo.com/women.html");
        ProductListPage list = new ProductListPage(driver, wait);

        Assert.assertTrue(list.getProducts().size() > 0, "No products on women listing");
        Assert.assertTrue(list.hoverChangesSomething(0), "Hover did not change any detectable style/element");
    }
}
