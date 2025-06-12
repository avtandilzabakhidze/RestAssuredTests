package model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {
    private String isbn;
    private String title;
    private String author;
    private String publish_date;
    private int pages;
}
