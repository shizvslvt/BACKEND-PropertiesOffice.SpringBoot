package com.example.propertiesoffice.recommendation;

import java.util.List;

public record GuestRecommendationRequest(List<Long> propertyIds) {
}