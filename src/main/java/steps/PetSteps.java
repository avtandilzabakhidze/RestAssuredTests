package steps;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import data.models.pet.Pet;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;

import static data.Constants.PETSTORE_BASE_URI_V3;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PetSteps {
    public PetSteps() {
        XmlMapper xmlMapper = new XmlMapper();
        RestAssured.config = RestAssured.config()
                .objectMapperConfig(new ObjectMapperConfig(ObjectMapperType.JACKSON_2)
                        .jackson2ObjectMapperFactory((cls, charset) -> xmlMapper));
    }

    @Step("Send POST /pet with Pet payload")
    public Pet createPet(Pet pet) {
        return RestAssured
                .given()
                .baseUri(PETSTORE_BASE_URI_V3)
                .contentType(ContentType.XML)
                .accept(ContentType.XML)
                .body(pet)
                .when()
                .post("/pet")
                .then()
                .extract()
                .as(Pet.class);
    }

    @Step("Validate created pet matches expected pet")
    public PetSteps validatePet(Pet actual, Pet expected) {
        assertThat(actual.getId(), equalTo(expected.getId()));
        assertThat(actual.getName(), equalTo(expected.getName()));
        assertThat(actual.getStatus(), equalTo(expected.getStatus()));
        return this;
    }
}
