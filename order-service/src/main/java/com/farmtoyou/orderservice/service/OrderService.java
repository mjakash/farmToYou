package com.farmtoyou.orderservice.service;

import com.farmtoyou.orderservice.dto.OrderRequest;
import com.farmtoyou.orderservice.dto.OrderResponse;

public interface OrderService {
    OrderResponse createOrder(OrderRequest orderRequest);
}