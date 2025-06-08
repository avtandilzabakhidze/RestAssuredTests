import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import model.Book;
import model.Order;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static data.Constants.*;

public class RestAssuredTest {
    @Test
    public void getBooksList() {
        RestAssured.baseURI = BOOKSTORE_BASE_URI;
        List<Book> books = fetchBooks();

        Assert.assertFalse(books.isEmpty(), BOOKS_NOT_EMPTY);
    }

    @Test(priority = 2)
    public void validateFirstAndSecondBookDetails() {
        RestAssured.baseURI = BOOKSTORE_BASE_URI;
        List<Book> books = fetchBooks();

        Book firstBook = books.get(0);
        Book secondBook = books.get(1);

        Assert.assertNotNull(firstBook.getIsbn(), ISBN_NOT_NULL);
        Assert.assertNotNull(firstBook.getAuthor(), AUTHOR_NOT_EMPTY);
        Assert.assertNotNull(secondBook.getIsbn(), ISBN_NOT_NULL);
        Assert.assertNotNull(secondBook.getAuthor(), AUTHOR_NOT_EMPTY);
    }

    @Test(priority = 3)
    public void validateEachBookDetailsByIsbn() {
        RestAssured.baseURI = BOOKSTORE_BASE_URI;
        List<Book> books = fetchBooks();

        for (Book book : books) {
            Response response = RestAssured
                    .given()
                    .queryParam(ISBN_BASE, book.getIsbn())
                    .when()
                    .get("/BookStore/v1/Book")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            Book bookDetails = response.as(Book.class);

            Assert.assertEquals(bookDetails.getAuthor(), book.getAuthor(), AUTHOR_MISMATCH);
            Assert.assertNotNull(bookDetails.getTitle(), TITLE_NOT_NULL);
            Assert.assertNotNull(bookDetails.getIsbn(), ISBN_NOT_NULL_DETAIL);
            Assert.assertNotNull(bookDetails.getPublish_date(), PUBLISH_DATE_NOT_NULL);
            Assert.assertTrue(bookDetails.getPages() > 0, PAGES_COUNT_INVALID);

        }
    }

    @DataProvider(name = "isbn")
    public Object[][] isbnProvider() {
        RestAssured.baseURI = BOOKSTORE_BASE_URI;
        List<Book> books = fetchBooks();

        Object[][] data = new Object[books.size()][3];

        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            data[i][0] = i;
            data[i][1] = book.getIsbn();
            data[i][2] = book.getAuthor();
        }

        return data;
    }

    @Test(dataProvider = "isbn", priority = 4)
    public void validateBookDetailsByIsbnDataProvider(int index, String isbn, String expectedAuthor) {
        RestAssured.baseURI = BOOKSTORE_BASE_URI;

        Response response = RestAssured
                .given()
                .queryParam(ISBN_BASE, isbn)
                .when()
                .get("/BookStore/v1/Book")
                .then()
                .statusCode(200)
                .extract()
                .response();

        Book bookDetails = response.as(Book.class);

        Assert.assertEquals(bookDetails.getIsbn(), isbn, ISBN_NOT_NULL_DETAIL);
        Assert.assertEquals(bookDetails.getAuthor(), expectedAuthor, AUTHOR_MISMATCH);
        Assert.assertNotNull(bookDetails.getTitle(), TITLE_NOT_NULL);
        Assert.assertNotNull(bookDetails.getPublish_date(), PUBLISH_DATE_NOT_NULL);
        Assert.assertTrue(bookDetails.getPages() > 0, PAGES_COUNT_INVALID);

    }

    @Test(priority = 5)
    public void deleteBookShouldReturnUnauthorized() {
        RestAssured.baseURI = BOOKSTORE_BASE_URI;

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("isbn", ISBN);
        requestBody.put("userId", USER_NAME);

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .delete("/BookStore/v1/Book")
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.statusCode(), 401, CODE_401);
        String actualMessage = response.jsonPath().getString(MESSAGE);
        Assert.assertEquals(actualMessage, USER_NOT_AUTHORIZED, UNEXPECTED_ERROR);
    }


    private List<Book> fetchBooks() {
        Response response = RestAssured
                .given()
                .when()
                .get("/BookStore/v1/Books")
                .then()
                .statusCode(200)
                .extract()
                .response();

        return response.jsonPath().getList(BOOKS, Book.class);
    }

    @Test(priority = 6)
    public void createOrderAndValidateResponse() {
        RestAssured.baseURI = PETSTORE_BASE_URI;

        //lombok-ის დამსახურებით ბილდერ დიზაინ პატერნი გამოვიყენე
        Order order = Order.builder()
                .id(ORDER_ID)
                .petId(PET_ID)
                .quantity(QUANTITY)
                .shipDate(SHIP_DATE)
                .status(STATUS)
                .complete(COMPLETE)
                .build();

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(order)
                .when()
                .post("/store/order")
                .then()
                .statusCode(200)
                .extract()
                .response();

        Order responseOrder = response.as(Order.class);
        String expectedDateStr = order.getShipDate();
        String actualDateStr = fixTimezoneFormat(responseOrder.getShipDate());

        OffsetDateTime expectedDate = OffsetDateTime.parse(expectedDateStr);
        OffsetDateTime actualDate = OffsetDateTime.parse(actualDateStr);

        Assert.assertEquals(actualDate, expectedDate, SHIP_DATE_MISMATCH);
        Assert.assertEquals(responseOrder.getId(), order.getId(), ORDER_ID_MISMATCH);
        Assert.assertEquals(responseOrder.getPetId(), order.getPetId(), PET_ID_MISMATCH);
        Assert.assertEquals(responseOrder.getQuantity(), order.getQuantity(), QUANTITY_MISMATCH);
        Assert.assertEquals(responseOrder.getStatus(), order.getStatus(), STATUS_MISMATCH);
        Assert.assertEquals(responseOrder.getComplete(), order.getComplete(), COMPLETE_FLAG_MISMATCH);
    }

    private String fixTimezoneFormat(String dateStr) {
        return dateStr.replaceAll("([+-]\\d{2})(\\d{2})$", "$1:$2");
    }
}
