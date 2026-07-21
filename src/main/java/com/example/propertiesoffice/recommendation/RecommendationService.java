package com.example.propertiesoffice.recommendation;

import com.example.propertiesoffice.property.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private static final double WEIGHT_PRICE = 0.3;
    private static final double WEIGHT_AREA = 0.2;
    private static final double WEIGHT_RATING = 0.2;
    private static final double WEIGHT_LOCATION = 0.3;
    private static final double NEUTRAL_LOCATION_AFFINITY = 0.5;

    private static final int RECENT_VIEWS_LIMIT = 20;
    private static final double SMOOTHING_ALPHA = 1.0;

    private static final int TREND_WINDOW = 10;
    private static final int MIN_TREND_SAMPLES = 4;
    private static final double TREND_BOOST_FACTOR = 1.5;

    private final PropertyRepository propertyRepository;
    private final PropertyViewRepository propertyViewRepository;

    public RecommendationService(PropertyRepository propertyRepository,
                                 PropertyViewRepository propertyViewRepository) {
        this.propertyRepository = propertyRepository;
        this.propertyViewRepository = propertyViewRepository;
    }

    // Существующий способ - по авторизованному пользователю (история из БД)
    public List<PropertyRecommendationResponse> getRecommendations(Long userId, int limit) {
        var candidates = availableCandidates();
        if (candidates.isEmpty()) {
            return List.of();
        }

        var viewedProperties = propertyViewRepository.findRecentViewedPropertiesByUserId(
                        userId, PageRequest.of(0, RECENT_VIEWS_LIMIT))
                .stream()
                .map(PropertyViewedRow::property)
                .toList();

        log.info("recommendations for userId={}", userId);
        return buildRecommendations(viewedProperties, candidates, limit);
    }

    public List<PropertyRecommendationResponse> getGeneralRecommendations(int limit) {
        var candidates = availableCandidates();
        if (candidates.isEmpty()) {
            return List.of();
        }

        var viewedProperties = propertyViewRepository.findRecentGlobalViews(PageRequest.of(0, RECENT_VIEWS_LIMIT))
                .stream()
                .map(PropertyViewedRow::property)
                .toList();

        log.info("general recommendations based on {} recent global views", viewedProperties.size());
        return buildRecommendations(viewedProperties, candidates, limit);
    }

    public List<PropertyRecommendationResponse> getRecommendationsForGuest(List<Long> viewedPropertyIds, int limit) {
        var candidates = availableCandidates();
        if (candidates.isEmpty()) {
            return List.of();
        }

        var viewedProperties = fetchOrdered(viewedPropertyIds);

        log.info("recommendations for guest, viewedIds={}", viewedPropertyIds);
        return buildRecommendations(viewedProperties, candidates, limit);
    }

    private List<PropertyEntity> availableCandidates() {
        return propertyRepository.findAll().stream()
                .filter(p -> p.getStatus() == PropertyStatus.AVAILABLE)
                .toList();
    }

    // Сохраняет порядок id из запроса (важно - там уже "от новых к старым")
    private List<PropertyEntity> fetchOrdered(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        var found = propertyRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(PropertyEntity::getId, p -> p));

        return ids.stream()
                .map(found::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<PropertyRecommendationResponse> buildRecommendations(
            List<PropertyEntity> viewedProperties,
            List<PropertyEntity> candidates,
            int limit) {

        var profile = buildUserProfile(viewedProperties, candidates);
        var locationAffinity = buildLocationAffinity(viewedProperties, candidates);

        var priceRange = range(candidates, PropertyEntity::getPrice);
        var areaRange = range(candidates, PropertyEntity::getArea);
        var ratingRange = range(candidates, PropertyEntity::getRealtorRating);

        var weights = computeDynamicWeights(viewedProperties, candidates, priceRange, areaRange, ratingRange);

        return candidates.stream()
                .map(p -> {
                    double priceScore = closeness(p.getPrice(), profile.avgPrice(), priceRange);
                    double areaScore = closeness(p.getArea(), profile.avgArea(), areaRange);
                    double ratingScore = closeness(p.getRealtorRating(), profile.avgRating(), ratingRange);
                    double locationScore = locationAffinity.getOrDefault(p.getLocation(), NEUTRAL_LOCATION_AFFINITY);

                    double matchScore = priceScore * weights.price()
                            + areaScore * weights.area()
                            + ratingScore * weights.rating()
                            + locationScore * weights.location();

                    return toResponse(p, matchScore);
                })
                .sorted((a, b) -> Double.compare(b.matchScore(), a.matchScore()))
                .limit(limit)
                .toList();
    }

    private UserProfile buildUserProfile(List<PropertyEntity> viewed, List<PropertyEntity> candidates) {
        var source = viewed.isEmpty() ? candidates : viewed;

        double avgPrice = source.stream().mapToDouble(PropertyEntity::getPrice).average().orElse(0);
        double avgArea = source.stream().mapToDouble(PropertyEntity::getArea).average().orElse(0);
        double avgRating = source.stream().mapToDouble(PropertyEntity::getRealtorRating).average().orElse(0);

        return new UserProfile(avgPrice, avgArea, avgRating);
    }

    private Map<String, Double> buildLocationAffinity(List<PropertyEntity> viewed, List<PropertyEntity> candidates) {
        if (viewed.isEmpty()) {
            return Map.of();
        }

        Set<String> locations = new HashSet<>();
        candidates.forEach(p -> locations.add(p.getLocation()));
        viewed.forEach(p -> locations.add(p.getLocation()));
        int locationCount = locations.size();

        var catalogCounts = candidates.stream()
                .collect(Collectors.groupingBy(PropertyEntity::getLocation, Collectors.counting()));
        long totalCatalog = candidates.size();

        var viewCounts = viewed.stream()
                .collect(Collectors.groupingBy(PropertyEntity::getLocation, Collectors.counting()));
        long totalViews = viewed.size();

        Map<String, Double> affinity = new HashMap<>();
        for (String location : locations) {
            double viewShare = (viewCounts.getOrDefault(location, 0L) + SMOOTHING_ALPHA)
                    / (totalViews + SMOOTHING_ALPHA * locationCount);
            double catalogShare = (catalogCounts.getOrDefault(location, 0L) + SMOOTHING_ALPHA)
                    / (totalCatalog + SMOOTHING_ALPHA * locationCount);

            double lift = viewShare / catalogShare;
            affinity.put(location, lift / (lift + 1));
        }

        return affinity;
    }

    private Weights computeDynamicWeights(List<PropertyEntity> viewed,
                                          List<PropertyEntity> candidates,
                                          double[] priceRange,
                                          double[] areaRange,
                                          double[] ratingRange) {
        var recent = viewed.stream().limit(TREND_WINDOW).toList();

        if (recent.size() < MIN_TREND_SAMPLES) {
            return new Weights(WEIGHT_PRICE, WEIGHT_AREA, WEIGHT_RATING, WEIGHT_LOCATION);
        }

        double priceSignal = numericConsistency(recent, candidates, PropertyEntity::getPrice);
        double areaSignal = numericConsistency(recent, candidates, PropertyEntity::getArea);
        double ratingSignal = numericConsistency(recent, candidates, PropertyEntity::getRealtorRating);
        double locationSignal = locationConsistency(recent);

        double boostedPrice = WEIGHT_PRICE * (1 + TREND_BOOST_FACTOR * priceSignal);
        double boostedArea = WEIGHT_AREA * (1 + TREND_BOOST_FACTOR * areaSignal);
        double boostedRating = WEIGHT_RATING * (1 + TREND_BOOST_FACTOR * ratingSignal);
        double boostedLocation = WEIGHT_LOCATION * (1 + TREND_BOOST_FACTOR * locationSignal);

        double total = boostedPrice + boostedArea + boostedRating + boostedLocation;

        return new Weights(
                boostedPrice / total,
                boostedArea / total,
                boostedRating / total,
                boostedLocation / total
        );
    }

    private double numericConsistency(List<PropertyEntity> recent,
                                      List<PropertyEntity> candidates,
                                      ToDoubleFunction<PropertyEntity> extractor) {
        double catalogStdDev = stdDevOf(candidates, extractor);
        if (catalogStdDev == 0) {
            return 0;
        }

        double viewedStdDev = stdDevOf(recent, extractor);
        return clamp(1 - (viewedStdDev / catalogStdDev));
    }

    private double stdDevOf(List<PropertyEntity> list, ToDoubleFunction<PropertyEntity> extractor) {
        double mean = list.stream().mapToDouble(extractor).average().orElse(0);
        double variance = list.stream()
                .mapToDouble(p -> {
                    double diff = extractor.applyAsDouble(p) - mean;
                    return diff * diff;
                })
                .average().orElse(0);
        return Math.sqrt(variance);
    }

    private double locationConsistency(List<PropertyEntity> recent) {
        Map<String, Long> counts = recent.stream()
                .collect(Collectors.groupingBy(PropertyEntity::getLocation, Collectors.counting()));
        long maxCount = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        return (double) maxCount / recent.size();
    }

    private double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private double[] range(List<PropertyEntity> candidates, ToDoubleFunction<PropertyEntity> extractor) {
        double min = candidates.stream().mapToDouble(extractor).min().orElse(0);
        double max = candidates.stream().mapToDouble(extractor).max().orElse(0);
        return new double[]{min, max};
    }

    private double closeness(double value, double avg, double[] range) {
        double span = range[1] - range[0];
        if (span == 0) {
            return 1.0;
        }
        double diff = Math.abs(value - avg);
        return Math.max(0, 1 - diff / span);
    }

    private PropertyRecommendationResponse toResponse(PropertyEntity entity, double matchScore) {
        return new PropertyRecommendationResponse(
                entity.getId(), entity.getTitle(), entity.getDescription(), entity.getImgUrl(),
                entity.getPrice(), entity.getArea(), entity.getLocation(), entity.getRealtorRating(),
                entity.getStatus(), entity.getCreatedAt(),
                Math.round(matchScore * 1000.0) / 1000.0
        );
    }

    private record UserProfile(double avgPrice, double avgArea, double avgRating) {}
    private record Weights(double price, double area, double rating, double location) {}
}