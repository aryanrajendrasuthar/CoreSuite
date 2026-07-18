package com.coresuite.inventory.repository;

import com.coresuite.inventory.domain.StockLevel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StockLevelRepository extends JpaRepository<StockLevel, Long> {

    Optional<StockLevel> findByWarehouseIdAndSku(Long warehouseId, String sku);

    List<StockLevel> findByWarehouseId(Long warehouseId);

    @Query("SELECT s FROM StockLevel s WHERE s.quantity <= s.reorderThreshold")
    List<StockLevel> findAllBelowReorderThreshold();
}
