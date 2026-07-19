package com.coresuite.reporting.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record KpiResponse(
        long totalOrders,
        BigDecimal totalRevenue,
        Map<String, Long> ordersByStatus,
        long lowStockCount,
        List<StockAlert> lowStockItems) {
}
