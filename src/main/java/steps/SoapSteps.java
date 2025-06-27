package steps;
import io.qameta.allure.Step;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;

import java.util.List;
import java.util.stream.Collectors;

import static data.Constants.*;
import static io.restassured.RestAssured.given;
import static org.testng.Assert.*;
public class SoapSteps {
    private static final String SOAP_BODY =
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://www.oorsprong.org/websamples.countryinfo\">\n" +
                    "   <soapenv:Header/>\n" +
                    "   <soapenv:Body>\n" +
                    "      <web:ListOfContinentsByName/>\n" +
                    "   </soapenv:Body>\n" +
                    "</soapenv:Envelope>";

    private Response response;
    private XmlPath xmlPath;
    private List<String> sNames;
    private List<String> sCodes;

    @Step("Call ListOfContinentsByName SOAP service")
    public static SoapSteps callContinentsService() {
        SoapSteps soapSteps = new SoapSteps();
        new SoapSteps().response = given()
                .header("Content-Type", "text/xml;charset=UTF-8")
                .body(SOAP_BODY)
                .when()
                .post(BASE_SOUP_URL)
                .then()
                .statusCode(200)
                .extract()
                .response();

        soapSteps.xmlPath = new XmlPath(soapSteps.response.asString());
        soapSteps.sNames = soapSteps.xmlPath.getList("Envelope.Body.ListOfContinentsByNameResponse.ListOfContinentsByNameResult.tContinent.sName");
        soapSteps.sCodes = soapSteps.xmlPath.getList("Envelope.Body.ListOfContinentsByNameResponse.ListOfContinentsByNameResult.tContinent.sCode");

        return soapSteps;
    }

    @Step("Validate total number of continents is 6")
    public SoapSteps validateCount() {
        assertEquals(sNames.size(), SIX);
        return this;
    }

    @Step("Validate sName values are not empty")
    public SoapSteps validateNotEmptyNames() {
        assertTrue(sNames.stream().noneMatch(String::isEmpty));
        return this;
    }

    @Step("Validate sCode 'AN' corresponds to Antarctica")
    public SoapSteps validateANCode() {
        String sNameOfAN = xmlPath.getString("Envelope.Body.ListOfContinentsByNameResponse.ListOfContinentsByNameResult.tContinent.find { it.sCode == 'AN' }.sName");
        assertEquals(sNameOfAN, ANTARCTICA);
        return this;
    }

    @Step("Validate last continent is South America")
    public SoapSteps validateLastContinent() {
        assertEquals(sNames.getLast(), SOUTH_AMERICA);
        return this;
    }

    @Step("Validate all sNames are unique")
    public SoapSteps validateUniqueNames() {
        assertEquals(sNames.size(), sNames.stream().distinct().count());
        return this;
    }

    @Step("Validate all sCode values match pattern: {REGEX1}")
    public SoapSteps validateSCodePattern() {
        assertTrue(sCodes.stream().allMatch(code -> code.matches(REGEX1)));
        return this;
    }

    @Step("Validate sNames are sorted alphabetically")
    public SoapSteps validateAlphabeticalOrder() {
        List<String> sorted = sNames.stream().sorted().collect(Collectors.toList());
        assertEquals(sNames, sorted);
        return this;
    }

    @Step("Validate no sName contains numeric characters")
    public SoapSteps validateNoNumericNames() {
        assertTrue(sNames.stream().noneMatch(name -> name.matches(REGEX3)));
        return this;
    }

    @Step("Validate sCode starting with 'O' is Oceania")
    public SoapSteps validateOceaniaCode() {
        String oceania = xmlPath.getString("Envelope.Body.ListOfContinentsByNameResponse.ListOfContinentsByNameResult.tContinent.find { it.sCode.startsWith('O') }.sName");
        assertEquals(oceania, OCEANIA);
        return this;
    }

    @Step("Validate sName starts with 'A' and ends with 'ca'")
    public SoapSteps validateAtoCaFilter() {
        List<String> aToCa = sNames.stream()
                .filter(n -> n.startsWith(A) && n.endsWith(CA))
                .toList();
        assertTrue(aToCa.contains(AFRICA) || aToCa.contains(ANTARCTICA));
        return this;
    }
}
