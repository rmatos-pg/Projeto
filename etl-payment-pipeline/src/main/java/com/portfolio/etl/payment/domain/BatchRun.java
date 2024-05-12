package com.portfolio.etl.payment.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "batch_runs", indexes = @Index(unique = true, columnList = "batchId"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 128)
    private String batchId;

    @Column(nullable = false, updatable = false)
    private Instant startedAt;

    private Instant completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RunStatus status;

    @PrePersist
    void prePersist() {
        if (startedAt == null) startedAt = Instant.now();
    }

    public enum RunStatus {
        RUNNING, COMPLETED, FAILED
    }
}
