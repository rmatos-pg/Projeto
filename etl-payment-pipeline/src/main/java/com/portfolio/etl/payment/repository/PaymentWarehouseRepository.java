package com.portfolio.etl.payment.repository;

import com.portfolio.etl.payment.domain.PaymentWarehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentWarehouseRepository extends JpaRepository<PaymentWarehouse, UUID> {

    Optional<PaymentWarehouse> findByBatchIdAndSourcePaymentId(String batchId, String sourcePaymentId);
}
