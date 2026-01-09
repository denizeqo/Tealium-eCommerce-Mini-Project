package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.LoginPage;
import pages.ProductListPage;
import pages.RegisterPage;
import utils.TestData;

import java.util.List;

public class MiniProjectTests_1_to_6 extends BaseTest {

    @Test
    public void test1_createAccount() {
        TestData.firstName = "Deni";
        TestData.lastName = "Zeqo";
        TestData.fullName = TestData.firstName + " " + TestData.lastName;
        TestData.email = "deni+" + System.currentTimeMillis() + "@testmail.com";
        TestData.password = "Test@12345";

        HomePage home = new HomePage(driver, wait).open();
        RegisterPage registerPage = home.goToRegister();

        String title = registerPage.getTitle().toLowerCase();
        Assert.assertTrue(title.contains("create") && title.contains("account"),
                "Register page title unexpected: " + registerPage.getTitle());

        registerPage.register(TestData.firstName, TestData.lastName, TestData.email, TestData.password, TestData.password);

        String msg = registerPage.getSuccessMessage().toLowerCase();
        Assert.assertTrue(msg.contains("thank you"), "Success msg missing/unexpected: " + msg);

        new HomePage(driver, wait).logout();
    }

    @Test(dependsOnMethods = "test1_createAccount")
    public void test2_signIn() {
        HomePage home = new HomePage(driver, wait).open();
        LoginPage loginPage = home.goToSignIn();

        home = loginPage.login(TestData.email, TestData.password);

        Assert.assertTrue(home.isUsernameDisplayed(TestData.fullName),
                "Username not displayed. Expected: " + TestData.fullName);

        home.logout();
    }

    private void loginQuick() {
        HomePage home = new HomePage(driver, wait).open();
        home.goToSignIn().login(TestData.email, TestData.password);
    }

    @Test(dependsOnMethods = "test2_signIn")
    public void test3_hoverStyle() {
        loginQuick();
        HomePage home = new HomePage(driver, wait);

        // REQUIREMENT: Hover over Woman menu and click "View All Women"
        // (Assuming you add these methods to your HomePage or ProductListPage)
        home.hoverMenu("Women");
        ProductListPage list = home.clickViewAll("Women");

        Assert.assertTrue(list.getProducts().size() > 0, "No products found on Women page");

        // Perform hover and assert
        boolean styleChanged = list.hoverChangesSomething(0);
        Assert.assertTrue(styleChanged, "The product card did not show a hover effect (style/class/buttons).");
    }

    @Test(dependsOnMethods = "test3_hoverStyle")
    public void test4_saleProductsStyle() {
        loginQuick();

        driver.get("https://ecommerce.tealiumdemo.com/sale.html");
        ProductListPage list = new ProductListPage(driver, wait);

        List<org.openqa.selenium.WebElement> saleCards = list.getSaleProductCards();
        Assert.assertTrue(saleCards.size() > 0, "No sale items found (old+special price) on Sale page");

        for (org.openqa.selenium.WebElement card : saleCards) {
            String oldDeco = list.getOldPriceTextDecoration(card).toLowerCase();
            String newDeco = list.getFinalPriceTextDecoration(card).toLowerCase();

            Assert.assertTrue(oldDeco.contains("line-through"),
                    "Old price not strikethrough. text-decoration=" + oldDeco);

            Assert.assertTrue(!newDeco.contains("line-through"),
                    "Final price should NOT be strikethrough. text-decoration=" + newDeco);

            String oldColor = list.getOldPriceColor(card);
            String newColor = list.getFinalPriceColor(card);

            Assert.assertNotEquals(oldColor, newColor,
                    "Old and final price colors should differ. old=" + oldColor + " new=" + newColor);

            Assert.assertTrue(list.isBlueish(newColor),
                    "Final price not blue-ish. color=" + newColor);
        }
    }

    @Test(dependsOnMethods = "test4_saleProductsStyle")
    public void test5_filters() {
        loginQuick();

        driver.get("https://ecommerce.tealiumdemo.com/men.html");
        ProductListPage list = new ProductListPage(driver, wait);

        list.applyBlackColorFilter();

        // IMPORTANT: this site often shows only a black square, not the text "Black"
        Assert.assertTrue(list.isColorFilterApplied(),
                "Black filter not shown as applied. URL=" + driver.getCurrentUrl());

        int count = list.getProducts().size();
        Assert.assertTrue(count > 0, "No products after applying black filter. URL=" + driver.getCurrentUrl());

        // Reliable assertion: every visible product should have a black swatch option
        for (org.openqa.selenium.WebElement card : list.getProducts()) {
            Assert.assertTrue(list.cardHasBlackSwatch(card),
                    "A product does not show any black swatch option on the listing.");
        }

        // price filter (first option in PRICE group)
        list.selectPriceFilterFirstOption();

        List<Double> prices = list.getDisplayedPrices();
        Assert.assertTrue(prices.size() > 0, "No prices found after price filter.");
        for (Double p : prices) {
            Assert.assertNotNull(p, "Could not parse a displayed product price");
            Assert.assertTrue(p >= 0.0 && p <= 99.99, "Price out of range (expected 0..99.99): " + p);
        }

        // This demo usually ends up with 3 items in Men for this scenario
        Assert.assertEquals(list.getProducts().size(), 3, "Expected exactly 3 products after filters on Men.");
    }

    @Test(dependsOnMethods = "test5_filters")
    public void test6_sortingAndWishlist() {
        loginQuick();

        driver.get("https://ecommerce.tealiumdemo.com/women.html");
        ProductListPage list = new ProductListPage(driver, wait);

        list.selectSortByContains("Price");

        List<Double> prices = list.getDisplayedPrices();
        Assert.assertTrue(prices.size() > 1, "Not enough prices to validate sorting");

        for (int i = 1; i < prices.size(); i++) {
            Assert.assertTrue(prices.get(i) >= prices.get(i - 1),
                    "Prices not sorted ascending at index " + i + ": " + prices);
        }

        // Stable wishlist add: via Product Detail Page (avoids stale grid elements)
        list.addFirstNToWishlistViaPdp(2);

        list.goToWishlist();
        Assert.assertEquals(list.getWishlistItemCount(), 2, "Wishlist should have exactly 2 items.");
    }
}
