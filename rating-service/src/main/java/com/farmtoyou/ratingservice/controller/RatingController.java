package com.farmtoyou.ratingservice.controller;

import com.farmtoyou.ratingservice.dto.RatingRequest;
import com.farmtoyou.ratingservice.dto.RatingResponse;
import com.farmtoyou.ratingservice.service.RatingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<RatingResponse> createRating(@RequestBody RatingRequest request) {
        RatingResponse response = ratingService.createRating(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RatingResponse>> getRatingsForUser(@PathVariable Long userId) {
        List<RatingResponse> responses = ratingService.getRatingsForUser(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<RatingResponse>> getRatingsForOrder(@PathVariable Long orderId) {
        List<RatingResponse> responses = ratingService.getRatingsForOrder(orderId);
        return ResponseEntity.ok(responses);
    }
}