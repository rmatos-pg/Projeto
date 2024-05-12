package com.portfolio.etl.order.repository;

import com.portfolio.etl.order.domain.OrderStaging;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderStagingRepository extends JpaRepository<OrderStaging, UUID> {

    Optional<OrderStaging> findBySourceId(String sourceId);
}
