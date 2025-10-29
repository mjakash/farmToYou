package com.farmtoyou.ratingservice.dto;

import com.farmtoyou.ratingservice.entity.RatingType;
import lombok.Data;

@Data
public class RatingRequest {
    private Long orderId;
    private Long ratingSubmitterId;
    private Long ratingSubjectId;
    private RatingType ratingType;
    private int score;
    private String comment;
}