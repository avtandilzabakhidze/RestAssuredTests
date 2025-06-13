package steps;

import data.models.booking.Book;
import io.restassured.RestAssured;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import java.util.List;

import static data.Constants.*;

public class BookstoreSteps {
    public List<Book> fetchBooks() {
        RestAssured.baseURI = BOOKSTORE_BASE_URI;
        return RestAssured
                .given()
                .when()
                .get("/BookStore/v1/Books")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("books", Book.class);
    }

    public BookstoreSteps validateAllBooksPagesLessThan1000(List<Book> books) {
        for (Book book : books) {
            MatcherAssert.assertThat("Book pages should be less than 1000",
                    book.getPages(), Matchers.lessThan(THOUSAND));
        }

        return this;
    }

    public BookstoreSteps validateAuthorsOfLastTwoBooks(List<Book> books) {
        MatcherAssert.assertThat("Books list size", books.size(), Matchers.greaterThanOrEqualTo(QUANTITY));

        Book secondLastBook = books.get(books.size() - QUANTITY);
        Book lastBook = books.get(books.size() - FIRST);

        MatcherAssert.assertThat("Second last book author", secondLastBook.getAuthor(), Matchers.equalTo(SECOND_LASTNAME));
        MatcherAssert.assertThat("Last book author", lastBook.getAuthor(), Matchers.equalTo(FIRST_LASTNAME));

        return this;
    }
}
