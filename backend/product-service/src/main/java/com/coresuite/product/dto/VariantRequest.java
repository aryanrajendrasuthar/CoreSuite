package com.coresuite.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.Map;

public record VariantRequest(
        @NotNull @Pattern(regexp = "^[A-Za-z0-9_-]{2,64}$") String sku,
        Map<String, String> attributes,
        @NotNull @DecimalMin(value = "0.01") BigDecimal price,
        @Pattern(regexp = "^[A-Z]{3}$") String currency) {
}
