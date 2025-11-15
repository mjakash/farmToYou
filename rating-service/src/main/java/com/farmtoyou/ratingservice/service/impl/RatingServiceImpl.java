package com.farmtoyou.ratingservice.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.farmtoyou.ratingservice.dto.OrderResponseDTO;
import com.farmtoyou.ratingservice.dto.RatingRequest;
import com.farmtoyou.ratingservice.dto.RatingResponse;
import com.farmtoyou.ratingservice.entity.Rating;
import com.farmtoyou.ratingservice.repository.RatingRepository;
import com.farmtoyou.ratingservice.service.RatingService;

@Service
public class RatingServiceImpl implements RatingService {

	private static final Logger log = LoggerFactory.getLogger(RatingServiceImpl.class);
	private final RatingRepository ratingRepository;
	private final WebClient.Builder webClientBuilder;
	private final String orderServiceUrl;

	// This is the only constructor that should be in this file
	public RatingServiceImpl(RatingRepository ratingRepository, WebClient.Builder webClientBuilder,
			@Value("${order.service.url}") String orderServiceUrl) {
		this.ratingRepository = ratingRepository;
		this.webClientBuilder = webClientBuilder;
		this.orderServiceUrl = orderServiceUrl + "/api/orders";
	}

	@Override
	public RatingResponse createRating(RatingRequest request, Long userId, String userRole) {
		try {

			OrderResponseDTO order = webClientBuilder.build().get().uri(orderServiceUrl + "/" + request.getOrderId())
					.retrieve().bodyToMono(OrderResponseDTO.class).block();

			if (null == order) {
				throw new IllegalArgumentException("Order not found: " + request.getOrderId());
			}
			if (request.getScore() < 1 || request.getScore() > 5) {
				throw new IllegalArgumentException("Rating score must be between 1 and 5.");
			}

			// Authorization check
			if (!Objects.equals(request.getRatingSubmitterId(), userId)) {
				throw new AccessDeniedException("You can only submit ratings as yourself.");
			}

			if ("CUSTOMER".equals(userRole) && !Objects.equals(order.getCustomerId(), userId)) {
				throw new AccessDeniedException("You are not the customer for this order.");
			}

			// (We can add more checks here later by calling orderServiceUrl)

			Rating rating = new Rating();
			rating.setOrderId(request.getOrderId());
			rating.setRatingSubmitterId(request.getRatingSubmitterId());
			rating.setRatingSubjectId(request.getRatingSubjectId());
			rating.setRatingType(request.getRatingType());
			rating.setScore(request.getScore());
			rating.setComment(request.getComment());

			Rating savedRating = ratingRepository.save(rating);
			return mapToResponse(savedRating);
		} catch (Exception e) {
			log.error("Failed to validate order {} from order-service", request.getOrderId(), e);
			throw new RuntimeException("Could not validate order: " + e.getMessage());
		}
	}

	@Override
	public List<RatingResponse> getRatingsForUser(Long userId) {
		return ratingRepository.findByRatingSubjectId(userId).stream().map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	@Override
	public List<RatingResponse> getRatingsForOrder(Long orderId) {
		return ratingRepository.findByOrderId(orderId).stream().map(this::mapToResponse).collect(Collectors.toList());
	}

	private RatingResponse mapToResponse(Rating rating) {
		RatingResponse response = new RatingResponse();
		response.setId(rating.getId());
		response.setOrderId(rating.getOrderId());
		response.setRatingSubmitterId(rating.getRatingSubmitterId());
		response.setRatingSubjectId(rating.getRatingSubjectId());
		response.setRatingType(rating.getRatingType());
		response.setScore(rating.getScore());
		response.setComment(rating.getComment());
		response.setCreatedAt(rating.getCreatedAt());
		return response;
	}
}