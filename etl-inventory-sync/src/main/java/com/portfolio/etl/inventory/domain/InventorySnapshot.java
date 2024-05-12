package com.portfolio.etl.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_snapshots", indexes = @Index(unique = true, columnList = "syncId, sku"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventorySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 64)
    private String syncId;

    @Column(nullable = false, length = 64)
    private String sku;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, updatable = false)
    private Instant syncedAt;

    @PrePersist
    void prePersist() {
        if (syncedAt == null) syncedAt = Instant.now();
    }
}
