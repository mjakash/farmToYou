package com.farmtoyou.ratingservice.service;

import com.farmtoyou.ratingservice.dto.RatingRequest;
import com.farmtoyou.ratingservice.dto.RatingResponse;
import java.util.List;

public interface RatingService {
    RatingResponse createRating(RatingRequest request);
    List<RatingResponse> getRatingsForUser(Long userId);
    List<RatingResponse> getRatingsForOrder(Long orderId);
}