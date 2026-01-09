package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.ProductListPage;
import pages.components.TopNavComponent;

public class NavSmokeTest extends BaseTest {

    @Test
    public void canNavigateToWomenMenSale() {
        driver.get("https://ecommerce.tealiumdemo.com/");

        TopNavComponent nav = new TopNavComponent(driver, wait);

        nav.clickViewAllWomen();
        ProductListPage women = new ProductListPage(driver, wait);
        Assert.assertTrue(driver.getCurrentUrl().toLowerCase().contains("women"),
                "Not on Women listing. URL=" + driver.getCurrentUrl());
        Assert.assertTrue(women.getProducts().size() > 0, "No products on Women page");

        driver.get("https://ecommerce.tealiumdemo.com/");
        nav.clickViewAllMen();
        ProductListPage men = new ProductListPage(driver, wait);
        Assert.assertTrue(driver.getCurrentUrl().toLowerCase().contains("men"),
                "Not on Men listing. URL=" + driver.getCurrentUrl());
        Assert.assertTrue(men.getProducts().size() > 0, "No products on Men page");

        driver.get("https://ecommerce.tealiumdemo.com/");
        nav.clickViewAllSale();
        ProductListPage sale = new ProductListPage(driver, wait);
        Assert.assertTrue(driver.getCurrentUrl().toLowerCase().contains("sale"),
                "Not on Sale listing. URL=" + driver.getCurrentUrl());
        Assert.assertTrue(sale.getProducts().size() > 0, "No products on Sale page");
        System.out.println("Now at: " + driver.getCurrentUrl());

    }
}
