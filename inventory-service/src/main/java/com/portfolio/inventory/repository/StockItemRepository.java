package com.portfolio.inventory.repository;

import com.portfolio.inventory.domain.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StockItemRepository extends JpaRepository<StockItem, UUID> {

    Optional<StockItem> findBySku(String sku);
}
