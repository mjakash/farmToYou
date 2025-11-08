package com.farmtoyou.deliveryservice.dto;

import lombok.Data;

@Data
public class OrderResponse {
	private Long id;
	private Long customerId;
	private String status;
	private String deliveryAddress;
}