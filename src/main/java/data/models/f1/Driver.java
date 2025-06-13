package data.models.f1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Driver {
    private String driverId;
    private String givenName;
    private String familyName;
    private String nationality;
    private String dateOfBirth;
}
