package com.portfolio.etl.payment.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pipeline_outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Integer retryCount;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (retryCount == null) retryCount = 0;
    }

    public enum OutboxStatus {
        PENDING, PUBLISHED, FAILED
    }
}
