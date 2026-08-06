package com.example.propertiesoffice.property;

import com.example.propertiesoffice.user.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "property_views")
public class PropertyViewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private PropertyEntity property;

    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    public PropertyViewEntity() {
    }

    public PropertyViewEntity(UserEntity user, PropertyEntity property, LocalDateTime viewedAt) {
        this.user = user;
        this.property = property;
        this.viewedAt = viewedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    public PropertyEntity getProperty() { return property; }
    public void setProperty(PropertyEntity property) { this.property = property; }

    public LocalDateTime getViewedAt() { return viewedAt; }
    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
}