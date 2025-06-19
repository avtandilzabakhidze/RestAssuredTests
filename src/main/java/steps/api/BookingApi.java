package steps.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;

import static data.Constants.*;
import static io.restassured.RestAssured.given;

public class BookingApi {
    public Response getAllBookings() {
        return given()
                .baseUri(BOOKING_BASE_URI)
                .when()
                .get(BOOKING_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .response();
    }

    public Response updateBooking(int bookingId, JSONObject bookingPayload) {
        return given()
                .baseUri(BOOKING_BASE_URI)
                .auth().preemptive().basic(ADMIN_USERNAME, ADMIN_PASSWORD)
                .contentType(ContentType.JSON)
                .body(bookingPayload.toString())
                .when()
                .put(BOOKING_ENDPOINT + SLASH + bookingId)
                .then()
                .extract()
                .response();
    }

    public Response getBooks() {
        return given()
                .baseUri(BOOKSTORE_BASE_URI)
                .contentType(ContentType.JSON)
                .when()
                .get(BOOKSTORE_BOOKS_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .response();
    }
}
