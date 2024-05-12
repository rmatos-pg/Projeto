package com.portfolio.etl.payment.service;

import com.portfolio.etl.payment.domain.BatchRun;
import com.portfolio.etl.payment.domain.PaymentWarehouse;
import com.portfolio.etl.payment.domain.PipelineOutbox;
import com.portfolio.etl.payment.repository.BatchRunRepository;
import com.portfolio.etl.payment.repository.PaymentWarehouseRepository;
import com.portfolio.etl.payment.repository.PipelineOutboxRepository;
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
public class PaymentLoadService {

    private static final int RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 500;
    private static final int LOAD_TIMEOUT_SECONDS = 45;

    private final PaymentWarehouseRepository warehouseRepository;
    private final PipelineOutboxRepository outboxRepository;
    private final BatchRunRepository batchRunRepository;

    @Transactional(rollbackFor = Exception.class)
    public int loadBatch(String batchId) {
        if (batchRunRepository.findByBatchId(batchId).isPresent()) {
            log.info("Idempotent skip: batchId already processed");
            return 0;
        }
        BatchRun run = BatchRun.builder()
                .batchId(batchId)
                .status(BatchRun.RunStatus.RUNNING)
                .build();
        batchRunRepository.save(run);

        int count = 0;
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(LOAD_TIMEOUT_SECONDS);
        for (int i = 0; i < 8 && System.currentTimeMillis() < deadline; i++) {
            String sourceId = "PAY-" + batchId + "-" + i;
            if (warehouseRepository.findByBatchIdAndSourcePaymentId(batchId, sourceId).isEmpty()) {
                PaymentWarehouse wh = PaymentWarehouse.builder()
                        .batchId(batchId)
                        .sourcePaymentId(sourceId)
                        .orderId("ORD-" + i)
                        .amount(BigDecimal.valueOf(50 + i * 10))
                        .build();
                warehouseRepository.save(wh);
                PipelineOutbox outbox = PipelineOutbox.builder()
                        .aggregateId(wh.getId().toString())
                        .eventType("PaymentLoaded")
                        .payload("{\"sourcePaymentId\":\"" + sourceId + "\"}")
                        .status(PipelineOutbox.OutboxStatus.PENDING)
                        .build();
                outboxRepository.save(outbox);
                count++;
            }
        }
        run.setStatus(BatchRun.RunStatus.COMPLETED);
        run.setCompletedAt(Instant.now());
        batchRunRepository.save(run);
        return count;
    }

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = RETRY_ATTEMPTS,
            backoff = @Backoff(delay = RETRY_DELAY_MS)
    )
    public void publishOutbox(PipelineOutbox outbox) {
        log.info("Publishing pipeline outbox: {}", outbox.getEventType());
        outbox.setStatus(PipelineOutbox.OutboxStatus.PUBLISHED);
        outbox.setRetryCount(outbox.getRetryCount() + 1);
        outboxRepository.save(outbox);
    }
}
