package com.farmtoyou.deliveryservice.service;

import java.nio.file.AccessDeniedException;

import com.farmtoyou.deliveryservice.dto.*;

public interface DeliveryService {
	DeliveryResponse createOrUpdateAssignment(DeliveryAssignmentRequest request);

	DeliveryResponse getDeliveryByOrderId(Long orderId);

	CustomerDeliveryView getCustomerView(Long orderId, String customerEmail) throws AccessDeniedException;

	AgentDeliveryView getAgentView(Long orderId, String agentEmail) throws AccessDeniedException;

	DeliveryResponse completeDelivery(Long orderId, Long userId, String userRole) throws AccessDeniedException;
}