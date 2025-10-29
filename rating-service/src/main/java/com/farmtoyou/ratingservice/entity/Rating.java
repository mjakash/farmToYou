package com.farmtoyou.ratingservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
@Data
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long ratingSubmitterId;

    @Column(nullable = false)
    private Long ratingSubjectId; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RatingType ratingType;

    @Column(nullable = false)
    private int score; // e.g., 1 to 5

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}