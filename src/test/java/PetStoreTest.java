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

    @Test(priority = 1)
    public void addAndUpdatePetTest() {
        Integer petId = new Random().nextInt(FIRST_RANDOM, LAST_RANDOM);
        String petName = faker.animal().name();

        JSONObject category = new JSONObject();
        category.put("id", CATEGORY_ID);
        category.put("name", DOGS_CATEGORY);

        JSONObject petRequest = new JSONObject();
        petRequest.put("id", petId);
        petRequest.put("name", petName);
        petRequest.put("status", AVAILABLE_STATUS);
        petRequest.put("category", category);

        given()
                .baseUri(PETSTORE_BASE_URI)
                .contentType(ContentType.JSON)
                .body(petRequest.toString())
                .when()
                .post(PET_ENDPOINT)
                .then()
                .statusCode(200)
                .body("id", equalTo((Number) petId))
                .body("name", equalTo(petName))
                .body("status", equalTo(AVAILABLE_STATUS));

        Response findResponse = await()
                .atMost(TEN, TimeUnit.SECONDS)
                .pollInterval(FIRST, TimeUnit.SECONDS)
                .until(() -> {
                    Response response = given()
                            .baseUri(PETSTORE_BASE_URI)
                            .queryParam("status", AVAILABLE_STATUS)
                            .when()
                            .get(PET_FIND_BY_STATUS_ENDPOINT);

                    List<Integer> ids = response.path("id");
                    return ids != null && ids.contains(petId) ? response : null;
                }, response -> response != null);

        List<Integer> petIds = findResponse.path("id");
        assertThat(petIds, hasItem(petId));

        Map<String, Object> foundPet = findResponse.jsonPath()
                .getMap("find { it.id == " + petId + " }");

        assertThat(foundPet.get("name").toString(), equalTo(petName));
        assertThat(foundPet.get("status").toString(), equalTo(AVAILABLE_STATUS));
        assertThat(((Map<?, ?>) foundPet.get("category")).get("name").toString(), equalTo(DOGS_CATEGORY));

        String newName = faker.animal().name();
        String newStatus = SOLD_STATUS;

        given()
                .baseUri(PETSTORE_BASE_URI)
                .contentType(ContentType.URLENC)
                .formParam("name", newName)
                .formParam("status", newStatus)
                .when()
                .post(PET_ENDPOINT_WITH_ID, petId)
                .then()
                .statusCode(200)
                .body(MESSAGE, equalTo(String.valueOf(petId)));

        given()
                .baseUri(PETSTORE_BASE_URI)
                .when()
                .get(PET_ENDPOINT_WITH_ID, petId)
                .then()
                .statusCode(200)
                .body("name", equalTo(newName))
                .body("status", equalTo(newStatus));
    }

    @Test(priority = 2)
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
