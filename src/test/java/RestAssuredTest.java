import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static data.Constants.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

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

        Book firstBook = books.get(ZERO);
        Book secondBook = books.get(FIRST);

        assertNotNull(firstBook.getIsbn(), ISBN_NOT_NULL);
        assertNotNull(firstBook.getAuthor(), AUTHOR_NOT_EMPTY);
        assertNotNull(secondBook.getIsbn(), ISBN_NOT_NULL);
        assertNotNull(secondBook.getAuthor(), AUTHOR_NOT_EMPTY);
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

            assertEquals(bookDetails.getAuthor(), book.getAuthor(), AUTHOR_MISMATCH);
            assertNotNull(bookDetails.getTitle(), TITLE_NOT_NULL);
            assertNotNull(bookDetails.getIsbn(), ISBN_NOT_NULL_DETAIL);
            assertNotNull(bookDetails.getPublish_date(), PUBLISH_DATE_NOT_NULL);
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

        assertEquals(bookDetails.getIsbn(), isbn, ISBN_NOT_NULL_DETAIL);
        assertEquals(bookDetails.getAuthor(), expectedAuthor, AUTHOR_MISMATCH);
        assertNotNull(bookDetails.getTitle(), TITLE_NOT_NULL);
        assertNotNull(bookDetails.getPublish_date(), PUBLISH_DATE_NOT_NULL);
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

        assertEquals(response.statusCode(), 401, CODE_401);
        String actualMessage = response.jsonPath().getString(MESSAGE);
        assertEquals(actualMessage, USER_NOT_AUTHORIZED, UNEXPECTED_ERROR);
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

        assertEquals(actualDate, expectedDate, SHIP_DATE_MISMATCH);
        assertEquals(responseOrder.getId(), order.getId(), ORDER_ID_MISMATCH);
        assertEquals(responseOrder.getPetId(), order.getPetId(), PET_ID_MISMATCH);
        assertEquals(responseOrder.getQuantity(), order.getQuantity(), QUANTITY_MISMATCH);
        assertEquals(responseOrder.getStatus(), order.getStatus(), STATUS_MISMATCH);
        assertEquals(responseOrder.getComplete(), order.getComplete(), COMPLETE_FLAG_MISMATCH);
    }

    private String fixTimezoneFormat(String dateStr) {
        return dateStr.replaceAll("([+-]\\d{2})(\\d{2})$", "$1:$2");
    }

    @Test(priority = 7)
    public void updatePetWithFormData() {
        RestAssured.baseURI = PETSTORE_BASE_URI;

        Response response = RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .pathParam("petId", SECOND)
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
    public void updatePetWithNonExistentPetId_shouldReturn404() {
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
        Assert.assertEquals(tenDigitNumber.length(), CONTAINS_NUM, CONTAINS_DIGIT);
    }

    @Test(priority = 10)
    public void validateHarryPotterBookExists() {
        RestAssured.baseURI = OPENLIBRARY_BASE_URI;

        Response response = RestAssured.given()
                .queryParam(CON_Q, HARRY)
                .when()
                .get("/search.json")
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath jsonPath = response.jsonPath();

        List<Map<String, Object>> docs = jsonPath.getList(DOCS);
        Assert.assertNotNull(docs, NOT_BE_NULL);
        Assert.assertFalse(docs.isEmpty(), SHOULD_NOT_EMPTY);

        boolean found = false;

        for (Map<String, Object> book : docs) {
            String title = (String) book.get("title");
            List<String> authorNames = (List<String>) book.get("author_name");

            if ("Harry Potter and the Philosopher's Stone".equals(title)
                    && authorNames != null && authorNames.contains("J. K. Rowling")) {
                found = true;
                break;
            }
        }

        Assert.assertTrue(found, EXPECTED_BOOK);
    }
}
