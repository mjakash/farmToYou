package com.farmtoyou.inventoryservice.service.impl;

import com.farmtoyou.inventoryservice.dto.InventoryRequest;
import com.farmtoyou.inventoryservice.dto.InventoryResponse;
import com.farmtoyou.inventoryservice.entity.Inventory;
import com.farmtoyou.inventoryservice.repository.InventoryRepository;
import com.farmtoyou.inventoryservice.service.InventoryService;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException; // Import this
import java.time.LocalDateTime;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public InventoryResponse updateStock(InventoryRequest request) {
        // 1. Try to find existing inventory by productId
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                .orElse(new Inventory()); // If not found, create a new one

        // 2. Set/Update the details
        if (inventory.getId() == null) { // This is a new record
            inventory.setProductId(request.getProductId());
        }
        inventory.setQuantity(request.getQuantity());
        inventory.setLastUpdated(LocalDateTime.now());

        // 3. Save to database (JPA handles create vs. update)
        Inventory savedInventory = inventoryRepository.save(inventory);

        // 4. Map to response and return
        return mapToResponse(savedInventory);
    }

    @Override
    public InventoryResponse getStockByProductId(Long productId) {
        // 1. Find the inventory
        Inventory inventory = inventoryRepository.findByProductId(productId)
				.orElseThrow(() -> new EntityNotFoundException("No inventory found for productId: " + productId));
        
        // 2. Map to response and return
        return mapToResponse(inventory);
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        InventoryResponse response = new InventoryResponse();
        response.setId(inventory.getId());
        response.setProductId(inventory.getProductId());
        response.setQuantity(inventory.getQuantity());
        response.setLastUpdated(inventory.getLastUpdated());
        return response;
    }
}