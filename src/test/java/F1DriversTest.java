import enums.Countries;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;

import static data.Constants.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class F1DriversTest {
    @Test(priority = 1)
    public void validateF1Drivers() {
        RestAssured.baseURI = F1_BASE_URI;

        Response response = given()
                .when()
                .get(F1_BASE_URI + F1_2025_DRIVERS_ENDPOINT);

        response.then()
                .statusCode(200)
                .body("MRData.series", equalTo(SERIES_F1))
                .body("MRData.DriverTable.season", equalTo(SEASON_2025));

        int actualSize = response.path("MRData.DriverTable.Drivers.size()");
        int total = Integer.parseInt(response.path("MRData.total"));
        assertThat(actualSize, equalTo(total));

        String driverName = response.path("MRData.DriverTable.Drivers.find { it.dateOfBirth < '1990-01-01' }.givenName") +
                " " + response.path("MRData.DriverTable.Drivers.find { it.dateOfBirth < '1990-01-01' }.familyName");
        assertThat(DRIVER_NAME_NOT_EMPTY, driverName, not(emptyString()));

        List<String> post2000 = response.path("MRData.DriverTable.Drivers.findAll { it.dateOfBirth > '2000-01-01' }.collect { it.givenName + ' ' + it.familyName }");
        assertThat(POST_2000_MIN_COUNT_MSG, post2000.size(), greaterThanOrEqualTo(EIGHT));

        int frenchCount = response.path("MRData.DriverTable.Drivers.findAll { it.nationality == 'French' }.size()");
        assertThat(FRENCH_DRIVERS_EXPECTED, frenchCount, equalTo(THREE));

        int abCount = response.path("MRData.DriverTable.Drivers.findAll { it.familyName ==~ /^[ABab].*/ }.size()");
        assertThat(AB_NAME_START_MIN, abCount, greaterThanOrEqualTo(FIVE));

        List<String> british = response.path("MRData.DriverTable.Drivers.findAll { it.nationality == 'British' && it.dateOfBirth > '1990-01-01' }.collect { it.givenName + ' ' + it.familyName }");
        assertThat(BRITISH_YOUNG_MIN, british.size(), greaterThanOrEqualTo(THREE));

        List<String> specialDrivers = response.path("MRData.DriverTable.Drivers.findAll { it.permanentNumber?.toInteger() < 10 || it.familyName.length() > 7 }.collect { it.givenName + ' ' + it.familyName }");
        System.out.println(specialDrivers);

        assertThat(SPECIAL_DRIVERS_MIN, specialDrivers.size(), greaterThanOrEqualTo(FIVE));

        assertThat(findDriversByNationality(Countries.DUTCH.getValue(), response), hasItem(containsString(EXPECTED_DUTCH_DRIVER)));
        assertThat(findDriversByNationality(Countries.BRAZILIAN.getValue(), response).size(), greaterThanOrEqualTo(FIRST));
        assertThat(findDriversByNationality(Countries.CANADIAN.getValue(), response), hasItem(EXPECTED_CANADIAN_DRIVER));
    }

    public List<String> findDriversByNationality(String nationality, Response response) {
        return response.path("MRData.DriverTable.Drivers.findAll { it.nationality == '" + nationality + "' }.collect { it.givenName + ' ' + it.familyName }");
    }
}
