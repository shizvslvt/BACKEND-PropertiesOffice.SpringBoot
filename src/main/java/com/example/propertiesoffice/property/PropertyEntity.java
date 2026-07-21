package com.example.propertiesoffice.property;

import jakarta.persistence.*;

        import java.time.LocalDateTime;

@Table(name = "properties")
@Entity
public class PropertyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "price")
    private Integer price;

    @Column(name = "area")
    private Double area;

    @Column(name = "location")
    private String location;

    @Column(name = "realtor_rating")
    private Double realtorRating;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PropertyStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public PropertyEntity() {
    }

    public PropertyEntity(Long id, String title, String description, String imgUrl,
                          Integer price, Double area, String location,
                          Double realtorRating, PropertyStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imgUrl = imgUrl;
        this.price = price;
        this.area = area;
        this.location = location;
        this.realtorRating = realtorRating;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public Double getArea() { return area; }
    public void setArea(Double area) { this.area = area; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getRealtorRating() { return realtorRating; }
    public void setRealtorRating(Double realtorRating) { this.realtorRating = realtorRating; }

    public PropertyStatus getStatus() { return status; }
    public void setStatus(PropertyStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}