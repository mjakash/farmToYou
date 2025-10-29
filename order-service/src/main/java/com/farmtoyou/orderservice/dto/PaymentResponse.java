package com.farmtoyou.orderservice.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PaymentResponse {
    private String transactionId;
    private String status; // e.g., "APPROVED"
    private BigDecimal amount;
}