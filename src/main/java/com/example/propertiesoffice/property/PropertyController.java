package com.example.propertiesoffice.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/properties")
public class PropertyController {
    private final Logger log = LoggerFactory.getLogger(PropertyController.class);

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping()
    public ResponseEntity<List<PropertyResponse>> getAllProperties() {
        log.info("getAllProperties");
        var response = propertyService.getAllProperties().stream().map(this::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable("id") Long id) {
        log.info("getPropertyById");
        var response = propertyService.getPropertyById(id);
        return ResponseEntity.ok(toResponse(response));
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<PropertyResponse> updateProperty(@PathVariable("id") Long id, @RequestBody PropertyRequest request) {
        log.info("updateProperty");
        var property = propertyService.updateProperty(request,id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(toResponse(property));
    }

    @PostMapping("/create")
    public ResponseEntity<PropertyResponse> createProperty(@RequestBody PropertyRequest request) {
        log.info("createProperty");
        var property = propertyService.createProperty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(property));
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteProperty(@PathVariable("id") Long id) {
        log.info("deleteProperty");
        propertyService.deleteProperty(id);
        return ResponseEntity.ok().build();
    }

    private PropertyResponse toResponse(Property property) {
        return new PropertyResponse(
                property.id(),
                property.title(),
                property.description(),
                property.imgUrl(),
                property.price(),
                property.area(),
                property.location(),
                property.realtorRating(),
                property.status(),
                property.createdAt()
        );
    }
}