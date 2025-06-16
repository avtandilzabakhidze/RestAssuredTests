import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import steps.SwapiSteps;

public class SwapiPlanetTest {
    private final SwapiSteps swapiSteps = new SwapiSteps();

    @BeforeClass
    public void setup() {
        swapiSteps.fetchPlanets();
    }

    @Test
    public void validateFieldsOfPlanet() {
        swapiSteps.validatePlanetFields();
    }

    @Test
    public void validateRecentThreeCreatedPlanets() {
        swapiSteps.validateRecentThreeCreatedPlanets();
    }

    @Test
    public void validateTopRotationPeriodPlanet() {
        swapiSteps.validateTopRotationPlanet();
    }
}
