package com.portfolio.etl.order.job;

import com.portfolio.etl.order.service.OrderExtractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExtractJobRunner implements ApplicationRunner {

    private final OrderExtractService orderExtractService;

    @Override
    public void run(ApplicationArguments args) {
        String runKey = args.containsOption("runKey")
                ? args.getOptionValues("runKey").get(0)
                : Instant.now().toString();
        int count = orderExtractService.extractOrders(runKey);
        log.info("Extract completed: {} records", count);
    }
}
