package com.portfolio.inventory.repository;

import com.portfolio.inventory.domain.IdempotencyEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyEntryRepository extends JpaRepository<IdempotencyEntry, java.util.UUID> {

    Optional<IdempotencyEntry> findByKeyValue(String keyValue);
}
