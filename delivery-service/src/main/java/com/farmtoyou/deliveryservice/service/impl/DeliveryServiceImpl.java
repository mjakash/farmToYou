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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WebClient.Builder webClientBuilder;
    private final String orderServiceUrl;
    
    private static final String LOCATION_KEY_PREFIX = "location:";

    public DeliveryServiceImpl(DeliveryRepository deliveryRepository,
                               RedisTemplate<String, Object> redisTemplate,
                               WebClient.Builder webClientBuilder,
                               @Value("${order.service.url}") String orderServiceUrl) {
        this.deliveryRepository = deliveryRepository;
        this.redisTemplate = redisTemplate;
        this.webClientBuilder = webClientBuilder;
        this.orderServiceUrl = orderServiceUrl + "/api/orders";
    }

    // --- PostgreSQL Logic ---

    @Override
    @Transactional
    public DeliveryResponse createOrUpdateAssignment(DeliveryAssignmentRequest request) {
        Delivery delivery = deliveryRepository.findByOrderId(request.getOrderId())
                .orElse(new Delivery());

        delivery.setOrderId(request.getOrderId());
        delivery.setDeliveryPersonId(request.getDeliveryPersonId());
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedAt(LocalDateTime.now());
        delivery.setDeliveredAt(null); 

        Delivery savedDelivery = deliveryRepository.save(delivery);
        
        return mapToDeliveryResponse(savedDelivery);
    }

    @Override
    public DeliveryResponse getDeliveryByOrderId(Long orderId) {
        Delivery delivery = findDeliveryByOrderIdOrThrow(orderId);
        return mapToDeliveryResponse(delivery);
    }

    @Override
    @Transactional
    public DeliveryResponse completeDelivery(Long orderId) {
        // 1. Update local delivery status
        Delivery delivery = findDeliveryByOrderIdOrThrow(orderId);

        if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw new IllegalStateException("Delivery for order " + orderId + " has already been completed.");
        }

        if (delivery.getStatus() != DeliveryStatus.ASSIGNED && delivery.getStatus() != DeliveryStatus.EN_ROUTE) {
            throw new IllegalStateException("Delivery cannot be completed. Status is: " + delivery.getStatus());
        }

        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(LocalDateTime.now());
        Delivery savedDelivery = deliveryRepository.save(delivery);

        // 2. Call Order Service to notify it of completion
        try {
            webClientBuilder.build()
                    .post()
                    .uri(orderServiceUrl + "/" + orderId + "/complete")
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to notify order-service of completion: " + e.getMessage());
        }

        // 3. Delete the location from Redis.
        redisTemplate.delete(LOCATION_KEY_PREFIX + orderId);

        return mapToDeliveryResponse(savedDelivery);
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

    // --- UTILITY METHODS ---

    private Delivery findDeliveryByOrderIdOrThrow(Long orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found for orderId: " + orderId)); // <-- This line is fixed
    }
    
    private DeliveryResponse mapToDeliveryResponse(Delivery delivery) {
        return DeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .deliveryPersonId(delivery.getDeliveryPersonId())
                .status(delivery.getStatus())
                .assignedAt(delivery.getAssignedAt())
                .deliveredAt(delivery.getDeliveredAt())
                .build();
    }
}