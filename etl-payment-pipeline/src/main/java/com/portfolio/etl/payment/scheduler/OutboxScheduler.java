package com.portfolio.etl.payment.scheduler;

import com.portfolio.etl.payment.domain.PipelineOutbox;
import com.portfolio.etl.payment.repository.PipelineOutboxRepository;
import com.portfolio.etl.payment.service.PaymentLoadService;
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

    private final PipelineOutboxRepository outboxRepository;
    private final PaymentLoadService paymentLoadService;

    @Scheduled(fixedDelay = 2500)
    public void publishPending() {
        List<PipelineOutbox> pending = outboxRepository.findByStatusOrderByCreatedAtAsc(PipelineOutbox.OutboxStatus.PENDING);
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        for (PipelineOutbox outbox : pending) {
            if (System.currentTimeMillis() > deadline) break;
            try {
                paymentLoadService.publishOutbox(outbox);
            } catch (Exception e) {
                log.warn("Outbox failed: {}", e.getMessage());
                outbox.setStatus(PipelineOutbox.OutboxStatus.FAILED);
                outboxRepository.save(outbox);
            }
        }
    }
}
