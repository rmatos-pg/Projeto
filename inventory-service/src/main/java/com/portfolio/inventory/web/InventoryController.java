package com.portfolio.inventory.web;

import com.portfolio.inventory.domain.StockItem;
import com.portfolio.inventory.service.InventoryApplicationService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryApplicationService inventoryApplicationService;

    @PostMapping("/reserve")
    public ResponseEntity<StockItem> reserve(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody ReserveRequest request) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        StockItem item = inventoryApplicationService.reserveStock(
                idempotencyKey,
                request.sku(),
                request.name(),
                request.quantity());
        return ResponseEntity.ok(item);
    }

    public record ReserveRequest(
            @NotBlank String sku,
            @NotBlank String name,
            @Min(1) int quantity
    ) {}
}
