package com.portfolio.etl.payment.job;

import com.portfolio.etl.payment.service.PaymentLoadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoadJobRunner implements ApplicationRunner {

    private final PaymentLoadService paymentLoadService;

    @Override
    public void run(ApplicationArguments args) {
        String batchId = args.containsOption("batchId")
                ? args.getOptionValues("batchId").get(0)
                : "batch-" + Instant.now().toEpochMilli();
        int count = paymentLoadService.loadBatch(batchId);
        log.info("Load completed: {} records", count);
    }
}
