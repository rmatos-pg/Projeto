package com.portfolio.etl.inventory.job;

import com.portfolio.etl.inventory.service.InventorySyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class SyncJobRunner implements ApplicationRunner {

    private final InventorySyncService inventorySyncService;

    @Override
    public void run(ApplicationArguments args) {
        String syncId = args.containsOption("syncId")
                ? args.getOptionValues("syncId").get(0)
                : "sync-" + Instant.now().toEpochMilli();
        int count = inventorySyncService.sync(syncId);
        log.info("Sync completed: {} records", count);
    }
}
