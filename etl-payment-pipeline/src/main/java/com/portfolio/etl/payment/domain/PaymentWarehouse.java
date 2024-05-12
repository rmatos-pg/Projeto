package com.portfolio.etl.payment.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_warehouse", indexes = @Index(unique = true, columnList = "batchId, sourcePaymentId"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentWarehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 64)
    private String batchId;

    @Column(nullable = false, length = 64)
    private String sourcePaymentId;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private Instant loadedAt;

    @PrePersist
    void prePersist() {
        if (loadedAt == null) loadedAt = Instant.now();
    }
}
