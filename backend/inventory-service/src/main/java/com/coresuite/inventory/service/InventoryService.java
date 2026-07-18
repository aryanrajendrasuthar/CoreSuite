package com.coresuite.inventory.service;

import com.coresuite.inventory.domain.StockLevel;
import com.coresuite.inventory.domain.Warehouse;
import com.coresuite.inventory.dto.StockAdjustRequest;
import com.coresuite.inventory.dto.StockLevelCreateRequest;
import com.coresuite.inventory.dto.StockLevelResponse;
import com.coresuite.inventory.dto.WarehouseRequest;
import com.coresuite.inventory.dto.WarehouseResponse;
import com.coresuite.inventory.repository.StockLevelRepository;
import com.coresuite.inventory.repository.WarehouseRepository;
import com.coresuite.shared.error.ConflictException;
import com.coresuite.shared.error.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final WarehouseRepository warehouseRepository;
    private final StockLevelRepository stockLevelRepository;

    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        if (warehouseRepository.existsByName(request.name())) {
            throw new ConflictException("Warehouse already exists: " + request.name());
        }
        Warehouse warehouse = new Warehouse();
        warehouse.setName(request.name());
        warehouse.setLocation(request.location());
        return WarehouseResponse.from(warehouseRepository.save(warehouse));
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> listWarehouses() {
        return warehouseRepository.findAll().stream().map(WarehouseResponse::from).toList();
    }

    @Transactional
    public StockLevelResponse initializeStock(StockLevelCreateRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + request.warehouseId()));
        if (stockLevelRepository.findByWarehouseIdAndSku(request.warehouseId(), request.sku()).isPresent()) {
            throw new ConflictException(
                    "Stock already tracked for SKU " + request.sku() + " in warehouse " + request.warehouseId());
        }
        StockLevel stockLevel = new StockLevel();
        stockLevel.setWarehouse(warehouse);
        stockLevel.setSku(request.sku());
        stockLevel.setQuantity(request.quantity());
        stockLevel.setReorderThreshold(request.reorderThreshold());
        return StockLevelResponse.from(stockLevelRepository.save(stockLevel));
    }

    @Transactional(readOnly = true)
    public List<StockLevelResponse> listStockByWarehouse(Long warehouseId) {
        return stockLevelRepository.findByWarehouseId(warehouseId).stream()
                .map(StockLevelResponse::from)
                .toList();
    }

    @Transactional
    public StockLevelResponse adjustStock(Long stockLevelId, StockAdjustRequest request) {
        StockLevel stockLevel = stockLevelRepository.findById(stockLevelId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock level not found: " + stockLevelId));
        int newQuantity = stockLevel.getQuantity() + request.delta();
        if (newQuantity < 0) {
            throw new ConflictException(
                    "Adjustment of " + request.delta() + " would take stock below zero (current: "
                            + stockLevel.getQuantity() + ")");
        }
        stockLevel.setQuantity(newQuantity);
        return StockLevelResponse.from(stockLevel);
    }

    @Transactional(readOnly = true)
    public List<StockLevelResponse> reorderAlerts() {
        return stockLevelRepository.findAllBelowReorderThreshold().stream()
                .map(StockLevelResponse::from)
                .toList();
    }
}
