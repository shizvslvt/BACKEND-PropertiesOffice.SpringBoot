package com.example.propertiesoffice.property;

import java.time.LocalDateTime;

public record Property(
        Long id,
        String title,
        String description,
        String imgUrl,
        Integer price,
        Double area,
        String location,
        Double realtorRating,
        PropertyStatus status,
        LocalDateTime createdAt
) {
}