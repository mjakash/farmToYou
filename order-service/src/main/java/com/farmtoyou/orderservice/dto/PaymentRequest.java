package com.farmtoyou.orderservice.dto;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder // Builder pattern is nice for this
public class PaymentRequest {
    private Long orderId;
    private BigDecimal amount;
    private String paymentMethod;
}