import com.github.javafaker.Faker;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static data.Constants.*;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PetStoreTest {
    Faker faker = new Faker();

    @Test
    public void addAndUpdatePetTest() {
        Integer petId = new Random().nextInt(1000000, 9999999);
        String petName = faker.animal().name();
        String petStatus = "available";

        JSONObject category = new JSONObject();
        category.put("id", 1);
        category.put("name", "Dogs");

        JSONObject petRequest = new JSONObject();
        petRequest.put("id", petId);
        petRequest.put("name", petName);
        petRequest.put("status", petStatus);
        petRequest.put("category", category);

        given()
                .baseUri("https://petstore.swagger.io/v2")
                .contentType(ContentType.JSON)
                .body(petRequest.toString())
                .when()
                .post("/pet")
                .then()
                .statusCode(200)
                .body("id", equalTo((Number) petId))
                .body("name", equalTo(petName))
                .body("status", equalTo(petStatus));

        Response findResponse = await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> {
                    Response response = given()
                            .baseUri("https://petstore.swagger.io/v2")
                            .queryParam("status", petStatus)
                            .when()
                            .get("/pet/findByStatus");

                    List<Integer> ids = response.path("id");
                    return ids != null && ids.contains(petId) ? response : null;
                }, response -> response != null);

        List<Integer> petIds = findResponse.path("id");
        assertThat(petIds, hasItem(petId));

        Map<String, Object> foundPet = findResponse.jsonPath()
                .getMap("find { it.id == " + petId + " }");

        assertThat(foundPet.get("name").toString(), equalTo(petName));
        assertThat(foundPet.get("status").toString(), equalTo(petStatus));
        assertThat(((Map<?, ?>) foundPet.get("category")).get("name").toString(), equalTo("Dogs"));

        String newName = faker.animal().name();
        String newStatus = "sold";

        given()
                .baseUri("https://petstore.swagger.io/v2")
                .contentType(ContentType.URLENC)
                .formParam("name", newName)
                .formParam("status", newStatus)
                .when()
                .post("/pet/{petId}", petId)
                .then()
                .statusCode(200)
                .body("message", equalTo(String.valueOf(petId)));

        Response finalResponse = given()
                .baseUri("https://petstore.swagger.io/v2")
                .when()
                .get("/pet/{petId}", petId)
                .then()
                .statusCode(200)
                .body("name", equalTo(newName))
                .body("status", equalTo(newStatus))
                .extract().response();
    }

    @Test
    public void uploadPetImage() {
        long expectedFileSize = PICTURE_FILE.length();

        Response response = given()
                .baseUri(PETSTORE_BASE_URI)
                .contentType(ContentType.MULTIPART)
                .multiPart("file", PICTURE_FILE)
                .multiPart("additionalMetadata", ADDITIONAL_METADATA)
                .pathParam("petId", FIRST_PET_ID)
                .when()
                .post("/pet/{petId}/uploadImage")
                .then()
                .statusCode(200)
                .body("message", allOf(
                        containsString(ADDITIONAL_METADATA),
                        containsString(PICTURE_FILE.getName())
                ))
                .extract()
                .response();

        String message = response.path(MESSAGE);

        Matcher matcher = Pattern.compile(SIZE_REGEX).matcher(message);
        long actualSize = NEGATIVE_ONE;
        while (matcher.find()) {
            long candidate = Long.parseLong(matcher.group());
            if (candidate == expectedFileSize) {
                actualSize = candidate;
                break;
            }
        }

        assertThat(FILE_NOT_MATCH, actualSize, equalTo(expectedFileSize));
    }
}
