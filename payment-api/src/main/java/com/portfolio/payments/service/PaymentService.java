package com.portfolio.payments.service;

import com.portfolio.payments.domain.IdempotencyRecord;
import com.portfolio.payments.domain.Payment;
import com.portfolio.payments.domain.PaymentOutbox;
import com.portfolio.payments.repository.IdempotencyRecordRepository;
import com.portfolio.payments.repository.PaymentOutboxRepository;
import com.portfolio.payments.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private static final int RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 500;
    private static final int OPERATION_TIMEOUT_SECONDS = 15;

    private final PaymentRepository paymentRepository;
    private final PaymentOutboxRepository outboxRepository;
    private final IdempotencyRecordRepository idempotencyRepository;

    @Transactional(rollbackFor = Exception.class)
    public Payment processPayment(String idempotencyKey, String orderId, String customerId, BigDecimal amount) {
        return idempotencyRepository.findByIdempotencyKey(idempotencyKey)
                .filter(r -> r.getExpiresAt().isAfter(Instant.now()))
                .map(r -> deserializePayment(r.getResponseBody()))
                .orElseGet(() -> doProcessPayment(idempotencyKey, orderId, customerId, amount));
    }

    private Payment doProcessPayment(String idempotencyKey, String orderId, String customerId, BigDecimal amount) {
        Payment payment = Payment.builder()
                .orderId(orderId)
                .customerId(customerId)
                .amount(amount)
                .status(Payment.PaymentStatus.PENDING)
                .build();
        payment = paymentRepository.save(payment);

        PaymentOutbox outbox = PaymentOutbox.builder()
                .aggregateId(payment.getId().toString())
                .eventType("PaymentCreated")
                .payload(String.format("{\"paymentId\":\"%s\",\"orderId\":\"%s\"}", payment.getId(), orderId))
                .status(PaymentOutbox.OutboxStatus.PENDING)
                .build();
        outboxRepository.save(outbox);

        IdempotencyRecord record = IdempotencyRecord.builder()
                .idempotencyKey(idempotencyKey)
                .responseBody(serializePayment(payment))
                .expiresAt(Instant.now().plusSeconds(86400))
                .build();
        idempotencyRepository.save(record);

        return payment;
    }

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = RETRY_ATTEMPTS,
            backoff = @Backoff(delay = RETRY_DELAY_MS)
    )
    public void publishOutbox(PaymentOutbox outbox) {
        log.info("Publishing payment outbox: {}", outbox.getEventType());
        outbox.setStatus(PaymentOutbox.OutboxStatus.PUBLISHED);
        outbox.setRetryCount(outbox.getRetryCount() + 1);
        outboxRepository.save(outbox);
    }

    private String serializePayment(Payment p) {
        return String.format("{\"id\":\"%s\",\"orderId\":\"%s\",\"status\":\"%s\"}",
                p.getId(), p.getOrderId(), p.getStatus());
    }

    private Payment deserializePayment(String body) {
        String id = body.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
        return paymentRepository.findById(UUID.fromString(id)).orElseThrow();
    }
}
