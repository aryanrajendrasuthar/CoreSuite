package com.coresuite.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record OrderCreateRequest(
        @NotNull Long customerId,
        @Pattern(regexp = "^[A-Z]{3}$") String currency,
        @NotEmpty List<@Valid OrderLineItemRequest> lineItems) {
}
