package steps;

import com.github.javafaker.Faker;
import data.models.petstore.Category;
import data.models.petstore.Pet;
import io.restassured.response.Response;
import steps.api.PetStoreApi;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static data.Constants.*;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class PetStoreSteps {
    private final Faker faker = new Faker();
    private final PetStoreApi petStoreApi = new PetStoreApi();

    private int petId;
    private String petName;
    private String newName;
    private String newStatus;
    private Response uploadResponse;

    public PetStoreSteps createRandomPet() {
        petId = new Random().nextInt(FIRST_RANDOM, LAST_RANDOM);
        petName = faker.animal().name();

        Category category = new Category(CATEGORY_ID, DOGS_CATEGORY);
        Pet petRequest = new Pet(petId, petName, AVAILABLE_STATUS, category);

        Response response = petStoreApi.createPet(petRequest);

        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.path("id"), equalTo(petId));
        assertThat(response.path("name"), equalTo(petName));
        assertThat(response.path("status"), equalTo(AVAILABLE_STATUS));

        return this;
    }

    public PetStoreSteps verifyPetInList() {
        Response findResponse = await()
                .atMost(TIME, TimeUnit.SECONDS)
                .pollInterval(FIRST, TimeUnit.SECONDS)
                .until(() -> {
                    Response r = petStoreApi.findPetsByStatus(AVAILABLE_STATUS);
                    List<Integer> ids = r.path("id");
                    return ids != null && ids.contains(petId) ? r : null;
                }, response -> response != null);

        List<Integer> ids = findResponse.path("id");
        assertThat(ids, hasItem(petId));

        Map<String, Object> foundPet = findResponse.jsonPath()
                .getMap("find { it.id == " + petId + " }");

        assertThat(foundPet.get("name"), equalTo(petName));
        assertThat(foundPet.get("status"), equalTo(AVAILABLE_STATUS));
        assertThat(((Map<?, ?>) foundPet.get("category")).get("name"), equalTo(DOGS_CATEGORY));

        return this;
    }

    public PetStoreSteps updatePetDetails() {
        newName = faker.animal().name();
        newStatus = SOLD_STATUS;

        Response response = petStoreApi.updatePet(petId, newName, newStatus);

        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.path(MESSAGE), equalTo(String.valueOf(petId)));

        return this;
    }

    public PetStoreSteps verifyUpdatedPetDetails() {
        Response response = petStoreApi.getPetById(petId);

        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.path("name"), equalTo(newName));
        assertThat(response.path("status"), equalTo(newStatus));

        return this;
    }

    public PetStoreSteps uploadPetImage(File imageFile) {
        long expectedFileSize = imageFile.length();

        uploadResponse = petStoreApi.uploadPetImage(FIRST, imageFile, ADDITIONAL_METADATA);

        assertThat(uploadResponse.statusCode(), equalTo(200));
        assertThat(uploadResponse.path("message"), allOf(
                containsString(ADDITIONAL_METADATA),
                containsString(imageFile.getName())
        ));

        String message = uploadResponse.path(MESSAGE);

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
        return this;
    }
}
