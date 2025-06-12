import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import static data.Constants.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BookingTest {
    @Test(priority = 1)
    public void updateBooking() {
        int firstBookingId = given()
                .baseUri(BOOKING_BASE_URI)
                .when()
                .get(BOOKING_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getInt("[0].bookingid");

        JSONObject booking = new JSONObject();
        booking.put("firstname", FIRST_NAME);
        booking.put("lastname", LAST_NAME);
        booking.put("totalprice", TOTAL_PRICE);
        booking.put("depositpaid", IS_TRUE);

        JSONObject bookingDates = new JSONObject();
        bookingDates.put("checkin", CHECKIN_DATE);
        bookingDates.put("checkout", CHECKOUT_DATE);
        booking.put("bookingdates", bookingDates);

        booking.put("additionalneeds", ADDITIONAL_NEEDS);

        Response response = given()
                .baseUri(BOOKING_BASE_URI)
                .auth().preemptive().basic(ADMIN_USERNAME, ADMIN_PASSWORD)
                .contentType(ContentType.JSON)
                .body(booking.toString())
                .when()
                .put(BOOKING_ENDPOINT + SLASH + firstBookingId);

        int statusCode = response.then().extract().statusCode();
        Assert.assertEquals(statusCode, 200, CODE_401);

        if (statusCode == 201) {
            response.prettyPrint();
        }
    }

    @Test(priority = 2)
    public void validateBooksPagesAndAuthors() {
        RestAssured.baseURI = BOOKSTORE_BASE_URI;

        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(BOOKSTORE_BOOKS_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertThat(PAGES_COUNT_INVALID,
                response.jsonPath().getList(BOOKS + ".pages"),
                everyItem(lessThan(QUAN)));

        String firstAuthor = response.jsonPath().getString(BOOKS + "[0].author");
        String secondAuthor = response.jsonPath().getString(BOOKS + "[1].author");

        assertThat(AUTHOR_MISMATCH, firstAuthor, equalTo(FIRST_AUTH));
        assertThat(AUTHOR_MISMATCH, secondAuthor, equalTo(SECOND_AUTH));
    }
}
