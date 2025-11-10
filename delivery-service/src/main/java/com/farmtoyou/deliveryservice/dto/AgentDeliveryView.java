package com.farmtoyou.deliveryservice.dto;

import com.farmtoyou.deliveryservice.entity.DeliveryStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentDeliveryView {
	private Long orderId;
	private DeliveryStatus status;
	private String customerName;
	private String customerPhone;
	private String customerDeliveryAddress;
}