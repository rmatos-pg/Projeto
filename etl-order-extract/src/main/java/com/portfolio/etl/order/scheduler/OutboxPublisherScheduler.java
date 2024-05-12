package com.portfolio.etl.order.scheduler;

import com.portfolio.etl.order.domain.EtlOutbox;
import com.portfolio.etl.order.repository.EtlOutboxRepository;
import com.portfolio.etl.order.service.OrderExtractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherScheduler {

    private static final long TIMEOUT_MS = 5_000;

    private final EtlOutboxRepository outboxRepository;
    private final OrderExtractService orderExtractService;

    @Scheduled(fixedDelay = 3000)
    public void publishPending() {
        List<EtlOutbox> pending = outboxRepository.findByStatusOrderByCreatedAtAsc(EtlOutbox.OutboxStatus.PENDING);
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        for (EtlOutbox outbox : pending) {
            if (System.currentTimeMillis() > deadline) break;
            try {
                orderExtractService.publishOutbox(outbox);
            } catch (Exception e) {
                log.warn("Outbox publish failed: {}", e.getMessage());
                outbox.setStatus(EtlOutbox.OutboxStatus.FAILED);
                outboxRepository.save(outbox);
            }
        }
    }
}
