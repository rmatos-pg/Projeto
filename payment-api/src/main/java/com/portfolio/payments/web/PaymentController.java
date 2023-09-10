package com.portfolio.payments.web;

import com.portfolio.payments.domain.Payment;
import com.portfolio.payments.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Payment> process(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Payment payment = paymentService.processPayment(
                idempotencyKey,
                request.orderId(),
                request.customerId(),
                request.amount());
        return ResponseEntity.ok(payment);
    }

    public record PaymentRequest(
            @NotBlank String orderId,
            @NotBlank String customerId,
            @DecimalMin("0.01") BigDecimal amount
    ) {}
}
