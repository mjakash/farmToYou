package com.farmtoyou.orderservice.dto;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder 
public class PaymentRequest {
    private Long orderId;
    private BigDecimal amount;
    private String paymentMethod;
}