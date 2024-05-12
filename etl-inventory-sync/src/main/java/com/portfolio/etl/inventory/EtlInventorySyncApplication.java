package com.portfolio.etl.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRetry
@EnableScheduling
public class EtlInventorySyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(EtlInventorySyncApplication.class, args);
    }
}
