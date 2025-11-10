package com.farmtoyou.deliveryservice.service.impl;

import com.farmtoyou.deliveryservice.dto.*;
import com.farmtoyou.deliveryservice.entity.Delivery;
import com.farmtoyou.deliveryservice.entity.DeliveryStatus;
import com.farmtoyou.deliveryservice.repository.DeliveryRepository;
import com.farmtoyou.deliveryservice.service.DeliveryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class DeliveryServiceImpl implements DeliveryService {

	private final DeliveryRepository deliveryRepository;
	private final WebClient.Builder webClientBuilder;
	private final String orderServiceUrl;
	private final String userServiceUrl;

	// --- THIS IS THE CORRECT, NEW CONSTRUCTOR ---
	public DeliveryServiceImpl(DeliveryRepository deliveryRepository, WebClient.Builder webClientBuilder,
			@Value("${order.service.url}") String orderServiceUrl,
			@Value("${user.service.url}") String userServiceUrl) {
		this.deliveryRepository = deliveryRepository;
		this.webClientBuilder = webClientBuilder;
		this.orderServiceUrl = orderServiceUrl + "/api/orders";
		this.userServiceUrl = userServiceUrl + "/api/users";
	}

	@Override
	@Transactional
	public DeliveryResponse createAssignment(DeliveryAssignmentRequest request, Long orderId) {
		try {

			Delivery delivery = deliveryRepository.findByOrderId(orderId).orElse(new Delivery());

			delivery.setOrderId(orderId);
			delivery.setDeliveryPersonId(request.getDeliveryPersonId());
			delivery.setStatus(DeliveryStatus.ASSIGNED);
			delivery.setAssignedAt(LocalDateTime.now());
			delivery.setDeliveredAt(null);

			Delivery savedDelivery = deliveryRepository.save(delivery);

			return mapToDeliveryResponse(savedDelivery);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	@Override
	@Transactional
	public DeliveryResponse completeDelivery(Long orderId, Long userId, String userRole) {
		try {

			Delivery delivery = findDeliveryByOrderIdOrThrow(orderId);

			if (!Objects.equals(delivery.getDeliveryPersonId(), userId)) {
				throw new AccessDeniedException("You are not authorized to complete this delivery.");
			}
			if (!"DELIVERY_AGENT".equals(userRole) && !"FARMER".equals(userRole)) {
				throw new AccessDeniedException("Only a DELIVERY_AGENT or FARMER can complete a delivery.");
			}

			if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
				throw new IllegalStateException("Delivery for order " + orderId + " has already been completed.");
			}
			delivery.setStatus(DeliveryStatus.DELIVERED);
			delivery.setDeliveredAt(LocalDateTime.now());
			Delivery savedDelivery = deliveryRepository.save(delivery);

			try {
				webClientBuilder.build().post().uri(orderServiceUrl + "/" + orderId + "/complete").retrieve()
						.bodyToMono(Void.class).block();
			} catch (Exception e) {
				throw new RuntimeException("Failed to notify order-service of completion: " + e.getMessage());
			}

			return mapToDeliveryResponse(savedDelivery);
		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}

	// --- REDIS-RELATED METHODS (updateLocation, getLocation) ARE REMOVED ---

	@Override
	public CustomerDeliveryView getCustomerView(Long orderId, Long customerId) {

		try {

			OrderResponse order = getOrderFromOrderService(orderId);
			UserResponse customer = getUserFromUserService(order.getCustomerId());

			if (!Objects.equals(customer.getId(), customerId)) {
				throw new AccessDeniedException("You are not authorized to view this order.");
			}

			Delivery delivery = findDeliveryByOrderIdOrThrow(orderId);
			UserResponse deliveryPerson = getUserFromUserService(delivery.getDeliveryPersonId());

			return CustomerDeliveryView.builder().orderId(orderId).status(delivery.getStatus())
					.deliveryPersonName(deliveryPerson.getName()).deliveryPersonRole(deliveryPerson.getRole())
					.deliveryPersonPhone(deliveryPerson.getPhone()).build();
		} catch (Exception e) {

			e.printStackTrace();
			return null;
		}

	}

	@Override
	public AgentDeliveryView getAgentView(Long orderId, Long agentId) {
		try {
			Delivery delivery = findDeliveryByOrderIdOrThrow(orderId);
			UserResponse agent = getUserFromUserService(delivery.getDeliveryPersonId());

			if (!Objects.equals(agent.getId(), agentId)) {
				throw new AccessDeniedException("You are not authorized to manage this delivery.");
			}

			OrderResponse order = getOrderFromOrderService(orderId);
			UserResponse customer = getUserFromUserService(order.getCustomerId());

			return AgentDeliveryView.builder().orderId(orderId).status(delivery.getStatus())
					.customerName(customer.getName()).customerPhone(customer.getPhone())
					.customerDeliveryAddress(order.getDeliveryAddress()).build();
		} catch (Exception e) {

			e.printStackTrace();
			return null;
		}
	}


	private OrderResponse getOrderFromOrderService(Long orderId) {
		return webClientBuilder.build().get().uri(orderServiceUrl + "/" + orderId).retrieve().onStatus(
				status -> status.value() == 404,
				clientResponse -> Mono
						.error(new EntityNotFoundException("Order data not found in Order Service for ID: " + orderId)))
				.onStatus(status -> status.isError(),
						clientResponse -> clientResponse.bodyToMono(String.class).flatMap(
								errorBody -> Mono.error(new RuntimeException("Order Service failed: " + errorBody))))
				.bodyToMono(OrderResponse.class).block();
	}

	private UserResponse getUserFromUserService(Long userId) {
		return webClientBuilder.build().get().uri(userServiceUrl + "/" + userId).retrieve()
				.onStatus(status -> status.value() == 404,
						clientResponse -> Mono.error(
								new EntityNotFoundException("User data not found in User Service for ID: " + userId)))
				.onStatus(status -> status.isError(),
						clientResponse -> clientResponse.bodyToMono(String.class).flatMap(
								errorBody -> Mono.error(new RuntimeException("User Service failed: " + errorBody))))
				.bodyToMono(UserResponse.class).block();
	}

	private Delivery findDeliveryByOrderIdOrThrow(Long orderId) {
		return deliveryRepository.findByOrderId(orderId)
				.orElseThrow(() -> new EntityNotFoundException("Delivery not found for orderId: " + orderId));
	}

	private DeliveryResponse mapToDeliveryResponse(Delivery delivery) {
		return DeliveryResponse.builder().id(delivery.getId()).orderId(delivery.getOrderId())
				.deliveryPersonId(delivery.getDeliveryPersonId()).status(delivery.getStatus())
				.assignedAt(delivery.getAssignedAt()).deliveredAt(delivery.getDeliveredAt()).build();
	}

	// This method was missing from the interface
	@Override
	public DeliveryResponse getDeliveryByOrderId(Long orderId) {
		Delivery delivery = findDeliveryByOrderIdOrThrow(orderId);
		return mapToDeliveryResponse(delivery);
	}
}