package com.portfolio.etl.order.repository;

import com.portfolio.etl.order.domain.EtlOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EtlOutboxRepository extends JpaRepository<EtlOutbox, UUID> {

    List<EtlOutbox> findByStatusOrderByCreatedAtAsc(EtlOutbox.OutboxStatus status);
}
