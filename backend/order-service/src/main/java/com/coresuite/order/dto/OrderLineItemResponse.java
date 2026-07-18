package com.coresuite.order.dto;

import com.coresuite.order.domain.OrderLineItem;
import java.math.BigDecimal;

public record OrderLineItemResponse(Long id, String sku, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {

    public static OrderLineItemResponse from(OrderLineItem lineItem) {
        return new OrderLineItemResponse(
                lineItem.getId(), lineItem.getSku(), lineItem.getQuantity(),
                lineItem.getUnitPrice(), lineItem.getSubtotal());
    }
}
