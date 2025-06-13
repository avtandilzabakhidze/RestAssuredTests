package steps;

import com.github.javafaker.Faker;
import data.models.auth.AuthRequest;
import data.models.auth.AuthResponse;
import data.models.profile.ProfileResponse;
import data.models.user.UserRequest;
import data.models.user.UserResponse;
import steps.api.LoginApi;

import static data.Constants.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class UserSteps {
    private final Faker faker = new Faker();

    private String email;
    private String name;
    private UserRequest userRequest;
    private AuthRequest authRequest;
    private AuthResponse authResponse;
    private ProfileResponse profileResponse;

    public UserSteps generateRandomUserData() {
        email = faker.internet().emailAddress();
        name = USER;
        return this;
    }

    public UserSteps buildUserRequest() {
        userRequest = UserRequest.builder()
                .name(name)
                .email(email)
                .password(PASSWORD_RANDOM)
                .avatar(AVATAR)
                .build();
        return this;
    }

    public UserSteps createUser() {
        UserResponse response = given()
                .spec(LoginApi.BASE_SPEC)
                .body(userRequest)
                .when()
                .post("/v1/users")
                .then()
                .statusCode(201)
                .extract()
                .as(UserResponse.class);

        assertThat(response.getEmail(), equalTo(email));
        assertThat(response.getName(), equalTo(name));
        assertThat(response.getAvatar(), equalTo(AVATAR));

        return this;
    }

    public UserSteps loginUser() {
        authRequest = AuthRequest.builder()
                .email(email)
                .password(PASSWORD_RANDOM)
                .build();

        authResponse = given()
                .spec(LoginApi.BASE_SPEC)
                .body(authRequest)
                .when()
                .post("/v1/auth/login")
                .then()
                .statusCode(201)
                .extract()
                .as(AuthResponse.class);

        assertThat(authResponse.getAccess_token(), notNullValue());
        assertThat(authResponse.getRefresh_token(), notNullValue());

        return this;
    }

    public UserSteps getUserProfile() {
        profileResponse = given()
                .spec(LoginApi.BASE_SPEC)
                .auth().oauth2(authResponse.getAccess_token())
                .when()
                .get("/v1/auth/profile")
                .then()
                .statusCode(200)
                .extract()
                .as(ProfileResponse.class);

        return this;
    }

    public UserSteps validateUserProfile() {
        assertThat(profileResponse.getEmail(), equalTo(email));
        assertThat(profileResponse.getName(), equalTo(name));
        assertThat(profileResponse.getAvatar(), equalTo(AVATAR));
        return this;
    }
}
