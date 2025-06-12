package tests;

import api.F1DriversApi;
import io.restassured.response.Response;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import steps.F1DriversSteps;

import static data.Constants.F1_BASE_URI;

public class F1DriversTest {

    private F1DriversApi f1DriversApi;

    @BeforeMethod
    public void setUp() {
        f1DriversApi = new F1DriversApi();
    }

    @Test
    public void validateF1DriversTest() {
        Response response = f1DriversApi.getF1Drivers(F1_BASE_URI);

        new F1DriversSteps(response)
                .validateStatusAndMetadata()
                .validateDriverCount()
                .validateOldDriver()
                .validatePost2000Drivers()
                .validateFrenchDriversCount()
                .validateDriversStartingWithAB()
                .validateYoungBritishDrivers()
                .validateSpecialDrivers()
                .validateNationalityChecks();
    }
}
