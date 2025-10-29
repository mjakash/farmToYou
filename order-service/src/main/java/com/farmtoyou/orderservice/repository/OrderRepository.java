package com.farmtoyou.orderservice.repository;

import com.farmtoyou.orderservice.entity.Order;
import com.farmtoyou.orderservice.entity.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByFarmerId(Long farmerId);
    List<Order> findByStatusAndAcceptanceDeadlineBefore(OrderStatus status, LocalDateTime deadline);
}