package data.models.booking;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Setter;

@Builder
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingRequest {
    @JsonProperty("firstname")
    private String firstName;

    @JsonProperty("lastname")
    private String lastName;

    private int totalprice;
    private boolean depositpaid;
    private BookingDates bookingdates;
    private String additionalneeds;
    private String passportNo;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Double saleprice;
}
