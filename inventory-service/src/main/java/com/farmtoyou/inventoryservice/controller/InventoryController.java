package com.farmtoyou.inventoryservice.controller;

import com.farmtoyou.inventoryservice.dto.InventoryRequest;
import com.farmtoyou.inventoryservice.dto.InventoryResponse;
import com.farmtoyou.inventoryservice.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

	private final InventoryService inventoryService;

	public InventoryController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	@PostMapping
	public ResponseEntity<InventoryResponse> updateStock(@RequestBody InventoryRequest request) {
		InventoryResponse response = inventoryService.updateStock(request);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{productId}")
	public ResponseEntity<InventoryResponse> getStock(@PathVariable Long productId) {
		InventoryResponse response = inventoryService.getStockByProductId(productId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/reduce")
	public ResponseEntity<InventoryResponse> reduceStock(@RequestBody InventoryRequest request) {
		InventoryResponse response = inventoryService.reduceStock(request);
		return ResponseEntity.ok(response);
	}
}