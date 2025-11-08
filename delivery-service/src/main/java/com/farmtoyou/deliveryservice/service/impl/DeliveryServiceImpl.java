package com.farmtoyou.deliveryservice.service.impl;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.farmtoyou.deliveryservice.dto.AgentDeliveryView;
import com.farmtoyou.deliveryservice.dto.CustomerDeliveryView;
import com.farmtoyou.deliveryservice.dto.DeliveryAssignmentRequest;
import com.farmtoyou.deliveryservice.dto.DeliveryResponse;
import com.farmtoyou.deliveryservice.dto.OrderResponse;
import com.farmtoyou.deliveryservice.dto.UserResponse;
import com.farmtoyou.deliveryservice.entity.Delivery;
import com.farmtoyou.deliveryservice.entity.DeliveryStatus;
import com.farmtoyou.deliveryservice.repository.DeliveryRepository;
import com.farmtoyou.deliveryservice.service.DeliveryService;

import jakarta.persistence.EntityNotFoundException;
import reactor.core.publisher.Mono;

@Service
public class DeliveryServiceImpl implements DeliveryService {

	private final DeliveryRepository deliveryRepository;
	private final WebClient.Builder webClientBuilder;
	private final String orderServiceUrl;
	private final String userServiceUrl;

	private static final String LOCATION_KEY_PREFIX = "location:";

	public DeliveryServiceImpl(DeliveryRepository deliveryRepository, WebClient.Builder webClientBuilder,
			@Value("${order.service.url}") String orderServiceUrl,
			@Value("${user.service.url}") String userServiceUrl) {
		this.deliveryRepository = deliveryRepository;
		this.webClientBuilder = webClientBuilder;
		this.orderServiceUrl = orderServiceUrl + "/api/orders";
		this.userServiceUrl = userServiceUrl + "/api/users";

	}

	// --- PostgreSQL Logic ---

	@Override
	@Transactional
	public DeliveryResponse createOrUpdateAssignment(DeliveryAssignmentRequest request) {
		Delivery delivery = deliveryRepository.findByOrderId(request.getOrderId()).orElse(new Delivery());

		delivery.setOrderId(request.getOrderId());
		delivery.setDeliveryPersonId(request.getDeliveryPersonId());
		delivery.setStatus(DeliveryStatus.ASSIGNED);
		delivery.setAssignedAt(LocalDateTime.now());
		delivery.setDeliveredAt(null);

		Delivery savedDelivery = deliveryRepository.save(delivery);

		return mapToDeliveryResponse(savedDelivery);
	}

	@Override
	public DeliveryResponse getDeliveryByOrderId(Long orderId) {
		Delivery delivery = findDeliveryByOrderIdOrThrow(orderId);
		return mapToDeliveryResponse(delivery);
	}

	@Override
	@Transactional
	public DeliveryResponse completeDelivery(Long orderId, Long userId, String userRole) throws AccessDeniedException {
		// 1. Update local delivery status
		Delivery delivery = findDeliveryByOrderIdOrThrow(orderId);

		// 2. --- AUTHORIZATION CHECK ---

		if (!Objects.equals(delivery.getDeliveryPersonId(), userId)) {
			throw new AccessDeniedException("You are not authorized to complete this delivery.");
		}

		// 3. --- ROLE CHECK (Optional but good) ---

		if (!"DELIVERY_AGENT".equals(userRole) && !"FARMER".equals(userRole)) {
			throw new AccessDeniedException("Only a DELIVERY_AGENT or FARMER can complete a delivery.");
		}

		// 4. Update local delivery status

		if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
			throw new IllegalStateException("Delivery for order " + orderId + " has already been completed.");
		}

		if (delivery.getStatus() != DeliveryStatus.ASSIGNED && delivery.getStatus() != DeliveryStatus.EN_ROUTE) {
			throw new IllegalStateException("Delivery cannot be completed. Status is: " + delivery.getStatus());
		}

		delivery.setStatus(DeliveryStatus.DELIVERED);
		delivery.setDeliveredAt(LocalDateTime.now());
		Delivery savedDelivery = deliveryRepository.save(delivery);

