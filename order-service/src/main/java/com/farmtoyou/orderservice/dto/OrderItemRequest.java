package com.farmtoyou.orderservice.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long productId;
    private BigDecimal quantity; // e.g., 2.5 (for 2.5kg)
}