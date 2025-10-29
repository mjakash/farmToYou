package com.farmtoyou.deliveryservice.dto;

import lombok.Data;

@Data
public class LocationUpdateRequest {
    private Long orderId;
    private Double latitude;
    private Double longitude;
}