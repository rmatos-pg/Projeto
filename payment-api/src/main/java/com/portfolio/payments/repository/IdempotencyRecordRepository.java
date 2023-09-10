package com.portfolio.payments.repository;

import com.portfolio.payments.domain.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, java.util.UUID> {

    Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);
}
