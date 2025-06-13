package api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;

import java.io.File;

import static data.Constants.*;
import static io.restassured.RestAssured.given;

public class PetStoreApi {
    public Response createPet(JSONObject petRequest) {
        return given()
                .baseUri(PETSTORE_BASE_URI)
                .contentType(ContentType.JSON)
                .body(petRequest.toString())
                .when()
                .post(PET_ENDPOINT)
                .then()
                .extract()
                .response();
    }

    public Response findPetsByStatus(String status) {
        return given()
                .baseUri(PETSTORE_BASE_URI)
                .queryParam("status", status)
                .when()
                .get(PET_FIND_BY_STATUS_ENDPOINT)
                .then()
                .extract()
                .response();
    }

    public Response updatePet(int petId, String name, String status) {
        return given()
                .baseUri(PETSTORE_BASE_URI)
                .contentType(ContentType.URLENC)
                .formParam("name", name)
                .formParam("status", status)
                .when()
                .post(PET_ENDPOINT_WITH_ID, petId)
                .then()
                .extract()
                .response();
    }

    public Response getPetById(int petId) {
        return given()
                .baseUri(PETSTORE_BASE_URI)
                .when()
                .get(PET_ENDPOINT_WITH_ID, petId)
                .then()
                .extract()
                .response();
    }

    public Response uploadPetImage(int petId, File imageFile, String metadata) {
        return given()
                .baseUri(PETSTORE_BASE_URI)
                .contentType(ContentType.MULTIPART)
                .multiPart("file", imageFile)
                .multiPart("additionalMetadata", metadata)
                .pathParam("petId", petId)
                .when()
                .post("/pet/{petId}/uploadImage")
                .then()
                .extract()
                .response();
    }
}
