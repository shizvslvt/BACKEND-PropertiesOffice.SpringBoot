package com.example.propertiesoffice.recommendation;

import com.example.propertiesoffice.property.PropertyStatus;

import java.time.LocalDateTime;

public record PropertyRecommendationResponse(
        Long id,
        String title,
        String description,
        String imgUrl,
        Integer price,
        Double area,
        String location,
        Double realtorRating,
        PropertyStatus status,
        LocalDateTime createdAt,
        Double matchScore
) {
}