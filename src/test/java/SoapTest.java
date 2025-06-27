import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import steps.SoapSteps;

public class SoapTest {
    private SoapSteps steps;

    @BeforeMethod
    public void setUp() {
        steps = new SoapSteps();
    }

    @Test
    public void validateContinentsResponse() {
        steps
                .callContinentsService()
                .validateCount()
                .validateNotEmptyNames()
                .validateANCode()
                .validateLastContinent()
                .validateUniqueNames()
                .validateSCodePattern()
                .validateAlphabeticalOrder()
                .validateNoNumericNames()
                .validateOceaniaCode()
                .validateAtoCaFilter();
    }
}
