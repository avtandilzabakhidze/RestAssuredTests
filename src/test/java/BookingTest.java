import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import steps.BookingSteps;

public class BookingTest {
    private BookingSteps bookingSteps;

    @BeforeMethod
    public void setUp() {
        bookingSteps = new BookingSteps();
    }

    @Test(priority = 1)
    public void updateBooking() {
        bookingSteps
                .fetchFirstBookingId()
                .buildBookingPayload()
                .updateBooking()
                .validateUpdateResponse()
                .logDataIfStatusCodeIS201();
    }

    @Test(priority = 2)
    public void validateBooksPagesAndAuthors() {
        bookingSteps
                .setBookstoreBaseUri()
                .sendGetBooksRequest()
                .validatePagesLessThan1000()
                .validateFirstTwoAuthors();
    }
}
