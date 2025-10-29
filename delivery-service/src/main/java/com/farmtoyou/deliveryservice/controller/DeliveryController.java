package com.farmtoyou.deliveryservice.controller;

import com.farmtoyou.deliveryservice.dto.DeliveryAssignmentRequest;
import com.farmtoyou.deliveryservice.dto.DeliveryResponse;
import com.farmtoyou.deliveryservice.dto.LocationUpdateRequest;
import com.farmtoyou.deliveryservice.dto.LocationResponse;
import com.farmtoyou.deliveryservice.service.DeliveryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // --- Assignment Endpoint ---

    // POST http://localhost:8086/api/delivery/assign
    @PostMapping("/assign")
    public ResponseEntity<DeliveryResponse> assignDelivery(@RequestBody DeliveryAssignmentRequest request) {
        DeliveryResponse response = deliveryService.createOrUpdateAssignment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    // GET http://localhost:8086/api/delivery/order/1
    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponse> getDeliveryStatus(@PathVariable Long orderId) {
        return ResponseEntity.ok(deliveryService.getDeliveryByOrderId(orderId));
    }

    // --- Location Endpoints ---

    // POST http://localhost:8086/api/delivery/location
    @PostMapping("/location")
    public ResponseEntity<Void> updateLocation(@RequestBody LocationUpdateRequest request) {
        deliveryService.updateLocation(request);
        return ResponseEntity.ok().build();
    }

    // GET http://localhost:8086/api/delivery/location/1
    @GetMapping("/location/{orderId}")
    public ResponseEntity<LocationResponse> getLocation(@PathVariable Long orderId) {
        return ResponseEntity.ok(deliveryService.getLocation(orderId));
    }
}