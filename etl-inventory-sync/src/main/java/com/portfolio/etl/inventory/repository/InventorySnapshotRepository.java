package com.portfolio.etl.inventory.repository;

import com.portfolio.etl.inventory.domain.InventorySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventorySnapshotRepository extends JpaRepository<InventorySnapshot, UUID> {

    Optional<InventorySnapshot> findBySyncIdAndSku(String syncId, String sku);
}
