package com.farmtoyou.ratingservice.service.impl;

import com.farmtoyou.ratingservice.dto.RatingRequest;
import com.farmtoyou.ratingservice.dto.RatingResponse;
import com.farmtoyou.ratingservice.entity.Rating;
import com.farmtoyou.ratingservice.repository.RatingRepository;
import com.farmtoyou.ratingservice.service.RatingService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;

    public RatingServiceImpl(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @Override
    public RatingResponse createRating(RatingRequest request) {
        if (request.getScore() < 1 || request.getScore() > 5) {
            throw new IllegalArgumentException("Rating score must be between 1 and 5.");
        }

        Rating rating = new Rating();
        rating.setOrderId(request.getOrderId());
        rating.setRatingSubmitterId(request.getRatingSubmitterId());
        rating.setRatingSubjectId(request.getRatingSubjectId());
        rating.setRatingType(request.getRatingType());
        rating.setScore(request.getScore());
        rating.setComment(request.getComment());

        Rating savedRating = ratingRepository.save(rating);
        return mapToResponse(savedRating);
    }

    @Override
    public List<RatingResponse> getRatingsForUser(Long userId) {
        return ratingRepository.findByRatingSubjectId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<RatingResponse> getRatingsForOrder(Long orderId) {
        return ratingRepository.findByOrderId(orderId).stream()
                .map(this::mapToResponse)
                .toList();
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