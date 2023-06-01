package com.portfolio.orders.service;

import com.portfolio.orders.domain.IdempotencyKey;
import com.portfolio.orders.domain.Order;
import com.portfolio.orders.domain.OutboxEvent;
import com.portfolio.orders.repository.IdempotencyKeyRepository;
import com.portfolio.orders.repository.OrderRepository;
import com.portfolio.orders.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderApplicationService {

    private static final int OUTBOX_RETRY_MAX_ATTEMPTS = 3;
    private static final long OUTBOX_RETRY_DELAY_MS = 1000;

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(String idempotencyKey, String customerId, BigDecimal totalAmount) {
        return idempotencyKeyRepository.findByKey(idempotencyKey)
                .map(key -> deserializeAndReturn(key.getResponsePayload()))
                .orElseGet(() -> doCreateOrder(idempotencyKey, customerId, totalAmount));
    }

    private Order doCreateOrder(String idempotencyKey, String customerId, BigDecimal totalAmount) {
        Order order = Order.builder()
                .customerId(customerId)
                .totalAmount(totalAmount)
                .status(Order.OrderStatus.PENDING)
                .build();
        order = orderRepository.save(order);

        OutboxEvent event = OutboxEvent.builder()
                .aggregateType("Order")
                .aggregateId(order.getId().toString())
                .eventType("OrderCreated")
                .payload("{\"orderId\":\"" + order.getId() + "\",\"customerId\":\"" + customerId + "\"}")
                .status(OutboxEvent.OutboxStatus.PENDING)
                .build();
        outboxRepository.save(event);

        IdempotencyKey key = IdempotencyKey.builder()
                .key(idempotencyKey)
                .responsePayload(serializeOrder(order))
                .expiresAt(Instant.now().plusSeconds(86400))
                .build();
        idempotencyKeyRepository.save(key);

        return order;
    }

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = OUTBOX_RETRY_MAX_ATTEMPTS,
            backoff = @Backoff(delay = OUTBOX_RETRY_DELAY_MS)
    )
    public void publishOutboxEvent(OutboxEvent event) {
        log.info("Publishing outbox event: {} for aggregate {}", event.getEventType(), event.getAggregateId());
        event.setStatus(OutboxEvent.OutboxStatus.PUBLISHED);
        event.setRetryCount((event.getRetryCount() != null ? event.getRetryCount() : 0) + 1);
        outboxRepository.save(event);
    }

    private String serializeOrder(Order order) {
        return String.format("{\"id\":\"%s\",\"customerId\":\"%s\",\"totalAmount\":%s,\"status\":\"%s\"}",
                order.getId(), order.getCustomerId(), order.getTotalAmount(), order.getStatus());
    }

    private Order deserializeAndReturn(String payload) {
        return orderRepository.findById(UUID.fromString(extractId(payload))).orElseThrow();
    }

    private String extractId(String json) {
        int start = json.indexOf("\"id\":\"") + 6;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
