package com.example.propertiesoffice.property;

public record PropertyRequest(
        String title,
        String description,
        String imgUrl,
        Integer price,
        Double area,
        String location,
        Double realtorRating
) {
}