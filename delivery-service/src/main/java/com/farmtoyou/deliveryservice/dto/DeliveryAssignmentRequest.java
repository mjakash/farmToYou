package com.farmtoyou.deliveryservice.dto;

import lombok.Data;

@Data
public class DeliveryAssignmentRequest {
	private Long orderId;
	private Long deliveryPersonId;
}
