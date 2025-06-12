package steps;

import data.BookingRequests;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;

import static data.Constants.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BookingSteps {
    private int firstBookingId;
    private JSONObject bookingPayload;
    private Response updateResponse;
    private Response bookstoreResponse;

    private final BookingRequests bookingRequests = new BookingRequests();

    public BookingSteps fetchFirstBookingId() {
        firstBookingId = given()
                .baseUri(BOOKING_BASE_URI)
                .when()
                .get(BOOKING_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getInt("[0].bookingid");
        return this;
    }

    public BookingSteps buildBookingPayload() {
        bookingPayload = bookingRequests.getBookingPayload();
        return this;
    }

    public BookingSteps updateBooking() {
        updateResponse = given()
                .baseUri(BOOKING_BASE_URI)
                .auth().preemptive().basic(ADMIN_USERNAME, ADMIN_PASSWORD)
                .contentType(ContentType.JSON)
                .body(bookingPayload.toString())
                .when()
                .put(BOOKING_ENDPOINT + SLASH + firstBookingId);
        return this;
    }

    public BookingSteps validateUpdateResponse() {
        int statusCode = updateResponse.then().extract().statusCode();
        assertThat(CODE_200, statusCode, equalTo(200));
        return this;
    }

    public BookingSteps logDataIfStatusCodeIS201() {
        int statusCode = updateResponse.then().extract().statusCode();

        if (statusCode == 201) {
            bookstoreResponse.prettyPrint();
        }

        return this;
    }

    public BookingSteps setBookstoreBaseUri() {
        RestAssured.baseURI = BOOKSTORE_BASE_URI;
        return this;
    }

    public BookingSteps sendGetBooksRequest() {
        bookstoreResponse = given()
                .contentType(ContentType.JSON)
                .when()
                .get(BOOKSTORE_BOOKS_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .response();
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
