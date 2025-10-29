package com.farmtoyou.deliveryservice.dto;

import java.time.LocalDateTime;

import com.farmtoyou.deliveryservice.entity.DeliveryStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeliveryResponse {
    private Long id;
    private Long orderId;
    private Long deliveryPersonId;
    private DeliveryStatus status;
    private LocalDateTime assignedAt;
}