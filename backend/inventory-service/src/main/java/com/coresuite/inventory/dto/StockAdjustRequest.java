package com.coresuite.inventory.dto;

import jakarta.validation.constraints.NotNull;

public record StockAdjustRequest(@NotNull Integer delta) {
}
