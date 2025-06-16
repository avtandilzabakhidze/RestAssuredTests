package data.models.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanetResponse {
    private String message;
    private int total_records;
    private int total_pages;
    private String previous;
    private String next;
    private List<PlanetShort> results;
}
