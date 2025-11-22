package com.farmtoyou.orderservice.service.impl;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.farmtoyou.orderservice.dto.DeliveryAssignmentRequest;
import com.farmtoyou.orderservice.dto.DispatchRequest;
import com.farmtoyou.orderservice.dto.InventoryRequest;
import com.farmtoyou.orderservice.dto.InventoryResponse;
import com.farmtoyou.orderservice.dto.OrderItemRequest;
import com.farmtoyou.orderservice.dto.OrderRequest;
import com.farmtoyou.orderservice.dto.OrderResponse;
import com.farmtoyou.orderservice.dto.PackageOrderRequest;
import com.farmtoyou.orderservice.dto.PaymentRequest;
import com.farmtoyou.orderservice.dto.PaymentResponse;
import com.farmtoyou.orderservice.dto.ProductResponse;
import com.farmtoyou.orderservice.entity.Order;
import com.farmtoyou.orderservice.entity.OrderItem;
import com.farmtoyou.orderservice.entity.OrderStatus;
import com.farmtoyou.orderservice.entity.PaymentMethod;
import com.farmtoyou.orderservice.exception.OrderValidationException;
import com.farmtoyou.orderservice.repository.OrderRepository;
import com.farmtoyou.orderservice.service.OrderService;

import reactor.core.publisher.Mono;

@Service
public class OrderServiceImpl implements OrderService {

	private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

	private final OrderRepository orderRepository;
	private final WebClient.Builder webClientBuilder;

	private final String productServiceUrl;
	private final String inventoryServiceUrl;
	private final String paymentServiceUrl;
	private final String deliveryServiceUrl;

	public OrderServiceImpl(OrderRepository orderRepository, WebClient.Builder webClientBuilder,
			@Value("${product.service.url}") String productServiceUrl,
			@Value("${inventory.service.url}") String inventoryServiceUrl,
			@Value("${payment.service.url}") String paymentServiceUrl,
			@Value("${delivery.service.url}") String deliveryServiceUrl) {
		this.orderRepository = orderRepository;
		this.webClientBuilder = webClientBuilder;
		this.productServiceUrl = productServiceUrl + "/api/products";
		this.inventoryServiceUrl = inventoryServiceUrl + "/api/inventory";
		this.paymentServiceUrl = paymentServiceUrl + "/api/payments";
		this.deliveryServiceUrl = deliveryServiceUrl + "/api/delivery";
	}

