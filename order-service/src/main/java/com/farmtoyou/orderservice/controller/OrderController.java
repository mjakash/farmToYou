package com.farmtoyou.orderservice.controller;

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

import com.farmtoyou.orderservice.dto.DispatchRequest; // Import this
import com.farmtoyou.orderservice.dto.OrderRequest;
import com.farmtoyou.orderservice.dto.OrderResponse;
import com.farmtoyou.orderservice.dto.PackageOrderRequest;
import com.farmtoyou.orderservice.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	// ... (createOrder, getOrderById, acceptOrder, rejectOrder, packageOrder
	// endpoints are unchanged) ...

	@PostMapping("/{orderId}/dispatch")
	public ResponseEntity<OrderResponse> dispatchOrder(@PathVariable Long orderId,
			@RequestBody DispatchRequest dispatchRequest, @RequestHeader("X-User-Id") Long farmerId)
			throws AccessDeniedException {
		OrderResponse response = orderService.dispatchOrder(orderId, dispatchRequest, farmerId);
		return ResponseEntity.ok(response);
	}

	// --- UPDATE THIS ENDPOINT ---
	// This is now an internal endpoint called by delivery-service,
	// so it no longer needs user headers.
	@PostMapping("/{orderId}/complete")
	public ResponseEntity<OrderResponse> completeOrder(@PathVariable Long orderId) throws AccessDeniedException {
		OrderResponse response = orderService.completeOrder(orderId);
		return ResponseEntity.ok(response);
	}

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
		// Note: We'd normally get customerId from the token,
		// but for now, the service implementation expects it in the request.
		OrderResponse response = orderService.createOrder(orderRequest);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
		OrderResponse response = orderService.getOrderById(orderId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{orderId}/accept")
	public ResponseEntity<OrderResponse> acceptOrder(@PathVariable Long orderId) {
		// Note: Should be secured so only a Farmer can call this.
		OrderResponse response = orderService.acceptOrder(orderId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{orderId}/reject")
	public ResponseEntity<OrderResponse> rejectOrder(@PathVariable Long orderId) {
		// Note: Should be secured so only a Farmer can call this.
		OrderResponse response = orderService.rejectOrder(orderId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{orderId}/package")
	public ResponseEntity<OrderResponse> packageOrder(@PathVariable Long orderId,
			@RequestBody PackageOrderRequest packageRequest) {
		// Note: Should be secured so only a Farmer can call this.
		OrderResponse response = orderService.packageOrder(orderId, packageRequest);
		return ResponseEntity.ok(response);
	}
}