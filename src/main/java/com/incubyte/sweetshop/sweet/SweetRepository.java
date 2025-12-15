package com.incubyte.sweetshop.sweet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SweetRepository extends JpaRepository<Sweet, Long> {
    // Spring Data JPA magic: automatically creates the query!
    List<Sweet> findByNameContainingIgnoreCase(String name);
}