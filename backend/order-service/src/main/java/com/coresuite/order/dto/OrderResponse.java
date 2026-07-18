package com.coresuite.order.dto;

import com.coresuite.order.domain.Order;
import com.coresuite.order.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Long customerId,
        OrderStatus status,
        String currency,
        BigDecimal totalAmount,
        List<OrderLineItemResponse> lineItems,
        Instant createdAt,
        Instant updatedAt) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getCurrency(),
                order.getTotalAmount(),
                order.getLineItems().stream().map(OrderLineItemResponse::from).toList(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
