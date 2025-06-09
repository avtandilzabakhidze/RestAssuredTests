package data;

public class Constants {
    public static final String BOOKSTORE_BASE_URI = "https://bookstore.toolsqa.com",
            PETSTORE_BASE_URI = "https://petstore.swagger.io/v2",
            OPENLIBRARY_BASE_URI = "https://openlibrary.org",
            ISBN = "123123",
            USER_NAME = "userNam",
            PASSWORD = "userNam",
            MESSAGE = "message",
            BOOKS = "books",
            DOCS = "docs",
            HARRY = "Harry Potter",
            REGEX = "\\b\\d{13}\\b",
            REGEX2 = "([+-]\\d{2})(\\d{2})$",
            REPLACE = "$1:$2",
            ISBN_BASE = "ISBN",
            CONTAINS_DIGIT = "should contain a 13-digit",
            CODE_401 = "Expected 401 Code",
            USER_NOT_AUTHORIZED = "User not authorized!",
            UNEXPECTED_ERROR = "Unexpected error message",
            ISBN_NOT_NULL = "ISBN not be null",
            AUTHOR_NOT_EMPTY = "Author not be null",
            BOOKS_NOT_EMPTY = "Books list is empty",
            CON_Q = "q",
            AUTHOR_MISMATCH = "Author does not match",
            TITLE_NOT_NULL = "Title is null",
            ISBN_NOT_NULL_DETAIL = "ISBN is null",
            PUBLISH_DATE_NOT_NULL = "Publish date is null",
            PAGES_COUNT_INVALID = "Pages count invalid",
            ORDER_ID_MISMATCH = "Order ID mismatch",
            PET_ID_MISMATCH = "Pet ID mismatch",
            QUANTITY_MISMATCH = "Quantity mismatch",
            NOT_BE_NULL = "should not be null",
            SHOULD_NOT_EMPTY = "Docs list should not be empty",
            STATUS_MISMATCH = "Status mismatch",
            EXPECTED_BOOK = "Expected book with the correct title and author was not found",
            COMPLETE_FLAG_MISMATCH = "Complete flag mismatch",
            SHIP_DATE = "2025-06-10T10:00:00.000Z",
            STATUS = "placed",
            NEW_NAME = "one",
            NEW_STATUS = "two",
            SHIP_DATE_MISMATCH = "ShipDate mismatch";

    public static final long ORDER_ID = 12345L,
            PET_ID = 54321L,
            SECOND = 2,
            LAST = 999999L;

    public static final int QUANTITY = 2,
            ZERO = 0,
            FIRST = 1,
            CONTAINS_NUM = 13;

    public static final boolean IS_TRUE = true,
            IS_FALSE = false;
}
