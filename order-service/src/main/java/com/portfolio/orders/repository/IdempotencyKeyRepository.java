package com.portfolio.orders.repository;

import com.portfolio.orders.domain.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, java.util.UUID> {

    Optional<IdempotencyKey> findByKey(String key);
}
