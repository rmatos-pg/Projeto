package com.portfolio.etl.inventory.service;

import com.portfolio.etl.inventory.domain.InventorySnapshot;
import com.portfolio.etl.inventory.domain.SyncOutbox;
import com.portfolio.etl.inventory.domain.SyncRun;
import com.portfolio.etl.inventory.repository.InventorySnapshotRepository;
import com.portfolio.etl.inventory.repository.SyncOutboxRepository;
import com.portfolio.etl.inventory.repository.SyncRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventorySyncService {

    private static final int RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 800;
    private static final int SYNC_TIMEOUT_SECONDS = 40;

    private final InventorySnapshotRepository snapshotRepository;
    private final SyncOutboxRepository outboxRepository;
    private final SyncRunRepository syncRunRepository;

    @Transactional(rollbackFor = Exception.class)
    public int sync(String syncId) {
        if (syncRunRepository.findBySyncId(syncId).isPresent()) {
            log.info("Idempotent skip: syncId already processed");
            return 0;
        }
        SyncRun run = SyncRun.builder()
                .syncId(syncId)
                .status(SyncRun.RunStatus.RUNNING)
                .build();
        syncRunRepository.save(run);

        int count = 0;
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(SYNC_TIMEOUT_SECONDS);
        for (int i = 0; i < 6 && System.currentTimeMillis() < deadline; i++) {
            String sku = "SKU-" + syncId + "-" + i;
            if (snapshotRepository.findBySyncIdAndSku(syncId, sku).isEmpty()) {
                InventorySnapshot snap = InventorySnapshot.builder()
                        .syncId(syncId)
                        .sku(sku)
                        .quantity(100 + i)
                        .build();
                snapshotRepository.save(snap);
                SyncOutbox outbox = SyncOutbox.builder()
                        .aggregateId(snap.getId().toString())
                        .eventType("InventorySynced")
                        .payload("{\"sku\":\"" + sku + "\",\"quantity\":" + (100 + i) + "}")
                        .status(SyncOutbox.OutboxStatus.PENDING)
                        .build();
                outboxRepository.save(outbox);
                count++;
            }
        }
        run.setStatus(SyncRun.RunStatus.COMPLETED);
        run.setCompletedAt(Instant.now());
        syncRunRepository.save(run);
        return count;
    }

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = RETRY_ATTEMPTS,
            backoff = @Backoff(delay = RETRY_DELAY_MS)
    )
    public void publishOutbox(SyncOutbox outbox) {
        log.info("Publishing sync outbox: {}", outbox.getEventType());
        outbox.setStatus(SyncOutbox.OutboxStatus.PUBLISHED);
        outbox.setRetryCount(outbox.getRetryCount() + 1);
        outboxRepository.save(outbox);
    }
}
