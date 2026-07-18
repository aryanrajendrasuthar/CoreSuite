package com.coresuite.order.web;

import com.coresuite.order.domain.OrderStatus;
import com.coresuite.order.dto.OrderCreateRequest;
import com.coresuite.order.dto.OrderResponse;
import com.coresuite.order.dto.OrderStatusHistoryResponse;
import com.coresuite.order.dto.OrderStatusUpdateRequest;
import com.coresuite.order.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.get(id);
    }

    @GetMapping
    public Page<OrderResponse> list(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {
        return orderService.list(customerId, status, pageable);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusUpdateRequest request) {
        return orderService.updateStatus(id, request);
    }

    @GetMapping("/{id}/history")
    public List<OrderStatusHistoryResponse> history(@PathVariable Long id) {
        return orderService.history(id);
    }
}
