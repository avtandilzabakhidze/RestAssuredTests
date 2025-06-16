package data.models.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "petId", "quantity", "status", "shipDate", "complete"})
@JsonDeserialize(builder = Order.OrderBuilder.class)
public class Order {
    private Long id;
    private Long petId;
    private Integer quantity;
    private String status;
    private String shipDate;
    private Boolean complete;

    @JsonPOJOBuilder(withPrefix = "")
    public static class OrderBuilder {
    }
}
