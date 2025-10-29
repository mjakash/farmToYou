package com.farmtoyou.ratingservice.repository;

import com.farmtoyou.ratingservice.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    // Finds all ratings given to a specific user (e.g., all ratings for a farmer)
    List<Rating> findByRatingSubjectId(Long subjectId);

    // Finds all ratings associated with a single order
    List<Rating> findByOrderId(Long orderId);
}