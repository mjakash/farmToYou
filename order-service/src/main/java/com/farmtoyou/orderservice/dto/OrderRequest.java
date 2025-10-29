package com.farmtoyou.orderservice.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderRequest {
    private Long customerId;
    private Long farmerId; // Assuming one farmer per order
    private List<OrderItemRequest> items;
}