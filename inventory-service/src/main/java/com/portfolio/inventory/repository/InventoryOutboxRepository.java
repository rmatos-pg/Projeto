package com.portfolio.inventory.repository;

import com.portfolio.inventory.domain.InventoryOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryOutboxRepository extends JpaRepository<InventoryOutbox, UUID> {

    List<InventoryOutbox> findByStatusOrderByCreatedAtAsc(InventoryOutbox.OutboxStatus status);
}
