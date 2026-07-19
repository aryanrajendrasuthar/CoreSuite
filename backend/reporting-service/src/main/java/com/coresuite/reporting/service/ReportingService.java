package com.coresuite.reporting.service;

import com.coresuite.reporting.client.InventoryServiceClient;
import com.coresuite.reporting.client.OrderServiceClient;
import com.coresuite.reporting.dto.KpiResponse;
import com.coresuite.reporting.dto.OrderSummary;
import com.coresuite.reporting.dto.StockAlert;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private static final String CANCELLED = "CANCELLED";

    private final OrderServiceClient orderServiceClient;
    private final InventoryServiceClient inventoryServiceClient;

    public KpiResponse getKpis(Instant from, Instant to) {
        List<OrderSummary> orders = ordersInRange(from, to);

        BigDecimal totalRevenue = orders.stream()
                .filter(order -> !CANCELLED.equals(order.status()))
                .map(OrderSummary::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(OrderSummary::status, Collectors.counting()));

        List<StockAlert> lowStockItems = inventoryServiceClient.fetchReorderAlerts();

        return new KpiResponse(orders.size(), totalRevenue, ordersByStatus, lowStockItems.size(), lowStockItems);
    }

    public List<OrderSummary> ordersInRange(Instant from, Instant to) {
        Predicate<OrderSummary> afterFrom = order -> from == null || !order.createdAt().isBefore(from);
        Predicate<OrderSummary> beforeTo = order -> to == null || !order.createdAt().isAfter(to);
        return orderServiceClient.fetchAllOrders().stream()
                .filter(afterFrom.and(beforeTo))
                .toList();
    }
}
