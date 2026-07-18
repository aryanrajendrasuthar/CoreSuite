package com.coresuite.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WarehouseRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 255) String location) {
}
