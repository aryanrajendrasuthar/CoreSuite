package com.coresuite.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.coresuite.order.domain.Order;
import com.coresuite.order.domain.OrderStatus;
import com.coresuite.order.dto.OrderCreateRequest;
import com.coresuite.order.dto.OrderLineItemRequest;
import com.coresuite.order.dto.OrderResponse;
import com.coresuite.order.dto.OrderStatusUpdateRequest;
import com.coresuite.order.repository.OrderRepository;
import com.coresuite.shared.error.ConflictException;
import com.coresuite.shared.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createComputesTotalFromLineItems() {
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.create(new OrderCreateRequest(
                1L,
                null,
                List.of(
                        new OrderLineItemRequest("SKU-1", 2, new BigDecimal("10.00")),
                        new OrderLineItemRequest("SKU-2", 1, new BigDecimal("5.50")))));

        assertThat(response.totalAmount()).isEqualByComparingTo("25.50");
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void updateStatusAllowsValidTransition() {
        Order order = pendingOrder();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.updateStatus(1L, new OrderStatusUpdateRequest(
                OrderStatus.CONFIRMED, "payment received"));

        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void updateStatusRejectsInvalidTransition() {
        Order order = pendingOrder();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, new OrderStatusUpdateRequest(
                        OrderStatus.SHIPPED, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateStatusRejectsTransitionOutOfTerminalState() {
        Order order = pendingOrder();
        order.transitionTo(OrderStatus.CANCELLED, "test");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, new OrderStatusUpdateRequest(
                        OrderStatus.CONFIRMED, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void getThrowsWhenOrderNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.get(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    private Order pendingOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(1L);
        order.transitionTo(OrderStatus.PENDING, "placed");
        return order;
    }
}
