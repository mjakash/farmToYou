package com.farmtoyou.orderservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long productId;

	@Column(nullable = false)
	private BigDecimal quantity;

	@Column(nullable = false)
	private BigDecimal priceAtTimeOfOrder; // Price when the order was placed

	// This links the item back to its order
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	@ToString.Exclude
	private Order order;
}