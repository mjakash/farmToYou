package com.farmtoyou.inventoryservice.service;

import com.farmtoyou.inventoryservice.dto.InventoryRequest;
import com.farmtoyou.inventoryservice.dto.InventoryResponse;

public interface InventoryService {
	InventoryResponse updateStock(InventoryRequest request);

	InventoryResponse getStockByProductId(Long productId);

	InventoryResponse reduceStock(InventoryRequest request);
}
