package com.farmtoyou.deliveryservice.service.impl;

import com.farmtoyou.deliveryservice.dto.DeliveryAssignmentRequest;
import com.farmtoyou.deliveryservice.dto.DeliveryResponse;
import com.farmtoyou.deliveryservice.dto.LocationResponse;
import com.farmtoyou.deliveryservice.dto.LocationUpdateRequest;
import com.farmtoyou.deliveryservice.entity.Delivery;
import com.farmtoyou.deliveryservice.entity.DeliveryStatus;
import com.farmtoyou.deliveryservice.repository.DeliveryRepository;
import com.farmtoyou.deliveryservice.service.DeliveryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // This is the key prefix we'll use in Redis
    private static final String LOCATION_KEY_PREFIX = "location:";

    public DeliveryServiceImpl(DeliveryRepository deliveryRepository, RedisTemplate<String, Object> redisTemplate) {
        this.deliveryRepository = deliveryRepository;
        this.redisTemplate = redisTemplate;
    }

    // --- PostgreSQL Logic ---

    @Override
    public DeliveryResponse createOrUpdateAssignment(DeliveryAssignmentRequest request) {
        // Find existing or create new
        Delivery delivery = deliveryRepository.findByOrderId(request.getOrderId())
                .orElse(new Delivery());

        delivery.setOrderId(request.getOrderId());
        delivery.setDeliveryPersonId(request.getDeliveryPersonId());
        delivery.setStatus(DeliveryStatus.ASSIGNED); // Set status to ASSIGNED
        delivery.setAssignedAt(LocalDateTime.now());

        Delivery savedDelivery = deliveryRepository.save(delivery);
        
        return DeliveryResponse.builder()
                .id(savedDelivery.getId())
                .orderId(savedDelivery.getOrderId())
                .deliveryPersonId(savedDelivery.getDeliveryPersonId())
                .status(savedDelivery.getStatus())
                .assignedAt(savedDelivery.getAssignedAt())
                .build();
    }

    @Override
    public DeliveryResponse getDeliveryByOrderId(Long orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found for orderId: " + orderId));
        
        return DeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .deliveryPersonId(delivery.getDeliveryPersonId())
                .status(delivery.getStatus())
                .assignedAt(delivery.getAssignedAt())
                .build();
    }

    // --- Redis Logic ---

    @Override
    public void updateLocation(LocationUpdateRequest request) {
        String key = LOCATION_KEY_PREFIX + request.getOrderId();
        
        LocationResponse location = new LocationResponse(
                request.getOrderId(),
                request.getLatitude(),
                request.getLongitude(),
                LocalDateTime.now()
        );
        
        // Store in Redis with a 1-hour expiration time
        redisTemplate.opsForValue().set(key, location, Duration.ofHours(1));
    }

    @Override
    public LocationResponse getLocation(Long orderId) {
        String key = LOCATION_KEY_PREFIX + orderId;
        LocationResponse location = (LocationResponse) redisTemplate.opsForValue().get(key);
        
        if (location == null) {
            throw new EntityNotFoundException("No location data found for orderId: " + orderId);
        }
        
        return location;
    }
}