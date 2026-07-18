package com.coresuite.inventory.dto;

import com.coresuite.inventory.domain.StockLevel;
import java.time.Instant;

public record StockLevelResponse(
        Long id,
        Long warehouseId,
        String sku,
        int quantity,
        int reorderThreshold,
        boolean belowReorderThreshold,
        Instant createdAt,
        Instant updatedAt) {

    public static StockLevelResponse from(StockLevel stockLevel) {
        return new StockLevelResponse(
                stockLevel.getId(),
                stockLevel.getWarehouse().getId(),
                stockLevel.getSku(),
                stockLevel.getQuantity(),
                stockLevel.getReorderThreshold(),
                stockLevel.isBelowReorderThreshold(),
                stockLevel.getCreatedAt(),
                stockLevel.getUpdatedAt());
    }
}
