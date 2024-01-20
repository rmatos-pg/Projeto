package com.portfolio.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Configuration
public class RetryAndTimeoutConfig {

    @Bean
    public RetryTemplate outboxRetryTemplate() {
        RetryTemplate template = new RetryTemplate();
        FixedBackOffPolicy backOff = new FixedBackOffPolicy();
        backOff.setBackOffPeriod(800);
        template.setBackOffPolicy(backOff);
        template.setRetryPolicy(new SimpleRetryPolicy(3));
        return template;
    }

    @Bean(name = "outboxExecutor")
    public Executor outboxExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setAwaitTerminationSeconds((int) TimeUnit.SECONDS.toSeconds(10));
        executor.initialize();
        return executor;
    }
}
