package com.portfolio.etl.order.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_runs", indexes = @Index(unique = true, columnList = "jobId, runKey"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 64)
    private String jobId;

    @Column(nullable = false, unique = true, length = 128)
    private String runKey;

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
