package com.example.propertiesoffice.recommendation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private final Logger log = LoggerFactory.getLogger(RecommendationController.class);

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<PropertyRecommendationResponse>> getRecommendations(
            @PathVariable("userId") Long userId
    ) {
        log.info("getRecommendations for userId={}", userId);
        return ResponseEntity.ok(recommendationService.getRecommendations(userId, 12));
    }

    @GetMapping("/general")
    public ResponseEntity<List<PropertyRecommendationResponse>> getGeneralRecommendations() {
        log.info("getGeneralRecommendations");
        return ResponseEntity.ok(recommendationService.getGeneralRecommendations(12));
    }

    @PostMapping("/guest")
    public ResponseEntity<List<PropertyRecommendationResponse>> getGuestRecommendations(
            @RequestBody GuestRecommendationRequest request
    ) {
        log.info("getGuestRecommendations for propertyIds={}", request.propertyIds());
        return ResponseEntity.ok(recommendationService.getRecommendationsForGuest(request.propertyIds(), 12));
    }
}