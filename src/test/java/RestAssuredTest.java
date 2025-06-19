import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import data.models.booking.Book;
import data.models.ordering.Order;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static data.Constants.*;

public class RestAssuredTest {
    @Test
    public void validateGetBooksList() {
        RestAssured.baseURI = BOOKSTORE_BASE_URI;
        List<Book> books = fetchBooks();

        Assert.assertFalse(books.isEmpty(), BOOKS_NOT_EMPTY);
    }

    @Test(priority = 2)
    public void validateFirstAndSecondBookDetails() {
        RestAssured.baseURI = BOOKSTORE_BASE_URI;
        List<Book> books = fetchBooks();

        Book firstBook = books.getFirst();
        Book secondBook = books.get(FIRST);

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

        Object[][] data = new Object[books.size()][2];

        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            data[i][0] = i;
            data[i][1] = book.getIsbn();
        }

        return data;
    }

    @Test(dataProvider = "isbn", priority = 4)
    public void validateBookDetailsByIsbnDataProvider(int index, String isbn) {
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
        Assert.assertNotNull(bookDetails.getTitle(), TITLE_NOT_NULL);
        Assert.assertNotNull(bookDetails.getPublish_date(), PUBLISH_DATE_NOT_NULL);
        Assert.assertTrue(bookDetails.getPages() > 0, PAGES_COUNT_INVALID);
    }

    @Test(priority = 5)
    public void deleteBookReturnUnauthorized() {
        RestAssured.baseURI = BOOKSTORE_BASE_URI;

        Response response = RestAssured
                .given()
                .queryParam("UserId", FIRST)
                .when()
                .delete("/BookStore/v1/Book")
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.statusCode(), 401, CODE_401);
        String actualMessage = response.jsonPath().getString(MESSAGE);
        Assert.assertEquals(actualMessage, USER_NOT_AUTHORIZED, UNEXPECTED_ERROR);
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
                .complete(IS_TRUE)
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

    @Test(priority = 7)
    public void updatePetWithFormData() {
        RestAssured.baseURI = PETSTORE_BASE_URI;

        Response response = RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .pathParam("petId", TEN)
                .formParam("name", NEW_NAME)
                .formParam("status", NEW_STATUS)
                .when()
                .post("/pet/{petId}")
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath jsonPath = response.jsonPath();
        Assert.assertNotNull(jsonPath.get("code"), NOT_BE_NULL);
        Assert.assertNotNull(jsonPath.get("type"), NOT_BE_NULL);
        Assert.assertNotNull(jsonPath.get("message"), NOT_BE_NULL);
    }

    @Test(priority = 8)
    public void updatePetWithFormDataReturn404() {
        RestAssured.baseURI = PETSTORE_BASE_URI;

        Response response = RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .pathParam("petId", LAST)
                .formParam("name", NEW_NAME)
                .formParam("status", NEW_STATUS)
                .when()
                .post("/pet/{petId}")
                .then()
                .statusCode(404)
                .extract()
                .response();
    }

    @Test(priority = 9)
    public void userLoginWithValidCredentials() {
        RestAssured.baseURI = PETSTORE_BASE_URI;

        Response response = RestAssured.given()
                .queryParam("username", USER_NAME)
                .queryParam("password", PASSWORD)
                .when()
                .get("/user/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath jsonPath = response.jsonPath();
        String message = jsonPath.getString(MESSAGE);
        Assert.assertNotNull(message, NOT_BE_NULL);

        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(message);

        Assert.assertTrue(matcher.find(), CONTAINS_DIGIT);
        String tenDigitNumber = matcher.group();
        System.out.println(tenDigitNumber);
        Assert.assertTrue(tenDigitNumber.length() >= TEN, CONTAINS_DIGIT);
    }

    @Test(priority = 10)
    public void searchHarryPotterBooksAndValidateFirstBook() {
        Response response = RestAssured
                .given()
                .baseUri("https://openlibrary.org")
                .queryParam("q", KEYWORD)
                .when()
                .get("/search.json")
                .then()
                .statusCode(200)
                .extract()
                .response();

        List<Map<String, Object>> docs = response.jsonPath().getList(DOC);
        Assert.assertFalse(docs.isEmpty(), BOOKS_NOT_EMPTY);

        Map<String, Object> firstBook = docs.getFirst();

        String actualTitle = (String) firstBook.get("title");
        Assert.assertEquals(actualTitle, EXPECTED_TITLE, MISMATCH);

        Map<String, Object> books = docs.stream()
                .filter(book -> EXPECTED_TITLE.equals(book.get("title")))
                .findFirst()
                .orElseThrow();

        List<String> authors = (List<String>) books.get("author_name");
        Assert.assertTrue(authors.contains(EXPECTED_AUTHOR), AUTHOR_MISMATCH);

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

    private String fixTimezoneFormat(String dateStr) {
        return dateStr.replaceAll(REGEX2, REPLACE);
    }
}
