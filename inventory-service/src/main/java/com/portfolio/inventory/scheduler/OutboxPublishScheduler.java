package com.portfolio.inventory.scheduler;

import com.portfolio.inventory.domain.InventoryOutbox;
import com.portfolio.inventory.repository.InventoryOutboxRepository;
import com.portfolio.inventory.service.InventoryApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublishScheduler {

    private static final long TIMEOUT_MS = 5_000;

    private final InventoryOutboxRepository outboxRepository;
    private final InventoryApplicationService inventoryApplicationService;

    @Scheduled(fixedDelay = 2500)
    public void publishPending() {
        List<InventoryOutbox> pending = outboxRepository.findByStatusOrderByCreatedAtAsc(InventoryOutbox.OutboxStatus.PENDING);
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        for (InventoryOutbox outbox : pending) {
            if (System.currentTimeMillis() > deadline) break;
            try {
                inventoryApplicationService.publishOutbox(outbox);
            } catch (Exception e) {
                log.warn("Outbox publish failed: {}", e.getMessage());
                outbox.setStatus(InventoryOutbox.OutboxStatus.FAILED);
                outboxRepository.save(outbox);
            }
        }
    }
}
