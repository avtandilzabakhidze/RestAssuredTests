package steps;

import data.BookingRequests;
import io.restassured.response.Response;
import org.json.JSONObject;
import steps.api.BookingApi;

import static data.Constants.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BookingSteps {
    private int firstBookingId;
    private JSONObject bookingPayload;
    private Response updateResponse;
    private Response bookstoreResponse;

    private final BookingApi bookingApi = new BookingApi();
    private final BookingRequests bookingRequests = new BookingRequests();

    public BookingSteps fetchFirstBookingId() {
        Response response = bookingApi.getAllBookings();
        firstBookingId = response.jsonPath().getInt("[0].bookingid");
        return this;
    }

    public BookingSteps buildBookingPayload() {
        bookingPayload = bookingRequests.getBookingPayload();
        return this;
    }

    public BookingSteps updateBooking() {
        updateResponse = bookingApi.updateBooking(firstBookingId, bookingPayload);
        return this;
    }

    public BookingSteps validateUpdateResponse() {
        int statusCode = updateResponse.then().extract().statusCode();
        assertThat(CODE_200, statusCode, equalTo(200));
        return this;
    }

    public BookingSteps logDataIfStatusCodeIS201() {
        int statusCode = updateResponse.getStatusCode();
        if (statusCode == 201) {
            updateResponse.prettyPrint();
        }
        return this;
    }

    public BookingSteps sendGetBooksRequest() {
        bookstoreResponse = bookingApi.getBooks();
        return this;
    }

    public BookingSteps validatePagesLessThan1000() {
        assertThat(PAGES_COUNT_INVALID,
                bookstoreResponse.jsonPath().getList(BOOKS + ".pages"),
                everyItem(lessThan(THOUSAND)));
        return this;
    }

    public BookingSteps validateFirstTwoAuthors() {
        String firstAuthor = bookstoreResponse.jsonPath().getString(BOOKS + "[0].author");
        String secondAuthor = bookstoreResponse.jsonPath().getString(BOOKS + "[1].author");

        assertThat(AUTHOR_MISMATCH, firstAuthor, equalTo(FIRST_AUTH));
        assertThat(AUTHOR_MISMATCH, secondAuthor, equalTo(SECOND_AUTH));
        return this;
    }
}
