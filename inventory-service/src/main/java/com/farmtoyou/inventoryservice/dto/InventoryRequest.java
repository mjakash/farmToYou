package com.farmtoyou.inventoryservice.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class InventoryRequest {
	private Long productId;
	private BigDecimal quantity;
}
