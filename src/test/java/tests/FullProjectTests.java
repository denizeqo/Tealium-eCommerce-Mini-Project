package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.*;

public class FullProjectTests extends BaseTest {

    String email = "student" + System.currentTimeMillis() + "@test.com";
    String password = "Password123";

    @Test(priority = 1)
    public void test1_CreateAccount() {
        HomePage home = new HomePage(driver, wait).open();
        RegisterPage register = home.goToRegister();
        register.register("Sheah", "Piath", email, password, password);
        Assert.assertTrue(register.getSuccessMessage().contains("Thank you"));
        home.logout();
    }

    @Test(priority = 2)
    public void test2_SignIn() {
        HomePage home = new HomePage(driver, wait).open();
        LoginPage login = home.goToSignIn();
        login.login(email, password);
        Assert.assertTrue(home.isUsernameDisplayed("John"));
        home.logout();
    }

    @Test(priority = 3)
    public void test3_HoverStyle() {
        HomePage home = new HomePage(driver, wait).open();
        LoginPage login = home.goToSignIn();
        login.login(email, password);

        driver.get("https://ecommerce.tealiumdemo.com/women.html");

        ProductListPage list = new ProductListPage(driver, wait);
        Assert.assertTrue(list.hoverChangesSomething(0), "Hover effect failed");
    }

    @Test(priority = 4)
    public void test4_SaleProductStyle() {
        driver.get("https://ecommerce.tealiumdemo.com/sale.html");
        ProductListPage list = new ProductListPage(driver, wait);
        Assert.assertTrue(list.isOldPriceStrikeThrough(0), "Sale items should have line-through style");
    }

    @Test(priority = 5)
    public void test5_Filtering() {
        driver.get("https://ecommerce.tealiumdemo.com/women.html");
        ProductListPage list = new ProductListPage(driver, wait);

        // Uses the new Robust Locator
        list.filterByColor("Black");
        list.filterByPrice(0);

        // Wait extra time after filtering
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        int count = list.getPrices().size();
        Assert.assertTrue(count > 0 && count <= 3, "Expected 3 or fewer items, found " + count);
    }

    @Test(priority = 6)
    public void test6_Sorting() {
        driver.get("https://ecommerce.tealiumdemo.com/women.html");
        ProductListPage list = new ProductListPage(driver, wait);

        // This now waits for the DOM to refresh using Staleness
        list.sortBy("Price");

        java.util.List<Double> prices = list.getPrices();
        for (int i = 0; i < prices.size() - 1; i++) {
            Assert.assertTrue(prices.get(i) <= prices.get(i + 1),
                    "Prices not sorted! " + prices.get(i) + " > " + prices.get(i+1));
        }
    }

    @Test(priority = 7)
    public void test7_ShoppingCart() throws InterruptedException {
        ProductListPage list = new ProductListPage(driver, wait);

        // 1. Ensure we have something in the wishlist!
        driver.get("https://ecommerce.tealiumdemo.com/women.html");
        list.addToWishlist(0);

        // 2. Go to wishlist and move to cart
        list.goToWishlist();
        list.editWishlistItemsSelectOptionsAndAddAllToCart(1);

        // 3. Cart Calculations
        driver.get("https://ecommerce.tealiumdemo.com/checkout/cart/");
        Thread.sleep(5000);
        CartPage cart = new CartPage(driver);
        Assert.assertTrue(cart.getItemCount() > 0, "Expected at least 1 item in cart before calculations");
        cart.setQuantity(0, 2);
        cart.clickUpdateCart();

        double sumSubtotals = cart.calculateSumOfSubtotals();
        double grandTotal = cart.getGrandTotal();

        Assert.assertTrue(grandTotal > 0.0, "Grand Total should be > 0");
        Assert.assertEquals(sumSubtotals, grandTotal, 0.1, "Grand Total matches sum of subtotals");
    }

    @Test(priority = 8)
    public void test8_EmptyCart() throws InterruptedException {
        driver.get("https://ecommerce.tealiumdemo.com/checkout/cart/");
        Thread.sleep(2000);

        // Just click the Empty Cart button
        WebElement emptyBtn = driver.findElement(By.cssSelector("button[name='update_cart_action'][value='empty_cart'], #empty_cart_button"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", emptyBtn);

        Thread.sleep(3000);

        CartPage cart = new CartPage(driver);
        Assert.assertTrue(cart.isCartEmptyMessageDisplayed(), "Empty cart message was not found!");
    }
}
