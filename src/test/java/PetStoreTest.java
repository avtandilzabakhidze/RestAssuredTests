import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import steps.PetStoreSteps;

import static data.Constants.PICTURE_FILE;

public class PetStoreTest {
    private PetStoreSteps petStoreSteps;

    @BeforeMethod
    public void setUp() {
        petStoreSteps = new PetStoreSteps();
    }

    @Test(priority = 1)
    public void addAndUpdatePetTest() {
        petStoreSteps
                .createRandomPet()
                .verifyPetInList()
                .updatePetDetails()
                .verifyUpdatedPetDetails();
    }

    @Test(priority = 2)
    public void uploadPetImage() {
        petStoreSteps
                .uploadPetImage(PICTURE_FILE);
    }
}
