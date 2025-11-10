package com.farmtoyou.deliveryservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.farmtoyou.deliveryservice.entity.Delivery;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
	Optional<Delivery> findByOrderId(Long orderId);
}