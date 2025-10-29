package com.farmtoyou.orderservice.dto;

import com.farmtoyou.orderservice.entity.DeliveryChoice;
import lombok.Data;

@Data
public class DispatchRequest {
    private DeliveryChoice deliveryChoice; 
    private Long deliveryPersonId; 
}