package com.coresuite.reporting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.coresuite.reporting.dto.KpiResponse;
import com.coresuite.reporting.dto.OrderSummary;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderPdfExportServiceTest {

    @Mock
    private ReportingService reportingService;

    @InjectMocks
    private OrderPdfExportService orderPdfExportService;

    @Test
    void exportProducesNonEmptyPdfBytes() {
        when(reportingService.ordersInRange(null, null)).thenReturn(List.of(
                new OrderSummary(1L, 2L, "PENDING", "USD", new BigDecimal("9.99"), Instant.parse("2026-01-01T00:00:00Z"))));
        when(reportingService.getKpis(null, null)).thenReturn(
                new KpiResponse(1, new BigDecimal("9.99"), Map.of("PENDING", 1L), 0, List.of()));

        byte[] pdf = orderPdfExportService.export(null, null);

        assertThat(pdf).isNotEmpty();
        // PDF files start with the "%PDF-" magic bytes.
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }
}
