import data.models.book.Book;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import steps.BookstoreSteps;

import java.util.List;

public class BookstoreValidationTest {
    private BookstoreSteps bookstoreSteps;

    @BeforeMethod
    public void setUp() {
        bookstoreSteps = new BookstoreSteps();
    }

    @Test
    public void validateAllBooksPagesLessThan1000() {
        List<Book> books = bookstoreSteps.fetchBooks();
        bookstoreSteps.validateAllBooksPagesLessThan1000(books);
    }

    @Test
    public void validateAuthorsOfLastTwoBooks() {
        List<Book> books = bookstoreSteps.fetchBooks();
        bookstoreSteps.validateAuthorsOfLastTwoBooks(books);
    }
}
