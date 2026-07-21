package com.example.propertiesoffice.property;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PropertyViewRepository extends JpaRepository<PropertyViewEntity, Long> {

    @Query("""
            select new com.example.propertiesoffice.property.PropertyViewedRow(pe, pv.viewedAt)
            from PropertyViewEntity pv
            join PropertyEntity pe on pe.id = pv.propertyId
            where pv.userId = :userId
            order by pv.viewedAt desc
            """)
    List<PropertyViewedRow> findRecentViewedPropertiesByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        select new com.example.propertiesoffice.property.PropertyViewedRow(pe, pv.viewedAt)
        from PropertyViewEntity pv
        join PropertyEntity pe on pe.id = pv.propertyId
        order by pv.viewedAt desc
        """)
    List<PropertyViewedRow> findRecentGlobalViews(Pageable pageable);
}