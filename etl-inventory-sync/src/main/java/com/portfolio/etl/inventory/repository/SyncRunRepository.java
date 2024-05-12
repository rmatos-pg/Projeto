package com.portfolio.etl.inventory.repository;

import com.portfolio.etl.inventory.domain.SyncRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SyncRunRepository extends JpaRepository<SyncRun, UUID> {

    Optional<SyncRun> findBySyncId(String syncId);
}
