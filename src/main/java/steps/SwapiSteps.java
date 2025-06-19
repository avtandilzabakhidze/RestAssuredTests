package steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import data.Constants;
import data.models.responses.PlanetRecord;
import data.models.responses.PlanetResponse;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

import static data.Constants.IS_TRUE;
import static data.Constants.PLANET;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SwapiSteps {
    private List<PlanetRecord> planets;

    public SwapiSteps() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((cls, charset) -> mapper)
        );
    }

    @Step("Fetch all full planets from SWAPI")
    public SwapiSteps fetchPlanets() {
        RestAssured.baseURI = Constants.SWAPI_BASE_URI;

        PlanetResponse response = given()
                .filter(new io.qameta.allure.restassured.AllureRestAssured())
                .log().ifValidationFails(LogDetail.ALL)
                .queryParam(Constants.QUERY_PARAM_FORMAT, Constants.SWAPI_QUERY_FORMAT)
                .when()
                .get(Constants.SWAPI_PLANETS_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .as(PlanetResponse.class);

        this.planets = response.getResults().stream()
                .map(summary -> fetchFullPlanet(summary.getUrl()))
                .toList();

        return this;
    }

    @Step("Fetch full planet data from URL: {url}")
    private PlanetRecord fetchFullPlanet(String url) {
        return given()
                .filter(new io.qameta.allure.restassured.AllureRestAssured())
                .log().ifValidationFails(LogDetail.ALL)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(Constants.JSON_PATH_PLANET, PlanetRecord.class);
    }

    @Step("Validate 5 fields on first planet")
    public SwapiSteps validatePlanetFields() {
        PlanetRecord planet = planets.getFirst();
        assertThat(Constants.EMPTY_PLANET_NAME, planet.name(), not(isEmptyString()));
        assertThat(Constants.ROTATION_PERIOD_NOT_NUMERIC, planet.rotationPeriod(), matchesPattern(Constants.DIGITS_REGEX));
        assertThat(Constants.NULL_ORBITAL_PERIOD, planet.orbitalPeriod(), notNullValue());
        assertThat(Constants.EMPTY_CLIMATE, planet.climate(), not(isEmptyString()));
        assertThat(Constants.INVALID_CREATION_DATE, planet.created(), lessThan(OffsetDateTime.now()));
        return this;
    }

    @Step("Validate 3 most recent planets are returned and sorted by created date")
    public SwapiSteps validateRecentThreeCreatedPlanets() {
        List<PlanetRecord> recent = getMostRecentPlanets();
        assertThat(PLANET, recent, hasSize(Constants.EXPECTED_RECENT_PLANET_COUNT));
        validateRecentPlanetsSortedByCreatedDate(recent);
        return this;
    }

    @Step("Validate top rotation period planet is greater than threshold and has name")
    public SwapiSteps validateTopRotationPlanet() {
        PlanetRecord top = getTopByRotation();
        assertThat(PLANET, Integer.parseInt(top.rotationPeriod()), greaterThan(20));
        assertThat(PLANET, top.name(), notNullValue());
        return this;
    }

    @Step("Get 3 most recent planets by created timestamp")
    public List<PlanetRecord> getMostRecentPlanets() {
        return planets.stream()
                .sorted(Comparator.comparing(PlanetRecord::created).reversed())
                .limit(Constants.EXPECTED_RECENT_PLANET_COUNT)
                .toList();
    }

    @Step("Get top planet by rotation period")
    public PlanetRecord getTopByRotation() {
        return planets.stream()
                .filter(p -> p.rotationPeriod().matches(Constants.DIGITS_REGEX))
                .max(Comparator.comparingInt(p -> Integer.parseInt(p.rotationPeriod())))
                .orElseThrow();
    }

    @Step("Validate recent planets are sorted by created date descending and print timestamps")
    public SwapiSteps validateRecentPlanetsSortedByCreatedDate(List<PlanetRecord> recentPlanets) {
        for (PlanetRecord planet : recentPlanets) {
            System.out.println(planet.created());
        }

        for (int i = 0; i < recentPlanets.size() - 1; i++) {
            OffsetDateTime current = recentPlanets.get(i).created();
            OffsetDateTime next = recentPlanets.get(i + 1).created();
            assertThat(
                    Constants.CREATED_DATE_SORT_ERROR_PREFIX + i,
                    current.isAfter(next) || current.isEqual(next),
                    is(IS_TRUE)
            );
        }

        return this;
    }

    public List<PlanetRecord> getPlanets() {
        return planets;
    }
}
