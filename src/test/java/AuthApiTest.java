package tests;

import com.github.javafaker.Faker;
import org.testng.Assert;
import org.testng.annotations.*;
import steps.AuthApiSteps;

public class AuthApiTest {

    private AuthApiSteps steps;
    private Faker faker;

    @BeforeClass
    public void setUp() {
        steps = new AuthApiSteps();
        faker = new Faker();
    }

    @Test(dataProvider = "invalidPasswords")
    public void testInvalidRegistration(String invalidPassword) {
            steps.registerWithInvalidPassword(invalidPassword, faker.internet().emailAddress());
    }

    @Test(priority = 1)
    public void testValidRegistration() {
        steps.registerValidAdmin();
    }

    @Test(priority = 2)
    public void testAuthorizationWithAccessToken() {
        steps.authorizeAndValidateProtectedResource();
    }

    @Test(priority = 3)
    public void testAuthenticateAndValidateRoles() {
        steps.authenticateAndValidateRoles();
    }

    @Test(priority = 4)
    public void testRefreshToken() {
        steps.refreshToken();
    }

    @Test(priority = 5)
    public void testOldTokenStillValid() {
        steps.verifyTokenStillValid();
    }
}
