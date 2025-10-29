package com.farmtoyou.orderservice.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class InventoryResponse {
    private Long productId;
    private BigDecimal quantity;
}