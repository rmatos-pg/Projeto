package com.portfolio.payments.scheduler;

import com.portfolio.payments.domain.PaymentOutbox;
import com.portfolio.payments.repository.PaymentOutboxRepository;
import com.portfolio.payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private static final long TIMEOUT_MS = 5_000;

    private final PaymentOutboxRepository outboxRepository;
    private final PaymentService paymentService;

    @Scheduled(fixedDelay = 3000)
    public void processOutbox() {
        List<PaymentOutbox> pending = outboxRepository.findByStatusOrderByCreatedAtAsc(PaymentOutbox.OutboxStatus.PENDING);
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        for (PaymentOutbox outbox : pending) {
            if (System.currentTimeMillis() > deadline) break;
            try {
                paymentService.publishOutbox(outbox);
            } catch (Exception e) {
                log.warn("Outbox failed: {}", e.getMessage());
                outbox.setStatus(PaymentOutbox.OutboxStatus.FAILED);
                outboxRepository.save(outbox);
            }
        }
    }
}
