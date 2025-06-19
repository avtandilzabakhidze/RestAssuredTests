import data.models.pet.Category;
import data.models.pet.*;
import org.testng.annotations.Test;
import steps.PetSteps;

import java.util.List;

import static data.Constants.*;

public class PetStoreXmlTest {
    private final PetSteps petSteps = new PetSteps();

    @Test
    public void testPostNewPetAsXml() {
        Pet pet = Pet.builder()
                .id(THOU)
                .name(NAME)
                .status(AVAILABLE_STATUS)
                .category(Category
                        .builder()
                        .id(FIRST_PET_ID)
                        .name(DOGS_CATEGORY)
                        .build())
                .photoUrls(List.of())
                .tags(List.of(Tag.builder()
                        .id(HUN)
                        .name(NUM)
                        .build()))
                .build();

        Pet responsePet = petSteps.createPet(pet);
        petSteps.validatePet(responsePet, pet);
    }
}
