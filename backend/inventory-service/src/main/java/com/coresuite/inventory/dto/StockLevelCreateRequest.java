package com.coresuite.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record StockLevelCreateRequest(
        @NotNull Long warehouseId,
        @NotNull @Pattern(regexp = "^[A-Za-z0-9_-]{2,64}$") String sku,
        @Min(0) int quantity,
        @Min(0) int reorderThreshold) {
}
