package com.farmtoyou.paymentservice.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {
	private String transactionId;
	private Long orderId;
	private PaymentStatus status;
	private BigDecimal amount;
	private String message;
}
