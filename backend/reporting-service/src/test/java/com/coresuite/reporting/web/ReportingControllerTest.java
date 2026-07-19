package com.coresuite.reporting.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coresuite.reporting.dto.KpiResponse;
import com.coresuite.reporting.service.OrderCsvExportService;
import com.coresuite.reporting.service.OrderPdfExportService;
import com.coresuite.reporting.service.ReportingService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReportingController.class)
class ReportingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportingService reportingService;

    @MockBean
    private OrderCsvExportService orderCsvExportService;

    @MockBean
    private OrderPdfExportService orderPdfExportService;

    @Test
    void kpisReturnsAggregatedJson() throws Exception {
        when(reportingService.getKpis(any(), any())).thenReturn(
                new KpiResponse(3, new BigDecimal("42.00"), Map.of("PENDING", 3L), 1, List.of()));

        mockMvc.perform(get("/api/reports/kpis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(3))
                .andExpect(jsonPath("$.totalRevenue").value(42.00));
    }

    @Test
    void ordersCsvReturnsCsvContentType() throws Exception {
        when(orderCsvExportService.export(any(), any())).thenReturn("Order ID\n".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/reports/orders/csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }

    @Test
    void ordersPdfReturnsPdfContentType() throws Exception {
        when(orderPdfExportService.export(any(), any())).thenReturn(new byte[] {'%', 'P', 'D', 'F'});

        mockMvc.perform(get("/api/reports/orders/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/pdf"));
    }
}
