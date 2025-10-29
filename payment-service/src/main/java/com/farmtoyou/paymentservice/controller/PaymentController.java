package com.farmtoyou.paymentservice.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmtoyou.paymentservice.dto.PaymentRequest;
import com.farmtoyou.paymentservice.dto.PaymentResponse;
import com.farmtoyou.paymentservice.dto.PaymentStatus;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
	private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

	@PostMapping("/process")
	public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
		log.info("Processing payment for orderId: {} for amount: {}", request.getOrderId(), request.getAmount());

		PaymentStatus status = PaymentStatus.APPROVED;
		String message = "Payment Successful.";

		PaymentResponse response = new PaymentResponse(UUID.randomUUID().toString(), request.getOrderId(), status,
				request.getAmount(), message);
		log.info("Payment for orderId: {} was {}", request.getOrderId(), status);
		return ResponseEntity.ok(response);
	}
}
