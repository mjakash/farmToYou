package com.farmtoyou.orderservice.controller;

import com.farmtoyou.orderservice.dto.OrderRequest;
import com.farmtoyou.orderservice.dto.OrderResponse;
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
}