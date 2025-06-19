package data.models.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlanetRecord(
        String name,
        @JsonProperty("rotation_period") String rotationPeriod,
        @JsonProperty("orbital_period") String orbitalPeriod,
        String climate,
        OffsetDateTime created
) {}
