package com.portfolio.etl.payment.repository;

import com.portfolio.etl.payment.domain.BatchRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BatchRunRepository extends JpaRepository<BatchRun, UUID> {

    Optional<BatchRun> findByBatchId(String batchId);
}
