package com.portfolio.payments.repository;

import com.portfolio.payments.domain.PaymentOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentOutboxRepository extends JpaRepository<PaymentOutbox, UUID> {

    List<PaymentOutbox> findByStatusOrderByCreatedAtAsc(PaymentOutbox.OutboxStatus status);
}
