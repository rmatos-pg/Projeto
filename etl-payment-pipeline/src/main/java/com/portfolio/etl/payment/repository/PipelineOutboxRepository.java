package com.portfolio.etl.payment.repository;

import com.portfolio.etl.payment.domain.PipelineOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PipelineOutboxRepository extends JpaRepository<PipelineOutbox, UUID> {

    List<PipelineOutbox> findByStatusOrderByCreatedAtAsc(PipelineOutbox.OutboxStatus status);
}
