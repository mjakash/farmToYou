package com.farmtoyou.orderservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeliveryAssignmentResponse {
    private Long id;
    private Long orderId;
    private Long deliveryPersonId;
    private String status; 
    private LocalDateTime assignedAt;
}