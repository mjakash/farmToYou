package com.farmtoyou.deliveryservice.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationResponse implements Serializable {
    private Long orderId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime lastUpdated;
}