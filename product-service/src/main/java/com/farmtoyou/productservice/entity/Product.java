package com.farmtoyou.productservice.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="products")
@Data
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String name;
	
	private String description;
	
	@Column(nullable = false)
	private BigDecimal price;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PriceUnit unit;
	
	@Column(nullable = false)
	private Long farmerId;
	
	@Column(nullable = false)
    private String imageUrl;
	
}
