package com.coresuite.inventory.web;

import com.coresuite.inventory.dto.WarehouseRequest;
import com.coresuite.inventory.dto.WarehouseResponse;
import com.coresuite.inventory.service.InventoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<WarehouseResponse> create(@Valid @RequestBody WarehouseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createWarehouse(request));
    }

    @GetMapping
    public List<WarehouseResponse> list() {
        return inventoryService.listWarehouses();
    }
}
