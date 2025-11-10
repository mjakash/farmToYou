package com.farmtoyou.deliveryservice.service;

import java.nio.file.AccessDeniedException;

import com.farmtoyou.deliveryservice.dto.AgentDeliveryView;
import com.farmtoyou.deliveryservice.dto.CustomerDeliveryView;
import com.farmtoyou.deliveryservice.dto.DeliveryAssignmentRequest;
import com.farmtoyou.deliveryservice.dto.DeliveryResponse;

public interface DeliveryService {
	DeliveryResponse createAssignment(DeliveryAssignmentRequest request, Long orderId);

	DeliveryResponse completeDelivery(Long orderId, Long userId, String userRole) throws AccessDeniedException;

	CustomerDeliveryView getCustomerView(Long orderId, Long customerId) throws AccessDeniedException;

	AgentDeliveryView getAgentView(Long orderId, Long agentId) throws AccessDeniedException;

	DeliveryResponse getDeliveryByOrderId(Long orderId);
}