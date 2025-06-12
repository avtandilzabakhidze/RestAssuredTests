import ge.tbc.tbcitacademy.Steps.F1Steps.F1DriversSteps;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class F1DriversTest {
    private F1DriversSteps f1DriversSteps;

    @BeforeMethod
    public void setUp() {
        f1DriversSteps = new F1DriversSteps();
    }

    @Test(priority = 1)
    public void validateF1Drivers() {
        new F1DriversSteps()
                .setBaseUri()
                .sendGetRequest()
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
