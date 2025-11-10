package com.farmtoyou.orderservice.service;

import java.nio.file.AccessDeniedException;

import com.farmtoyou.orderservice.dto.DispatchRequest;
import com.farmtoyou.orderservice.dto.OrderRequest;
import com.farmtoyou.orderservice.dto.OrderResponse;
import com.farmtoyou.orderservice.dto.PackageOrderRequest;

public interface OrderService {
	OrderResponse createOrder(OrderRequest orderRequest);

	OrderResponse acceptOrder(Long orderId);

	OrderResponse rejectOrder(Long orderId);

	OrderResponse packageOrder(Long orderId, PackageOrderRequest packageRequest);

	OrderResponse dispatchOrder(Long orderId, DispatchRequest dispatchRequest, Long farmerId) throws AccessDeniedException;

	// Update this
	OrderResponse completeOrder(Long orderId) throws AccessDeniedException;

	OrderResponse getOrderById(Long orderId);
}