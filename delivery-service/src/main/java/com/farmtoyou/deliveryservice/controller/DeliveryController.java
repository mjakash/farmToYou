package com.farmtoyou.deliveryservice.controller;

import java.nio.file.AccessDeniedException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmtoyou.deliveryservice.dto.AgentDeliveryView;
import com.farmtoyou.deliveryservice.dto.CustomerDeliveryView;
import com.farmtoyou.deliveryservice.dto.DeliveryAssignmentRequest;
import com.farmtoyou.deliveryservice.dto.DeliveryResponse;
import com.farmtoyou.deliveryservice.service.DeliveryService;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

	private final DeliveryService deliveryService;

	public DeliveryController(DeliveryService deliveryService) {
		this.deliveryService = deliveryService;
	}

	// This is the "smart" endpoint for both customer and agent
	@GetMapping("/view/{orderId}")
	public ResponseEntity<?> getDeliveryView(@PathVariable Long orderId, @RequestHeader("X-User-Id") Long userId,
			@RequestHeader("X-User-Role") String userRole) throws AccessDeniedException {

		if ("CUSTOMER".equals(userRole)) {
			CustomerDeliveryView view = deliveryService.getCustomerView(orderId, userId);
			return ResponseEntity.ok(view);
		} else if ("FARMER".equals(userRole) || "DELIVERY_AGENT".equals(userRole)) {
			AgentDeliveryView view = deliveryService.getAgentView(orderId, userId);
			return ResponseEntity.ok(view);
		} else {
			return new ResponseEntity<>("Invalid user role", HttpStatus.FORBIDDEN);
		}
	}

	// This is called by the order-service to create the delivery
	@PostMapping("/assign/{orderId}")
	public ResponseEntity<DeliveryResponse> assignDelivery(@PathVariable Long orderId,
			@RequestBody DeliveryAssignmentRequest request) {
		DeliveryResponse response = deliveryService.createAssignment(request, orderId);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	// This is called by the agent/farmer to complete the delivery
	@PostMapping("/{orderId}/complete")
	public ResponseEntity<DeliveryResponse> completeDelivery(@PathVariable Long orderId,
			@RequestHeader("X-User-Id") Long userId, @RequestHeader("X-User-Role") String userRole) throws AccessDeniedException {

		DeliveryResponse response = deliveryService.completeDelivery(orderId, userId, userRole);
		return ResponseEntity.ok(response);
	}
}