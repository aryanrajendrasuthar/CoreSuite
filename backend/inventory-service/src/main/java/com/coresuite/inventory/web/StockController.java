package com.coresuite.inventory.web;

import com.coresuite.inventory.dto.StockAdjustRequest;
import com.coresuite.inventory.dto.StockLevelCreateRequest;
import com.coresuite.inventory.dto.StockLevelResponse;
import com.coresuite.inventory.service.InventoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<StockLevelResponse> initialize(@Valid @RequestBody StockLevelCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.initializeStock(request));
    }

    @GetMapping("/warehouse/{warehouseId}")
    public List<StockLevelResponse> listByWarehouse(@PathVariable Long warehouseId) {
        return inventoryService.listStockByWarehouse(warehouseId);
    }

    @PatchMapping("/{id}/adjust")
    public StockLevelResponse adjust(@PathVariable Long id, @Valid @RequestBody StockAdjustRequest request) {
        return inventoryService.adjustStock(id, request);
    }

    @GetMapping("/reorder-alerts")
    public List<StockLevelResponse> reorderAlerts() {
        return inventoryService.reorderAlerts();
    }
}
