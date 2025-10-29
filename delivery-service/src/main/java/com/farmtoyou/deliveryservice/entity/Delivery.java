package com.farmtoyou.deliveryservice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "deliveries")
@Data
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This is the link to the Order
    @Column(nullable = false, unique = true)
    private Long orderId;

    // This is the ID of the person (Farmer or Agent) from the User-Service
    private Long deliveryPersonId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    private LocalDateTime assignedAt;
    private LocalDateTime deliveredAt;
}