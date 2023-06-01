package com.portfolio.orders.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Configuration
public class TimeoutConfig {

    public static final int DEFAULT_OPERATION_TIMEOUT_SECONDS = 30;

    @Bean(name = "outboxPublisherExecutor")
    public Executor outboxPublisherExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("outbox-");
        executor.setAwaitTerminationSeconds((int) TimeUnit.SECONDS.toSeconds(10));
        executor.initialize();
        return executor;
    }
}
