package steps;

import data.models.booking.Book;
import io.restassured.RestAssured;
import org.testng.Assert;

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

    public void validateAllBooksPagesLessThan1000(List<Book> books) {
        for (Book book : books) {
            Assert.assertTrue(book.getPages() < THOUSAND, NOT_FOUND);
        }
    }

    public void validateAuthorsOfLastTwoBooks(List<Book> books) {
        Assert.assertTrue(books.size() >= QUANTITY, NOT_FOUND);

        Book secondLastBook = books.get(books.size() - QUANTITY);
        Book lastBook = books.get(books.size() - 1);

        Assert.assertEquals(secondLastBook.getAuthor(), SECOND_LASTNAME, MISMATCH);
        Assert.assertEquals(lastBook.getAuthor(), FIRST_LASTNAME, MISMATCH);
    }
}
