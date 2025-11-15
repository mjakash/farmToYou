package com.farmtoyou.orderservice.service.impl;

import com.farmtoyou.orderservice.dto.*;
import com.farmtoyou.orderservice.entity.Order;
import com.farmtoyou.orderservice.entity.OrderItem;
import com.farmtoyou.orderservice.entity.OrderStatus; // Import this
import com.farmtoyou.orderservice.entity.PaymentMethod;
import com.farmtoyou.orderservice.exception.OrderValidationException;
import com.farmtoyou.orderservice.repository.OrderRepository;
import com.farmtoyou.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.scheduling.annotation.Scheduled; // Import this
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime; // Import this
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		// --- 1. VALIDATE AND CALCULATE ORDER ---
		for (OrderItemRequest itemReq : orderRequest.getItems()) {

			ProductResponse product = webClientBuilder.build().get()
					.uri(productServiceUrl + "/" + itemReq.getProductId()).retrieve().bodyToMono(ProductResponse.class)
					.block();

			if (product == null) {
				throw new OrderValidationException("Product not found: " + itemReq.getProductId());
			}

			InventoryResponse inventory = webClientBuilder.build().get()
					.uri(inventoryServiceUrl + "/" + itemReq.getProductId()).retrieve()
					.bodyToMono(InventoryResponse.class).block();

			if (inventory == null || inventory.getQuantity().compareTo(itemReq.getQuantity()) < 0) {
				throw new OrderValidationException("Not enough stock for product: " + product.getName());
			}

			totalPrice = totalPrice.add(product.getPrice().multiply(itemReq.getQuantity()));

			if ("PER_KG".equals(product.getUnit())) {
				totalWeight = totalWeight.add(itemReq.getQuantity());
			} else if ("PER_PIECE".equals(product.getUnit())) {
				totalWeight = totalWeight.add(BigDecimal.ONE.multiply(itemReq.getQuantity()));
			}

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

		// --- 3. PROCESS PAYMENT (Updated Logic) ---
		if (orderRequest.getPaymentMethod() == PaymentMethod.PREPAID) {
			PaymentRequest paymentRequest = PaymentRequest.builder().orderId(0L) // Mock ID
					.amount(totalPrice).paymentMethod("MOCK_PAYMENT").build();

			PaymentResponse paymentResponse = webClientBuilder.build().post().uri(paymentServiceUrl + "/process")
					.body(Mono.just(paymentRequest), PaymentRequest.class).retrieve().bodyToMono(PaymentResponse.class)
					.block();

			if (null == paymentResponse || !"APPROVED".equals(paymentResponse.getStatus())) {
				throw new OrderValidationException("Payment failed or was declined.");
			}

			newOrder.setStatus(OrderStatus.PENDING_FARMER_ACCEPTANCE);

		} else if (orderRequest.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
			newOrder.setStatus(OrderStatus.PENDING_FARMER_ACCEPTANCE);
		}

		// Set the 2-hour acceptance window
		newOrder.setAcceptanceDeadline(LocalDateTime.now().plusHours(2));

		// --- 4. SAVE ORDER ---
		Order savedOrder = orderRepository.save(newOrder);

		// --- 5. RETURN RESPONSE ---
		OrderResponse response = mapToOrderResponse(savedOrder);
		response.setMessage("Order placed successfully! Waiting for farmer acceptance.");

		return response;
	}

	@Override
	public OrderResponse getOrderById(Long orderId) {
		Order order = findOrderByIdOrThrow(orderId);
		return mapToOrderResponse(order);
	}

	// --- NEW METHOD: ACCEPT ORDER ---
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

	// --- NEW METHOD: REJECT ORDER ---
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
			// TODO: Trigger a refund process for the customer
			// This could be publishing an event to a Kafka topic or calling a Refund
			// service.
//			System.out.println("LOG: Initiating refund for PREPAID order: " + orderId);
			log.warn("CRITICAL: Refund required for PREPAID order: {}. Amount: {}", order.getId(),
					order.getTotalPrice());
		}

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

		// Logic: Must be packaged to be dispatched
		if (order.getStatus() != OrderStatus.PACKAGED) {
			throw new OrderValidationException("Order must be PACKAGED to be dispatched.");
		}

		// Call Delivery Service to create the assignment
		DeliveryAssignmentRequest assignmentRequest = DeliveryAssignmentRequest.builder()
				.deliveryPersonId(dispatchRequest.getDeliveryPersonId()).build();

		try {
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
			throw new OrderValidationException(
					"Order must be in FARMER_CONFIRMED status to be packaged. Current status: " + order.getStatus());
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
				// TODO: Trigger a refund
				log.warn("CRITICAL: Order {} EXPIRED. Refund required. Amount: {}", order.getId(),
						order.getTotalPrice());
//				System.out.println("LOG: Order " + order.getId() + " expired. Initiating refund.");
			} else {
				log.info("Order {} expired (COD). No refund needed.", order.getId());
//				System.out.println("LOG: Order " + order.getId() + " expired.");
			}
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

//		if (order.getStatus() != OrderStatus.PACKAGED) {
//			throw new OrderValidationException(
//					"Order must be PACKAGED to be completed. Current status: " + order.getStatus());
//		}

		order.setStatus(OrderStatus.DELIVERED);
		Order savedOrder = orderRepository.save(order);

		// For COD, this is where we would also trigger a notification
		// to the farmer that payment has been collected and will be settled.
		if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
			System.out.println("LOG: Payment collected for COD order: " + orderId);
			// In a real system, this would trigger a financial settlement event.
		}

		OrderResponse response = mapToOrderResponse(savedOrder);
		response.setMessage("Order marked as DELIVERED.");
		return response;
	}

}