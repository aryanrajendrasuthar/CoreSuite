package com.coresuite.inventory.dto;

import com.coresuite.inventory.domain.Warehouse;
import java.time.Instant;

public record WarehouseResponse(Long id, String name, String location, Instant createdAt, Instant updatedAt) {

    public static WarehouseResponse from(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getLocation(),
                warehouse.getCreatedAt(),
                warehouse.getUpdatedAt());
    }
}
