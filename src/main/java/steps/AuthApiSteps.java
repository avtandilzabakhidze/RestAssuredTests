package steps;

import com.example.api.AuthenticationApi;
import com.example.api.AuthorizationApi;
import com.example.invoker.ApiClient;
import com.example.model.*;
import data.Constants;
import io.qameta.allure.Step;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.testng.Assert.*;

public class AuthApiSteps {
    private final AuthenticationApi authApi;
    private AuthorizationApi authzApi;
    private String accessToken;
    private String refreshToken;
    private final String testEmail;

    public AuthApiSteps() {
        ApiClient client = new ApiClient();
        client.setBasePath(Constants.BASE_URL);
        this.authApi = new AuthenticationApi(client);
        this.testEmail = "admin" + UUID.randomUUID() + "@test.com";
    }

    @Step("Attempt to register user with password: {password}")
    public AuthApiSteps registerWithInvalidPassword(String password, String email) {
        RegisterRequest request = new RegisterRequest()
                .firstname("Fail")
                .lastname("Case")
                .email(email)
                .password(password)
                .role(Constants.ROLE_ADMIN);

        authApi.register(request);

        return this;
    }

    @Step("Register a valid admin user")
    public AuthApiSteps registerValidAdmin() {
        RegisterRequest request = new RegisterRequest()
                .firstname(Constants.VALID_FIRST_NAME)
                .lastname(Constants.VALID_LAST_NAME)
                .email(testEmail)
                .password(Constants.VALID_PASSWORD)
                .role(Constants.ROLE_ADMIN);

        AuthenticationResponse response = authApi.register(request);
        assertNotNull(response.getAccessToken());
        this.accessToken = response.getAccessToken();
        this.refreshToken = response.getRefreshToken();

        return this;
    }

    @Step("Authorize using access token and verify protected resource response")
    public AuthApiSteps authorizeAndValidateProtectedResource() {
        ApiClient client = new ApiClient();
        client.setBasePath(Constants.BASE_URL);
        client.setBearerToken(accessToken);
        authzApi = new AuthorizationApi(client);

        String response = authzApi.sayHelloWithRoleAdminAndReadAuthority();
        assertTrue(response.contains(Constants.PROTECTED_RESOURCE_MESSAGE));

        return this;
    }

    @Step("Authenticate user and validate all expected roles")
    public AuthApiSteps authenticateAndValidateRoles() {
        AuthenticationRequest request = new AuthenticationRequest()
                .email(testEmail)
                .password(Constants.VALID_PASSWORD);

        AuthenticationResponse response = authApi.authenticate(request);
        List<String> actualRoles = response.getRoles();
        assertTrue(actualRoles.containsAll(Arrays.asList(Constants.EXPECTED_ROLES)),
                ctualRoles);

        return this;
    }

    @Step("Send refresh token request and update new access token")
    public AuthApiSteps refreshToken() {
        RefreshTokenRequest request = new RefreshTokenRequest()
                .refreshToken(this.refreshToken);

        RefreshTokenResponse response = authApi.refreshToken(request);
        assertNotNull(response.getAccessToken());
        assertNotEquals(response.getAccessToken(), accessToken);
        this.accessToken = response.getAccessToken();

        return this;
    }

    @Step("Verify old token is still valid (after refresh)")
    public AuthApiSteps verifyTokenStillValid() {
        ApiClient client = new ApiClient();
        client.setBasePath(Constants.BASE_URL);
        client.setBearerToken(accessToken);

        AuthorizationApi authzApi = new AuthorizationApi(client);
        String response = authzApi.sayHelloWithRoleAdminAndReadAuthority();
        assertTrue(response.contains(Constants.PROTECTED_RESOURCE_MESSAGE));

        return this;
    }
}
