package com.farmtoyou.orderservice.dto;

import java.util.List;
import com.farmtoyou.orderservice.entity.PaymentMethod;
import lombok.Data;

@Data
public class OrderRequest {
	private Long customerId;
	private Long farmerId;
	private List<OrderItemRequest> items;
	private PaymentMethod paymentMethod;
	private String deliveryAddress;
}