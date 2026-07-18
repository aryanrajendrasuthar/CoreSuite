package com.coresuite.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.coresuite.inventory.domain.StockLevel;
import com.coresuite.inventory.domain.Warehouse;
import com.coresuite.inventory.dto.StockAdjustRequest;
import com.coresuite.inventory.dto.StockLevelCreateRequest;
import com.coresuite.inventory.dto.StockLevelResponse;
import com.coresuite.inventory.dto.WarehouseRequest;
import com.coresuite.inventory.repository.StockLevelRepository;
import com.coresuite.inventory.repository.WarehouseRepository;
import com.coresuite.shared.error.ConflictException;
import com.coresuite.shared.error.ResourceNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private StockLevelRepository stockLevelRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void createWarehouseThrowsConflictWhenNameAlreadyExists() {
        when(warehouseRepository.existsByName("Main")).thenReturn(true);

        assertThatThrownBy(() -> inventoryService.createWarehouse(new WarehouseRequest("Main", null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void initializeStockThrowsNotFoundWhenWarehouseMissing() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.initializeStock(
                        new StockLevelCreateRequest(1L, "SKU-1", 10, 5)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void initializeStockThrowsConflictWhenAlreadyTracked() {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(stockLevelRepository.findByWarehouseIdAndSku(1L, "SKU-1"))
                .thenReturn(Optional.of(new StockLevel()));

        assertThatThrownBy(() -> inventoryService.initializeStock(
                        new StockLevelCreateRequest(1L, "SKU-1", 10, 5)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void adjustStockRejectsAdjustmentThatWouldGoNegative() {
        StockLevel stockLevel = existingStockLevel(5);
        when(stockLevelRepository.findById(1L)).thenReturn(Optional.of(stockLevel));

        assertThatThrownBy(() -> inventoryService.adjustStock(1L, new StockAdjustRequest(-10)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void adjustStockAppliesPositiveDelta() {
        StockLevel stockLevel = existingStockLevel(5);
        when(stockLevelRepository.findById(1L)).thenReturn(Optional.of(stockLevel));

        StockLevelResponse response = inventoryService.adjustStock(1L, new StockAdjustRequest(3));

        assertThat(response.quantity()).isEqualTo(8);
    }

    private StockLevel existingStockLevel(int quantity) {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);
        StockLevel stockLevel = new StockLevel();
        stockLevel.setId(1L);
        stockLevel.setWarehouse(warehouse);
        stockLevel.setSku("SKU-1");
        stockLevel.setQuantity(quantity);
        stockLevel.setReorderThreshold(2);
        return stockLevel;
    }
}
