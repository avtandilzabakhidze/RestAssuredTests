import data.models.order.Order;
import org.testng.annotations.Test;
import steps.OrderSteps;

import static data.Constants.*;

public class PetStoreOrderTest {
    private final OrderSteps steps = new OrderSteps();

    @Test
    public void createAndValidateOrder() {
        Order expected = Order.builder()
                .petId(NINE)
                .quantity(TWO)
                .status("placed")
                .shipDate(TIMER)
                .complete(false)
                .build();

        OrderSteps steps = new OrderSteps();
        Order actual = steps.createOrder(expected);
        steps.validateOrder(actual, expected);
    }
}
