package com.example.propertiesoffice.property;

import java.time.LocalDateTime;

public record PropertyViewedRow(PropertyEntity property, LocalDateTime viewedAt) {
}