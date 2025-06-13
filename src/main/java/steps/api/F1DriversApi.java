package steps.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import static data.Constants.F1_2025_DRIVERS_ENDPOINT;
import static io.restassured.RestAssured.given;

public class F1DriversApi {
    public Response getF1Drivers(String baseUri) {
        RestAssured.baseURI = baseUri;
        return given()
                .when()
                .get(F1_2025_DRIVERS_ENDPOINT);
    }
}
