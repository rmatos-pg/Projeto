package com.portfolio.etl.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sync_runs", indexes = @Index(unique = true, columnList = "syncId"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 128)
    private String syncId;

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
