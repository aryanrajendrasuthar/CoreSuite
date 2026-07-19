package com.coresuite.reporting.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** Minimal read-only view of an order, as returned by order-service's API. */
public record OrderSummary(
        Long id, Long customerId, String status, String currency, BigDecimal totalAmount, Instant createdAt) {
}
