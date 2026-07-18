package com.coresuite.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record OrderLineItemRequest(
        @NotNull @Pattern(regexp = "^[A-Za-z0-9_-]{2,64}$") String sku,
        @Min(1) int quantity,
        @NotNull @DecimalMin(value = "0.01") BigDecimal unitPrice) {
}
