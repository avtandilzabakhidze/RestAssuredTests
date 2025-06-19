import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import steps.UserSteps;

public class UserTest {
    private UserSteps userSteps;

    @BeforeMethod
    public void setUp() {
        userSteps = new UserSteps();
    }

    @Test
    public void userCreationAndAuthFlow() {
        userSteps
                .generateRandomUserData()
                .buildUserRequest()
                .createUser()
                .loginUser()
                .getUserProfile()
                .validateUserProfile();
    }
}
