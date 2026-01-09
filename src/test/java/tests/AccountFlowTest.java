package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.LoginPage;
import pages.RegisterPage;
import utils.TestData;

public class AccountFlowTest extends BaseTest {

    @Test
    public void testCreateAccount() {
        TestData.firstName = "Deni";
        TestData.lastName = "Zeqo";
        TestData.fullName = TestData.firstName + " " + TestData.lastName;

        TestData.email = "deni+" + System.currentTimeMillis() + "@testmail.com";
        TestData.password = "Test@12345";

        HomePage home = new HomePage(driver, wait).open();
        RegisterPage registerPage = home.goToRegister();

        // 1) Assert title
        String title = registerPage.getTitle().toLowerCase();
        Assert.assertTrue(title.contains("create") && title.contains("account"),
                "Register page title unexpected: " + registerPage.getTitle());

        // 2) Fill + register
        registerPage.register(TestData.firstName, TestData.lastName, TestData.email, TestData.password, TestData.password);

        // 3) Assert success message
        String msg = registerPage.getSuccessMessage().toLowerCase();
        Assert.assertTrue(msg.contains("thank you for registering") || msg.contains("thank you"),
                "Success message unexpected/missing: " + msg);

        // 4) Logout
        new HomePage(driver, wait).logout();
    }

    @Test(dependsOnMethods = "testCreateAccount")
    public void testSignIn() {
        HomePage home = new HomePage(driver, wait).open();
        LoginPage loginPage = home.goToSignIn();

        home = loginPage.login(TestData.email, TestData.password);

        // Assert username displayed (welcome text)
        Assert.assertTrue(home.isUsernameDisplayed(TestData.fullName),
                "Username not displayed. Expected: " + TestData.fullName);

        // Logout
        home.logout();
    }
}
