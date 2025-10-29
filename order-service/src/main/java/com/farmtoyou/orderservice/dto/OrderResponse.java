package com.farmtoyou.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.farmtoyou.orderservice.entity.OrderStatus;

import lombok.Data;

@Data
public class OrderResponse {
    private Long id;
    private Long customerId;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private BigDecimal totalWeight;
    private LocalDateTime createdAt;
    private String message;
}