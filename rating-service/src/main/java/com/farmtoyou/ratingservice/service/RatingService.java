package com.farmtoyou.ratingservice.service;

import com.farmtoyou.ratingservice.dto.RatingRequest;
import com.farmtoyou.ratingservice.dto.RatingResponse;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface RatingService {

	List<RatingResponse> getRatingsForUser(Long userId);

	List<RatingResponse> getRatingsForOrder(Long orderId);

	RatingResponse createRating(RatingRequest request, Long userId, String userRole) throws AccessDeniedException;
}