		// 5. Call Order Service to notify it of completion
		try {
			webClientBuilder.build().post().uri(orderServiceUrl + "/" + orderId + "/complete").retrieve()
					.bodyToMono(Void.class).block();
		} catch (Exception e) {
			throw new RuntimeException("Failed to notify order-service of completion: " + e.getMessage());
		}

		return mapToDeliveryResponse(savedDelivery);
	}

	// --- UTILITY METHODS ---

	private Delivery findDeliveryByOrderIdOrThrow(Long orderId) {
		return deliveryRepository.findByOrderId(orderId)
				.orElseThrow(() -> new EntityNotFoundException("Delivery not found for orderId: " + orderId)); // <--
																												// This
																												// line
																												// is
																												// fixed
	}

	private DeliveryResponse mapToDeliveryResponse(Delivery delivery) {
		return DeliveryResponse.builder().id(delivery.getId()).orderId(delivery.getOrderId())
				.deliveryPersonId(delivery.getDeliveryPersonId()).status(delivery.getStatus())
				.assignedAt(delivery.getAssignedAt()).deliveredAt(delivery.getDeliveredAt()).build();
	}

	@Override
	public CustomerDeliveryView getCustomerView(Long orderId, String customerEmail) throws AccessDeniedException {
		// 1. Get Order details
		OrderResponse order = getOrderFromOrderService(orderId);

		// 2. Get Customer details
		UserResponse customer = getUserFromUserService(order.getCustomerId());

		// 3. Check Authorization
		if (!customer.getEmail().equals(customerEmail)) {
			throw new AccessDeniedException("You are not authorized to view this order.");
		}

		// 4. Get local Delivery details
		Delivery delivery = findDeliveryByOrderIdOrThrow(orderId);

		// 5. Get Delivery Person details
		UserResponse deliveryPerson = getUserFromUserService(delivery.getDeliveryPersonId());

		// 6. Build and return the view
		return CustomerDeliveryView.builder().orderId(orderId).status(delivery.getStatus())
				.deliveryPersonName(deliveryPerson.getName()).deliveryPersonPhone(deliveryPerson.getPhone()).build();
	}

	@Override
	public AgentDeliveryView getAgentView(Long orderId, String agentEmail) throws AccessDeniedException {
		// 1. Get local Delivery details
		Delivery delivery = findDeliveryByOrderIdOrThrow(orderId);

		// 2. Get Agent details
		UserResponse agent = getUserFromUserService(delivery.getDeliveryPersonId());

		// 3. Check Authorization
		if (!agent.getEmail().equals(agentEmail)) {
			throw new AccessDeniedException("You are not authorized to manage this delivery.");
		}

		// 4. Get Order details
		OrderResponse order = getOrderFromOrderService(orderId);

		// 5. Get Customer details
		UserResponse customer = getUserFromUserService(order.getCustomerId());

		// 6. Build and return the view
		return AgentDeliveryView.builder().orderId(orderId).status(delivery.getStatus())
				.customerName(customer.getName()).customerPhone(customer.getPhone())
				.customerDeliveryAddress(order.getDeliveryAddress()).build();
	}

	private OrderResponse getOrderFromOrderService(Long orderId) {
		return webClientBuilder
				.build()
				.get()
				.uri(orderServiceUrl + "/" + orderId)
				.retrieve()
				.onStatus(
						status -> status.value() == 404,
						clientResponse -> Mono
								.error(new EntityNotFoundException("Order data not found in Order Service for ID: " + orderId)))
				.onStatus(
						status -> status.isError(),
						clientResponse -> clientResponse.bodyToMono(String.class).flatMap(
								errorBody -> Mono.error(new RuntimeException("Order Service failed: " + errorBody))))
				.bodyToMono(OrderResponse.class)
				.block();
	}

	private UserResponse getUserFromUserService(Long userId) {
		return webClientBuilder
				.build()
				.get()
				.uri(userServiceUrl + "/" + userId)
				.retrieve()
				.onStatus(
						status -> status.value() == 404,
		                clientResponse -> Mono.error(new EntityNotFoundException("User data not found in User Service for ID: " + userId)))
		        .onStatus(
		        		status -> status.isError(),
		                clientResponse -> clientResponse.bodyToMono(String.class)
		                        .flatMap(errorBody -> Mono.error(new RuntimeException("User Service failed: " + errorBody))))
				.bodyToMono(UserResponse.class) 
				.block(); 
	}

}