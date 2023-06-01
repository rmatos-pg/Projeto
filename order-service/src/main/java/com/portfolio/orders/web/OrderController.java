package com.portfolio.orders.web;

import com.portfolio.orders.domain.Order;
import com.portfolio.orders.service.OrderApplicationService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    @PostMapping
    public ResponseEntity<Order> create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody CreateOrderRequest request) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Order order = orderApplicationService.createOrder(
                idempotencyKey,
                request.customerId(),
                request.totalAmount());
        return ResponseEntity.ok(order);
    }

    public record CreateOrderRequest(
            @NotBlank String customerId,
            @DecimalMin("0.01") BigDecimal totalAmount
    ) {}
}
