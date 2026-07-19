package com.coresuite.reporting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.coresuite.reporting.dto.OrderSummary;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderCsvExportServiceTest {

    @Mock
    private ReportingService reportingService;

    @InjectMocks
    private OrderCsvExportService orderCsvExportService;

    @Test
    void exportProducesHeaderAndOneRowPerOrder() {
        when(reportingService.ordersInRange(null, null)).thenReturn(List.of(
                new OrderSummary(1L, 2L, "PENDING", "USD", new BigDecimal("9.99"), Instant.parse("2026-01-01T00:00:00Z"))));

        String csv = new String(orderCsvExportService.export(null, null), StandardCharsets.UTF_8);

        assertThat(csv).startsWith("Order ID,Customer ID,Status,Currency,Total Amount,Created At\n");
        assertThat(csv).contains("1,2,PENDING,USD,9.99,2026-01-01T00:00:00Z");
    }
}
