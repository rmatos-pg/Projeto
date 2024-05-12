package com.portfolio.etl.inventory.repository;

import com.portfolio.etl.inventory.domain.SyncOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SyncOutboxRepository extends JpaRepository<SyncOutbox, UUID> {

    List<SyncOutbox> findByStatusOrderByCreatedAtAsc(SyncOutbox.OutboxStatus status);
}
