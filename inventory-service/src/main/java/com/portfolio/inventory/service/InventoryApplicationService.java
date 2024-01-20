package com.portfolio.inventory.service;

import com.portfolio.inventory.domain.IdempotencyEntry;
import com.portfolio.inventory.domain.InventoryOutbox;
import com.portfolio.inventory.domain.StockItem;
import com.portfolio.inventory.repository.IdempotencyEntryRepository;
import com.portfolio.inventory.repository.InventoryOutboxRepository;
import com.portfolio.inventory.repository.StockItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryApplicationService {

    private static final int RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 800;

    private final StockItemRepository stockItemRepository;
    private final InventoryOutboxRepository outboxRepository;
    private final IdempotencyEntryRepository idempotencyRepository;

    @Transactional(rollbackFor = Exception.class)
    public StockItem reserveStock(String idempotencyKey, String sku, String name, int quantity) {
        return idempotencyRepository.findByKeyValue(idempotencyKey)
                .filter(e -> e.getExpiresAt().isAfter(Instant.now()))
                .map(e -> fromCached(e.getCachedResponse()))
                .orElseGet(() -> doReserveStock(idempotencyKey, sku, name, quantity));
    }

    private StockItem doReserveStock(String idempotencyKey, String sku, String name, int quantity) {
        StockItem item = stockItemRepository.findBySku(sku)
                .orElseGet(() -> {
                    StockItem newItem = StockItem.builder().sku(sku).name(name).quantity(0).build();
                    return stockItemRepository.save(newItem);
                });
        item.setQuantity(item.getQuantity() + quantity);
        item = stockItemRepository.save(item);

        InventoryOutbox outbox = InventoryOutbox.builder()
                .aggregateType("StockItem")
                .aggregateId(item.getId().toString())
                .eventType("StockReserved")
                .payload(String.format("{\"sku\":\"%s\",\"quantity\":%d}", sku, quantity))
                .status(InventoryOutbox.OutboxStatus.PENDING)
                .build();
        outboxRepository.save(outbox);

        IdempotencyEntry entry = IdempotencyEntry.builder()
                .keyValue(idempotencyKey)
                .cachedResponse(serialize(item))
                .expiresAt(Instant.now().plusSeconds(86400))
                .build();
        idempotencyRepository.save(entry);

        return item;
    }

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = RETRY_ATTEMPTS,
            backoff = @Backoff(delay = RETRY_DELAY_MS)
    )
    public void publishOutbox(InventoryOutbox outbox) {
        log.info("Publishing inventory outbox: {}", outbox.getEventType());
        outbox.setStatus(InventoryOutbox.OutboxStatus.PUBLISHED);
        outbox.setRetryCount(outbox.getRetryCount() + 1);
        outboxRepository.save(outbox);
    }

    private String serialize(StockItem item) {
        return String.format("{\"id\":\"%s\",\"sku\":\"%s\",\"quantity\":%d}",
                item.getId(), item.getSku(), item.getQuantity());
    }

    private StockItem fromCached(String cached) {
        String id = cached.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
        return stockItemRepository.findById(UUID.fromString(id)).orElseThrow();
    }
}
