package com.farmtoyou.inventoryservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "inventory")
@Data
public class Inventory {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private Long productId;

	@Column(nullable = false)
	private BigDecimal quantity;

	@Column(columnDefinition = "TIMESTAMP")
	private LocalDateTime lastUpdated;
}
