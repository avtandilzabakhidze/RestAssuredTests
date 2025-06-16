package steps;

import data.models.order.Order;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static data.Constants.PETSTORE_BASE_URI_V3;
import static data.Constants.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class OrderSteps {
    @Step("Send POST /store/order with Order payload")
    public Order createOrder(Order order) {
        return RestAssured
                .given()
                .baseUri(PETSTORE_BASE_URI_V3)
                .contentType(ContentType.JSON)
                .body(order)
                .when()
                .post("/store/order")
                .then()
                .statusCode(200)
                .extract()
                .as(Order.class);
    }

    public OrderSteps validateOrder(Order actual, Order expected) {
        assertThat(actual.id(), equalTo(ZERO));

        return this;
    }
}
