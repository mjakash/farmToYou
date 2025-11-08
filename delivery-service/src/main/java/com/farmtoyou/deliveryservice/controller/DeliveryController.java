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

	// --- Assignment Endpoint ---
	
	@PostMapping("/{orderId}/complete")
    public ResponseEntity<DeliveryResponse> completeDelivery(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId,
			@RequestHeader("X-User-Role") String userRole) throws AccessDeniedException {
        
        DeliveryResponse response = deliveryService.completeDelivery(orderId, userId, userRole);
        return ResponseEntity.ok(response);
    }

	@GetMapping("/view/{orderId}")
	public ResponseEntity<?> getDeliveryView(@PathVariable Long orderId,
			@RequestHeader("X-User-Email") String userEmail) {

		// This is a simplified check. We'd normally check the user's role.
		// For now, we'll assume if the service call works, the role is correct.

		try {
			// Try to get the view as if the user is a customer
			CustomerDeliveryView customerView = deliveryService.getCustomerView(orderId, userEmail);
			return ResponseEntity.ok(customerView);
		} catch (Exception customerException) {
			// If that fails, try to get the view as if the user is an agent
			try {
				AgentDeliveryView agentView = deliveryService.getAgentView(orderId, userEmail);
				return ResponseEntity.ok(agentView);
			} catch (Exception agentException) {
				// If both fail, the user is not authorized
				return new ResponseEntity<>("Unauthorized to view this order's delivery details.",
						HttpStatus.UNAUTHORIZED);
			}
		}
	}

	// POST http://localhost:8086/api/delivery/assign
	@PostMapping("/assign")
	public ResponseEntity<DeliveryResponse> assignDelivery(@RequestBody DeliveryAssignmentRequest request) {
		DeliveryResponse response = deliveryService.createOrUpdateAssignment(request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	// GET http://localhost:8086/api/delivery/order/1
	@GetMapping("/order/{orderId}")
	public ResponseEntity<DeliveryResponse> getDeliveryStatus(@PathVariable Long orderId) {
		return ResponseEntity.ok(deliveryService.getDeliveryByOrderId(orderId));
	}

	// --- Location Endpoints ---



}