package com.farmtoyou.inventoryservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class InventoryResponse {
	private Long id;
	private Long productId;
	private BigDecimal quantity;
	private LocalDateTime lastUpdated;
}
