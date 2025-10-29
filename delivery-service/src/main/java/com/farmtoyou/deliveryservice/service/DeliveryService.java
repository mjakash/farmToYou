package com.farmtoyou.deliveryservice.service;

import com.farmtoyou.deliveryservice.dto.DeliveryAssignmentRequest;
import com.farmtoyou.deliveryservice.dto.DeliveryResponse;
import com.farmtoyou.deliveryservice.dto.LocationUpdateRequest;
import com.farmtoyou.deliveryservice.dto.LocationResponse;

public interface DeliveryService {
    // --- PostgreSQL Operations ---
    DeliveryResponse createOrUpdateAssignment(DeliveryAssignmentRequest request);
    DeliveryResponse getDeliveryByOrderId(Long orderId);
    DeliveryResponse completeDelivery(Long orderId);

    // --- Redis Operations ---
    void updateLocation(LocationUpdateRequest request);
    LocationResponse getLocation(Long orderId);
}