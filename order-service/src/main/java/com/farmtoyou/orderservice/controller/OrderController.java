package com.farmtoyou.orderservice.controller;

import com.farmtoyou.orderservice.dto.DispatchRequest;
import com.farmtoyou.orderservice.dto.OrderRequest;
import com.farmtoyou.orderservice.dto.OrderResponse;
import com.farmtoyou.orderservice.dto.PackageOrderRequest;
import com.farmtoyou.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
		OrderResponse response = orderService.createOrder(orderRequest);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PostMapping("/{orderId}/accept")
	public ResponseEntity<OrderResponse> acceptOrder(@PathVariable Long orderId) {
		OrderResponse response = orderService.acceptOrder(orderId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{orderId}/reject")
	public ResponseEntity<OrderResponse> rejectOrder(@PathVariable Long orderId) {
		OrderResponse response = orderService.rejectOrder(orderId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{orderId}/dispatch")
	public ResponseEntity<OrderResponse> dispatchOrder(@PathVariable Long orderId,
			@RequestBody DispatchRequest dispatchRequest) {
		OrderResponse response = orderService.dispatchOrder(orderId, dispatchRequest);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{orderId}/pack")
	public ResponseEntity<OrderResponse> packageOrder(@PathVariable Long orderId,
			@RequestBody PackageOrderRequest packageRequest) {
		OrderResponse response = orderService.packageOrder(orderId, packageRequest);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{orderId}/complete")
	public ResponseEntity<OrderResponse> completeOrder(@PathVariable Long orderId) {
		OrderResponse response = orderService.completeOrder(orderId);
		return ResponseEntity.ok(response);
	}

}