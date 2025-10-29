package com.farmtoyou.paymentservice.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PaymentRequest {
	private long orderId;
	private BigDecimal amount;
	private String paymentMethod;
}
