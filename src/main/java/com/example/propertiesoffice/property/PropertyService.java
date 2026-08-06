package com.example.propertiesoffice.property;

import com.example.propertiesoffice.user.UserEntity;
import com.example.propertiesoffice.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PropertyService {
    private final Logger log = LoggerFactory.getLogger(PropertyService.class);

    private final PropertyRepository repository;
    private final PropertyViewRepository viewRepository;
    private final UserRepository userRepository;

    public PropertyService(PropertyRepository repository,
                           PropertyViewRepository viewRepository,
                           UserRepository userRepository) {
        this.repository = repository;
        this.viewRepository = viewRepository;
        this.userRepository = userRepository;
    }

    public List<Property> getAllProperties() {
        log.info("getAllProperties");
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    public Property getPropertyById(Long id, Long uid) {
        log.info("getPropertyById");
        var entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Property not found"));

        if (uid != null) logView(uid, entity);

        return toDomain(entity);
    }

    public Property createProperty(PropertyRequest request) {
        log.info("createProperty");
        var entity = new PropertyEntity(
                null,
                request.title(),
                request.description(),
                request.imgUrl(),
                request.price(),
                request.area(),
                request.location(),
                request.realtorRating(),
                PropertyStatus.AVAILABLE,
                LocalDateTime.now().withNano(0)
        );

        var createdEntity = repository.save(entity);
        return toDomain(createdEntity);
    }

    public Property updateProperty(PropertyRequest request, Long id) {
        log.info("updateProperty");
        var entity = new PropertyEntity(
                id,
                request.title(),
                request.description(),
                request.imgUrl(),
                request.price(),
                request.area(),
                request.location(),
                request.realtorRating(),
                PropertyStatus.AVAILABLE,
                LocalDateTime.now().withNano(0)
        );

        var createdEntity = repository.save(entity);
        return toDomain(createdEntity);
    }

    public void deleteProperty(Long id) {
        log.info("deleteProperty");
        repository.deleteById(id);
    }

    private Property toDomain(PropertyEntity entity) {
        return new Property(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getImgUrl(),
                entity.getPrice(),
                entity.getArea(),
                entity.getLocation(),
                entity.getRealtorRating(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }

    private void logView(Long userId, PropertyEntity propertyEntity) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        var view = new PropertyViewEntity(userEntity, propertyEntity, LocalDateTime.now().withNano(0));
        viewRepository.save(view);
    }
}