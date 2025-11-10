package com.farmtoyou.deliveryservice.dto;

import com.farmtoyou.deliveryservice.entity.DeliveryStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerDeliveryView {
	private Long orderId;
	private DeliveryStatus status;
	private String deliveryPersonName;
	private String deliveryPersonRole; // "FARMER" or "DELIVERY_AGENT"
	private String deliveryPersonPhone; // In future, this could be a proxy number
}