package com.incubyte.sweetshop.sweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SweetRepository extends JpaRepository<Sweet, Long> {

    @Query("SELECT s FROM Sweet s WHERE " +
            "(:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) AND " +
            "(:category IS NULL OR LOWER(s.category) LIKE LOWER(CONCAT('%', CAST(:category AS string), '%'))) AND " +
            "(:maxPrice IS NULL OR s.price <= :maxPrice)")
    List<Sweet> searchSweets(
            @Param("name") String name,
            @Param("category") String category,
            @Param("maxPrice") BigDecimal maxPrice
    );
}