	@Override
	@Transactional
	public OrderResponse createOrder(OrderRequest orderRequest) {

		Order newOrder = new Order();
		newOrder.setCustomerId(orderRequest.getCustomerId());
		newOrder.setFarmerId(orderRequest.getFarmerId());
		newOrder.setPaymentMethod(orderRequest.getPaymentMethod());
		newOrder.setDeliveryAddress(orderRequest.getDeliveryAddress());

		BigDecimal totalPrice = BigDecimal.ZERO;
		BigDecimal totalWeight = BigDecimal.ZERO;
		List<OrderItem> orderItems = new ArrayList<>();

		// --- 1. VALIDATE PRODUCT & RESERVE INVENTORY ---
		for (OrderItemRequest itemReq : orderRequest.getItems()) {

			// A. Validate Product
			ProductResponse product = webClientBuilder.build().get()
					.uri(productServiceUrl + "/" + itemReq.getProductId()).retrieve().bodyToMono(ProductResponse.class)
					.block();

			if (product == null) {
				throw new OrderValidationException("Product not found: " + itemReq.getProductId());
			}

			// B. Reserve/Reduce Inventory (CRITICAL FIX)
			try {
				InventoryRequest reduceRequest = new InventoryRequest();
				reduceRequest.setProductId(itemReq.getProductId());
				reduceRequest.setQuantity(itemReq.getQuantity());

				// Call the new /reduce endpoint in Inventory Service
				webClientBuilder.build().post().uri(inventoryServiceUrl + "/reduce")
						.body(Mono.just(reduceRequest), InventoryRequest.class).retrieve()
						.bodyToMono(InventoryResponse.class).block();
			} catch (Exception e) {
				log.error("Inventory reduction failed for product {}", product.getId(), e);
				throw new OrderValidationException(
						"Insufficient stock or inventory error for product: " + product.getName());
			}

			// C. Calculate Totals
			totalPrice = totalPrice.add(product.getPrice().multiply(itemReq.getQuantity()));

			if ("PER_KG".equals(product.getUnit())) {
				totalWeight = totalWeight.add(itemReq.getQuantity());
			} else if ("PER_PIECE".equals(product.getUnit())) {
				totalWeight = totalWeight.add(BigDecimal.ONE.multiply(itemReq.getQuantity()));
			}

			// D. Build Order Item
			OrderItem orderItem = new OrderItem();
			orderItem.setProductId(itemReq.getProductId());
			orderItem.setQuantity(itemReq.getQuantity());
			orderItem.setPriceAtTimeOfOrder(product.getPrice());
			orderItem.setOrder(newOrder);
			orderItems.add(orderItem);
		}

		newOrder.setItems(orderItems);
		newOrder.setTotalPrice(totalPrice);
		newOrder.setTotalWeight(totalWeight);

		// --- 2. CHECK BUSINESS RULES ---
		if (totalWeight.compareTo(new BigDecimal("5")) < 0 && totalPrice.compareTo(new BigDecimal("1000")) < 0) {
			throw new OrderValidationException("Order failed: Must be at least 5kg or Rs 1000.");
		}

		// --- 3. SAVE INITIAL ORDER (Fix for Ghost Orders) ---
		newOrder.setStatus(OrderStatus.PENDING_PAYMENT);
		Order savedOrder = orderRepository.save(newOrder);
		log.info("Order {} created with status PENDING_PAYMENT", savedOrder.getId());

		// --- 4. PROCESS PAYMENT ---
		if (orderRequest.getPaymentMethod() == PaymentMethod.PREPAID) {
			try {
				PaymentRequest paymentRequest = PaymentRequest.builder().orderId(savedOrder.getId()) // Use real ID
						.amount(totalPrice).paymentMethod("MOCK_PAYMENT").build();

				PaymentResponse paymentResponse = webClientBuilder.build().post().uri(paymentServiceUrl + "/process")
						.body(Mono.just(paymentRequest), PaymentRequest.class).retrieve()
						.bodyToMono(PaymentResponse.class).block();

				if (paymentResponse != null && "APPROVED".equals(paymentResponse.getStatus().toString())) {
					savedOrder.setStatus(OrderStatus.PENDING_FARMER_ACCEPTANCE);
				} else {
					// Payment declined
					savedOrder.setStatus(OrderStatus.CANCELLED);
					orderRepository.save(savedOrder);
					throw new OrderValidationException("Payment declined.");
				}
			} catch (Exception e) {
				// Network error or other issue during payment
				log.error("Payment processing error for Order {}", savedOrder.getId(), e);
				savedOrder.setStatus(OrderStatus.CANCELLED);
				orderRepository.save(savedOrder);
				// In a real system, you might trigger an inventory compensation (rollback)
				// transaction here
				throw new OrderValidationException("Payment processing failed: " + e.getMessage());
			}

		} else if (orderRequest.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
			savedOrder.setStatus(OrderStatus.PENDING_FARMER_ACCEPTANCE);
		}

		// Set the 2-hour acceptance window
		savedOrder.setAcceptanceDeadline(LocalDateTime.now().plusHours(2));

		// --- 5. SAVE FINAL STATUS ---
		Order finalOrder = orderRepository.save(savedOrder);

		// --- 6. RETURN RESPONSE ---
		OrderResponse response = mapToOrderResponse(finalOrder);
		response.setMessage("Order placed successfully! Waiting for farmer acceptance.");

		return response;
	}

	@Override
	public OrderResponse getOrderById(Long orderId) {
		Order order = findOrderByIdOrThrow(orderId);
		return mapToOrderResponse(order);
	}

	@Override
	@Transactional
	public OrderResponse acceptOrder(Long orderId) {
		Order order = findOrderByIdOrThrow(orderId);

		if (order.getStatus() != OrderStatus.PENDING_FARMER_ACCEPTANCE) {
			throw new OrderValidationException("Order cannot be accepted. Current status: " + order.getStatus());
		}

		order.setStatus(OrderStatus.FARMER_CONFIRMED);
		Order savedOrder = orderRepository.save(order);

		OrderResponse response = mapToOrderResponse(savedOrder);
		response.setMessage("Order accepted by farmer.");
		return response;
	}

	@Override
	@Transactional
	public OrderResponse rejectOrder(Long orderId) {
		Order order = findOrderByIdOrThrow(orderId);

		if (order.getStatus() != OrderStatus.PENDING_FARMER_ACCEPTANCE) {
			throw new OrderValidationException("Order cannot be rejected. Current status: " + order.getStatus());
		}

		order.setStatus(OrderStatus.FARMER_REJECTED);
		Order savedOrder = orderRepository.save(order);

		if (order.getPaymentMethod() == PaymentMethod.PREPAID) {
			// TODO: Call Payment Service to initiate Refund
			log.warn("CRITICAL: Refund required for PREPAID order: {}. Amount: {}", order.getId(),
					order.getTotalPrice());
		}

		// TODO: Call Inventory Service to compensate (add back) stock

		OrderResponse response = mapToOrderResponse(savedOrder);
		response.setMessage("Order rejected by farmer.");
		return response;
	}

