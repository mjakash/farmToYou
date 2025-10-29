package com.farmtoyou.orderservice.service.impl;

import com.farmtoyou.orderservice.dto.*;
import com.farmtoyou.orderservice.entity.Order;
import com.farmtoyou.orderservice.entity.OrderItem;
import com.farmtoyou.orderservice.exception.OrderValidationException;
import com.farmtoyou.orderservice.repository.OrderRepository;
import com.farmtoyou.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    
    // Injecting the URLs from application.properties
    private final String productServiceUrl;
    private final String inventoryServiceUrl;
    private final String paymentServiceUrl;

    public OrderServiceImpl(OrderRepository orderRepository,
                            WebClient.Builder webClientBuilder,
                            @Value("${product.service.url}") String productServiceUrl,
                            @Value("${inventory.service.url}") String inventoryServiceUrl,
                            @Value("${payment.service.url}") String paymentServiceUrl) {
        this.orderRepository = orderRepository;
        this.webClientBuilder = webClientBuilder;
        this.productServiceUrl = productServiceUrl + "/api/products";
        this.inventoryServiceUrl = inventoryServiceUrl + "/api/inventory";
        this.paymentServiceUrl = paymentServiceUrl + "/api/payments";
    }

    @Override
    @Transactional // This ensures that if any part fails, the database save is rolled back
    public OrderResponse createOrder(OrderRequest orderRequest) {
        
        Order newOrder = new Order();
        newOrder.setCustomerId(orderRequest.getCustomerId());
        newOrder.setFarmerId(orderRequest.getFarmerId());

        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // --- 1. VALIDATE AND CALCULATE ORDER ---
        for (OrderItemRequest itemReq : orderRequest.getItems()) {
            
            // --- CALL PRODUCT SERVICE ---
            // We use .block() to make the web call synchronous. 
            // In a real high-load app, we'd use reactive chains.
            ProductResponse product = webClientBuilder.build()
                    .get()
                    .uri(productServiceUrl + "/" + itemReq.getProductId()) // This endpoint doesn't exist yet!
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .block(); // <-- We'll need to create this endpoint in Product-Service

            if (product == null) {
                throw new OrderValidationException("Product not found: " + itemReq.getProductId());
            }

            // --- CALL INVENTORY SERVICE ---
            InventoryResponse inventory = webClientBuilder.build()
                    .get()
                    .uri(inventoryServiceUrl + "/" + itemReq.getProductId())
                    .retrieve()
                    .bodyToMono(InventoryResponse.class)
                    .block();

            if (inventory == null || inventory.getQuantity().compareTo(itemReq.getQuantity()) < 0) {
                throw new OrderValidationException("Not enough stock for product: " + product.getName());
            }

            // --- Calculate totals ---
            totalPrice = totalPrice.add(product.getPrice().multiply(itemReq.getQuantity()));
            
            // Assume PER_KG means quantity is weight. PER_PIECE has a default weight of 1kg.
            if ("PER_KG".equals(product.getUnit())) {
                totalWeight = totalWeight.add(itemReq.getQuantity());
            } else if ("PER_PIECE".equals(product.getUnit())) {
                totalWeight = totalWeight.add(BigDecimal.ONE.multiply(itemReq.getQuantity()));
            }

            // Build the OrderItem entity
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

        // --- 3. PROCESS PAYMENT ---
        // (We set a temporary ID for the request; the real one is set on save)
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(0L) // Mock ID
                .amount(totalPrice)
                .paymentMethod("MOCK_PAYMENT")
                .build();
        
        PaymentResponse paymentResponse = webClientBuilder.build()
                .post()
                .uri(paymentServiceUrl + "/process")
                .body(Mono.just(paymentRequest), PaymentRequest.class)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .block();

        if (paymentResponse == null || !"APPROVED".equals(paymentResponse.getStatus())) {
            throw new OrderValidationException("Payment failed or was declined.");
        }

        // --- 4. SAVE ORDER ---
        Order savedOrder = orderRepository.save(newOrder);

       
        
        // --- 6. RETURN RESPONSE ---
        OrderResponse response = new OrderResponse();
        response.setId(savedOrder.getId());
        response.setCustomerId(savedOrder.getCustomerId());
        response.setStatus(savedOrder.getStatus());
        response.setTotalPrice(savedOrder.getTotalPrice());
        response.setTotalWeight(savedOrder.getTotalWeight());
        response.setCreatedAt(savedOrder.getCreatedAt());
        response.setMessage("Order placed successfully!");
        
        return response;
    }
}