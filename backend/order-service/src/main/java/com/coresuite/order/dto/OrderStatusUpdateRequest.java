package com.coresuite.order.dto;

import com.coresuite.order.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderStatusUpdateRequest(@NotNull OrderStatus toStatus, @Size(max = 500) String note) {
}
