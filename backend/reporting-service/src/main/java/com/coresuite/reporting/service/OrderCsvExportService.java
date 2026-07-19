package com.coresuite.reporting.service;

import com.coresuite.reporting.dto.OrderSummary;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCsvExportService {

    private static final String HEADER = "Order ID,Customer ID,Status,Currency,Total Amount,Created At\n";

    private final ReportingService reportingService;

    public byte[] export(Instant from, Instant to) {
        List<OrderSummary> orders = reportingService.ordersInRange(from, to);
        StringBuilder csv = new StringBuilder(HEADER);
        for (OrderSummary order : orders) {
            csv.append(order.id()).append(',')
                    .append(order.customerId()).append(',')
                    .append(order.status()).append(',')
                    .append(order.currency()).append(',')
                    .append(order.totalAmount()).append(',')
                    .append(order.createdAt())
                    .append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
}
