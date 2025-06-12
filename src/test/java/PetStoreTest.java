import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static data.Constants.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PetStoreTest {
    private final Random random = new Random();

    @Test
    public void addUpdateAndValidatePet() {
        // 1. Create a random pet ID and initial data
        long petId = random.nextInt(10000) + 100000;  // safer bigger id
        String initialName = "Buddy" + petId;
        String initialStatus = "available";

        // Build JSON body for new pet
        JSONObject petBody = new JSONObject();
        petBody.put("id", petId);
        petBody.put("name", initialName);
        petBody.put("status", initialStatus);

        // Category (optional)
        JSONObject category = new JSONObject();
        category.put("id", 0);
        category.put("name", "dogs");
        petBody.put("category", category);

        // Tags (optional)
        petBody.put("tags", List.of(
                new JSONObject().put("id", 0).put("name", "friendly"),
                new JSONObject().put("id", 1).put("name", "cute")
        ));

        // Photos URLs (optional)
        petBody.put("photoUrls", List.of("https://example.com/photo1.jpg"));

        // 2. Add the pet to the store
        Response createResponse = given()
                .baseUri("https://petstore.swagger.io/v2")
                .contentType(ContentType.JSON)
                .body(petBody.toString())    // pass JSON string here!
                .when()
                .post("/pet");

        createResponse.then()
                .statusCode(200);

        // 3. Find pets by status - should include our pet
        Response findResponse = given()
                .baseUri("https://petstore.swagger.io/v2")
                .queryParam("status", initialStatus)
                .when()
                .get("/pet/findByStatus");

        findResponse.then()
                .statusCode(200);

        // Validate that the pet ID is in the response array
        List<Long> petIds = findResponse.jsonPath().getList("id", Long.class);
        assertThat(petIds, hasItem(petId));

        // Extract the pet info using JsonPath with correct petId filtering
        String foundName = findResponse.jsonPath().getString("find { it.id == " + petId + " }.name");
        String foundStatus = findResponse.jsonPath().getString("find { it.id == " + petId + " }.status");

        assertThat(foundName, equalTo(initialName));
        assertThat(foundStatus, equalTo(initialStatus));

        // 4. Update the pet name and status via form data
        String updatedName = initialName + "_Updated";
        String updatedStatus = "sold";

        given()
                .baseUri("https://petstore.swagger.io/v2")
                .contentType(ContentType.URLENC)
                .formParam("name", updatedName)
                .formParam("status", updatedStatus)
                .when()
                .post("/pet/" + petId)
                .then()
                .statusCode(200);

        // 5. Get the pet by ID and verify the updated fields
        Response getResponse = given()
                .baseUri("https://petstore.swagger.io/v2")
                .when()
                .get("/pet/" + petId);

        getResponse.then()
                .statusCode(200)
                .body("name", equalTo(updatedName))
                .body("status", equalTo(updatedStatus));
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
