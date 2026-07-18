package com.coresuite.order.service;

import com.coresuite.order.domain.Order;
import com.coresuite.order.domain.OrderLineItem;
import com.coresuite.order.domain.OrderStatus;
import com.coresuite.order.dto.OrderCreateRequest;
import com.coresuite.order.dto.OrderLineItemRequest;
import com.coresuite.order.dto.OrderResponse;
import com.coresuite.order.dto.OrderStatusHistoryResponse;
import com.coresuite.order.dto.OrderStatusUpdateRequest;
import com.coresuite.order.repository.OrderRepository;
import com.coresuite.shared.error.ConflictException;
import com.coresuite.shared.error.ResourceNotFoundException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(OrderStatus.PENDING, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.PROCESSING, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.SHIPPED, EnumSet.of(OrderStatus.DELIVERED));
        ALLOWED_TRANSITIONS.put(OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        Order order = new Order();
        order.setCustomerId(request.customerId());
        if (request.currency() != null) {
            order.setCurrency(request.currency());
        }
        for (OrderLineItemRequest itemRequest : request.lineItems()) {
            OrderLineItem lineItem = new OrderLineItem();
            lineItem.setSku(itemRequest.sku());
            lineItem.setQuantity(itemRequest.quantity());
            lineItem.setUnitPrice(itemRequest.unitPrice());
            order.addLineItem(lineItem);
        }
        order.recalculateTotal();
        order.transitionTo(OrderStatus.PENDING, "Order placed");
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return OrderResponse.from(findOrderOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> list(Long customerId, OrderStatus status, Pageable pageable) {
        Page<Order> page;
        if (customerId != null && status != null) {
            page = orderRepository.findByCustomerIdAndStatus(customerId, status, pageable);
        } else if (customerId != null) {
            page = orderRepository.findByCustomerId(customerId, pageable);
        } else if (status != null) {
            page = orderRepository.findByStatus(status, pageable);
        } else {
            page = orderRepository.findAll(pageable);
        }
        return page.map(OrderResponse::from);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request) {
        Order order = findOrderOrThrow(id);
        OrderStatus current = order.getStatus();
        if (!ALLOWED_TRANSITIONS.get(current).contains(request.toStatus())) {
            throw new ConflictException(
                    "Cannot transition order from " + current + " to " + request.toStatus());
        }
        order.transitionTo(request.toStatus(), request.note());
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> history(Long id) {
        return findOrderOrThrow(id).getStatusHistory().stream()
                .map(OrderStatusHistoryResponse::from)
                .toList();
    }

    private Order findOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }
}
