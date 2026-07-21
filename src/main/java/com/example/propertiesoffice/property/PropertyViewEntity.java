package com.example.propertiesoffice.property;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Table(name = "property_views")
@Entity
public class PropertyViewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "property_id")
    private Long propertyId;

    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    public PropertyViewEntity() {
    }

    public PropertyViewEntity(Long id, Long userId, Long propertyId, LocalDateTime viewedAt) {
        this.id = id;
        this.userId = userId;
        this.propertyId = propertyId;
        this.viewedAt = viewedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getPropertyId() { return propertyId; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }

    public LocalDateTime getViewedAt() { return viewedAt; }
    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
}