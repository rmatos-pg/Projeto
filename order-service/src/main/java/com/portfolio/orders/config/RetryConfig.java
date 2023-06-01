package com.portfolio.orders.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryConfig {

    @Bean
    public RetryTemplate outboxRetryTemplate() {
        RetryTemplate template = new RetryTemplate();
        FixedBackOffPolicy backOff = new FixedBackOffPolicy();
        backOff.setBackOffPeriod(1000);
        template.setBackOffPolicy(backOff);
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3);
        template.setRetryPolicy(retryPolicy);
        return template;
    }
}
