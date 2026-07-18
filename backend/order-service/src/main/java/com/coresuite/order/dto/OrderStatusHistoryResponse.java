package com.coresuite.order.dto;

import com.coresuite.order.domain.OrderStatus;
import com.coresuite.order.domain.OrderStatusHistory;
import java.time.Instant;

public record OrderStatusHistoryResponse(
        Long id, OrderStatus fromStatus, OrderStatus toStatus, String note, Instant changedAt) {

    public static OrderStatusHistoryResponse from(OrderStatusHistory history) {
        return new OrderStatusHistoryResponse(
                history.getId(), history.getFromStatus(), history.getToStatus(),
                history.getNote(), history.getChangedAt());
    }
}
