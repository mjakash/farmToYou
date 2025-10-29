package com.farmtoyou.ratingservice.dto;

import com.farmtoyou.ratingservice.entity.RatingType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RatingResponse {
    private Long id;
    private Long orderId;
    private Long ratingSubmitterId;
    private Long ratingSubjectId;
    private RatingType ratingType;
    private int score;
    private String comment;
    private LocalDateTime createdAt;
}