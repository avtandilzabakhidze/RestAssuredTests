package data;

import org.json.JSONObject;
import static data.Constants.*;

public class BookingRequests {
    public JSONObject getBookingPayload() {
        JSONObject bookingPayload = new JSONObject();

        bookingPayload.put("firstname", FIRST_NAME);
        bookingPayload.put("lastname", LAST_NAME);
        bookingPayload.put("totalprice", TOTAL_PRICE);
        bookingPayload.put("depositpaid", IS_TRUE);

        JSONObject bookingDates = new JSONObject();
        bookingDates.put("checkin", CHECKIN_DATE);
        bookingDates.put("checkout", CHECKOUT_DATE);
        bookingPayload.put("bookingdates", bookingDates);

        bookingPayload.put("additionalneeds", ADDITIONAL_NEEDS);
        return bookingPayload;
    }
}
