package com.farmtoyou.productservice.dto;

import java.math.BigDecimal;

import com.farmtoyou.productservice.entity.PriceUnit;

import lombok.Data;

@Data
public class ProductRequest {
	private String name;
	private String description;
	private BigDecimal price;
	private PriceUnit unit;
	private String imageUrl;
	private Long farmerId;
}
