package com.portfolio.orders.scheduler;

import com.portfolio.orders.domain.OutboxEvent;
import com.portfolio.orders.repository.OutboxEventRepository;
import com.portfolio.orders.service.OrderApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherScheduler {

    private static final long PUBLISH_TIMEOUT_MS = 5_000;

    private final OutboxEventRepository outboxRepository;
    private final OrderApplicationService orderApplicationService;

    @Scheduled(fixedDelay = 2000)
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxEvent.OutboxStatus.PENDING);
        for (OutboxEvent event : pending) {
            try {
                long deadline = System.currentTimeMillis() + PUBLISH_TIMEOUT_MS;
                orderApplicationService.publishOutboxEvent(event);
            } catch (Exception e) {
                log.warn("Outbox publish failed for event {}: {}", event.getId(), e.getMessage());
                event.setStatus(OutboxEvent.OutboxStatus.FAILED);
                event.setLastError(e.getMessage());
                outboxRepository.save(event);
            }
        }
    }
}