	@Override
	@Transactional
	public OrderResponse dispatchOrder(Long orderId, DispatchRequest dispatchRequest, Long farmerId)
			throws AccessDeniedException {
		Order order = findOrderByIdOrThrow(orderId);

		// Authorization: Only the farmer of this order can dispatch it
		if (!Objects.equals(order.getFarmerId(), farmerId)) {
			throw new AccessDeniedException("You are not authorized to dispatch this order.");
		}

		if (order.getStatus() != OrderStatus.PACKAGED) {
			throw new OrderValidationException("Order must be PACKAGED to be dispatched.");
		}

		// Call Delivery Service to create the assignment
		try {
			DeliveryAssignmentRequest assignmentRequest = DeliveryAssignmentRequest.builder()
					.deliveryPersonId(dispatchRequest.getDeliveryPersonId()).build();

			webClientBuilder.build().post().uri(deliveryServiceUrl + "/assign/" + orderId)
					.body(Mono.just(assignmentRequest), DeliveryAssignmentRequest.class).retrieve()
					.bodyToMono(Void.class).block();
		} catch (Exception e) {
			throw new RuntimeException("Failed to assign order to delivery service: " + e.getMessage());
		}

		order.setDeliveryChoice(dispatchRequest.getDeliveryChoice());
		order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
		Order savedOrder = orderRepository.save(order);

		OrderResponse response = mapToOrderResponse(savedOrder);
		response.setMessage("Order dispatched and assigned for delivery.");
		return response;
	}

	@Override
	@Transactional
	public OrderResponse packageOrder(Long orderId, PackageOrderRequest packageRequest) {
		Order order = findOrderByIdOrThrow(orderId);

		if (order.getStatus() != OrderStatus.FARMER_CONFIRMED) {
			throw new OrderValidationException("Order must be in FARMER_CONFIRMED status to be packaged.");
		}

		if (packageRequest.getImageUrls() == null || packageRequest.getImageUrls().isEmpty()) {
			throw new OrderValidationException("At least one image URL is required to package the order.");
		}

		order.setImageUrls(packageRequest.getImageUrls());
		order.setStatus(OrderStatus.PACKAGED);
		Order savedOrder = orderRepository.save(order);

		OrderResponse response = mapToOrderResponse(savedOrder);
		response.setMessage("Order packaged successfully with " + savedOrder.getImageUrls().size() + " photo(s).");
		return response;
	}

	@Scheduled(fixedRate = 60000)
	@Transactional
	public void expirePendingOrders() {
		List<Order> expiredOrders = orderRepository
				.findByStatusAndAcceptanceDeadlineBefore(OrderStatus.PENDING_FARMER_ACCEPTANCE, LocalDateTime.now());

		for (Order order : expiredOrders) {
			order.setStatus(OrderStatus.FARMER_REJECTED);
			orderRepository.save(order);

			if (order.getPaymentMethod() == PaymentMethod.PREPAID) {
				log.warn("CRITICAL: Order {} EXPIRED. Refund required. Amount: {}", order.getId(),
						order.getTotalPrice());
			} else {
				log.info("Order {} expired (COD). No refund needed.", order.getId());
			}
			// TODO: Trigger inventory restock (compensation) here as well
		}
	}

	private Order findOrderByIdOrThrow(Long orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderValidationException("Order not found with id: " + orderId));
	}

	private OrderResponse mapToOrderResponse(Order order) {
		OrderResponse response = new OrderResponse();
		response.setId(order.getId());
		response.setCustomerId(order.getCustomerId());
		response.setFarmerId(order.getFarmerId());
		response.setStatus(order.getStatus());
		response.setTotalPrice(order.getTotalPrice());
		response.setTotalWeight(order.getTotalWeight());
		response.setCreatedAt(order.getCreatedAt());
		response.setDeliveryAddress(order.getDeliveryAddress());
		return response;
	}

	@Override
	@Transactional
	public OrderResponse completeOrder(Long orderId) throws AccessDeniedException {
		Order order = findOrderByIdOrThrow(orderId);

		if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
			throw new OrderValidationException("Order must be OUT_FOR_DELIVERY to be completed.");
		}

		order.setStatus(OrderStatus.DELIVERED);
		Order savedOrder = orderRepository.save(order);

		if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
			log.info("Payment collected for COD order: {}", orderId);
			// In a real system, trigger financial settlement event here
		}

		OrderResponse response = mapToOrderResponse(savedOrder);
		response.setMessage("Order marked as DELIVERED.");
		return response;
	}
}