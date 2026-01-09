package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.RegisterPage;

public class RegisterSmokeTest extends BaseTest {

    @Test
    public void registerAssertSuccessAndLogout() {
        String first = "Deni";
        String last = "Zeqo";
        String email = "deni+" + System.currentTimeMillis() + "@testmail.com";
        String password = "Test@12345";

        HomePage home = new HomePage(driver, wait).open();
        RegisterPage registerPage = home.goToRegister();

        String title = registerPage.getTitle().toLowerCase();

        Assert.assertTrue(
                title.contains("create") && title.contains("account"),
                "Register page title was unexpected: " + registerPage.getTitle()
        );
        registerPage.register(first, last, email, password, password);

        String msg = registerPage.getSuccessMessage();
        Assert.assertTrue(msg.toLowerCase().contains("thank you for registering")
                        || msg.toLowerCase().contains("thank you"),
                "Success message not shown / unexpected: " + msg);

        // logout
        new HomePage(driver, wait).logout();
    }
}
