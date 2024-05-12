package com.portfolio.etl.order.service;

import com.portfolio.etl.order.domain.EtlOutbox;
import com.portfolio.etl.order.domain.JobRun;
import com.portfolio.etl.order.domain.OrderStaging;
import com.portfolio.etl.order.repository.EtlOutboxRepository;
import com.portfolio.etl.order.repository.JobRunRepository;
import com.portfolio.etl.order.repository.OrderStagingRepository;
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
public class OrderExtractService {

    private static final int RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final int EXTRACT_TIMEOUT_SECONDS = 60;

    private final OrderStagingRepository stagingRepository;
    private final EtlOutboxRepository outboxRepository;
    private final JobRunRepository jobRunRepository;

    @Transactional(rollbackFor = Exception.class)
    public int extractOrders(String runKey) {
        if (jobRunRepository.findByJobIdAndRunKey("order-extract", runKey).isPresent()) {
            log.info("Idempotent skip: runKey already processed");
            return 0;
        }
        JobRun run = JobRun.builder()
                .jobId("order-extract")
                .runKey(runKey)
                .status(JobRun.RunStatus.RUNNING)
                .build();
        jobRunRepository.save(run);

        int count = 0;
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(EXTRACT_TIMEOUT_SECONDS);
        for (int i = 0; i < 10 && System.currentTimeMillis() < deadline; i++) {
            String sourceId = "ORD-" + runKey + "-" + i;
            if (stagingRepository.findBySourceId(sourceId).isEmpty()) {
                OrderStaging staging = OrderStaging.builder()
                        .sourceId(sourceId)
                        .customerId("CUST-" + i)
                        .totalAmount(BigDecimal.valueOf(100 + i))
                        .status("PENDING")
                        .build();
                stagingRepository.save(staging);
                EtlOutbox outbox = EtlOutbox.builder()
                        .aggregateId(staging.getId().toString())
                        .eventType("OrderExtracted")
                        .payload("{\"sourceId\":\"" + sourceId + "\"}")
                        .status(EtlOutbox.OutboxStatus.PENDING)
                        .build();
                outboxRepository.save(outbox);
                count++;
            }
        }
        run.setStatus(JobRun.RunStatus.COMPLETED);
        run.setCompletedAt(Instant.now());
        jobRunRepository.save(run);
        return count;
    }

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = RETRY_ATTEMPTS,
            backoff = @Backoff(delay = RETRY_DELAY_MS)
    )
    public void publishOutbox(EtlOutbox outbox) {
        log.info("Publishing ETL outbox: {}", outbox.getEventType());
        outbox.setStatus(EtlOutbox.OutboxStatus.PUBLISHED);
        outbox.setRetryCount(outbox.getRetryCount() + 1);
        outboxRepository.save(outbox);
    }
}
