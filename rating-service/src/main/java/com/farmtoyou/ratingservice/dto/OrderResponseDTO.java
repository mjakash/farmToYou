package com.farmtoyou.ratingservice.dto;

import lombok.Data;

/**
 * This is a local DTO for the Rating Service. It represents the JSON data we
 * expect to get back from the order-service. We only include the fields we need
 * for validation.
 */
@Data
public class OrderResponseDTO {
	private Long id;
	private Long customerId;
	private Long farmerId;
	private String status; // e.g., "DELIVERED"
}