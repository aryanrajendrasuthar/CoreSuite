package com.coresuite.reporting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.coresuite.reporting.client.InventoryServiceClient;
import com.coresuite.reporting.client.OrderServiceClient;
import com.coresuite.reporting.dto.KpiResponse;
import com.coresuite.reporting.dto.OrderSummary;
import com.coresuite.reporting.dto.StockAlert;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    private OrderServiceClient orderServiceClient;

    @Mock
    private InventoryServiceClient inventoryServiceClient;

    @InjectMocks
    private ReportingService reportingService;

    @Test
    void revenueExcludesCancelledOrders() {
        when(orderServiceClient.fetchAllOrders()).thenReturn(List.of(
                order(1L, "DELIVERED", "50.00", "2026-01-01T00:00:00Z"),
                order(2L, "CANCELLED", "999.00", "2026-01-02T00:00:00Z")));
        when(inventoryServiceClient.fetchReorderAlerts()).thenReturn(List.of());

        KpiResponse kpis = reportingService.getKpis(null, null);

        assertThat(kpis.totalOrders()).isEqualTo(2);
        assertThat(kpis.totalRevenue()).isEqualByComparingTo("50.00");
        assertThat(kpis.ordersByStatus()).containsEntry("DELIVERED", 1L).containsEntry("CANCELLED", 1L);
    }

    @Test
    void ordersInRangeFiltersByDate() {
        when(orderServiceClient.fetchAllOrders()).thenReturn(List.of(
                order(1L, "DELIVERED", "10.00", "2026-01-01T00:00:00Z"),
                order(2L, "DELIVERED", "10.00", "2026-02-01T00:00:00Z"),
                order(3L, "DELIVERED", "10.00", "2026-03-01T00:00:00Z")));

        List<OrderSummary> inRange = reportingService.ordersInRange(
                Instant.parse("2026-01-15T00:00:00Z"), Instant.parse("2026-02-15T00:00:00Z"));

        assertThat(inRange).extracting(OrderSummary::id).containsExactly(2L);
    }

    @Test
    void lowStockCountReflectsInventoryClient() {
        when(orderServiceClient.fetchAllOrders()).thenReturn(List.of());
        when(inventoryServiceClient.fetchReorderAlerts()).thenReturn(List.of(
                new StockAlert("SKU-1", 1, 5), new StockAlert("SKU-2", 0, 3)));

        KpiResponse kpis = reportingService.getKpis(null, null);

        assertThat(kpis.lowStockCount()).isEqualTo(2);
    }

    private OrderSummary order(Long id, String status, String amount, String createdAt) {
        return new OrderSummary(id, 1L, status, "USD", new BigDecimal(amount), Instant.parse(createdAt));
    }
}
