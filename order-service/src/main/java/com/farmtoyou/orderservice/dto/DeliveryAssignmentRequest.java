package com.farmtoyou.orderservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliveryAssignmentRequest {
    private Long orderId;
    private Long deliveryPersonId;
}