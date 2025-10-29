package com.farmtoyou.orderservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import lombok.Data;

@Entity
@Table(name = "orders")
@Data
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long customerId;

	@Column(nullable = false)
	private Long farmerId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DeliveryChoice deliveryChoice;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentMethod paymentMethod;

	@Column(columnDefinition = "TIMESTAMP")
	private LocalDateTime acceptanceDeadline;

	@Column(nullable = false)
	private BigDecimal totalPrice;

	@Column(nullable = false)
	private BigDecimal totalWeight;

	@Column(columnDefinition = "TIMESTAMP")
	private LocalDateTime createdAt;

	@ElementCollection
	@CollectionTable(name = "order_image_urls", joinColumns = @JoinColumn(name = "order_id"))
	@Column(name = "image_url")
	private List<String> imageUrls;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
	private List<OrderItem> items;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		deliveryChoice = DeliveryChoice.PENDING;
	}

}
