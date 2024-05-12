package com.portfolio.etl.inventory.scheduler;

import com.portfolio.etl.inventory.domain.SyncOutbox;
import com.portfolio.etl.inventory.repository.SyncOutboxRepository;
import com.portfolio.etl.inventory.service.InventorySyncService;
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

    private final SyncOutboxRepository outboxRepository;
    private final InventorySyncService inventorySyncService;

    @Scheduled(fixedDelay = 2000)
    public void publishPending() {
        List<SyncOutbox> pending = outboxRepository.findByStatusOrderByCreatedAtAsc(SyncOutbox.OutboxStatus.PENDING);
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        for (SyncOutbox outbox : pending) {
            if (System.currentTimeMillis() > deadline) break;
            try {
                inventorySyncService.publishOutbox(outbox);
            } catch (Exception e) {
                log.warn("Outbox failed: {}", e.getMessage());
                outbox.setStatus(SyncOutbox.OutboxStatus.FAILED);
                outboxRepository.save(outbox);
            }
        }
    }
}
